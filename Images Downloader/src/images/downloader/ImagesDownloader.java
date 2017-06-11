package images.downloader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;

public class ImagesDownloader {
    
    static ArrayList<String> links = new ArrayList<>(), jpegs = new ArrayList<>(), pngs = new ArrayList<>(), gifs = new ArrayList<>();
    static int i = 0, bytes = 0, errorcount = 0;

    public static void main(String[] args) throws IOException {
        try {
            FileInputStream fstream = new FileInputStream("./links.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
            String strLine;
            while ((strLine = br.readLine()) != null){
                links.add(strLine);
            }
        } catch (IOException e){
            System.out.println("Проблема с файлом links.txt");
        }
        for (String link : links){
            if (link.contains(".gif"))
                gifs.add(link);
            else if (link.contains(".png"))
                pngs.add(link);
            else if (link.contains(".jpeg") || link.contains(".jpg"))
                jpegs.add(link);
        }
        
        System.out.println("So, we have a lot of images(" + links.size() + ").");
        System.out.println("Gifs:\n\t" + gifs.size());
        System.out.println("Pngs:\n\t" + pngs.size());
        System.out.println("Jpegs:\n\t" + jpegs.size());
        System.out.println();
        
        System.out.println("Starting downloading pngs...");
        for (String link : pngs){
            downloadUsingNIO(decoder(link), link);
        }
        System.out.println("All pngs are downloaded!");
        
        System.out.println("Starting downloading jpegs...");
        for (String link : jpegs){
            downloadUsingNIO(decoder(link), link);
        }
        System.out.println("All jpegs are downloaded!");
        
        System.out.println("Starting downloading gifs...");
        for (String link : gifs){
            downloadUsingNIO(decoder(link), link);
        }
        System.out.println("All gifs are downloaded!");
        System.out.println();
        System.out.println("Everything is downloaded!");
        System.out.println("Errors happend: " + errorcount);
        System.out.println("MegaBytes got: " + bytes/1024/1024);
        System.out.println(i);
        
    }
    
    public static String getFilenameFromURL(String url){
        return url.split("/")[url.split("/").length-1];
    }
    
    public static String decoder(String url){
        return URLEncoder.encode(url).replace("%2F", "/").replace("%3A", ":");
    }
    
    private static int downloadUsingNIO(String urlStr, String file) {
        try {
            urlStr = "http://dev.nuarknoir.h1n.ru/api/reactor/imgview.php?l=" + urlStr;
            URL url = new URL(urlStr);
            file = "./images/" + getFilenameFromURL(urlStr).replaceAll("[|/\\:?<>\"*]", "");
            if (new File(file).exists()) { i++; return 0;}
            ReadableByteChannel rbc = Channels.newChannel(url.openStream());
            FileOutputStream fos = new FileOutputStream(file);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            bytes += fos.getChannel().size();
            fos.close();
            rbc.close();
        } catch (IOException e){
            errorcount++;
        }
        return 1;
    }
}
