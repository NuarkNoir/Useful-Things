package launcher.request.update;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import launcher.Launcher;
import launcher.LauncherAPI;
import launcher.helper.IOHelper;
import launcher.request.Request;
import launcher.serialize.HInput;
import launcher.serialize.HOutput;

public final class UpdateListRequest extends Request {
   @LauncherAPI
   public UpdateListRequest(Launcher.Config config) {
      super(config);
   }

   @LauncherAPI
   public UpdateListRequest() {
      this((Launcher.Config)null);
   }

   public Request.Type getType() {
      return Request.Type.UPDATE_LIST;
   }

   protected Set requestDo(HInput input, HOutput output) throws IOException {
      int count = input.readLength(0);
      Set result = new HashSet(count);

      for(int i = 0; i < count; ++i) {
         result.add(IOHelper.verifyFileName(input.readString(1024)));
      }

      return Collections.unmodifiableSet(result);
   }
}
