package launcher.serialize.signed;

import java.io.IOException;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import launcher.LauncherAPI;
import launcher.serialize.HInput;
import launcher.serialize.stream.StreamObject;

public final class SignedObjectHolder extends SignedBytesHolder {
   @LauncherAPI
   public final StreamObject object;

   @LauncherAPI
   public SignedObjectHolder(HInput input, RSAPublicKey publicKey, StreamObject.Adapter adapter) throws IOException, SignatureException {
      super(input, publicKey);
      this.object = this.newInstance(adapter);
   }

   @LauncherAPI
   public SignedObjectHolder(StreamObject object, RSAPrivateKey privateKey) throws IOException {
      super(object.write(), privateKey);
      this.object = object;
   }

   @LauncherAPI
   public StreamObject newInstance(StreamObject.Adapter adapter) throws IOException {
      HInput input = new HInput(this.bytes);
      Throwable var3 = null;

      StreamObject var4;
      try {
         var4 = adapter.convert(input);
      } catch (Throwable var13) {
         var3 = var13;
         throw var13;
      } finally {
         if (input != null) {
            if (var3 != null) {
               try {
                  input.close();
               } catch (Throwable var12) {
                  var3.addSuppressed(var12);
               }
            } else {
               input.close();
            }
         }

      }

      return var4;
   }

   public boolean equals(Object obj) {
      return obj instanceof SignedObjectHolder && this.object.equals(((SignedObjectHolder)obj).object);
   }

   public int hashCode() {
      return this.object.hashCode();
   }

   public String toString() {
      return this.object.toString();
   }
}
