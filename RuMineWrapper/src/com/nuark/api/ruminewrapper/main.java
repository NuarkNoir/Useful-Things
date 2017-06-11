package com.nuark.api.ruminewrapper;

import com.nuark.api.ruminewrapper.RumineAPI.RumineAPI;
import com.nuark.api.ruminewrapper.Utils.Globals;

public class main {

    public static void main(String[] args) {
        RumineAPI api = new RumineAPI("15781");
        api.Init();
        while (!Globals.isAPIInited) {  }
        System.out.println(Globals.SavedState.getForum_url());
        System.out.println(Globals.SavedState.getTopic_id());
        System.out.println(Globals.SavedState.getTopic_last_page());
    }
}
