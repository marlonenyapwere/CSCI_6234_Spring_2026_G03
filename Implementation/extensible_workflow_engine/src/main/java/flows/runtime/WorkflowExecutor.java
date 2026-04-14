package flows.runtime;

import flows.definition.Step;
import flows.definition.Workflow;
import flows.registry.StepTypeRegistry;


import flows.work.Work;
import flows.work.WorkContext;
import flows.work.WorkReport;
import flows.work.WorkStatus;

import java.time.LocalDateTime;
import java.util.Map;

public class WorkflowExecutor {

    public void execute(WorkflowInstance instance, Workflow def) {

        if (instance.currentStepId == null) {
            instance.currentStepId = def.root.id;
        }

        if (instance.startTime == null) {
            instance.startTime = LocalDateTime.now();
        }

        instance.status = "RUNNING";
        WorkflowRepository.save(instance);

        while (instance.currentStepId != null) {

            Step current = def.stepMap.get(instance.currentStepId);

            if (current == null) {
                throw new RuntimeException(
                        "No step found for currentStepId=" + instance.currentStepId +
                                ". Available step ids=" + def.stepMap.keySet()
                );
            }

            System.out.println("Executing step: " + current.id + " (" + current.type + ")");

            Work work = StepTypeRegistry.create(current);

            WorkContext context = new WorkContext();
            context.put("data", instance.data);

            WorkReport report = work.execute(context);

            Object updatedData = context.get("data");
            if (updatedData instanceof Map<?, ?> map) {
                instance.data.clear();
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    if (entry.getKey() instanceof String key) {
                        instance.data.put(key, entry.getValue());
                    }
                }
            }

            if (context.get("WAITING") != null) {
                instance.status = "WAITING";
                instance.waitingStepId = current.id;
                WorkflowRepository.save(instance);
                return;
            }

            if (report.getStatus() == WorkStatus.FAILED) {
                instance.status = "FAILED";
                instance.endTime = LocalDateTime.now();
                WorkflowRepository.save(instance);
                return;
            }

            instance.currentStepId = resolveNextStep(current, report, def, instance);

            WorkflowRepository.save(instance);
        }

        instance.status = "COMPLETED";
        instance.endTime = LocalDateTime.now();
        WorkflowRepository.save(instance);
    }

    private String resolveNextStep(Step step,
                                   WorkReport report,
                                   Workflow def,
                                   WorkflowInstance instance) {

        switch (step.type) {

            case "TASK":
            case "USER_TASK":
                return step.nextStepId;

            case "CONDITIONAL":
                if (report.getStatus() == WorkStatus.COMPLETED) {
                    return step.onSuccess != null ? step.onSuccess.id : step.nextStepId;
                } else {
                    return step.onFailure != null ? step.onFailure.id : step.nextStepId;
                }

            case "REPEAT":
                String key = "repeat_" + step.id;

                Integer count = (Integer) instance.data.get(key);
                if (count == null) {
                    count = 0;
                }

                count++;
                instance.data.put(key, count);

                if (count < step.times) {
                    return step.step.id;
                } else {
                    return step.nextStepId;
                }

            case "SEQUENTIAL", "PARALLEL":
                return step.steps != null && !step.steps.isEmpty()
                        ? step.steps.get(0).id
                        : step.nextStepId;

            default:
                throw new RuntimeException("Unknown type: " + step.type);
        }
    }

    public void resume(WorkflowInstance instance,
                       Workflow def,
                       Object userResponse) {

        if (!"WAITING".equals(instance.status)) {
            throw new RuntimeException("Workflow is not waiting");
        }

        Step waitingStep = def.stepMap.get(instance.waitingStepId);
        if (waitingStep == null) {
            throw new RuntimeException("Waiting step not found: " + instance.waitingStepId);
        }

        instance.data.put("userResponse", userResponse);
        instance.status = "RUNNING";
        instance.waitingStepId = null;
        instance.currentStepId = waitingStep.nextStepId;

        WorkflowRepository.save(instance);

        execute(instance, def);
    }
}