package launcher.hasher;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import launcher.LauncherAPI;

public final class FileNameMatcher {
   private static final FileNameMatcher.Entry[] NO_ENTRIES = new FileNameMatcher.Entry[0];
   private final FileNameMatcher.Entry[] update;
   private final FileNameMatcher.Entry[] verify;
   private final FileNameMatcher.Entry[] exclusions;

   @LauncherAPI
   public FileNameMatcher(String[] update, String[] verify, String[] exclusions) {
      this.update = toEntries(update);
      this.verify = toEntries(verify);
      this.exclusions = toEntries(exclusions);
   }

   private FileNameMatcher(FileNameMatcher.Entry[] update, FileNameMatcher.Entry[] verify, FileNameMatcher.Entry[] exclusions) {
      this.update = update;
      this.verify = verify;
      this.exclusions = exclusions;
   }

   @LauncherAPI
   public boolean shouldUpdate(Collection path) {
      return (anyMatch(this.update, path) || anyMatch(this.verify, path)) && !anyMatch(this.exclusions, path);
   }

   @LauncherAPI
   public boolean shouldVerify(Collection path) {
      return anyMatch(this.verify, path) && !anyMatch(this.exclusions, path);
   }

   @LauncherAPI
   public FileNameMatcher verifyOnly() {
      return new FileNameMatcher(NO_ENTRIES, this.verify, this.exclusions);
   }

   private static boolean anyMatch(FileNameMatcher.Entry[] entries, Collection path) {
      return Arrays.stream(entries).anyMatch((e) -> {
         return e.matches(path);
      });
   }

   private static FileNameMatcher.Entry[] toEntries(String... entries) {
      return (FileNameMatcher.Entry[])Arrays.stream(entries).map((x$0) -> {
         return new FileNameMatcher.Entry(x$0);
      }).toArray((x$0) -> {
         return new FileNameMatcher.Entry[x$0];
      });
   }

   private static final class Entry {
      private static final Pattern SPLITTER = Pattern.compile(Pattern.quote("/") + '+');
      private final Pattern[] parts;

      private Entry(CharSequence exclusion) {
         this.parts = (Pattern[])SPLITTER.splitAsStream(exclusion).map(Pattern::compile).toArray((x$0) -> {
            return new Pattern[x$0];
         });
      }

      private boolean matches(Collection path) {
         if (this.parts.length > path.size()) {
            return false;
         } else {
            Iterator iterator = path.iterator();

            for(Pattern patternPart : this.parts) {
               String pathPart = (String)iterator.next();
               if (!patternPart.matcher(pathPart).matches()) {
                  return false;
               }
            }

            return true;
         }
      }
   }
}
