package launcher.serialize.config.entry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import launcher.LauncherAPI;
import launcher.serialize.HInput;
import launcher.serialize.HOutput;

public final class ListConfigEntry extends ConfigEntry {
   @LauncherAPI
   public ListConfigEntry(List value, boolean ro, int cc) {
      super(value, ro, cc);
   }

   @LauncherAPI
   public ListConfigEntry(HInput input, boolean ro) throws IOException {
      super(readList(input, ro), ro, 0);
   }

   public ConfigEntry.Type getType() {
      return ConfigEntry.Type.LIST;
   }

   public void write(HOutput output) throws IOException {
      List<ConfigEntry> value = (List)this.getValue();
      output.writeLength(value.size(), 0);

      for(ConfigEntry element : value) {
         writeEntry(element, output);
      }

   }

   protected void uncheckedSetValue(List value) {
      List list = new ArrayList(value);
      super.uncheckedSetValue(this.ro ? Collections.unmodifiableList(list) : list);
   }

   @LauncherAPI
   public Stream stream(Class clazz) {
      Stream var10000 = ((List)this.getValue()).stream();
      clazz.getClass();
      return var10000.map(clazz::cast).map((configEntry) -> ((ConfigEntry)configEntry).getValue());
   }

   @LauncherAPI
   public void verifyOfType(ConfigEntry.Type type) {
      if (((List)this.getValue()).stream().anyMatch((e) -> {
         return e.getClass().getTypeName() != type.getClass().getTypeName();
      })) {
         throw new IllegalArgumentException("List type mismatch: " + type.name());
      }
   }

   private static List readList(HInput input, boolean ro) throws IOException {
      int elementsCount = input.readLength(0);
      List list = new ArrayList(elementsCount);

      for(int i = 0; i < elementsCount; ++i) {
         list.add(readEntry(input, ro));
      }

      return list;
   }
}
