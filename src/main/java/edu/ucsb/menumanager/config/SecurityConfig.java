package edu.ucsb.menumanager.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;

@Configuration
@EnableResourceServer
public class SecurityConfig extends ResourceServerConfigurerAdapter {

  @Value("${security.oauth2.resource.id}")
  private String resourceId;

  @Override
  public void configure(HttpSecurity http) throws Exception {
    http.authorizeRequests().mvcMatchers("/api/public").permitAll().mvcMatchers("/api/**")
        .authenticated().anyRequest().permitAll();
  }

  @Override
  public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
    resources.resourceId(resourceId);
  }
}
