package flows.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Table;

@Entity
@Table(name = "workflow_definition")
public class WorkflowDefinitionEntity {

    @Id
    @Column(name = "id", nullable = false)
    public String id;

    @Column(name = "name")
    public String name;

    @Column(name = "version_num")
    public Integer version;

    @Lob
    @Column(name = "definition_json", nullable = false, columnDefinition = "CLOB")
    public String definitionJson;

    @Column(name = "created_at")
    public LocalDateTime createdAt;
}
