package com.swyp14.phocamatch.chat.config;

import com.swyp14.phocamatch.chat.interceptor.StompJwtChannelInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompJwtChannelInterceptor
            stompJwtChannelInterceptor;

    @Override
    public void registerStompEndpoints(
            StompEndpointRegistry registry
    ) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(
                        "http://localhost:3000"
                );
    }

    @Override
    public void configureMessageBroker(
            MessageBrokerRegistry registry
    ) {
        /*
         * 클라이언트가 서버의 @MessageMapping 메서드로
         * 메시지를 보낼 때 사용하는 prefix
         */
        registry.setApplicationDestinationPrefixes(
                "/pub"
        );

        /*
         * 클라이언트가 서버 메시지를 받기 위해
         * 구독하는 prefix
         */
        registry.enableSimpleBroker(
                "/sub"
        );
    }

    @Override
    public void configureClientInboundChannel(
            ChannelRegistration registration
    ) {
        registration.interceptors(
                stompJwtChannelInterceptor
        );
    }
}
