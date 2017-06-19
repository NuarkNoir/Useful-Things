package launcher.helper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;

public final class jWMIHelper {
   private static final String CRLF = "\r\n";
   public static Map mapOfClass = new LinkedHashMap();

   private static String getVBScript() {
      String vbs = "Dim oWMI : Set oWMI = GetObject(\"winmgmts:\")\r\n";
      vbs = vbs + "Dim classComponent : Set classComponent = oWMI.ExecQuery(\"SELECT Manufacturer, Product, SerialNumber FROM Win32_BaseBoard\")\r\n";
      vbs = vbs + "Dim classComponent1 : Set classComponent1 = oWMI.ExecQuery(\"SELECT SerialNumber FROM Win32_BIOS\")\r\n";
      vbs = vbs + "Dim classComponent2 : Set classComponent2 = oWMI.ExecQuery(\"SELECT ProcessorId FROM Win32_Processor\")\r\n";
      vbs = vbs + "Dim classComponent3 : Set classComponent3 = oWMI.ExecQuery(\"SELECT VolumeSerialNumber FROM Win32_LogicalDisk Where DeviceID = 'C:'\")\r\n";
      vbs = vbs + "Dim obj, strData\r\n";
      vbs = vbs + "For Each obj in classComponent\r\n";
      vbs = vbs + "strData = strData & obj.Manufacturer & VBCrLf\r\n";
      vbs = vbs + "strData = strData & \"|\"\r\n";
      vbs = vbs + "strData = strData & obj. Product & VBCrLf\r\n";
      vbs = vbs + "strData = strData & \"|\"\r\n";
      vbs = vbs + "strData = strData & obj. SerialNumber & VBCrLf\r\n";
      vbs = vbs + "Next\r\n";
      vbs = vbs + "strData = strData & \"|\"\r\n";
      vbs = vbs + "For Each obj in classComponent1\r\n";
      vbs = vbs + "strData = strData & obj.SerialNumber & VBCrLf\r\n";
      vbs = vbs + "Next\r\n";
      vbs = vbs + "strData = strData & \"|\"\r\n";
      vbs = vbs + "For Each obj in classComponent2\r\n";
      vbs = vbs + "strData = strData & obj.ProcessorId & VBCrLf\r\n";
      vbs = vbs + "Next\r\n";
      vbs = vbs + "strData = strData & \"|\"\r\n";
      vbs = vbs + "For Each obj in classComponent3\r\n";
      vbs = vbs + "strData = strData & obj.VolumeSerialNumber & VBCrLf\r\n";
      vbs = vbs + "Next\r\n";
      vbs = vbs + "wscript.echo strData\r\n";
      return vbs;
   }

   private static void writeStrToFile(String filename, String data) throws Exception {
      if (data != "") {
         FileWriter output = new FileWriter(filename);
         output.write(data);
         output.flush();
         output.close();
         output = null;
      }
   }

   public static String getWMIValue() throws Exception {
      String vbScript = getVBScript();
      String tmpFileName = Files.createTempFile("jwmi", ".vbs").toString();
      writeStrToFile(tmpFileName, vbScript);
      String output = execute(new String[]{"cmd.exe", "/C", "cscript.exe", tmpFileName});
      (new File(tmpFileName)).delete();
      return output.trim();
   }

   private static String execute(String[] cmdArray) throws Exception {
      Process process = Runtime.getRuntime().exec(cmdArray);
      BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream(), "IBM866"));
      String output = "";

      String line;
      while((line = input.readLine()) != null) {
         if (!line.contains("Microsoft") && !line.equals("") && !line.contains("Copyright")) {
            output = output + line;
         }
      }

      process.destroy();
      process = null;
      return output.trim();
   }

   public static String getDataWin() {
      String s = "";

      try {
         s = s + getWMIValue() + "|";
      } catch (Exception var2) {
         var2.printStackTrace();
      }

      if (s.length() > 1) {
         s = s.substring(0, s.length() - 1);
         s = s.replaceAll("[\\s]{2,}", " ");
      }

      return s;
   }

   public static String getDataMacOS() {
      ProcessBuilder pb = new ProcessBuilder(new String[]{"bash", "-c", "ioreg -l | awk '/IOPlatformSerialNumber/ { print $4;}'"});
      pb.redirectErrorStream(true);
      String s = "";
      String out = "";

      try {
         Process p = pb.start();

         for(BufferedReader stdout = new BufferedReader(new InputStreamReader(p.getInputStream())); (s = stdout.readLine()) != null; out = out + s) {
            ;
         }

         p.getInputStream().close();
         p.getOutputStream().close();
         p.getErrorStream().close();
      } catch (Exception var5) {
         var5.printStackTrace();
      }

      return out;
   }

   public static String getDatalinux() {
      String out = "";
      String sc = "/sbin/udevadm info --query=property --name=sda";
      String[] scargs = new String[]{"/bin/sh", "-c", sc};

      try {
         Process p = Runtime.getRuntime().exec(scargs);
         p.waitFor();
         BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
         StringBuilder sb = new StringBuilder();

         String line;
         while((line = reader.readLine()) != null) {
            if (line.indexOf("ID_SERIAL_SHORT") != -1) {
               sb.append(line);
            }
         }

         out = out + sb.toString().substring(sb.toString().indexOf("=") + 1);
      } catch (Exception var7) {
         var7.printStackTrace();
      }

      return out;
   }
}
