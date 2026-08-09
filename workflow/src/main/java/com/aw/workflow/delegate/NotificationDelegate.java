package com.aw.workflow.delegate;

import com.aw.workflow.event.WorkflowCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component("notificationDelegate")
@RequiredArgsConstructor
@Slf4j
public class NotificationDelegate implements JavaDelegate {

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String recipients = (String) execution.getVariable("recipientEmails");
        String subject = (String) execution.getVariable("notificationTitle");
        String content = (String) execution.getVariable("notificationContent");

        if (recipients != null && !recipients.isEmpty()) {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(recipients);
            message.setSubject(subject);
            message.setText(content);

            mailSender.send(message);
        }
    }
}
