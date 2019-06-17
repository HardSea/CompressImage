package com.hillywave.compressimage;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.Arrays;

public class StorageFragment extends Fragment {

    public static final String PARAMETER_STORAGES_PATH = "storages.path";

    private SelectFilesActivity selectFilesActivity;
    private ListView listView;
    private StorageAdapter adapter;


    public static StorageFragment newInstance(String[] storagesPath) {

        StorageFragment fragment = new StorageFragment();
        Bundle parameters = new Bundle();
        parameters.putStringArray(PARAMETER_STORAGES_PATH, storagesPath);
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
        View view = inflater.inflate(R.layout.screen_storage, container, false);

        listView = view.findViewById(R.id.list);

        return view;
    }

    @Override
    public final void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        adapter = new StorageAdapter(selectFilesActivity);

        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            String storagePath = (String) parent.getItemAtPosition(position);
            openStorage(storagePath);
        });

        reload();
    }

    public void reload() {
        adapter.update(Arrays.asList(storages()));
    }

    private String[] storages() {
        Bundle extras = getArguments();

        return (extras != null) ? extras.getStringArray(PARAMETER_STORAGES_PATH) : new String[0];
    }

    private void openStorage(String storagePath) {
        FolderFragment folderFragment = FolderFragment.newInstance(storagePath);

        selectFilesActivity.addFragment(folderFragment, true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // no call for super(). Bug on API Level > 11.
    }
}
