package discord.client;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HttpDownloader implements Runnable {
    // Fields:
    private final URL url;
    private final String fileName;
    private final String username;
    //private final View printer;

    // Constructors:
    public HttpDownloader(String username, URL url, String fileName/*, View printer*/) {
        this.url = url;
        this.username = username;
        this.fileName = fileName;
        //this.printer = printer;
    }

    public HttpDownloader(String username, String url, String fileName/*, View printer*/) throws MalformedURLException {
        this.url = new URL(url);
        this.username = username;
        this.fileName = fileName;
        //this.printer = printer;
    }

    // Methods:
    @Override
    public void run() {
        HttpURLConnection connection;
        try {
            if (url.getProtocol().equals("http")) {
                connection = (HttpURLConnection) url.openConnection();
            } else if (url.getProtocol().equals("https")) {
                connection = (HttpsURLConnection) url.openConnection();
            } else {
                //printer.printErrorMessage("UNSUPPORTED PROTOCOL!");
                return;
            }

            connection.connect();
            if (connection.getResponseCode() / 100 != 2) {  // check if response code is in the 200 range or not
                throw new IOException(connection.getResponseCode() + connection.getResponseMessage());
            }
        } catch (IOException e) {
            //printer.printErrorMessage("Failed to get the content! " + e);
            return;
        }

        long contentLength = connection.getContentLengthLong();
        //printer.println("Content Length = " + contentLength + " bytes");
        makeDirectory("Downloads");
        makeDirectory("Downloads" + File.separator + username + "'s downloads");
        makeDirectory("Downloads" + File.separator + username + "'s downloads" + File.separator + "URLs");
        String directory = "Downloads" + File.separator + username + "'s downloads" + File.separator + "URLs" + File.separator;
        File file = new File(directory + fileName);
        try (FileOutputStream fileOutputStream = new FileOutputStream(file);
             InputStream inputStream = connection.getInputStream()) {

            //printer.println("Download file from the URL started.");
            int totalRead = 0;
            byte[] buffer = new byte[1048576];  // size of buffer: 1MB
            while (totalRead < contentLength) {
                int read = inputStream.read(buffer);
                if (read == -1) {
                    break;
                }
                totalRead += read;
                fileOutputStream.write(buffer, 0, read);
            }
            //printer.println("Download finished. the file saved in " + directory + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void makeDirectory(String path) {
        File directory = new File(path);
        if (!directory.exists()) {
            if (!directory.mkdir()) {
                //printer.printErrorMessage("Could not create the " + path + " directory!");
                throw new RuntimeException();
            }
        }
    }
}
