package launcher.serialize.config;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import launcher.LauncherAPI;
import launcher.helper.VerifyHelper;
import launcher.serialize.config.entry.BlockConfigEntry;
import launcher.serialize.config.entry.BooleanConfigEntry;
import launcher.serialize.config.entry.ConfigEntry;
import launcher.serialize.config.entry.IntegerConfigEntry;
import launcher.serialize.config.entry.ListConfigEntry;
import launcher.serialize.config.entry.StringConfigEntry;

public final class TextConfigReader {
   private final LineNumberReader reader;
   private final boolean ro;
   private String skipped;
   private int ch = -1;

   private TextConfigReader(Reader reader, boolean ro) {
      this.reader = new LineNumberReader(reader);
      this.reader.setLineNumber(1);
      this.ro = ro;
   }

   private IOException newIOException(String message) {
      return new IOException(message + " (line " + this.reader.getLineNumber() + ')');
   }

   private int nextChar(boolean eof) throws IOException {
      this.ch = this.reader.read();
      if (eof && this.ch < 0) {
         throw this.newIOException("Unexpected end of config");
      } else {
         return this.ch;
      }
   }

   private int nextClean(boolean eof) throws IOException {
      this.nextChar(eof);
      return this.skipWhitespace(eof);
   }

   private BlockConfigEntry readBlock(int cc) throws IOException {
      Map map = new LinkedHashMap(16);
      boolean brackets = this.ch == 123;

      while(this.nextClean(brackets) >= 0 && (!brackets || this.ch != 125)) {
         String preNameComment = this.skipped;
         String name = this.readToken();
         if (this.skipWhitespace(true) != 58) {
            throw this.newIOException("Value start expected");
         }

         String postNameComment = this.skipped;
         this.nextClean(true);
         String preValueComment = this.skipped;
         ConfigEntry entry = this.readEntry(4);
         if (this.skipWhitespace(true) != 59) {
            throw this.newIOException("Value end expected");
         }

         entry.setComment(0, preNameComment);
         entry.setComment(1, postNameComment);
         entry.setComment(2, preValueComment);
         entry.setComment(3, this.skipped);
         if (map.put(name, entry) != null) {
            throw this.newIOException(String.format("Duplicate config entry: '%s'", name));
         }
      }

      BlockConfigEntry block = new BlockConfigEntry(map, this.ro, cc + 1);
      block.setComment(cc, this.skipped);
      this.nextChar(false);
      return block;
   }

   private ConfigEntry readEntry(int cc) throws IOException {
      switch(this.ch) {
      case 34:
         return this.readString(cc);
      case 91:
         return this.readList(cc);
      case 123:
         return this.readBlock(cc);
      default:
         if (this.ch != 45 && (this.ch < 48 || this.ch > 57)) {
            String statement = this.readToken();
            byte var4 = -1;
            switch(statement.hashCode()) {
            case 3569038:
               if (statement.equals("true")) {
                  var4 = 0;
               }
               break;
            case 97196323:
               if (statement.equals("false")) {
                  var4 = 1;
               }
            }

            switch(var4) {
            case 0:
               return new BooleanConfigEntry(Boolean.TRUE.booleanValue(), this.ro, cc);
            case 1:
               return new BooleanConfigEntry(Boolean.FALSE.booleanValue(), this.ro, cc);
            default:
               throw this.newIOException(String.format("Unknown statement: '%s'", statement));
            }
         } else {
            return this.readInteger(cc);
         }
      }
   }

   private ConfigEntry readInteger(int cc) throws IOException {
      return new IntegerConfigEntry(Integer.parseInt(this.readToken()), this.ro, cc);
   }

   private ConfigEntry readList(int cc) throws IOException {
      List listValue = new ArrayList(16);
      boolean hasNextElement = this.nextClean(true) != 93;
      String preValueComment = this.skipped;

      while(hasNextElement) {
         ConfigEntry element = this.readEntry(2);
         hasNextElement = this.skipWhitespace(true) != 93;
         element.setComment(0, preValueComment);
         element.setComment(1, this.skipped);
         listValue.add(element);
         if (hasNextElement) {
            if (this.ch != 44) {
               throw this.newIOException("Comma expected");
            }

            this.nextClean(true);
            preValueComment = this.skipped;
         }
      }

      boolean additional = listValue.isEmpty();
      ConfigEntry list = new ListConfigEntry(listValue, this.ro, additional ? cc + 1 : cc);
      if (additional) {
         list.setComment(cc, this.skipped);
      }

      this.nextChar(false);
      return list;
   }

   private ConfigEntry readString(int cc) throws IOException {
      StringBuilder builder = new StringBuilder();

      while(this.nextChar(true) != 34) {
         switch(this.ch) {
         case 10:
         case 13:
            throw this.newIOException("String termination");
         case 92:
            int next = this.nextChar(true);
            switch(next) {
            case 34:
            case 92:
               builder.append((char)next);
               continue;
            case 98:
               builder.append('\b');
               continue;
            case 102:
               builder.append('\f');
               continue;
            case 110:
               builder.append('\n');
               continue;
            case 114:
               builder.append('\r');
               continue;
            case 116:
               builder.append('\t');
               continue;
            default:
               throw this.newIOException("Illegal char escape: " + (char)next);
            }
         default:
            builder.append((char)this.ch);
         }
      }

      this.nextChar(false);
      return new StringConfigEntry(builder.toString(), this.ro, cc);
   }

   private String readToken() throws IOException {
      StringBuilder builder = new StringBuilder();

      while(VerifyHelper.isValidIDNameChar(this.ch)) {
         builder.append((char)this.ch);
         this.nextChar(false);
      }

      String token = builder.toString();
      if (token.isEmpty()) {
         throw this.newIOException("Not a token");
      } else {
         return token;
      }
   }

   private void skipComment(StringBuilder skippedBuilder, boolean eof) throws IOException {
      while(this.ch >= 0 && this.ch != 13 && this.ch != 10) {
         skippedBuilder.append((char)this.ch);
         this.nextChar(eof);
      }

   }

   private int skipWhitespace(boolean eof) throws IOException {
      StringBuilder skippedBuilder = new StringBuilder();

      while(Character.isWhitespace(this.ch) || this.ch == 35) {
         if (this.ch == 35) {
            this.skipComment(skippedBuilder, eof);
         } else {
            skippedBuilder.append((char)this.ch);
            this.nextChar(eof);
         }
      }

      this.skipped = skippedBuilder.toString();
      return this.ch;
   }

   @LauncherAPI
   public static BlockConfigEntry read(Reader reader, boolean ro) throws IOException {
      return (new TextConfigReader(reader, ro)).readBlock(0);
   }
}
