package ro.ase.csie.licenta.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task; // needed for google auth
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.dialogflow.v2.DetectIntentResponse;
import com.google.cloud.dialogflow.v2.QueryInput;
import com.google.cloud.dialogflow.v2.SessionName;
import com.google.cloud.dialogflow.v2.SessionsClient;
import com.google.cloud.dialogflow.v2.SessionsSettings;
import com.google.cloud.dialogflow.v2.TextInput;
import com.makeramen.roundedimageview.RoundedImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import ro.ase.csie.licenta.R;
import ro.ase.csie.licenta.adapters.TaskAdapter;
import ro.ase.csie.licenta.classes.entities.User;
import ro.ase.csie.licenta.classes.helpers.AssistantReply;
import ro.ase.csie.licenta.classes.helpers.SendMessageAsync;
import ro.ase.csie.licenta.viewmodels.TaskViewModel;

public class MainActivity extends AppCompatActivity implements AssistantReply {

    public static final int NEW_TASK = 1000;
    public static final int EDIT_TASK = 1001;
    public static final int POMODORO_START = 1002;


    public static final String URL_VALUE = "urlValue";

    Uri photo;
    
    private AppBarConfiguration mAppBarConfiguration;

    //menu - navigation
    private TextView tvUserName;
    private TextView tvEmail;
    private TextView tvBot;
    private RoundedImageView imgProfile;

    //sign out
    private GoogleSignInClient mGoogleSignInClient;

    //ViewModel
    public TaskViewModel taskViewModel;

    public TaskAdapter taskAdapter;

    //user - db
//    User user = null;
    String user;
    String userName = "";

    //dialogflow
    private SessionsClient sessionsClient;
    private SessionName sessionName;
    private final String uuid = UUID.randomUUID().toString();

    private CardView cvMain;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_pomodoro, R.id.nav_logOut) //am sters astea...aici era pb
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        //get email
        if(getIntent().getSerializableExtra(SignInActivity.USER_EMAIL) != null){
            user = getIntent().getStringExtra(SignInActivity.USER_EMAIL);
        }


        //controls
        View headerView = navigationView.getHeaderView(0);
        tvUserName = headerView.findViewById(R.id.tvGoogleName);
        tvEmail = headerView.findViewById(R.id.tvEmail);
        imgProfile = headerView.findViewById(R.id.imgProfile);
        FloatingActionButton fabAddTask = findViewById(R.id.fabAddTask);
        cvMain = findViewById(R.id.cvMain);




        //google
        setGoogleInfo();

        tvBot = findViewById(R.id.tvBot);


        RecyclerView rvTasks = findViewById(R.id.rvTasks);
        rvTasks.setLayoutManager(new LinearLayoutManager(this));
        //+
        rvTasks.setHasFixedSize(true); // know the rv size dont change...
        taskAdapter = new TaskAdapter();
        rvTasks.setAdapter(taskAdapter);

        //from comment
        taskViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(this.getApplication())).get(TaskViewModel.class);
        taskViewModel.getAllTasksForUser(user).observe(this, new Observer<List<ro.ase.csie.licenta.classes.entities.Task>>() {
            @Override
            public void onChanged(List<ro.ase.csie.licenta.classes.entities.Task> tasks) {
                //update recyclerView
                taskAdapter.submitList(tasks);
            }
        });

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                if (direction == ItemTouchHelper.LEFT) {

                    AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Delete")
                            .setMessage("Are you sure you want to delete?")
                            .setCancelable(false)
                            .setIcon(R.drawable.ic_delete)
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    taskAdapter.notifyItemChanged(viewHolder.getAdapterPosition());
                                    dialog.cancel();
                                }
                            })
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    taskViewModel.delete(taskAdapter.getTaskAtPosition(viewHolder.getAdapterPosition()));//this
                                    Toast.makeText(MainActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .create();

                    alertDialog.show();
                }
                if(direction == ItemTouchHelper.RIGHT){
                    ro.ase.csie.licenta.classes.entities.Task task = taskAdapter.getTaskAtPosition(viewHolder.getAdapterPosition());
                    Intent intent = new Intent(MainActivity.this, AddEditTaskActivity.class);

                    intent.putExtra(AddEditTaskActivity.TASK_ID, task.getId());
                    intent.putExtra(AddEditTaskActivity.TASK_NAME, task.getName());
                    intent.putExtra(AddEditTaskActivity.TASK_PRIORITY, task.getPriority());
                    intent.putExtra(AddEditTaskActivity.TASK_NOOFPOMODOROS, task.getNoOfPomodoros());
                    intent.putExtra(AddEditTaskActivity.TASK_HAS_TIMER, task.hasTimer());

                    startActivityForResult(intent, EDIT_TASK);

                    taskAdapter.notifyItemChanged(viewHolder.getAdapterPosition());
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

                Drawable icon;
                ColorDrawable background;

                View itemView = viewHolder.itemView;
                int bgCornerOffset = 20;


                if(dX > 0){
                    //swiping to the left
                    icon = ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_edit);
                    background = new ColorDrawable(Color.BLUE);
                }
                else{
                    icon = ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_delete);
                    background = new ColorDrawable(Color.RED);
                }

                int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
                int iconTop = itemView.getTop() + (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
                int iconBottom = iconTop + icon.getIntrinsicHeight();

                if(dX > 0){
                    int iconLeft = itemView.getLeft() + iconMargin;
                    int iconRight = itemView.getLeft() + iconMargin + icon.getIntrinsicWidth();
                    icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

                    background.setBounds(itemView.getLeft(), itemView.getTop(), itemView.getLeft() + (int)dX + bgCornerOffset, itemView.getBottom());
                }
                else if(dX < 0){
                    int iconLeft = itemView.getRight() - iconMargin - icon.getIntrinsicWidth();
                    int iconRight = itemView.getRight() - iconMargin;
                    icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

                    background.setBounds(itemView.getRight() + (int)dX - bgCornerOffset, itemView.getTop(),
                            itemView.getRight(), itemView.getBottom());
                }
                else{
                    background.setBounds(0, 0, 0, 0);
                }

                background.draw(c);
                icon.draw(c);
            }
        }).attachToRecyclerView(rvTasks);


        taskAdapter.setOnItemClickListener(new TaskAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(ro.ase.csie.licenta.classes.entities.Task task) {
                Intent intent = new Intent(MainActivity.this, PomodoroActivity.class);

                if(task.getName().contains("zoom")){

                    AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Task completion")
                            .setMessage("Did you complete your task?")
                            .setCancelable(false)
                            .setIcon(R.drawable.ic_chatbot)
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            })
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    taskViewModel.delete(task);
                                }
                            })
                            .setNeutralButton("Open Zoom", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    PackageManager pm = getPackageManager();
                                    Intent i = pm.getLaunchIntentForPackage("us.zoom.videomeetings");
                                    if (i != null) {
                                        startActivity(i);
                                    }
                                }
                            })
                            .create();

                    alertDialog.show();


                }
                else if(task.getName().contains("email") || task.getName().contains("gmail")){

                    AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Task completion")
                            .setMessage("Did you complete your task?")
                            .setCancelable(false)
                            .setIcon(R.drawable.ic_chatbot)
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            })
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    taskViewModel.delete(task);
                                }
                            })
                            .setNeutralButton("Open email app", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent1 = new Intent(Intent.ACTION_MAIN);
                                    intent1.addCategory(Intent.CATEGORY_APP_EMAIL);
                                    startActivity(intent1);
                                }
                            })
                            .create();

                    alertDialog.show();
                }
                else{
                    if(!task.hasTimer()){
                        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Task completion")
                                .setMessage("Did you complete your task?")
                                .setCancelable(false)
                                .setIcon(R.drawable.ic_chatbot)
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                })
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        taskViewModel.delete(task);
                                    }
                                })
                                .create();

                        alertDialog.show();
                    }
                    else{
                        switch (task.getPriority()){
                            case 1:
                                intent.putExtra(AddEditTaskActivity.TASK_NOOFPOMODOROS, task);
                                startActivityForResult(intent, POMODORO_START);
                                break;
                            case 2:
                                intent.putExtra(AddEditTaskActivity.TASK_NOOFPOMODOROS, task);
                                startActivityForResult(intent, POMODORO_START);
                                break;
                            case 3:
                                intent.putExtra(AddEditTaskActivity.TASK_NOOFPOMODOROS, task);
                                startActivityForResult(intent, POMODORO_START);
                                break;
                            case 4:
                                intent.putExtra(AddEditTaskActivity.TASK_NOOFPOMODOROS, task);
                                startActivityForResult(intent, POMODORO_START);
                                break;

                        }
                    }
                }
            }
        });


        //google
//        setGoogleInfo();

        //sign out
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        fabAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddEditTaskActivity.class);
                startActivityForResult(intent, NEW_TASK);
            }
        });



        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if(item.getItemId() == R.id.nav_pomodoro){
                    Intent intent = new Intent(MainActivity.this, PomodoroActivity.class);
                    startActivity(intent);
                }
                else if(item.getItemId() == R.id.nav_logOut){
                    signOut();
                    Intent outIntent = new Intent(MainActivity.this, SignInActivity.class);
                    startActivity(outIntent);
                }
                else if(item.getItemId() == R.id.nav_chatbot){
                    Intent intent = new Intent(MainActivity.this, ChatBotActivity.class);
                    intent.putExtra(URL_VALUE, photo.toString());
                    startActivity(intent);
                }

                return false;
            }
        });

        setUpBot(); // forgot to call...
        sendMessageToBot("Hi");

        cvMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ChatBotActivity.class);
                startActivity(intent);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.deleteAllTasks){
            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Delete")
                    .setMessage("Are you sure you want to delete?")
                    .setCancelable(true)
                    .setIcon(R.drawable.ic_delete)
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            taskViewModel.deleteAllTasksForUser(user);
                        }
                    })
                    .create();

            alertDialog.show();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                    }
                });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == NEW_TASK && resultCode == RESULT_OK && data != null){
            String name = data.getStringExtra(AddEditTaskActivity.TASK_NAME);
            int noOfPomodoros = data.getIntExtra(AddEditTaskActivity.TASK_NOOFPOMODOROS, 0);
            int priority = data.getIntExtra(AddEditTaskActivity.TASK_PRIORITY, 0);
            boolean hasTimer = data.getBooleanExtra(AddEditTaskActivity.TASK_HAS_TIMER, false);

            ro.ase.csie.licenta.classes.entities.Task task = new ro.ase.csie.licenta.classes.entities.Task(name, priority, hasTimer, noOfPomodoros, user);

            taskViewModel.insert(task);

        }
        else if(requestCode == EDIT_TASK && resultCode == RESULT_OK && data != null){
            int id = data.getIntExtra(AddEditTaskActivity.TASK_ID, -1);

            if(id == -1){
                Toast.makeText(this, "Task can't be updated", Toast.LENGTH_SHORT).show();
                return;
            }

            String name = data.getStringExtra(AddEditTaskActivity.TASK_NAME);
            int noOfPomodoros = data.getIntExtra(AddEditTaskActivity.TASK_NOOFPOMODOROS, 0);
            int priority = data.getIntExtra(AddEditTaskActivity.TASK_PRIORITY, 0);
            boolean hasTimer = data.getBooleanExtra(AddEditTaskActivity.TASK_HAS_TIMER, false);

            ro.ase.csie.licenta.classes.entities.Task task = new ro.ase.csie.licenta.classes.entities.Task(name, priority, hasTimer, noOfPomodoros, user);
            task.setId(id); //dont put this line, the update wont happen, room can't identify the entry

            taskViewModel.update(task);

            Toast.makeText(this, "Task updated", Toast.LENGTH_SHORT).show();
        }
    }

    private void setGoogleInfo(){
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        if(acct != null){
            userName = acct.getDisplayName();
            String email = acct.getEmail();
//            Uri photo = acct.getPhotoUrl();
            photo = acct.getPhotoUrl();

            tvUserName.setText(userName);
            tvEmail.setText(email);

            Glide.with(this).load(String.valueOf(photo)).into(imgProfile);
        }
    }


    @Override
    public void onBackPressed() {
        // don't want to be able to press back here
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
                if(botReply.equals("Opening zoom...")){
                    PackageManager pm = getPackageManager();
                    Intent i = pm.getLaunchIntentForPackage("us.zoom.videomeetings");
                    if (i != null) {
                        startActivity(i);
                    }
                }
                tvBot.setText(botReply);
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

    @Override
    protected void onResume() {
        super.onResume();
    }
}