package flows.tasks;

import flows.work.DefaultWorkReport;
import flows.work.Work;
import flows.work.WorkContext;
import flows.work.WorkReport;
import flows.work.WorkStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

public class EmailTask implements Work {

    private final JavaMailSender mailSender;
    private final String from;
    private final String to;
    private final String subject;
    private final String body;

    public EmailTask(JavaMailSender mailSender,
                     String from,
                     String to,
                     String subject,
                     String body) {
        this.mailSender = mailSender;
        this.from = from;
        this.to = to;
        this.subject = subject;
        this.body = body;
    }

    @Override
    public String getName() {
        return "EmailTask";
    }

    @Override
    public WorkReport execute(WorkContext context) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();

            if (from != null && !from.isBlank()) {
                message.setFrom(from);
            }

            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);

            System.out.println("Email sent successfully to " + to);
            return new DefaultWorkReport(WorkStatus.COMPLETED, context);

        } catch (Exception e) {
            System.out.println("Email send failed: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
            return new DefaultWorkReport(WorkStatus.FAILED, context, e);
        }
    }
}