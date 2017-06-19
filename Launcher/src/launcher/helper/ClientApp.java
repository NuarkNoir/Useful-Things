package launcher.helper;

public class ClientApp implements Runnable {
   private static long nextTime = 0L;
   private static ClientApp clientApp = null;
   public String username;
   public boolean alreadyExecuted;

   public static void ClientAppmain(String login) throws InterruptedException {
      clientApp = new ClientApp();
      clientApp.username = login;
      Thread thread = new Thread(clientApp);
      thread.start();
   }

   public void run() {
      // $FF: Couldn't be decompiled
   }
}
