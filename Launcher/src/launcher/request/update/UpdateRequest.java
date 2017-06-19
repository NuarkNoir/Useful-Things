package launcher.request.update;

import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.SignatureException;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Map.Entry;
import launcher.Launcher;
import launcher.LauncherAPI;
import launcher.hasher.FileNameMatcher;
import launcher.hasher.HashedDir;
import launcher.hasher.HashedEntry;
import launcher.hasher.HashedFile;
import launcher.helper.IOHelper;
import launcher.helper.SecurityHelper;
import launcher.request.Request;
import launcher.serialize.HInput;
import launcher.serialize.HOutput;
import launcher.serialize.signed.SignedObjectHolder;
import launcher.serialize.stream.EnumSerializer;
import launcher.serialize.stream.StreamObject;

public final class UpdateRequest extends Request {
   @LauncherAPI
   public static final int MAX_QUEUE_SIZE = 128;
   private final String dirName;
   private final Path dir;
   private final FileNameMatcher matcher;
   private volatile UpdateRequest.State.Callback stateCallback;
   private HashedDir localDir;
   private long totalDownloaded;
   private long totalSize;
   private Instant startTime;

   @LauncherAPI
   public UpdateRequest(Launcher.Config config, String dirName, Path dir, FileNameMatcher matcher) {
      super(config);
      this.dirName = IOHelper.verifyFileName(dirName);
      this.dir = (Path)Objects.requireNonNull(dir, "dir");
      this.matcher = matcher;
   }

   @LauncherAPI
   public UpdateRequest(String dirName, Path dir, FileNameMatcher matcher) {
      this((Launcher.Config)null, dirName, dir, matcher);
   }

   public Request.Type getType() {
      return Request.Type.UPDATE;
   }

   public SignedObjectHolder request() throws Exception {
      Files.createDirectories(this.dir);
      this.localDir = new HashedDir(this.dir, this.matcher, false);
      return (SignedObjectHolder)super.request();
   }

   protected SignedObjectHolder requestDo(HInput input, HOutput output) throws IOException, SignatureException {
      output.writeString(this.dirName, 255);
      output.flush();
      this.readError(input);
      SignedObjectHolder remoteHDirHolder = new SignedObjectHolder(input, this.config.publicKey, HashedDir::new);
      HashedDir.Diff diff = ((HashedDir)remoteHDirHolder.object).diff(this.localDir, this.matcher);
      this.totalSize = diff.mismatch.size();
      Queue queue = new LinkedList();
      this.fillActionsQueue(queue, diff.mismatch);
      queue.add(UpdateRequest.Action.FINISH);
      this.startTime = Instant.now();
      Path currentDir = this.dir;
      UpdateRequest.Action[] actionsSlice = new UpdateRequest.Action[128];

      while(!queue.isEmpty()) {
         int length = Math.min(queue.size(), 128);
         output.writeLength(length, 128);

         for(int i = 0; i < length; ++i) {
            UpdateRequest.Action action = (UpdateRequest.Action)queue.remove();
            actionsSlice[i] = action;
            action.write(output);
         }

         output.flush();

         for(int i = 0; i < length; ++i) {
            UpdateRequest.Action action = actionsSlice[i];
            switch(action.type) {
            case CD:
               currentDir = currentDir.resolve(action.name);
               Files.createDirectories(currentDir);
               break;
            case GET:
               this.downloadFile(currentDir.resolve(action.name), (HashedFile)action.entry, input);
               break;
            case CD_BACK:
               currentDir = currentDir.getParent();
            case FINISH:
               break;
            default:
               throw new AssertionError(String.format("Unsupported action type: '%s'", action.type.name()));
            }
         }
      }

      this.deleteExtraDir(this.dir, diff.extra, diff.extra.flag);
      return remoteHDirHolder;
   }

   @LauncherAPI
   public void setStateCallback(UpdateRequest.State.Callback callback) {
      this.stateCallback = callback;
   }

   private void deleteExtraDir(Path subDir, HashedDir subHDir, boolean flag) throws IOException {
      for(Entry mapEntry : ((Map<String, HashedEntry>)subHDir.map()).entrySet()) {
         String name = (String)mapEntry.getKey();
         Path path = subDir.resolve(name);
         HashedEntry entry = (HashedEntry)mapEntry.getValue();
         HashedEntry.Type entryType = entry.getType();
         switch(entryType) {
         case FILE:
            this.updateState(IOHelper.toString(path), 0L, 0L);
            Files.delete(path);
            break;
         case DIR:
            this.deleteExtraDir(path, (HashedDir)entry, flag || entry.flag);
            break;
         default:
            throw new AssertionError("Unsupported hashed entry type: " + entryType.name());
         }
      }

      if (flag) {
         this.updateState(IOHelper.toString(subDir), 0L, 0L);
         Files.delete(subDir);
      }

   }

   private void downloadFile(Path file, HashedFile hFile, HInput input) throws IOException {
      String filePath = IOHelper.toString(this.dir.relativize(file));
      this.updateState(filePath, 0L, hFile.size);
      MessageDigest digest = SecurityHelper.newDigest(SecurityHelper.DigestAlgorithm.MD5);
      OutputStream fileOutput = IOHelper.newOutput(file);
      Throwable var7 = null;

      try {
         long downloaded = 0L;
         byte[] bytes = IOHelper.newBuffer();

         while(downloaded < hFile.size) {
            int remaining = (int)Math.min(hFile.size - downloaded, (long)bytes.length);
            int length = input.stream.read(bytes, 0, remaining);
            if (length < 0) {
               throw new EOFException(String.format("%d bytes remaining", hFile.size - downloaded));
            }

            digest.update(bytes, 0, length);
            fileOutput.write(bytes, 0, length);
            downloaded += (long)length;
            this.totalDownloaded += (long)length;
            this.updateState(filePath, downloaded, hFile.size);
         }
      } catch (Throwable var20) {
         var7 = var20;
         throw var20;
      } finally {
         if (fileOutput != null) {
            if (var7 != null) {
               try {
                  fileOutput.close();
               } catch (Throwable var19) {
                  var7.addSuppressed(var19);
               }
            } else {
               fileOutput.close();
            }
         }

      }

      byte[] digestBytes = digest.digest();
      if (!hFile.isSameDigest(digestBytes)) {
         throw new SecurityException(String.format("File digest mismatch: '%s'", filePath));
      }
   }

   private void fillActionsQueue(Queue queue, HashedDir mismatch) {
      for(Entry mapEntry : ((Map<String, HashedEntry>)mismatch.map()).entrySet()) {
         String name = (String)mapEntry.getKey();
         HashedEntry entry = (HashedEntry)mapEntry.getValue();
         HashedEntry.Type entryType = entry.getType();
         switch(entryType) {
         case FILE:
            queue.add(new UpdateRequest.Action(UpdateRequest.Action.Type.GET, name, entry));
            break;
         case DIR:
            queue.add(new UpdateRequest.Action(UpdateRequest.Action.Type.CD, name, entry));
            this.fillActionsQueue(queue, (HashedDir)entry);
            queue.add(UpdateRequest.Action.CD_BACK);
            break;
         default:
            throw new AssertionError("Unsupported hashed entry type: " + entryType.name());
         }
      }

   }

   private void updateState(String filePath, long fileDownloaded, long fileSize) {
      if (this.stateCallback != null) {
         this.stateCallback.call(new UpdateRequest.State(filePath, fileDownloaded, fileSize, this.totalDownloaded, this.totalSize, Duration.between(this.startTime, Instant.now())));
      }

   }

   public static final class Action extends StreamObject {
      public static final UpdateRequest.Action CD_BACK = new UpdateRequest.Action(UpdateRequest.Action.Type.CD_BACK, (String)null, (HashedEntry)null);
      public static final UpdateRequest.Action FINISH = new UpdateRequest.Action(UpdateRequest.Action.Type.FINISH, (String)null, (HashedEntry)null);
      public final UpdateRequest.Action.Type type;
      public final String name;
      public final HashedEntry entry;

      public Action(UpdateRequest.Action.Type type, String name, HashedEntry entry) {
         this.type = type;
         this.name = name;
         this.entry = entry;
      }

      public Action(HInput input) throws IOException {
         this.type = UpdateRequest.Action.Type.read(input);
         this.name = this.type != UpdateRequest.Action.Type.CD && this.type != UpdateRequest.Action.Type.GET ? null : IOHelper.verifyFileName(input.readString(255));
         this.entry = null;
      }

      public void write(HOutput output) throws IOException {
         EnumSerializer.write(output, this.type);
         if (this.type == UpdateRequest.Action.Type.CD || this.type == UpdateRequest.Action.Type.GET) {
            output.writeString(this.name, 255);
         }

      }

      public static enum Type implements EnumSerializer.Itf {
         CD(1),
         CD_BACK(2),
         GET(3),
         FINISH(255);

         private static final EnumSerializer SERIALIZER = new EnumSerializer(UpdateRequest.Action.Type.class);
         private final int n;

         private Type(int n) {
            this.n = n;
         }

         public int getNumber() {
            return this.n;
         }

         public static UpdateRequest.Action.Type read(HInput input) throws IOException {
            return (UpdateRequest.Action.Type)SERIALIZER.read(input);
         }
      }
   }

   public static final class State {
      @LauncherAPI
      public final long fileDownloaded;
      @LauncherAPI
      public final long fileSize;
      @LauncherAPI
      public final long totalDownloaded;
      @LauncherAPI
      public final long totalSize;
      @LauncherAPI
      public final String filePath;
      @LauncherAPI
      public final Duration duration;

      public State(String filePath, long fileDownloaded, long fileSize, long totalDownloaded, long totalSize, Duration duration) {
         this.filePath = filePath;
         this.fileDownloaded = fileDownloaded;
         this.fileSize = fileSize;
         this.totalDownloaded = totalDownloaded;
         this.totalSize = totalSize;
         this.duration = duration;
      }

      @LauncherAPI
      public double getBps() {
         long seconds = this.duration.getSeconds();
         return seconds == 0L ? -1.0D : (double)this.totalDownloaded / (double)seconds;
      }

      @LauncherAPI
      public Duration getEstimatedTime() {
         double bps = this.getBps();
         return bps <= 0.0D ? null : Duration.ofSeconds((long)((double)this.getTotalRemaining() / bps));
      }

      @LauncherAPI
      public double getFileDownloadedKiB() {
         return (double)this.fileDownloaded / 1024.0D;
      }

      @LauncherAPI
      public double getFileDownloadedMiB() {
         return this.getFileDownloadedKiB() / 1024.0D;
      }

      @LauncherAPI
      public double getFileDownloadedPart() {
         return this.fileSize == 0L ? 0.0D : (double)this.fileDownloaded / (double)this.fileSize;
      }

      @LauncherAPI
      public long getFileRemaining() {
         return this.fileSize - this.fileDownloaded;
      }

      @LauncherAPI
      public double getFileRemainingKiB() {
         return (double)this.getFileRemaining() / 1024.0D;
      }

      @LauncherAPI
      public double getFileRemainingMiB() {
         return this.getFileRemainingKiB() / 1024.0D;
      }

      @LauncherAPI
      public double getFileSizeKiB() {
         return (double)this.fileSize / 1024.0D;
      }

      @LauncherAPI
      public double getFileSizeMiB() {
         return this.getFileSizeKiB() / 1024.0D;
      }

      @LauncherAPI
      public double getTotalDownloadedKiB() {
         return (double)this.totalDownloaded / 1024.0D;
      }

      @LauncherAPI
      public double getTotalDownloadedMiB() {
         return this.getTotalDownloadedKiB() / 1024.0D;
      }

      @LauncherAPI
      public double getTotalDownloadedPart() {
         return this.totalSize == 0L ? 0.0D : (double)this.totalDownloaded / (double)this.totalSize;
      }

      @LauncherAPI
      public long getTotalRemaining() {
         return this.totalSize - this.totalDownloaded;
      }

      @LauncherAPI
      public double getTotalRemainingKiB() {
         return (double)this.getTotalRemaining() / 1024.0D;
      }

      @LauncherAPI
      public double getTotalRemainingMiB() {
         return this.getTotalRemainingKiB() / 1024.0D;
      }

      @LauncherAPI
      public double getTotalSizeKiB() {
         return (double)this.totalSize / 1024.0D;
      }

      @LauncherAPI
      public double getTotalSizeMiB() {
         return this.getTotalSizeKiB() / 1024.0D;
      }

      @FunctionalInterface
      public interface Callback {
         void call(UpdateRequest.State var1);
      }
   }
}
