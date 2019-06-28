package com.example.memokeeper.MemoEditor;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.example.memokeeper.R;

public class MemoEditActivity extends AppCompatActivity {
    private MenuInflater inflater;
    private String memoContent;
    private String memoTitle;
    private EditText textField;
    private EditText titleField;
    private RecyclerView attachedItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_memo);
        Toolbar editToolbar = findViewById(R.id.memoEditBar);
        titleField = findViewById(R.id.titleEditText);
        textField = findViewById(R.id.memoEditText);
        attachedItems = findViewById(R.id.attachedItemList);
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
        switch (item.getItemId()){
            case R.id.action_save:
                saveMemo();
                return true;

            case R.id.action_exit:
                exitMemo();
                return true;

            default:
                return false;
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
}
