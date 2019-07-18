package com.example.memokeeper.MainScreen;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.memokeeper.MemoEditor.AttachItemAdapter;
import com.example.memokeeper.R;

import java.util.ArrayList;

public class MemoAdapter extends RecyclerView.Adapter<MemoAdapter.MemoHolder> {

    public class MemoHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public TextView date;

        public MemoHolder(@NonNull View memoView) {
            super(memoView);

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
        memoHolder.date.setText(newMemo.date);
    }

    @Override
    public int getItemCount() {
        return memoList.size();
    }


}
