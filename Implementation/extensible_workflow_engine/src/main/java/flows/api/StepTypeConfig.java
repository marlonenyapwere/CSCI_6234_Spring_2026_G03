package flows.api;

import flows.registry.StepTypeRegistry;
import flows.runtime.UserTask;
import flows.tasks.PrintMessageTask;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StepTypeConfig {

    @PostConstruct
    public void registerStepTypes() {
        StepTypeRegistry.register("TASK",
                def -> new PrintMessageTask(def.message));

        StepTypeRegistry.register("USER_TASK",
                def -> new UserTask(def.message));
    }
}