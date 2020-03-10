package ir.barantelecom.downloader;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import kong.unirest.GetRequest;
import kong.unirest.HttpRequestWithBody;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;

public class PerceptionHttpDAO {
  public PerceptionHttpDAO() {
  }

  public static void main(String[] args) throws PerceptionCommunicationProblemException {
    String login = login("989126119588", "123456");
    String guid = getProfileList(login);
    List<ChannelDTO> channelList = getChannelList(login, guid);
    Iterator var4 = channelList.iterator();

    while(var4.hasNext()) {
      ChannelDTO channelDTO = (ChannelDTO)var4.next();
      System.out.println(channelDTO);
    }

  }

  public static String login(String cellphone, String password) throws PerceptionCommunicationProblemException {
    String body = String.format("{\"password\":\"%s\",\"username\":\"%s\",\"deviceName\":\"AIO Downloader\",\"locale\":\"fa-IR\"}", password, cellphone);
    String digest = calculateDigest(Config.SECRET, Config.API_VERSION, "json", Config.CLIENT_ID, "client/login", body);
    String url = String.format("%sCatherine/api/%s/json/%s/%s/client/login", Config.AIO_SERVER, Config.API_VERSION, Config.CLIENT_ID, digest);
    HttpResponse<JsonNode> response = ((HttpRequestWithBody)Unirest.post(url).header("Content-Type", "application/json")).body(body).asJson();
    if (response != null && response.getStatus() == 200) {
      String responseBody = ((JsonNode)response.getBody()).toString();
      JsonParser var7 = new JsonParser();
      JsonObject result = var7.parse(responseBody).getAsJsonObject();
      String key = null;
      if (result.has("key")) {
        key = result.get("key").getAsString().trim();
        return key;
      } else {
        throw new PerceptionCommunicationProblemException(result);
      }
    } else {
      throw new PerceptionCommunicationProblemException((JsonObject)null);
    }
  }

  public static String getProfileList(String userKey) {
    String digest = calculateDigest(Config.SECRET, Config.API_VERSION, "json", Config.CLIENT_ID, "client/profiles/list", "fa-IR" + userKey);
    String url = String.format("%sCatherine/api/%s/json/%s/%s/client/profiles/list", Config.AIO_SERVER, Config.API_VERSION, Config.CLIENT_ID, digest);
    HttpResponse<JsonNode> response = ((GetRequest)((GetRequest)((GetRequest)Unirest.get(url).header("Content-Type", "application/json")).queryString("sessionId", userKey)).queryString("locale", "fa-IR")).asJson();
    String responseBody = ((JsonNode)response.getBody()).toString();
    JsonParser p = new JsonParser();
    JsonObject result = p.parse(responseBody).getAsJsonObject();
    JsonArray profiles = result.getAsJsonArray("profiles");
    JsonObject profileObject = profiles.get(0).getAsJsonObject();
    return profileObject.has("guid") ? profileObject.get("guid").getAsString().trim() : null;
  }

  public static String getChannelUrl(String userKey, String profileId, int channel, long delay) {
    String body = String.format("{\"locale\":\"fa-IR\",\"protocols\":[\"HLS_SECURE\"],\"audioFormats\":[\"AAC\"],\"pictureTypes\":[\"_2D\"],\"sessionId\":\"%s\",\"profileGuid\":\"%s\",\"channelId\":%s,\"channelType\":\"TV\",\"playbackType\":\"%s\",\"delay\":%s}", userKey, profileId, channel, delay == 0L ? "LIVE" : "PLTV", delay);
    System.out.println("body = " + body);
    String digest = calculateDigest(Config.SECRET, Config.API_VERSION, "json", Config.CLIENT_ID, "client/channels/linear/getUrl", body);
    String url = String.format("%sCatherine/api/%s/json/%s/%s/client/channels/linear/getUrl", Config.AIO_SERVER, Config.API_VERSION, Config.CLIENT_ID, digest);
    HttpResponse<JsonNode> response = ((HttpRequestWithBody)Unirest.post(url).header("Content-Type", "application/json")).body(body).asJson();
    System.out.println(response.getStatus() + "-" + response.getStatusText() + "-" + response.getBody());
    String responseBody = ((JsonNode)response.getBody()).toString();
    JsonParser p = new JsonParser();
    JsonObject result = p.parse(responseBody).getAsJsonObject();
    return result.has("url") ? result.get("url").getAsString().trim() : null;
  }

  public static List<ChannelDTO> getChannelList(String userKey, String profileId) throws PerceptionCommunicationProblemException {
    String body = String.format("{\"locale\":\"en-GB\",\"protocols\":[\"HLS_SECURE\"],\"audioFormats\":[\"AAC\"],\"pictureTypes\":[\"_2D\"],\"sessionId\":\"%s\",\"profileGuid\":\"%s\",\"type\":\"TV\",\"imageInfo\":[{\"type\":\"DARK\",\"height\":35,\"width\":49},{\"type\":\"LIGHT\",\"height\":35,\"width\":49},{\"type\":\"LIGHT\",\"height\":85,\"width\":119},{\"type\":\"LIGHT\",\"height\":170,\"width\":238}]}", userKey, profileId);
    System.out.println("body = " + body);
    String digest = calculateDigest(Config.SECRET, Config.API_VERSION, "json", Config.CLIENT_ID, "client/channels/list", body);
    String url = String.format("%sCatherine/api/%s/json/%s/%s/client/channels/list", Config.AIO_SERVER, Config.API_VERSION, Config.CLIENT_ID, digest);
    HttpResponse<JsonNode> response = ((HttpRequestWithBody)Unirest.post(url).header("Content-Type", "application/json")).body(body).asJson();
    String responseBody = ((JsonNode)response.getBody()).toString();
    JsonParser p = new JsonParser();
    JsonObject result = p.parse(responseBody).getAsJsonObject();
    List<ChannelDTO> channelList = new ArrayList();
    if (!result.has("channels")) {
      System.out.println(result);
      throw new PerceptionCommunicationProblemException(result);
    } else {
      JsonArray channels = result.get("channels").getAsJsonArray();
      Iterator var11 = channels.iterator();

      while(var11.hasNext()) {
        JsonElement ch = (JsonElement)var11.next();
        channelList.add(new ChannelDTO(ch.getAsJsonObject()));
      }

      return (List)channelList.stream().sorted(Comparator.comparingInt((o) -> {
        return o.number;
      })).collect(Collectors.toList());
    }
  }

  private static JsonObject getPlatformInformation() {
    String digest = calculateDigest(Config.SECRET, Config.API_VERSION, "json", Config.CLIENT_ID, "client/getPlatformInformation", "111fa-IR");
    String url = String.format("%sCatherine/api/%s/json/%s/%s/client/getPlatformInformation", Config.AIO_SERVER, Config.API_VERSION, Config.CLIENT_ID, digest);
    GetRequest getRequest = (GetRequest)((GetRequest)((GetRequest)((GetRequest)((GetRequest)Unirest.get(url).header("Content-Type", "application/json")).queryString("appVersion", "1")).queryString("deviceHeight", "1")).queryString("deviceWidth", "1")).queryString("locale", "fa-IR");
    HttpResponse<String> response = getRequest.asString();
    System.out.println(response.getStatus() + "-" + response.getStatusText() + "-" + (String)response.getBody());
    JsonParser p = new JsonParser();
    return p.parse((String)response.getBody()).getAsJsonObject();
  }

  public static long getTimeDiffInMillis() {
    JsonObject platformInformation = getPlatformInformation();
    long currentTime = platformInformation.get("currentTime").getAsLong();
    Date date = new Date();
    System.out.println("date = " + date);
    return date.getTime() - currentTime;
  }

  private static String calculateDigest(String apiSecret, String apiVersion, String format, String clientId, String path, String paramStr) {
    String key = apiSecret + apiVersion + format + clientId + path + paramStr;
    MessageDigest digest = null;

    try {
      digest = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException var11) {
      var11.printStackTrace();
    }

    digest.update(key.getBytes());
    byte[] digestBytes = digest.digest();
    StringBuffer sb = new StringBuffer();

    for(int i = 0; i < digestBytes.length; ++i) {
      sb.append(Integer.toString((digestBytes[i] & 255) + 256, 16).substring(1));
    }

    System.out.println("key = " + key);
    return sb.toString();
  }
}
