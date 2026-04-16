package flows.api;

import flows.definition.WorkflowDefinition;
import flows.dto.ErrorResponseDto;
import flows.runtime.WorkflowInstance;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@Tag(
        name = "Workflow REST APIs for managing Workflows",
        description = "Workflow REST APIs to DEFINE workflows, CREATE workflow instance, QUERY execution states AND RESUME from user tasks"
)
@RestController
@RequestMapping("/api")
public class WorkflowController {

    private final WorkflowService workflowService;

    public WorkflowController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @Operation(
            summary = "REST endpoint to define a Workflow",
            description = "Define and save your workflow. Obtain Workflow id to create an execution instance of the workflow"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "HTTP Status CREATED"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "HTTP Status Internal Server Error",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            )
    }
    )
    @PostMapping("/definition")
    public WorkflowDefinition createDefinition(@RequestBody WorkflowDefinition definition) {
        return workflowService.saveDefinition(definition);
    }


    @Operation(
            summary = "REST endpoint to create a Workflow instance to be executed",
            description = "Create your workflow instance. Includes state persist."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "HTTP Status CREATED"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "HTTP Status Internal Server Error",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            )
    }
    )
    @PostMapping("/workflow/{definitionId}")
    public WorkflowInstance startWorkflow(@PathVariable String definitionId,
                                          @RequestBody(required = false) Map<String, Object> data) {
        return workflowService.startWorkflow(definitionId, data);
    }

    @Operation(
            summary = "REST endpoint to search workflow by instanceID",
            description = "REST API to fetch workflow details based on a instance idr"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "HTTP Status OK"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "HTTP Status Internal Server Error",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            )
    }
    )
    @GetMapping("/workflow/{workflowId}")
    public WorkflowInstance getWorkflow(@PathVariable String workflowId) {
        return workflowService.getWorkflow(workflowId);
    }

    @Operation(
            summary = "Update Workflow execution based on a user action",
            description = "REST API to resume workflow execution based on user task completion"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "HTTP Status OK"
            ),
            @ApiResponse(
                    responseCode = "417",
                    description = "Expectation Failed"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "HTTP Status Internal Server Error",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            )
    }
    )
    @PutMapping("/workflow/{workflowId}/resume")
    public WorkflowInstance resumeWorkflow(@PathVariable String workflowId,
                                           @RequestBody ResumeRequest request) {
        Object response = request != null ? request.userResponse : null;
        return workflowService.resumeWorkflow(workflowId, response);
    }
}
