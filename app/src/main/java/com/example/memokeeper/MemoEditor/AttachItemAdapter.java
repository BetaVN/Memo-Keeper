package com.example.memokeeper.MemoEditor;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.memokeeper.R;

import java.util.List;

public class AttachItemAdapter extends RecyclerView.Adapter<AttachItemAdapter.ItemHolder> {

    public class ItemHolder extends RecyclerView.ViewHolder {
        public ImageView imageFileType;
        public TextView fileName;

        public ItemHolder(@NonNull View itemView) {
            super(itemView);

            imageFileType = itemView.findViewById(R.id.fileType);
            fileName = itemView.findViewById(R.id.fileName);
        }
    }

    private List<AttachedItem> itemList;

    public AttachItemAdapter(List<AttachedItem> items){
        itemList = items;
    }


    @Override
    public AttachItemAdapter.ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.item_attached_item, parent, false);
        AttachItemAdapter.ItemHolder newItem = new AttachItemAdapter.ItemHolder(itemView);
        return newItem;
    }


    @Override
    public int getItemCount() {
        return itemList.size();
    }

    @Override
    public void onBindViewHolder(AttachItemAdapter.ItemHolder itemHolder, int position) {
        AttachedItem newItem = itemList.get(position);

        itemHolder.fileName.setText(newItem.fileName);

        if(newItem.isImage) {
            itemHolder.imageFileType.setImageResource(R.drawable.ic_image_type);
        }
    }
}
