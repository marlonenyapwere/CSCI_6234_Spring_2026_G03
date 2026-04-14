package flows.definition;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Step {

    public String id;
    public String type;
    public String nextStepId;

    public List<Step> steps;

    public Integer times;
    public Step step;

    public Step initial;
    public String condition;
    public Step onSuccess;
    public Step onFailure;

    public String message;
}