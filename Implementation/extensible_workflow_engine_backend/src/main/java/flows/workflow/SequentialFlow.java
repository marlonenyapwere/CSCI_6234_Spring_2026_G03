package flows.workflow;

import flows.work.Work;
import flows.work.WorkContext;
import flows.work.WorkReport;
import flows.work.WorkStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SequentialFlow extends AbstractWorkFlow {

    private static final Logger LOGGER = LoggerFactory.getLogger(SequentialFlow.class.getName());

    private final List<Work> workUnits = new ArrayList<>();

    SequentialFlow(String name, List<Work> workUnits) {
        super(name);
        this.workUnits.addAll(workUnits);
    }

    public WorkReport execute(WorkContext workContext) {
        WorkReport workReport = null;
        for (Work work : workUnits) {
            workReport = work.execute(workContext);
            if (workReport != null && WorkStatus.FAILED.equals(workReport.getStatus())) {
                LOGGER.info("Work unit ''{}'' has failed, skipping subsequent work units", work.getName());
                break;
            }
        }
        return workReport;
    }

    public static class Builder {

        private Builder() {
            // force usage of static method aNewSequentialFlow
        }

        public static NameStep aNewSequentialFlow() {
            return new BuildSteps();
        }

        public interface NameStep extends ExecuteStep {
            ExecuteStep named(String name);
        }

        public interface ExecuteStep {
            ThenStep execute(Work initialWork);
            ThenStep execute(List<Work> initialWorkUnits);
        }

        public interface ThenStep {
            ThenStep then(Work nextWork);
            ThenStep then(List<Work> nextWorkUnits);
            SequentialFlow build();
        }

        private static class BuildSteps implements NameStep, ExecuteStep, ThenStep {

            private String name;
            private final List<Work> works;
            
            BuildSteps() {
                this.name = UUID.randomUUID().toString();
                this.works = new ArrayList<>();
            }
            
            public ExecuteStep named(String name) {
                this.name = name;
                return this;
            }

            @Override
            public ThenStep execute(Work initialWork) {
                this.works.add(initialWork);
                return this;
            }

            @Override
            public ThenStep execute(List<Work> initialWorkUnits) {
                this.works.addAll(initialWorkUnits);
                return this;
            }

            @Override
            public ThenStep then(Work nextWork) {
                this.works.add(nextWork);
                return this;
            }

            @Override
            public ThenStep then(List<Work> nextWorkUnits) {
                this.works.addAll(nextWorkUnits);
                return this;
            }

            @Override
            public SequentialFlow build() {
                return new SequentialFlow(this.name, this.works);
            }
        }
    }
}
