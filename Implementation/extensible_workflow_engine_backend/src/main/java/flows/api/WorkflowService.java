package flows.api;

import flows.definition.Workflow;
import flows.runtime.WorkflowExecutor;
import flows.runtime.WorkflowInstance;
import flows.runtime.WorkflowRepository;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class WorkflowService {

    private final WorkflowDefinitionRepository definitionRepository;
    private final WorkflowExecutor executor = new WorkflowExecutor();

    public WorkflowService(WorkflowDefinitionRepository definitionRepository) {
        this.definitionRepository = definitionRepository;
    }

    public Workflow saveDefinition(Workflow definition) {
        definitionRepository.save(definition);
        return definitionRepository.get(definition.id);
    }

    public WorkflowInstance startWorkflow(String definitionId, Map<String, Object> data) {
        Workflow def = definitionRepository.get(definitionId);
        if (def == null) {
            throw new RuntimeException("Definition not found: " + definitionId);
        }

        WorkflowInstance instance = new WorkflowInstance();
        instance.id = UUID.randomUUID().toString();
        instance.definitionId = def.id;
        instance.status = "RUNNING";

        if (data != null) {
            instance.data.putAll(data);
        }

        WorkflowRepository.save(instance);
        executor.execute(instance, def);

        return WorkflowRepository.get(instance.id);
    }

    public WorkflowInstance getWorkflow(String workflowId) {
        WorkflowInstance instance = WorkflowRepository.get(workflowId);
        if (instance == null) {
            throw new RuntimeException("Workflow instance not found: " + workflowId);
        }
        return instance;
    }

    public WorkflowInstance resumeWorkflow(String workflowId, Object userResponse) {
        WorkflowInstance instance = WorkflowRepository.get(workflowId);
        if (instance == null) {
            throw new RuntimeException("Workflow instance not found: " + workflowId);
        }

        Workflow def = definitionRepository.get(instance.definitionId);
        if (def == null) {
            throw new RuntimeException("Definition not found: " + instance.definitionId);
        }

        executor.resume(instance, def, userResponse);
        return WorkflowRepository.get(instance.id);
    }
}