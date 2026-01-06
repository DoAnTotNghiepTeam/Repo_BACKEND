package com.example.WorkWite_Repo_BE.services;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import com.example.WorkWite_Repo_BE.entities.PaymentTransaction;
import com.example.WorkWite_Repo_BE.repositories.PaymentTransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;


@Slf4j
@RequiredArgsConstructor
@Service
public class VnpayPaymentService {

	private final com.example.WorkWite_Repo_BE.config.payment.VNPAYConfig vnpayConfig;
	private final com.example.WorkWite_Repo_BE.services.UserBalanceService userBalanceService;
	private final PaymentTransactionRepository paymentTransactionRepository;
	private final com.example.WorkWite_Repo_BE.repositories.UserJpaRepository userJpaRepository;

	/**
	 * TẠO URL THANH TOÁN (Theo phong cách Ants-KTC)
	 */
	public String createPayment(long amount, Long userId, String orderInfo, jakarta.servlet.http.HttpServletRequest request) {
		try {
			// 1. Kiểm tra điều kiện đầu vào
			if (amount <= 0) return "Error: Amount must be greater than 0";
			if (userId == null || !userJpaRepository.existsById(userId)) return "Error: User not found";

			String transactionId = com.example.WorkWite_Repo_BE.config.payment.VNPAYConfig.getRandomNumber(8);

			// 2. Lưu giao dịch PENDING vào database
			PaymentTransaction transaction = PaymentTransaction.builder()
					.txnRef(transactionId)
					.userId(userId)
					.amount(amount)
					.status("PENDING")
					.orderInfo(orderInfo)
					.ipAddress(getIpAddress(request))
					.build();
			paymentTransactionRepository.save(transaction);

			// 3. Thiết lập các tham số cho VNPAY
			Map<String, String> vnp_Params = vnpayConfig.getVNPayBaseParams();
			vnp_Params.put("vnp_Amount", String.valueOf(amount * 100));
			vnp_Params.put("vnp_TxnRef", transactionId);

			// Đóng gói OrderInfo bằng Base64 để tránh lỗi ký tự đặc biệt/tiếng Việt
			String safeDescription = URLEncoder.encode(orderInfo, StandardCharsets.UTF_8);
			String rawInfo = "WALLET|" + userId + "|" + transactionId + "|" + safeDescription;
			String base64OrderInfo = Base64.getUrlEncoder().encodeToString(rawInfo.getBytes(StandardCharsets.UTF_8));
			vnp_Params.put("vnp_OrderInfo", base64OrderInfo);

			String ipAddr = getIpAddress(request);
			if (ipAddr.equals("0:0:0:0:0:0:0:1")) ipAddr = "127.0.0.1";
			vnp_Params.put("vnp_IpAddr", ipAddr);

			// 4. Xây dựng Query và HashData (Sắp xếp Alphabet)
			List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
			Collections.sort(fieldNames);

			StringBuilder hashData = new StringBuilder();
			StringBuilder query = new StringBuilder();

			for (String fieldName : fieldNames) {
				String fieldValue = vnp_Params.get(fieldName);
				if ((fieldValue != null) && (fieldValue.length() > 0)) {
					// Sử dụng US_ASCII giống dự án Ants-KTC
					String encodedKey = URLEncoder.encode(fieldName, StandardCharsets.US_ASCII);
					String encodedValue = URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII);

					hashData.append(encodedKey).append('=').append(encodedValue).append('&');
					query.append(encodedKey).append('=').append(encodedValue).append('&');
				}
			}

			// Loại bỏ dấu '&' thừa ở cuối
			hashData.setLength(hashData.length() - 1);
			query.setLength(query.length() - 1);

			// 5. Tạo chữ ký (Dùng chuỗi đã encode và thay + bằng %20)
			String finalHashData = hashData.toString().replace("+", "%20");
			String finalQuery = query.toString().replace("+", "%20");

			String secureHash = hmacSHA512(vnpayConfig.getSecretKey(), finalHashData);

			// LOG DEBUG
			log.info("--- VNPAY DEBUG (ANTS STYLE) ---");
			log.info("Raw Hash Data: {}", finalHashData);
			log.info("Secure Hash: {}", secureHash);

			return vnpayConfig.getVnp_PayUrl() + "?" + finalQuery + "&vnp_SecureHash=" + secureHash;

		} catch (Exception e) {
			log.error("Lỗi tạo link VNPAY", e);
			return "Error: " + e.getMessage();
		}
	}

	/**
	 * XỬ LÝ KẾT QUẢ TRẢ VỀ (CONFIRM/RETURN)
	 */
	@Transactional
	public String vnpayReturn(Map<String, String> params) {
		try {
			log.info("VNPay Callback nhận được: {}", params);

			// 1. Kiểm tra chữ ký bằng hàm verify riêng
			if (!verifySignature(params, vnpayConfig.getSecretKey())) {
				log.error("Sai chữ ký xác thực từ VNPAY!");
				return generateRedirectScript(vnpayConfig.getFrontendFailUrl() + "?error=InvalidSignature");
			}

			// 2. Giải mã thông tin từ vnp_OrderInfo (Base64)
			String orderInfoBase64 = params.get("vnp_OrderInfo");
			String decodedInfo = new String(Base64.getUrlDecoder().decode(orderInfoBase64), StandardCharsets.UTF_8);
			String[] parts = decodedInfo.split("\\|");

			// Format: WALLET | userId | transactionId | description
			Long userId = Long.parseLong(parts[1]);
			String txnRef = params.get("vnp_TxnRef");
			String responseCode = params.get("vnp_ResponseCode");

			// 3. Kiểm tra xem giao dịch đã xử lý thành công trước đó chưa
			PaymentTransaction transaction = paymentTransactionRepository.findByTxnRef(txnRef).orElse(null);
			if (transaction != null && !"PENDING".equals(transaction.getStatus())) {
				return generateRedirectScript(vnpayConfig.getFrontendSuccessUrl() + "?status=already_processed");
			}

			// 4. Xử lý logic thành công/thất bại
			if ("00".equals(responseCode)) {
				long amount = Long.parseLong(params.get("vnp_Amount")) / 100;

				// Cộng tiền vào số dư User
				userBalanceService.addBalance(userId, amount);

				// Cập nhật trạng thái DB
				updateTransactionStatus(txnRef, "SUCCESS", responseCode, params);
				log.info("Thanh toán thành công cho User ID: {}, Số tiền: {}", userId, amount);

				return generateRedirectScript(vnpayConfig.getFrontendSuccessUrl() + "?amount=" + amount);
			}

			// Giao dịch thất bại
			updateTransactionStatus(txnRef, "FAILED", responseCode, params);
			return generateRedirectScript(vnpayConfig.getFrontendFailUrl() + "?code=" + responseCode);

		} catch (Exception e) {
			log.error("Lỗi xử lý VNPAY Return", e);
			return generateRedirectScript(vnpayConfig.getFrontendFailUrl() + "?error=SystemError");
		}
	}

	// ========== HÀM TIỆN ÍCH THEO PHONG CÁCH ANTS-KTC ==========

	public boolean verifySignature(Map<String, String> vnpParams, String secret) {
		try {
			String vnpSecureHash = vnpParams.get("vnp_SecureHash");
			if (vnpSecureHash == null) return false;

			Map<String, String> data = new HashMap<>(vnpParams);
			data.remove("vnp_SecureHash");
			data.remove("vnp_SecureHashType");

			List<String> fieldNames = new ArrayList<>(data.keySet());
			Collections.sort(fieldNames);

			StringBuilder hashData = new StringBuilder();
			for (String fieldName : fieldNames) {
				String fieldValue = data.get(fieldName);
				if (fieldValue != null && fieldValue.length() > 0) {
					hashData.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII))
							.append("=")
							.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII))
							.append("&");
				}
			}
			hashData.setLength(hashData.length() - 1);

			String calcHash = hmacSHA512(secret, hashData.toString().replace("+", "%20"));
			return vnpSecureHash.equalsIgnoreCase(calcHash);
		} catch (Exception e) {
			return false;
		}
	}

	public String hmacSHA512(String key, String data) throws Exception {
		Mac hmac512 = Mac.getInstance("HmacSHA512");
		SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
		hmac512.init(secretKey);
		byte[] bytes = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));

		// Sử dụng HexFormat (Yêu cầu Java 17+) giống Ants-KTC
		return java.util.HexFormat.of().formatHex(bytes);
	}

	private String generateRedirectScript(String url) {
		return "<script>window.location.href='" + url + "';</script>";
	}

	private String getIpAddress(jakarta.servlet.http.HttpServletRequest request) {
		String ip = request.getHeader("X-Forwarded-For");
		return (ip == null) ? request.getRemoteAddr() : ip;
	}

	private void updateTransactionStatus(String txnRef, String status, String code, Map<String, String> data) {
		paymentTransactionRepository.findByTxnRef(txnRef).ifPresent(txn -> {
			try {
				txn.setStatus(status);
				txn.setResponseCode(code);
				txn.setTransactionNo(data.get("vnp_TransactionNo"));
				txn.setBankCode(data.get("vnp_BankCode"));
				txn.setVnpayData(new ObjectMapper().writeValueAsString(data));
				paymentTransactionRepository.save(txn);
			} catch (Exception e) {
				log.error("Lỗi cập nhật trạng thái giao dịch: {}", txnRef);
			}
		});
	}

	// ========== CÁC HÀM LỊCH SỬ GIAO DỊCH ==========

	public List<com.example.WorkWite_Repo_BE.dtos.TransactionHistoryResponse> getTransactionHistory(Long userId) {
		return paymentTransactionRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
				.map(txn -> com.example.WorkWite_Repo_BE.dtos.TransactionHistoryResponse.builder()
						.id(txn.getId())
						.txnRef(txn.getTxnRef())
						.amount(txn.getAmount())
						.status(txn.getStatus())
						.responseCode(txn.getResponseCode())
						.orderInfo(txn.getOrderInfo())
						.createdAt(txn.getCreatedAt())
						.build())
				.collect(Collectors.toList());
	}

	public com.example.WorkWite_Repo_BE.dtos.TransactionHistoryResponse getTransactionByTxnRef(String txnRef) {
		return paymentTransactionRepository.findByTxnRef(txnRef)
				.map(txn -> com.example.WorkWite_Repo_BE.dtos.TransactionHistoryResponse.builder()
						.id(txn.getId())
						.txnRef(txn.getTxnRef())
						.amount(txn.getAmount())
						.status(txn.getStatus())
						.orderInfo(txn.getOrderInfo())
						.createdAt(txn.getCreatedAt())
						.build())
				.orElse(null);
	}

	/**
	 * LƯU GIAO DỊCH PAYPAL (Giữ nguyên logic của bạn)
	 */
	@Transactional
	public void savePayPalTransaction(Long userId, String txnCode, double amountVND, String description, String bankName, String orderId) {
		try {
			if (paymentTransactionRepository.existsByTxnRef(txnCode)) return;

			PaymentTransaction transaction = PaymentTransaction.builder()
					.txnRef(txnCode).userId(userId).amount((long) amountVND)
					.status("SUCCESS").responseCode("00").orderInfo(description)
					.transactionNo(orderId).bankCode(bankName).build();

			paymentTransactionRepository.save(transaction);
			userBalanceService.addBalance(userId, (long) amountVND);
		} catch (Exception e) {
			log.error("Lỗi lưu giao dịch PayPal", e);
		}
	}
}