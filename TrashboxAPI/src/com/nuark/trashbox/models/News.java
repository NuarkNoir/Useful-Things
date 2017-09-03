package com.nuark.trashbox.models;

import java.util.ArrayList;

public class News {

    private String title, topicLink, imageLink;

    public News(String title, String topicLink, String imageLink) {
        this.title = title;
        this.topicLink = topicLink;
        this.imageLink = imageLink;
    }

    public String getTitle() {
        return title;
    }

    public String getTopicLink() {
        return topicLink;
    }

    public String getImageLink() {
        return imageLink;
    }
}
