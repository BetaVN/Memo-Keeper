package com.example.memokeeper.MemoEditor;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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

import com.example.memokeeper.Constants.INTENT_CODE;
import com.example.memokeeper.R;

import java.io.File;
import java.util.ArrayList;

public class MemoEditActivity extends AppCompatActivity {
    private MenuInflater inflater;
    private String memoContent;
    private String memoTitle;
    private EditText textField;
    private EditText titleField;
    private RecyclerView attachedItems;

    private AttachItemAdapter attachItemAdapter;
    private ArrayList<AttachedItem> items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_memo);
        Toolbar editToolbar = findViewById(R.id.memoEditBar);
        titleField = findViewById(R.id.titleEditText);
        textField = findViewById(R.id.memoEditText);
        attachedItems = findViewById(R.id.attachedItemList);
        items = new ArrayList<>();

        attachItemAdapter = new AttachItemAdapter(items);
        attachedItems.setAdapter(attachItemAdapter);
        attachedItems.setLayoutManager(new LinearLayoutManager(this));

        setSupportActionBar(editToolbar);
        getSupportActionBar().setTitle("Memo Keeper");
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
                intent.setType("file/*");
                startActivityForResult(intent, INTENT_CODE.MEMO_PICK_FILE);
                return true;

            case R.id.action_add_photos:
                intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, INTENT_CODE.MEMO_PICK_IMAGE);
                return true;

            default:
                return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case INTENT_CODE.MEMO_PICK_FILE:
                if(resultCode==RESULT_OK){
                    String filePath = data.getData().getPath();
                    File newFile = new File(filePath);
                    items.add(new AttachedItem(newFile.getName(), false, filePath));
                    Log.d("Filepath", filePath);
                    attachItemAdapter.notifyItemInserted(items.size() - 1);
                }

            case INTENT_CODE.MEMO_PICK_IMAGE:
                if(resultCode==RESULT_OK){
                    String filePath = data.getData().getPath();
                    File newFile = new File(filePath);
                    items.add(new AttachedItem(newFile.getName(), true, filePath));
                    Log.d("Filepath", filePath);
                    attachItemAdapter.notifyItemInserted(items.size() - 1);
                }
        }
    }

    public void saveMemo() {
        memoContent = textField.getText().toString();
        memoTitle = titleField.getText().toString();
        Toast.makeText(MemoEditActivity.this, "Memo saved successfully", Toast.LENGTH_SHORT).show();
        //TODO: Save to database, show failed toast if can't save
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

    private void exitActivity() {
        super.finish();
    }

    /*private ArrayList<AttachedItem> addDummyItem() {
        ArrayList<AttachedItem> newDummyList = new ArrayList<>();
        newDummyList.add(new AttachedItem("Sample File.txt", false, ""));
        newDummyList.add(new AttachedItem("Sample image.txt", true, ""));
        return newDummyList;
    }*/
}
