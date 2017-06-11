package com.nuark.api.ruminewrapper.RumineAPI;

import com.nuark.api.ruminewrapper.Utils.Globals;

public class RumineAPI {

    private final String topic_id, forum_url;
    private String topic_last_page = "0";

    public RumineAPI(String topic_id) {
        this.topic_id = topic_id;
        forum_url = Globals.getForumEndpoint(topic_id);
    }

    public void Init() {
        new Runnable() {
            @Override
            public void run() {
                topic_last_page = Globals.getForumLatestPage(forum_url);
                System.out.println("[SUCC]OK! Topic's last page is " + topic_last_page);
                SaveState();
                Globals.isAPIInited = true;
            }
        }.run();
    }
    
    public void SaveState(){
        System.out.println("Saving state...");
        if (topic_last_page.equals("0")) System.out.println("[WARN]Topic's last page equals 0. Any problems?");
        Globals.SavedState.Save(topic_id, forum_url, topic_last_page);
    }
}
