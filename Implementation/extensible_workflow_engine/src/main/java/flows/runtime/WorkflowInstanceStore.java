package flows.runtime;

import flows.persistence.WorkflowJsonMapper;
import flows.persistence.WorkflowInstanceEntity;
import flows.persistence.WorkflowInstanceJpaRepository;
import org.springframework.stereotype.Component;

@Component
public class WorkflowInstanceStore {

    private static WorkflowInstanceJpaRepository staticJpaRepository;
    private static WorkflowJsonMapper staticJsonMapper;

    public WorkflowInstanceStore(WorkflowInstanceJpaRepository jpaRepository,
                                 WorkflowJsonMapper jsonMapper) {
        staticJpaRepository = jpaRepository;
        staticJsonMapper = jsonMapper;
    }

    public static void save(WorkflowInstance instance) {
        WorkflowInstanceEntity entity = new WorkflowInstanceEntity();
        entity.id = instance.id;
        entity.definitionId = instance.definitionId;
        entity.definitionVersion = 1;
        entity.status = instance.status;
        entity.currentStepId = instance.currentStepId;
        entity.waitingStepId = instance.waitingStepId;
        entity.dataJson = staticJsonMapper.toJson(instance.data);
        entity.startTime = instance.startTime;
        entity.endTime = instance.endTime;

        staticJpaRepository.save(entity);
    }

    public static WorkflowInstance get(String id) {
        WorkflowInstanceEntity entity = staticJpaRepository.findById(id)
                .orElse(null);

        if (entity == null) {
            return null;
        }

        WorkflowInstance instance = new WorkflowInstance();
        instance.id = entity.id;
        instance.definitionId = entity.definitionId;
        instance.status = entity.status;
        instance.currentStepId = entity.currentStepId;
        instance.waitingStepId = entity.waitingStepId;
        instance.data = staticJsonMapper.mapFromJson(entity.dataJson);
        instance.startTime = entity.startTime;
        instance.endTime = entity.endTime;

        return instance;
    }
}
