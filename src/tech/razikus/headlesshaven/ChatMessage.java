package tech.razikus.headlesshaven;

import java.util.Objects;

public class ChatMessage {
    private String line;
    private String from;
    private String channel;

    public ChatMessage(String line, String from, String channel) {
        this.line = line;
        this.from = from;
        this.channel = channel;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatMessage that = (ChatMessage) o;
        return Objects.equals(line, that.line) && Objects.equals(from, that.from) && Objects.equals(channel, that.channel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(line, from, channel);
    }

    @Override
    public String toString() {
        return "ChatMessage{" +
                "line='" + line + '\'' +
                ", from='" + from + '\'' +
                ", channel='" + channel + '\'' +
                '}';
    }
}
