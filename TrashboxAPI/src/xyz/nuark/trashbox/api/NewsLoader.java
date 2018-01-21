package xyz.nuark.trashbox.api;

import xyz.nuark.trashbox.Globals.Statics;
import xyz.nuark.trashbox.models.News;
import xyz.nuark.trashbox.utils.Enumerators.Sort;
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
        // TODO
        return news;
    }
}
