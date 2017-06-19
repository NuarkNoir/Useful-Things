package launcher.helper;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.Set;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import launcher.LauncherAPI;

public final class IOHelper {
   @LauncherAPI
   public static final Charset UNICODE_CHARSET = StandardCharsets.UTF_8;
   @LauncherAPI
   public static final Charset ASCII_CHARSET = StandardCharsets.US_ASCII;
   @LauncherAPI
   public static final int SOCKET_TIMEOUT = VerifyHelper.verifyInt(Integer.parseUnsignedInt(System.getProperty("launcher.socketTimeout", Integer.toString(30000))), VerifyHelper.POSITIVE, "launcher.socketTimeout can't be <= 0");
   @LauncherAPI
   public static final int HTTP_TIMEOUT = VerifyHelper.verifyInt(Integer.parseUnsignedInt(System.getProperty("launcher.httpTimeout", Integer.toString(5000))), VerifyHelper.POSITIVE, "launcher.httpTimeout can't be <= 0");
   @LauncherAPI
   public static final int BUFFER_SIZE = VerifyHelper.verifyInt(Integer.parseUnsignedInt(System.getProperty("launcher.bufferSize", Integer.toString(4096))), VerifyHelper.POSITIVE, "launcher.bufferSize can't be <= 0");
   @LauncherAPI
   public static final String CROSS_SEPARATOR = "/";
   @LauncherAPI
   public static final FileSystem FS = FileSystems.getDefault();
   @LauncherAPI
   public static final String PLATFORM_SEPARATOR = FS.getSeparator();
   @LauncherAPI
   public static final boolean POSIX = FS.supportedFileAttributeViews().contains("posix");
   @LauncherAPI
   public static final Path JVM_DIR = Paths.get(System.getProperty("java.home"));
   @LauncherAPI
   public static final Path HOME_DIR = Paths.get(System.getProperty("user.home"));
   @LauncherAPI
   public static final Path WORKING_DIR = Paths.get(System.getProperty("user.dir"));
   private static final LinkOption[] LINK_OPTIONS = new LinkOption[0];
   private static final OpenOption[] READ_OPTIONS = new OpenOption[]{StandardOpenOption.READ};
   private static final CopyOption[] COPY_OPTIONS = new CopyOption[]{StandardCopyOption.REPLACE_EXISTING};
   private static final OpenOption[] APPEND_OPTIONS = new OpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND};
   private static final OpenOption[] WRITE_OPTIONS = new OpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING};
   private static final Set WALK_OPTIONS = Collections.singleton(FileVisitOption.FOLLOW_LINKS);

   @LauncherAPI
   public static void close(AutoCloseable closeable) {
      try {
         closeable.close();
      } catch (Exception var2) {
         LogHelper.error(var2);
      }

   }

   @LauncherAPI
   public static void copy(Path source, Path target) throws IOException {
      createParentDirs(target);
      Files.copy(source, target, COPY_OPTIONS);
   }

   @LauncherAPI
   public static void createParentDirs(Path path) throws IOException {
      Path parent = path.getParent();
      if (parent != null && !isDir(parent)) {
         Files.createDirectories(parent);
      }

   }

   @LauncherAPI
   public static String decode(byte[] bytes) {
      return new String(bytes, UNICODE_CHARSET);
   }

   @LauncherAPI
   public static String decodeASCII(byte[] bytes) {
      return new String(bytes, ASCII_CHARSET);
   }

   @LauncherAPI
   public static void deleteDir(Path dir, boolean self) throws IOException {
      walk(dir, new IOHelper.DeleteDirVisitor(dir, self), true);
   }

   @LauncherAPI
   public static byte[] encode(String s) {
      return s.getBytes(UNICODE_CHARSET);
   }

   @LauncherAPI
   public static byte[] encodeASCII(String s) {
      return s.getBytes(ASCII_CHARSET);
   }

   @LauncherAPI
   public static boolean exists(Path path) {
      return Files.exists(path, LINK_OPTIONS);
   }

   @LauncherAPI
   public static Path getCodeSource(Class clazz) {
      return Paths.get(toURI(clazz.getProtectionDomain().getCodeSource().getLocation()));
   }

   @LauncherAPI
   public static String getFileName(Path path) {
      return path.getFileName().toString();
   }

   @LauncherAPI
   public static String getIP(SocketAddress address) {
      return ((InetSocketAddress)address).getAddress().getHostAddress();
   }

   @LauncherAPI
   public static byte[] getResourceBytes(String name) throws IOException {
      return read(getResourceURL(name));
   }

   @LauncherAPI
   public static URL getResourceURL(String name) throws NoSuchFileException {
      URL url = ClassLoader.getSystemResource(name);
      if (url == null) {
         throw new NoSuchFileException(name);
      } else {
         return url;
      }
   }

   @LauncherAPI
   public static boolean hasExtension(Path file, String extension) {
      return getFileName(file).endsWith('.' + extension);
   }

   @LauncherAPI
   public static boolean isDir(Path path) {
      return Files.isDirectory(path, LINK_OPTIONS);
   }

   @LauncherAPI
   public static boolean isEmpty(Path dir) throws IOException {
      DirectoryStream stream = Files.newDirectoryStream(dir);
      Throwable var2 = null;

      boolean var3;
      try {
         var3 = !stream.iterator().hasNext();
      } catch (Throwable var12) {
         var2 = var12;
         throw var12;
      } finally {
         if (stream != null) {
            if (var2 != null) {
               try {
                  stream.close();
               } catch (Throwable var11) {
                  var2.addSuppressed(var11);
               }
            } else {
               stream.close();
            }
         }

      }

      return var3;
   }

   @LauncherAPI
   public static boolean isFile(Path path) {
      return Files.isRegularFile(path, LINK_OPTIONS);
   }

   @LauncherAPI
   public static boolean isValidFileName(String fileName) {
      return !fileName.equals(".") && !fileName.equals("..") && fileName.chars().noneMatch((ch) -> {
         return ch == 47 || ch == 92;
      }) && isValidPath(fileName);
   }

   @LauncherAPI
   public static boolean isValidPath(String path) {
      try {
         toPath(path);
         return true;
      } catch (InvalidPathException var2) {
         return false;
      }
   }

   @LauncherAPI
   public static boolean isValidTextureBounds(int width, int height) {
      return width % 64 == 0 && height * 2 == width && width <= 1024;
   }

   @LauncherAPI
   public static void move(Path source, Path target) throws IOException {
      createParentDirs(target);
      Files.move(source, target, COPY_OPTIONS);
   }

   @LauncherAPI
   public static byte[] newBuffer() {
      return new byte[4096];
   }

   @LauncherAPI
   public static ByteArrayOutputStream newByteArrayOutput() {
      return new ByteArrayOutputStream();
   }

   @LauncherAPI
   public static char[] newCharBuffer() {
      return new char[4096];
   }

   @LauncherAPI
   public static InputStream newInput(URL url) throws IOException {
      URLConnection connection = url.openConnection();
      if (connection instanceof HttpURLConnection) {
         connection.setReadTimeout(HTTP_TIMEOUT);
         connection.setConnectTimeout(HTTP_TIMEOUT);
      }

      connection.setDoInput(true);
      connection.setDoOutput(false);
      return connection.getInputStream();
   }

   @LauncherAPI
   public static InputStream newInput(Path file) throws IOException {
      return Files.newInputStream(file, READ_OPTIONS);
   }

   @LauncherAPI
   public static OutputStream newOutput(Path file) throws IOException {
      return newOutput(file, false);
   }

   @LauncherAPI
   public static OutputStream newOutput(Path file, boolean append) throws IOException {
      createParentDirs(file);
      return Files.newOutputStream(file, append ? APPEND_OPTIONS : WRITE_OPTIONS);
   }

   @LauncherAPI
   public static BufferedReader newReader(InputStream input) {
      return newReader(input, UNICODE_CHARSET);
   }

   @LauncherAPI
   public static BufferedReader newReader(InputStream input, Charset charset) {
      return new BufferedReader(new InputStreamReader(input, charset));
   }

   @LauncherAPI
   public static BufferedReader newReader(URL url) throws IOException {
      return newReader(newInput(url));
   }

   @LauncherAPI
   public static BufferedReader newReader(Path file) throws IOException {
      return Files.newBufferedReader(file, UNICODE_CHARSET);
   }

   @LauncherAPI
   public static Socket newSocket() throws SocketException {
      Socket socket = new Socket();
      setSocketFlags(socket);
      return socket;
   }

   @LauncherAPI
   public static BufferedWriter newWriter(OutputStream output) {
      return new BufferedWriter(new OutputStreamWriter(output, UNICODE_CHARSET));
   }

   @LauncherAPI
   public static BufferedWriter newWriter(Path file) throws IOException {
      return newWriter(file, false);
   }

   @LauncherAPI
   public static BufferedWriter newWriter(Path file, boolean append) throws IOException {
      createParentDirs(file);
      return Files.newBufferedWriter(file, UNICODE_CHARSET, append ? APPEND_OPTIONS : WRITE_OPTIONS);
   }

   @LauncherAPI
   public static BufferedWriter newWriter(FileDescriptor fd) {
      return newWriter(new FileOutputStream(fd));
   }

   @LauncherAPI
   public static ZipEntry newZipEntry(String name) {
      ZipEntry entry = new ZipEntry(name);
      entry.setTime(0L);
      return entry;
   }

   @LauncherAPI
   public static ZipEntry newZipEntry(ZipEntry entry) {
      return newZipEntry(entry.getName());
   }

   @LauncherAPI
   public static ZipInputStream newZipInput(InputStream input) {
      return new ZipInputStream(input, UNICODE_CHARSET);
   }

   @LauncherAPI
   public static ZipInputStream newZipInput(URL url) throws IOException {
      return newZipInput(newInput(url));
   }

   @LauncherAPI
   public static ZipInputStream newZipInput(Path file) throws IOException {
      return newZipInput(newInput(file));
   }

   @LauncherAPI
   public static byte[] read(Path file) throws IOException {
      long size = readAttributes(file).size();
      if (size > 2147483647L) {
         throw new IOException("File too big");
      } else {
         byte[] bytes = new byte[(int)size];
         InputStream input = newInput(file);
         Throwable var5 = null;

         try {
            read(input, bytes);
         } catch (Throwable var14) {
            var5 = var14;
            throw var14;
         } finally {
            if (input != null) {
               if (var5 != null) {
                  try {
                     input.close();
                  } catch (Throwable var13) {
                     var5.addSuppressed(var13);
                  }
               } else {
                  input.close();
               }
            }

         }

         return bytes;
      }
   }

   @LauncherAPI
   public static byte[] read(URL url) throws IOException {
      InputStream input = newInput(url);
      Throwable var2 = null;

      byte[] var3;
      try {
         var3 = read(input);
      } catch (Throwable var12) {
         var2 = var12;
         throw var12;
      } finally {
         if (input != null) {
            if (var2 != null) {
               try {
                  input.close();
               } catch (Throwable var11) {
                  var2.addSuppressed(var11);
               }
            } else {
               input.close();
            }
         }

      }

      return var3;
   }

   @LauncherAPI
   public static void read(InputStream input, byte[] bytes) throws IOException {
      int length;
      for(int offset = 0; offset < bytes.length; offset += length) {
         length = input.read(bytes, offset, bytes.length - offset);
         if (length < 0) {
            throw new EOFException(String.format("%d bytes remaining", bytes.length - offset));
         }
      }

   }

   @LauncherAPI
   public static byte[] read(InputStream input) throws IOException {
      ByteArrayOutputStream output = newByteArrayOutput();
      Throwable var2 = null;

      byte[] var3;
      try {
         transfer(input, output);
         var3 = output.toByteArray();
      } catch (Throwable var12) {
         var2 = var12;
         throw var12;
      } finally {
         if (output != null) {
            if (var2 != null) {
               try {
                  output.close();
               } catch (Throwable var11) {
                  var2.addSuppressed(var11);
               }
            } else {
               output.close();
            }
         }

      }

      return var3;
   }

   @LauncherAPI
   public static BasicFileAttributes readAttributes(Path path) throws IOException {
      return Files.readAttributes(path, BasicFileAttributes.class, LINK_OPTIONS);
   }

   @LauncherAPI
   public static BufferedImage readTexture(Object input) throws IOException {
      ImageReader reader = (ImageReader)ImageIO.getImageReadersByMIMEType("image/png").next();

      BufferedImage var4;
      try {
         reader.setInput(ImageIO.createImageInputStream(input), false, false);
         int width = reader.getWidth(0);
         int height = reader.getHeight(0);
         if (!isValidTextureBounds(width, height)) {
            throw new IOException(String.format("Invalid texture bounds: %dx%d", width, height));
         }

         var4 = reader.read(0);
      } finally {
         reader.dispose();
      }

      return var4;
   }

   @LauncherAPI
   public static String request(URL url) throws IOException {
      return decode(read(url)).trim();
   }

   @LauncherAPI
   public static InetSocketAddress resolve(InetSocketAddress address) {
      return address.isUnresolved() ? new InetSocketAddress(address.getHostString(), address.getPort()) : address;
   }

   @LauncherAPI
   public static Path resolveIncremental(Path dir, String name, String extension) {
      Path original = dir.resolve(name + '.' + extension);
      if (!exists(original)) {
         return original;
      } else {
         int counter = 1;

         while(true) {
            Path path = dir.resolve(String.format("%s (%d).%s", name, counter, extension));
            if (!exists(path)) {
               return path;
            }

            ++counter;
         }
      }
   }

   @LauncherAPI
   public static Path resolveJavaBin(Path javaDir) {
      Path javaBinDir = (javaDir == null ? JVM_DIR : javaDir).resolve("bin");
      if (!LogHelper.isDebugEnabled()) {
         Path javawExe = javaBinDir.resolve("javaw.exe");
         if (isFile(javawExe)) {
            return javawExe;
         }
      }

      Path javaExe = javaBinDir.resolve("java.exe");
      if (isFile(javaExe)) {
         return javaExe;
      } else {
         Path java = javaBinDir.resolve("java");
         if (isFile(java)) {
            return java;
         } else {
            throw new RuntimeException("Java binary wasn't found");
         }
      }
   }

   @LauncherAPI
   public static void setSocketFlags(Socket socket) throws SocketException {
      socket.setKeepAlive(false);
      socket.setTcpNoDelay(false);
      socket.setReuseAddress(true);
      socket.setSoTimeout(SOCKET_TIMEOUT);
      socket.setTrafficClass(28);
      socket.setPerformancePreferences(1, 0, 2);
   }

   @LauncherAPI
   public static Path toPath(String path) {
      return Paths.get(path.replace("/", PLATFORM_SEPARATOR));
   }

   @LauncherAPI
   public static String toString(Path path) {
      return path.toString().replace(PLATFORM_SEPARATOR, "/");
   }

   @LauncherAPI
   public static URI toURI(URL url) {
      try {
         return url.toURI();
      } catch (URISyntaxException var2) {
         throw new IllegalArgumentException(var2);
      }
   }

   @LauncherAPI
   public static URL toURL(Path path) {
      try {
         return path.toUri().toURL();
      } catch (MalformedURLException var2) {
         throw new InternalError(var2);
      }
   }

   @LauncherAPI
   public static int transfer(InputStream input, OutputStream output) throws IOException {
      int transferred = 0;
      byte[] buffer = newBuffer();

      for(int length = input.read(buffer); length >= 0; length = input.read(buffer)) {
         output.write(buffer, 0, length);
         transferred += length;
      }

      return transferred;
   }

   @LauncherAPI
   public static void transfer(Path file, OutputStream output) throws IOException {
      InputStream input = newInput(file);
      Throwable var3 = null;

      try {
         transfer(input, output);
      } catch (Throwable var12) {
         var3 = var12;
         throw var12;
      } finally {
         if (input != null) {
            if (var3 != null) {
               try {
                  input.close();
               } catch (Throwable var11) {
                  var3.addSuppressed(var11);
               }
            } else {
               input.close();
            }
         }

      }

   }

   @LauncherAPI
   public static int transfer(InputStream input, Path file) throws IOException {
      return transfer(input, file, false);
   }

   @LauncherAPI
   public static int transfer(InputStream input, Path file, boolean append) throws IOException {
      OutputStream output = newOutput(file, append);
      Throwable var4 = null;

      int var5;
      try {
         var5 = transfer(input, output);
      } catch (Throwable var14) {
         var4 = var14;
         throw var14;
      } finally {
         if (output != null) {
            if (var4 != null) {
               try {
                  output.close();
               } catch (Throwable var13) {
                  var4.addSuppressed(var13);
               }
            } else {
               output.close();
            }
         }

      }

      return var5;
   }

   @LauncherAPI
   public static String urlDecode(String s) {
      try {
         return URLDecoder.decode(s, UNICODE_CHARSET.name());
      } catch (UnsupportedEncodingException var2) {
         throw new InternalError(var2);
      }
   }

   @LauncherAPI
   public static String urlEncode(String s) {
      try {
         return URLEncoder.encode(s, UNICODE_CHARSET.name());
      } catch (UnsupportedEncodingException var2) {
         throw new InternalError(var2);
      }
   }

   @LauncherAPI
   public static String verifyFileName(String fileName) {
      return (String)VerifyHelper.verify(fileName, (fileName1) -> IOHelper.isValidFileName((String) fileName1), String.format("Invalid file name: '%s'", fileName));
   }

   @LauncherAPI
   public static int verifyLength(int length, int max) throws IOException {
      if (length >= 0 && (max >= 0 || length == -max) && (max <= 0 || length <= max)) {
         return length;
      } else {
         throw new IOException("Illegal length: " + length);
      }
   }

   @LauncherAPI
   public static BufferedImage verifyTexture(BufferedImage skin) {
      return (BufferedImage)VerifyHelper.verify(skin, (i) -> {
         return isValidTextureBounds(skin.getWidth(), skin.getHeight());
      }, String.format("Invalid texture bounds: %dx%d", skin.getWidth(), skin.getHeight()));
   }

   @LauncherAPI
   public static String verifyURL(String url) {
      try {
         (new URL(url)).toURI();
         return url;
      } catch (URISyntaxException | MalformedURLException var2) {
         throw new IllegalArgumentException("Invalid URL", var2);
      }
   }

   @LauncherAPI
   public static void walk(Path dir, FileVisitor visitor, boolean hidden) throws IOException {
      Files.walkFileTree(dir, WALK_OPTIONS, Integer.MAX_VALUE, (FileVisitor)(hidden ? visitor : new IOHelper.SkipHiddenVisitor(visitor)));
   }

   @LauncherAPI
   public static void write(Path file, byte[] bytes) throws IOException {
      createParentDirs(file);
      Files.write(file, bytes, WRITE_OPTIONS);
   }

   private static final class DeleteDirVisitor extends SimpleFileVisitor {
      private final Path dir;
      private final boolean self;

      private DeleteDirVisitor(Path dir, boolean self) {
         this.dir = dir;
         this.self = self;
      }

      public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
         FileVisitResult result = super.postVisitDirectory(dir, exc);
         if (this.self || !this.dir.equals(dir)) {
            Files.delete(dir);
         }

         return result;
      }

      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
         Files.delete(file);
         return super.visitFile(file, attrs);
      }
   }

   private static final class SkipHiddenVisitor implements FileVisitor {
      private final FileVisitor visitor;

      private SkipHiddenVisitor(FileVisitor visitor) {
         this.visitor = visitor;
      }

      public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
         return Files.isHidden(dir) ? FileVisitResult.CONTINUE : this.visitor.postVisitDirectory(dir, exc);
      }

      public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
         return Files.isHidden(dir) ? FileVisitResult.SKIP_SUBTREE : this.visitor.preVisitDirectory(dir, attrs);
      }

      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
         return Files.isHidden(file) ? FileVisitResult.CONTINUE : this.visitor.visitFile(file, attrs);
      }

      public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
         return this.visitor.visitFileFailed(file, exc);
      }

        @Override
        public FileVisitResult preVisitDirectory(Object dir, BasicFileAttributes attrs) throws IOException {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public FileVisitResult visitFile(Object file, BasicFileAttributes attrs) throws IOException {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public FileVisitResult visitFileFailed(Object file, IOException exc) throws IOException {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public FileVisitResult postVisitDirectory(Object dir, IOException exc) throws IOException {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
   }
}
