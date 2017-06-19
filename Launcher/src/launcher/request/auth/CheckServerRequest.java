package launcher.request.auth;

import java.io.IOException;
import launcher.Launcher;
import launcher.LauncherAPI;
import launcher.client.PlayerProfile;
import launcher.helper.VerifyHelper;
import launcher.request.Request;
import launcher.serialize.HInput;
import launcher.serialize.HOutput;

public final class CheckServerRequest extends Request {
   private final String username;
   private final String serverID;

   @LauncherAPI
   public CheckServerRequest(Launcher.Config config, String username, String serverID) {
      super(config);
      this.username = VerifyHelper.verifyUsername(username);
      this.serverID = JoinServerRequest.verifyServerID(serverID);
   }

   @LauncherAPI
   public CheckServerRequest(String username, String serverID) {
      this((Launcher.Config)null, username, serverID);
   }

   public Request.Type getType() {
      return Request.Type.CHECK_SERVER;
   }

   protected PlayerProfile requestDo(HInput input, HOutput output) throws IOException {
      output.writeASCII(this.username, 16);
      output.writeASCII(this.serverID, 41);
      output.flush();
      this.readError(input);
      return input.readBoolean() ? new PlayerProfile(input) : null;
   }
}
