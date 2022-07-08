package discord.client;

import java.io.Serializable;

public interface Asset extends Serializable {

    Integer getID();
    byte[] getAvatarImage();
    String getAvatarContentType();
}
