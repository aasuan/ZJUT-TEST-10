package com.sky.task;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sky.websocket.WebSocketServer;

/**
 * M5 — {@link WebSocketTask} 定时推送文案（Mock {@link WebSocketServer}）。
 */
@ExtendWith(MockitoExtension.class)
class WebSocketTaskTest {

    @Mock
    private WebSocketServer webSocketServer;

    @InjectMocks
    private WebSocketTask webSocketTask;

    @Test
    void sendMessageToClient_prefixesServerMessageAndTime() {
        webSocketTask.sendMessageToClient();

        verify(webSocketServer).sendToAllClient(argThat(msg ->
                msg != null
                        && msg.startsWith("这是来自服务端的消息：")
                        && msg.length() > "这是来自服务端的消息：".length()));
    }
}
