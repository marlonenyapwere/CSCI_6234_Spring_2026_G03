package flows.tasks;

import flows.work.*;

public class HttpTask implements Work {

    private final String url;
    private final String method;

    public HttpTask(String url, String method) {
        this.url = url;
        this.method = method;
    }

    public WorkReport execute(WorkContext ctx) {
        System.out.println("HTTP " + method + " -> " + url);
        return new DefaultWorkReport(WorkStatus.COMPLETED, ctx);
    }
}
