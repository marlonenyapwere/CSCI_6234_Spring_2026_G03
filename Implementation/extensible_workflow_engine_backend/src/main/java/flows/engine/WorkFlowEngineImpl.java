
package flows.engine;

import flows.work.WorkContext;
import flows.work.WorkReport;
import flows.workflow.WorkFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


class WorkFlowEngineImpl implements WorkflowEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkFlowEngineImpl.class);

    public WorkReport run(WorkFlow workFlow, WorkContext workContext) {
        LOGGER.info("Running workflow ''{}''", workFlow.getName());
        return workFlow.execute(workContext);
    }

}
