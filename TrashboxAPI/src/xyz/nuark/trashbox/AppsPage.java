package xyz.nuark.trashbox;

import java.io.IOException;
import java.util.ArrayList;
import org.jsoup.Jsoup;
import xyz.nuark.trashbox.api.AppsLoader;
import xyz.nuark.trashbox.models.App;
import xyz.nuark.trashbox.utils.Enumerators.Sort;
import xyz.nuark.trashbox.utils.Enumerators.Type;
import xyz.nuark.trashbox.utils.Utils;

public class AppsPage {
    public ArrayList<App> appslist;
    private String url, sort;
    private int currentPage = 0;

    public AppsPage() {
        this(Type.Apps, Sort.Recomendation);
    }

    public AppsPage(Sort sort) {
        this(Type.Apps, sort);
    }

    public AppsPage(Type type) {
        this(type, Sort.Recomendation);
    }

    public AppsPage(Type type, Sort sort) {
        this.url = formatUrl(type, sort);
        this.currentPage = getLastPage();
    }

    public AppsPage(String url) {
        this(url, Sort.Recomendation);
    }

    public AppsPage(String url, Sort sort) {
        this.url = url;
        switch (sort) {
            case Recomendation:
                this.sort = "";
                break;
            case Date:
                this.sort = "?sort=edited";
                break;
            case Rating:
                this.sort = "?sort=votes";
                break;
        }
        this.currentPage = getLastPage();
    }
    
    private String formatUrl(Type type, Sort sort){
        String _url = Globals.Statics.getMainUrl();
        switch (type){
            case Apps:
                _url += Globals.Statics.getProgsUrl();
                break;
            case Games:
                _url += Globals.Statics.getGamesUrl();
                break;
        }
        switch (sort) {
            case Recomendation:
                this.sort = "";
                break;
            case Date:
                this.sort = "?sort=edited";
                break;
            case Rating:
                this.sort = "?sort=votes";
                break;
        }
        return _url;
    }
    
    private int getLastPage(){
        try {
            String pager = Jsoup.connect(url).get().select("div.div_navigator_new span.span_item_active").first().text();
            return Integer.parseInt(pager);
        } catch (Exception ex){
            System.err.println(ex);
            ex.printStackTrace();
            return 0;
        }
    }
    
    public ArrayList<App> getAppsPage(int pagenum) throws IOException, Exception{
        this.appslist = new ArrayList();
        String _url = this.url + String.valueOf(pagenum);
        AppsLoader loader = new AppsLoader(_url);
        this.appslist = loader.Load();
        this.currentPage = pagenum;
        return this.appslist;
    }
    
    public ArrayList<App> getNextAppsPage(){
        this.appslist = new ArrayList();
        if (currentPage <= 0) 
            currentPage = 1;
        else 
            currentPage--;
        String _url = this.url + String.valueOf(currentPage);
        AppsLoader loader = new AppsLoader(_url);
        try {
            this.appslist = this.getAppsPage(currentPage);
        } catch (IOException ex){
            System.err.println("IOEx:: Проблемы с интернетом");
            System.err.println(ex);
            currentPage++;
        } catch (Exception ex){
            System.err.println("Ex:: Неаозможная ошибка, лол");
            System.err.println(ex);
            currentPage++;
        }
        return this.appslist;
    }
    
    public ArrayList<App> getPreviousAppsPage(){
        this.appslist = new ArrayList();
        currentPage++;
        String _url = this.url + String.valueOf(currentPage) + this.sort;
        AppsLoader loader = new AppsLoader(_url);
        try {
            this.appslist = this.getAppsPage(currentPage);
        } catch (IOException ex){
            System.err.println("IOEx:: Проблемы с интернетом");
            System.err.println(ex);
            currentPage--;
        } catch (Exception ex){
            System.err.println("Ex:: Неаозможная ошибка, лол");
            System.err.println(ex);
            currentPage--;
        }
        return this.appslist;
    }

    public int getCurrentPage() {
        return currentPage;
    }
}
