package xyz.nuark.trashbox.models;

public class User {
    String nick, stats, info;

    public User(String nick, String info, String stats) {
        this.nick = nick;
        this.info = info;
        this.stats = stats;
    }

    public String getNick() {
        return nick;
    }

    public String getInfo() {
        return info;
    }

    public String getStats() {
        return stats;
    }
}
