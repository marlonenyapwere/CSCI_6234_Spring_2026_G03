
package flows.workflow;

import flows.work.Work;
import flows.work.WorkContext;
import flows.work.WorkReport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

public class ParallelFlow extends AbstractWorkFlow {

    private final List<Work> workUnits = new ArrayList<>();
    private final ParallelFlowExecutor workExecutor;

    ParallelFlow(String name, List<Work> workUnits, ParallelFlowExecutor parallelFlowExecutor) {
        super(name);
        this.workUnits.addAll(workUnits);
        this.workExecutor = parallelFlowExecutor;
    }


    public ParallelFlowReport execute(WorkContext workContext) {
        ParallelFlowReport workFlowReport = new ParallelFlowReport();
        List<WorkReport> workReports = workExecutor.executeInParallel(workUnits, workContext);
        workFlowReport.addAll(workReports);
        return workFlowReport;
    }

    public static class Builder {

        private Builder() {
            // force usage of method aNewParallelFlow
        }
        
        public static NameStep aNewParallelFlow() {
            return new BuildSteps();
        }

        public interface NameStep extends ExecuteStep {
            ExecuteStep named(String name);
        }

        public interface ExecuteStep {
            WithStep execute(Work... workUnits);
        }

        public interface WithStep {
            BuildStep with(ExecutorService executorService);
        }

        public interface BuildStep {
            ParallelFlow build();
        }

        private static class BuildSteps implements NameStep, ExecuteStep, WithStep, BuildStep {

            private String name;
            private final List<Work> works;
            private ExecutorService executorService;

            public BuildSteps() {
                this.name = UUID.randomUUID().toString();
                this.works = new ArrayList<>();
            }

            @Override
            public ExecuteStep named(String name) {
                this.name = name;
                return this;
            }

            @Override
            public WithStep execute(Work... workUnits) {
                this.works.addAll(Arrays.asList(workUnits));
                return this;
            }
            
            @Override
            public BuildStep with(ExecutorService executorService) {
                this.executorService = executorService;
                return this;
            }

            @Override
            public ParallelFlow build() {
                return new ParallelFlow(
                        this.name, this.works,
                        new ParallelFlowExecutor(this.executorService));
            }
        }

    }
}
