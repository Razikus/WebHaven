package tech.razikus.headlesshaven;

public class SimpleAuthResponse {
    private String username;
    private byte[] cookie;

    public SimpleAuthResponse(String username, byte[] cookie) {
        this.username = username;
        this.cookie = cookie;
    }

    public SimpleAuthResponse() {
        this.username = null;
        this.cookie = null;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public byte[] getCookie() {
        return cookie;
    }

    public void setCookie(byte[] cookie) {
        this.cookie = cookie;
    }
}
