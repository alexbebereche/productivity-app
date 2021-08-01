package ro.ase.csie.licenta.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.dialogflow.v2.DetectIntentResponse;
import com.google.cloud.dialogflow.v2.QueryInput;
import com.google.cloud.dialogflow.v2.SessionName;
import com.google.cloud.dialogflow.v2.SessionsClient;
import com.google.cloud.dialogflow.v2.SessionsSettings;
import com.google.cloud.dialogflow.v2.TextInput;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.UUID;

import ro.ase.csie.licenta.R;
import ro.ase.csie.licenta.classes.Priority;
import ro.ase.csie.licenta.classes.helpers.AssistantReply;
import ro.ase.csie.licenta.classes.helpers.SendMessageAsync;

public class AddEditTaskActivity extends AppCompatActivity implements AssistantReply {
    public static final String TASK_ID = "taskId";
    public static final String TASK_NAME = "taskName";
    public static final String TASK_PRIORITY = "taskPriority";
    public static final String TASK_NOOFPOMODOROS = "noOfPomodoros";
    public static final String TASK_HAS_TIMER = "hasTimer";

    Spinner spinner;
    EditText edName;
    EditText edNoOfPomodoros;
    Button btnAddTask;
    CheckBox checkBox;

    int priority;

    // bot
    TextView tvSquareBot;
    ImageView botImage;
    ImageView imgInfo;
    ImageView imgInfoPrio;
    private CardView cvBot;


    //dialogflow
    private SessionsClient sessionsClient;
    private SessionName sessionName;
    private final String uuid = UUID.randomUUID().toString();

    Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        getSupportActionBar().hide();

        edName = findViewById(R.id.edName);
        edNoOfPomodoros = findViewById(R.id.edNoOfPomodoros);
        spinner = findViewById(R.id.prioritySpinner);
        btnAddTask = findViewById(R.id.btnAddTask);
        checkBox = findViewById(R.id.cbHasTimer);
        cvBot = findViewById(R.id.botCard);

        tvSquareBot = findViewById(R.id.tvBot);
        botImage = findViewById(R.id.botImage);
        imgInfo = findViewById(R.id.imgInfo);
        imgInfoPrio = findViewById(R.id.imgInfoPrio);
        dialog = new Dialog(this);


        botImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(AddEditTaskActivity.this);
                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.image_layout_dialog, null);
                builder.setView(dialogView)
                        .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).create().show();
            }
        });

        imgInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessageToBot("Pomodoro");
            }
        });

        imgInfoPrio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog();
            }
        });


        checkBox.setChecked(true);

        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkBox.isChecked()){
                    edNoOfPomodoros.setVisibility(View.VISIBLE);
                }
                else{
                    edNoOfPomodoros.setVisibility(View.INVISIBLE);
                }
            }
        });

        ArrayAdapter<Priority> adapter = new ArrayAdapter<Priority>(this, android.R.layout.simple_spinner_item, Priority.values());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch(position){
                    case 0:
                        priority = 1;
                        break;
                    case 1:
                        priority = 2;
                        break;
                    case 2:
                        priority = 3;
                        break;
                    case 3:
                        priority = 4;
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }


        });

        Intent intent = getIntent();

        if(intent.hasExtra(TASK_ID)){
            edName.setText(intent.getStringExtra(TASK_NAME));
            checkBox.setChecked(intent.getBooleanExtra(TASK_HAS_TIMER, false));

            if(intent.getBooleanExtra(TASK_HAS_TIMER, false)){
                edNoOfPomodoros.setText(String.valueOf(intent.getIntExtra(TASK_NOOFPOMODOROS, -1)));
            }
            else{
                edNoOfPomodoros.setVisibility(View.INVISIBLE);
            }
            spinner.setSelection(intent.getIntExtra(TASK_PRIORITY, 0) - 1);
        }

        btnAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTask();
            }
        });

        cvBot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddEditTaskActivity.this, ChatBotActivity.class);
                startActivity(intent);
            }
        });

        setUpBot();
        sendMessageToBot("Hello!");
    }

    private void saveTask(){
        String name = edName.getText().toString();
        boolean hasTimer = checkBox.isChecked();

        Intent data = new Intent();

        data.putExtra(TASK_NAME, name);
        data.putExtra(TASK_PRIORITY, priority);
        data.putExtra(TASK_HAS_TIMER, hasTimer);

        if(!hasTimer){
            data.putExtra(TASK_NOOFPOMODOROS, 0);
        }

        int id = getIntent().getIntExtra(TASK_ID, -1); // no entry with id -1
        if(id != -1){
            data.putExtra(TASK_ID, id);
        }

        if(name.length() == 0){
            Toast.makeText(this, "Choose the name of the task!", Toast.LENGTH_SHORT).show();
        }
        else if(hasTimer && edNoOfPomodoros.getText().toString().isEmpty()){
            Toast.makeText(this, "Enter the duration of the task!", Toast.LENGTH_SHORT).show();
        }
        else if(hasTimer && Integer.parseInt(edNoOfPomodoros.getText().toString()) <= 0){
            Toast.makeText(this, "Choose a suitable length!", Toast.LENGTH_SHORT).show();
        }
        else if(hasTimer && Integer.parseInt(edNoOfPomodoros.getText().toString()) > 6){
            Toast.makeText(this, "The task is too long!", Toast.LENGTH_SHORT).show();
        }
        else{
            if(hasTimer){
                int noOfPomodoros = Integer.parseInt(edNoOfPomodoros.getText().toString());
                data.putExtra(TASK_NOOFPOMODOROS, noOfPomodoros);
            }

            String message = "";

            switch (priority){
                case 1:
                    break;
                case 2:
                    message = "Not a task you should focus on. You should try to do it another time. Do you still want to add it to your list?";
                    break;
                case 3:
                    message = "Not a task you should focus on. You should try to delegate it. Do you still want to add it to your list?";
                    break;
                case 4:
                    message = "Not a task you should focus on. You should do this type of task only in moderation. Do you still want to add it to your list?";
                    break;
            }


            if(priority != 1){
                if(priority == 2){
                    AlertDialog alertDialog = new AlertDialog.Builder(AddEditTaskActivity.this)
                            .setTitle("Not a high priority task")
                            .setMessage(message)
                            .setCancelable(false)
                            .setIcon(R.drawable.ic_chatbot)
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                    finish();
                                }
                            })
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    setResult(RESULT_OK, data);
                                    finish();
                                }
                            })
                            .setNeutralButton("Add to calendar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(Intent.ACTION_INSERT)
                                            .setData(CalendarContract.Events.CONTENT_URI)
                                            .putExtra(CalendarContract.Events.TITLE, edName.getText().toString());
                                    if (intent.resolveActivity(getPackageManager()) != null) {
                                        startActivity(intent);
                                    }
                                }
                            })
                            .create();

                    alertDialog.show();
                }
                else{
                    AlertDialog alertDialog = new AlertDialog.Builder(AddEditTaskActivity.this)
                            .setTitle("Not a high priority task")
                            .setMessage(message)
                            .setCancelable(false)
                            .setIcon(R.drawable.ic_chatbot)
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                    finish();
                                }
                            })
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    setResult(RESULT_OK, data);
                                    finish();
                                }
                            })
                            .create();

                    alertDialog.show();
                }

            }
            else{
                setResult(RESULT_OK, data);
                finish();
            }


        }
    }


    //dialogflow
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

    @Override
    public void callback(DetectIntentResponse returnResponse) {
        if (returnResponse != null) {
            String botReply = returnResponse.getQueryResult().getFulfillmentText(); // written response from bot

            if (!botReply.isEmpty()) {
                tvSquareBot.setText(botReply);
            } else {
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Connection failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendMessageToBot(String message) {
        QueryInput input = QueryInput.newBuilder()
                .setText(TextInput.newBuilder()
                        .setText(message).setLanguageCode("en-US")).build();

        new SendMessageAsync(this, sessionName, sessionsClient, input).execute();
    }

    private void openDialog(){
        dialog.setContentView(R.layout.layout_matrix_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        Button btnOk = dialog.findViewById(R.id.btnOk);

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }
}