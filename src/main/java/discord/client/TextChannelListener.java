package discord.client;

import discord.signals.DBConnectFailSignal;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TextChannelListener implements Runnable {

    private final Controller clientController;

    public TextChannelListener(Controller clientController) {
        this.clientController = clientController;
    }

    @Override
    public void run() {

        Model user = clientController.getUser();
        MySocket mySocket = clientController.getMySocket();
        //View printer = clientController.getPrinter();
        ExecutorService executorService = Executors.newCachedThreadPool();
        Object inObject;
        while (mySocket.isConnected()) {
            try {
                inObject = mySocket.read();
                if (inObject instanceof DBConnectFailSignal) {
                    //printer.printErrorMessage("db");
                    synchronized (user.getUsername()) {  // should it be user or user.getUsername() ??????????????????????????
                        user.getUsername().notify();
                    }
                    break;
                } else if (inObject instanceof String) {    // The String signals are the messages from the friend
                    //printer.println((String) inObject);
                } else if (inObject instanceof TextChannelMessage) {
                    //printer.println(((TextChannelMessage) inObject).getMessage());
                } else if (inObject instanceof DownloadURL downloadUrl) {
                    executorService.execute(new HttpDownloader(user.getUsername(), downloadUrl.getUrl(), downloadUrl.getFileName()/*, printer*/));
                } else if (inObject instanceof DownloadableFile downloadingFile) {
                    executorService.execute(new FileDownloader(user.getUsername(), downloadingFile/*, printer*/));
                } else if (inObject instanceof Model) {
                    synchronized (user.getUsername()) {  // should it be user or user.getUsername() ??????????????????????????
                        user.getUsername().notify();
                        //user = (Model) inObject;
                    }
                    break;
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                mySocket.closeEverything();
                break;
            }
        }
        //printer.printSuccessMessage("exit");
    }
}
