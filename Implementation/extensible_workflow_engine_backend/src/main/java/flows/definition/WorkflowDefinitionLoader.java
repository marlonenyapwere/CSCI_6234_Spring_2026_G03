package flows.definition;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;

public class WorkflowDefinitionLoader {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static WorkflowDefinition load(String path) {
        try {
            WorkflowDefinition def = mapper.readValue(new File(path), WorkflowDefinition.class);
            def.buildStepMap();

            System.out.println("Loaded steps: " + def.stepMap.keySet());

            return def;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}