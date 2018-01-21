package xyz.nuark.trashbox.utils;

import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class Utils {

    public Utils() {
    }
    
    public String GenerateDownloadLink(String url) throws IOException {
        Document d = Jsoup.connect(url).get();
        for (Element script : d.select("script")){
            String st = script.toString().replaceAll("<(.*?)>", "");
            if (st.contains("show_landing_link2") && !st.contains("function")){
                String[] parts = st.replace("show_landing_link2(", "").replace(");", "").replace("'", "").split(", ");
                return parts[0] + "/files20/" + parts[1] + "_" + parts[2] + "/" + parts[3];
            }
        }
        return "";
    }
    
    public String GetLatestPage(String url) throws IOException {
        return Jsoup.connect(url).get().select("div.div_navigator_new span.span_item_active").first().text();
    }
}
