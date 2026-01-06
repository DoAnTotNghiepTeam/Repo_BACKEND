package com.example.WorkWite_Repo_BE.helpers;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class EmailTemplateHelper {

    // ✅ Mail xác nhận ứng tuyển cho ứng viên
    public String buildApplySuccessEmail(String candidateName, String jobTitle, String companyName, Long applicantId) {
        return """
    <html>
    <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
        <div style="max-width: 600px; margin: auto; border: 1px solid #eee; border-radius: 8px; overflow: hidden;">
            <div style="background-color: #4CAF50; color: white; padding: 16px; text-align: center;">
                <h2 style="margin: 0;">✅ Xác nhận ứng tuyển thành công</h2>
            </div>
            <div style="padding: 20px;">
                <p>Xin chào <b>%s</b>,</p>
                <p>Bạn đã ứng tuyển thành công vào công việc:</p>
                <div style="background-color: #f9f9f9; padding: 15px; border-left: 4px solid #4CAF50; margin: 15px 0;">
                    <p style="margin: 5px 0; font-size: 18px; font-weight: bold; color: #333;">%s</p>
                    <p style="margin: 5px 0; color: #666;"><strong>Công ty:</strong> %s</p>
                </div>
                <p>Đơn ứng tuyển của bạn đã được gửi tới nhà tuyển dụng. Vui lòng theo dõi trạng thái đơn ứng tuyển trong hệ thống.</p>
                <div style="text-align: center; margin: 30px 0;">
                    <a href="http://localhost:3000/candidate-profile?tab=apply&page=1"
                       style="display:inline-block; padding: 12px 24px; background-color: #4CAF50; 
                              color: white; text-decoration: none; font-size: 16px; border-radius: 6px;">
                       Xem chi tiết đơn ứng tuyển
                    </a>
                </div>
                <p style="font-size: 14px; color: #999;">Trân trọng,<br>Đội ngũ WorkWite</p>
            </div>
            <div style="background-color: #f5f5f5; padding: 10px; text-align: center; font-size: 12px; color: #777;">
                Đây là email tự động, vui lòng không trả lời.
            </div>
        </div>
    </body>
    </html>
    """.formatted(candidateName, jobTitle, companyName, applicantId);
    }

    // ✅ Mail thông báo cho Employer khi có ứng viên mới
    public String buildNewApplicantEmail(String employerName, String jobTitle, String candidateName, Long applicantId) {
        return """
        <html>
        <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
            <div style="max-width: 600px; margin: auto; border: 1px solid #eee; border-radius: 8px; overflow: hidden;">
                <div style="background-color: #2196F3; color: white; padding: 16px; text-align: center;">
                    <h2 style="margin: 0;">Có ứng viên mới ứng tuyển</h2>
                </div>
                <div style="padding: 20px;">
                    <p>Xin chào <b>%s</b>,</p>
                    <p>Ứng viên <b>%s</b> vừa ứng tuyển vào công việc <b>%s</b>.</p>
                    <p>Vui lòng đăng nhập để xem hồ sơ và xử lý ứng viên.</p>
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="http://localhost:3000/employer/applicants/%d" 
                           style="display:inline-block; padding: 12px 24px; background-color: #2196F3; 
                                  color: white; text-decoration: none; font-size: 16px; border-radius: 6px;">
                           Xem ứng viên
                        </a>
                    </div>
                    <p style="font-size: 14px; color: #999;">Trân trọng,<br>Đội ngũ WorkWite</p>
                </div>
                <div style="background-color: #f5f5f5; padding: 10px; text-align: center; font-size: 12px; color: #777;">
                    Đây là email tự động, vui lòng không trả lời.
                </div>
            </div>
        </body>
        </html>
        """.formatted(employerName, candidateName, jobTitle, applicantId);
    }

    // ✅ Mail khi trạng thái ứng tuyển thay đổi
    public String buildStatusUpdateEmail(String candidateName, String jobTitle, String companyName, String status, String note, Long applicantId) {
        return """
        <html>
        <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
            <div style="max-width: 600px; margin: auto; border: 1px solid #eee; border-radius: 8px; overflow: hidden;">
                <div style="background-color: #FF9800; color: white; padding: 16px; text-align: center;">
                    <h2 style="margin: 0;">🔔 Cập nhật trạng thái ứng tuyển</h2>
                </div>
                <div style="padding: 20px;">
                    <p>Xin chào <b>%s</b>,</p>
                    <p>Đơn ứng tuyển của bạn đã được cập nhật trạng thái:</p>
                    <div style="background-color: #f9f9f9; padding: 15px; border-left: 4px solid #FF9800; margin: 15px 0;">
                        <p style="margin: 5px 0; font-size: 16px; font-weight: bold;">%s</p>
                        <p style="margin: 5px 0; color: #666;"><strong>Công ty:</strong> %s</p>
                        <p style="font-size: 18px; font-weight: bold; color: #FF9800; margin: 10px 0;">Trạng thái: %s</p>
                    </div>
                    <p><b>Ghi chú từ nhà tuyển dụng:</b> %s</p>
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="http://localhost:3000/candidate-profile?tab=apply&page=1" 
                           style="display:inline-block; padding: 12px 24px; background-color: #FF9800; 
                                  color: white; text-decoration: none; font-size: 16px; border-radius: 6px;">
                           Truy cập vào để xem chi tiết 
                        </a>
                    </div>
                    <p style="font-size: 14px; color: #999;">Trân trọng,<br>Đội ngũ WorkWite</p>
                </div>
                <div style="background-color: #f5f5f5; padding: 10px; text-align: center; font-size: 12px; color: #777;">
                    Đây là email tự động, vui lòng không trả lời.
                </div>
            </div>
        </body>
        </html>
        """.formatted(candidateName, jobTitle, companyName, status, note != null ? note : "(Không có ghi chú)", applicantId);
    }
    public String buildInterviewScheduleEmail(String candidateName, String jobTitle,
                                              LocalDateTime time, String location, String interviewer) {
        return """
    <html>
    <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
        <div style="max-width: 600px; margin: auto; border: 1px solid #eee; border-radius: 8px; overflow: hidden;">
            <div style="background-color: #673AB7; color: white; padding: 16px; text-align: center;">
                <h2 style="margin: 0;">Thư mời phỏng vấn</h2>
            </div>
            <div style="padding: 20px;">
                <p>Xin chào <b>%s</b>,</p>
                <p>Bạn đã được mời phỏng vấn cho vị trí <b>%s</b>.</p>
                <p><b>Thời gian:</b> %s</p>
                <p><b>Địa điểm:</b> %s</p>
                <p><b>Người phỏng vấn:</b> %s</p>

                                    <div style="text-align: center; margin: 30px 0;">
                        <a href="http://localhost:3000/candidate-profile?tab=apply&page=1" 
                           style="display:inline-block; padding: 12px 24px; background-color:  #673AB7; 
                                  color: white; text-decoration: none; font-size: 16px; border-radius: 6px;">
                           Truy cập vào để xem chi tiết 
                        </a>
                    </div>
                <p style="font-size: 14px; color: #999;">Chúc bạn may mắn!<br>Đội ngũ WorkWite</p>
            </div>
            <div style="background-color: #f5f5f5; padding: 10px; text-align: center; font-size: 12px; color: #777;">
                Đây là email tự động, vui lòng không trả lời.
            </div>
        </div>
    </body>
    </html>
    """.formatted(candidateName, jobTitle, time, location, interviewer);
    }

    public String buildInterviewReminderEmail(String candidateName, String jobTitle,
                                              LocalDateTime time, String location) {
        return """
    <html>
    <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
        <div style="max-width: 600px; margin: auto; border: 1px solid #eee; border-radius: 8px; overflow: hidden;">
            <div style="background-color: #FF5722; color: white; padding: 16px; text-align: center;">
                <h2 style="margin: 0;">Nhắc nhở lịch phỏng vấn</h2>
            </div>
            <div style="padding: 20px;">
                <p>Xin chào <b>%s</b>,</p>
                <p>Bạn có lịch phỏng vấn cho vị trí <b>%s</b> trong 2 ngày tới.</p>
                <p><b>Thời gian:</b> %s</p>
                <p><b>Địa điểm:</b> %s</p>
                <p>Vui lòng sắp xếp thời gian tham dự đúng giờ.</p>
                <div style="text-align: center; margin: 30px 0;">
                    <a href="#" 
                       style="display:inline-block; padding: 12px 24px; background-color: #FF5722; 
                              color: white; text-decoration: none; font-size: 16px; border-radius: 6px;">
                       Xem chi tiết
                    </a>
                </div>
                <p style="font-size: 14px; color: #999;">Đội ngũ WorkWite</p>
            </div>
            <div style="background-color: #f5f5f5; padding: 10px; text-align: center; font-size: 12px; color: #777;">
                Đây là email tự động, vui lòng không trả lời.
            </div>
        </div>
    </body>
    </html>
    """.formatted(candidateName, jobTitle, time, location);
    }


}
