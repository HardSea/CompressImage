package com.hillywave.compressimage;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
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
                    if (selectFilesActivity.requestCode == 41) {
                        adapter.unselectAll();
                        adapter.updateSelection(fileInfo.toggleSelection());
                        selectFilesActivity.updateWatermarkImage(fileInfo.path());
                    }
                }

            } else {
                if (fileInfo.isDirectory()) {
                    openFolder(fileInfo);
                } else {
                    if (selectFilesActivity.requestCode != 11) {
                        if (fileInfo.isImage()) {
                            adapter.updateSelection(fileInfo.toggleSelection());
                            selectFilesActivity.addElement(fileInfo);
                            if (selectFilesActivity.requestCode == 41) {
                                selectFilesActivity.updateWatermarkImage(fileInfo.path());
                            }
                        }
                    }

                }
            }
        });

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            if (selectFilesActivity.requestCode != 41) {
                FileInfo fileInfo = (FileInfo) parent.getItemAtPosition(position);
                if (fileInfo.isDirectory() || fileInfo.isImage()) {
                        adapter.updateSelection(fileInfo.toggleSelection());
                        selectFilesActivity.addElement(fileInfo);
                    }
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
        selectFilesActivity.removeAll();
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



}
