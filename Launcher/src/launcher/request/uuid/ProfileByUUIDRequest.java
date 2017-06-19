package launcher.request.uuid;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
import launcher.Launcher;
import launcher.LauncherAPI;
import launcher.client.PlayerProfile;
import launcher.request.Request;
import launcher.serialize.HInput;
import launcher.serialize.HOutput;

public final class ProfileByUUIDRequest extends Request {
   private final UUID uuid;

   @LauncherAPI
   public ProfileByUUIDRequest(Launcher.Config config, UUID uuid) {
      super(config);
      this.uuid = (UUID)Objects.requireNonNull(uuid, "uuid");
   }

   @LauncherAPI
   public ProfileByUUIDRequest(UUID uuid) {
      this((Launcher.Config)null, uuid);
   }

   public Request.Type getType() {
      return Request.Type.PROFILE_BY_UUID;
   }

   protected PlayerProfile requestDo(HInput input, HOutput output) throws IOException {
      output.writeUUID(this.uuid);
      output.flush();
      return input.readBoolean() ? new PlayerProfile(input) : null;
   }
}
