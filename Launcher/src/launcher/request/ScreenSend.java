package launcher.request;

import java.io.IOException;
import launcher.Launcher;
import launcher.LauncherAPI;
import launcher.client.PlayerProfile;
import launcher.helper.VerifyHelper;
import launcher.serialize.HInput;
import launcher.serialize.HOutput;

public final class ScreenSend extends Request {
   private final String username;
   private byte[] screenshot;

   @LauncherAPI
   public ScreenSend(Launcher.Config config, String username, byte[] screenshot) {
      super(config);
      this.username = VerifyHelper.verifyUsername(username);
      this.screenshot = screenshot;
   }

   @LauncherAPI
   public ScreenSend(String username, byte[] screenshot) {
      this((Launcher.Config)null, username, screenshot);
   }

   public Request.Type getType() {
      return Request.Type.SCREENSHOT;
   }

   protected PlayerProfile requestDo(HInput input, HOutput output) throws IOException {
      output.writeASCII(this.username, 16);
      output.writeByteArray(this.screenshot, 5000000);
      output.flush();
      return null;
   }
}
