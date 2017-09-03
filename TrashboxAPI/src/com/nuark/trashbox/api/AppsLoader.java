package com.nuark.trashbox.api;

import com.nuark.trashbox.Globals.Statics;
import com.nuark.trashbox.models.App;
import com.nuark.trashbox.utils.Enumerators.Sort;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class AppsLoader {
    String url;

    public AppsLoader(String url, Sort sort) {
        if (sort == null) ;
        switch (sort) {
            default:
            case Recomendation:
                this.url = url;
                break;
            case Date:
                this.url = url + "?sort=edited";
                break;
            case Rating:
                this.url = url + "?sort=votes";
                break;
        }
    }
    
    public ArrayList<App> Load() throws IOException {
        ArrayList<App> apps = new ArrayList<>();
        Elements d;
        if (Statics.getCu() != null) d = Jsoup.connect(url).cookies(Statics.getCu().getCookies()).get().body().select(".div_content_cat_topics");
        else d = Jsoup.connect(url).get().body().select(".div_content_cat_topics");
        for (Element els : d.select(".div_topic_cat_content")){
            String title = els.select(".div_topic_tcapt_content").first().text();
            String androidVersion = els.select(".div_topic_cat_tag_os_android").first().text();
            String topicLink = els.select(".div_topic_cat_content .a_topic_content").first().attr("href");
            String imageLink = els.select(".div_topic_content_icon").first().attr("style")
                    .trim().replace("&quot;", "\"").replaceFirst("(.*?)(\\(\")", "").replace("\");", "");
            ArrayList<String> tagList = new ArrayList<>();
            tagList.addAll(Arrays.asList(els.select(".div_topic_cat_tags").first().text().split(", ")));
            if (els.text().contains("Требуется ROOT")) title = "®" + title;
            apps.add(new App(title, androidVersion, topicLink, imageLink, tagList));
        }
        return apps;
    }
}
