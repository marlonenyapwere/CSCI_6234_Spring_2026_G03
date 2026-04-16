package flows.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Table;

@Entity
@Table(name = "workflow_instance")
public class WorkflowInstanceEntity {

    @Id
    @Column(name = "id", nullable = false)
    public String id;

    @Column(name = "definition_id", nullable = false)
    public String definitionId;

    @Column(name = "definition_version")
    public Integer definitionVersion;

    @Column(name = "status")
    public String status;

    @Column(name = "current_step_id")
    public String currentStepId;

    @Column(name = "waiting_step_id")
    public String waitingStepId;

    @Column(name = "data_json", columnDefinition = "TEXT")
    public String dataJson;

    @Column(name = "start_time")
    public LocalDateTime startTime;

    @Column(name = "end_time")
    public LocalDateTime endTime;
}