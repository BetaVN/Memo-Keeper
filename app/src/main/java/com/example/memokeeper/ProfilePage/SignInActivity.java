package com.example.memokeeper.ProfilePage;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Toast;

import com.example.memokeeper.Constants.REQUEST_CODE;
import com.example.memokeeper.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class SignInActivity extends AppCompatActivity {

    private SignInButton googleSignIn;
    private GoogleSignInAccount user = null;
    private Context context = this;
    private MenuInflater inflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        googleSignIn = findViewById(R.id.sign_in_button);
        Toolbar signInToolbar = findViewById(R.id.sign_in_toolbar);

        setSupportActionBar(signInToolbar);
        getSupportActionBar().setTitle("Memo Keeper");

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        final GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(context, googleSignInOptions);

        googleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = googleSignInClient.getSignInIntent();
                startActivityForResult(intent, REQUEST_CODE.GOOGLE_SIGN_IN);
            }
        });
    }

    @Override
    protected void onActivityResult(int RequestCode, int ResultCode, Intent data) {
        if (RequestCode == REQUEST_CODE.GOOGLE_SIGN_IN) {
            if (ResultCode == RESULT_OK) {
                try {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    user = task.getResult(ApiException.class);
                    Toast.makeText(context, "Google account signed in successfully", Toast.LENGTH_SHORT).show();
                    exitActivity();
                } catch (ApiException e) {
                    Toast.makeText(context, "Failed to sign in", Toast.LENGTH_SHORT).show();
                    Log.d("Sign in failed", "signInResult:failed code=" + e.getStatusCode());
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_empty, menu);
        return true;
    }

    private void exitActivity() {
        Intent signInReturn = new Intent();
        if (user != null) {
            signInReturn.putExtra("account", user);
            setResult(RESULT_OK, signInReturn);
            super.finish();
        }
    }
}
