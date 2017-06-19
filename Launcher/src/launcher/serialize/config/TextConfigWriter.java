package launcher.serialize.config;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import launcher.LauncherAPI;
import launcher.serialize.config.entry.BlockConfigEntry;
import launcher.serialize.config.entry.BooleanConfigEntry;
import launcher.serialize.config.entry.ConfigEntry;
import launcher.serialize.config.entry.IntegerConfigEntry;
import launcher.serialize.config.entry.ListConfigEntry;
import launcher.serialize.config.entry.StringConfigEntry;

public final class TextConfigWriter {
   private final Writer writer;
   private final boolean comments;

   private TextConfigWriter(Writer writer, boolean comments) {
      this.writer = writer;
      this.comments = comments;
   }

   private void writeBlock(BlockConfigEntry block, boolean brackets) throws IOException {
      if (brackets) {
         this.writer.write(123);
      }

      Map<String, ConfigEntry> map = block.getValue();

      for(Entry mapEntry : map.entrySet()) {
         String name = (String)mapEntry.getKey();
         ConfigEntry entry = (ConfigEntry)mapEntry.getValue();
         this.writeComment(entry.getComment(0));
         this.writer.write(name);
         this.writeComment(entry.getComment(1));
         this.writer.write(58);
         this.writeComment(entry.getComment(2));
         this.writeEntry(entry);
         this.writeComment(entry.getComment(3));
         this.writer.write(59);
      }

      this.writeComment(block.getComment(-1));
      if (brackets) {
         this.writer.write(125);
      }

   }

   private void writeBoolean(BooleanConfigEntry entry) throws IOException {
      this.writer.write(((Boolean)entry.getValue()).toString());
   }

   private void writeComment(String comment) throws IOException {
      if (this.comments && comment != null) {
         this.writer.write(comment);
      }

   }

   private void writeEntry(ConfigEntry entry) throws IOException {
      ConfigEntry.Type type = entry.getType();
      switch(type) {
      case BLOCK:
         this.writeBlock((BlockConfigEntry)entry, true);
         break;
      case STRING:
         this.writeString((StringConfigEntry)entry);
         break;
      case INTEGER:
         this.writeInteger((IntegerConfigEntry)entry);
         break;
      case BOOLEAN:
         this.writeBoolean((BooleanConfigEntry)entry);
         break;
      case LIST:
         this.writeList((ListConfigEntry)entry);
         break;
      default:
         throw new AssertionError("Unsupported config entry type: " + type.name());
      }

   }

   private void writeInteger(IntegerConfigEntry entry) throws IOException {
      this.writer.write(Integer.toString(((Integer)entry.getValue()).intValue()));
   }

   private void writeList(ListConfigEntry entry) throws IOException {
      this.writer.write(91);
      List value = (List)entry.getValue();

      for(int i = 0; i < value.size(); ++i) {
         if (i > 0) {
            this.writer.write(44);
         }

         ConfigEntry element = (ConfigEntry)value.get(i);
         this.writeComment(element.getComment(0));
         this.writeEntry(element);
         this.writeComment(element.getComment(1));
      }

      this.writeComment(entry.getComment(-1));
      this.writer.write(93);
   }

   private void writeString(StringConfigEntry entry) throws IOException {
      this.writer.write(34);
      String s = (String)entry.getValue();

      for(int i = 0; i < s.length(); ++i) {
         char ch = s.charAt(i);
         switch(ch) {
         case '\b':
            this.writer.write("\\b");
            break;
         case '\t':
            this.writer.write("\\t");
            break;
         case '\n':
            this.writer.write("\\n");
            break;
         case '\f':
            this.writer.write("\\f");
            break;
         case '\r':
            this.writer.write("\\r");
            break;
         case '"':
         case '\\':
            this.writer.write(92);
            this.writer.write(ch);
            break;
         default:
            this.writer.write(ch);
         }
      }

      this.writer.write(34);
   }

   @LauncherAPI
   public static void write(BlockConfigEntry block, Writer writer, boolean comments) throws IOException {
      (new TextConfigWriter(writer, comments)).writeBlock(block, false);
   }
}
