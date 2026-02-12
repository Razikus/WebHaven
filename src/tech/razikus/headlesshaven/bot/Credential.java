package tech.razikus.headlesshaven.bot;

import java.util.Objects;

public class Credential {
    private String username;
    private String password;
    private String charname;

    public Credential(String username, String password, String charname) {
        this.username = username;
        this.password = password;
        this.charname = charname;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getCharname() {
        return charname;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Credential that = (Credential) o;
        return Objects.equals(username, that.username) && Objects.equals(password, that.password) && Objects.equals(charname, that.charname);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, password, charname);
    }

    @Override
    public String toString() {
        return "Credential{" +
                "username='" + username + '\'' +
                ", password=:)'" +
                ", charname='" + charname + '\'' +
                '}';
    }
}
