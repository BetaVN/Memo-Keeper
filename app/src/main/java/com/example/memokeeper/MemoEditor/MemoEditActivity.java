package com.example.memokeeper.MemoEditor;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.example.memokeeper.Constants.REQUEST_CODE;
import com.example.memokeeper.Utilities.PathUtils;
import com.example.memokeeper.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class MemoEditActivity extends AppCompatActivity {
    final private Context context = this;
    final private int PERMISSION_REQUEST = 1;

    private MenuInflater inflater;
    private String memoContent;
    private String memoTitle;
    private int listPos;
    private EditText textField;
    private EditText titleField;
    private RecyclerView attachedItems;
    private ArrayList<String> attachedFilePath;
    private ArrayList<String> newFilePath;

    private AttachItemAdapter attachItemAdapter;
    private ArrayList<AttachedItem> items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_memo);

        if (ContextCompat.checkSelfPermission(MemoEditActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MemoEditActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(MemoEditActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST);

            } else {
                ActivityCompat.requestPermissions(MemoEditActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST);

            }
        }

        if (ContextCompat.checkSelfPermission(MemoEditActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MemoEditActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(MemoEditActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST);

            } else {
                ActivityCompat.requestPermissions(MemoEditActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST);

            }
        }

        Toolbar editToolbar = findViewById(R.id.memoEditBar);
        titleField = findViewById(R.id.titleEditText);
        textField = findViewById(R.id.memoEditText);
        attachedItems = findViewById(R.id.attachedItemList);
        items = new ArrayList<>();
        newFilePath = new ArrayList<>();
        attachedFilePath = new ArrayList<>();

        attachItemAdapter = new AttachItemAdapter(items, context);
        attachedItems.setAdapter(attachItemAdapter);
        attachedItems.setLayoutManager(new LinearLayoutManager(this));

        syncFilePath();

        setSupportActionBar(editToolbar);
        getSupportActionBar().setTitle("Memo Keeper");

        Intent intent = getIntent();
        titleField.setText(intent.getStringExtra("memoTitle"));
        textField.setText(intent.getStringExtra("memoText"));
        listPos = intent.getIntExtra("listPosition", -1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_memo_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        Intent intent;
        switch (item.getItemId()){
            case R.id.action_save:
                saveMemo();
                return true;

            case R.id.action_exit:
                exitMemo();
                return true;

            case R.id.action_add_file:
                intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                startActivityForResult(intent, REQUEST_CODE.MEMO_PICK_FILE);
                return true;

            case R.id.action_add_photos:
                intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_CODE.MEMO_PICK_IMAGE);
                return true;

            default:
                return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE.MEMO_PICK_FILE:
                if(resultCode==RESULT_OK){
                    Uri content = data.getData();
                    String filePath = PathUtils.getPath(context, content);
                    File newFile = new File(filePath);
                    Boolean success = addNewFilesLocal(newFile);
                    if (success) {
                        filePath = context.getFilesDir() + "/" + newFile.getName();
                    }
                    newFilePath.add(filePath);
                    items.add(new AttachedItem(newFile.getName(), false, filePath));
                    attachItemAdapter.notifyItemInserted(items.size() - 1);
                    syncFilePath();
                }

            case REQUEST_CODE.MEMO_PICK_IMAGE:
                if(resultCode==RESULT_OK){
                    Uri content = data.getData();
                    String filePath = PathUtils.getPath(context, content);
                    File newFile = new File(filePath);
                    Boolean success = addNewFilesLocal(newFile);
                    if (success) {
                        filePath = context.getFilesDir() + "/" + newFile.getName();
                    }
                    newFilePath.add(filePath);
                    items.add(new AttachedItem(newFile.getName(), true, filePath));
                    attachItemAdapter.notifyItemInserted(items.size() - 1);
                    syncFilePath();
                }
        }
    }

    public void saveMemo() {
        memoContent = textField.getText().toString();
        memoTitle = titleField.getText().toString();
        Toast.makeText(MemoEditActivity.this, "Memo saved successfully", Toast.LENGTH_SHORT).show();
    }

    private void exitMemo() {
        AlertDialog.Builder confirmDialog = new AlertDialog.Builder(MemoEditActivity.this);
        confirmDialog.setTitle("Confirmation");
        confirmDialog.setMessage("Do you wish to save your memo?");
        confirmDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                saveMemo();
                exitActivity();
            }
        });
        confirmDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                exitActivity();
            }
        });
        confirmDialog.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        confirmDialog.show();
    }

    private Boolean addNewFilesLocal(File newFile) {
        try {
            FileInputStream inputStream = new FileInputStream(newFile);
            File target = new File(context.getFilesDir(), newFile.getName());
            FileOutputStream outputStream = new FileOutputStream(target);
            byte[] b = new byte[1024];
            int len;
            while ((len = inputStream.read(b)) > 0) {
                outputStream.write(b, 0, len);
            }
            inputStream.close();
            outputStream.close();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e1) {
            e1.printStackTrace();
        }
        return false;
    }

    private void syncFilePath() {
        ArrayList<String> syncedList = attachItemAdapter.returnFilePath();
        if (syncedList.addAll(newFilePath)) {
            attachedFilePath = syncedList;
            attachItemAdapter.getNewFilePath(syncedList);
        }
        newFilePath.clear();
    }

    private void exitActivity() {
        Intent memoReturn = new Intent();
        memoReturn.putExtra("memoContent", memoContent);
        memoReturn.putExtra("memoTitle", memoTitle);
        memoReturn.putExtra("attachedFile", attachedFilePath);
        memoReturn.putExtra("listPosition", listPos);
        setResult(RESULT_OK, memoReturn);
        super.finish();
    }
}
