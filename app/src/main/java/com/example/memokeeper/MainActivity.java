package com.example.memokeeper;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.example.memokeeper.MainScreen.MemoAdapter;
import com.example.memokeeper.MainScreen.MemoInfo;
import com.example.memokeeper.MemoEditor.MemoEditActivity;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    final private Context context = this;

    private MemoAdapter memoAdapter;
    private ArrayList<MemoInfo> memo;
    private RecyclerView memoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Button testButton = findViewById(R.id.button);


        /*testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent modeSelection = new Intent(CONTEXT, MemoEditActivity.class);
                startActivity(modeSelection);
            }
        });*/

        memoList = findViewById(R.id.memoList);
        memo = new ArrayList<>();

        memo.add(new MemoInfo("This is a sample memo", "18/7/2019"));
        memo.add(new MemoInfo("This is another sample memo", "19/7/2018"));


        memoAdapter = new MemoAdapter(memo, context);
        memoList.setAdapter(memoAdapter);
        memoList.setLayoutManager(new LinearLayoutManager(this));
    }

}
