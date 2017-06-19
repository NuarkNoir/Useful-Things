package launcher.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntFunction;
import launcher.LauncherAPI;
import launcher.hasher.FileNameMatcher;
import launcher.helper.IOHelper;
import launcher.helper.VerifyHelper;
import launcher.serialize.HInput;
import launcher.serialize.config.ConfigObject;
import launcher.serialize.config.entry.BlockConfigEntry;
import launcher.serialize.config.entry.ConfigEntry;
import launcher.serialize.config.entry.IntegerConfigEntry;
import launcher.serialize.config.entry.ListConfigEntry;
import launcher.serialize.config.entry.StringConfigEntry;
import launcher.serialize.stream.StreamObject;

public final class ClientProfile extends ConfigObject implements Comparable {
   @LauncherAPI
   public static final StreamObject.Adapter RO_ADAPTER = (input) -> {
      return new ClientProfile(input, true);
   };
   private static final FileNameMatcher ASSET_MATCHER = new FileNameMatcher(new String[0], new String[]{"indexes", "objects"}, new String[0]);
   private final StringConfigEntry version;
   private final StringConfigEntry assetIndex;
   private final IntegerConfigEntry sortIndex;
   private final StringConfigEntry title;
   private final StringConfigEntry serverAddress;
   private final IntegerConfigEntry serverPort;
   private final ListConfigEntry update;
   private final ListConfigEntry updateExclusions;
   private final ListConfigEntry updateVerify;
   private final StringConfigEntry mainClass;
   private final ListConfigEntry jvmArgs;
   private final ListConfigEntry classPath;
   private final ListConfigEntry clientArgs;

   @LauncherAPI
   public ClientProfile(BlockConfigEntry block) {
      super(block);
      this.version = (StringConfigEntry)block.getEntry("version", StringConfigEntry.class);
      this.assetIndex = (StringConfigEntry)block.getEntry("assetIndex", StringConfigEntry.class);
      this.sortIndex = (IntegerConfigEntry)block.getEntry("sortIndex", IntegerConfigEntry.class);
      this.title = (StringConfigEntry)block.getEntry("title", StringConfigEntry.class);
      this.serverAddress = (StringConfigEntry)block.getEntry("serverAddress", StringConfigEntry.class);
      this.serverPort = (IntegerConfigEntry)block.getEntry("serverPort", IntegerConfigEntry.class);
      this.update = (ListConfigEntry)block.getEntry("update", ListConfigEntry.class);
      this.updateVerify = (ListConfigEntry)block.getEntry("updateVerify", ListConfigEntry.class);
      this.updateExclusions = (ListConfigEntry)block.getEntry("updateExclusions", ListConfigEntry.class);
      this.mainClass = (StringConfigEntry)block.getEntry("mainClass", StringConfigEntry.class);
      this.classPath = (ListConfigEntry)block.getEntry("classPath", ListConfigEntry.class);
      this.jvmArgs = (ListConfigEntry)block.getEntry("jvmArgs", ListConfigEntry.class);
      this.clientArgs = (ListConfigEntry)block.getEntry("clientArgs", ListConfigEntry.class);
   }

   @LauncherAPI
   public ClientProfile(HInput input, boolean ro) throws IOException {
      this(new BlockConfigEntry(input, ro));
   }

   @LauncherAPI
   public String getAssetIndex() {
      return (String)this.assetIndex.getValue();
   }

   @LauncherAPI
   public FileNameMatcher getAssetUpdateMatcher() {
      return this.getVersion().compareTo(ClientProfile.Version.MC1710) >= 0 ? ASSET_MATCHER : null;
   }

   @LauncherAPI
   public String[] getClassPath() {
      return (String[])this.classPath.stream(StringConfigEntry.class).toArray((x$0) -> {
         return new String[x$0];
      });
   }

   @LauncherAPI
   public String[] getClientArgs() {
      return (String[])this.clientArgs.stream(StringConfigEntry.class).toArray((x$0) -> {
         return new String[x$0];
      });
   }

   @LauncherAPI
   public String[] getJvmArgs() {
      return (String[])this.jvmArgs.stream(StringConfigEntry.class).toArray((x$0) -> {
         return new String[x$0];
      });
   }

   @LauncherAPI
   public String getMainClass() {
      return (String)this.mainClass.getValue();
   }

   @LauncherAPI
   public String getServerAddress() {
      return (String)this.serverAddress.getValue();
   }

   @LauncherAPI
   public int getServerPort() {
      return ((Integer)this.serverPort.getValue()).intValue();
   }

   @LauncherAPI
   public InetSocketAddress getServerSocketAddress() {
      return InetSocketAddress.createUnresolved(this.getServerAddress(), this.getServerPort());
   }

   @LauncherAPI
   public int getSortIndex() {
      return ((Integer)this.sortIndex.getValue()).intValue();
   }

   @LauncherAPI
   public String getTitle() {
      return (String)this.title.getValue();
   }

   @LauncherAPI
   public FileNameMatcher getClientUpdateMatcher(boolean shaders, boolean opis) {
      String[] updateArray = (String[])this.update.stream(StringConfigEntry.class).toArray((x$0) -> {
         return new String[x$0];
      });
      String[] verifyArray = (String[])this.updateVerify.stream(StringConfigEntry.class).toArray((x$0) -> {
         return new String[x$0];
      });
      String[] exclusionsArray = (String[])this.updateExclusions.stream(StringConfigEntry.class).toArray((x$0) -> {
         return new String[x$0];
      });
      List exclusionslist = Arrays.asList(exclusionsArray);
      ArrayList updatableList = new ArrayList();
      updatableList.addAll(exclusionslist);
      if (shaders) {
         updatableList.add("mods/shaders\\.jar");
      }

      if (opis) {
         updatableList.add("mods/MobiusCore\\.jar");
         updatableList.add("mods/Opis\\.jar");
      }

      String[] exclusionsArrayNew = (String[])updatableList.toArray(new String[0]);
      return new FileNameMatcher(updateArray, verifyArray, exclusionsArrayNew);
   }

   @LauncherAPI
   public ClientProfile.Version getVersion() {
      return ClientProfile.Version.byName((String)this.version.getValue());
   }

   @LauncherAPI
   public void setTitle(String title) {
      this.title.setValue(title);
   }

   @LauncherAPI
   public void setVersion(ClientProfile.Version version) {
      this.version.setValue(version.name);
   }

   @LauncherAPI
   public void verify() {
      this.getVersion();
      IOHelper.verifyFileName(this.getAssetIndex());
      VerifyHelper.verify(this.getTitle(), VerifyHelper.NOT_EMPTY, "Profile title can't be empty");
      VerifyHelper.verify(this.getServerAddress(), VerifyHelper.NOT_EMPTY, "Server address can't be empty");
      VerifyHelper.verifyInt(this.getServerPort(), VerifyHelper.range(0, 65535), "Illegal server port: " + this.getServerPort());
      this.update.verifyOfType(ConfigEntry.Type.STRING);
      this.updateVerify.verifyOfType(ConfigEntry.Type.STRING);
      this.updateExclusions.verifyOfType(ConfigEntry.Type.STRING);
      this.jvmArgs.verifyOfType(ConfigEntry.Type.STRING);
      this.classPath.verifyOfType(ConfigEntry.Type.STRING);
      this.clientArgs.verifyOfType(ConfigEntry.Type.STRING);
      VerifyHelper.verify(this.getTitle(), VerifyHelper.NOT_EMPTY, "Main class can't be empty");
   }

   public int compareTo(ClientProfile o) {
      return Integer.compare(this.getSortIndex(), o.getSortIndex());
   }

   public String toString() {
      return (String)this.title.getValue();
   }

    @Override
    public int compareTo(Object o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

   @LauncherAPI
   public static enum Version {
      MC164("1.6.4", 78),
      MC172("1.7.2", 4),
      MC1710("1.7.10", 5),
      MC189("1.8.9", 47),
      MC194("1.9.4", 110),
      MC1102("1.10.2", 210);

      private static final Map VERSIONS = new HashMap(6);
      public final String name;
      public final int protocol;

      private Version(String name, int protocol) {
         this.name = name;
         this.protocol = protocol;
      }

      public String toString() {
         return "Minecraft " + this.name;
      }

      public static ClientProfile.Version byName(String name) {
         return (ClientProfile.Version)VerifyHelper.getMapValue(VERSIONS, name, String.format("Unknown client version: '%s'", name));
      }

      static {
         ClientProfile.Version[] versionsValues = values();

         for(ClientProfile.Version version : versionsValues) {
            VERSIONS.put(version.name, version);
         }

      }
   }
}
