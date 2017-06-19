package launcher.request.update;

import java.nio.file.Path;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import launcher.Launcher;
import launcher.LauncherAPI;
import launcher.client.ClientLauncher;
import launcher.client.ClientProfile;
import launcher.helper.IOHelper;
import launcher.helper.JVMHelper;
import launcher.helper.LogHelper;
import launcher.helper.SecurityHelper;
import launcher.request.Request;
import launcher.serialize.HInput;
import launcher.serialize.HOutput;
import launcher.serialize.signed.SignedObjectHolder;

public final class LauncherRequest extends Request {
   @LauncherAPI
   public static final Path BINARY_PATH = IOHelper.getCodeSource(Launcher.class);
   @LauncherAPI
   public static final boolean EXE_BINARY = IOHelper.hasExtension(BINARY_PATH, "exe");

   @LauncherAPI
   public LauncherRequest(Launcher.Config config) {
      super(config);
   }

   @LauncherAPI
   public LauncherRequest() {
      this((Launcher.Config)null);
   }

   public Request.Type getType() {
      return Request.Type.LAUNCHER;
   }

   protected LauncherRequest.Result requestDo(HInput input, HOutput output) throws Exception {
      output.writeBoolean(EXE_BINARY);
      output.flush();
      this.readError(input);
      RSAPublicKey publicKey = this.config.publicKey;
      byte[] sign = input.readByteArray(-256);
      boolean shouldUpdate = !SecurityHelper.isValidSign(BINARY_PATH, sign, publicKey);
      output.writeBoolean(shouldUpdate);
      output.flush();
      if (shouldUpdate) {
         byte[] binary = input.readByteArray(0);
         SecurityHelper.verifySign(binary, sign, publicKey);
         ProcessBuilder builder = new ProcessBuilder(new String[]{IOHelper.resolveJavaBin((Path)null).toString(), ClientLauncher.jvmProperty("launcher.debug", Boolean.toString(LogHelper.isDebugEnabled())), "-jar", BINARY_PATH.toString()});
         builder.inheritIO();
         IOHelper.write(BINARY_PATH, binary);
         builder.start();
         JVMHelper.RUNTIME.exit(255);
         throw new AssertionError("Why Launcher wasn't restarted?!");
      } else {
         int count = input.readLength(0);
         List profiles = new ArrayList(count);

         for(int i = 0; i < count; ++i) {
            profiles.add(new SignedObjectHolder(input, publicKey, ClientProfile.RO_ADAPTER));
         }

         return new LauncherRequest.Result(sign, profiles);
      }
   }

   public static final class Result {
      @LauncherAPI
      public final List profiles;
      private final byte[] sign;

      private Result(byte[] sign, List profiles) {
         this.sign = Arrays.copyOf(sign, sign.length);
         this.profiles = Collections.unmodifiableList(profiles);
      }

      @LauncherAPI
      public byte[] getSign() {
         return Arrays.copyOf(this.sign, this.sign.length);
      }
   }
}
