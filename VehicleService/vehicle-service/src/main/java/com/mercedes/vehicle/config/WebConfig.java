package com.mercedes.vehicle.config;
import com.mercedes.vehicle.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.*;import org.springframework.boot.web.servlet.FilterRegistrationBean;
@Configuration public class WebConfig {
 @Bean FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration(JwtAuthenticationFilter filter){FilterRegistrationBean<JwtAuthenticationFilter> bean=new FilterRegistrationBean<>(filter);bean.setEnabled(false);return bean;}
}
