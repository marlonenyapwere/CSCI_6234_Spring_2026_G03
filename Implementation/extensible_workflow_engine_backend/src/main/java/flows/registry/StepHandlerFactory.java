package flows.registry;

import flows.definition.Step;
import flows.work.Work;

public interface StepHandlerFactory {
    Work create(Step def);
}
