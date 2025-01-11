package com.smashspot.websocketchat.controller;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpSession;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.springframework.stereotype.Component;

import com.google.gson.Gson;
//import com.smashspot.admin.model.AdmVO;
import com.smashspot.websocketchat.chat.ChatMessage;
import com.smashspot.websocketchat.chat.ChatMessageService;
import com.smashspot.websocketchat.chat.HttpSessionConfigurator;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@ServerEndpoint(value = "mem/websocket/chat/{memname}", configurator = HttpSessionConfigurator.class)
public class MemChatController {

	private final ChatMessageService chatMessageService;
	private static final Map<String, Session> sessionsMap = new ConcurrentHashMap<>();
	private final Gson gson = new Gson();

	/**
	 * WebSocket 連接建立時的回調
	 */
	@OnOpen
	public void onOpen(@PathParam("admname") String admname, Session session, EndpointConfig config) {
		HttpSession httpSession = (HttpSession) config.getUserProperties().get(HttpSession.class.getName());
		if (httpSession == null || httpSession.getAttribute("loginAdm") == null) {
			try {
				session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Unauthorized admin"));
			} catch (Exception e) {
				e.printStackTrace();
			}
			return;
		}

		// 驗證成功，繼續處理
		sessionsMap.put(admname, session);
		System.out.println(admname + " connected as admin.");
	}

	/**
	 * WebSocket 接收到訊息時的回調
	 */
	@OnMessage
	public void onMessage(String message, Session session) {
		try {
			ChatMessage chatMessage = gson.fromJson(message, ChatMessage.class);
			chatMessageService.save(chatMessage);

			// 發送給接收者
			String senderName = chatMessage.getSenderName();
			Session senderSession = sessionsMap.get(senderName);
			if (senderSession != null && senderSession.isOpen()) {
				senderSession.getBasicRemote().sendText(message);
			} else {
				// 保存為離線訊息
				System.out.println("Receiver " + senderName + " is offline. Storing message.");
				chatMessageService.save(chatMessage); // 離線訊息儲存（可擴展專門的離線訊息處理邏輯）
			}
		} catch (Exception e) {
			System.err.println("Error processing message: " + e.getMessage());
		}
	}

	/**
	 * WebSocket 錯誤處理
	 */
	@OnError
	public void onError(Session session, Throwable throwable) {
		System.err.println("WebSocket error: " + throwable.getMessage());
	}

	/**
	 * WebSocket 連接關閉時的回調
	 */
	@OnClose
	public void onClose(Session session, CloseReason closeReason) {
		String disconnectedUser = null;
		for (Map.Entry<String, Session> entry : sessionsMap.entrySet()) {
			if (entry.getValue().equals(session)) {
				disconnectedUser = entry.getKey();
				sessionsMap.remove(entry.getKey());
				break;
			}
		}
		if (disconnectedUser != null) {
			System.out.println(disconnectedUser + " disconnected. Reason: " + closeReason.getReasonPhrase());
		}
	}

}