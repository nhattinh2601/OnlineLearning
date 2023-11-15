package src.config.gmail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.internet.MimeMessage;

@Component
public class EmailUtil {

    @Autowired
    private JavaMailSender javaMailSender;

    public void sendOtpEmail(String email, String otp) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
            mimeMessageHelper.setTo(email);
            mimeMessageHelper.setSubject("Verify OTP");

            String emailContent = String.format("""
                    <div>
                      <a href="http://localhost:8080/verify-account?email=%s&otp=%s" target="_blank">Click this link to verify</a>
                    </div>
                    """, email, otp);

            mimeMessageHelper.setText(emailContent, true);

            javaMailSender.send(mimeMessage);
        } catch (Exception e) {
            // Handle exception appropriately (e.g., log it)
            e.printStackTrace();
        }
    }
}
