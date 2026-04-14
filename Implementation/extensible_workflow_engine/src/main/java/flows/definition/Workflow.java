package flows.definition;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.HashMap;
import java.util.Map;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Workflow {

    public String id;
    public String name;
    public Step root;
    public List<Step> steps;

    @JsonIgnore
    public Map<String, Step> stepMap = new HashMap<>();

    public void buildStepMap() {
        stepMap.clear();

        traverse(root);

        if (steps != null) {
            for (Step step : steps) {
                traverse(step);
            }
        }
    }

    private void traverse(Step step) {
        if (step == null) {
            return;
        }

        if (step.id != null) {
            stepMap.put(step.id, step);
        }

        if (step.steps != null) {
            for (Step child : step.steps) {
                traverse(child);
            }
        }

        traverse(step.step);
        traverse(step.initial);
        traverse(step.onSuccess);
        traverse(step.onFailure);
    }
}