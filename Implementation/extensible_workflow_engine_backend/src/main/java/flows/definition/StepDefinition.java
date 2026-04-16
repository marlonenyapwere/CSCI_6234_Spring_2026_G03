package flows.definition;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Map;

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

    // EMAIL task fields
    public String to;
    public String subject;
    public String body;
    public String from;

    // HTTP task fields
    public String url;
    public String method;
    public Map<String, String> headers;
    public String requestBody;

    @JsonIgnore
    public String parentStepId;
}