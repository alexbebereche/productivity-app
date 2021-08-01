package ro.ase.csie.licenta.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.SearchManager;
import android.app.TimePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.provider.CalendarContract;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.dialogflow.v2.DetectIntentResponse;
import com.google.cloud.dialogflow.v2.QueryInput;
import com.google.cloud.dialogflow.v2.SessionName;
import com.google.cloud.dialogflow.v2.SessionsClient;
import com.google.cloud.dialogflow.v2.SessionsSettings;
import com.google.cloud.dialogflow.v2.TextInput;

import ro.ase.csie.licenta.R;
import ro.ase.csie.licenta.adapters.ChatAdapter;
import ro.ase.csie.licenta.classes.bot.Message;
import ro.ase.csie.licenta.classes.helpers.AssistantReply;
import ro.ase.csie.licenta.classes.helpers.SendMessageAsync;
import ro.ase.csie.licenta.util.TimePickerFragment;


public class ChatBotActivity extends AppCompatActivity implements AssistantReply, TimePickerDialog.OnTimeSetListener {
    private RecyclerView rvChat;
    private EditText edUserMsg;

    private ChatAdapter chatAdapter;
    List<Message> messageList = new ArrayList<>();

    //dialogflow
    private SessionsClient sessionsClient;
    private SessionName sessionName;
    private final String uuid = UUID.randomUUID().toString();

    ImageView imgSend;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_bot);

        rvChat = findViewById(R.id.rvChat);
        edUserMsg = findViewById(R.id.edMsg);
        imgSend = findViewById(R.id.imgSend);
        FloatingActionButton fabSendMsg = findViewById(R.id.fabSendMsg);

        chatAdapter = new ChatAdapter(messageList, this);

        LinearLayoutManager manager = new LinearLayoutManager(this);
        rvChat.setLayoutManager(manager);
        rvChat.setAdapter(chatAdapter);

        messageList.add(new Message("Hello, I can help you with the following:\n-alarm\n-timer\n-emails\n-calendar\n-Google search", true));

        fabSendMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = edUserMsg.getText().toString();
                if (!message.isEmpty()) {
                    messageList.add(new Message(message, false));
                    edUserMsg.setText("");
                    sendMessageToBot(message);
                    Objects.requireNonNull(rvChat.getAdapter()).notifyDataSetChanged();
                    Objects.requireNonNull(rvChat.getLayoutManager())
                            .scrollToPosition(messageList.size() - 1);
                } else {
                    Toast.makeText(ChatBotActivity.this, "Please enter a message!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        setUpBot();
    }

    private void setUpBot() {
        try {
            InputStream stream = this.getResources().openRawResource(R.raw.credentialbun);
            GoogleCredentials credentials = GoogleCredentials.fromStream(stream)
                    .createScoped(Arrays.asList("https://www.googleapis.com/auth/cloud-platform"));
            String projectId = ((ServiceAccountCredentials) credentials).getProjectId();

            SessionsSettings.Builder settingsBuilder = SessionsSettings.newBuilder();
            SessionsSettings sessionsSettings = settingsBuilder.setCredentialsProvider(
                    FixedCredentialsProvider.create(credentials)).build();

            sessionsClient = SessionsClient.create(sessionsSettings);
            sessionName = SessionName.of(projectId, uuid);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMessageToBot(String message) {
        QueryInput input = QueryInput.newBuilder()
                .setText(TextInput.newBuilder()
                        .setText(message).setLanguageCode("en-US")).build();

        new SendMessageAsync(this, sessionName, sessionsClient, input).execute();
    }

    @Override
    public void callback(DetectIntentResponse returnResponse) {
        if (returnResponse != null) {
            String assistantReply = returnResponse.getQueryResult().getFulfillmentText(); // written response from bot


            if (!assistantReply.isEmpty()) {
                if(assistantReply.contains("https")){
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(assistantReply));
                    startActivity(intent);
                }


                messageList.add(new Message(assistantReply, true));
                chatAdapter.notifyDataSetChanged();
                Objects.requireNonNull(rvChat.getLayoutManager()).scrollToPosition(messageList.size() - 1); // scroll to last poition

                if(assistantReply.equals("Starting  a Pomodoro timer for you.")){
                    Intent intent = new Intent(ChatBotActivity.this, PomodoroActivity.class);
                    intent.putExtra("isStarted", true);
                    startActivity(intent);
                }
                else if(assistantReply.equals("Let's set an alarm...")){

                    DialogFragment timePicker = new TimePickerFragment();
                    timePicker.show(getSupportFragmentManager(), "Choose a time");
                }
                else if(assistantReply.startsWith("Adding an alarm for")){

                    int index = assistantReply.indexOf(":");
                    int hour = Integer.parseInt(assistantReply.substring(index - 2, index));
                    int minute = Integer.parseInt(assistantReply.substring(index + 1, index + 3));
                    Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM)
                            .putExtra(AlarmClock.EXTRA_HOUR, hour)
                            .putExtra(AlarmClock.EXTRA_MINUTES, minute)
                            .putExtra(AlarmClock.EXTRA_SKIP_UI, true);
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }
                }
                else if(assistantReply.endsWith("to calendar")){

                    String toSearch = assistantReply.substring("Adding".length() + 1, assistantReply.length() - "to calendar".length());

                    Intent intent = new Intent(Intent.ACTION_INSERT)
                            .setData(CalendarContract.Events.CONTENT_URI)
                            .putExtra(CalendarContract.Events.TITLE, toSearch);
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }
                }
                else if(assistantReply.toLowerCase().startsWith("searching")){
                    String toSearch = assistantReply.substring("searching".length()).trim();
                    searchOnInternet(toSearch);
                }
                else if(assistantReply.equals("Opening zoom...")){
                    launchZoomClient();
                }
                else if(assistantReply.equals("Opening email...")){
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_APP_EMAIL);
                    startActivity(intent);
                }
                else if(assistantReply.startsWith("Sending email to")){
                    String[] words = assistantReply.split("\\s");

                    ArrayList<String> emails = new ArrayList<>();

                    for(String email : words){
                        if(email.contains("@")){
                            emails.add(email);
                        }
                    }

                    String[] to = new String[emails.size()];
                    for(int i = 0; i < emails.size(); i++){
                        to[i] = emails.get(i);
                    }

                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.putExtra(Intent.EXTRA_EMAIL, to);

                    intent.setType("message/rfc822");
                    startActivity(Intent.createChooser(intent, "Choose an email client"));
                }
                else if(assistantReply.equals("Let's search on Google")){
                    searchOnInternet(messageList.get(messageList.size() - 2).getMessage());
                }
                else if(assistantReply.startsWith("I will start a timer for")){
                    String stringNumber = extractInt(assistantReply);

                    int nr = Integer.parseInt(stringNumber);
                    int numberInSeconds = 0;


                    if(assistantReply.substring("I will start a timer for".length() + 1).contains("s")){
                        numberInSeconds = nr;
                    }
                    else if(assistantReply.substring("I will start a timer for".length() + 1).contains("min")){
                        numberInSeconds = nr * 60;
                    }
                    else if(assistantReply.substring("I will start a timer for".length() + 1).contains("h")){
                        numberInSeconds = nr * 60 * 60;
                    }

                    Intent intent = new Intent(AlarmClock.ACTION_SET_TIMER)
                            .putExtra(AlarmClock.EXTRA_LENGTH, numberInSeconds)
                            .putExtra(AlarmClock.EXTRA_SKIP_UI, true);
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }

                }
            } else {
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Connection failed", Toast.LENGTH_SHORT).show();
        }
    }



    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM)
                .putExtra(AlarmClock.EXTRA_HOUR, hourOfDay)
                .putExtra(AlarmClock.EXTRA_MINUTES, minute)
                .putExtra(AlarmClock.EXTRA_SKIP_UI, true);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private void searchOnInternet(String sentence){
        try{
            Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
            intent.putExtra(SearchManager.QUERY, sentence);
            startActivity(intent);
        }
        catch(ActivityNotFoundException e){
            searchOnInternetCompat(sentence);
        }

    }

    private void searchOnInternetCompat(String sentence){
        try{
            Uri uri = Uri.parse("https://www.google.com/#q=" + sentence);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
        catch(ActivityNotFoundException e){
            Toast.makeText(this, "error", Toast.LENGTH_SHORT).show();
        }
    }

    private void launchZoomClient() {
        PackageManager pm = getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage("us.zoom.videomeetings");
        if (intent != null) {
            startActivity(intent);
        }
    }

    private String extractInt(String sentence)
    {
        sentence = sentence.replaceAll("[^\\d]", " ");
        sentence = sentence.trim();
        sentence = sentence.replaceAll(" +", " ");
        if (sentence.equals(""))
            return "-1";

        return sentence;
    }
}