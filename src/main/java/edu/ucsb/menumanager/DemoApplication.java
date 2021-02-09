package edu.ucsb.menumanager;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = "classpath:secrets-localhost.properties", ignoreResourceNotFound = true)
@SpringBootApplication
public class DemoApplication {

  public static void main(String[] args) {
    SpringApplication app = new SpringApplication(DemoApplication.class);
    Map<String, Object> defaults = new HashMap<String, Object>();
    defaults.put("app.namespace", "https://FAKE-NAMESPACE.example.org");
    defaults.put("app.admin.emails", "phtcon@ucsb.edu");
    defaults.put("auth0.domain", "YOUR-AUTH0-TENANT.us.auth0.com");
    defaults.put("security.oauth2.resource.id", defaults.get("app.namespace"));
    defaults.put("security.oauth2.resource.jwk.keySetUri", "https://" + defaults.get("auth0.domain") + "/.well-known/jwks.json");
    app.setDefaultProperties(defaults);
    app.run(args);
  }

}
