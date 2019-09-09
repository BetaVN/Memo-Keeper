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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.memokeeper.Constants.REQUEST_CODE;
import com.example.memokeeper.DatabaseHelper.MemoContract;
import com.example.memokeeper.GoogleDriveHelper.DriveServiceHelper;
import com.example.memokeeper.GoogleDriveHelper.GoogleDriveFileHolder;
import com.example.memokeeper.MainScreen.MemoInfo;
import com.example.memokeeper.R;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
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
    private Boolean isUploading = false;
    private Boolean isDownloading = false;

    private ImageView googleAvatar;
    private TextView googleEmail;
    private TextView googleName;
    private TextView syncGG;
    private TextView pullGG;
    private TextView taskRunning;
    private Toolbar profileToolbar;
    private ProgressBar taskProgressBar;

    private GoogleSignInAccount user;
    private MenuInflater inflater;
    private GoogleAccountCredential credential;
    private DriveServiceHelper driveServiceHelper;
    private MemoContract.MemoDbHelper dbHelper;
    private ArrayList<MemoInfo> allMemoFolders;
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
        syncGG = findViewById(R.id.syncButton);
        pullGG = findViewById(R.id.pullButton);
        taskRunning = findViewById(R.id.taskRunning);
        taskProgressBar = findViewById(R.id.progressBar);

        taskRunning.setVisibility(View.INVISIBLE);
        taskProgressBar.setVisibility(View.INVISIBLE);

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
            public void onClick(View view) { restoreProcess(); }
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
        setResult(REQUEST_CODE.RESULT_SIGN_OUT, signOutIntent);
        super.finish();
    }

    private class MemoHashGrabber extends AsyncTask<String, Void, ArrayList<MemoInfo>> {

        @Override
        protected ArrayList<MemoInfo> doInBackground(String... strings) {
            Cursor allMemo = dbHelper.getAllMemo();
            ArrayList<MemoInfo> memoList = new ArrayList<>();
            if (allMemo.moveToFirst()) {
                do {
                    String memoTitle = allMemo.getString(allMemo.getColumnIndex(MemoContract.MemoEntry.COLLUMN_MEMO_TITLE));
                    String memoContent = allMemo.getString(allMemo.getColumnIndex(MemoContract.MemoEntry.COLLUMN_MEMO_CONTENT));
                    String memoAttachment = allMemo.getString(allMemo.getColumnIndex(MemoContract.MemoEntry.COLLUMN_MEMO_ATTACHMENT));
                    int date = allMemo.getInt(allMemo.getColumnIndex(MemoContract.MemoEntry.COLLUMN_MEMO_DATE));
                    String hash = allMemo.getString(allMemo.getColumnIndex(MemoContract.MemoEntry.COLLUMN_MEMO_HASH));
                    memoList.add(new MemoInfo(memoTitle, date, memoContent, memoAttachment, hash));
                } while (allMemo.moveToNext());
            }
            allMemo.close();
            return memoList;
        }

        protected void onPostExecute(ArrayList<MemoInfo> result) {
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
        if (grabbingAllHash) {
            Toast.makeText(this, "Currently getting all of your memos. Please wait.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!backupFolderCreated) {
            Toast.makeText(this, "No backup folder detected.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (allMemoFolders.size() == 0) {
            Toast.makeText(this, "You don't have any memo to backup.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (isUploading) {
            Toast.makeText(this, "The app is already backing up your files with Drive. Please wait.", Toast.LENGTH_SHORT);
            return;
        }
        if (isDownloading) {
            Toast.makeText(this, "The app is syncing files from Drive to your app. Please wait.", Toast.LENGTH_LONG).show();
            return;
        }
        isUploading = true;
        taskRunning.setText("Uploading Files");
        taskRunning.setVisibility(View.VISIBLE);
        taskProgressBar.setProgress(0);
        taskProgressBar.setVisibility(View.VISIBLE);
        Toast.makeText(this, "Backing up all of your memos. This could take a while.", Toast.LENGTH_LONG).show();
        Task<Void> startUploadMemoFiles = driveServiceHelper.uploadBackupProcess(allMemoFolders, backupFolder.getId(), taskProgressBar, taskRunning);
        startUploadMemoFiles.addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (startUploadMemoFiles.isSuccessful()) {
                    Toast.makeText(context, "Memo backup is complete.", Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(context, "Something has gone wrong. Please try again.", Toast.LENGTH_LONG).show();
                }
                isUploading = false;
                taskRunning.setVisibility(View.INVISIBLE);
                taskProgressBar.setVisibility(View.INVISIBLE);
            }
        });
        startUploadMemoFiles.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                isUploading = false;
                taskRunning.setVisibility(View.INVISIBLE);
                taskProgressBar.setVisibility(View.INVISIBLE);
            }
        });
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

    private void restoreProcess() {
        if (isUploading) {
            Toast.makeText(this, "The app is backing up your files with Drive. Please wait.", Toast.LENGTH_SHORT);
            return;
        }
        if (isDownloading) {
            Toast.makeText(this, "The app is already syncing files from Drive to your app. Please wait.", Toast.LENGTH_LONG).show();
            return;
        }
        if (!backupFolderCreated) {
            Toast.makeText(this, "No backup folder detected.", Toast.LENGTH_SHORT).show();
            return;
        }
        isDownloading = true;
        taskRunning.setText("Downloading Files");
        taskRunning.setVisibility(View.VISIBLE);
        taskProgressBar.setProgress(0);
        taskProgressBar.setVisibility(View.VISIBLE);
        Toast.makeText(this, "Downloading files from Drive. This could take a while.", Toast.LENGTH_SHORT).show();
        Task<Void> startDownloadBackupProcess = driveServiceHelper.downloadBackupProcess(backupFolder.getId(), taskProgressBar, taskRunning);
        startDownloadBackupProcess.addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (startDownloadBackupProcess.isSuccessful()) {
                    taskRunning.setText("Transferring files to app");
                    transferNewData();
                    return;
                }
                Toast.makeText(context, "Something has gone wrong. Please try again.", Toast.LENGTH_LONG).show();
                isDownloading = false;
                taskRunning.setVisibility(View.INVISIBLE);
                taskProgressBar.setVisibility(View.INVISIBLE);
            }
        });
        startDownloadBackupProcess.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                isDownloading = false;
                taskRunning.setVisibility(View.INVISIBLE);
                taskProgressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void transferNewData() {
        File[] listFiles = getFilesDir().listFiles();
        for (File memo: listFiles) {
            if (memo.isDirectory()) {
                File memoText = new File(memo, memo.getName() + ".txt");
                Log.d("Path: ", memoText.getAbsolutePath() + "");
                if (!memoText.exists()) {
                    continue;
                }
                MemoInfo newMemoInfo = driveServiceHelper.readFromMemoTextFile(memoText);
                if (!dbHelper.addNewMemo(newMemoInfo)) {
                    dbHelper.updateMemo(newMemoInfo);
                }
            }
        }
        isDownloading = false;
        taskProgressBar.setProgress(taskProgressBar.getProgress() + 1);
        Toast.makeText(this, "All data transfers completed.", Toast.LENGTH_LONG).show();
        taskRunning.setVisibility(View.INVISIBLE);
        taskProgressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onBackPressed() {
        if (isDownloading || isUploading) {
            AlertDialog.Builder confirmDialog = new AlertDialog.Builder(ProfilePageActivity.this);
            confirmDialog.setTitle("Confirmation");
            confirmDialog.setMessage("Current backup tasks are still running. Do you want to return?");
            confirmDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    exitActivity();
                }
            });
            confirmDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            confirmDialog.show();
        }
        else {
            exitActivity();
        }
    }

    public void exitActivity() {
        super.finish();
    }
}

