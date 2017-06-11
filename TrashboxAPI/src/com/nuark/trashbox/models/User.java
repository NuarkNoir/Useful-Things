package com.nuark.trashbox.models;

import java.util.ArrayList;

public class User {
    String nick;
    ArrayList<String> stats, info;

    public User(String nick, ArrayList<String> info, ArrayList<String> stats) {
        this.nick = nick;
        this.info = info;
        this.stats = stats;
    }

    public String getNick() {
        return nick;
    }

    public ArrayList<String> getInfo() {
        info.remove(info.size()-2);
        return info;
    }

    public ArrayList<String> getStats() {
        return stats;
    }
}
