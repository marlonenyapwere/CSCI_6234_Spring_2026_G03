package flows.runtime;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class WorkflowInstance {

    public String id;
    public String definitionId;
    public String status; // RUNNING, WAITING, COMPLETED, FAILED

    public Map<String, Object> data = new HashMap<>();

    public String currentStepId;
    public String waitingStepId;

    public LocalDateTime startTime;
    public LocalDateTime endTime;
}