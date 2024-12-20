package com.hmdp.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.*;

public class MailUtils {
    public static void main(String[] args) throws MessagingException {
        // 测试发送邮件
//        sendTestMail("741766792@qq.com", achieveCode());
    }

    public static void sendTestMail(String email, String code) throws MessagingException {
        // 创建 Properties 类用于记录邮箱属性
        Properties props = new Properties();
        // SMTP 服务器地址
        props.put("mail.smtp.host", "smtp.qq.com");
        // 启用 SMTP 身份验证
        props.put("mail.smtp.auth", "true");
        // 启用 STARTTLS
        props.put("mail.smtp.starttls.enable", "true");
        // SMTP 端口号
        props.put("mail.smtp.port", "587");
        // 发件人邮箱账号
        props.put("mail.user", "408658287@qq.com");
        // 授权码
        props.put("mail.password", "xpbrlsdjpvtlbibe");

        // 创建会话，使用授权信息
        Authenticator authenticator = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                String userName = props.getProperty("mail.user");
                String password = props.getProperty("mail.password");
                return new PasswordAuthentication(userName, password);
            }
        };

        // 使用环境属性和授权信息创建邮件会话
        Session mailSession = Session.getInstance(props, authenticator);

        // 创建邮件消息
        MimeMessage message = new MimeMessage(mailSession);
        // 设置发件人
        InternetAddress from = new InternetAddress(props.getProperty("mail.user"));
        message.setFrom(from);
        // 设置收件人
        InternetAddress to = new InternetAddress(email);
        message.setRecipient(Message.RecipientType.TO, to);
        // 设置邮件标题
        message.setSubject("验证码邮件测试");
        // 设置邮件内容
        message.setContent("尊敬的用户：<br>您的验证码是：" + code + "<br>请在1分钟内使用。", "text/html;charset=UTF-8");

        // 发送邮件
        Transport.send(message);
        System.out.println("邮件发送成功！");
    }

    public static String achieveCode() {
        // 生成验证码，去掉易混淆字符
        String[] chars = new String[]{"2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "G", "H",
                "J", "K", "L", "M", "N", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "a", "b", "c", "d", "e",
                "f", "g", "h", "j", "k", "m", "n", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"};
        List<String> list = Arrays.asList(chars);
        Collections.shuffle(list);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            sb.append(list.get(i));
        }
        return sb.toString();
    }
}

