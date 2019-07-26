package com.example.memokeeper;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.support.v7.widget.Toolbar;

import com.example.memokeeper.Constants.REQUEST_CODE;
import com.example.memokeeper.MainScreen.MemoAdapter;
import com.example.memokeeper.MainScreen.MemoInfo;
import com.example.memokeeper.MainScreen.VerticalSpaceItemDecoration;
import com.example.memokeeper.MemoEditor.MemoEditActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class MainActivity extends AppCompatActivity {

    final private Context context = this;

    private MenuInflater inflater;
    private MemoAdapter memoAdapter;
    private ArrayList<MemoInfo> memo;
    private RecyclerView memoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar mainScreenToolbar = findViewById(R.id.mainScreenToolbar);
        memoList = findViewById(R.id.memoList);
        memo = new ArrayList<>();

        memo.add(new MemoInfo("This is a sample memo", "18/7/2019", "Please remember to feed the dog.\nAnd also pick up Carl from school"));
        memo.add(new MemoInfo("This is another sample memo", "19/7/2018", "Vacation day. Things to do:\n\nBuy food\nBuy sunscreen\nBuy swimsuits"));


        memoAdapter = new MemoAdapter(memo, context);
        memoList.setAdapter(memoAdapter);
        memoList.setLayoutManager(new LinearLayoutManager(this));
        memoList.addItemDecoration(new VerticalSpaceItemDecoration(5));

        setSupportActionBar(mainScreenToolbar);
        getSupportActionBar().setTitle("Memo Keeper");
        registerForContextMenu(memoList);
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
                startActivityForResult(INTENT, REQUEST_CODE.MEMO_EDIT);
                return true;

            default:
                return false;
        }
    }

    @Override
    protected void onActivityResult(int RequestCode, int ResultCode, Intent data) {
        if (RequestCode == REQUEST_CODE.MEMO_EDIT && ResultCode == RESULT_OK) {
            if(data.getIntExtra("listPosition", -1) == -1) {
                String memoTitle = data.getStringExtra("memoTitle");
                String memoContent = data.getStringExtra("memoContent");
                Date today = Calendar.getInstance().getTime();
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                memo.add(new MemoInfo(memoTitle, dateFormat.format(today), memoContent));
                memoAdapter.notifyItemInserted(memo.size() - 1);
            }
            else {
                int pos = data.getIntExtra("listPosition", -1);
                String memoTitle = data.getStringExtra("memoTitle");
                String memoContent = data.getStringExtra("memoContent");
                Date today = Calendar.getInstance().getTime();
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                memo.set(pos, new MemoInfo(memoTitle, dateFormat.format(today), memoContent));
                memoAdapter.notifyItemChanged(pos);
            }
        }
    }

}
