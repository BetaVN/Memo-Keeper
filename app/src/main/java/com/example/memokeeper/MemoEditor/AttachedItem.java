package com.example.memokeeper.MemoEditor;

public class AttachedItem {

    public String fileName;
    public boolean isImage;
    public String filePath;

    public AttachedItem(String name, boolean image, String path) {
        fileName = name;
        isImage = image;
        filePath = path;
    }
}
