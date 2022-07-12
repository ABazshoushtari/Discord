package discord.client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileDownloader implements Runnable {
    // Fields:
    private final String username;
    private final ChatFileMessage downloadingFile;

    // Constructors:
    public FileDownloader(String username, ChatFileMessage downloadingFile) {
        this.username = username;
        this.downloadingFile = downloadingFile;
    }

    // Methods:
    @Override
    public void run() {
        makeDirectory("Downloads");
        makeDirectory("Downloads" + File.separator + username + "'s downloads");
        makeDirectory("Downloads" + File.separator + username + "'s downloads" + File.separator + "Files");
        String directory = "Downloads" + File.separator + username + "'s downloads" + File.separator + "Files";
        File file = new File(directory, downloadingFile.getMessage());  // getMessage returns file name
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            System.out.println("Download of the file started.");
            fileOutputStream.write(downloadingFile.getBytes());
            fileOutputStream.flush();
            System.out.println("Download finished. the file saved in " + directory + File.separator + downloadingFile.getMessage());  // getMessage returns file name
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void makeDirectory(String path) {
        File directory = new File(path);
        if (!directory.exists()) {
            if (!directory.mkdir()) {
                System.out.println("Could not create the " + path + " directory!");
                throw new RuntimeException();
            }
        }
    }
}
