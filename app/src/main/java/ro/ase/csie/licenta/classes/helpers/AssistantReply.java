package ro.ase.csie.licenta.classes.helpers;

import com.google.cloud.dialogflow.v2.DetectIntentResponse;

public interface AssistantReply {

  void callback(DetectIntentResponse returnResponse);
}