/*
 * Создавая сборки для майна часто начинаешь сходить с ума.
 * А так как я вконец ёбнутый, то я стараюсь всё автоматизировать.
 * Короче, эта программа скачивает все моды с сайта TLauncher'а
 * На вход принимает либо файл со ссылками, либо ссылку на раздел с модами.
 * При втором варианте он генерирует файл со ссылками и предлагает скачать всё, что есть в файле.
 * По идее, оно может скачивать любые файлы, ссылки на которые указаны в файле links.txt
 * Ах да, для работы требуется библиотека JSoup 1.10.2
 * Её надо положить в папку ./lib
 * P.S. Рядом с *.jar файлом надо создать папку ./downloads
 */
package com.nuark.tlaunchermodsgrabber;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Scanner;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * @author Nuark 
 * @version 1.0
 * @since 08.06.17 16:20
 */
public class TLauncherModsGrabber {

    static ArrayList<String> links = new ArrayList<>();
    static int i = 0, bytes = 0, errorcount = 0;
    static Scanner in;
    static boolean decodeNeeded = false;
    static FileWriter fw, logger;
    
    public static void main(String[] args) {
        in = new Scanner(System.in);
        System.out.println("Ссылки сграблены?(1/0)");
        switch (in.nextLine()) {
            case "1":
                linker();
                break;
            case "0":
                System.out.println("Начинаем грабить ссылки!");
                lister();
                System.out.println("Обработано!");
                System.out.println("Скачать всё?(1/0)");
                if ("1".equals(in.nextLine())) loader();
                break;
            default:
                System.out.println("Unexcepected behaviour!");
                break;
        }
    }
    
    public static void linker(){
        try {
            FileInputStream fstream = new FileInputStream("./links.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
            String strLine;
            while ((strLine = br.readLine()) != null){
                links.add(strLine);
            }
        } catch (IOException e){
            System.out.println("Проблема с файлом links.txt!");
        }
    }
    
    public static void lister(){
        try {
            fw = new FileWriter(new File("./links.txt"), true);
            ArrayList<String> tmp = new ArrayList<>();
            String baseUrl = "https://tlauncher.org/ru/";
            System.out.println("Ссылка на раздел: ");
            System.out.println("(https://tlauncher.org/ru/{вот эта часть + /})");
            baseUrl = baseUrl + in.nextLine();
            int lastPage = 0;
            Document d = Jsoup.connect(baseUrl).get();
            lastPage = Integer.decode(d.select("a.navi_page").last().text());
            System.out.println("Получили последнюю страницу: " + lastPage);
            System.out.println("Начинаем первичную обработку!");
            for (int iterator = 1; iterator <= lastPage; iterator++){
                System.out.println("Обрабатываем страницу номер " + iterator);
                d = Jsoup.connect(baseUrl + iterator + "/").followRedirects(true).get();
                d.select("article.b-anons h2 a").forEach((el) -> { tmp.add("https://tlauncher.org/" + el.attr("href")); });
                System.out.println("Обработано!");
            }
            System.out.println("Первичная обработка завершена!");
            System.out.println("Всего модов в данном разделе: " + tmp.size());
            System.out.println("Начинаем вторичную обработку!");
            for (String link : tmp){
                d = Jsoup.connect(link).followRedirects(true).get();
                if (d.select("a.b-button").first() == null) continue;
                links.add(d.select("a.b-button").first().attr("href"));
                System.out.println("Обработан мод \"" + d.select("article.single-content h1").first().text() + "\"");
            }
            System.out.println("Вторичная обработка завершена!");
            System.out.println("Пишем файл...");
            for (String l : links){
                fw.write(l + "\n");
            }
            System.out.println("Всё отработано!");
        } catch (IOException e){
            System.out.println("Ошибка соединения!");
        } catch (NullPointerException e){
            System.out.println("NPE!");
        }
    }

    private static void loader() {
        System.out.println("Требуется ли названиям декодирование?(1/0)");
        if ("1".equals(in.nextLine())) decodeNeeded = true;
        System.out.println("Обработано!");
        System.out.println("Всего файлов: " + links.size());
        links.forEach((link) -> { downloadUsingNIO(link); });
        System.out.println("Всё сделано!");
        System.out.println("Скачано примерно " + bytes/1024/1024 + " мегабайт.");
    }
    
    public static String getFilenameFromURL(String url){
        return url.split("/")[url.split("/").length-1];
    }
    
    public static String decoder(String url){
        return URLEncoder.encode(url).replace("%2F", "/").replace("%3A", ":");
    }
    
    private static int downloadUsingNIO(String urlStr) {
        try {
            URL url = new URL(urlStr);
            String file = "./downloaded/" + getFilenameFromURL(urlStr).replaceAll("[|/\\:?<>\"*]", "");
            if (decodeNeeded) file = decoder(file);
            if (new File(file).exists()) { i++; return 0;}
            try (ReadableByteChannel rbc = Channels.newChannel(url.openStream()); FileOutputStream fos = new FileOutputStream(file)) {
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                bytes += fos.getChannel().size();
            }
        } catch (IOException e){
            errorcount++;
        }
        return 1;
    }
}
