package launcher.serialize;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Objects;
import java.util.UUID;
import launcher.LauncherAPI;
import launcher.helper.IOHelper;

public final class HInput implements AutoCloseable {
   @LauncherAPI
   public final InputStream stream;

   @LauncherAPI
   public HInput(InputStream stream) {
      this.stream = (InputStream)Objects.requireNonNull(stream, "stream");
   }

   @LauncherAPI
   public HInput(byte[] bytes) {
      this.stream = new ByteArrayInputStream(bytes);
   }

   public void close() throws IOException {
      this.stream.close();
   }

   @LauncherAPI
   public String readASCII(int maxBytes) throws IOException {
      return IOHelper.decodeASCII(this.readByteArray(maxBytes));
   }

   @LauncherAPI
   public BigInteger readBigInteger(int maxBytes) throws IOException {
      return new BigInteger(this.readByteArray(maxBytes));
   }

   @LauncherAPI
   public boolean readBoolean() throws IOException {
      int b = this.readUnsignedByte();
      switch(b) {
      case 0:
         return false;
      case 1:
         return true;
      default:
         throw new IOException("Invalid boolean state: " + b);
      }
   }

   @LauncherAPI
   public byte[] readByteArray(int max) throws IOException {
      byte[] bytes = new byte[this.readLength(max)];
      IOHelper.read(this.stream, bytes);
      return bytes;
   }

   @LauncherAPI
   public int readInt() throws IOException {
      return (this.readUnsignedByte() << 24) + (this.readUnsignedByte() << 16) + (this.readUnsignedByte() << 8) + this.readUnsignedByte();
   }

   @LauncherAPI
   public int readLength(int max) throws IOException {
      return max < 0 ? -max : IOHelper.verifyLength(this.readVarInt(), max);
   }

   @LauncherAPI
   public long readLong() throws IOException {
      return (long)this.readInt() << 32 | (long)this.readInt() & 4294967295L;
   }

   @LauncherAPI
   public short readShort() throws IOException {
      return (short)((this.readUnsignedByte() << 8) + this.readUnsignedByte());
   }

   @LauncherAPI
   public String readString(int maxBytes) throws IOException {
      return IOHelper.decode(this.readByteArray(maxBytes));
   }

   @LauncherAPI
   public UUID readUUID() throws IOException {
      return new UUID(this.readLong(), this.readLong());
   }

   @LauncherAPI
   public int readUnsignedByte() throws IOException {
      int b = this.stream.read();
      if (b < 0) {
         throw new EOFException("readUnsignedByte");
      } else {
         return b;
      }
   }

   @LauncherAPI
   public int readUnsignedShort() throws IOException {
      return Short.toUnsignedInt(this.readShort());
   }

   @LauncherAPI
   public int readVarInt() throws IOException {
      int shift = 0;

      for(int result = 0; shift < 32; shift += 7) {
         int b = this.readUnsignedByte();
         result |= (b & 127) << shift;
         if ((b & 128) == 0) {
            return result;
         }
      }

      throw new IOException("VarInt too big");
   }

   @LauncherAPI
   public long readVarLong() throws IOException {
      int shift = 0;

      for(long result = 0L; shift < 64; shift += 7) {
         int b = this.readUnsignedByte();
         result |= (long)(b & 127) << shift;
         if ((b & 128) == 0) {
            return result;
         }
      }

      throw new IOException("VarLong too big");
   }
}
