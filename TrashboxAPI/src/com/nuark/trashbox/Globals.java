package com.nuark.trashbox;

import java.util.Map;
import org.jsoup.Jsoup;

public class Globals {
    public static class Statics {
        static String mainUrl = "https://trashbox.ru/";
        static String progsUrl = mainUrl + "public/progs/tags/os_android/";
        static String gamesUrl = mainUrl + "public/games/tags/os_android/";
        static String ajaxUrl = mainUrl + "ajax.php";
        static String userURL = mainUrl + "users/";
        static String newsURL = mainUrl + "public/b_news/";
        static String reviewsURL = mainUrl + "public/reviews/";
        static String textsURL = mainUrl + "public/b_text/";
        static String currentUrl = progsUrl;
        public static String separator = "page_topics/";
        static CurrentUser cu = null;

        public static String getMainUrl() {
            return mainUrl;
        }

        public static String getProgsUrl() {
            return progsUrl;
        }

        public static String getGamesUrl() {
            return gamesUrl;
        }

        public static String getAjaxUrl() {
            return ajaxUrl;
        }

        public static String getUserURL() {
            return userURL;
        }

        public static String getNewsURL() {
            return newsURL;
        }

        public static String getReviewsURL() {
            return reviewsURL;
        }

        public static String getTextsURL() {
            return textsURL;
        }

        public static CurrentUser getCu() {
            return cu;
        }

        public static void setCu(CurrentUser cu) {
            Statics.cu = cu;
        }

        public static String getCurrentUrl() {
            return currentUrl;
        }

        public static void setCurrentUrl(Section s) {
            switch (s){
                case Games:
                    currentUrl = gamesUrl;
                    break;
                case Programs:
                    currentUrl =  progsUrl;
                    break;
            }
        }

        public enum Section{
            Programs,
            Games
        }
    }
    
    public static class CurrentUser {
        private String login, password, ownUrl;
        private Map<String, String> cookies;

        public CurrentUser(String login, String password, String ownUrl, Map<String, String> cookies) {
            this.login = login;
            this.password = password;
            this.ownUrl = ownUrl;
            this.cookies = cookies;
        }
        
        public String getLogin() {
            return login;
        }

        public String getPassword() {
            return password;
        }

        public String getOwnUrl() {
            return ownUrl;
        }

        public Map<String, String> getCookies() {
            return cookies;
        }
    }
}
