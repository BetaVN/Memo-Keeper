package com.example.memokeeper.GoogleDriveHelper;

/**
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.memokeeper.Constants.REQUEST_CODE;
import com.example.memokeeper.MainScreen.MemoInfo;
import com.example.memokeeper.Utilities.PathUtils;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * A utility for performing read/write operations on Drive files via the REST API and opening a
 * file picker UI via Storage Access Framework.
 */
public class DriveServiceHelper {

    public Context context;

    public static String TYPE_AUDIO = "application/vnd.google-apps.audio";
    public static String TYPE_GOOGLE_DOCS = "application/vnd.google-apps.document";
    public static String TYPE_GOOGLE_DRAWING = "application/vnd.google-apps.drawing";
    public static String TYPE_GOOGLE_DRIVE_FILE = "application/vnd.google-apps.file";
    public static String TYPE_GOOGLE_DRIVE_FOLDER = DriveFolder.MIME_TYPE;
    public static String TYPE_GOOGLE_FORMS = "application/vnd.google-apps.form";
    public static String TYPE_GOOGLE_FUSION_TABLES = "application/vnd.google-apps.fusiontable";
    public static String TYPE_GOOGLE_MY_MAPS = "application/vnd.google-apps.map";
    public static String TYPE_PHOTO = "application/vnd.google-apps.photo";
    public static String TYPE_GOOGLE_SLIDES = "application/vnd.google-apps.presentation";
    public static String TYPE_GOOGLE_APPS_SCRIPTS = "application/vnd.google-apps.script";
    public static String TYPE_GOOGLE_SITES = "application/vnd.google-apps.site";
    public static String TYPE_GOOGLE_SHEETS = "application/vnd.google-apps.spreadsheet";
    public static String TYPE_UNKNOWN = "application/vnd.google-apps.unknown";
    public static String TYPE_VIDEO = "application/vnd.google-apps.video";
    public static String TYPE_3_RD_PARTY_SHORTCUT = "application/vnd.google-apps.drive-sdk";

    private final Executor mExecutor = Executors.newSingleThreadExecutor();
    private final Drive mDriveService;

    public DriveServiceHelper(Drive driveService, Context profile_page) {
        mDriveService = driveService;
        context = profile_page;
    }

    /**
     * Creates a text file in the user's My Drive folder and returns its file ID.
     */
    public Task<String> createFile() {
        return Tasks.call(mExecutor, () -> {
            File metadata = new File()
                    .setParents(Collections.singletonList("root"))
                    .setMimeType("text/plain")
                    .setName("Untitled file");

            File googleFile = mDriveService.files().create(metadata).execute();
            if (googleFile == null) {
                throw new IOException("Null result when requesting file creation.");
            }

            return googleFile.getId();
        });
    }

    /**
     * Opens the file identified by {@code fileId} and returns a {@link Pair} of its name and
     * contents.
     */
    public Task<Pair<String, String>> readFile(String fileId) {
        return Tasks.call(mExecutor, () -> {
            // Retrieve the metadata as a File object.
            File metadata = mDriveService.files().get(fileId).execute();
            String name = metadata.getName();

            // Stream the file contents to a String.
            try (InputStream is = mDriveService.files().get(fileId).executeMediaAsInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                StringBuilder stringBuilder = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                String contents = stringBuilder.toString();

                return Pair.create(name, contents);
            }
        });
    }

    /**
     * Updates the file identified by {@code fileId} with the given {@code name} and {@code
     * content}.
     */
    public Task<Void> saveFile(String fileId, String name, String content) {
        return Tasks.call(mExecutor, () -> {
            // Create a File containing any metadata changes.
            File metadata = new File().setName(name);

            // Convert content to an AbstractInputStreamContent instance.
            ByteArrayContent contentStream = ByteArrayContent.fromString("text/plain", content);

            // Update the metadata and contents.
            mDriveService.files().update(fileId, metadata, contentStream).execute();
            return null;
        });
    }

    /**
     * Returns a {@link FileList} containing all the visible files in the user's My Drive.
     *
     * <p>The returned list will only contain files visible to this app, i.e. those which were
     * created by this app. To perform operations on files not created by the app, the project must
     * request Drive Full Scope in the <a href="https://play.google.com/apps/publish">Google
     * Developer's Console</a> and be submitted to Google for verification.</p>
     */
    public Task<FileList> queryFiles() {
        return Tasks.call(mExecutor, () ->
                mDriveService.files().list().setSpaces("drive").execute());
    }

    /**
     * Returns an {@link Intent} for opening the Storage Access Framework file picker.
     */
    public Intent createFilePickerIntent() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");

        return intent;
    }

    /**
     * Opens the file at the {@code uri} returned by a Storage Access Framework {@link Intent}
     * created by {@link #createFilePickerIntent()} using the given {@code contentResolver}.
     */
    public Task<Pair<String, String>> openFileUsingStorageAccessFramework(
            ContentResolver contentResolver, Uri uri) {
        return Tasks.call(mExecutor, () -> {
            // Retrieve the document's display name from its metadata.
            String name;
            try (Cursor cursor = contentResolver.query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    name = cursor.getString(nameIndex);
                } else {
                    throw new IOException("Empty cursor returned for file.");
                }
            }

            // Read the document's contents as a String.
            String content;
            try (InputStream is = contentResolver.openInputStream(uri);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                content = stringBuilder.toString();
            }

            return Pair.create(name, content);
        });
    }

    public Task<GoogleDriveFileHolder> searchFile(String fileName, String mimeType) {
        return Tasks.call(mExecutor, () -> {

            FileList result = mDriveService.files().list()
                    .setQ("name = '" + fileName + "' and mimeType ='" + mimeType + "'")
                    .setSpaces("drive")
                    .setFields("files(id, name,size,createdTime,modifiedTime,starred)")
                    .execute();
            GoogleDriveFileHolder googleDriveFileHolder = new GoogleDriveFileHolder();
            if (result.getFiles().size() > 0) {

                googleDriveFileHolder.setId(result.getFiles().get(0).getId());
                googleDriveFileHolder.setName(result.getFiles().get(0).getName());
                googleDriveFileHolder.setModifiedTime(result.getFiles().get(0).getModifiedTime());
                googleDriveFileHolder.setSize(result.getFiles().get(0).getSize());
            }


            return googleDriveFileHolder;
        });
    }

    public Task<GoogleDriveFileHolder> searchFolder(String folderName) {
        return Tasks.call(mExecutor, () -> {
            FileList result = null;
            // Retrieve the metadata as a File object.
            try {
                result = mDriveService.files().list()
                        .setQ("mimeType = '" + DriveFolder.MIME_TYPE + "' and name = '" + folderName + "' ")
                        .setSpaces("drive")
                        .execute();
            } catch (UserRecoverableAuthIOException e) {
                ((Activity) context).startActivityForResult(e.getIntent(), REQUEST_CODE.DRIVE_AUTH);
            }
            GoogleDriveFileHolder googleDriveFileHolder = new GoogleDriveFileHolder();
            if ((result != null) &&(result.getFiles().size() > 0)) {
                googleDriveFileHolder.setId(result.getFiles().get(0).getId());
                googleDriveFileHolder.setName(result.getFiles().get(0).getName());

            }
            return googleDriveFileHolder;
        });
    }

    public Task<List<GoogleDriveFileHolder>> queryFiles(@Nullable final String folderId) {
        return Tasks.call(mExecutor, new Callable<List<GoogleDriveFileHolder>>() {
                    @Override
                    public List<GoogleDriveFileHolder> call() throws Exception {
                        List<GoogleDriveFileHolder> googleDriveFileHolderList = new ArrayList<>();
                        String parent = "root";
                        if (folderId != null) {
                            parent = folderId;
                        }

                        FileList result = mDriveService.files().list().setQ("'" + parent + "' in parents").setFields("files(id, name,size,createdTime,modifiedTime,starred)").setSpaces("drive").execute();

                        for (int i = 0; i < result.getFiles().size(); i++) {
                            GoogleDriveFileHolder googleDriveFileHolder = new GoogleDriveFileHolder();
                            googleDriveFileHolder.setId(result.getFiles().get(i).getId());
                            googleDriveFileHolder.setName(result.getFiles().get(i).getName());
                            if (result.getFiles().get(i).getSize() != null) {
                                googleDriveFileHolder.setSize(result.getFiles().get(i).getSize());
                            }

                            if (result.getFiles().get(i).getModifiedTime() != null) {
                                googleDriveFileHolder.setModifiedTime(result.getFiles().get(i).getModifiedTime());
                            }

                            if (result.getFiles().get(i).getCreatedTime() != null) {
                                googleDriveFileHolder.setCreatedTime(result.getFiles().get(i).getCreatedTime());
                            }

                            if (result.getFiles().get(i).getStarred() != null) {
                                googleDriveFileHolder.setStarred(result.getFiles().get(i).getStarred());
                            }

                            googleDriveFileHolderList.add(googleDriveFileHolder);

                        }


                        return googleDriveFileHolderList;


                    }
                }
        );
    }

    public Task<GoogleDriveFileHolder> createFolder(String folderName, @Nullable String folderId) {
        return Tasks.call(mExecutor, () -> {

            GoogleDriveFileHolder googleDriveFileHolder = new GoogleDriveFileHolder();

            List<String> root;
            if (folderId == null) {
                root = Collections.singletonList("root");
            } else {

                root = Collections.singletonList(folderId);
            }
            File metadata = new File()
                    .setParents(root)
                    .setMimeType(DriveFolder.MIME_TYPE)
                    .setName(folderName);

            File googleFile = mDriveService.files().create(metadata).execute();
            if (googleFile == null) {
                throw new IOException("Null result when requesting file creation.");
            }
            googleDriveFileHolder.setId(googleFile.getId());
            return googleDriveFileHolder;
        });
    }

    public Task<GoogleDriveFileHolder> uploadFile(final java.io.File localFile, final String mimeType, @Nullable final String folderId) {
        return Tasks.call(mExecutor, new Callable<GoogleDriveFileHolder>() {
            @Override
            public GoogleDriveFileHolder call() throws Exception {
                // Retrieve the metadata as a File object.

                List<String> root;
                if (folderId == null) {
                    root = Collections.singletonList("root");
                } else {

                    root = Collections.singletonList(folderId);
                }

                File metadata = new File()
                        .setParents(root)
                        .setMimeType(mimeType)
                        .setName(localFile.getName());

                FileContent fileContent = new FileContent(mimeType, localFile);

                File fileMeta = mDriveService.files().create(metadata, fileContent).execute();
                GoogleDriveFileHolder googleDriveFileHolder = new GoogleDriveFileHolder();
                googleDriveFileHolder.setId(fileMeta.getId());
                googleDriveFileHolder.setName(fileMeta.getName());
                return googleDriveFileHolder;
            }
        });
    }

    public Task<Void> downloadFile(java.io.File targetFile, String fileId) {
        return Tasks.call(mExecutor, () -> {
            // Retrieve the metadata as a File object.
            OutputStream outputStream = new FileOutputStream(targetFile);
            mDriveService.files().get(fileId).executeMediaAndDownloadTo(outputStream);
            return null;
        });
    }

    public Task<Void> deleteFolderFile(String fileId) {
        return Tasks.call(mExecutor, () -> {
            // Retrieve the metadata as a File object.
            if (fileId != null) {
                mDriveService.files().delete(fileId).execute();
            }

            return null;

        });
    }

    public Task<Void> checkAllTaskProgress(ArrayList<Task<?>> allTasks) {
        return Tasks.call(mExecutor, () -> {
            Boolean allTaskCompleted = false;
            while (!allTaskCompleted) {
                allTaskCompleted = true;
                for (Task<?> task : allTasks) {
                    if (!task.isComplete()) {
                        allTaskCompleted = false;
                        break;
                    }
                }
            }
            return null;
        });
    }

    public Task<Void> uploadBackupProcess(ArrayList<MemoInfo> allMemo, String backupFolderID, ProgressBar progressTracker, TextView taskName) {
        return Tasks.call(mExecutor, () -> {
            int doneTask = 0;
            progressTracker.setMax(allMemo.size());
            for (MemoInfo memoInfo : allMemo) {
                doneTask += 1;
                FileList result = null;
                // Retrieve the metadata as a File object.
                try {
                    result = mDriveService.files().list()
                            .setQ("mimeType = '" + DriveFolder.MIME_TYPE + "' and name = '" + memoInfo.hash + "' ")
                            .setSpaces("drive")
                            .execute();
                } catch (UserRecoverableAuthIOException e) {
                    ((Activity) context).startActivityForResult(e.getIntent(), REQUEST_CODE.DRIVE_AUTH);
                }
                GoogleDriveFileHolder googleDriveFileHolder = new GoogleDriveFileHolder();
                if ((result != null) &&(result.getFiles().size() > 0)) {
                    googleDriveFileHolder.setId(result.getFiles().get(0).getId());
                    googleDriveFileHolder.setName(result.getFiles().get(0).getName());

                }

                if (googleDriveFileHolder.getId() != null) {
                    mDriveService.files().delete(googleDriveFileHolder.getId()).execute();
                }

                GoogleDriveFileHolder newFolder = new GoogleDriveFileHolder();
                String folderName = memoInfo.hash;

                List<String> root;
                root = Collections.singletonList(backupFolderID);

                File metadata = new File()
                        .setParents(root)
                        .setMimeType(DriveFolder.MIME_TYPE)
                        .setName(folderName);

                File googleFile = mDriveService.files().create(metadata).execute();
                if (googleFile == null) {
                    throw new IOException("Null result when requesting file creation.");
                }
                newFolder.setId(googleFile.getId());
                createNewMemoTextFile(memoInfo);
                java.io.File[] memoFiles = new java.io.File(context.getFilesDir(), folderName).listFiles();
                for (java.io.File memoFile: memoFiles) {
                    List<String> root2;
                    root2 = Collections.singletonList(newFolder.getId());
                    String mimeType = getMimeType(memoFile);
                    File metadata2 = new File()
                            .setParents(root2)
                            .setMimeType(mimeType)
                            .setName(memoFile.getName());

                    FileContent fileContent = new FileContent(mimeType, memoFile);

                    File fileMeta = mDriveService.files().create(metadata2, fileContent).execute();
                    GoogleDriveFileHolder newFile = new GoogleDriveFileHolder();
                    newFile.setId(fileMeta.getId());
                    newFile.setName(fileMeta.getName());
                }
                progressTracker.setProgress(doneTask);
            }
            return null;
        });
    }

    public Task<Void> downloadBackupProcess(String backupFolderId, ProgressBar progressTracker, TextView taskName) {
        return Tasks.call(mExecutor, () -> {
            List<GoogleDriveFileHolder> allMemoFolder = findAllFiles(backupFolderId);
            if (allMemoFolder.isEmpty()) {
                return null;
            }
            int doneTask = 0;
            progressTracker.setMax(allMemoFolder.size() + 1);
            for (GoogleDriveFileHolder folder: allMemoFolder) {
                doneTask += 1;
                java.io.File memoDir = new java.io.File(context.getFilesDir(), folder.getName());
                if (memoDir.exists()) {
                    PathUtils.folderClean(memoDir);
                }
                memoDir.mkdir();
                List<GoogleDriveFileHolder> allFilesInFolder = findAllFiles(folder.getId());
                if (allFilesInFolder.isEmpty()) {
                    continue;
                }
                for (GoogleDriveFileHolder file: allFilesInFolder) {
                    java.io.File targetFile = new java.io.File(memoDir, file.getName());
                    OutputStream outputStream = new FileOutputStream(targetFile);
                    mDriveService.files().get(file.getId()).executeMediaAndDownloadTo(outputStream);
                }
                progressTracker.setProgress(doneTask);
            }
            return null;
        });
    }

    private String getMimeType(java.io.File target) {
        MimeTypeMap myMime = MimeTypeMap.getSingleton();
        String mimeType = myMime.getMimeTypeFromExtension(target.getPath().substring(target.getPath().lastIndexOf(".") + 1));
        return  mimeType;
    }

    private void createNewMemoTextFile(MemoInfo memoInfo) {
        StringBuilder memoData = new StringBuilder();
        memoData.append(memoInfo.memoTitle + "\n");
        memoData.append(memoInfo.memoDate + "\n");
        memoData.append(memoInfo.memoAttachment + "\n");
        memoData.append(memoInfo.hash + "\n");
        memoData.append("\n");
        memoData.append(memoInfo.memoText + "\n");

        try {
            java.io.File newText = new java.io.File(context.getFilesDir() + "/" + memoInfo.hash + "/", memoInfo.hash + ".txt");
            FileWriter writer = new FileWriter(newText);
            writer.append(memoData.toString());
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public MemoInfo readFromMemoTextFile(java.io.File text) {
        String memoTitle = "";
        String memoAttachment = "";
        String hash = "";
        int memoDate = 0;
        StringBuilder memoText = new StringBuilder();
        memoText.append("");
        try {
            java.io.BufferedReader buf = new BufferedReader(new FileReader(text));
            String line;


            line = buf.readLine();
            memoTitle = line;
            line = buf.readLine();
            memoDate = Integer.parseInt(line);
            line = buf.readLine();
            memoAttachment = line;
            line = buf.readLine();
            hash = line;
            line = buf.readLine();
            while ((line = buf.readLine()) != null) {
                memoText.append(line + "\n");
            }
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        MemoInfo newInfo = new MemoInfo(memoTitle, memoDate, memoText.toString(), memoAttachment, hash);
        Log.d("Memo Reader: ", "Title = " + memoTitle);
        Log.d("Memo Reader: ", "Date = " + memoDate);
        Log.d("Memo Reader: ", "Text = " + memoText.toString());
        Log.d("Memo Reader: ", "Attachment = " + memoAttachment);
        Log.d("Memo Reader: ", "Hash = " + hash);
        return newInfo;
    }

    private List<GoogleDriveFileHolder> findAllFiles(String folderId) {
        List<GoogleDriveFileHolder> googleDriveFileHolderList = new ArrayList<>();
        String parent = "root";
        if (folderId != null) {
            parent = folderId;
        }
        try {
            FileList result = mDriveService.files().list().setQ("'" + parent + "' in parents").setFields("files(id, name,size,createdTime,modifiedTime,starred)").setSpaces("drive").execute();

            for (int i = 0; i < result.getFiles().size(); i++) {

                GoogleDriveFileHolder googleDriveFileHolder = new GoogleDriveFileHolder();
                googleDriveFileHolder.setId(result.getFiles().get(i).getId());
                googleDriveFileHolder.setName(result.getFiles().get(i).getName());
                if (result.getFiles().get(i).getSize() != null) {
                    googleDriveFileHolder.setSize(result.getFiles().get(i).getSize());
                }

                if (result.getFiles().get(i).getModifiedTime() != null) {
                    googleDriveFileHolder.setModifiedTime(result.getFiles().get(i).getModifiedTime());
                }

                if (result.getFiles().get(i).getCreatedTime() != null) {
                    googleDriveFileHolder.setCreatedTime(result.getFiles().get(i).getCreatedTime());
                }

                if (result.getFiles().get(i).getStarred() != null) {
                    googleDriveFileHolder.setStarred(result.getFiles().get(i).getStarred());
                }

                googleDriveFileHolderList.add(googleDriveFileHolder);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return googleDriveFileHolderList;
    }
}