package xyz.nuark.trashbox.api;

import xyz.nuark.trashbox.models.App;
import java.io.IOException;
import java.util.ArrayList;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class AppsLoader {
    private final String url;

    public AppsLoader(String url) {
        this.url = url;
    }
    
    public ArrayList<App> Load() throws IOException {
        ArrayList<App> apps = new ArrayList<>();
        Elements d = Jsoup.connect(this.url).get().body().select("div.div_content_cat_topics");
        for (Element app : d.select("div.div_topic_cat_content")){
            String title = "";
            if (app.text().contains("Требуется ROOT")) title += "®";
            title += app.select("span.div_topic_tcapt_content").first().text().trim();
            String androVer = app.select("span.div_topic_cat_tag_android").first().text().trim();
            String appUri = app.select("a.a_topic_content").first().attr("href");
            String imageLink = app.select("div.div_topic_content_icon").first().attr("style").trim()
                    .replaceFirst("(width: \\d*px; height: \\d*px; background-image: url\\(\\\")", "")
                    .replaceFirst("(\"\\);).*", "");
            ArrayList<String> tags = new ArrayList<>();
            for (Element tag : app.select("div.div_topic_cat_tags a")){
                tags.add(tag.text().trim());
            }
            App a = new App(title, androVer, appUri, imageLink, tags);
            apps.add(a);
        }
        return apps;
    }
}
