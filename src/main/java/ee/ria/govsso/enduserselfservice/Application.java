package ee.ria.govsso.enduserselfservice;

import co.elastic.apm.attach.ElasticApmAttacher;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
@ConfigurationPropertiesScan
public class Application extends SpringBootServletInitializer {

    public static void main(String[] args) {
        ElasticApmAttacher.attach();
        SpringApplication.run(Application.class, args);
    }

    // Support servlet container as described in https://docs.spring.io/spring-boot/docs/2.7.x/reference/htmlsingle/#howto.traditional-deployment.war
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        ElasticApmAttacher.attach();
        return application.sources(Application.class);
    }
}
