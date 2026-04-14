package flows.tasks;

import flows.registry.StepTypeRegistry;
import flows.runtime.UserTask;


public class Main {

    public static void main(String[] args) throws Exception {

        StepTypeRegistry.register("TASK",
                def -> new PrintMessageTask(def.message));

        StepTypeRegistry.register("USER_TASK",
                def -> new UserTask(def.message));
    }
}