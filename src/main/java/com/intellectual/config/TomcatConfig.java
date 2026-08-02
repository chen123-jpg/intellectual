package com.intellectual.config;

import org.apache.catalina.connector.Connector;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TomcatConfig {

    /** 额外开一个 5051 端口专供 WebSocket 连接 */
    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> wsConnector() {
        return factory -> {
            Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
            connector.setPort(5051);
            factory.addAdditionalTomcatConnectors(connector);
        };
    }
}
