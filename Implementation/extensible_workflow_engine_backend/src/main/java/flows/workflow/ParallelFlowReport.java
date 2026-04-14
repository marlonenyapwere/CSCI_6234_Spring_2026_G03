package flows.workflow;

import flows.work.WorkContext;
import flows.work.WorkReport;
import flows.work.WorkStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class ParallelFlowReport implements WorkReport {

    private final List<WorkReport> reports;

    public ParallelFlowReport() {
        this(new ArrayList<>());
    }

    public ParallelFlowReport(List<WorkReport> reports) {
        this.reports = reports;
    }

    public List<WorkReport> getReports() {
        return reports;
    }

    void add(WorkReport workReport) {
        reports.add(workReport);
    }

    void addAll(List<WorkReport> workReports) {
        reports.addAll(workReports);
    }


    @Override
    public WorkStatus getStatus() {
        for (WorkReport report : reports) {
            if (report.getStatus().equals(WorkStatus.FAILED)) {
                return WorkStatus.FAILED;
            }
        }
        return WorkStatus.COMPLETED;
    }

    @Override
    public Throwable getError() {
        for (WorkReport report : reports) {
            Throwable error = report.getError();
            if (error != null) {
                return error;
            }
        }
        return null;
    }

    @Override
    public WorkContext getWorkContext() {
        WorkContext workContext = new WorkContext();
        for (WorkReport report : reports) {
            WorkContext partialContext = report.getWorkContext();
            for (Map.Entry<String, Object> entry : partialContext.getEntrySet()) {
                workContext.put(entry.getKey(), entry.getValue());
            }
        }
        return workContext;
    }
}
