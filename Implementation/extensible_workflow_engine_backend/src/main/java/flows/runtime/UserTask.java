package flows.runtime;

import flows.work.DefaultWorkReport;
import flows.work.Work;
import flows.work.WorkContext;
import flows.work.WorkReport;
import flows.work.WorkStatus;

public class UserTask implements Work {

    private final String prompt;

    public UserTask(String prompt) {
        this.prompt = prompt;
    }

    @Override
    public String getName() {
        return "UserTask";
    }

    @Override
    public WorkReport execute(WorkContext context) {
        System.out.println("USER TASK: " + prompt);
        context.put("WAITING", true);
        return new DefaultWorkReport(WorkStatus.COMPLETED, context);
    }
}