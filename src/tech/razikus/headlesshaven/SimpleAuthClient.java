package tech.razikus.headlesshaven;

import haven.AuthClient;
import haven.HackThread;

public class SimpleAuthClient {
    private String host = "game.havenandhearth.com";
    private int authPort = 1871;

    public SimpleAuthClient() {
    }

    public SimpleAuthClient(String host, int authPort) {
        this.host = host;
        this.authPort = authPort;
    }

    public SimpleAuthResponse getCookie(String username, String password) throws InterruptedException {
        SimpleAuthResponse response = new SimpleAuthResponse();

        byte[] cookie = new byte[]{};
        HackThread th = new HackThread(() -> {
            try {
                AuthClient client = new AuthClient(host, authPort);
                AuthClient.NativeCred cred = new AuthClient.NativeCred(username, password);
                String what = cred.tryauth(client);
                response.setCookie(client.getcookie());
                response.setUsername(what);

            } catch (Exception e) {
                System.out.println(e);
            }

        }, "th1");
        th.start();
        th.join();
        return response;
    }
}
