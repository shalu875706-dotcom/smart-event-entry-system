package com.smarterevententrysystem.util;

import jakarta.activation.DataHandler;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Part;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import java.util.Properties;

/** Sends the QR ticket by email (the QR picture is shown inside the mail and attached). */
public class EmailSender {

    private EmailSender() { }

    public static void sendTicket(String toEmail, String participantName, String eventName,
                                  String ticketCode, byte[] qrPng) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.host", EmailConfig.SMTP_HOST);
        props.put("mail.smtp.port", String.valueOf(EmailConfig.SMTP_PORT));
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EmailConfig.SMTP_USER, EmailConfig.SMTP_APP_PASSWORD);
            }
        });

        String html = "<div style=\"font-family:Arial,sans-serif\">"
                + "<h2>Your event ticket</h2>"
                + "<p>Hello " + escapeHtml(participantName) + ",</p>"
                + "<p>You are registered for <b>" + escapeHtml(eventName) + "</b>.</p>"
                + "<p>Your Ticket ID: <b>" + escapeHtml(ticketCode) + "</b></p>"
                + "<p>Show this QR code at the entrance. A ticket can be used only once.</p>"
                + "<img src=\"cid:qr\" width=\"250\" height=\"250\" alt=\"QR ticket\">"
                + "</div>";

        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(html, "text/html; charset=UTF-8");

        MimeBodyPart imagePart = new MimeBodyPart();
        imagePart.setDataHandler(new DataHandler(new ByteArrayDataSource(qrPng, "image/png")));
        imagePart.setHeader("Content-ID", "<qr>");
        imagePart.setDisposition(Part.INLINE);
        imagePart.setFileName(ticketCode + ".png");

        MimeMultipart content = new MimeMultipart("related");
        content.addBodyPart(htmlPart);
        content.addBodyPart(imagePart);

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(EmailConfig.SMTP_USER));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject("Your ticket for " + eventName, "UTF-8");
        message.setContent(content);

        Transport.send(message);
    }

    private static String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
