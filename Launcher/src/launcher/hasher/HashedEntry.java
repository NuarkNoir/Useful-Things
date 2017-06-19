package launcher.hasher;

import java.io.IOException;
import launcher.LauncherAPI;
import launcher.serialize.HInput;
import launcher.serialize.stream.EnumSerializer;
import launcher.serialize.stream.StreamObject;

public abstract class HashedEntry extends StreamObject {
   @LauncherAPI
   public boolean flag;

   @LauncherAPI
   public abstract HashedEntry.Type getType();

   @LauncherAPI
   public abstract long size();

   @LauncherAPI
   public static enum Type implements EnumSerializer.Itf {
      DIR(1),
      FILE(2);

      private static final EnumSerializer SERIALIZER = new EnumSerializer(HashedEntry.Type.class);
      private final int n;

      private Type(int n) {
         this.n = n;
      }

      public int getNumber() {
         return this.n;
      }

      public static HashedEntry.Type read(HInput input) throws IOException {
         return (HashedEntry.Type)SERIALIZER.read(input);
      }
   }
}
