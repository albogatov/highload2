import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {

    // TODO: Each microservice main class must contain annotation EnableDiscoveryClient to be connected to Eureka Server
    // TODO: Each microservice must contain parameter eureka.client.service-url.defaultZone=http://localhost:8761/eureka/ in its properties
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }

}

