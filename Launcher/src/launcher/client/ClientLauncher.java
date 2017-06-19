package launcher.client;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.WriterConfig;
import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.regex.Pattern;
import launcher.Launcher;
import launcher.LauncherAPI;
import launcher.hasher.DirWatcher;
import launcher.hasher.FileNameMatcher;
import launcher.hasher.HashedDir;
import launcher.helper.CommonHelper;
import launcher.helper.IOHelper;
import launcher.helper.JVMHelper;
import launcher.helper.LogHelper;
import launcher.helper.SecurityHelper;
import launcher.helper.VerifyHelper;
import launcher.request.update.LauncherRequest;
import launcher.serialize.HInput;
import launcher.serialize.HOutput;
import launcher.serialize.signed.SignedObjectHolder;
import launcher.serialize.stream.StreamObject;

public final class ClientLauncher {
   private static final String MAGICAL_INTEL_OPTION = "-XX:HeapDumpPath=ThisTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump";
   private static final Set BIN_POSIX_PERMISSIONS = Collections.unmodifiableSet(EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_EXECUTE, PosixFilePermission.GROUP_READ, PosixFilePermission.GROUP_EXECUTE, PosixFilePermission.OTHERS_READ, PosixFilePermission.OTHERS_EXECUTE));
   private static final Path NATIVES_DIR = IOHelper.toPath("natives");
   private static final Path MODS_DIR = IOHelper.toPath("mods");
   private static final String SHADERS_FILE_ENABLE = "shaders.jar";
   private static final String SHADERS_FILE_DISABLE = "shaders.jar.disable";
   private static final String OPIS_FILE_ENABLE = "Opis.jar";
   private static final String OPIS_FILE_DISABLE = "Opis.jar.disable";
   private static final String MOBIUS_FILE_ENABLE = "MobiusCore.jar";
   private static final String MOBIUS_FILE_DISABLE = "MobiusCore.jar.disable";
   private static final Path RESOURCEPACKS_DIR = IOHelper.toPath("resourcepacks");
   private static final Pattern UUID_PATTERN = Pattern.compile("-", 16);
   @LauncherAPI
   public static final String SKIN_URL_PROPERTY = "skinURL";
   @LauncherAPI
   public static final String SKIN_DIGEST_PROPERTY = "skinDigest";
   @LauncherAPI
   public static final String CLOAK_URL_PROPERTY = "cloakURL";
   @LauncherAPI
   public static final String CLOAK_DIGEST_PROPERTY = "cloakDigest";
   private static final AtomicBoolean LAUNCHED = new AtomicBoolean(false);

   @LauncherAPI
   public static boolean isLaunched() {
      return LAUNCHED.get();
   }

   public static String jvmProperty(String name, String value) {
      return String.format("-D%s=%s", name, value);
   }

   @LauncherAPI
   public static Process launch(Path jvmDir, SignedObjectHolder jvmHDir, SignedObjectHolder assetHDir, SignedObjectHolder clientHDir, SignedObjectHolder profile, ClientLauncher.Params params, boolean pipeOutput) throws Throwable {
      LogHelper.debug("Writing ClientLauncher params file");
      Path paramsFile = Files.createTempFile("ClientLauncherParams", ".bin");
      HOutput output = new HOutput(IOHelper.newOutput(paramsFile));
      Object args = null;

      try {
         params.write(output);
         profile.write(output);
         jvmHDir.write(output);
         assetHDir.write(output);
         clientHDir.write(output);
      } catch (Throwable var22) {
         args = var22;
         throw var22;
      } finally {
         if (output != null) {
            if (args != null) {
               try {
                  output.close();
               } catch (Throwable var21) {
                  ((Throwable)args).addSuppressed(var21);
               }
            } else {
               output.close();
            }
         }

      }

      LogHelper.debug("Resolving JVM binary");
      Path javaBin = IOHelper.resolveJavaBin(jvmDir);
      if (IOHelper.POSIX) {
         Files.setPosixFilePermissions(javaBin, BIN_POSIX_PERMISSIONS);
      }

      args = new LinkedList();
      ((LinkedList)args).add(javaBin.toString());
      ((LinkedList)args).add("-XX:HeapDumpPath=ThisTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump");
      if (params.ram > 0 && params.ram <= JVMHelper.RAM) {
         ((LinkedList)args).add("-Xmx" + params.ram + 'M');
      }

      ((LinkedList)args).add(jvmProperty("launcher.debug", Boolean.toString(LogHelper.isDebugEnabled())));
      if (JVMHelper.OS_TYPE == JVMHelper.OS.MUSTDIE && JVMHelper.OS_VERSION.startsWith("10.")) {
         LogHelper.debug("MustDie 10 fix is applied");
         ((LinkedList)args).add(jvmProperty("os.name", "Windows 10"));
         ((LinkedList)args).add(jvmProperty("os.version", "10.0"));
      }

      if (params.shaders) {
         LogHelper.info("shaders true");
      } else {
         LogHelper.info("shaders false");
      }

      if (params.opis) {
         LogHelper.info("opis true");
      } else {
         LogHelper.info("opis false");
      }

      File shadersfileenable = new File(params.clientDir.resolve(MODS_DIR).resolve("shaders.jar").toString());
      File shadersfiledisable = new File(params.clientDir.resolve(MODS_DIR).resolve("shaders.jar.disable").toString());
      if (params.shaders) {
         if (shadersfileenable.exists()) {
            Files.delete(params.clientDir.resolve(MODS_DIR).resolve("shaders.jar"));
         }

         Files.copy(shadersfiledisable.toPath(), shadersfileenable.toPath());
         LogHelper.info("shaders rename to enable ");
      }

      if (!params.shaders) {
         LogHelper.info("shaders rename to disable");
      }

      File opisfileenable = new File(params.clientDir.resolve(MODS_DIR).resolve("Opis.jar").toString());
      File opisfiledisable = new File(params.clientDir.resolve(MODS_DIR).resolve("Opis.jar.disable").toString());
      File mobiusfileenable = new File(params.clientDir.resolve(MODS_DIR).resolve("MobiusCore.jar").toString());
      File mobiusfiledisable = new File(params.clientDir.resolve(MODS_DIR).resolve("MobiusCore.jar.disable").toString());
      if (params.opis) {
         if (opisfileenable.exists()) {
            Files.delete(params.clientDir.resolve(MODS_DIR).resolve("Opis.jar"));
            Files.delete(params.clientDir.resolve(MODS_DIR).resolve("MobiusCore.jar"));
         }

         Files.copy(opisfiledisable.toPath(), opisfileenable.toPath());
         Files.copy(mobiusfiledisable.toPath(), mobiusfileenable.toPath());
         LogHelper.info("opis rename to enable ");
      }

      if (!params.opis) {
         LogHelper.info("opis rename to disable");
      }

      Collections.addAll(((LinkedList)args), ((ClientProfile)profile.object).getJvmArgs());
      Collections.addAll(((LinkedList)args), new String[]{"-classpath", IOHelper.getCodeSource(ClientLauncher.class).toString(), ClientLauncher.class.getName()});
      ((LinkedList)args).add(paramsFile.toString());
      LogHelper.debug("Launching client instance");
      ProcessBuilder builder = new ProcessBuilder(((LinkedList)args));
      builder.directory(params.clientDir.toFile());
      builder.inheritIO();
      if (pipeOutput) {
         builder.redirectErrorStream(true);
         builder.redirectOutput(Redirect.PIPE);
      }

      return builder.start();
   }

   @LauncherAPI
   public static void main(String... args) throws Throwable {
      JVMHelper.verifySystemProperties(ClientLauncher.class);
      SecurityHelper.verifyCertificates(ClientLauncher.class);
      LogHelper.printVersion("Client Launcher");
      VerifyHelper.verifyInt(args.length, (l) -> {
         return l >= 1;
      }, "Missing args: <paramsFile>");
      Path paramsFile = IOHelper.toPath(args[0]);
      LogHelper.debug("Reading ClientLauncher params file");
      RSAPublicKey publicKey = Launcher.getConfig().publicKey;

      ClientLauncher.Params params;
      SignedObjectHolder profile;
      SignedObjectHolder jvmHDir;
      SignedObjectHolder assetHDir;
      SignedObjectHolder clientHDir;
      try {
         HInput input = new HInput(IOHelper.newInput(paramsFile));
         Throwable assetMatcher = null;

         try {
            params = new ClientLauncher.Params(input);
            profile = new SignedObjectHolder(input, publicKey, ClientProfile.RO_ADAPTER);
            jvmHDir = new SignedObjectHolder(input, publicKey, HashedDir::new);
            assetHDir = new SignedObjectHolder(input, publicKey, HashedDir::new);
            clientHDir = new SignedObjectHolder(input, publicKey, HashedDir::new);
         } catch (Throwable var105) {
            assetMatcher = var105;
            throw var105;
         } finally {
            if (input != null) {
               if (assetMatcher != null) {
                  try {
                     input.close();
                  } catch (Throwable var103) {
                     assetMatcher.addSuppressed(var103);
                  }
               } else {
                  input.close();
               }
            }

         }
      } finally {
         Files.delete(paramsFile);
      }

      LogHelper.debug("Verifying ClientLauncher sign and classpath");
      SecurityHelper.verifySign(LauncherRequest.BINARY_PATH, params.launcherSign, publicKey);
      URL[] var113 = JVMHelper.LOADER.getURLs();

      
      for(String ss : ((ClientProfile)profile.object).getClassPath() ) {
          URL classpathURL = new URL(ss);
         Path file = Paths.get(classpathURL.toURI());
         if (!file.startsWith(IOHelper.JVM_DIR) && !file.equals(LauncherRequest.BINARY_PATH)) {
            throw new SecurityException(String.format("Forbidden classpath entry: '%s'", file));
         }
      }

      LogHelper.debug("Starting JVM and client WatchService");
      FileNameMatcher assetMatcher = ((ClientProfile)profile.object).getAssetUpdateMatcher();
      FileNameMatcher clientMatcher = ((ClientProfile)profile.object).getClientUpdateMatcher(params.shaders, params.opis);
      DirWatcher jvmWatcher = new DirWatcher(IOHelper.JVM_DIR, (HashedDir)jvmHDir.object, (FileNameMatcher)null);
      Throwable var118 = null;

      try {
         DirWatcher assetWatcher = new DirWatcher(params.assetDir, (HashedDir)assetHDir.object, assetMatcher);
         Throwable var14 = null;

         try {
            DirWatcher clientWatcher = new DirWatcher(params.clientDir, (HashedDir)clientHDir.object, clientMatcher);
            Throwable var16 = null;

            try {
               verifyHDir(IOHelper.JVM_DIR, (HashedDir)jvmHDir.object, (FileNameMatcher)null);
               verifyHDir(params.assetDir, (HashedDir)assetHDir.object, assetMatcher);
               verifyHDir(params.clientDir, (HashedDir)clientHDir.object, clientMatcher);
               CommonHelper.newThread("JVM Directory Watcher", true, jvmWatcher).start();
               CommonHelper.newThread("Asset Directory Watcher", true, assetWatcher).start();
               CommonHelper.newThread("Client Directory Watcher", true, clientWatcher).start();
               launch((ClientProfile)profile.object, params);
            } catch (Throwable var104) {
               var16 = var104;
               throw var104;
            } finally {
               if (clientWatcher != null) {
                  if (var16 != null) {
                     try {
                        clientWatcher.close();
                     } catch (Throwable var102) {
                        var16.addSuppressed(var102);
                     }
                  } else {
                     clientWatcher.close();
                  }
               }

            }
         } catch (Throwable var107) {
            var14 = var107;
            throw var107;
         } finally {
            if (assetWatcher != null) {
               if (var14 != null) {
                  try {
                     assetWatcher.close();
                  } catch (Throwable var101) {
                     var14.addSuppressed(var101);
                  }
               } else {
                  assetWatcher.close();
               }
            }

         }
      } catch (Throwable var109) {
         var118 = var109;
         throw var109;
      } finally {
         if (jvmWatcher != null) {
            if (var118 != null) {
               try {
                  jvmWatcher.close();
               } catch (Throwable var100) {
                  var118.addSuppressed(var100);
               }
            } else {
               jvmWatcher.close();
            }
         }

      }

   }

   @LauncherAPI
   public static String toHash(UUID uuid) {
      return UUID_PATTERN.matcher(uuid.toString()).replaceAll("");
   }

   @LauncherAPI
   public static void verifyHDir(Path dir, HashedDir hdir, FileNameMatcher matcher) throws IOException {
      if (matcher != null) {
         matcher = matcher.verifyOnly();
      }

      HashedDir currentHDir = new HashedDir(dir, matcher, false);
      if (!hdir.diff(currentHDir, matcher).isSame()) {
         throw new SecurityException(String.format("Forbidden modification: '%s'", IOHelper.getFileName(dir)));
      }
   }

   private static void addClientArgs(Collection args, ClientProfile profile, ClientLauncher.Params params) {
      PlayerProfile pp = params.pp;
      ClientProfile.Version version = profile.getVersion();
      Collections.addAll(args, new String[]{"--username", pp.username});
      if (version.compareTo(ClientProfile.Version.MC172) >= 0) {
         Collections.addAll(args, new String[]{"--uuid", toHash(pp.uuid)});
         Collections.addAll(args, new String[]{"--accessToken", params.accessToken});
         if (version.compareTo(ClientProfile.Version.MC1710) >= 0) {
            Collections.addAll(args, new String[]{"--userType", "mojang"});
            JsonObject properties = Json.object();
            if (pp.skin != null) {
               properties.add("skinURL", Json.array(pp.skin.url));
               properties.add("skinDigest", Json.array(SecurityHelper.toHex(pp.skin.digest)));
            }

            if (pp.cloak != null) {
               properties.add("cloakURL", Json.array(pp.cloak.url));
               properties.add("cloakDigest", Json.array(SecurityHelper.toHex(pp.cloak.digest)));
            }

            Collections.addAll(args, new String[]{"--userProperties", properties.toString(WriterConfig.MINIMAL)});
            Collections.addAll(args, new String[]{"--assetIndex", profile.getAssetIndex()});
         }
      } else {
         Collections.addAll(args, new String[]{"--session", params.accessToken});
      }

      Collections.addAll(args, new String[]{"--version", profile.getVersion().name});
      Collections.addAll(args, new String[]{"--gameDir", params.clientDir.toString()});
      Collections.addAll(args, new String[]{"--assetsDir", params.assetDir.toString()});
      Collections.addAll(args, new String[]{"--resourcePackDir", params.clientDir.resolve(RESOURCEPACKS_DIR).toString()});
      if (version.compareTo(ClientProfile.Version.MC194) >= 0) {
         Collections.addAll(args, new String[]{"--versionType", "Launcher v15.2.1"});
      }

      if (params.autoEnter) {
         Collections.addAll(args, new String[]{"--server", profile.getServerAddress()});
         Collections.addAll(args, new String[]{"--port", Integer.toString(profile.getServerPort())});
      }

      if (params.fullScreen) {
         Collections.addAll(args, new String[]{"--fullscreen", Boolean.toString(true)});
      }

      if (params.width > 0 && params.height > 0) {
         Collections.addAll(args, new String[]{"--width", Integer.toString(params.width)});
         Collections.addAll(args, new String[]{"--height", Integer.toString(params.height)});
      }

   }

   private static void launch(ClientProfile profile, ClientLauncher.Params params) throws Throwable {
      JVMHelper.addNativePath(params.clientDir.resolve(NATIVES_DIR));
      Collection args = new LinkedList();
      addClientArgs(args, profile, params);
      Collections.addAll(args, profile.getClientArgs());
      URL[] classPath = resolveClassPath(params.clientDir, profile.getClassPath());

      for(URL url : classPath) {
         JVMHelper.UCP.addURL(url);
      }

      Class mainClass = Class.forName(profile.getMainClass());
      Method mainMethod = mainClass.getDeclaredMethod("main", String[].class);
      LAUNCHED.set(true);
      JVMHelper.fullGC();

      try {
         mainMethod.invoke((Object)null, (Object)args.toArray(new String[args.size()]));
      } catch (InvocationTargetException var11) {
         throw var11.getTargetException();
      } finally {
         LAUNCHED.set(false);
      }

   }

   private static URL[] resolveClassPath(Path clientDir, String... classPath) throws IOException {
      Collection result = new LinkedList();

      for(String classPathEntry : classPath) {
         Path path = clientDir.resolve(IOHelper.toPath(classPathEntry));
         if (IOHelper.isDir(path)) {
            IOHelper.walk(path, new ClientLauncher.ClassPathFileVisitor(result), false);
         } else {
            result.add(path);
         }
      }

      return (URL[])result.stream().map((path) -> IOHelper.toURL((Path) path)).toArray((x$0) -> {
         return new URL[x$0];
      });
   }

   private static final class ClassPathFileVisitor extends SimpleFileVisitor {
      private final Collection result;

      private ClassPathFileVisitor(Collection result) {
         this.result = result;
      }

      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
         if (IOHelper.hasExtension(file, "jar") || IOHelper.hasExtension(file, "zip")) {
            this.result.add(file);
         }

         return super.visitFile(file, attrs);
      }
   }

   public static final class Params extends StreamObject {
      private final byte[] launcherSign;
      @LauncherAPI
      public final Path assetDir;
      @LauncherAPI
      public final Path clientDir;
      @LauncherAPI
      public final PlayerProfile pp;
      @LauncherAPI
      public final String accessToken;
      @LauncherAPI
      public final boolean autoEnter;
      @LauncherAPI
      public final boolean fullScreen;
      @LauncherAPI
      public final boolean shaders;
      @LauncherAPI
      public final boolean opis;
      @LauncherAPI
      public final int ram;
      @LauncherAPI
      public final int width;
      @LauncherAPI
      public final int height;

      @LauncherAPI
      public Params(byte[] launcherSign, Path assetDir, Path clientDir, PlayerProfile pp, String accessToken, boolean autoEnter, boolean fullScreen, boolean shaders, boolean opis, int ram, int width, int height) {
         this.launcherSign = Arrays.copyOf(launcherSign, launcherSign.length);
         this.assetDir = assetDir;
         this.clientDir = clientDir;
         this.pp = pp;
         this.accessToken = SecurityHelper.verifyToken(accessToken);
         this.autoEnter = autoEnter;
         this.fullScreen = fullScreen;
         this.shaders = shaders;
         this.opis = opis;
         this.ram = ram;
         this.width = width;
         this.height = height;
      }

      @LauncherAPI
      public Params(HInput input) throws IOException {
         this.launcherSign = input.readByteArray(-256);
         this.assetDir = IOHelper.toPath(input.readString(0));
         this.clientDir = IOHelper.toPath(input.readString(0));
         this.pp = new PlayerProfile(input);
         this.accessToken = SecurityHelper.verifyToken(input.readASCII(-32));
         this.autoEnter = input.readBoolean();
         this.fullScreen = input.readBoolean();
         this.shaders = input.readBoolean();
         this.opis = input.readBoolean();
         this.ram = input.readVarInt();
         this.width = input.readVarInt();
         this.height = input.readVarInt();
      }

      public void write(HOutput output) throws IOException {
         output.writeByteArray(this.launcherSign, -256);
         output.writeString(this.assetDir.toString(), 0);
         output.writeString(this.clientDir.toString(), 0);
         this.pp.write(output);
         output.writeASCII(this.accessToken, -32);
         output.writeBoolean(this.autoEnter);
         output.writeBoolean(this.fullScreen);
         output.writeBoolean(this.shaders);
         output.writeBoolean(this.opis);
         output.writeVarInt(this.ram);
         output.writeVarInt(this.width);
         output.writeVarInt(this.height);
      }
   }
}
