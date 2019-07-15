package com.example.memokeeper.MemoEditor;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.memokeeper.R;

import java.io.File;
import java.util.ArrayList;

public class AttachItemAdapter extends RecyclerView.Adapter<AttachItemAdapter.ItemHolder> {

    public class ItemHolder extends RecyclerView.ViewHolder {
        public ImageView imageFileType;
        public Button fileName;
        public Button clearButton;

        public ItemHolder(@NonNull View itemView) {
            super(itemView);

            imageFileType = itemView.findViewById(R.id.fileType);
            fileName = itemView.findViewById(R.id.fileName);
            clearButton = itemView.findViewById(R.id.clearButton);
        }
    }

    private ArrayList<AttachedItem> itemList;
    private Context context;

    public AttachItemAdapter(ArrayList<AttachedItem> items, Context context){
        itemList = items;
        this.context = context;
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
    public void onBindViewHolder(final AttachItemAdapter.ItemHolder itemHolder, final int position) {
        final AttachedItem newItem = itemList.get(position);

        itemHolder.fileName.setText(newItem.fileName);

        if(newItem.isImage) {
            itemHolder.imageFileType.setImageResource(R.drawable.ic_image_type);
        }

        itemHolder.clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder confirmDialog = new AlertDialog.Builder(context);
                confirmDialog.setTitle("Confirmation");
                confirmDialog.setMessage("Do you wish to remove this file?");
                confirmDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        itemList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, itemList.size());
                        Toast.makeText(context, "Attached file removed", Toast.LENGTH_SHORT).show();
                    }
                });
                confirmDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                confirmDialog.show();
            }
        });

        itemHolder.fileName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (android.os.Build.VERSION.SDK_INT < 24) {
                    MimeTypeMap myMime = MimeTypeMap.getSingleton();
                    File file = new File(newItem.filePath);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    String mimeType = myMime.getMimeTypeFromExtension(newItem.filePath.substring(newItem.filePath.lastIndexOf(".") + 1));
                    intent.setDataAndType(Uri.fromFile(file), mimeType);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    try {
                        context.startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(context, "No handler for this type of file.", Toast.LENGTH_LONG).show();
                    }
                }
                /*else {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    File file = new File(newItem.filePath);
                    Uri data = FileProvider.getUriForFile(context, "com.example.memokeeper.fileProvider", file);
                TODO: Implement FileProvider for Android 8+
                }*/
            }
        });

    }
}
