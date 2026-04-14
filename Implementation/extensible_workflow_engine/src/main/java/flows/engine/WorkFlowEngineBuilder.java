package flows.engine;

public class WorkFlowEngineBuilder {

    public static WorkFlowEngineBuilder aNewWorkFlowEngine() {
        return new WorkFlowEngineBuilder();
    }

    private WorkFlowEngineBuilder() {
    }

    public WorkflowEngine build() {
        return new WorkFlowEngineImpl();
    }
}
