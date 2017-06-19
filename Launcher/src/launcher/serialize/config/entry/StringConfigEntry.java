package launcher.serialize.config.entry;

import java.io.IOException;
import launcher.LauncherAPI;
import launcher.serialize.HInput;
import launcher.serialize.HOutput;

public final class StringConfigEntry extends ConfigEntry {
   @LauncherAPI
   public StringConfigEntry(String value, boolean ro, int cc) {
      super(value, ro, cc);
   }

   @LauncherAPI
   public StringConfigEntry(HInput input, boolean ro) throws IOException {
      this(input.readString(0), ro, 0);
   }

   public ConfigEntry.Type getType() {
      return ConfigEntry.Type.STRING;
   }

   public void write(HOutput output) throws IOException {
      output.writeString((String)this.getValue(), 0);
   }

   protected void uncheckedSetValue(String value) {
      super.uncheckedSetValue(value);
   }
}
