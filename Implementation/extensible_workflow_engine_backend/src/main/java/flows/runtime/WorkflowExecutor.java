package flows.runtime;

import flows.definition.StepDefinition;
import flows.definition.WorkflowDefinition;
import flows.registry.StepTypeRegistry;
import flows.work.DefaultWorkReport;
import flows.work.Work;
import flows.work.WorkContext;
import flows.work.WorkReport;
import flows.work.WorkStatus;

import java.time.LocalDateTime;
import java.util.Map;

public class WorkflowExecutor {

    public void execute(WorkflowInstance instance, WorkflowDefinition def) {

        if (instance.currentStepId == null) {
            instance.currentStepId = def.root.id;
        }

        if (instance.startTime == null) {
            instance.startTime = LocalDateTime.now();
        }

        instance.status = "RUNNING";
        WorkflowRepository.save(instance);

        while (instance.currentStepId != null) {

            StepDefinition current = def.stepMap.get(instance.currentStepId);

            if (current == null) {
                throw new RuntimeException(
                        "No step found for currentStepId=" + instance.currentStepId +
                                ". Available step ids=" + def.stepMap.keySet()
                );
            }

            System.out.println("Executing step: " + current.id + " (" + current.type + ")");

            WorkContext context = new WorkContext();
            context.put("data", instance.data);

            WorkReport report;

            if (isStructural(current.type)) {
                report = executeStructuralStep(current, context);
            } else {
                Work work = StepTypeRegistry.create(current);
                report = work.execute(context);
            }

            Object updatedData = context.get("data");
            if (updatedData instanceof Map<?, ?> map) {
                java.util.Map<String, Object> copiedData = new java.util.HashMap<>();

                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    if (entry.getKey() instanceof String key) {
                        copiedData.put(key, entry.getValue());
                    }
                }

                instance.data.clear();
                instance.data.putAll(copiedData);
            }

            if (context.get("WAITING") != null) {
                instance.status = "WAITING";
                instance.waitingStepId = current.id;
                WorkflowRepository.save(instance);
                return;
            }

            if (report.getStatus() == WorkStatus.FAILED && !"CONDITIONAL".equals(current.type)) {
                instance.status = "FAILED";
                instance.endTime = LocalDateTime.now();
                WorkflowRepository.save(instance);
                return;
            }

            instance.currentStepId = resolveNextStep(current, report, instance, def);

            WorkflowRepository.save(instance);
        }

        instance.status = "COMPLETED";
        instance.endTime = LocalDateTime.now();
        WorkflowRepository.save(instance);
    }

    private WorkReport executeStructuralStep(StepDefinition step, WorkContext context) {
        switch (step.type) {
            case "CONDITIONAL":
                return evaluateConditional(context);
            case "REPEAT":
            case "SEQUENTIAL":
                return new DefaultWorkReport(WorkStatus.COMPLETED, context);
            default:
                throw new RuntimeException("Unsupported structural step type: " + step.type);
        }
    }

    private WorkReport evaluateConditional(WorkContext context) {
        Object dataObj = context.get("data");

        if (!(dataObj instanceof Map<?, ?> map)) {
            return new DefaultWorkReport(WorkStatus.FAILED, context);
        }

        Object userResponse = map.get("userResponse");
        if (userResponse == null) {
            return new DefaultWorkReport(WorkStatus.FAILED, context);
        }

        String value = String.valueOf(userResponse).trim().toLowerCase();

        if ("reject".equals(value) || "rejected".equals(value)
                || "fail".equals(value) || "failed".equals(value)
                || "false".equals(value) || "no".equals(value)) {
            return new DefaultWorkReport(WorkStatus.FAILED, context);
        }

        return new DefaultWorkReport(WorkStatus.COMPLETED, context);
    }

    private String resolveNextStep(StepDefinition step,
                                   WorkReport report,
                                   WorkflowInstance instance,
                                   WorkflowDefinition def) {

        switch (step.type) {

            case "TASK":
            case "USER_TASK":
            case "HTTP":
            case "EMAIL":
                return resolveAfterTask(step, def);

            case "CONDITIONAL":
                if (report.getStatus() == WorkStatus.COMPLETED) {
                    return step.onSuccess != null ? step.onSuccess.id : resolveAfterContainer(step, def);
                } else {
                    return step.onFailure != null ? step.onFailure.id : resolveAfterContainer(step, def);
                }

            case "REPEAT":
                return resolveRepeatEntry(step, instance, def);

            case "SEQUENTIAL":
                if (step.steps != null && !step.steps.isEmpty()) {
                    return step.steps.get(0).id;
                }
                return resolveAfterContainer(step, def);

            default:
                throw new RuntimeException("Unknown type: " + step.type);
        }
    }

    private String resolveAfterTask(StepDefinition step, WorkflowDefinition def) {
        if (step.nextStepId != null) {
            return step.nextStepId;
        }

        if (step.parentStepId == null) {
            return null;
        }

        StepDefinition parent = def.stepMap.get(step.parentStepId);
        if (parent == null) {
            return null;
        }

        switch (parent.type) {
            case "SEQUENTIAL":
                return resolveNextSequentialChild(parent, step, def);
            case "REPEAT":
                return parent.id;
            case "CONDITIONAL":
                return resolveAfterContainer(parent, def);
            default:
                return resolveAfterContainer(parent, def);
        }
    }

    private String resolveAfterContainer(StepDefinition container, WorkflowDefinition def) {
        if (container.nextStepId != null) {
            return container.nextStepId;
        }

        if (container.parentStepId == null) {
            return null;
        }

        StepDefinition parent = def.stepMap.get(container.parentStepId);
        if (parent == null) {
            return null;
        }

        switch (parent.type) {
            case "SEQUENTIAL":
                return resolveNextSequentialChild(parent, container, def);
            case "REPEAT":
                return parent.id;
            case "CONDITIONAL":
                return resolveAfterContainer(parent, def);
            default:
                return resolveAfterContainer(parent, def);
        }
    }

    private String resolveNextSequentialChild(StepDefinition sequential,
                                              StepDefinition completedChild,
                                              WorkflowDefinition def) {

        if (sequential.steps == null || sequential.steps.isEmpty()) {
            return resolveAfterContainer(sequential, def);
        }

        for (int i = 0; i < sequential.steps.size(); i++) {
            StepDefinition child = sequential.steps.get(i);
            if (child.id.equals(completedChild.id)) {
                if (i + 1 < sequential.steps.size()) {
                    return sequential.steps.get(i + 1).id;
                }
                return resolveAfterContainer(sequential, def);
            }
        }

        return resolveAfterContainer(sequential, def);
    }

    private String resolveRepeatEntry(StepDefinition repeat,
                                      WorkflowInstance instance,
                                      WorkflowDefinition def) {

        String counterKey = "repeat_" + repeat.id + "_count";

        int count = getInt(instance.data.get(counterKey));

        if (count < repeat.times) {
            count++;
            instance.data.put(counterKey, count);
            return repeat.step.id;
        }

        instance.data.remove(counterKey);
        return resolveAfterContainer(repeat, def);
    }

    private int getInt(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Integer i) {
            return i;
        }
        if (value instanceof Long l) {
            return l.intValue();
        }
        if (value instanceof Number n) {
            return n.intValue();
        }
        return Integer.parseInt(String.valueOf(value));
    }

    private boolean isStructural(String type) {
        return "CONDITIONAL".equals(type)
                || "REPEAT".equals(type)
                || "SEQUENTIAL".equals(type);
    }

    public void resume(WorkflowInstance instance,
                       WorkflowDefinition def,
                       Object userResponse) {

        if (!"WAITING".equals(instance.status)) {
            throw new RuntimeException("Workflow is not waiting");
        }

        StepDefinition waitingStep = def.stepMap.get(instance.waitingStepId);
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