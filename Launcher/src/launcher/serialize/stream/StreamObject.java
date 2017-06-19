package launcher.serialize.stream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import launcher.LauncherAPI;
import launcher.helper.IOHelper;
import launcher.serialize.HInput;
import launcher.serialize.HOutput;

public abstract class StreamObject {
   @LauncherAPI
   public abstract void write(HOutput var1) throws IOException;

   @LauncherAPI
   public final byte[] write() throws IOException {
      ByteArrayOutputStream array = IOHelper.newByteArrayOutput();
      Throwable var2 = null;

      byte[] var31;
      try {
         HOutput output = new HOutput(array);
         Throwable var4 = null;

         try {
            this.write(output);
         } catch (Throwable var27) {
            var4 = var27;
            throw var27;
         } finally {
            if (output != null) {
               if (var4 != null) {
                  try {
                     output.close();
                  } catch (Throwable var26) {
                     var4.addSuppressed(var26);
                  }
               } else {
                  output.close();
               }
            }

         }

         var31 = array.toByteArray();
      } catch (Throwable var29) {
         var2 = var29;
         throw var29;
      } finally {
         if (array != null) {
            if (var2 != null) {
               try {
                  array.close();
               } catch (Throwable var25) {
                  var2.addSuppressed(var25);
               }
            } else {
               array.close();
            }
         }

      }

      return var31;
   }

   @FunctionalInterface
   public interface Adapter {
      @LauncherAPI
      StreamObject convert(HInput var1) throws IOException;
   }
}
