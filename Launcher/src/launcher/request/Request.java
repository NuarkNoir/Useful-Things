package launcher.request;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;
import launcher.Launcher;
import launcher.LauncherAPI;
import launcher.helper.IOHelper;
import launcher.serialize.HInput;
import launcher.serialize.HOutput;
import launcher.serialize.stream.EnumSerializer;

public abstract class Request {
   private final AtomicBoolean started;
   @LauncherAPI
   protected final Launcher.Config config;

   @LauncherAPI
   protected Request(Launcher.Config config) {
      this.started = new AtomicBoolean(false);
      this.config = config == null ? Launcher.getConfig() : config;
   }

   @LauncherAPI
   protected Request() {
      this((Launcher.Config)null);
   }

   @LauncherAPI
   public abstract Request.Type getType();

   @LauncherAPI
   public Object request() throws Exception {
      if (!this.started.compareAndSet(false, true)) {
         throw new IllegalStateException("Request already started");
      } else {
         Socket socket = IOHelper.newSocket();
         Throwable var2 = null;

         Object var7;
         try {
            socket.connect(IOHelper.resolve(this.config.address));
            HInput input = new HInput(socket.getInputStream());
            Throwable var4 = null;

            try {
               HOutput output = new HOutput(socket.getOutputStream());
               Throwable var6 = null;

               try {
                  this.writeHandshake(input, output);
                  var7 = this.requestDo(input, output);
               } catch (Throwable var51) {
                  var7 = var51;
                  var6 = var51;
                  throw var51;
               } finally {
                  if (output != null) {
                     if (var6 != null) {
                        try {
                           output.close();
                        } catch (Throwable var50) {
                           var6.addSuppressed(var50);
                        }
                     } else {
                        output.close();
                     }
                  }

               }
            } catch (Throwable var53) {
               var4 = var53;
               throw var53;
            } finally {
               if (input != null) {
                  if (var4 != null) {
                     try {
                        input.close();
                     } catch (Throwable var49) {
                        var4.addSuppressed(var49);
                     }
                  } else {
                     input.close();
                  }
               }

            }
         } catch (Throwable var55) {
            var2 = var55;
            throw var55;
         } finally {
            if (socket != null) {
               if (var2 != null) {
                  try {
                     socket.close();
                  } catch (Throwable var48) {
                     var2.addSuppressed(var48);
                  }
               } else {
                  socket.close();
               }
            }

         }

         return var7;
      }
   }

   @LauncherAPI
   protected final void readError(HInput input) throws IOException {
      String error = input.readString(0);
      if (!error.isEmpty()) {
         requestError(error);
      }

   }

   @LauncherAPI
   protected abstract Object requestDo(HInput var1, HOutput var2) throws Exception;

   private void writeHandshake(HInput input, HOutput output) throws IOException {
      output.writeInt(1917264918);
      output.writeBigInteger(this.config.publicKey.getModulus(), 257);
      EnumSerializer.write(output, this.getType());
      output.flush();
      if (!input.readBoolean()) {
         requestError("Serverside not accepted this connection");
      }

   }

   @LauncherAPI
   public static void requestError(String message) throws RequestException {
      throw new RequestException(message);
   }

   @LauncherAPI
   public static enum Type implements EnumSerializer.Itf {
      PING(0),
      LAUNCHER(1),
      UPDATE(2),
      UPDATE_LIST(3),
      AUTH(4),
      JOIN_SERVER(5),
      CHECK_SERVER(6),
      PROFILE_BY_USERNAME(7),
      PROFILE_BY_UUID(8),
      BATCH_PROFILE_BY_USERNAME(9),
      SCREENSHOT(10),
      CUSTOM(255);

      private static final EnumSerializer SERIALIZER = new EnumSerializer(Request.Type.class);
      private final int n;

      private Type(int n) {
         this.n = n;
      }

      public int getNumber() {
         return this.n;
      }

      @LauncherAPI
      public static Request.Type read(HInput input) throws IOException {
         return (Request.Type)SERIALIZER.read(input);
      }
   }
}
