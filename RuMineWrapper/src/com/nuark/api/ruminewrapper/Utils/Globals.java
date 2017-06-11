package com.nuark.api.ruminewrapper.Utils;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;

public class Globals {
    private static final String rumine_url = "https://ru-minecraft.ru/";
    public static boolean isAPIInited = false;
    
    public static String getForumEndpoint(String topic_id){
        return rumine_url + "forum/showtopic-" + topic_id + "/";
    }
    
    public static String getForumLatestPage(String forum_url){
        try {
            return Jsoup.connect(forum_url).get().select("").first().text();
        } catch (IOException ex) {
            Logger.getLogger(Globals.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "0";
    }
    
    public static class SavedState{
        private static String topic_id, forum_url, topic_last_page;
        
        public static void Save(String t_id, String f_url, String t_lastpage){
            topic_id = t_id;
            forum_url = f_url;
            topic_last_page = t_lastpage;
        }

        public static String getTopic_id() {
            return topic_id;
        }

        public static String getForum_url() {
            return forum_url;
        }

        public static String getTopic_last_page() {
            return topic_last_page;
        }
    }
}
