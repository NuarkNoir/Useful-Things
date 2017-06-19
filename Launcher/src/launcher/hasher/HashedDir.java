package launcher.hasher;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.ToLongFunction;
import launcher.LauncherAPI;
import launcher.helper.IOHelper;
import launcher.helper.VerifyHelper;
import launcher.serialize.HInput;
import launcher.serialize.HOutput;
import launcher.serialize.stream.EnumSerializer;

public final class HashedDir extends HashedEntry {
   private final Map map = new HashMap(32);

   @LauncherAPI
   public HashedDir() {
   }

   @LauncherAPI
   public HashedDir(Path dir, FileNameMatcher matcher, boolean allowSymlinks) throws IOException {
      IOHelper.walk(dir, new HashedDir.HashFileVisitor(dir, matcher, allowSymlinks), true);
   }

   @LauncherAPI
   public HashedDir(HInput input) throws IOException {
      int entriesCount = input.readLength(0);

      for(int i = 0; i < entriesCount; ++i) {
         String name = IOHelper.verifyFileName(input.readString(255));
         HashedEntry.Type type = HashedEntry.Type.read(input);
         HashedEntry entry;
         switch(type) {
         case FILE:
            entry = new HashedFile(input);
            break;
         case DIR:
            entry = new HashedDir(input);
            break;
         default:
            throw new AssertionError("Unsupported hashed entry type: " + type.name());
         }

         VerifyHelper.putIfAbsent(this.map, name, entry, String.format("Duplicate dir entry: '%s'", name));
      }

   }

   public HashedEntry.Type getType() {
      return HashedEntry.Type.DIR;
   }

   public long size() {
      return this.map.values().stream().mapToLong((hashedEntry) -> ((HashedEntry)hashedEntry).size()).sum();
   }

   public void write(HOutput output) throws IOException {
      Set<Entry> entries = this.map.entrySet();
      output.writeLength(entries.size(), 0);

      for(Entry mapEntry : entries) {
         output.writeString((String)mapEntry.getKey(), 255);
         HashedEntry entry = (HashedEntry)mapEntry.getValue();
         EnumSerializer.write(output, entry.getType());
         entry.write(output);
      }

   }

   @LauncherAPI
   public HashedDir.Diff diff(HashedDir other, FileNameMatcher matcher) {
      HashedDir mismatch = this.sideDiff(other, matcher, new LinkedList(), true);
      HashedDir extra = other.sideDiff(this, matcher, new LinkedList(), false);
      return new HashedDir.Diff(mismatch, extra);
   }

   @LauncherAPI
   public HashedEntry getEntry(String name) {
      return (HashedEntry)this.map.get(name);
   }

   @LauncherAPI
   public boolean isEmpty() {
      return this.map.isEmpty();
   }

   @LauncherAPI
   public Map map() {
      return Collections.unmodifiableMap(this.map);
   }

   @LauncherAPI
   public HashedEntry resolve(Iterable path) {
      HashedEntry current = this;

      for(Object pathEntry : path) {
         if (!(current instanceof HashedDir)) {
            return null;
         }

         current = (HashedEntry)((HashedDir)current).map.get(pathEntry.toString());
      }

      return current;
   }

   private HashedDir sideDiff(HashedDir other, FileNameMatcher matcher, Deque path, boolean mismatchList) {
      HashedDir diff = new HashedDir();

      for(Entry mapEntry : ((Map<String, HashedEntry>)this.map).entrySet()) {
         String name = (String)mapEntry.getKey();
         HashedEntry entry = (HashedEntry)mapEntry.getValue();
         path.add(name);
         boolean shouldUpdate = matcher == null || matcher.shouldUpdate(path);
         HashedEntry.Type type = entry.getType();
         HashedEntry otherEntry = (HashedEntry)other.map.get(name);
         if (otherEntry != null && otherEntry.getType() == type) {
            switch(type) {
            case FILE:
               HashedFile file = (HashedFile)entry;
               HashedFile otherFile = (HashedFile)otherEntry;
               if (mismatchList && shouldUpdate && !file.isSame(otherFile)) {
                  diff.map.put(name, entry);
               }
               break;
            case DIR:
               HashedDir dir = (HashedDir)entry;
               HashedDir otherDir = (HashedDir)otherEntry;
               if (mismatchList || shouldUpdate) {
                  HashedDir mismatch = dir.sideDiff(otherDir, matcher, path, mismatchList);
                  if (!mismatch.isEmpty()) {
                     diff.map.put(name, mismatch);
                  }
               }
               break;
            default:
               throw new AssertionError("Unsupported hashed entry type: " + type.name());
            }

            path.removeLast();
         } else {
            if (shouldUpdate || mismatchList && otherEntry == null) {
               diff.map.put(name, entry);
               if (!mismatchList) {
                  entry.flag = true;
               }
            }

            path.removeLast();
         }
      }

      return diff;
   }

   public static final class Diff {
      @LauncherAPI
      public final HashedDir mismatch;
      @LauncherAPI
      public final HashedDir extra;

      private Diff(HashedDir mismatch, HashedDir extra) {
         this.mismatch = mismatch;
         this.extra = extra;
      }

      @LauncherAPI
      public boolean isSame() {
         return this.mismatch.isEmpty() && this.extra.isEmpty();
      }
   }

   private final class HashFileVisitor extends SimpleFileVisitor {
      private final Path dir;
      private final FileNameMatcher matcher;
      private final boolean allowSymlinks;
      private HashedDir current;
      private final Deque path;
      private final Deque stack;

      private HashFileVisitor(Path dir, FileNameMatcher matcher, boolean allowSymlinks) {
         this.current = HashedDir.this;
         this.path = new LinkedList();
         this.stack = new LinkedList();
         this.dir = dir;
         this.matcher = matcher;
         this.allowSymlinks = allowSymlinks;
      }

      public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
         FileVisitResult result = super.postVisitDirectory(dir, exc);
         if (this.dir.equals(dir)) {
            return result;
         } else {
            HashedDir parent = (HashedDir)this.stack.removeLast();
            parent.map.put(this.path.removeLast(), this.current);
            this.current = parent;
            return result;
         }
      }

      public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
         FileVisitResult result = super.preVisitDirectory(dir, attrs);
         if (this.dir.equals(dir)) {
            return result;
         } else if (!this.allowSymlinks && attrs.isSymbolicLink()) {
            throw new SecurityException("Symlinks are not allowed");
         } else {
            this.stack.add(this.current);
            this.current = new HashedDir();
            this.path.add(IOHelper.getFileName(dir));
            return result;
         }
      }

      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
         if (!this.allowSymlinks && attrs.isSymbolicLink()) {
            throw new SecurityException("Symlinks are not allowed");
         } else {
            this.path.add(IOHelper.getFileName(file));
            boolean hash = this.matcher == null || this.matcher.shouldUpdate(this.path);
            this.current.map.put(this.path.removeLast(), new HashedFile(file, attrs.size(), hash));
            return super.visitFile(file, attrs);
         }
      }
   }
}
