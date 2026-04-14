package flows.definition;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkflowDefinition {

    public String id;
    public String name;
    public StepDefinition root;
    public List<StepDefinition> steps;

    @JsonIgnore
    public Map<String, StepDefinition> stepMap = new HashMap<>();

    public void buildStepMap() {
        stepMap.clear();

        traverse(root, null);

        if (steps != null) {
            for (StepDefinition step : steps) {
                traverse(step, null);
            }
        }
    }

    private void traverse(StepDefinition step, String parentStepId) {
        if (step == null) {
            return;
        }

        step.parentStepId = parentStepId;

        if (step.id != null) {
            stepMap.put(step.id, step);
        }

        if (step.steps != null) {
            for (StepDefinition child : step.steps) {
                traverse(child, step.id);
            }
        }

        if (step.step != null) {
            traverse(step.step, step.id);
        }

        if (step.initial != null) {
            traverse(step.initial, step.id);
        }

        if (step.onSuccess != null) {
            traverse(step.onSuccess, step.id);
        }

        if (step.onFailure != null) {
            traverse(step.onFailure, step.id);
        }
    }
}