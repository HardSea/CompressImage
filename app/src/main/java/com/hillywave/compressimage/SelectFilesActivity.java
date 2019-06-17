package com.hillywave.compressimage;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Stack;


public class SelectFilesActivity extends AppCompatActivity {


    private StorageFragment storageFragment = null;
    private final Stack<FolderFragment> fragments = new Stack<>();

    private ArrayList<FileInfo> listPathSelected;
    public int requestCode;
    private String folderSaveName;
    private String watermarkFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listPathSelected = new ArrayList<>();

        Toolbar toolBar = findViewById(R.id.toolbar);
        setSupportActionBar(toolBar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        Button btnSelectImage = toolBar.findViewById(R.id.btnSelect);
        TextView textViewToolBar = findViewById(R.id.toolBarText);


        Intent i = getIntent();
        requestCode = i.getIntExtra("request_code", 1);
        if (requestCode == 1) {
            textViewToolBar.setText(getApplicationContext().getString(R.string.text_select1));
        } else if (requestCode == 11) {
            textViewToolBar.setText(getApplicationContext().getString(R.string.text_select2));
        } else if (requestCode == 41) {
            textViewToolBar.setText(getApplicationContext().getString(R.string.text_select1));
        }

        btnSelectImage.setOnClickListener(view -> {

            if (requestCode == 1) {
                if (listPathSelected.size() > 0) {

                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("result", listPathSelected);
                    setResult(Activity.RESULT_OK, resultIntent);
                    finish();

                }

            } else if (requestCode == 11) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("resultNameFolder", folderSaveName);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            } else if (requestCode == 41) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("resultWatermarkImgName", watermarkFile);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }

        });

        if (!checkPermissionForWriteExtertalStorage()) {
            try {
                requestPermissionForWriteExtertalStorage();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        String[] storages = storages();


        if (storages.length > 1) {
            storageFragment = StorageFragment.newInstance(storages);

            FragmentManager fragmentManager = getSupportFragmentManager();

            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.add(R.id.fragmentContainer, storageFragment);
            transaction.addToBackStack(null);
            transaction.commitAllowingStateLoss();

        } else {
            String root = Environment.getExternalStorageDirectory().getAbsolutePath();
            FolderFragment folderFragment = FolderFragment.newInstance(root);

            addFragment(folderFragment, false);
        }


    }

    void addElement(FileInfo info) {
        listPathSelected.add(info);
    }

    void removeAll() {
        listPathSelected.clear();
    }

    void removeElement(FileInfo info) {
        listPathSelected.remove(info);
    }


    private String[] storages() {
        List<String> storages = new ArrayList<>();

        try {

            File[] externalStorageFiles = ContextCompat.getExternalFilesDirs(this, null);

            String base = String.format("/Android/data/%s/files", getPackageName());

            if (requestCode != 11) {
                for (File file : externalStorageFiles) {
                    try {
                        if (file != null) {
                            String path = file.getAbsolutePath();

                            if (path.contains(base)) {
                                String finalPath = path.replace(base, "");

                                if (validPath(finalPath)) {
                                    storages.add(finalPath);
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else if (requestCode == 11) {
                File file = externalStorageFiles[0];
                try {
                    if (file != null) {
                        String path = file.getAbsolutePath();

                        if (path.contains(base)) {
                            String finalPath = path.replace(base, "");

                            if (validPath(finalPath)) {
                                storages.add(finalPath);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        String[] result = new String[storages.size()];
        storages.toArray(result);

        return result;
    }

    private boolean validPath(String path) {
        try {
            StatFs stat = new StatFs(path);
            stat.getBlockCount();

            return true;
        } catch (Exception e) {
            e.printStackTrace();

            return false;
        }
    }

    public void addFragment(FolderFragment fragment, boolean addToBackStack) {
        fragments.push(fragment);

        FragmentManager fragmentManager = getSupportFragmentManager();

        FragmentTransaction transaction = fragmentManager.beginTransaction();

        if (addToBackStack) {
            transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right);
        }

        transaction.add(R.id.fragmentContainer, fragment);

        if (addToBackStack) {
            transaction.addToBackStack(null);
        }

        transaction.commitAllowingStateLoss();

        folderSaveName = fragment.folderName();

    }

    private void removeFragment(FolderFragment fragment) {
        fragments.pop();

        FragmentManager fragmentManager = getSupportFragmentManager();

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right);
        transaction.remove(fragment);
        transaction.commitAllowingStateLoss();

        if (!fragments.isEmpty()) {
            FolderFragment topFragment = fragments.peek();
            topFragment.refreshFolder();

            folderSaveName = topFragment.folderName();
        }

    }

    void updateWatermarkImage(String path) {
        watermarkFile = path;
    }

    @Override
    public void onBackPressed() {
        if (fragments.size() > 0) {
            FolderFragment fragment = fragments.peek();

            if (fragment.onBackPressed()) {
                if (storageFragment == null) {
                    if (fragments.size() > 1) {
                        removeFragment(fragment);
                    } else {
                        finish();
                    }
                } else {
                    removeFragment(fragment);
                }
            }
        } else {
            finish();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
    }


    public void requestPermissionForWriteExtertalStorage() {
        try {
            int READ_STORAGE_PERMISSION_REQUEST_CODE = 1;
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, READ_STORAGE_PERMISSION_REQUEST_CODE);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


        } else {
            Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.text_request), Toast.LENGTH_LONG).show();

            try {
                requestPermissionForWriteExtertalStorage();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public boolean checkPermissionForWriteExtertalStorage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int result = this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return result == PackageManager.PERMISSION_GRANTED;
        }
        return false;
    }


}
