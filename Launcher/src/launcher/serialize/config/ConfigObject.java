package launcher.serialize.config;

import java.io.IOException;
import java.util.Objects;
import launcher.LauncherAPI;
import launcher.serialize.HOutput;
import launcher.serialize.config.entry.BlockConfigEntry;
import launcher.serialize.stream.StreamObject;

public abstract class ConfigObject extends StreamObject {
   @LauncherAPI
   public final BlockConfigEntry block;

   @LauncherAPI
   protected ConfigObject(BlockConfigEntry block) {
      this.block = (BlockConfigEntry)Objects.requireNonNull(block, "block");
   }

   public final void write(HOutput output) throws IOException {
      this.block.write(output);
   }

   @FunctionalInterface
   public interface Adapter {
      @LauncherAPI
      ConfigObject convert(BlockConfigEntry var1);
   }
}
