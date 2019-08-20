package com.example.memokeeper.ProfilePage;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.memokeeper.R;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

public class ProfilePageActivity extends AppCompatActivity {

    private ImageView googleAvatar;
    private TextView googleEmail;
    private TextView googleName;
    private GoogleSignInAccount user;
    private MenuInflater inflater;
    private Toolbar profileToolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_page);

        Intent intent = getIntent();
        user = intent.getParcelableExtra("user");

        googleAvatar = findViewById(R.id.googleAvatar);
        googleEmail = findViewById(R.id.googleEmail);
        googleName = findViewById(R.id.googleName);
        profileToolbar = findViewById(R.id.profileToolbar);

        Uri profileIcon = user.getPhotoUrl();

        if (profileIcon == null) {
            googleAvatar.setImageResource(R.drawable.ic_empty_avatar);
        }
        else {
            googleAvatar.setImageURI(profileIcon);
        }

        googleName.setText(user.getDisplayName());
        googleEmail.setText(user.getEmail());

        setSupportActionBar(profileToolbar);
        getSupportActionBar().setTitle("Account Information");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_profile_page, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.action_google_signout:
                AlertDialog.Builder confirmDialog = new AlertDialog.Builder(ProfilePageActivity.this);
                confirmDialog.setTitle("Confirmation");
                confirmDialog.setMessage("Do you wish to save your memo?");
                confirmDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        signOut();
                    }
                });
                confirmDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                confirmDialog.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                confirmDialog.show();
                return true;

            default:
                return false;
        }
    }

    private void signOut() {
        Intent signOutIntent = new Intent();
        signOutIntent.putExtra("Sign out", true);
        setResult(RESULT_OK, signOutIntent);
        super.finish();
    }
}
