package launcher.hasher;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import launcher.LauncherAPI;
import launcher.helper.IOHelper;
import launcher.helper.SecurityHelper;
import launcher.helper.VerifyHelper;
import launcher.serialize.HInput;
import launcher.serialize.HOutput;

public final class HashedFile extends HashedEntry {
   private static final byte[] DUMMY_HASH = new byte[0];
   @LauncherAPI
   public final long size;
   private final byte[] digest;

   @LauncherAPI
   public HashedFile(long size, byte[] digest) {
      this.size = VerifyHelper.verifyLong(size, VerifyHelper.L_NOT_NEGATIVE, "Illegal size: " + size);
      this.digest = Arrays.copyOf(digest, digest.length);
   }

   @LauncherAPI
   public HashedFile(Path file, long size, boolean hash) throws IOException {
      this(size, hash ? SecurityHelper.digest(SecurityHelper.DigestAlgorithm.MD5, file) : DUMMY_HASH);
   }

   @LauncherAPI
   public HashedFile(HInput input) throws IOException {
      this(input.readVarLong(), input.readByteArray(2048));
   }

   public HashedEntry.Type getType() {
      return HashedEntry.Type.FILE;
   }

   public long size() {
      return this.size;
   }

   public void write(HOutput output) throws IOException {
      output.writeVarLong(this.size);
      output.writeByteArray(this.digest, 2048);
   }

   @LauncherAPI
   public boolean isSame(HashedFile o) {
      return this.size == o.size && Arrays.equals(this.digest, o.digest);
   }

   @LauncherAPI
   public boolean isSame(Path file) throws IOException {
      return this.isSame(new HashedFile(file, IOHelper.readAttributes(file).size(), true));
   }

   @LauncherAPI
   public boolean isSameDigest(byte[] digest) {
      return Arrays.equals(this.digest, digest);
   }
}
