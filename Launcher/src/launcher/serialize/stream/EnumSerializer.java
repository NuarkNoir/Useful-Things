package launcher.serialize.stream;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import launcher.LauncherAPI;
import launcher.helper.VerifyHelper;
import launcher.serialize.HInput;
import launcher.serialize.HOutput;

public final class EnumSerializer {
   private final Map map = new HashMap(16);

   @LauncherAPI
   public EnumSerializer(Class clazz) {
      for(Field field : clazz.getFields()) {
         if (field.isEnumConstant()) {
            EnumSerializer.Itf itf;
            try {
               itf = (EnumSerializer.Itf)field.get((Object)null);
            } catch (IllegalAccessException var8) {
               throw new InternalError(var8);
            }

            VerifyHelper.putIfAbsent(this.map, Integer.valueOf(itf.getNumber()), clazz.cast(itf), "Duplicate number for enum constant " + field.getName());
         }
      }

   }

   @LauncherAPI
   public Enum read(HInput input) throws IOException {
      int n = input.readVarInt();
      return (Enum)VerifyHelper.getMapValue(this.map, Integer.valueOf(n), "Unknown enum number: " + n);
   }

   @LauncherAPI
   public static void write(HOutput output, EnumSerializer.Itf itf) throws IOException {
      output.writeVarInt(itf.getNumber());
   }

   @FunctionalInterface
   public interface Itf {
      @LauncherAPI
      int getNumber();
   }
}
