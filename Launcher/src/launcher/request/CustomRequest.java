package launcher.request;

import launcher.Launcher;
import launcher.LauncherAPI;
import launcher.helper.VerifyHelper;
import launcher.serialize.HInput;
import launcher.serialize.HOutput;

public abstract class CustomRequest extends Request {
   @LauncherAPI
   public CustomRequest(Launcher.Config config) {
      super(config);
   }

   @LauncherAPI
   public CustomRequest() {
      this((Launcher.Config)null);
   }

   public final Request.Type getType() {
      return Request.Type.CUSTOM;
   }

   protected final Object requestDo(HInput input, HOutput output) throws Exception {
      output.writeASCII(VerifyHelper.verifyIDName(this.getName()), 255);
      output.flush();
      return this.requestDoCustom(input, output);
   }

   @LauncherAPI
   public abstract String getName();

   @LauncherAPI
   protected abstract Object requestDoCustom(HInput var1, HOutput var2);
}
