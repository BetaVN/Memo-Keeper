package com.example.memokeeper;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;

public class MemoEditActivity extends AppCompatActivity {
    private MenuInflater inflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_memo);
        Toolbar editToolbar = findViewById(R.id.memoEditBar);
        EditText textField = findViewById(R.id.memoEditText);
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
                //Todo: Add save function
                sampleEnd();
                return true;

            case R.id.action_exit:
                //Todo: Add exit function
                sampleEnd();
                return true;

            default:
                return false;
        }
    }

    private void sampleEnd(){
        super.finish();
    }
}
