package launcher.request.auth;

import java.io.IOException;
import java.util.regex.Pattern;
import launcher.Launcher;
import launcher.LauncherAPI;
import launcher.helper.SecurityHelper;
import launcher.helper.VerifyHelper;
import launcher.request.Request;
import launcher.serialize.HInput;
import launcher.serialize.HOutput;

public final class JoinServerRequest extends Request {
   private static final Pattern SERVERID_PATTERN = Pattern.compile("-?[0-9a-f]{1,40}");
   private final String username;
   private final String accessToken;
   private final String serverID;

   @LauncherAPI
   public JoinServerRequest(Launcher.Config config, String username, String accessToken, String serverID) {
      super(config);
      this.username = VerifyHelper.verifyUsername(username);
      this.accessToken = SecurityHelper.verifyToken(accessToken);
      this.serverID = verifyServerID(serverID);
   }

   @LauncherAPI
   public JoinServerRequest(String username, String accessToken, String serverID) {
      this((Launcher.Config)null, username, accessToken, serverID);
   }

   public Request.Type getType() {
      return Request.Type.JOIN_SERVER;
   }

   protected Boolean requestDo(HInput input, HOutput output) throws IOException {
      output.writeASCII(this.username, 16);
      output.writeASCII(this.accessToken, -32);
      output.writeASCII(this.serverID, 41);
      output.flush();
      this.readError(input);
      return input.readBoolean();
   }

   @LauncherAPI
   public static boolean isValidServerID(CharSequence serverID) {
      return SERVERID_PATTERN.matcher(serverID).matches();
   }

   @LauncherAPI
   public static String verifyServerID(String serverID) {
      return (String)VerifyHelper.verify(serverID, (serverID1) -> JoinServerRequest.isValidServerID((CharSequence) serverID1), String.format("Invalid server ID: '%s'", serverID));
   }
}
