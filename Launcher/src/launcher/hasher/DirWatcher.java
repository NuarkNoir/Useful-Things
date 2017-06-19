package launcher.hasher;

import com.sun.nio.file.ExtendedWatchEventModifier;
import com.sun.nio.file.SensitivityWatchEventModifier;
import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;
import launcher.LauncherAPI;
import launcher.helper.IOHelper;
import launcher.helper.JVMHelper;
import launcher.helper.LogHelper;

public final class DirWatcher implements Runnable, AutoCloseable {
   private static final boolean FILE_TREE_SUPPORTED = JVMHelper.OS_TYPE == JVMHelper.OS.MUSTDIE;
   private static final Modifier[] MODIFIERS = new Modifier[]{SensitivityWatchEventModifier.HIGH};
   private static final Modifier[] FILE_TREE_MODIFIERS = new Modifier[]{ExtendedWatchEventModifier.FILE_TREE, SensitivityWatchEventModifier.HIGH};
   private static final Kind[] KINDS = new Kind[]{StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE};
   private final Path dir;
   private final HashedDir hdir;
   private final FileNameMatcher matcher;
   private final WatchService service;

   @LauncherAPI
   public DirWatcher(Path dir, HashedDir hdir, FileNameMatcher matcher) throws IOException {
      this.dir = (Path)Objects.requireNonNull(dir, "dir");
      this.hdir = (HashedDir)Objects.requireNonNull(hdir, "hdir");
      this.matcher = matcher;
      this.service = dir.getFileSystem().newWatchService();
      if (FILE_TREE_SUPPORTED) {
         dir.register(this.service, KINDS, FILE_TREE_MODIFIERS);
      } else {
         IOHelper.walk(dir, new DirWatcher.RegisterFileVisitor(), true);
      }
   }

   @LauncherAPI
   public void close() throws IOException {
      this.service.close();
   }

   @LauncherAPI
   public void run() {
      try {
         this.processLoop();
      } catch (ClosedWatchServiceException | InterruptedException var2) {
         ;
      } catch (Throwable var3) {
         handleError(var3);
      }

   }

   private void processKey(WatchKey key) throws IOException {
      Path watchDir = (Path)key.watchable();
      Collection events = key.pollEvents();
      Iterator var4 = events.iterator();

      WatchEvent event;
      Kind kind;
      Path path;
      while(true) {
         if (!var4.hasNext()) {
            key.reset();
            return;
         }

         event = (WatchEvent)var4.next();
         kind = event.kind();
         if (kind.equals(StandardWatchEventKinds.OVERFLOW)) {
            throw new IOException("Overflow");
         }

         path = watchDir.resolve((Path)event.context());
         Deque stringPath = toPath(this.dir.relativize(path));
         if (this.matcher == null || this.matcher.shouldVerify(stringPath)) {
            if (!kind.equals(StandardWatchEventKinds.ENTRY_MODIFY)) {
               break;
            }

            HashedEntry entry = this.hdir.resolve(stringPath);
            if (entry == null || entry.getType() == HashedEntry.Type.FILE && !((HashedFile)entry).isSame(path)) {
               break;
            }
         }
      }

      throw new SecurityException(String.format("Forbidden modification (%s, %d times): '%s'", kind, event.count(), path));
   }

   private void processLoop() throws IOException, InterruptedException {
      while(!Thread.interrupted()) {
         this.processKey(this.service.take());
      }

   }

   private static void handleError(Throwable e) {
      LogHelper.error(e);
      JVMHelper.halt0(195952353);
   }

   private static Deque toPath(Iterable path) {
      Deque result = new LinkedList();

      for(Object pe : path) {
         result.add(pe.toString());
      }

      return result;
   }

   private final class RegisterFileVisitor extends SimpleFileVisitor {
      private final Deque path;

      private RegisterFileVisitor() {
         this.path = new LinkedList();
      }

      public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
         FileVisitResult result = super.postVisitDirectory(dir, exc);
         if (!DirWatcher.this.dir.equals(dir)) {
            this.path.removeLast();
         }

         return result;
      }

      public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
         FileVisitResult result = super.preVisitDirectory(dir, attrs);
         if (DirWatcher.this.dir.equals(dir)) {
            dir.register(DirWatcher.this.service, DirWatcher.KINDS, DirWatcher.MODIFIERS);
            return result;
         } else {
            this.path.add(IOHelper.getFileName(dir));
            if (DirWatcher.this.matcher != null && !DirWatcher.this.matcher.shouldVerify(this.path)) {
               return FileVisitResult.SKIP_SUBTREE;
            } else {
               dir.register(DirWatcher.this.service, DirWatcher.KINDS, DirWatcher.MODIFIERS);
               return result;
            }
         }
      }
   }
}
