package com.example.memokeeper.MainScreen;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class MemoAdapter extends RecyclerView.Adapter<MemoAdapter.MemoHolder> {

    public class MemoHolder extends RecyclerView.ViewHolder {

        public MemoHolder(@NonNull View memoView) {
            super(memoView);
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
    public MemoHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull MemoHolder memoHolder, int i) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }


}
