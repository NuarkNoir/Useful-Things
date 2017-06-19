package launcher.client;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.UUID;
import launcher.LauncherAPI;
import launcher.helper.IOHelper;
import launcher.helper.SecurityHelper;
import launcher.helper.VerifyHelper;
import launcher.serialize.HInput;
import launcher.serialize.HOutput;
import launcher.serialize.stream.StreamObject;

public final class PlayerProfile extends StreamObject {
   @LauncherAPI
   public final UUID uuid;
   @LauncherAPI
   public final String username;
   @LauncherAPI
   public final PlayerProfile.Texture skin;
   @LauncherAPI
   public final PlayerProfile.Texture cloak;

   @LauncherAPI
   public PlayerProfile(HInput input) throws IOException {
      this.uuid = input.readUUID();
      this.username = VerifyHelper.verifyUsername(input.readASCII(16));
      this.skin = input.readBoolean() ? new PlayerProfile.Texture(input) : null;
      this.cloak = input.readBoolean() ? new PlayerProfile.Texture(input) : null;
   }

   @LauncherAPI
   public PlayerProfile(UUID uuid, String username, PlayerProfile.Texture skin, PlayerProfile.Texture cloak) {
      this.uuid = (UUID)Objects.requireNonNull(uuid, "uuid");
      this.username = VerifyHelper.verifyUsername(username);
      this.skin = skin;
      this.cloak = cloak;
   }

   public void write(HOutput output) throws IOException {
      output.writeUUID(this.uuid);
      output.writeASCII(this.username, 16);
      output.writeBoolean(this.skin != null);
      if (this.skin != null) {
         this.skin.write(output);
      }

      output.writeBoolean(this.cloak != null);
      if (this.cloak != null) {
         this.cloak.write(output);
      }

   }

   @LauncherAPI
   public static PlayerProfile newOfflineProfile(String username) {
      return new PlayerProfile(offlineUUID(username), username, (PlayerProfile.Texture)null, (PlayerProfile.Texture)null);
   }

   @LauncherAPI
   public static UUID offlineUUID(String username) {
      return UUID.nameUUIDFromBytes(IOHelper.encodeASCII("OfflinePlayer:" + username));
   }

   public static final class Texture extends StreamObject {
      @LauncherAPI
      public final String url;
      @LauncherAPI
      public final byte[] digest;

      @LauncherAPI
      public Texture(String url, byte[] digest) {
         this.url = IOHelper.verifyURL(url);
         this.digest = (byte[])Objects.requireNonNull(digest, "digest");
      }

      @LauncherAPI
      public Texture(String url) throws IOException {
         this.url = IOHelper.verifyURL(url);
         this.digest = SecurityHelper.digest(SecurityHelper.DigestAlgorithm.SHA256, new URL(url));
      }

      @LauncherAPI
      public Texture(HInput input) throws IOException {
         this.url = IOHelper.verifyURL(input.readASCII(2048));
         this.digest = input.readByteArray(2048);
      }

      public void write(HOutput output) throws IOException {
         output.writeASCII(this.url, 2048);
         output.writeByteArray(this.digest, 2048);
      }
   }
}
