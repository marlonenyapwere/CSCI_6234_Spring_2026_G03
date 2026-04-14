package flows.definition;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StepDefinition {

    public String id;
    public String type;
    public String nextStepId;

    public List<StepDefinition> steps;

    public Integer times;
    public StepDefinition step;

    public StepDefinition initial;
    public String condition;
    public StepDefinition onSuccess;
    public StepDefinition onFailure;

    public String message;

    @JsonIgnore
    public String parentStepId;
}