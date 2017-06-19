package launcher.serialize.config.entry;

import java.io.IOException;
import launcher.LauncherAPI;
import launcher.serialize.HInput;
import launcher.serialize.HOutput;

public final class BooleanConfigEntry extends ConfigEntry {
   @LauncherAPI
   public BooleanConfigEntry(boolean value, boolean ro, int cc) {
      super(Boolean.valueOf(value), ro, cc);
   }

   @LauncherAPI
   public BooleanConfigEntry(HInput input, boolean ro) throws IOException {
      this(input.readBoolean(), ro, 0);
   }

   public ConfigEntry.Type getType() {
      return ConfigEntry.Type.BOOLEAN;
   }

   public void write(HOutput output) throws IOException {
      output.writeBoolean(((Boolean)this.getValue()).booleanValue());
   }
}
