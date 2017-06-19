package launcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.file.NoSuchFileException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import launcher.client.ClientLauncher;
import launcher.client.ClientProfile;
import launcher.client.PlayerProfile;
import launcher.client.ServerPinger;
import launcher.hasher.FileNameMatcher;
import launcher.hasher.HashedDir;
import launcher.hasher.HashedEntry;
import launcher.hasher.HashedFile;
import launcher.helper.ClientApp;
import launcher.helper.CommonHelper;
import launcher.helper.IOHelper;
import launcher.helper.JVMHelper;
import launcher.helper.LogHelper;
import launcher.helper.SecurityHelper;
import launcher.helper.VerifyHelper;
import launcher.helper.js.JSApplication;
import launcher.request.CustomRequest;
import launcher.request.PingRequest;
import launcher.request.Request;
import launcher.request.RequestException;
import launcher.request.ScreenSend;
import launcher.request.auth.AuthRequest;
import launcher.request.auth.CheckServerRequest;
import launcher.request.auth.JoinServerRequest;
import launcher.request.update.LauncherRequest;
import launcher.request.update.UpdateRequest;
import launcher.request.uuid.BatchProfileByUsernameRequest;
import launcher.request.uuid.ProfileByUUIDRequest;
import launcher.request.uuid.ProfileByUsernameRequest;
import launcher.serialize.HInput;
import launcher.serialize.HOutput;
import launcher.serialize.config.ConfigObject;
import launcher.serialize.config.TextConfigReader;
import launcher.serialize.config.TextConfigWriter;
import launcher.serialize.config.entry.BlockConfigEntry;
import launcher.serialize.config.entry.BooleanConfigEntry;
import launcher.serialize.config.entry.ConfigEntry;
import launcher.serialize.config.entry.IntegerConfigEntry;
import launcher.serialize.config.entry.ListConfigEntry;
import launcher.serialize.config.entry.StringConfigEntry;
import launcher.serialize.signed.SignedBytesHolder;
import launcher.serialize.signed.SignedObjectHolder;
import launcher.serialize.stream.EnumSerializer;
import launcher.serialize.stream.StreamObject;

public final class Launcher {
   private static final AtomicReference CONFIG = new AtomicReference();
   @LauncherAPI
   public static final String VERSION = "15.2.1";
   @LauncherAPI
   public static final String BUILD = readBuildNumber();
   @LauncherAPI
   public static final int PROTOCOL_MAGIC = 1917264918;
   @LauncherAPI
   public static final String RUNTIME_DIR = "runtime";
   @LauncherAPI
   public static final String CONFIG_FILE = "config.bin";
   @LauncherAPI
   public static final String INIT_SCRIPT_FILE = "init.js";
   private final AtomicBoolean started = new AtomicBoolean(false);
   private final ScriptEngine engine = CommonHelper.newScriptEngine();

   private Launcher() {
      this.setScriptBindings();
   }

   @LauncherAPI
   public Object loadScript(URL url) throws IOException, ScriptException {
      LogHelper.debug("Loading script: '%s'", url);
      BufferedReader reader = IOHelper.newReader(url);
      Throwable var3 = null;

      Object var4;
      try {
         var4 = this.engine.eval(reader);
      } catch (Throwable var13) {
         var3 = var13;
         throw var13;
      } finally {
         if (reader != null) {
            if (var3 != null) {
               try {
                  reader.close();
               } catch (Throwable var12) {
                  var3.addSuppressed(var12);
               }
            } else {
               reader.close();
            }
         }

      }

      return var4;
   }

   @LauncherAPI
   public void start(String... args) throws Throwable {
      Objects.requireNonNull(args, "args");
      if (this.started.getAndSet(true)) {
         throw new IllegalStateException("Launcher has been already started");
      } else {
         this.loadScript(getResourceURL("init.js"));
         LogHelper.info("Invoking start() function");
         ((Invocable)this.engine).invokeFunction("start", (Object) args);
      }
   }

   private void setScriptBindings() {
      LogHelper.info("Setting up script engine bindings");
      Bindings bindings = this.engine.getBindings(100);
      bindings.put("launcher", this);
      addLauncherClassBindings(bindings);
   }

   @LauncherAPI
   public static void addLauncherClassBindings(Map bindings) {
      bindings.put("LauncherClass", Launcher.class);
      bindings.put("PlayerProfileClass", PlayerProfile.class);
      bindings.put("PlayerProfileTextureClass", PlayerProfile.Texture.class);
      bindings.put("ClientProfileClass", ClientProfile.class);
      bindings.put("ClientProfileVersionClass", ClientProfile.Version.class);
      bindings.put("ClientLauncherClass", ClientLauncher.class);
      bindings.put("ClientLauncherParamsClass", ClientLauncher.Params.class);
      bindings.put("ServerPingerClass", ServerPinger.class);
      bindings.put("RequestClass", Request.class);
      bindings.put("RequestTypeClass", Request.Type.class);
      bindings.put("RequestExceptionClass", RequestException.class);
      bindings.put("CustomRequestClass", CustomRequest.class);
      bindings.put("PingRequestClass", PingRequest.class);
      bindings.put("AuthRequestClass", AuthRequest.class);
      bindings.put("JoinServerRequestClass", JoinServerRequest.class);
      bindings.put("CheckServerRequestClass", CheckServerRequest.class);
      bindings.put("ScreenSendClass", ScreenSend.class);
      bindings.put("ClientAppClass", ClientApp.class);
      bindings.put("UpdateRequestClass", UpdateRequest.class);
      bindings.put("LauncherRequestClass", LauncherRequest.class);
      bindings.put("ProfileByUsernameRequestClass", ProfileByUsernameRequest.class);
      bindings.put("ProfileByUUIDRequestClass", ProfileByUUIDRequest.class);
      bindings.put("BatchProfileByUsernameRequestClass", BatchProfileByUsernameRequest.class);
      bindings.put("FileNameMatcherClass", FileNameMatcher.class);
      bindings.put("HashedDirClass", HashedDir.class);
      bindings.put("HashedFileClass", HashedFile.class);
      bindings.put("HashedEntryTypeClass", HashedEntry.Type.class);
      bindings.put("HInputClass", HInput.class);
      bindings.put("HOutputClass", HOutput.class);
      bindings.put("StreamObjectClass", StreamObject.class);
      bindings.put("StreamObjectAdapterClass", StreamObject.Adapter.class);
      bindings.put("SignedBytesHolderClass", SignedBytesHolder.class);
      bindings.put("SignedObjectHolderClass", SignedObjectHolder.class);
      bindings.put("EnumSerializerClass", EnumSerializer.class);
      bindings.put("ConfigObjectClass", ConfigObject.class);
      bindings.put("ConfigObjectAdapterClass", ConfigObject.Adapter.class);
      bindings.put("BlockConfigEntryClass", BlockConfigEntry.class);
      bindings.put("BooleanConfigEntryClass", BooleanConfigEntry.class);
      bindings.put("IntegerConfigEntryClass", IntegerConfigEntry.class);
      bindings.put("ListConfigEntryClass", ListConfigEntry.class);
      bindings.put("StringConfigEntryClass", StringConfigEntry.class);
      bindings.put("ConfigEntryTypeClass", ConfigEntry.Type.class);
      bindings.put("TextConfigReaderClass", TextConfigReader.class);
      bindings.put("TextConfigWriterClass", TextConfigWriter.class);
      bindings.put("CommonHelperClass", CommonHelper.class);
      bindings.put("IOHelperClass", IOHelper.class);
      bindings.put("JVMHelperClass", JVMHelper.class);
      bindings.put("JVMHelperOSClass", JVMHelper.OS.class);
      bindings.put("LogHelperClass", LogHelper.class);
      bindings.put("LogHelperOutputClass", LogHelper.Output.class);
      bindings.put("SecurityHelperClass", SecurityHelper.class);
      bindings.put("DigestAlgorithmClass", SecurityHelper.DigestAlgorithm.class);
      bindings.put("VerifyHelperClass", VerifyHelper.class);

      try {
         Class.forName("javafx.application.Application");
         bindings.put("JSApplicationClass", JSApplication.class);
      } catch (ClassNotFoundException var2) {
         LogHelper.warning("JavaFX API isn't available");
      }

   }

   @LauncherAPI
   public static Launcher.Config getConfig() {
      Launcher.Config config = (Launcher.Config)CONFIG.get();
      if (config == null) {
         try {
            HInput input = new HInput(IOHelper.newInput(IOHelper.getResourceURL("config.bin")));
            Throwable var2 = null;

            try {
               config = new Launcher.Config(input);
            } catch (Throwable var12) {
               var2 = var12;
               throw var12;
            } finally {
               if (input != null) {
                  if (var2 != null) {
                     try {
                        input.close();
                     } catch (Throwable var11) {
                        var2.addSuppressed(var11);
                     }
                  } else {
                     input.close();
                  }
               }

            }
         } catch (InvalidKeySpecException | IOException var14) {
            throw new SecurityException(var14);
         }

         CONFIG.set(config);
      }

      return config;
   }

   @LauncherAPI
   public static URL getResourceURL(String name) throws IOException {
      Launcher.Config config = getConfig();
      byte[] validDigest = (byte[])config.runtime.get(name);
      if (validDigest == null) {
         throw new NoSuchFileException(name);
      } else {
         URL url = IOHelper.getResourceURL("runtime/" + name);
         if (!Arrays.equals(validDigest, SecurityHelper.digest(SecurityHelper.DigestAlgorithm.MD5, url))) {
            throw new NoSuchFileException(name);
         } else {
            return url;
         }
      }
   }

   @LauncherAPI
   public static String getVersion() {
      return "15.2.1";
   }

   public static void main(String... args) throws Throwable {
      JVMHelper.verifySystemProperties(Launcher.class);
      SecurityHelper.verifyCertificates(Launcher.class);
      LogHelper.printVersion("Launcher");
      Instant start = Instant.now();

      try {
         (new Launcher()).start(args);
      } catch (Exception var3) {
         LogHelper.error(var3);
         return;
      }

      Instant end = Instant.now();
      LogHelper.debug("Launcher started in %dms", Duration.between(start, end).toMillis());
   }

   private static String readBuildNumber() {
      try {
         return IOHelper.request(IOHelper.getResourceURL("buildnumber"));
      } catch (IOException var1) {
         return "dev";
      }
   }

   public static final class Config extends StreamObject {
      private static final String ADDRESS_OVERRIDE = System.getProperty("launcher.addressOverride", (String)null);
      @LauncherAPI
      public final InetSocketAddress address;
      @LauncherAPI
      public final RSAPublicKey publicKey;
      @LauncherAPI
      public final Map runtime;

      @LauncherAPI
      public Config(String address, int port, RSAPublicKey publicKey, Map runtime) {
         this.address = InetSocketAddress.createUnresolved(address, port);
         this.publicKey = (RSAPublicKey)Objects.requireNonNull(publicKey, "publicKey");
         this.runtime = Collections.unmodifiableMap(new HashMap(runtime));
      }

      @LauncherAPI
      public Config(HInput input) throws IOException, InvalidKeySpecException {
         String localAddress = input.readASCII(255);
         this.address = InetSocketAddress.createUnresolved(ADDRESS_OVERRIDE == null ? localAddress : ADDRESS_OVERRIDE, input.readLength(65535));
         this.publicKey = SecurityHelper.toPublicRSAKey(input.readByteArray(2048));
         int count = input.readLength(0);
         Map localResources = new HashMap(count);

         for(int i = 0; i < count; ++i) {
            String name = input.readString(255);
            VerifyHelper.putIfAbsent(localResources, name, input.readByteArray(2048), String.format("Duplicate runtime resource: '%s'", name));
         }

         this.runtime = Collections.unmodifiableMap(localResources);
         if (ADDRESS_OVERRIDE != null) {
            LogHelper.warning("Address override is enabled: '%s'", ADDRESS_OVERRIDE);
         }

      }

      public void write(HOutput output) throws IOException {
         output.writeASCII(this.address.getHostString(), 255);
         output.writeLength(this.address.getPort(), 65535);
         output.writeByteArray(this.publicKey.getEncoded(), 2048);
         Set<Entry> entrySet = this.runtime.entrySet();
         output.writeLength(entrySet.size(), 0);

         for(Entry entry : entrySet) {
            output.writeString((String)entry.getKey(), 255);
            output.writeByteArray((byte[])entry.getValue(), 2048);
         }

      }
   }
}
