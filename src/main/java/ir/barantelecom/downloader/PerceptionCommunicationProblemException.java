package ir.barantelecom.downloader;

import com.google.gson.JsonObject;

public class PerceptionCommunicationProblemException extends Throwable {
  public PerceptionCommunicationProblemException(JsonObject result) {
    super(!result.has("errorDetails") ? "Perception communication problem" : result.getAsJsonObject("errorDetails").get("errorMessage").getAsString());
  }
}
