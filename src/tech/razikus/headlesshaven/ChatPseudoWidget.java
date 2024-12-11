package tech.razikus.headlesshaven;

import java.util.ArrayList;

public class ChatPseudoWidget extends PseudoWidget{
    private String chatName = "";
    private PseudoWidgetManager manager;
    private ArrayList<ChatCallback> callbacks = new ArrayList<>();
    public ChatPseudoWidget(PseudoWidget original, PseudoWidgetManager manager) {
        super(original);
        this.chatName = (String) original.getCargs()[0];
        this.manager = manager;
    }

    public String getChatName() {
        return chatName;
    }

    public void addCallback(ChatCallback callback) {
        this.callbacks.add(callback);
    }

    public void removeCallback(ChatCallback callback) {
        this.callbacks.remove(callback);
    }

    public boolean isAreaChat() {
        return chatName.equals("Area Chat");
    }

    public boolean isVillageChat() {
        return this.manager.getVillageName() != null && this.chatName.equals(this.manager.getVillageName());
    }

    public void sendMessage(String what) {
        this.WidgetMsg("msg", what);
    }


    @Override
    public void ReceiveMessage(WidgetMessage message) {
        switch (message.getName()) {
            case "msg":
                Number from = (Number)message.getArgs()[0];
                String line = (String)message.getArgs()[1];
                if(from == null) {
                    // handle my own message
                } else {
                    Integer fromInt = from.intValue();
                    String visibleName = "???";
                    if(this.manager.getInstantiatedBuddy() != null) {
                        visibleName = this.manager.getInstantiatedBuddy().getBuddyName(fromInt);
                    }

                    for(ChatCallback cb: callbacks) {
                        ChatMessage messageAppeared = new ChatMessage(line, visibleName, this.chatName);
                        cb.onChatMessage(messageAppeared);
                    }

                }
                break;
            default:
                System.out.println("UNHANDLED CHAT MESSAGE:" + message);
        }
    }
}
