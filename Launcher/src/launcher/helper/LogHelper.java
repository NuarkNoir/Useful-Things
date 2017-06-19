package launcher.helper;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import launcher.Launcher;
import launcher.LauncherAPI;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;
import org.fusesource.jansi.Ansi.Attribute;
import org.fusesource.jansi.Ansi.Color;

public final class LogHelper {
   @LauncherAPI
   public static final String DEBUG_PROPERTY = "launcher.debug";
   @LauncherAPI
   public static final String NO_JANSI_PROPERTY = "launcher.noJAnsi";
   @LauncherAPI
   public static final boolean JANSI;
   private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss", Locale.US);
   private static final AtomicBoolean DEBUG_ENABLED = new AtomicBoolean(Boolean.getBoolean("launcher.debug"));
   private static final Set OUTPUTS = Collections.newSetFromMap(new ConcurrentHashMap(2));
   private static final LogHelper.Output STD_OUTPUT;

   @LauncherAPI
   public static void addOutput(LogHelper.Output output) {
      OUTPUTS.add(Objects.requireNonNull(output, "output"));
   }

   @LauncherAPI
   public static void addOutput(Path file) throws IOException {
      if (JANSI) {
         //addOutput(new LogHelper.JAnsiOutput(IOHelper.newOutput(file, true)));
         addOutput(IOHelper.newWriter(file, true));
      } else {
         addOutput(IOHelper.newWriter(file, true));
      }

   }

   @LauncherAPI
   public static void addOutput(Writer writer) throws IOException {
      addOutput(new LogHelper.WriterOutput(writer));
   }

   @LauncherAPI
   public static void debug(String message) {
      if (isDebugEnabled()) {
         log(LogHelper.Level.DEBUG, message, false);
      }

   }

   @LauncherAPI
   public static void debug(String format, Object... args) {
      debug(String.format(format, args));
   }

   @LauncherAPI
   public static void error(Throwable exc) {
      error(isDebugEnabled() ? toString(exc) : exc.toString());
   }

   @LauncherAPI
   public static void error(String message) {
      log(LogHelper.Level.ERROR, message, false);
   }

   @LauncherAPI
   public static void error(String format, Object... args) {
      error(String.format(format, args));
   }

   @LauncherAPI
   public static void info(String message) {
      log(LogHelper.Level.INFO, message, false);
   }

   @LauncherAPI
   public static void info(String format, Object... args) {
      info(String.format(format, args));
   }

   @LauncherAPI
   public static boolean isDebugEnabled() {
      return DEBUG_ENABLED.get();
   }

   @LauncherAPI
   public static void log(LogHelper.Level level, String message, boolean sub) {
      String dateTime = DATE_TIME_FORMATTER.format(LocalDateTime.now());
      println(JANSI ? ansiFormatLog(level, dateTime, message, sub) : formatLog(level, message, dateTime, sub));
   }

   @LauncherAPI
   public static void printVersion(String product) {
      println(JANSI ? ansiFormatVersion(product) : formatVersion(product));
   }

   @LauncherAPI
   public static synchronized void println(String message) {
      for(Object output : OUTPUTS) {
         ((LogHelper.Output)output).println(message);
      }

   }

   @LauncherAPI
   public static boolean removeOutput(LogHelper.Output output) {
      return OUTPUTS.remove(output);
   }

   @LauncherAPI
   public static boolean removeStdOutput() {
      return removeOutput(STD_OUTPUT);
   }

   @LauncherAPI
   public static void setDebugEnabled(boolean debugEnabled) {
      DEBUG_ENABLED.set(debugEnabled);
   }

   @LauncherAPI
   public static void subDebug(String message) {
      if (isDebugEnabled()) {
         log(LogHelper.Level.DEBUG, message, true);
      }

   }

   @LauncherAPI
   public static void subDebug(String format, Object... args) {
      subDebug(String.format(format, args));
   }

   @LauncherAPI
   public static void subInfo(String message) {
      log(LogHelper.Level.INFO, message, true);
   }

   @LauncherAPI
   public static void subInfo(String format, Object... args) {
      subInfo(String.format(format, args));
   }

   @LauncherAPI
   public static void subWarning(String message) {
      log(LogHelper.Level.WARNING, message, true);
   }

   @LauncherAPI
   public static void subWarning(String format, Object... args) {
      subWarning(String.format(format, args));
   }

   @LauncherAPI
   public static String toString(Throwable exc) {
      try {
         StringWriter sw = new StringWriter();
         Throwable var2 = null;

         String var34;
         try {
            PrintWriter pw = new PrintWriter(sw);
            Throwable var4 = null;

            try {
               exc.printStackTrace(pw);
            } catch (Throwable var29) {
               var4 = var29;
               throw var29;
            } finally {
               if (pw != null) {
                  if (var4 != null) {
                     try {
                        pw.close();
                     } catch (Throwable var28) {
                        var4.addSuppressed(var28);
                     }
                  } else {
                     pw.close();
                  }
               }

            }

            var34 = sw.toString();
         } catch (Throwable var31) {
            var2 = var31;
            throw var31;
         } finally {
            if (sw != null) {
               if (var2 != null) {
                  try {
                     sw.close();
                  } catch (Throwable var27) {
                     var2.addSuppressed(var27);
                  }
               } else {
                  sw.close();
               }
            }

         }

         return var34;
      } catch (IOException var33) {
         throw new InternalError(var33);
      }
   }

   @LauncherAPI
   public static void warning(String message) {
      log(LogHelper.Level.WARNING, message, false);
   }

   @LauncherAPI
   public static void warning(String format, Object... args) {
      warning(String.format(format, args));
   }

   private static String ansiFormatLog(LogHelper.Level level, String dateTime, String message, boolean sub) {
      boolean bright = level != LogHelper.Level.DEBUG;
      Color levelColor;
      switch(level) {
      case WARNING:
         levelColor = Color.YELLOW;
         break;
      case ERROR:
         levelColor = Color.RED;
         break;
      default:
         levelColor = Color.WHITE;
      }

      Ansi ansi = new Ansi();
      ansi.fg(Color.WHITE).a(dateTime);
      ansi.fgBright(Color.WHITE).a(" [").bold();
      if (bright) {
         ansi.fgBright(levelColor);
      } else {
         ansi.fg(levelColor);
      }

      ansi.a(level).boldOff().fgBright(Color.WHITE).a("] ");
      if (bright) {
         ansi.fgBright(levelColor);
      } else {
         ansi.fg(levelColor);
      }

      if (sub) {
         ansi.a(' ').a(Attribute.ITALIC);
      }

      ansi.a(message);
      return ansi.reset().toString();
   }

   private static String ansiFormatVersion(String product) {
      return (new Ansi()).bold().fgBright(Color.MAGENTA).a("sashok724's ").fgBright(Color.CYAN).a(product).fgBright(Color.WHITE).a(" v").fgBright(Color.BLUE).a("15.2.1").fgBright(Color.WHITE).a(" (build #").fgBright(Color.RED).a(Launcher.BUILD).fgBright(Color.WHITE).a(')').reset().toString();
   }

   private static String formatLog(LogHelper.Level level, String message, String dateTime, boolean sub) {
      if (sub) {
         message = ' ' + message;
      }

      return dateTime + " [" + level.name + "] " + message;
   }

   private static String formatVersion(String product) {
      return String.format("sashok724's %s v%s (build #%s)", product, "15.2.1", Launcher.BUILD);
   }

   static {
      boolean jansi;
      try {
         if (Boolean.getBoolean("launcher.noJAnsi")) {
            jansi = false;
         } else {
            Class.forName("org.fusesource.jansi.Ansi");
            AnsiConsole.systemInstall();
            jansi = true;
         }
      } catch (ClassNotFoundException var4) {
         jansi = false;
      }

      JANSI = jansi;
      PrintStream var10000 = System.out;
      System.out.getClass();
      STD_OUTPUT = var10000::println;
      addOutput(STD_OUTPUT);
      String logFile = System.getProperty("launcher.logFile");
      if (logFile != null) {
         try {
            addOutput(IOHelper.toPath(logFile));
         } catch (IOException var3) {
            error(var3);
         }
      }

   }

   /*private static final class JAnsiOutput extends LogHelper.WriterOutput {
      private JAnsiOutput(OutputStream output) {
      }
   }*/

   @LauncherAPI
   public static enum Level {
      DEBUG("DEBUG"),
      INFO("INFO"),
      WARNING("WARN"),
      ERROR("ERROR");

      public final String name;

      private Level(String name) {
         this.name = name;
      }

      public String toString() {
         return this.name;
      }
   }

   @FunctionalInterface
   @LauncherAPI
   public interface Output {
      void println(String var1);
   }

   private static class WriterOutput implements LogHelper.Output, AutoCloseable {
      private final Writer writer;

      private WriterOutput(Writer writer) throws IOException {
         this.writer = writer;
      }

      public void close() throws IOException {
         this.writer.close();
      }

      public void println(String message) {
         try {
            this.writer.write(message + System.lineSeparator());
            this.writer.flush();
         } catch (IOException var3) {
            ;
         }

      }
   }
}
