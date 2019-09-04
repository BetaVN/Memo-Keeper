package com.example.memokeeper;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import androidx.appcompat.widget.Toolbar;

import com.example.memokeeper.Constants.REQUEST_CODE;
import com.example.memokeeper.DatabaseHelper.MemoContract;
import com.example.memokeeper.MainScreen.MemoAdapter;
import com.example.memokeeper.MainScreen.MemoInfo;
import com.example.memokeeper.MainScreen.VerticalSpaceItemDecoration;
import com.example.memokeeper.MemoEditor.MemoEditActivity;
import com.example.memokeeper.ProfilePage.ProfilePageActivity;
import com.example.memokeeper.ProfilePage.SignInActivity;
import com.example.memokeeper.Utilities.DateUtils;
import com.example.memokeeper.Utilities.PathUtils;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class MainActivity extends AppCompatActivity{

    final private Context context = this;

    private GoogleSignInAccount user = null;

    final private int PERMISSION_REQUEST = 1;
    private MenuInflater inflater;
    private MemoAdapter memoAdapter;
    private ArrayList<MemoInfo> memo;
    private RecyclerView memoList;
    private MemoAsync memoGrabber = new MemoAsync();
    private String currentUnusedHash;
    private boolean newMemoCreated = false;
    private MemoContract.MemoDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermission();

        Toolbar mainScreenToolbar = findViewById(R.id.mainScreenToolbar);
        dbHelper = new MemoContract().new MemoDbHelper(context);
        memoList = findViewById(R.id.memoList);
        memo = new ArrayList<>();
        memoGrabber.execute("");

        memoAdapter = new MemoAdapter(memo, context);
        memoList.setAdapter(memoAdapter);
        memoList.setLayoutManager(new LinearLayoutManager(this));
        memoList.addItemDecoration(new VerticalSpaceItemDecoration(5));

        setSupportActionBar(mainScreenToolbar);
        getSupportActionBar().setTitle("Memo Keeper");
        registerForContextMenu(memoList);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbHelper.close();
        if (newMemoCreated) {
            File deleteFolder = new File(getFilesDir().getAbsolutePath(), currentUnusedHash);
            deleteFolder.delete();
            newMemoCreated = false;
            Log.d("Destroy temp folder", "Temporary folder " + currentUnusedHash + " deleted!");
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_base, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.action_add_memo:
                Intent INTENT = new Intent(context, MemoEditActivity.class);
                do {
                    currentUnusedHash = PathUtils.generateFolderHash(8);
                } while(new File(getFilesDir().getAbsolutePath(), String.valueOf(currentUnusedHash)).exists() == true);
                newMemoCreated = true;
                INTENT.putExtra("listPosition", -1);
                INTENT.putExtra("hashFolder", currentUnusedHash);
                startActivityForResult(INTENT, REQUEST_CODE.MEMO_EDIT);
                return true;

            case R.id.action_google_signin:
                if (isSignedIn()) {
                    Intent PROFILE = new Intent(context, ProfilePageActivity.class);
                    PROFILE.putExtra("user", user);
                    startActivityForResult(PROFILE, REQUEST_CODE.VIEW_PROFILE);
                }
                else {
                    Intent PROFILE = new Intent(context, SignInActivity.class);
                    startActivityForResult(PROFILE, REQUEST_CODE.SIGN_IN);
                }
                return true;

            default:
                return false;
        }
    }

    @Override
    protected void onActivityResult(int RequestCode, int ResultCode, Intent data) {
        if (RequestCode == REQUEST_CODE.MEMO_EDIT) {
            if (ResultCode == RESULT_OK) {
                int pos = data.getIntExtra("listPosition", -1);
                String hash = data.getStringExtra("hashFolder");
                String memoTitle = data.getStringExtra("memoTitle");
                String memoContent = data.getStringExtra("memoContent");
                Date today = Calendar.getInstance().getTime();
                String memoAttachment = data.getStringExtra("memoAttachment");
                MemoInfo newMemo = new MemoInfo(memoTitle, DateUtils.dateToComparableInt(today), memoContent, memoAttachment, hash);
                if (pos == -1) {
                    memo.add(newMemo);
                    memoAdapter.notifyItemInserted(memo.size() - 1);
                    dbHelper.addNewMemo(newMemo);
                } else {
                    memo.set(pos, newMemo);
                    memoAdapter.notifyItemChanged(pos);
                    dbHelper.updateMemo(newMemo);

                }
            }
            else if (ResultCode == RESULT_CANCELED) {
                if (newMemoCreated) {
                    File deleteFolder = new File(getFilesDir().getAbsolutePath(), currentUnusedHash);
                    deleteFolder.delete();
                    newMemoCreated = false;
                }
            }
        }
        if (RequestCode == REQUEST_CODE.SIGN_IN) {
            if (ResultCode == RESULT_OK) {
                user = data.getParcelableExtra("account");
                Log.d("Account", user.getEmail() + "");
                Log.d("Account", user.getDisplayName() + "");
                Log.d("Account", user.getGivenName() + "");
                Log.d("Account", user.getId() + "");
            }
        }
        if (RequestCode == REQUEST_CODE.VIEW_PROFILE) {
            if (ResultCode == RESULT_OK) {
                if (data.getBooleanExtra("Sign out", false)) {
                    user = null;
                }
            }
        }
    }

    private void updateView(ArrayList<MemoInfo> result) {
        if (memo.addAll(result)) {
            memoAdapter.notifyItemInserted(memo.size() - 1);
        }
    }

    private class MemoAsync extends AsyncTask<String, Void, ArrayList<MemoInfo>> {
        @Override
        protected ArrayList<MemoInfo> doInBackground(String... url) {
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
            updateView(result);
        }
    }

    private boolean isSignedIn() {
        return (user != null);
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST);

            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST);

            }
        }

        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST);

            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST);

            }
        }
    }
}
