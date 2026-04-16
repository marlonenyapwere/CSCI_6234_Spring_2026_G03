package flows;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "flows")
@OpenAPIDefinition(
        info = @Info(
                title = "Extensible Workflow Engine REST API Documentation",
                description = "Extensible Workflow Engine REST API Documentation",
                version = "v1",
                contact = @Contact(
                        name = "Marlone Nyapwere",
                        email = "marlone.nyapwere@gwu.edu",
                        url = "marlonenyapwere@gwu.edu"
                ),
                license = @License(
                        name = "Apache 2.0",
                        url = "marlonenyapwere@gwu.edu"
                )
        ),
        externalDocs = @ExternalDocumentation(
                description =  "Extensible Workflow Engine REST API Documentation",
                url = "https://www.marlonenyapwere.com/swagger-ui.html"
        )
)
public class WorkflowApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(WorkflowApiApplication.class, args);
    }
}