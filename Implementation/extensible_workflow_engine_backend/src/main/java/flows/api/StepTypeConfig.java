package flows.api;

import flows.registry.StepTypeRegistry;
import flows.runtime.UserTask;
import flows.tasks.EmailTask;
import flows.tasks.HttpTask;
import flows.tasks.PrintMessageTask;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;


import org.springframework.mail.javamail.JavaMailSender;

@Configuration
public class StepTypeConfig {

    private final JavaMailSender javaMailSender;

    public StepTypeConfig(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @PostConstruct
    public void registerStepTypes() {
        StepTypeRegistry.register("TASK",
                def -> new PrintMessageTask(def.message));

        StepTypeRegistry.register("USER_TASK",
                def -> new UserTask(def.message));

        StepTypeRegistry.register("EMAIL",
                def -> new EmailTask(
                        javaMailSender,
                        def.from,
                        def.to,
                        def.subject,
                        def.body
                ));

        StepTypeRegistry.register("HTTP",
                def -> new HttpTask(
                        def.url,
                        def.method,
                        def.headers,
                        def.requestBody
                ));

        System.out.println("Registered TASK, USER_TASK, EMAIL, HTTP step types");
    }
}