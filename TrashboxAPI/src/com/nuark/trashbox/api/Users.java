package com.nuark.trashbox.api;

import com.nuark.trashbox.models.User;
import java.io.IOException;
import java.util.ArrayList;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Users {
    static String userURL = "https://trashbox.ru/users/";
    
    public static User getUser(String nick) throws IOException{
        ArrayList<String> stats = new ArrayList<>();
        ArrayList<String> info = new ArrayList<>();
        Elements d = Jsoup.connect(userURL + nick).get().body().select("div.div_padding_10_mobile");
        String[] _info = d.select("div.div_padding_10_mobile table td:nth-child(2) > div").first().select("*").html().split("<br>");
        for (String inf : _info) 
            info.add(inf.trim());
        for (Element el : d.select("div.div_submenu_user_topics a")){
            stats.add(el.text().replace(" ", ": ").replace(" :", ":").replace(": ", ":"));
        }
        return new User(nick, info, stats);
    }
}