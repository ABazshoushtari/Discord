package discord.client;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;

public class DownloadableFile implements Serializable {
    // Fields:
    private String fileName;
    private byte[] bytes;

    // Constructors:
    public DownloadableFile(String fileName, FileInputStream fileInputStream) throws IOException {
        this.fileName = fileName;
        bytes = fileInputStream.readAllBytes();
    }

    public DownloadableFile(String fileName, byte[] bytes) {
        this.fileName = fileName;
        this.bytes = bytes;
    }

    // Getters:
    public String getFileName() {
        return fileName;
    }

    public byte[] getBytes() {
        return bytes;
    }

    // Setters:
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    // Other Methods:
    @Override
    public String toString() {
        return fileName;
    }
}
