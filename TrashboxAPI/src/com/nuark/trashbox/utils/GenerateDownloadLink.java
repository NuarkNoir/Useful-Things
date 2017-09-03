package com.nuark.trashbox.utils;

import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class GenerateDownloadLink {
    public static String Generate(Elements elements) throws IOException {
        Document d = Jsoup.connect(elements.select("a.div_topic_top_download_button").first().attr("href")).get();
        String url = d.select("#div_landing_button_zone script").first().html();
        String[] components = url.replace("show_landing_link2(", "").replace(");", "").replace("'", "").split(",");
        url = components[0] + "/files20/" + components[1] + "_" + components[2] + "/" + components[3];
        return url.replace(" ", "");
    }
}
