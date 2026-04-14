package flows.registry;


import java.util.HashMap;
import java.util.Map;


import flows.definition.Step;
import flows.work.Work;

import java.util.function.Function;

public class StepTypeRegistry {

    private static final Map<String, Function<Step, Work>> registry = new HashMap<>();

    public static void register(String type, Function<Step, Work> creator) {
        registry.put(type, creator);
    }

    public static Work create(Step def) {
        Function<Step, Work> fn = registry.get(def.type);
        if (fn == null) {
            throw new RuntimeException("No handler for type: " + def.type);
        }
        return fn.apply(def);
    }
}