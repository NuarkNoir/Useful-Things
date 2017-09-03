package com.nuark.trashbox.utils;

import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

public class LoadFullArticle {
    public Elements GenerateDataset(String url) throws IOException {
        return Jsoup.connect(url).get().select("article > div[id^=div_topic]");
    }
}