package com.hongchu.qqrobotsign.service.impl;

import com.hongchu.qqrobotsign.content.EmailContext;
import com.hongchu.qqrobotsign.exception.BusinessException;
import com.hongchu.qqrobotsign.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;

    /**
     * 发送简单文本邮件
     */
    @Override
    public void sendSimpleEmail(String to, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("油签机 <2772167017@qq.com>");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            mailSender.send(message);
            log.info("邮件发送成功，收件人：{}", to);
        } catch (Exception e) {
            log.error("邮件发送失败，收件人：{}", to, e);
            throw new BusinessException("邮箱发送失败");
        }
    }

    /**
     * 通知用户签到时间安排
     */
    @Override
    public void sendScheduleNotice(String to, String username, String scheduledTime) {
        String subject = EmailContext.SCHEDULE_NOTICE_SUBJECT;
        String content = EmailContext.SCHEDULE_NOTICE_CONTENT.formatted(username, scheduledTime);
        sendSimpleEmail(to, subject, content);
    }

    /**
     * 通知用户签到结果
     */
    @Override
    public void sendSignResultNotice(String to, String username, boolean success, String resultMessage) {
        String subject = success ? EmailContext.SIGN_SUCCESS_SUBJECT : EmailContext.SIGN_FAIL_SUBJECT;

        String content;
        if (success) {
            content = EmailContext.SIGN_RESULT_CONTENT.formatted(
                    EmailContext.SIGN_SUCCESS_TITLE,
                    username,
                    LocalDateTime.now(),
                    EmailContext.SIGN_SUCCESS_STATUS,
                    EmailContext.SIGN_SUCCESS_MESSAGE
            );
        } else {
            content = EmailContext.SIGN_RESULT_CONTENT.formatted(
                    EmailContext.SIGN_FAIL_TITLE,
                    username,
                    LocalDateTime.now(),
                    EmailContext.SIGN_FAIL_STATUS,
                    EmailContext.SIGN_FAIL_PREFIX + resultMessage + EmailContext.SIGN_FAIL_SUFFIX
            );
        }

        sendSimpleEmail(to, subject, content);
    }
}
