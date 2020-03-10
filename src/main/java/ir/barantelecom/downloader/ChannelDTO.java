package ir.barantelecom.downloader;

import com.google.gson.JsonObject;

public class ChannelDTO {
  public Integer id;
  public String name;
  public Integer number;

  public ChannelDTO(JsonObject channelObj) {
    if (channelObj.has("id")) {
      this.id = channelObj.get("id").getAsInt();
    }

    if (channelObj.has("name")) {
      this.name = channelObj.get("name").getAsString();
    }

    if (channelObj.has("number")) {
      this.number = channelObj.get("number").getAsInt();
    }

  }

  public String toString() {
    return this.name;
  }
}
