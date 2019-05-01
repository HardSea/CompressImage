package com.hillywave.compressimage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FolderFragment extends Fragment {
    private static final String PARAMETER_FOLDER_PATH = "folder.path";

    private SelectFilesActivity selectFilesActivity;
    private SwipeRefreshLayout swipeContainer;
    String TAG = "FolderFragment";
    private ListView listView;
    private TextView labelNoItems;
    private FolderAdapter adapter;

    public static FolderFragment newInstance(String folderPath) {
        FolderFragment fragment = new FolderFragment();
        Bundle parameters = new Bundle();
        parameters.putSerializable(PARAMETER_FOLDER_PATH, folderPath);
        fragment.setArguments(parameters);

        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        selectFilesActivity = (SelectFilesActivity) context;
    }

    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.screen_folder, container, false);

        swipeContainer = view.findViewById(R.id.swipeContainer);
        listView = view.findViewById(R.id.list);
        labelNoItems = view.findViewById(R.id.label_noItems);

        return view;
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    public final void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        swipeContainer.setColorSchemeResources(R.color.blue1);
        swipeContainer.setOnRefreshListener(() -> {
            refreshFolder();
            swipeContainer.setRefreshing(false);
        });

        adapter = new FolderAdapter(selectFilesActivity);

        listView.setAdapter(adapter);


        listView.setOnItemClickListener((parent, view, position, id) -> {
            FileInfo fileInfo = (FileInfo) parent.getItemAtPosition(position);
            if (adapter.isSelectionMode()) {
                if (fileInfo.isDirectory() || fileInfo.isImage()) {
                    adapter.updateSelection(fileInfo.toggleSelection());
                    if (fileInfo.isSelected())
                        selectFilesActivity.addElement(fileInfo);
                    else
                        selectFilesActivity.removeElement(fileInfo);
                }
                Log.d(TAG, "onActivityCreated: " + fileInfo);
                Log.d(TAG, "onActivityCreated: " + fileInfo.path());
                Log.d(TAG, "onActivityCreated: " + fileInfo.name());
                Log.d(TAG, "onActivityCreated: " + fileInfo.isDirectory());
                Log.d(TAG, "onActivityCreated: " + fileInfo.isImage());
            } else {
                if (fileInfo.isDirectory()) {
                    openFolder(fileInfo);
                } else {
                    openFile(fileInfo);
                }
            }
        });

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            FileInfo fileInfo = (FileInfo) parent.getItemAtPosition(position);
            Log.d(TAG, "onActivityCreated: " + fileInfo);
            Log.d(TAG, "onActivityCreated: " + fileInfo.path());
            Log.d(TAG, "onActivityCreated: " + fileInfo.name());
            Log.d(TAG, "onActivityCreated: " + fileInfo.isDirectory());
            Log.d(TAG, "onActivityCreated: " + fileInfo.isImage());
            if (fileInfo.isDirectory() || fileInfo.isImage()) {
                adapter.updateSelection(fileInfo.toggleSelection());
                selectFilesActivity.addElement(fileInfo);
            }
            return true;
        });

        listView.setOnTouchListener((v, event) -> {
            if ((event.getAction() == MotionEvent.ACTION_DOWN) && listView.pointToPosition((int) (event.getX() * event.getXPrecision()), (int) (event.getY() * event.getYPrecision())) == -1) {
                onBackPressed();

                return true;
            }

            return false;
        });

        refreshFolder();
    }

    public synchronized boolean onBackPressed() {
        if ((adapter != null) && adapter.isSelectionMode()) {
            unselectAll();

            return false;
        } else {
            return true;
        }
    }

    private void unselectAll() {
        adapter.unselectAll();
    }


    public String folderName() {
        return folder().getAbsolutePath();
    }

    private File folder() {
        String folderPath = parameter(PARAMETER_FOLDER_PATH, "/");

        return new File(folderPath);
    }

    private List<FileInfo> fileList() {
        File root = folder();
        File[] fileArray = root.listFiles();

        if (fileArray != null) {
            List<File> files = Arrays.asList(fileArray);

            Collections.sort(files, (lhs, rhs) -> {
                if (lhs.isDirectory() && !rhs.isDirectory()) {
                    return -1;
                } else if (!lhs.isDirectory() && rhs.isDirectory()) {
                    return 1;
                } else {
                    return lhs.getName().toLowerCase().compareTo(rhs.getName().toLowerCase());
                }
            });

            List<FileInfo> result = new ArrayList<>();

            for (File file : files) {
                if (file != null) {
                    result.add(new FileInfo(file));
                }
            }

            return result;
        } else {
            return new ArrayList<>();
        }
    }

    @SuppressWarnings({"unchecked", "SameParameterValue"})
    private <Type> Type parameter(String key, Type defaultValue) {
        Bundle extras = getArguments();

        if ((extras != null) && extras.containsKey(key)) {
            return (Type) extras.get(key);
        } else {
            return defaultValue;
        }
    }

    private void openFolder(FileInfo fileInfo) {
        FolderFragment folderFragment = FolderFragment.newInstance(fileInfo.path());

        selectFilesActivity.addFragment(folderFragment, true);
    }

    private void openFile(FileInfo fileInfo) {
        try {
            String type = fileInfo.mimeType();
            Intent intent = openFileIntent(fileInfo.uri(getContext()), type);

            if (isResolvable(intent)) {
                startActivity(intent, R.string.open_unable);
            } else {
                showMessage(R.string.open_unable);
            }
        } catch (Exception e) {
            e.printStackTrace();

            showMessage(R.string.open_unable);
        }
    }

    private Intent openFileIntent(Uri uri, String type) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, type);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        return intent;
    }


    @SuppressLint("StaticFieldLeak")
    public void onPaste() {
        //Clipboard clipboard = mainActivity.clipboard();

//        String message = "";

//        if (clipboard.isCut()) {
//            message = getString(R.string.clipboard_cut);
//        } else if (clipboard.isCopy()) {
//            message = getString(R.string.clipboard_copy);
//        }
//
//        ProgressDialog dialog = Dialogs.progress(getContext(), message);
//
//        new AsyncTask<Void, Void, Void>() {
//            @Override
//            protected Void doInBackground(Void... params) {
//                clipboard.paste(new FileInfo(folder()));
//
//                return null;
//            }
//
//            @Override
//            protected void onPostExecute(Void result) {
//                try {
//                    dialog.dismiss();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//                refreshFolder();
//            }
//        }.execute();
    }


    private void createFolder(String name) {
//        File parent = folder();
//        File newFolder = new File(parent, name);
//
//        if (newFolder.mkdir()) {
//            refreshFolder();
//        } else {
//            showMessage("Create error");
//        }
    }

    @SuppressLint("StaticFieldLeak")
    private void deleteSelected(List<FileInfo> selectedItems) {
//        ProgressDialog dialog = Dialogs.progress(getContext(), getString(R.string.delete_deleting));
//
//        new AsyncTask<Void, Void, Boolean>() {
//            @Override
//            protected Boolean doInBackground(Void... params) {
//                boolean allDeleted = true;
//
//                for (FileInfo fileInfo : selectedItems) {
//                    if (!fileInfo.delete()) {
//                        allDeleted = false;
//                    }
//                }
//
//                return allDeleted;
//            }
//
//            @Override
//            protected void onPostExecute(Boolean result) {
//                try {
//                    dialog.dismiss();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//                refreshFolder();
//
//                if (!result) {
//                    showMessage("Delete error");
//                }
//            }
//        }.execute();
    }

    private void renameItem(FileInfo fileInfo, String newName) {
//        if (fileInfo.rename(newName)) {
//            refreshFolder();
//        } else {
//            showMessage("Rename error");
//        }
    }

    private void showMessage(@StringRes int text) {
        Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
    }

    public void refreshFolder() {
        List<FileInfo> files = fileList();
        adapter.setData(files);

        if (files.isEmpty()) {
            listView.setVisibility(View.GONE);
            labelNoItems.setVisibility(View.VISIBLE);
        } else {
            listView.setVisibility(View.VISIBLE);
            labelNoItems.setVisibility(View.GONE);
        }
    }

    private void startActivity(Intent intent, @StringRes int resId) {
        try {
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();

            showMessage(resId);
        }
    }

    private boolean isResolvable(Intent intent) {
        PackageManager manager = selectFilesActivity.getPackageManager();
        List<ResolveInfo> resolveInfo = manager.queryIntentActivities(intent, 0);

        return !resolveInfo.isEmpty();
    }


}
