package com.example.memokeeper.MainScreen;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.memokeeper.Constants.REQUEST_CODE;
import com.example.memokeeper.MemoEditor.MemoEditActivity;
import com.example.memokeeper.R;

import java.util.ArrayList;

public class MemoAdapter extends RecyclerView.Adapter<MemoAdapter.MemoHolder> {

    public class MemoHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public TextView date;
        public ConstraintLayout memoItem;

        public MemoHolder(@NonNull View memoView) {
            super(memoView);

            memoItem = memoView.findViewById(R.id.item_memo);
            title = memoView.findViewById(R.id.titleName);
            date = memoView.findViewById(R.id.date);
        }
    }

    private ArrayList<MemoInfo> memoList;
    private Context context;

    public MemoAdapter(ArrayList<MemoInfo> memoList, Context context) {
        this.memoList = memoList;
        this.context = context;
    }

    @NonNull
    @Override
    public MemoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View memoView = inflater.inflate(R.layout.item_memo, parent, false);
        MemoAdapter.MemoHolder newMemo = new MemoAdapter.MemoHolder(memoView);
        return newMemo;
    }

    @Override
    public void onBindViewHolder(@NonNull final MemoHolder memoHolder, final int position) {
        final MemoInfo newMemo = memoList.get(position);
        memoHolder.title.setText(newMemo.memoTitle);
        memoHolder.date.setText(newMemo.memoDate);

        memoHolder.memoItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent INTENT = new Intent(context, MemoEditActivity.class);
                INTENT.putExtra("memoTitle", newMemo.memoTitle);
                INTENT.putExtra("memoText", newMemo.memoText);
                ((Activity) context).startActivityForResult(INTENT, REQUEST_CODE.MEMO_EDIT);
            }
        });
    }

    @Override
    public int getItemCount() {
        return memoList.size();
    }


}
