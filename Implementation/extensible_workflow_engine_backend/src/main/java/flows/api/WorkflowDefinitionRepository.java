package flows.api;

import flows.definition.WorkflowDefinition;
import org.springframework.stereotype.Component;

import flows.persistence.WorkflowJsonMapper;
import flows.persistence.WorkflowDefinitionEntity;
import flows.persistence.WorkflowDefinitionJpaRepository;

import java.time.LocalDateTime;

@Component
public class WorkflowDefinitionRepository {

    private final WorkflowDefinitionJpaRepository jpaRepository;
    private final WorkflowJsonMapper jsonMapper;

    public WorkflowDefinitionRepository(WorkflowDefinitionJpaRepository jpaRepository,
                                        WorkflowJsonMapper jsonMapper) {
        this.jpaRepository = jpaRepository;
        this.jsonMapper = jsonMapper;
    }

    public void save(WorkflowDefinition definition) {
        definition.buildStepMap();

        WorkflowDefinitionEntity entity = new WorkflowDefinitionEntity();
        entity.id = definition.id;
        entity.name = definition.name;
        entity.version = 1;
        entity.definitionJson = jsonMapper.toJson(definition);
        entity.createdAt = LocalDateTime.now();

        jpaRepository.save(entity);
    }

    public WorkflowDefinition get(String id) {
        WorkflowDefinitionEntity entity = jpaRepository.findById(id)
                .orElse(null);

        if (entity == null) {
            return null;
        }

        WorkflowDefinition definition = jsonMapper.fromJson(entity.definitionJson, WorkflowDefinition.class);
        definition.buildStepMap();
        return definition;
    }
}
