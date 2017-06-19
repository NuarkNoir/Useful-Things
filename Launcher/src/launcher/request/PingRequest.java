package launcher.request;

import java.io.IOException;
import launcher.Launcher;
import launcher.LauncherAPI;
import launcher.serialize.HInput;
import launcher.serialize.HOutput;

public final class PingRequest extends Request {
   @LauncherAPI
   public static final byte EXPECTED_BYTE = 85;

   @LauncherAPI
   public PingRequest(Launcher.Config config) {
      super(config);
   }

   @LauncherAPI
   public PingRequest() {
      this((Launcher.Config)null);
   }

   public Request.Type getType() {
      return Request.Type.PING;
   }

   protected Void requestDo(HInput input, HOutput output) throws IOException {
      byte pong = (byte)input.readUnsignedByte();
      if (pong != 85) {
         throw new IOException("Illegal ping response: " + pong);
      } else {
         return null;
      }
   }
}
