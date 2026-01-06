package com.example.WorkWite_Repo_BE.controlers;

import com.example.WorkWite_Repo_BE.services.VnpayPaymentService;
import com.example.WorkWite_Repo_BE.config.payment.VNPAYConfig;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/payments")
public class VnpayPaymentController {

    @Autowired
    private VnpayPaymentService vnpayPaymentService;

    @Autowired
    private VNPAYConfig config;

    // ========== VNPAY ENDPOINTS ==========

    @GetMapping("/vnpay/create-payment")
    public ResponseEntity<?> createVnpayPayment(@RequestParam(name = "amount") long amount,
                                                @RequestParam(name = "userId") Long userId,
                                                @RequestParam(name = "orderInfo", defaultValue = "Nap tien ao") String orderInfo,
                                                HttpServletRequest request) {
        try {
            log.info("VNPay payment request: userId={}, amount={}", userId, amount);

            if (amount <= 0) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Amount must be greater than 0"));
            }

            if (userId == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "UserId is required"));
            }

            String result = vnpayPaymentService.createPayment(amount, userId, orderInfo, request);

            if (result.startsWith("Error:")) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", result));
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "paymentUrl", result,
                    "message", "VNPay payment URL created"
            ));
        } catch (Exception e) {
            log.error("Error creating VNPay payment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error: " + e.getMessage()));
        }
    }

    @GetMapping("/vnpay/confirm")
    public String vnpayConfirm(@RequestParam Map<String, String> params) {
        log.info("VNPay callback received");
        return vnpayPaymentService.vnpayReturn(params);
    }

    // ========== PAYPAL ENDPOINTS ==========

    @GetMapping("/paypal/create-payment")
    public ResponseEntity<?> createPaypalOrder(@RequestParam(name = "amount") double amountVND,
                                               @RequestParam(name = "userId") Long userId,
                                               @RequestParam(name = "orderInfo", defaultValue = "Nap tien PayPal") String description) {
        try {
            log.info("PayPal payment request: userId={}, amount={}", userId, amountVND);

            if (amountVND <= 0) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Amount must be greater than 0"));
            }

            if (userId == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "UserId is required"));
            }

            double amountUSD = convertVNDtoUSD(amountVND);

            // Get PayPal access token
            RestTemplate restTemplate = new RestTemplate();
            String auth = config.getPaypalClientId() + ":" + config.getPaypalSecret();
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Basic " + encodedAuth);
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<String> tokenRequest = new HttpEntity<>("grant_type=client_credentials", headers);
            ResponseEntity<Map> tokenResponse = restTemplate.exchange(
                    config.getPaypalApi() + "/v1/oauth2/token", HttpMethod.POST, tokenRequest, Map.class
            );
            String accessToken = tokenResponse.getBody().get("access_token").toString();

            // Create custom_id
            String transactionId = String.valueOf(System.currentTimeMillis());
            String safeDesc = URLEncoder.encode(description, StandardCharsets.UTF_8);
            String rawInfo = "WALLET|" + userId + "|" + transactionId + "|" + amountVND + "|" + safeDesc;
            String customId = Base64.getUrlEncoder().encodeToString(rawInfo.getBytes(StandardCharsets.UTF_8));

            // Create PayPal order
            headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> purchaseUnit = new HashMap<>();
            purchaseUnit.put("amount", Map.of("currency_code", "USD", "value", String.format(Locale.US, "%.2f", amountUSD)));
            purchaseUnit.put("description", description);
            purchaseUnit.put("custom_id", customId);

            Map<String, Object> orderBody = Map.of(
                    "intent", "CAPTURE",
                    "purchase_units", List.of(purchaseUnit),
                    "application_context", Map.of("return_url", config.getPaypalReturnUrl(), "cancel_url", config.getPaypalReturnUrl())
            );

            HttpEntity<Map<String, Object>> orderRequest = new HttpEntity<>(orderBody, headers);
            ResponseEntity<Map> orderResponse = restTemplate.postForEntity(
                    config.getPaypalApi() + "/v2/checkout/orders", orderRequest, Map.class
            );

            List<Map<String, String>> links = (List<Map<String, String>>) orderResponse.getBody().get("links");
            String approveUrl = links.stream()
                    .filter(link -> "approve".equals(link.get("rel")))
                    .findFirst()
                    .map(link -> link.get("href"))
                    .orElse(null);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "orderId", orderResponse.getBody().get("id"),
                    "approveUrl", approveUrl,
                    "amountVND", amountVND,
                    "amountUSD", amountUSD
            ));
        } catch (Exception e) {
            log.error("PayPal error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "PayPal error: " + e.getMessage()));
        }
    }

    @GetMapping("/paypal/confirm")
    public ResponseEntity<?> capturePaypalOrder(@RequestParam Map<String, String> params) {
        try {
            String orderId = params.getOrDefault("token", params.get("orderId"));
            if (orderId == null || orderId.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Missing order id"));
            }

            // Get access token
            RestTemplate restTemplate = new RestTemplate();
            String auth = config.getPaypalClientId() + ":" + config.getPaypalSecret();
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Basic " + encodedAuth);
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<String> tokenRequest = new HttpEntity<>("grant_type=client_credentials", headers);
            ResponseEntity<Map> tokenResponse = restTemplate.exchange(
                    config.getPaypalApi() + "/v1/oauth2/token", HttpMethod.POST, tokenRequest, Map.class
            );
            String accessToken = tokenResponse.getBody().get("access_token").toString();

            // Get order details
            headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<?> orderDetailsReq = new HttpEntity<>(headers);
            ResponseEntity<Map> orderDetailsRes = restTemplate.exchange(
                    config.getPaypalApi() + "/v2/checkout/orders/" + orderId, HttpMethod.GET, orderDetailsReq, Map.class
            );

            Map orderBody = orderDetailsRes.getBody();
            String status = orderBody != null ? orderBody.get("status").toString() : null;

            if (!"APPROVED".equalsIgnoreCase(status)) {
                return ResponseEntity.ok(Map.of("success", false, "message", "Payment not approved"));
            }

            // Capture order
            HttpEntity<?> captureRequest = new HttpEntity<>(headers);
            ResponseEntity<Map> captureResponse = restTemplate.postForEntity(
                    config.getPaypalApi() + "/v2/checkout/orders/" + orderId + "/capture", captureRequest, Map.class
            );

            // Get customId and save transaction
            String customId = null;
            if (orderBody.get("purchase_units") instanceof List) {
                List puList = (List) orderBody.get("purchase_units");
                if (!puList.isEmpty() && puList.get(0) instanceof Map) {
                    Object c = ((Map) puList.get(0)).get("custom_id");
                    if (c != null) customId = c.toString();
                }
            }

            if (customId != null) {
                String decoded = new String(Base64.getUrlDecoder().decode(customId), StandardCharsets.UTF_8);
                String[] parts = decoded.split("\\|");

                if (parts.length >= 4) {
                    Long userId = Long.parseLong(parts[1]);
                    String txnCode = parts[2];
                    double amountVND = Double.parseDouble(parts[3]);
                    String desc = parts.length > 4 ? URLDecoder.decode(parts[4], StandardCharsets.UTF_8) : "";

                    vnpayPaymentService.savePayPalTransaction(userId, txnCode, amountVND, desc, "PayPal", orderId);
                }
            }

            return ResponseEntity.ok(Map.of("success", true, "paypalResponse", captureResponse.getBody()));
        } catch (Exception e) {
            log.error("PayPal capture error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error: " + e.getMessage()));
        }
    }

    // ========== TRANSACTION HISTORY ==========

    @GetMapping("/transactions/{userId}")
    public ResponseEntity<?> getTransactionHistory(@PathVariable Long userId) {
        try {
            log.info("Get transaction history for userId={}", userId);

            if (userId == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "UserId is required"
                ));
            }

            List<com.example.WorkWite_Repo_BE.dtos.TransactionHistoryResponse> transactions =
                    vnpayPaymentService.getTransactionHistory(userId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Transaction history retrieved successfully",
                    "data", transactions,
                    "total", transactions.size()
            ));
        } catch (Exception e) {
            log.error("Error getting transaction history", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Internal server error: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/transactions/detail/{txnRef}")
    public ResponseEntity<?> getTransactionDetail(@PathVariable String txnRef) {
        try {
            log.info("Get transaction detail for txnRef={}", txnRef);

            var transaction = vnpayPaymentService.getTransactionByTxnRef(txnRef);

            if (transaction == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Transaction found",
                    "data", transaction
            ));
        } catch (Exception e) {
            log.error("Error getting transaction detail", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Internal server error: " + e.getMessage()
            ));
        }
    }

    // ========== HELPER METHODS ==========

    private double convertVNDtoUSD(double amountVND) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = "https://api.exchangerate-api.com/v4/latest/VND";
            ResponseEntity<Map> rateRes = restTemplate.getForEntity(url, Map.class);
            Map rateBody = rateRes.getBody();

            if (rateBody != null && rateBody.get("rates") != null) {
                Map<String, Object> rates = (Map<String, Object>) rateBody.get("rates");
                double usdRate = ((Number) rates.get("USD")).doubleValue();
                double amountUSD = amountVND * usdRate;
                return Math.round(amountUSD * 100.0) / 100.0;
            }
        } catch (Exception e) {
            log.error("Exchange rate API error: {}", e.getMessage());
        }
        return Math.round((amountVND / 25000.0) * 100.0) / 100.0;
    }

}


