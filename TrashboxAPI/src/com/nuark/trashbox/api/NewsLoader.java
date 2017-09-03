package com.nuark.trashbox.api;

import com.nuark.trashbox.Globals.Statics;
import com.nuark.trashbox.models.News;
import com.nuark.trashbox.utils.Enumerators.Sort;
import java.io.IOException;
import java.util.ArrayList;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class NewsLoader {
    String url;

    public NewsLoader(String url, Sort sort) {
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
    
    public ArrayList<News> Load() throws IOException {
        ArrayList<News> news = new ArrayList<>();
        Elements d;
        if (Statics.getCu() != null) d = Jsoup.connect(url).cookies(Statics.getCu().getCookies()).get().body().select(".div_text_cat_topics");
        else d = Jsoup.connect(url).get().body().select(".div_text_cat_topics");
        for (Element els : d.select(".div_topic_cover")){
            String title = els.select(".div_topic_cover_caption").first().text();
            String topicLink = Statics.getMainUrl() + els.select(".a_topic_cover").first().attr("href");
            String imageLink = Statics.getMainUrl() + els.select(".img_topic_cover").first().attr("style")
                    .trim().replace("&quot;", "\"").replaceFirst("(.*?)(\\(\")", "").replaceFirst("(\"\\)).*", "");
            news.add(new News(title, topicLink, imageLink));
        }
        return news;
    }
}
