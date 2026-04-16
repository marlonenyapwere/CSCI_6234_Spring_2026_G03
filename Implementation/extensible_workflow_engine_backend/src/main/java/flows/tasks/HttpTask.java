package flows.tasks;

import flows.work.*;

import flows.work.DefaultWorkReport;
import flows.work.Work;
import flows.work.WorkContext;
import flows.work.WorkReport;
import flows.work.WorkStatus;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class HttpTask implements Work {

    private final String url;
    private final String method;
    private final Map<String, String> headers;
    private final String requestBody;

    public HttpTask(String url,
                    String method,
                    Map<String, String> headers,
                    String requestBody) {
        this.url = url;
        this.method = method == null ? "GET" : method.toUpperCase();
        this.headers = headers;
        this.requestBody = requestBody;
    }

    @Override
    public String getName() {
        return "HttpTask";
    }

    @Override
    public WorkReport execute(WorkContext context) {
        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(url));

            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    builder.header(entry.getKey(), entry.getValue());
                }
            }

            switch (method) {
                case "POST":
                    builder.POST(HttpRequest.BodyPublishers.ofString(
                            requestBody == null ? "" : requestBody));
                    break;
                case "PUT":
                    builder.PUT(HttpRequest.BodyPublishers.ofString(
                            requestBody == null ? "" : requestBody));
                    break;
                case "DELETE":
                    builder.DELETE();
                    break;
                default:
                    builder.GET();
                    break;
            }

            HttpResponse<String> response =
                    client.send(builder.build(), HttpResponse.BodyHandlers.ofString());

            Object dataObj = context.get("data");
            if (dataObj instanceof Map<?, ?> map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) map;
                data.put("httpStatusCode", response.statusCode());
                data.put("httpResponseBody", response.body());
            }

            System.out.println("HTTP " + method + " " + url + " -> " + response.statusCode());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return new DefaultWorkReport(WorkStatus.COMPLETED, context);
            }

            return new DefaultWorkReport(
                    WorkStatus.FAILED,
                    context,
                    new RuntimeException("HTTP request failed with status " + response.statusCode())
            );

        } catch (Exception e) {
            return new DefaultWorkReport(WorkStatus.FAILED, context, e);
        }
    }
}