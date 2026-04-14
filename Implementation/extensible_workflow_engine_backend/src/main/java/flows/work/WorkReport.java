
package flows.work;

public interface WorkReport {

    WorkStatus getStatus();

    Throwable getError();

    WorkContext getWorkContext();

}
