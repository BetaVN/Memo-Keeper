package com.example.memokeeper;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.memokeeper.MemoEditor.MemoEditActivity;

public class MainActivity extends AppCompatActivity {

    final private Context CONTEXT = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button testButton = findViewById(R.id.button);


        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent modeSelection = new Intent(CONTEXT, MemoEditActivity.class);
                startActivity(modeSelection);
            }
        });
    }

}
