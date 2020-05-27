package me.mastercapexd.auth.email;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailService {

	private final String username, password, company;
	private final Properties properties = new Properties();
	
	private Session session;
	
	public EmailService(EmailSettings emailSettings) {
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.starttls.enable", "true");
		properties.put("mail.smtp.host", emailSettings.getSmtp());
		properties.put("mail.smtp.port", "25");
		
		this.username = emailSettings.getUsername();
		this.password = emailSettings.getPassword();
		this.company = emailSettings.getCompany();
		
		this.session = Session.getInstance(properties, new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		});
	}
	
	public void sendEmail(String to, String subject, String text) throws UnsupportedEncodingException, MessagingException {
		MimeMessage mimeMessage = new MimeMessage(session);
		mimeMessage.setFrom(new InternetAddress(username, company));
		mimeMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
		mimeMessage.setSubject(subject);
		mimeMessage.setContent(text, "text/html");
		Transport.send(mimeMessage);
	}
}