package com.nuark.trashbox;

import com.nuark.trashbox.api.Users;
import com.nuark.trashbox.models.User;
import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        trygetuser("IvS");
        trygetuser("Rost");
    }

    private static void trygetuser(String nick) {
        User usr;
        try {
            usr = Users.getUser(nick);
            System.out.println("Whois:");
            System.out.println(usr.getNick());
            System.out.println("\nInfo:");
            for (String inf : usr.getInfo()) System.out.println(inf);
            System.out.println("\nStats:");
            for (String stat : usr.getStats()) System.out.println(stat);
        } catch (IOException e) { /* IGNORED */ }
    }
    
}
