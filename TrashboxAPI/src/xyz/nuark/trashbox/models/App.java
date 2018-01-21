package xyz.nuark.trashbox.models;

import java.util.ArrayList;

public class App {

    private final String title, androidVersion, topicLink, imageLink;
    private final ArrayList<String> tagList;

    public App(String title, String androidVersion, String topicLink, String imageLink, ArrayList<String> tagList) {
        this.title = title;
        this.androidVersion = androidVersion;
        this.topicLink = topicLink;
        this.imageLink = imageLink;
        this.tagList = tagList;
    }

    public String getTitle() {
        return title;
    }

    public String getAndroidVersion() {
        return androidVersion;
    }

    public String getTopicLink() {
        return topicLink;
    }

    public String getImageLink() {
        return imageLink;
    }

    public ArrayList<String> getTagList() {
        return tagList;
    }
    
    public void debug_info(){
        String info = "";
        info += "Title: " + title + "\n";
        info += "Version: " + androidVersion + "\n";
        info += "Topic: " + topicLink + "\n";
        info += "Link: " + imageLink + "\n";
        info += "Tags: " + tagList.toString() + "\n";
        info += "\n";
        System.out.println(info);
    }
}
