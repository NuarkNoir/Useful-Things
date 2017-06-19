package launcher.helper;

import java.util.Map;
import java.util.function.DoublePredicate;
import java.util.function.IntPredicate;
import java.util.function.LongPredicate;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import launcher.LauncherAPI;

public final class VerifyHelper {
   @LauncherAPI
   public static final IntPredicate POSITIVE = (i) -> {
      return i > 0;
   };
   @LauncherAPI
   public static final IntPredicate NOT_NEGATIVE = (i) -> {
      return i >= 0;
   };
   @LauncherAPI
   public static final LongPredicate L_POSITIVE = (l) -> {
      return l > 0L;
   };
   @LauncherAPI
   public static final LongPredicate L_NOT_NEGATIVE = (l) -> {
      return l >= 0L;
   };
   @LauncherAPI
   public static final Predicate NOT_EMPTY = (s) -> {
      return s != null;
   };
   @LauncherAPI
   public static final Pattern USERNAME_PATTERN = Pattern.compile("[a-zA-Z0-9-_\\.]{1,16}");

   @LauncherAPI
   public static Object getMapValue(Map map, Object key, String error) {
      return verify(map.get(key), (v) -> {
         return v != null;
      }, error);
   }

   @LauncherAPI
   public static boolean isValidIDName(String name) {
      return !name.isEmpty() && name.length() <= 255 && name.chars().allMatch(VerifyHelper::isValidIDNameChar);
   }

   @LauncherAPI
   public static boolean isValidIDNameChar(int ch) {
      return ch >= 97 && ch <= 122 || ch >= 65 && ch <= 90 || ch >= 48 && ch <= 57 || ch == 45 || ch == 95;
   }

   @LauncherAPI
   public static boolean isValidUsername(CharSequence username) {
      return USERNAME_PATTERN.matcher(username).matches();
   }

   public static void putIfAbsent(Map map, Object key, Object value, String error) {
      verify(map.putIfAbsent(key, value), (o) -> {
         return o == null;
      }, error);
   }

   @LauncherAPI
   public static IntPredicate range(int min, int max) {
      return (i) -> {
         return i >= min && i <= max;
      };
   }

   @LauncherAPI
   public static Object verify(Object object, Predicate predicate, String error) {
      if (predicate.test(object)) {
         return object;
      } else {
         throw new IllegalArgumentException(error);
      }
   }

   @LauncherAPI
   public static double verifyDouble(double d, DoublePredicate predicate, String error) {
      if (predicate.test(d)) {
         return d;
      } else {
         throw new IllegalArgumentException(error);
      }
   }

   @LauncherAPI
   public static String verifyIDName(String name) {
      return (String)verify(name, (name1) -> VerifyHelper.isValidIDName((String) name1), String.format("Invalid name: '%s'", name));
   }

   @LauncherAPI
   public static int verifyInt(int i, IntPredicate predicate, String error) {
      if (predicate.test(i)) {
         return i;
      } else {
         throw new IllegalArgumentException(error);
      }
   }

   @LauncherAPI
   public static long verifyLong(long l, LongPredicate predicate, String error) {
      if (predicate.test(l)) {
         return l;
      } else {
         throw new IllegalArgumentException(error);
      }
   }

   @LauncherAPI
   public static String verifyUsername(String username) {
      return (String)verify(username, (username1) -> VerifyHelper.isValidUsername((CharSequence) username1), String.format("Invalid username: '%s'", username));
   }
}
