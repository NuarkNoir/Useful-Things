package launcher.request.uuid;

import java.io.IOException;
import java.util.Arrays;
import launcher.Launcher;
import launcher.LauncherAPI;
import launcher.client.PlayerProfile;
import launcher.helper.IOHelper;
import launcher.helper.VerifyHelper;
import launcher.request.Request;
import launcher.serialize.HInput;
import launcher.serialize.HOutput;

public final class BatchProfileByUsernameRequest extends Request {
   @LauncherAPI
   public static final int MAX_BATCH_SIZE = 128;
   private final String[] usernames;

   @LauncherAPI
   public BatchProfileByUsernameRequest(Launcher.Config config, String... usernames) throws IOException {
      super(config);
      this.usernames = (String[])Arrays.copyOf(usernames, usernames.length);
      IOHelper.verifyLength(this.usernames.length, 128);

      for(String username : this.usernames) {
         VerifyHelper.verifyUsername(username);
      }

   }

   @LauncherAPI
   public BatchProfileByUsernameRequest(String... usernames) throws IOException {
      this((Launcher.Config)null, usernames);
   }

   public Request.Type getType() {
      return Request.Type.BATCH_PROFILE_BY_USERNAME;
   }

   protected PlayerProfile[] requestDo(HInput input, HOutput output) throws IOException {
      output.writeLength(this.usernames.length, 128);

      for(String username : this.usernames) {
         output.writeASCII(username, 16);
      }

      output.flush();
      PlayerProfile[] profiles = new PlayerProfile[this.usernames.length];

      for(int i = 0; i < profiles.length; ++i) {
         profiles[i] = input.readBoolean() ? new PlayerProfile(input) : null;
      }

      return profiles;
   }
}
