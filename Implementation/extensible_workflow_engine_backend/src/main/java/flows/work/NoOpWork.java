package flows.work;

import java.util.UUID;

public class NoOpWork implements Work {

    @Override
    public String getName() {
        return UUID.randomUUID().toString();
    }

    @Override
    public WorkReport execute(WorkContext workContext) {
        return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
    }
}
