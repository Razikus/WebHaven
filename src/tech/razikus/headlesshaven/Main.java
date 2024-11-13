package tech.razikus.headlesshaven;


public class Main {


    public static void main2(String[] args) throws InterruptedException {
        // MINIMAL VERSION TO RUN
        String username = "";
        String password = "";
        String altname = "";
        WebHavenSession session = new WebHavenSession(username, password, altname);
        session.authenticate();

        Thread sessionThread = new Thread(session);
        sessionThread.start();
    }


}