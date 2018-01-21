package xyz.nuark.trashbox.api;

import xyz.nuark.trashbox.models.User;
import xyz.nuark.trashbox.Globals.Statics;
import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Users {
    public static User getUser(String nick) throws IOException{
        Elements d = Jsoup.connect(Statics.getUserURL() + nick).get().select("#div_page_left");
        try {
            nick += d.select("td > div span").first().text();
            d.select("td > div span").remove();
        } catch (NullPointerException e){  }
        String info = Jsoup.parse(d.select("td > div").first().html().replace("<br>", "")).text();
        String stats =  "";
        for (Element el : d.select(".div_submenu_user_topics").first().select("a")){
            stats += el.text().replace("  ", ": ") + "\n";
        }
        return new User(nick, info, stats);
    }
}