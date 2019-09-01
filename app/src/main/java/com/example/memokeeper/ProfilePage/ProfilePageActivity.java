package com.example.memokeeper.ProfilePage;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.memokeeper.DatabaseHelper.MemoContract;
import com.example.memokeeper.GoogleDriveHelper.DriveServiceHelper;
import com.example.memokeeper.GoogleDriveHelper.GoogleDriveFileHolder;
import com.example.memokeeper.R;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class ProfilePageActivity extends AppCompatActivity {

    private String backupFolderName;
    private Boolean backupFolderCreated = false;
    private Boolean grabbingAllHash = true;
    private Context context = this;

    private ImageView googleAvatar;
    private TextView googleEmail;
    private TextView googleName;
    private TextView statistic;
    private TextView syncGG;
    private TextView pullGG;
    private GoogleSignInAccount user;
    private MenuInflater inflater;
    private Toolbar profileToolbar;
    private GoogleAccountCredential credential;
    private DriveServiceHelper driveServiceHelper;
    private MemoContract.MemoDbHelper dbHelper;
    private ArrayList<String> allMemoFolders;
    private MemoHashGrabber hashGrabber = new MemoHashGrabber();

    private GoogleDriveFileHolder backupFolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_page);

        backupFolderName = getString(R.string.backup_folder_name);
        dbHelper = new MemoContract().new MemoDbHelper(this);
        hashGrabber.execute("");

        Intent intent = getIntent();
        user = intent.getParcelableExtra("user");

        googleAvatar = findViewById(R.id.googleAvatar);
        googleEmail = findViewById(R.id.googleEmail);
        googleName = findViewById(R.id.googleName);
        profileToolbar = findViewById(R.id.profileToolbar);
        statistic = findViewById(R.id.reportButton);
        syncGG = findViewById(R.id.syncButton);
        pullGG = findViewById(R.id.pullButton);

        Uri profileIcon = user.getPhotoUrl();

        if (profileIcon == null) {
            googleAvatar.setImageResource(R.drawable.ic_empty_avatar);
        }
        else {
            googleAvatar.setImageURI(profileIcon);
        }

        googleName.setText(user.getDisplayName());
        googleEmail.setText(user.getEmail());
        credential = GoogleAccountCredential.usingOAuth2(this, Collections.singleton(DriveScopes.DRIVE_FILE));
        credential.setSelectedAccount(user.getAccount());
        Drive googleDriveService = new Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), credential).setApplicationName("Memo Keeper").build();
        driveServiceHelper = new DriveServiceHelper(googleDriveService, this);

        assignBackupFolder();

        setSupportActionBar(profileToolbar);
        getSupportActionBar().setTitle("Account Information");

        syncGG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                syncProcess();
            }
        });
        pullGG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "Not implemented yet.", Toast.LENGTH_SHORT).show();
            }
        });
        statistic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "Not implemented yet.", Toast.LENGTH_SHORT).show();
            }
        });
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
                confirmDialog.setMessage("Do you wish to sign out of this account?");
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

    private class MemoHashGrabber extends AsyncTask<String, Void, ArrayList<String>> {

        @Override
        protected ArrayList<String> doInBackground(String... strings) {
            Cursor allMemoHash = dbHelper.getAllFolders();
            ArrayList<String> allFolders = new ArrayList<>();
            if (allMemoHash.moveToFirst()) {
                do {
                    String hash = allMemoHash.getString(allMemoHash.getColumnIndex(MemoContract.MemoEntry.COLLUMN_MEMO_HASH));
                    allFolders.add(hash);
                } while (allMemoHash.moveToNext());
            }
            allMemoHash.close();
            return allFolders;
        }

        protected void onPostExecute(ArrayList<String> result) {
            //result.add("databases");
            allMemoFolders = result;
            grabbingAllHash = false;
        }
    }

    private GoogleDriveFileHolder createNewFolder(String folderName, @Nullable String BaseFolder) {
        GoogleDriveFileHolder newFolder = new GoogleDriveFileHolder();
        Task<GoogleDriveFileHolder> createNewFolder = driveServiceHelper.createFolder(folderName, BaseFolder);
        createNewFolder.addOnSuccessListener(new OnSuccessListener<GoogleDriveFileHolder>() {
            @Override
            public void onSuccess(GoogleDriveFileHolder result) {
                if (createNewFolder.isSuccessful()) {
                    if ((result != null) && (result.getId() != null)) {
                        Log.d("Sync: ", "New folder " + folderName + " created...");
                        if (folderName.equals(backupFolderName)) {
                            backupFolderCreated = true;
                            backupFolder = result;
                        }
                    }
                    else {
                        Log.d("Sync: ", "Possible bug in folder creation...");
                    }
                }
                else {
                    Log.d("Sync: ", "Something has gone wrong while making a new folder...");
                }
            }
        });

        createNewFolder.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
                Log.d("Sync: ", "Failed to execute creating new folder...");
            }
        });
        return newFolder;
    }

    private void syncProcess() {
        if (grabbingAllHash == true) {
            Toast.makeText(this, "Currently getting all of your memos. Please wait.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (backupFolderCreated == false) {
            Toast.makeText(this, "No backup folder detected.", Toast.LENGTH_SHORT).show();
        }
        if (allMemoFolders.size() == 0) {
            Toast.makeText(this, "You don't have any memo to backup.", Toast.LENGTH_SHORT).show();
            return;
        }
        uploadDatabase();
        for (String hash: allMemoFolders) {
            Toast.makeText(this, "Backing up memo...", Toast.LENGTH_SHORT).show();
            Task<GoogleDriveFileHolder> checkForExistingFolders = driveServiceHelper.searchFolder(hash);
            checkForExistingFolders.addOnSuccessListener(new OnSuccessListener<GoogleDriveFileHolder>() {
                @Override
                public void onSuccess(GoogleDriveFileHolder result) {
                    if (checkForExistingFolders.isSuccessful()) {
                        if (result.getName() == null) {
                            Log.d("Sync: ", "Begin new upload...");
                            uploadNewMemoFolder(hash);
                        }
                        else {
                            Log.d("Sync: ", "Overwriting folder...");
                            Task<Void>  deleteCurrentFolder = driveServiceHelper.deleteFolderFile(result.getId());
                            deleteCurrentFolder.addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    uploadNewMemoFolder(hash);
                                }
                            });
                        }
                    }
                }
            });
            checkForExistingFolders.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private String getMimeType(File target) {
        MimeTypeMap myMime = MimeTypeMap.getSingleton();
        String mimeType = myMime.getMimeTypeFromExtension(target.getPath().substring(target.getPath().lastIndexOf(".") + 1));
        return  mimeType;
    }

    private void assignBackupFolder() {
        Task<GoogleDriveFileHolder> findBackupFolder = driveServiceHelper.searchFolder(backupFolderName);
        findBackupFolder.addOnSuccessListener(new OnSuccessListener<GoogleDriveFileHolder>() {
            @Override
            public void onSuccess(GoogleDriveFileHolder result) {
                if (findBackupFolder.isSuccessful()) {
                    if ((result != null) && (result.getId() != null)) {
                        Log.d("Sync: ", "Backup folder found...");
                        backupFolderCreated = true;
                        backupFolder = result;
                    }
                    else {
                        Log.d("Sync: ", "Backup folder not found. Creating a new one...");
                        backupFolder = createNewFolder(backupFolderName, null);
                    }
                }
                else {
                    Log.d("Sync: ", "Something has gone wrong while searching...");
                }
            }
        });

        findBackupFolder.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
                Log.d("Sync: ", "Failed to execute search");
            }
        });
    }

    private void uploadNewMemoFolder(String hash) {
        Task<GoogleDriveFileHolder> createNewFolder = driveServiceHelper.createFolder(hash, backupFolder.getId());

        createNewFolder.addOnSuccessListener(new OnSuccessListener<GoogleDriveFileHolder>() {
            @Override
            public void onSuccess(GoogleDriveFileHolder result) {
                File[] memoFiles = new File(getFilesDir(), hash).listFiles();
                for (File uploadFile: memoFiles) {
                    String mimeType = getMimeType(uploadFile);
                    Task<GoogleDriveFileHolder> uploadFileTask = driveServiceHelper.uploadFile(uploadFile, mimeType, result.getId());
                    uploadFileTask.addOnSuccessListener(new OnSuccessListener<GoogleDriveFileHolder>() {
                        @Override
                        public void onSuccess(GoogleDriveFileHolder result) {
                            Log.d("Sync: ", "File " + uploadFile.getName() + " uploaded...");
                        }
                    });
                }
            }
        });
    }

    private void uploadDatabase() {
        File database = getDatabasePath(MemoContract.MemoDbHelper.DATABASE_NAME);
        Task<GoogleDriveFileHolder> checkForExistingFolders = driveServiceHelper.searchFolder("databases");
        checkForExistingFolders.addOnSuccessListener(new OnSuccessListener<GoogleDriveFileHolder>() {
            @Override
            public void onSuccess(GoogleDriveFileHolder result) {
                if (checkForExistingFolders.isSuccessful()) {
                    if (result.getName() == null) {
                        Log.d("Sync: ", "Begin new upload...");
                        Task<GoogleDriveFileHolder> createNewFolder = driveServiceHelper.createFolder("databases", backupFolder.getId());

                        createNewFolder.addOnSuccessListener(new OnSuccessListener<GoogleDriveFileHolder>() {
                            @Override
                            public void onSuccess(GoogleDriveFileHolder result) {
                                Task<GoogleDriveFileHolder> uploadFileTask = driveServiceHelper.uploadFile(database, getMimeType(database), result.getId());
                                uploadFileTask.addOnSuccessListener(new OnSuccessListener<GoogleDriveFileHolder>() {
                                    @Override
                                    public void onSuccess(GoogleDriveFileHolder result) {
                                        Log.d("Sync: ", "Database updated.");
                                    }
                                });
                            }
                        });
                    }
                    else {
                        Log.d("Sync: ", "Overwriting folder...");
                        Task<Void>  deleteCurrentFolder = driveServiceHelper.deleteFolderFile(result.getId());
                        deleteCurrentFolder.addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Task<GoogleDriveFileHolder> createNewFolder = driveServiceHelper.createFolder("databases", backupFolder.getId());

                                createNewFolder.addOnSuccessListener(new OnSuccessListener<GoogleDriveFileHolder>() {
                                    @Override
                                    public void onSuccess(GoogleDriveFileHolder result) {
                                        Task<GoogleDriveFileHolder> uploadFileTask = driveServiceHelper.uploadFile(database, getMimeType(database), result.getId());
                                        uploadFileTask.addOnSuccessListener(new OnSuccessListener<GoogleDriveFileHolder>() {
                                            @Override
                                            public void onSuccess(GoogleDriveFileHolder result) {
                                                Log.d("Sync: ", "Database updated.");
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                }
            }
        });
        checkForExistingFolders.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        });
    }
}
