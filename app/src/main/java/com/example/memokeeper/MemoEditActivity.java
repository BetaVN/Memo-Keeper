package com.example.memokeeper;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.EditText;

public class MemoEditActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar editToolbar = findViewById(R.id.memoEditBar);
        EditText textField = findViewById(R.id.memoEditText);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.action_save:
                //Todo: Add save function
                return true;

            case R.id.action_exit:
                //Todo: Add exit function
                return true;

            default:
                return false;
        }
    }
}
