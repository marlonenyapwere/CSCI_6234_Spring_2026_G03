package flows.tasks;

import flows.work.*;

public class PrintMessageTask implements Work {
    private final String message;

    public PrintMessageTask(String message) {
        this.message = message;
    }

    public String getName() {
        return "print message work";
    }

    public WorkReport execute(WorkContext workContext) {
        System.out.println(message);
        return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
    }
}
