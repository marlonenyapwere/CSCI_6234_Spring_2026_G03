package flows.runtime;

public class WorkflowRepository {

    public static void save(WorkflowInstance instance) {
        WorkflowInstanceStore.save(instance);
    }

    public static WorkflowInstance get(String id) {
        return WorkflowInstanceStore.get(id);
    }
}