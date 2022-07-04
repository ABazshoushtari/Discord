package discord.client;

import java.io.Serializable;
import java.net.URL;

public class DownloadURL implements Serializable {
    // Fields:
    private String fileName;
    private URL url;

    // Constructors:
    public DownloadURL(String fileName, URL url) {
        this.fileName = fileName;
        this.url = url;
    }

    // Getters:
    public String getFileName() {
        return fileName;
    }

    public URL getUrl() {
        return url;
    }

    // Setters:
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setUrl(URL url) {
        this.url = url;
    }
}
