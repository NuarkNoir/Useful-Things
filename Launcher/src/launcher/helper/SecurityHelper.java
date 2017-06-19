package launcher.helper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.security.CodeSource;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.interfaces.RSAKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import launcher.LauncherAPI;
import sun.misc.Resource;

public final class SecurityHelper {
   @LauncherAPI
   public static final String RSA_ALGO = "RSA";
   @LauncherAPI
   public static final String RSA_SIGN_ALGO = "SHA256withRSA";
   @LauncherAPI
   public static final String RSA_CIPHER_ALGO = "RSA/ECB/PKCS1Padding";
   @LauncherAPI
   public static final int TOKEN_LENGTH = 16;
   @LauncherAPI
   public static final int TOKEN_STRING_LENGTH = 32;
   @LauncherAPI
   public static final int RSA_KEY_LENGTH_BITS = 2048;
   @LauncherAPI
   public static final int RSA_KEY_LENGTH = 256;
   @LauncherAPI
   public static final int CRYPTO_MAX_LENGTH = 2048;
   @LauncherAPI
   public static final String CERTIFICATE_DIGEST = "fca9659209c6b3b510d9d0e328f37ea0e8df11b6f897c70e4fce440501f43075";
   @LauncherAPI
   public static final String HEX = "0123456789abcdef";
   private static final char[] VOWELS = new char[]{'e', 'u', 'i', 'o', 'a'};
   private static final char[] CONS = new char[]{'r', 't', 'p', 's', 'd', 'f', 'g', 'h', 'k', 'l', 'c', 'v', 'b', 'n', 'm'};

   @LauncherAPI
   public static byte[] digest(SecurityHelper.DigestAlgorithm algo, String s) {
      return digest(algo, IOHelper.encode(s));
   }

   @LauncherAPI
   public static byte[] digest(SecurityHelper.DigestAlgorithm algo, URL url) throws IOException {
      InputStream input = IOHelper.newInput(url);
      Throwable var3 = null;

      byte[] var4;
      try {
         var4 = digest(algo, input);
      } catch (Throwable var13) {
         var3 = var13;
         throw var13;
      } finally {
         if (input != null) {
            if (var3 != null) {
               try {
                  input.close();
               } catch (Throwable var12) {
                  var3.addSuppressed(var12);
               }
            } else {
               input.close();
            }
         }

      }

      return var4;
   }

   @LauncherAPI
   public static byte[] digest(SecurityHelper.DigestAlgorithm algo, Path file) throws IOException {
      InputStream input = IOHelper.newInput(file);
      Throwable var3 = null;

      byte[] var4;
      try {
         var4 = digest(algo, input);
      } catch (Throwable var13) {
         var3 = var13;
         throw var13;
      } finally {
         if (input != null) {
            if (var3 != null) {
               try {
                  input.close();
               } catch (Throwable var12) {
                  var3.addSuppressed(var12);
               }
            } else {
               input.close();
            }
         }

      }

      return var4;
   }

   @LauncherAPI
   public static byte[] digest(SecurityHelper.DigestAlgorithm algo, byte[] bytes) {
      return newDigest(algo).digest(bytes);
   }

   @LauncherAPI
   public static byte[] digest(SecurityHelper.DigestAlgorithm algo, InputStream input) throws IOException {
      byte[] buffer = IOHelper.newBuffer();
      MessageDigest digest = newDigest(algo);

      for(int length = input.read(buffer); length != -1; length = input.read(buffer)) {
         digest.update(buffer, 0, length);
      }

      return digest.digest();
   }

   @LauncherAPI
   public static KeyPair genRSAKeyPair(SecureRandom random) {
      try {
         KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
         generator.initialize(2048, random);
         return generator.genKeyPair();
      } catch (NoSuchAlgorithmException var2) {
         throw new InternalError(var2);
      }
   }

   @LauncherAPI
   public static KeyPair genRSAKeyPair() {
      return genRSAKeyPair(newRandom());
   }

   @LauncherAPI
   public static boolean isValidCertificate(Certificate cert) {
      try {
         return toHex(digest(SecurityHelper.DigestAlgorithm.SHA256, cert.getEncoded())).equals("fca9659209c6b3b510d9d0e328f37ea0e8df11b6f897c70e4fce440501f43075");
      } catch (CertificateEncodingException var2) {
         throw new InternalError(var2);
      }
   }

   @LauncherAPI
   public static boolean isValidCertificates(Certificate... certs) {
      return certs != null && certs.length == 1 && isValidCertificate(certs[0]);
   }

   @LauncherAPI
   public static boolean isValidCertificates(Class clazz) {
      Resource metaInf = JVMHelper.UCP.getResource("META-INF/MANIFEST.MF");
      if (metaInf != null && isValidCertificates(metaInf.getCertificates())) {
         CodeSource source = clazz.getProtectionDomain().getCodeSource();
         return source != null && isValidCertificates(source.getCertificates());
      } else {
         return false;
      }
   }

   @LauncherAPI
   public static boolean isValidSign(Path path, byte[] sign, RSAPublicKey publicKey) throws IOException, SignatureException {
      InputStream input = IOHelper.newInput(path);
      Throwable var4 = null;

      boolean var5;
      try {
         var5 = isValidSign(input, sign, publicKey);
      } catch (Throwable var14) {
         var4 = var14;
         throw var14;
      } finally {
         if (input != null) {
            if (var4 != null) {
               try {
                  input.close();
               } catch (Throwable var13) {
                  var4.addSuppressed(var13);
               }
            } else {
               input.close();
            }
         }

      }

      return var5;
   }

   @LauncherAPI
   public static boolean isValidSign(byte[] bytes, byte[] sign, RSAPublicKey publicKey) throws SignatureException {
      Signature signature = newRSAVerifySignature(publicKey);

      try {
         signature.update(bytes);
      } catch (SignatureException var5) {
         throw new InternalError(var5);
      }

      return signature.verify(sign);
   }

   @LauncherAPI
   public static boolean isValidSign(InputStream input, byte[] sign, RSAPublicKey publicKey) throws IOException, SignatureException {
      Signature signature = newRSAVerifySignature(publicKey);
      updateSignature(input, signature);
      return signature.verify(sign);
   }

   @LauncherAPI
   public static boolean isValidSign(URL url, byte[] sign, RSAPublicKey publicKey) throws IOException, SignatureException {
      InputStream input = IOHelper.newInput(url);
      Throwable var4 = null;

      boolean var5;
      try {
         var5 = isValidSign(input, sign, publicKey);
      } catch (Throwable var14) {
         var4 = var14;
         throw var14;
      } finally {
         if (input != null) {
            if (var4 != null) {
               try {
                  input.close();
               } catch (Throwable var13) {
                  var4.addSuppressed(var13);
               }
            } else {
               input.close();
            }
         }

      }

      return var5;
   }

   @LauncherAPI
   public static boolean isValidToken(CharSequence token) {
      return token.length() == 32 && token.chars().allMatch((ch) -> {
         return "0123456789abcdef".indexOf(ch) >= 0;
      });
   }

   @LauncherAPI
   public static MessageDigest newDigest(SecurityHelper.DigestAlgorithm algo) {
      VerifyHelper.verify(algo, (a) -> {
         return a != SecurityHelper.DigestAlgorithm.PLAIN;
      }, "PLAIN digest");

      try {
         return MessageDigest.getInstance(algo.name);
      } catch (NoSuchAlgorithmException var2) {
         throw new InternalError(var2);
      }
   }

   @LauncherAPI
   public static Cipher newRSADecryptCipher(RSAPrivateKey key) {
      return newRSACipher(2, key);
   }

   @LauncherAPI
   public static Cipher newRSAEncryptCipher(RSAPublicKey key) {
      return newRSACipher(1, key);
   }

   @LauncherAPI
   public static Signature newRSASignSignature(RSAPrivateKey key) {
      Signature signature = newRSASignature();

      try {
         signature.initSign(key);
         return signature;
      } catch (InvalidKeyException var3) {
         throw new InternalError(var3);
      }
   }

   @LauncherAPI
   public static Signature newRSAVerifySignature(RSAPublicKey key) {
      Signature signature = newRSASignature();

      try {
         signature.initVerify(key);
         return signature;
      } catch (InvalidKeyException var3) {
         throw new InternalError(var3);
      }
   }

   @LauncherAPI
   public static SecureRandom newRandom() {
      return new SecureRandom();
   }

   @LauncherAPI
   public static byte[] randomBytes(Random random, int length) {
      byte[] bytes = new byte[length];
      random.nextBytes(bytes);
      return bytes;
   }

   @LauncherAPI
   public static byte[] randomBytes(int length) {
      return randomBytes(newRandom(), length);
   }

   @LauncherAPI
   public static String randomStringToken(Random random) {
      return toHex(randomToken(random));
   }

   @LauncherAPI
   public static String randomStringToken() {
      return randomStringToken(newRandom());
   }

   @LauncherAPI
   public static byte[] randomToken(Random random) {
      return randomBytes(random, 16);
   }

   @LauncherAPI
   public static byte[] randomToken() {
      return randomToken(newRandom());
   }

   @LauncherAPI
   public static String randomUsername(Random random) {
      int usernameLength = 3 + random.nextInt(7);
      int prefixType = random.nextInt(7);
      String prefix;
      if (usernameLength >= 5 && prefixType == 6) {
         prefix = random.nextBoolean() ? "Mr" : "Dr";
         usernameLength -= 2;
      } else if (usernameLength >= 6 && prefixType == 5) {
         prefix = "Mrs";
         usernameLength -= 3;
      } else {
         prefix = "";
      }

      int suffixType = random.nextInt(7);
      String suffix;
      if (usernameLength >= 5 && suffixType == 6) {
         suffix = String.valueOf(10 + random.nextInt(90));
         usernameLength -= 2;
      } else if (usernameLength >= 7 && suffixType == 5) {
         suffix = String.valueOf(1990 + random.nextInt(26));
         usernameLength -= 4;
      } else {
         suffix = "";
      }

      int consRepeat = 0;
      boolean consPrev = random.nextBoolean();
      char[] chars = new char[usernameLength];

      for(int i = 0; i < chars.length; ++i) {
         if (i > 1 && consPrev && random.nextInt(10) == 0) {
            chars[i] = chars[i - 1];
         } else {
            if (consRepeat < 1 && random.nextInt() == 5) {
               ++consRepeat;
            } else {
               consRepeat = 0;
               consPrev ^= true;
            }

            char[] alphabet = consPrev ? CONS : VOWELS;
            chars[i] = alphabet[random.nextInt(alphabet.length)];
         }
      }

      if (!prefix.isEmpty() || random.nextBoolean()) {
         chars[0] = Character.toUpperCase(chars[0]);
      }

      return VerifyHelper.verifyUsername(prefix + new String(chars) + suffix);
   }

   @LauncherAPI
   public static String randomUsername() {
      return randomUsername(newRandom());
   }

   @LauncherAPI
   public static byte[] sign(InputStream input, RSAPrivateKey privateKey) throws IOException {
      Signature signature = newRSASignSignature(privateKey);
      updateSignature(input, signature);

      try {
         return signature.sign();
      } catch (SignatureException var4) {
         throw new InternalError(var4);
      }
   }

   @LauncherAPI
   public static byte[] sign(byte[] bytes, RSAPrivateKey privateKey) {
      Signature signature = newRSASignSignature(privateKey);

      try {
         signature.update(bytes);
         return signature.sign();
      } catch (SignatureException var4) {
         throw new InternalError(var4);
      }
   }

   @LauncherAPI
   public static byte[] sign(Path path, RSAPrivateKey privateKey) throws IOException {
      InputStream input = IOHelper.newInput(path);
      Throwable var3 = null;

      byte[] var4;
      try {
         var4 = sign(input, privateKey);
      } catch (Throwable var13) {
         var3 = var13;
         throw var13;
      } finally {
         if (input != null) {
            if (var3 != null) {
               try {
                  input.close();
               } catch (Throwable var12) {
                  var3.addSuppressed(var12);
               }
            } else {
               input.close();
            }
         }

      }

      return var4;
   }

   @LauncherAPI
   public static String toHex(byte[] bytes) {
      int offset = 0;
      char[] hex = new char[bytes.length << 1];

      for(byte currentByte : bytes) {
         int ub = Byte.toUnsignedInt(currentByte);
         hex[offset] = "0123456789abcdef".charAt(ub >>> 4);
         ++offset;
         hex[offset] = "0123456789abcdef".charAt(ub & 15);
         ++offset;
      }

      return new String(hex);
   }

   @LauncherAPI
   public static RSAPrivateKey toPrivateRSAKey(byte[] bytes) throws InvalidKeySpecException {
      return (RSAPrivateKey)newRSAKeyFactory().generatePrivate(new PKCS8EncodedKeySpec(bytes));
   }

   @LauncherAPI
   public static RSAPublicKey toPublicRSAKey(byte[] bytes) throws InvalidKeySpecException {
      return (RSAPublicKey)newRSAKeyFactory().generatePublic(new X509EncodedKeySpec(bytes));
   }

   @LauncherAPI
   public static void verifyCertificates(Class clazz) {
      if (!isValidCertificates(clazz)) {
         ;
      }

   }

   @LauncherAPI
   public static void verifySign(byte[] bytes, byte[] sign, RSAPublicKey publicKey) throws SignatureException {
      if (!isValidSign(bytes, sign, publicKey)) {
         throw new SignatureException("Invalid sign");
      }
   }

   @LauncherAPI
   public static void verifySign(InputStream input, byte[] sign, RSAPublicKey publicKey) throws SignatureException, IOException {
      if (!isValidSign(input, sign, publicKey)) {
         throw new SignatureException("Invalid stream sign");
      }
   }

   @LauncherAPI
   public static void verifySign(Path path, byte[] sign, RSAPublicKey publicKey) throws SignatureException, IOException {
      if (!isValidSign(path, sign, publicKey)) {
         throw new SignatureException(String.format("Invalid file sign: '%s'", path));
      }
   }

   @LauncherAPI
   public static void verifySign(URL url, byte[] sign, RSAPublicKey publicKey) throws SignatureException, IOException {
      if (!isValidSign(url, sign, publicKey)) {
         throw new SignatureException(String.format("Invalid URL sign: '%s'", url));
      }
   }

   @LauncherAPI
   public static String verifyToken(String token) {
      return (String)VerifyHelper.verify(token, (token1) -> SecurityHelper.isValidToken((CharSequence) token1), String.format("Invalid token: '%s'", token));
   }

   private static Cipher newCipher(String algo) {
      try {
         return Cipher.getInstance(algo);
      } catch (NoSuchPaddingException | NoSuchAlgorithmException var2) {
         throw new InternalError(var2);
      }
   }

   private static Cipher newRSACipher(int mode, RSAKey key) {
      Cipher cipher = newCipher("RSA/ECB/PKCS1Padding");

      try {
         cipher.init(mode, (Key)key);
         return cipher;
      } catch (InvalidKeyException var4) {
         throw new InternalError(var4);
      }
   }

   private static KeyFactory newRSAKeyFactory() {
      try {
         return KeyFactory.getInstance("RSA");
      } catch (NoSuchAlgorithmException var1) {
         throw new InternalError(var1);
      }
   }

   private static Signature newRSASignature() {
      try {
         return Signature.getInstance("SHA256withRSA");
      } catch (NoSuchAlgorithmException var1) {
         throw new InternalError(var1);
      }
   }

   private static void updateSignature(InputStream input, Signature signature) throws IOException {
      byte[] buffer = IOHelper.newBuffer();

      for(int length = input.read(buffer); length >= 0; length = input.read(buffer)) {
         try {
            signature.update(buffer, 0, length);
         } catch (SignatureException var5) {
            throw new InternalError(var5);
         }
      }

   }

   @LauncherAPI
   public static enum DigestAlgorithm {
      PLAIN("plain"),
      MD5("MD5"),
      SHA1("SHA-1"),
      SHA224("SHA-224"),
      SHA256("SHA-256"),
      SHA512("SHA-512");

      private static final Map ALGORITHMS = new HashMap(6);
      public final String name;

      private DigestAlgorithm(String name) {
         this.name = name;
      }

      public String toString() {
         return this.name;
      }

      public static SecurityHelper.DigestAlgorithm byName(String name) {
         return (SecurityHelper.DigestAlgorithm)VerifyHelper.getMapValue(ALGORITHMS, name, String.format("Unknown digest algorithm: '%s'", name));
      }

      static {
         SecurityHelper.DigestAlgorithm[] algorithmsValues = values();

         for(SecurityHelper.DigestAlgorithm algorithm : algorithmsValues) {
            ALGORITHMS.put(algorithm.name, algorithm);
         }

      }
   }
}
