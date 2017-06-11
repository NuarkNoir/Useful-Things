package com.nuark.pornreactor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Scanner;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class main {
    public static void main(String[] args) throws IOException {
        FileWriter fw = new FileWriter(new File("./links[" + new Date().getTime() + "].txt"), true);
        try {
            Scanner in = new Scanner(System.in);
            System.out.print("Введите ссылку тэга(без pornreactor.cc): ");
            String url = "http://pornreactor.cc" + in.nextLine();
            Document d = Jsoup.connect(url).get();
            String lastpage = d.select(".pagination_expanded span.current").text();
            url = url + "/";
            System.out.println("Всего страниц данного тэга: " + lastpage);
            System.out.println("Начинаем отработку...");
            int max = Integer.parseInt(lastpage);
            int got_bytes = 0;
            int got_links = 0;
            for (int i = 1; i < max; i++) {
                System.out.println("Выполнено " + i + " из " + max + " операций(" + Math.round((100*i/max)) + "%)");
                d = Jsoup.connect(url + i).timeout(10000).get();
                Elements el = d.select("img[height]");
                for (Element e : el){
                    fw.write(e.attr("src") + "\n");
                    got_links++;
                }
                got_bytes += d.html().length();
            }
            fw.close();
            System.out.println();
            System.out.println();
            System.out.println();
            System.out.println("Всё сделано!");
            System.out.println("Было получено около " + got_bytes/1024/1024 + " мегабайт.");
            System.out.println("Сграблено " + got_links + " ссылок.");
        } catch (Exception ex){
            fw = new FileWriter(new File("crashlog.log"), true);
            fw.write(ex.fillInStackTrace().toString() + "\n\n\n\n");
            fw.close();
            System.out.println("Error happend!");
            System.out.println(ex.fillInStackTrace());
        }
    } 
}
