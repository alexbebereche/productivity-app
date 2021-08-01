package ro.ase.csie.licenta.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import ro.ase.csie.licenta.DB.dao.UserRepository;
import ro.ase.csie.licenta.R;
import ro.ase.csie.licenta.classes.entities.User;

public class SignInActivity extends AppCompatActivity {

    public static final String USER_EMAIL = "userEmail";

    SignInButton signIn;
    GoogleSignInClient mGoogleSignInClient;
    int RC_SIGN_IN = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        getSupportActionBar().hide();

        signIn = findViewById(R.id.btnSignIn);
        signIn.setSize(SignInButton.SIZE_STANDARD);
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getId() == R.id.btnSignIn){
                    signIn();
                }
            }
        });

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


    }

    @Override
    protected void onStart() {
        super.onStart();

        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if(account != null){
            Intent intent = new Intent(SignInActivity.this, MainActivity.class);  // SecondActivity.class, not . this

            intent.putExtra(USER_EMAIL, account.getEmail());

            startActivity(intent);
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
//            handleSignInResult(task);
            handleSignInResult();
        }
    }


    private void handleSignInResult() {
        try {

            GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
            if(acct != null){
                String personName = acct.getDisplayName();
                String email = acct.getEmail();
                Uri personPhoto = acct.getPhotoUrl();

                Intent intent = new Intent(SignInActivity.this, MainActivity.class);

                intent.putExtra("userName", personName);
                intent.putExtra("email", email);
                intent.putExtra("photo", personPhoto);

                User user = new User(email);

                UserRepository userRepository = new UserRepository(getApplication());
                userRepository.insert(user);

                intent.putExtra(USER_EMAIL, user.getEmail());

                startActivity(intent);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}