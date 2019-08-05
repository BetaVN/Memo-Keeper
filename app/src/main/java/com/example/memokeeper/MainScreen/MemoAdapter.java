package com.example.memokeeper.MainScreen;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.memokeeper.Constants.REQUEST_CODE;
import com.example.memokeeper.DatabaseHelper.MemoContract;
import com.example.memokeeper.MemoEditor.MemoEditActivity;
import com.example.memokeeper.R;
import com.example.memokeeper.Utilities.DateUtils;

import java.io.File;
import java.util.ArrayList;

public class MemoAdapter extends RecyclerView.Adapter<MemoAdapter.MemoHolder> {

    public class MemoHolder extends RecyclerView.ViewHolder{
        public TextView title;
        public TextView date;
        public ConstraintLayout memoItem;
        public int position;

        public MemoHolder(@NonNull View memoView) {
            super(memoView);

            memoItem = memoView.findViewById(R.id.item_memo);
            title = memoView.findViewById(R.id.titleName);
            date = memoView.findViewById(R.id.date);

            memoView.setOnCreateContextMenuListener(memoContextMenu);
        }

        private final View.OnCreateContextMenuListener memoContextMenu = new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                MenuItem uploadMemo = menu.add("Save to Drive");
                MenuItem deleteMemo = menu.add("Delete");
                deleteMemo.setOnMenuItemClickListener(deleteMemoAction);
                uploadMemo.setOnMenuItemClickListener(saveMemoAction);
            }
        };

        private final MenuItem.OnMenuItemClickListener deleteMemoAction = new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                AlertDialog.Builder confirmDialog = new AlertDialog.Builder(context);
                confirmDialog.setTitle("Confirmation");
                confirmDialog.setMessage("Do you wish to remove this memo?");
                confirmDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MemoContract.MemoDbHelper deleteMemo = new MemoContract().new MemoDbHelper(context);
                        deleteMemo.deleteMemo(memoList.get(position).hash);
                        File deleteFolder = new File(context.getFilesDir().getAbsolutePath(), memoList.get(position).hash);
                        if (deleteFolder.exists()) {
                            Log.d("Delete", "Found");
                            deleteFolder.delete();
                        }
                        memoList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, memoList.size());
                        Toast.makeText(context, "Memo deleted successfully", Toast.LENGTH_SHORT).show();
                    }
                });
                confirmDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                confirmDialog.show();
                return true;
            }
        };

        private final MenuItem.OnMenuItemClickListener saveMemoAction = new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Toast.makeText(context, "This feature is not yet implemented", Toast.LENGTH_SHORT).show();
                return false;
            }
        };
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
        memoHolder.position = position;
        memoHolder.title.setText(newMemo.memoTitle);
        memoHolder.date.setText(DateUtils.intToString(newMemo.memoDate));

        memoHolder.memoItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent INTENT = new Intent(context, MemoEditActivity.class);
                INTENT.putExtra("memoTitle", newMemo.memoTitle);
                INTENT.putExtra("memoText", newMemo.memoText);
                INTENT.putExtra("memoAttachment", newMemo.memoAttachment);
                INTENT.putExtra("hashFolder", newMemo.hash);
                INTENT.putExtra("listPosition", position);
                ((Activity) context).startActivityForResult(INTENT, REQUEST_CODE.MEMO_EDIT);
            }
        });
    }

    @Override
    public int getItemCount() {
        return memoList.size();
    }


}
