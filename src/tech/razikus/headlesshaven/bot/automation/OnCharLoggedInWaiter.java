package tech.razikus.headlesshaven.bot.automation;

import tech.razikus.headlesshaven.WebHavenSession;

public class OnCharLoggedInWaiter implements OnCharLoggedIn{

    private WebHavenSession session;


    public WebHavenSession waitForSession() {
        while (this.session == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return this.session;
    }

    @Override
    public void onCharLoggedIn(WebHavenSession session) {
        this.session = session;

    }
}
