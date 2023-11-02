package src.service.User.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {
    @Autowired
    private JavaMailSender javaMailSender;

    @Value("hieultkrm@gmail.com")
    private String fromMail;

    private String Subject;

    public void sendMessageMail(String mail, MessageDto messageDto)
    {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom(fromMail);
        Subject = "Chúng tôi xin được phản hồi thắc mắc của bạn";
        simpleMailMessage.setSubject(Subject);
        simpleMailMessage.setText(messageDto.getMessage());
        simpleMailMessage.setTo(mail);

        javaMailSender.send(simpleMailMessage);
    }

}
