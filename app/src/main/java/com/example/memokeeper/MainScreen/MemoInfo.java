package com.example.memokeeper.MainScreen;

public class MemoInfo {
    public String memoTitle;
    public String memoText;
    public int memoDate;
    public String memoAttachment;
    public String hash;

    public MemoInfo(String title, int date, String text, String attachment, String hashID) {
        memoTitle = title;
        memoDate = date;
        memoText = text;
        memoAttachment = attachment;
        hash = hashID;
    }
}
