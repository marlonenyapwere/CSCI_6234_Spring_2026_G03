
package flows.engine;

import flows.work.WorkContext;
import flows.work.WorkReport;
import flows.workflow.WorkFlow;

public interface WorkflowEngine {

    WorkReport run(WorkFlow workFlow, WorkContext workContext);

}
