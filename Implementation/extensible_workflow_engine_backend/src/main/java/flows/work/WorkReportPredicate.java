package flows.work;

import java.util.concurrent.atomic.AtomicInteger;

@FunctionalInterface
public interface WorkReportPredicate {

    boolean apply(WorkReport workReport);

    WorkReportPredicate ALWAYS_TRUE = workReport -> true;
    WorkReportPredicate ALWAYS_FALSE = workReport -> false;
    WorkReportPredicate COMPLETED = workReport -> workReport.getStatus().equals(WorkStatus.COMPLETED);
    WorkReportPredicate FAILED = workReport -> workReport.getStatus().equals(WorkStatus.FAILED);

    class TimesPredicate implements WorkReportPredicate {

        private final int times;

        private final AtomicInteger counter = new AtomicInteger();

        public TimesPredicate(int times) {
            this.times = times;
        }

        @Override
        public boolean apply(WorkReport workReport) {
            return counter.incrementAndGet() != times;
        }

        public static TimesPredicate times(int times) {
            return new TimesPredicate(times);
        }
    }
}
