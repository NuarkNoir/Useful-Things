package launcher.serialize;

import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.Objects;
import java.util.UUID;
import launcher.LauncherAPI;
import launcher.helper.IOHelper;

public final class HOutput implements AutoCloseable, Flushable {
   @LauncherAPI
   public final OutputStream stream;

   @LauncherAPI
   public HOutput(OutputStream stream) {
      this.stream = (OutputStream)Objects.requireNonNull(stream, "stream");
   }

   public void close() throws IOException {
      this.stream.close();
   }

   public void flush() throws IOException {
      this.stream.flush();
   }

   @LauncherAPI
   public void writeASCII(String s, int maxBytes) throws IOException {
      this.writeByteArray(IOHelper.encodeASCII(s), maxBytes);
   }

   @LauncherAPI
   public void writeBigInteger(BigInteger bi, int max) throws IOException {
      this.writeByteArray(bi.toByteArray(), max);
   }

   @LauncherAPI
   public void writeBoolean(boolean b) throws IOException {
      this.writeUnsignedByte(b ? 1 : 0);
   }

   @LauncherAPI
   public void writeByteArray(byte[] bytes, int max) throws IOException {
      this.writeLength(bytes.length, max);
      this.stream.write(bytes);
   }

   @LauncherAPI
   public void writeInt(int i) throws IOException {
      this.writeUnsignedByte(i >>> 24 & 255);
      this.writeUnsignedByte(i >>> 16 & 255);
      this.writeUnsignedByte(i >>> 8 & 255);
      this.writeUnsignedByte(i & 255);
   }

   @LauncherAPI
   public void writeLength(int length, int max) throws IOException {
      IOHelper.verifyLength(length, max);
      if (max >= 0) {
         this.writeVarInt(length);
      }

   }

   @LauncherAPI
   public void writeLong(long l) throws IOException {
      this.writeInt((int)(l >> 32));
      this.writeInt((int)l);
   }

   @LauncherAPI
   public void writeShort(short s) throws IOException {
      this.writeUnsignedByte(s >>> 8 & 255);
      this.writeUnsignedByte(s & 255);
   }

   @LauncherAPI
   public void writeString(String s, int maxBytes) throws IOException {
      this.writeByteArray(IOHelper.encode(s), maxBytes);
   }

   @LauncherAPI
   public void writeUUID(UUID uuid) throws IOException {
      this.writeLong(uuid.getMostSignificantBits());
      this.writeLong(uuid.getLeastSignificantBits());
   }

   @LauncherAPI
   public void writeUnsignedByte(int b) throws IOException {
      this.stream.write(b);
   }

   @LauncherAPI
   public void writeVarInt(int i) throws IOException {
      while(((long)i & -128L) != 0L) {
         this.writeUnsignedByte(i & 127 | 128);
         i >>>= 7;
      }

      this.writeUnsignedByte(i);
   }

   @LauncherAPI
   public void writeVarLong(long l) throws IOException {
      while((l & -128L) != 0L) {
         this.writeUnsignedByte((int)l & 127 | 128);
         l >>>= 7;
      }

      this.writeUnsignedByte((int)l);
   }
}
