package xyz.nuark.trashbox.models;

import java.util.ArrayList;
import java.util.Map;

public class AppArticle {
    String title, image, who, when, articleContent;
    Map<String, String> downloadLinks;
    ArrayList<String> screenshotsUrl;

    public AppArticle(String title, String image, String who, String when, String articleContent, Map<String, String> downloadLinks, ArrayList<String> screenshotsUrl) {
        this.title = title;
        this.image = image;
        this.who = who;
        this.when = when;
        this.articleContent = articleContent;
        this.downloadLinks = downloadLinks;
        this.screenshotsUrl = screenshotsUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getImage() {
        return image;
    }

    public String getWho() {
        return who;
    }

    public String getWhen() {
        return when;
    }

    public String getArticleContent() {
        return articleContent;
    }

    public Map<String, String> getDownloadLinks() {
        return downloadLinks;
    }

    public ArrayList<String> getScreenshotsUrl() {
        return screenshotsUrl;
    }
    
    public void debug_info(){
        String info = "";
        info += "Title: " + title + "\n";
        info += "Image: " + image + "\n";
        info += "Who: " + who + "\n";
        info += "When: " + when + "\n";
        info += "Content lenght: " + articleContent.length() + "\n";
        info += "Links: " + downloadLinks.toString() + "\n";
        info += "Screenshots: " + screenshotsUrl.toString() + "\n";
        info += "\n";
        System.out.println(info);
    }
}
