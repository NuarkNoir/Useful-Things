package launcher.request.auth;

import java.io.IOException;
import java.util.Arrays;
import launcher.Launcher;
import launcher.LauncherAPI;
import launcher.client.PlayerProfile;
import launcher.helper.JVMHelper;
import launcher.helper.VerifyHelper;
import launcher.helper.jWMIHelper;
import launcher.request.Request;
import launcher.serialize.HInput;
import launcher.serialize.HOutput;

public final class AuthRequest extends Request {
   private final String login;
   private String HWID;
   private final byte[] encryptedPassword;

   @LauncherAPI
   public AuthRequest(Launcher.Config config, String login, byte[] encryptedPassword) {
      super(config);
      this.login = (String)VerifyHelper.verify(login, VerifyHelper.NOT_EMPTY, "Login can't be empty");
      this.encryptedPassword = Arrays.copyOf(encryptedPassword, encryptedPassword.length);
   }

   @LauncherAPI
   public AuthRequest(String login, byte[] encryptedPassword) {
      this((Launcher.Config)null, login, encryptedPassword);
   }

   public Request.Type getType() {
      return Request.Type.AUTH;
   }

   protected AuthRequest.Result requestDo(HInput input, HOutput output) throws IOException {
      String HWIDstr = "+";
      HWIDstr = HWIDstr + "|" + JVMHelper.OS_TYPE;
      HWIDstr = HWIDstr + "|" + JVMHelper.OS_VERSION;
      switch(JVMHelper.OS_TYPE) {
      case MUSTDIE:
         HWIDstr = HWIDstr + "|" + jWMIHelper.getDataWin();
         this.HWID = HWIDstr;
         break;
      case LINUX:
         HWIDstr = HWIDstr + "|" + jWMIHelper.getDatalinux();
         this.HWID = HWIDstr;
         break;
      case MACOSX:
         HWIDstr = HWIDstr + "|" + jWMIHelper.getDataMacOS();
         this.HWID = HWIDstr;
         break;
      default:
         HWIDstr = HWIDstr + "Unknown OS";
         this.HWID = HWIDstr;
      }

      output.writeString(this.login, 255);
      output.writeByteArray(this.encryptedPassword, 2048);
      output.writeString(this.HWID, 10240);
      output.flush();
      this.readError(input);
      PlayerProfile pp = new PlayerProfile(input);
      String accessToken = input.readASCII(-32);
      return new AuthRequest.Result(pp, accessToken);
   }

   public static final class Result {
      @LauncherAPI
      public final PlayerProfile pp;
      @LauncherAPI
      public final String accessToken;

      private Result(PlayerProfile pp, String accessToken) {
         this.pp = pp;
         this.accessToken = accessToken;
      }
   }
}
