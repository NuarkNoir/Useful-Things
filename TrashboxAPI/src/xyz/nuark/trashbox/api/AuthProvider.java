package xyz.nuark.trashbox.api;

import xyz.nuark.trashbox.exceptions.AuthException;
import xyz.nuark.trashbox.Globals.Statics;
import java.io.IOException;
import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;

public class AuthProvider {
    private String login, password;

    public AuthProvider(String login, String password) {
        this.login = login;
        this.password = password;
    }
    
    public void Auth() throws IOException, AuthException{
        Connection connect = Jsoup.connect(Statics.getMainUrl() + "/ajax.php").ignoreContentType(true).method(Method.POST)
                .data("action", "auth").data("remember", "1").data("t_control", "1")
                .data("login", login).data("pass", password);
        Response response = connect.execute();
        String respcontent = response.body();
        /*
        if (respcontent.contains("window.location")) Statics.setCu(new UserManager(login, password, Statics.getUserURL() + login, response.cookies()));
        else throw new AuthException(respcontent.replace("$('div_auth_error').innerHTML = '", "").replace("<br><BR>';", ""));
        */
    }
}
