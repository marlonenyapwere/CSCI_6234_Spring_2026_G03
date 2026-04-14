
package flows.work;

import java.util.UUID;


public interface Work {

    default String getName() {
        return UUID.randomUUID().toString();
    }

    WorkReport execute(WorkContext workContext);
}
