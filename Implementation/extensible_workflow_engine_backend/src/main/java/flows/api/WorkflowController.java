package flows.api;

import flows.definition.WorkflowDefinition;
import flows.runtime.WorkflowInstance;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class WorkflowController {

    private final WorkflowService workflowService;

    public WorkflowController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @PostMapping("/definition")
    public WorkflowDefinition createDefinition(@RequestBody WorkflowDefinition definition) {
        return workflowService.saveDefinition(definition);
    }

    @PostMapping("/workflow/{definitionId}")
    public WorkflowInstance startWorkflow(@PathVariable String definitionId,
                                          @RequestBody(required = false) Map<String, Object> data) {
        return workflowService.startWorkflow(definitionId, data);
    }

    @GetMapping("/workflow/{workflowId}")
    public WorkflowInstance getWorkflow(@PathVariable String workflowId) {
        return workflowService.getWorkflow(workflowId);
    }

    @PutMapping("/workflow/{workflowId}/resume")
    public WorkflowInstance resumeWorkflow(@PathVariable String workflowId,
                                           @RequestBody ResumeRequest request) {
        Object response = request != null ? request.userResponse : null;
        return workflowService.resumeWorkflow(workflowId, response);
    }
}
