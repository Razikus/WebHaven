package tech.razikus.headlesshaven;

import java.util.ArrayList;

public class WebHavenState {
    private ArrayList<PseudoObject> playersAround;
    private ArrayList<String> chatChannels;

    public WebHavenState(ArrayList<PseudoObject> playersAround, ArrayList<String> chatChannels) {
        this.playersAround = playersAround;
        this.chatChannels = chatChannels;
    }

    public ArrayList<String> getChatChannels() {
        return chatChannels;
    }

    public void setChatChannels(ArrayList<String> chatChannels) {
        this.chatChannels = chatChannels;
    }

    public ArrayList<PseudoObject> getPlayersAround() {
        return playersAround;
    }

    public void setPlayersAround(ArrayList<PseudoObject> playersAround) {
        this.playersAround = playersAround;
    }
}
