package xyz.nuark.trashbox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import xyz.nuark.trashbox.api.AppsLoader;
import xyz.nuark.trashbox.models.App;
import xyz.nuark.trashbox.models.AppArticle;
import xyz.nuark.trashbox.utils.Utils;

public class Test {
    public static void main(String[] args) throws IOException, Exception {
        AppsPage ap = new AppsPage();
        App app = ap.getNextAppsPage().get(0);
        AppArticlePage aap = new AppArticlePage(app.getTopicLink(), app.getImageLink());
        AppArticle aa = aap.getArticle();
        Utils u = new Utils();
        String link = aa.getDownloadLinks().entrySet().iterator().next().getValue();
        String downloadLink = u.GenerateDownloadLink(link);
        System.out.println(downloadLink);
        
        ap = new AppsPage(Globals.Statics.mainUrl + Globals.Tagger.getTag("Аркады"));
        app = ap.getNextAppsPage().get(0);
        aap = new AppArticlePage(app.getTopicLink(), app.getImageLink());
        aa = aap.getArticle();
        u = new Utils();
        link = aa.getDownloadLinks().entrySet().iterator().next().getValue();
        downloadLink = u.GenerateDownloadLink(link);
        System.out.println(downloadLink);
        
        ap = new AppsPage(Globals.Statics.mainUrl + Globals.Tagger.getTag("VoIP"));
        app = ap.getNextAppsPage().get(0);
        aap = new AppArticlePage(app.getTopicLink(), app.getImageLink());
        aa = aap.getArticle();
        u = new Utils();
        link = aa.getDownloadLinks().entrySet().iterator().next().getValue();
        downloadLink = u.GenerateDownloadLink(link);
        System.out.println(downloadLink);
    }
}
