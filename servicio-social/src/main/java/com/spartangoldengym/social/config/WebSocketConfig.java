package com.spartangoldengym.social.config;

import com.spartangoldengym.social.websocket.WorkoutSyncHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final WorkoutSyncHandler workoutSyncHandler;

    public WebSocketConfig(WorkoutSyncHandler workoutSyncHandler) {
        this.workoutSyncHandler = workoutSyncHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(workoutSyncHandler, "/ws/workout-sync")
                .setAllowedOrigins("*");
    }
}
