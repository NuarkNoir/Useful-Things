package launcher.serialize.config.entry;

import java.io.IOException;
import java.util.Objects;
import launcher.LauncherAPI;
import launcher.serialize.HInput;
import launcher.serialize.HOutput;
import launcher.serialize.stream.EnumSerializer;
import launcher.serialize.stream.StreamObject;

public abstract class ConfigEntry extends StreamObject {
   @LauncherAPI
   public final boolean ro;
   private final String[] comments;
   private Object value;

   protected ConfigEntry(Object value, boolean ro, int cc) {
      this.ro = ro;
      this.comments = new String[cc];
      this.uncheckedSetValue(value);
   }

   @LauncherAPI
   public final String getComment(int i) {
      if (i < 0) {
         i += this.comments.length;
      }

      return i >= this.comments.length ? null : this.comments[i];
   }

   @LauncherAPI
   public abstract ConfigEntry.Type getType();

   @LauncherAPI
   public Object getValue() {
      return this.value;
   }

   @LauncherAPI
   public final void setComment(int i, String comment) {
      this.comments[i] = comment;
   }

   @LauncherAPI
   public final void setValue(Object value) {
      this.ensureWritable();
      this.uncheckedSetValue(value);
   }

   protected final void ensureWritable() {
      if (this.ro) {
         throw new UnsupportedOperationException("Read-only");
      }
   }

   protected void uncheckedSetValue(Object value) {
      this.value = Objects.requireNonNull(value, "value");
   }

   protected static ConfigEntry readEntry(HInput input, boolean ro) throws IOException {
      ConfigEntry.Type type = ConfigEntry.Type.read(input);
      switch(type) {
      case BOOLEAN:
         return new BooleanConfigEntry(input, ro);
      case INTEGER:
         return new IntegerConfigEntry(input, ro);
      case STRING:
         return new StringConfigEntry(input, ro);
      case LIST:
         return new ListConfigEntry(input, ro);
      case BLOCK:
         return new BlockConfigEntry(input, ro);
      default:
         throw new AssertionError("Unsupported config entry type: " + type.name());
      }
   }

   protected static void writeEntry(ConfigEntry entry, HOutput output) throws IOException {
      EnumSerializer.write(output, entry.getType());
      entry.write(output);
   }

   @LauncherAPI
   public static enum Type implements EnumSerializer.Itf {
      BLOCK(1),
      BOOLEAN(2),
      INTEGER(3),
      STRING(4),
      LIST(5);

      private static final EnumSerializer SERIALIZER = new EnumSerializer(ConfigEntry.Type.class);
      private final int n;

      private Type(int n) {
         this.n = n;
      }

      public int getNumber() {
         return this.n;
      }

      public static ConfigEntry.Type read(HInput input) throws IOException {
         return (ConfigEntry.Type)SERIALIZER.read(input);
      }
   }
}
