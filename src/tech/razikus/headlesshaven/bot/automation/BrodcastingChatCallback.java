package tech.razikus.headlesshaven.bot.automation;

import tech.razikus.headlesshaven.ChatCallback;
import tech.razikus.headlesshaven.ChatMessage;
import tech.razikus.headlesshaven.WebHavenSessionManager;

public class BrodcastingChatCallback extends ChatCallback {
    private String sessionId;
    private WebHavenSessionManager manager;

    public BrodcastingChatCallback(WebHavenSessionManager manager, String sessionId) {
        this.manager = manager;
        this.sessionId = sessionId;
    }

    @Override
    public void onChatMessage(ChatMessage message) {
        this.manager.broadcastChat(this.sessionId, message);

    }
}
