
package flows.work;

public class DefaultWorkReport implements WorkReport {

    private final WorkStatus status;
    private final WorkContext workContext;
    private Throwable error;

    public DefaultWorkReport(WorkStatus status, WorkContext workContext) {
        this.status = status;
        this.workContext = workContext;
    }


    public DefaultWorkReport(WorkStatus status, WorkContext workContext, Throwable error) {
        this(status, workContext);
        this.error = error;
    }

    public WorkStatus getStatus() {
        return status;
    }

    public Throwable getError() {
        return error;
    }

    @Override
    public WorkContext getWorkContext() {
        return workContext;
    }

    @Override
    public String toString() {
        return "DefaultWorkReport {" +
                "status=" + status +
                ", context=" + workContext +
                ", error=" + (error == null ? "''" : error) +
                '}';
    }
}
