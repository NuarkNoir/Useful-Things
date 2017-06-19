package launcher.serialize.config.entry;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Map.Entry;
import launcher.LauncherAPI;
import launcher.helper.VerifyHelper;
import launcher.serialize.HInput;
import launcher.serialize.HOutput;

public final class BlockConfigEntry extends ConfigEntry {
   @LauncherAPI
   public BlockConfigEntry(Map map, boolean ro, int cc) {
      super(map, ro, cc);
   }

   @LauncherAPI
   public BlockConfigEntry(int cc) {
      super(Collections.emptyMap(), false, cc);
   }

   @LauncherAPI
   public BlockConfigEntry(HInput input, boolean ro) throws IOException {
      super(readMap(input, ro), ro, 0);
   }

   public ConfigEntry.Type getType() {
      return ConfigEntry.Type.BLOCK;
   }

   public Map getValue() {
      Map value = (Map)super.getValue();
      return this.ro ? value : Collections.unmodifiableMap(value);
   }

   public void write(HOutput output) throws IOException {
      Set<Entry> entries = this.getValue().entrySet();
      output.writeLength(entries.size(), 0);

      for(Entry mapEntry : entries) {
         output.writeString((String)mapEntry.getKey(), 255);
         writeEntry((ConfigEntry)mapEntry.getValue(), output);
      }

   }

   protected void uncheckedSetValue(Map value) {
      Map newValue = new LinkedHashMap(value);
      newValue.keySet().stream().forEach((name) -> VerifyHelper.verifyIDName((String) name));
      super.uncheckedSetValue(this.ro ? Collections.unmodifiableMap(newValue) : newValue);
   }

   @LauncherAPI
   public void clear() {
      ((Map)super.getValue()).clear();
   }

   @LauncherAPI
   public ConfigEntry getEntry(String name, Class clazz) {
      Map map = (Map)super.getValue();
      ConfigEntry value = (ConfigEntry)map.get(name);
      if (!clazz.isInstance(value)) {
         throw new NoSuchElementException(name);
      } else {
         return (ConfigEntry)clazz.cast(value);
      }
   }

   @LauncherAPI
   public Object getEntryValue(String name, Class clazz) {
      return this.getEntry(name, clazz).getValue();
   }

   @LauncherAPI
   public boolean hasEntry(String name) {
      return this.getValue().containsKey(name);
   }

   @LauncherAPI
   public void remove(String name) {
      ((Map)super.getValue()).remove(name);
   }

   @LauncherAPI
   public void setEntry(String name, ConfigEntry entry) {
      ((Map)super.getValue()).put(VerifyHelper.verifyIDName(name), entry);
   }

   private static Map readMap(HInput input, boolean ro) throws IOException {
      int entriesCount = input.readLength(0);
      Map map = new LinkedHashMap(entriesCount);

      for(int i = 0; i < entriesCount; ++i) {
         String name = VerifyHelper.verifyIDName(input.readString(255));
         ConfigEntry entry = readEntry(input, ro);
         VerifyHelper.putIfAbsent(map, name, entry, String.format("Duplicate config entry: '%s'", name));
      }

      return map;
   }
}
