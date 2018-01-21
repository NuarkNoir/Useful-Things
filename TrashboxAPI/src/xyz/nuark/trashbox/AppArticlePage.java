package xyz.nuark.trashbox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import xyz.nuark.trashbox.models.AppArticle;

public class AppArticlePage {
    private final String link, image;

    public AppArticlePage(String link, String image) {
        this.link = link;
        this.image = image;
    }
    
    public AppArticle getArticle() throws IOException{
        Elements d = Jsoup.connect(this.link).get().body().select("div.div_topic");
        
        String title, who, when, content;
        Map<String, String> downloadsUrls = new TreeMap(Collections.reverseOrder());
        ArrayList<String> screenshotsUrl = new ArrayList();
        
        title = d.select("h1.h_topic_caption").text().trim();
        who = d.select("span a[href~=/users/]").text().trim();
        when = d.select("time[itemprop=dateModified]").text().trim();
        
        Elements screenshotsImgs = d.select("div.div_full_screens div.div_image_screenshot a");
        for (Element screenshot : screenshotsImgs){
            screenshotsUrl.add(screenshot.attr("href"));
        }
        Element c = d.select("div.div_text_content").first();
        c.select("div[id~=div_soc_]").remove();
        c.select("script").remove();
        content = c.html().trim();
        Elements filesDiv = d.select("div.div_files div.div_file_download");
        for (Element file : filesDiv.select("a.div_file_download_button1")){
            downloadsUrls.put(file.text(), file.attr("href"));
        }
        AppArticle aa = new AppArticle(title, this.image, who, when, content, downloadsUrls, screenshotsUrl);
        return aa;
    }
}
