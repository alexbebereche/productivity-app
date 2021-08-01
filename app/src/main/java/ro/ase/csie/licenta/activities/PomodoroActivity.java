package ro.ase.csie.licenta.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.app.Notification;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.material.navigation.NavigationView;

import java.util.Locale;

import ro.ase.csie.licenta.R;
import ro.ase.csie.licenta.classes.entities.Task;
import ro.ase.csie.licenta.viewmodels.TaskViewModel;

import static ro.ase.csie.licenta.activities.MainActivity.URL_VALUE;

public class PomodoroActivity extends AppCompatActivity {

    public static final String CHANNEL_1_ID = "channel1";

    public static final long TWENTY_FIVE_MINUTES_IN_MILLIS = 1_500_000; //25 minutes
    public static final long FIVE_MINUTES_IN_MILLIS_TMP = 300_000; // 5 minutes

//    public static final long FIVE_MINUTES_IN_MILLIS_TMP = 3000;
//    public static final long TWENTY_FIVE_MINUTES_IN_MILLIS = 5000;

//    public static final long START_TIME_IN_MILLIS_BREAK = 12000;

    Button btnStart;
    Button btnStartBreak;
    Button btnReset;
    Button btnSkip;

    private CountDownTimer countDownTimer;
    private long timeLeftInMillis = TWENTY_FIVE_MINUTES_IN_MILLIS;
    TextView tvCountDown;
    TextView tvAction;

    private NotificationManagerCompat notificationManager;

    TaskViewModel taskViewModel;
    Task task = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pomodoro);

        getSupportActionBar().hide();

        findByIds();

        btnStartBreak.setVisibility(View.INVISIBLE);
        btnSkip.setVisibility(View.INVISIBLE);
        btnReset.setVisibility(View.INVISIBLE);

        notificationManager = NotificationManagerCompat.from(this);

        taskViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(this.getApplication())).get(TaskViewModel.class);

        Intent intent = getIntent();
        if(intent.getSerializableExtra(AddEditTaskActivity.TASK_NOOFPOMODOROS) != null){
            task = (Task) intent.getSerializableExtra(AddEditTaskActivity.TASK_NOOFPOMODOROS);
        }

        if(intent.hasExtra("isStarted")){
            startTimer(timeLeftInMillis);
            btnReset.setVisibility(View.VISIBLE);
        }

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTimer(timeLeftInMillis);
                btnReset.setVisibility(View.VISIBLE);
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog alertDialog = new AlertDialog.Builder(PomodoroActivity.this)
                        .setTitle("Reset")
                        .setMessage("Want to reset?")
                        .setCancelable(true)
                        .setIcon(R.drawable.ic_reset)
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                resetTimer();
                                btnReset.setVisibility(View.INVISIBLE);
                                btnStart.setVisibility(View.VISIBLE);
                            }
                        })
                        .create();

                alertDialog.show();


            }
        });

        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(task != null){
                    if(task.getNoOfPomodoros() == 0){
                        finish();
                    }
                }


                updateUI2Work();
                timeLeftInMillis = TWENTY_FIVE_MINUTES_IN_MILLIS;

            }
        });

        btnStartBreak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startBreakTimer();
            }
        });

        updateCountDownText(); // when we start, want to have time left reset
        //not sure about that here...
    }

    private void findByIds(){
        btnStart = findViewById(R.id.btnStart);
        btnStartBreak = findViewById(R.id.btnStartBreak);
        btnReset = findViewById(R.id.btnReset);
        btnSkip = findViewById(R.id.btnSkip);

        tvCountDown = findViewById(R.id.tvCountDown);
        tvAction = findViewById(R.id.tvAction);
    }

    private void startBreakTimer(){
        btnStartBreak.setVisibility(View.INVISIBLE);
        btnSkip.setVisibility(View.VISIBLE);

        countDownTimer = new CountDownTimer(FIVE_MINUTES_IN_MILLIS_TMP, 1000) { //1000ms = 1s

            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                //send notification - break time
                if(task != null){
                    if(task.getNoOfPomodoros() == 0){
                        finish();
                    }
                    else{
                        sendBreakFinished();
                        timeLeftInMillis = TWENTY_FIVE_MINUTES_IN_MILLIS;
                    }
                }
                else{
                    sendBreakFinished();
                    timeLeftInMillis = TWENTY_FIVE_MINUTES_IN_MILLIS;
                }


                //set UI
                tvCountDown.setText(R.string.twenty_five);
                btnStart.setVisibility(View.VISIBLE);
                btnStartBreak.setVisibility(View.INVISIBLE);
                btnSkip.setVisibility(View.INVISIBLE);
                tvAction.setText(R.string.work);
            }
        }.start();
    }

    private void startTimer(long duration){
        btnStart.setVisibility(View.INVISIBLE);
        countDownTimer = new CountDownTimer(duration, 1000) { //1000ms = 1s

            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                //send notification - break time
                sendWorkFinished();
                timeLeftInMillis = FIVE_MINUTES_IN_MILLIS_TMP;

                updateUI2Break();

                if(task != null){
                    task.setNoOfPomodoros(task.getNoOfPomodoros() - 1);

                    if(task.getNoOfPomodoros() == 0){
                        taskViewModel.delete(task);
                        Toast.makeText(PomodoroActivity.this,
                                "Congrats, you finished the task! You can still take a break",
                                Toast.LENGTH_SHORT).show();
                    }
                    else{
                        taskViewModel.update(task);
                    }
                }


            }
        }.start();


    }



    private void updateUI2Work(){
        //nu doar UI, tot ce e in finish...
        tvCountDown.setText("25:00");
        btnStart.setVisibility(View.VISIBLE);
        btnStart.setText("Start");
        tvAction.setText(R.string.work);

        btnSkip.setVisibility(View.INVISIBLE);
        pauseTimer();
    }


    private void pauseTimer(){
        timeLeftInMillis = TWENTY_FIVE_MINUTES_IN_MILLIS;
        countDownTimer.cancel();
    }

    private void updateUI2Break(){
        tvCountDown.setText(R.string.five_minutes);
        btnStart.setVisibility(View.INVISIBLE);
        btnStartBreak.setVisibility(View.VISIBLE);
        btnSkip.setVisibility(View.VISIBLE);
        tvAction.setText(R.string.break_time);
    }

    private void resetTimer(){
        timeLeftInMillis = TWENTY_FIVE_MINUTES_IN_MILLIS;
        countDownTimer.cancel();
        tvCountDown.setText(R.string.twenty_five);
    }

    private void updateCountDownText(){
        int minutes = (int)(timeLeftInMillis / 1000) / 60;
        int seconds = (int)(timeLeftInMillis / 1000) % 60;

        String timeLeft = String.format(Locale.getDefault(),"%02d:%02d", minutes, seconds);
        tvCountDown.setText(timeLeft);
    }

    public void sendWorkFinished() {

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_chatbot)
                .setContentTitle("Work session finished!")
                .setContentText("Go take a break")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .build();

        notificationManager.notify(1, notification);
    }

    public void sendBreakFinished() {

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_chatbot)
                .setContentTitle("Break finished")
                .setContentText("Go back to work!")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .build();

        notificationManager.notify(1, notification);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        timeLeftInMillis = TWENTY_FIVE_MINUTES_IN_MILLIS;

        if(countDownTimer != null){
            countDownTimer.cancel();
        }
    }


}