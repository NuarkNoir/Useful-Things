package launcher.request.uuid;

import java.io.IOException;
import launcher.Launcher;
import launcher.LauncherAPI;
import launcher.client.PlayerProfile;
import launcher.helper.VerifyHelper;
import launcher.request.Request;
import launcher.serialize.HInput;
import launcher.serialize.HOutput;

public final class ProfileByUsernameRequest extends Request {
   private final String username;

   @LauncherAPI
   public ProfileByUsernameRequest(Launcher.Config config, String username) {
      super(config);
      this.username = VerifyHelper.verifyUsername(username);
   }

   @LauncherAPI
   public ProfileByUsernameRequest(String username) {
      this((Launcher.Config)null, username);
   }

   public Request.Type getType() {
      return Request.Type.PROFILE_BY_USERNAME;
   }

   protected PlayerProfile requestDo(HInput input, HOutput output) throws IOException {
      output.writeASCII(this.username, 16);
      output.flush();
      return input.readBoolean() ? new PlayerProfile(input) : null;
   }
}
