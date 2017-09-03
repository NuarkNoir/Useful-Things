package com.nuark.trashbox.utils;

import java.io.IOException;
import org.jsoup.Jsoup;

public class GetLatestPage {
    public String Get(String url) throws IOException {
        return Jsoup.connect(url).get().select("div.div_navigator_new span.span_item_active").first().text();
    }
}
