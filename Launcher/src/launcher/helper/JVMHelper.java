package launcher.helper;

import com.sun.management.OperatingSystemMXBean;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Locale;
import launcher.LauncherAPI;
import sun.misc.URLClassPath;

public final class JVMHelper {
   @LauncherAPI
   public static final JVMHelper.OS OS_TYPE = JVMHelper.OS.byName(System.getProperty("os.name"));
   @LauncherAPI
   public static final String OS_VERSION = System.getProperty("os.version");
   @LauncherAPI
   public static final int OS_BITS = getCorrectOSArch();
   @LauncherAPI
   public static final int JVM_BITS = Integer.parseInt(System.getProperty("sun.arch.data.model"));
   @LauncherAPI
   public static final int RAM = getRAMAmount();
   @LauncherAPI
   public static final Runtime RUNTIME = Runtime.getRuntime();
   @LauncherAPI
   public static final URLClassLoader LOADER = (URLClassLoader)ClassLoader.getSystemClassLoader();
   @LauncherAPI
   public static final URLClassPath UCP = getURLClassPath();
   private static final String JAVA_LIBRARY_PATH = "java.library.path";
   private static final Field USR_PATHS_FIELD = getUsrPathsField();
   private static final Field SYS_PATHS_FIELD = getSysPathsField();

   @LauncherAPI
   public static void addNativePath(Path path) {
      String stringPath = path.toString();
      String libraryPath = System.getProperty("java.library.path");
      if (libraryPath != null && !libraryPath.isEmpty()) {
         libraryPath = libraryPath + File.pathSeparatorChar + stringPath;
      } else {
         libraryPath = stringPath;
      }

      System.setProperty("java.library.path", libraryPath);

      try {
         USR_PATHS_FIELD.set((Object)null, (Object)null);
         SYS_PATHS_FIELD.set((Object)null, (Object)null);
      } catch (IllegalAccessException var4) {
         throw new InternalError(var4);
      }
   }

   @LauncherAPI
   public static void fullGC() {
      RUNTIME.gc();
      RUNTIME.runFinalization();
      LogHelper.debug("Used heap: %d MiB", RUNTIME.totalMemory() - RUNTIME.freeMemory() >> 20);
   }

   @LauncherAPI
   public static void halt0(int status) {
      try {
         getMethod(Class.forName("java.lang.Shutdown"), "halt0", Integer.TYPE).invoke((Object)null, status);
      } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException var2) {
         throw new InternalError(var2);
      }
   }

   @LauncherAPI
   public static boolean isJVMMatchesSystemArch() {
      return JVM_BITS == OS_BITS;
   }

   @LauncherAPI
   public static void verifySystemProperties(Class mainClass) {
      Locale.setDefault(Locale.US);
      LogHelper.debug("Verifying class loader");
      if (!mainClass.getClassLoader().equals(LOADER)) {
         throw new SecurityException("ClassLoader should be system");
      } else {
         LogHelper.debug("Verifying JVM architecture");
         if (!isJVMMatchesSystemArch()) {
            LogHelper.warning("Java and OS architecture mismatch");
            LogHelper.warning("It's recommended to download %d-bit JRE", OS_BITS);
         }

         LogHelper.debug("Disabling SNI extensions (SSL fix)");
         System.setProperty("jsse.enableSNIExtension", Boolean.toString(false));
      }
   }

   private static int getCorrectOSArch() {
      if (OS_TYPE == JVMHelper.OS.MUSTDIE) {
         return System.getenv("ProgramFiles(x86)") == null ? 32 : 64;
      } else {
         return System.getProperty("os.arch").contains("64") ? 64 : 32;
      }
   }

   private static Field getField(Class clazz, String name) throws NoSuchFieldException {
      Field field = clazz.getDeclaredField(name);
      field.setAccessible(true);
      return field;
   }

   private static Method getMethod(Class clazz, String name, Class... params) throws NoSuchMethodException {
      Method method = clazz.getDeclaredMethod(name, params);
      method.setAccessible(true);
      return method;
   }

   private static int getRAMAmount() {
      int physicalRam = (int)(((OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean()).getTotalPhysicalMemorySize() >> 20);
      return Math.min(physicalRam, OS_BITS == 32 ? 1536 : 4096);
   }

   private static Field getSysPathsField() {
      try {
         return getField(ClassLoader.class, "sys_paths");
      } catch (NoSuchFieldException var1) {
         throw new InternalError(var1);
      }
   }

   private static URLClassPath getURLClassPath() {
      try {
         return (URLClassPath)getField(URLClassLoader.class, "ucp").get(LOADER);
      } catch (IllegalAccessException | NoSuchFieldException var1) {
         throw new InternalError(var1);
      }
   }

   private static Field getUsrPathsField() {
      try {
         return getField(ClassLoader.class, "usr_paths");
      } catch (NoSuchFieldException var1) {
         throw new InternalError(var1);
      }
   }

   @LauncherAPI
   public static enum OS {
      MUSTDIE("mustdie"),
      LINUX("linux"),
      MACOSX("macosx");

      public final String name;

      private OS(String name) {
         this.name = name;
      }

      public static JVMHelper.OS byName(String name) {
         if (name.startsWith("Windows")) {
            return MUSTDIE;
         } else if (name.startsWith("Linux")) {
            return LINUX;
         } else if (name.startsWith("Mac OS X")) {
            return MACOSX;
         } else {
            throw new RuntimeException(String.format("This shit is not yet supported: '%s'", name));
         }
      }
   }
}
