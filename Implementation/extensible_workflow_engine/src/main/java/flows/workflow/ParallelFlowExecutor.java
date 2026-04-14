
package flows.workflow;

import flows.work.Work;
import flows.work.WorkContext;
import flows.work.WorkReport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

class ParallelFlowExecutor {

    private final ExecutorService workExecutor;

    ParallelFlowExecutor(ExecutorService workExecutor) {
        this.workExecutor = workExecutor;
    }

    List<WorkReport> executeInParallel(List<Work> workUnits, WorkContext workContext) {
        // prepare tasks for parallel submission
        List<Callable<WorkReport>> tasks = new ArrayList<>(workUnits.size());
        workUnits.forEach(work -> tasks.add(() -> work.execute(workContext)));

        // submit work units and wait for results
        List<Future<WorkReport>> futures;
        try {
            futures = this.workExecutor.invokeAll(tasks);
        } catch (InterruptedException e) {
            throw new RuntimeException("The parallel flow was interrupted while executing work units", e);
        }
        Map<Work, Future<WorkReport>> workToReportFuturesMap = new HashMap<>();
        for (int index = 0; index < workUnits.size(); index++) {
            workToReportFuturesMap.put(workUnits.get(index), futures.get(index));
        }

        // gather reports
        List<WorkReport> workReports = new ArrayList<>();
        for (Map.Entry<Work, Future<WorkReport>> entry : workToReportFuturesMap.entrySet()) {
            try {
                workReports.add(entry.getValue().get());
            } catch (InterruptedException e) {
                String message = String.format("The parallel flow was interrupted while waiting for the result of work unit '%s'", entry.getKey().getName());
                throw new RuntimeException(message, e);
            } catch (ExecutionException e) {
                String message = String.format("Unable to execute work unit '%s'", entry.getKey().getName());
                throw new RuntimeException(message, e);
            }
        }

        return workReports;
    }
}
