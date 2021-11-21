package com.chatapp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Configuration
public class FrontEndConfig {
    @Bean
    public RouterFunction<ServerResponse> indexRouter(@Value("classpath:/index.html") final Resource indexHtml){
        return  route(GET("/").or(GET("/index.html")),
                request -> ok().contentType(MediaType.TEXT_HTML).bodyValue(indexHtml));
    }
    @Bean
    public RouterFunction<ServerResponse> cssRouter() {
        return RouterFunctions
                .resources("/css/**", new ClassPathResource("css/"));
    }
    @Bean
    public RouterFunction<ServerResponse> jsRouter() {
        return RouterFunctions
                .resources("/js/**", new ClassPathResource("js/"));
    }
    @Bean
    public RouterFunction<ServerResponse> imgRouter() {
        return RouterFunctions
                .resources("/img/**", new ClassPathResource("img/"));
    }
}
