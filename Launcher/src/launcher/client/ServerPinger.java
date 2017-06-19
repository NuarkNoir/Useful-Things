package launcher.client;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.regex.Pattern;
import launcher.LauncherAPI;
import launcher.helper.IOHelper;
import launcher.helper.LogHelper;
import launcher.helper.VerifyHelper;
import launcher.serialize.HInput;
import launcher.serialize.HOutput;

public final class ServerPinger {
   private static final String LEGACY_PING_HOST_MAGIC = "§1";
   private static final String LEGACY_PING_HOST_CHANNEL = "MC|PingHost";
   private static final Pattern LEGACY_PING_HOST_DELIMETER = Pattern.compile("\u0000", 16);
   private static final int PACKET_LENGTH = 65535;
   private final InetSocketAddress address;
   private final ClientProfile.Version version;
   private final Object cacheLock = new Object();
   private ServerPinger.Result cache;
   private Instant cacheTime;

   @LauncherAPI
   public ServerPinger(InetSocketAddress address, ClientProfile.Version version) {
      this.address = address;
      this.version = version;
   }

   @LauncherAPI
   public ServerPinger.Result ping() throws IOException {
      Instant now = Instant.now();
      synchronized(this.cacheLock) {
         if (this.cache == null || this.cacheTime == null || Duration.between(now, this.cacheTime).getSeconds() >= 30L) {
            this.cache = this.doPing();
            this.cacheTime = now;
         }

         return this.cache;
      }
   }

   private ServerPinger.Result doPing() throws IOException {
      Socket socket = IOHelper.newSocket();
      Throwable var2 = null;

      Object var7;
      try {
         socket.connect(IOHelper.resolve(this.address), IOHelper.SOCKET_TIMEOUT);
         HInput input = new HInput(socket.getInputStream());
         Throwable var4 = null;

         try {
            HOutput output = new HOutput(socket.getOutputStream());
            Throwable var6 = null;

            try {
               var7 = this.version.compareTo(ClientProfile.Version.MC172) >= 0 ? this.modernPing(input, output) : this.legacyPing(input, output);
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

      return (ServerPinger.Result)var7;
   }

   private ServerPinger.Result legacyPing(HInput input, HOutput output) throws IOException {
      output.writeUnsignedByte(254);
      output.writeUnsignedByte(1);
      output.writeUnsignedByte(250);
      writeUTF16String(output, "MC|PingHost");
      ByteArrayOutputStream packetArray = IOHelper.newByteArrayOutput();
      Throwable response = null;

      byte[] customPayloadPacket;
      try {
         HOutput packetOutput = new HOutput(packetArray);
         Throwable magic = null;

         try {
            packetOutput.writeUnsignedByte(this.version.protocol);
            writeUTF16String(packetOutput, this.address.getHostString());
            packetOutput.writeInt(this.address.getPort());
         } catch (Throwable var30) {
            magic = var30;
            throw var30;
         } finally {
            if (packetOutput != null) {
               if (magic != null) {
                  try {
                     packetOutput.close();
                  } catch (Throwable var29) {
                     magic.addSuppressed(var29);
                  }
               } else {
                  packetOutput.close();
               }
            }

         }

         customPayloadPacket = packetArray.toByteArray();
      } catch (Throwable var32) {
         response = var32;
         throw var32;
      } finally {
         if (packetArray != null) {
            if (response != null) {
               try {
                  packetArray.close();
               } catch (Throwable var28) {
                  response.addSuppressed(var28);
               }
            } else {
               packetArray.close();
            }
         }

      }

      output.writeShort((short)customPayloadPacket.length);
      output.stream.write(customPayloadPacket);
      output.flush();
      int kickPacketID = input.readUnsignedByte();
      if (kickPacketID != 255) {
         throw new IOException("Illegal kick packet ID: " + kickPacketID);
      } else {
         String _response = readUTF16String(input);
         LogHelper.debug("Ping response (legacy): '%s'", _response);
         String[] splitted = LEGACY_PING_HOST_DELIMETER.split(_response);
         if (splitted.length != 6) {
            throw new IOException("Tokens count mismatch");
         } else {
            String magic = splitted[0];
            if (!magic.equals("§1")) {
               throw new IOException("Magic string mismatch: " + magic);
            } else {
               int protocol = Integer.parseInt(splitted[1]);
               if (protocol != this.version.protocol) {
                  throw new IOException("Protocol mismatch: " + protocol);
               } else {
                  String clientVersion = splitted[2];
                  if (!clientVersion.equals(this.version.name)) {
                     throw new IOException(String.format("Version mismatch: '%s'", clientVersion));
                  } else {
                     int onlinePlayers = VerifyHelper.verifyInt(Integer.parseInt(splitted[4]), VerifyHelper.NOT_NEGATIVE, "onlinePlayers can't be < 0");
                     int maxPlayers = VerifyHelper.verifyInt(Integer.parseInt(splitted[5]), VerifyHelper.NOT_NEGATIVE, "maxPlayers can't be < 0");
                     return new ServerPinger.Result(onlinePlayers, maxPlayers, _response);
                  }
               }
            }
         }
      }
   }

   private ServerPinger.Result modernPing(HInput input, HOutput output) throws IOException {
      ByteArrayOutputStream packetArray = IOHelper.newByteArrayOutput();
      Throwable ab = null;

      byte[] handshakePacket;
      try {
         HOutput packetOutput = new HOutput(packetArray);
         Throwable object = null;

         try {
            packetOutput.writeVarInt(0);
            packetOutput.writeVarInt(this.version.protocol);
            packetOutput.writeString(this.address.getHostString(), 0);
            packetOutput.writeShort((short)this.address.getPort());
            packetOutput.writeVarInt(1);
         } catch (Throwable var51) {
            object = var51;
            throw var51;
         } finally {
            if (packetOutput != null) {
               if (object != null) {
                  try {
                     packetOutput.close();
                  } catch (Throwable var50) {
                     object.addSuppressed(var50);
                  }
               } else {
                  packetOutput.close();
               }
            }

         }

         handshakePacket = packetArray.toByteArray();
      } catch (Throwable var53) {
         ab = var53;
         throw var53;
      } finally {
         if (packetArray != null) {
            if (ab != null) {
               try {
                  packetArray.close();
               } catch (Throwable var49) {
                  ab.addSuppressed(var49);
               }
            } else {
               packetArray.close();
            }
         }

      }

      output.writeByteArray(handshakePacket, 65535);
      output.writeVarInt(1);
      output.writeVarInt(0);
      output.flush();
      int _ab = IOHelper.verifyLength(input.readVarInt(), 65535);
      byte[] statusPacket = _ab == 0 ? input.readByteArray(65535) : input.readByteArray(-_ab);
      HInput packetInput = new HInput(statusPacket);
      Throwable playersObject = null;

      String response;
      try {
         int statusPacketID = packetInput.readVarInt();
         if (statusPacketID != 0) {
            throw new IOException("Illegal status packet ID: " + statusPacketID);
         }

         response = packetInput.readString(65535);
         LogHelper.debug("Ping response (modern): '%s'", response);
      } catch (Throwable var55) {
         playersObject = var55;
         throw var55;
      } finally {
         if (packetInput != null) {
            if (playersObject != null) {
               try {
                  packetInput.close();
               } catch (Throwable var48) {
                  playersObject.addSuppressed(var48);
               }
            } else {
               packetInput.close();
            }
         }

      }

      JsonObject object = Json.parse(response).asObject();
      JsonObject _playersObject = object.get("players").asObject();
      int online = _playersObject.get("online").asInt();
      int max = _playersObject.get("max").asInt();
      return new ServerPinger.Result(online, max, response);
   }

   private static String readUTF16String(HInput input) throws IOException {
      int length = input.readUnsignedShort() << 1;
      byte[] encoded = input.readByteArray(-length);
      return new String(encoded, StandardCharsets.UTF_16BE);
   }

   private static void writeUTF16String(HOutput output, String s) throws IOException {
      output.writeShort((short)s.length());
      output.stream.write(s.getBytes(StandardCharsets.UTF_16BE));
   }

   public static final class Result {
      private static final Pattern CODES_PATTERN = Pattern.compile("§[0-9a-fkmnor]", 2);
      @LauncherAPI
      public final int onlinePlayers;
      @LauncherAPI
      public final int maxPlayers;
      @LauncherAPI
      public final String raw;

      public Result(int onlinePlayers, int maxPlayers, String raw) {
         this.onlinePlayers = VerifyHelper.verifyInt(onlinePlayers, VerifyHelper.NOT_NEGATIVE, "onlinePlayers can't be < 0");
         this.maxPlayers = VerifyHelper.verifyInt(maxPlayers, VerifyHelper.NOT_NEGATIVE, "maxPlayers can't be < 0");
         this.raw = raw;
      }

      @LauncherAPI
      public boolean isOverfilled() {
         return this.onlinePlayers >= this.maxPlayers;
      }
   }
}
