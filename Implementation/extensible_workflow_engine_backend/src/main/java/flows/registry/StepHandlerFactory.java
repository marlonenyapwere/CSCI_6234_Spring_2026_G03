package flows.registry;

import flows.definition.StepDefinition;
import flows.work.Work;

public interface StepHandlerFactory {
    Work create(StepDefinition def);
}
