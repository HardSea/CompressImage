package com.hillywave.compressimage;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.provider.DocumentFile;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Stack;

import id.zelory.compressor.Compressor;

public class SelectFilesActivity extends AppCompatActivity {


    private StorageFragment storageFragment = null;
    private final Stack<FolderFragment> fragments = new Stack<>();
    private Toolbar toolBar;

    public static final int GALLERY = 1;
    public static final int REQUEST_CODE_STORAGE_ACCESS = 42;

    private ImageView imageView;
    public String CAMERA_IMAGE_BUCKET_NAME;
    public String CAMERA_IMAGE_BUCKET_ID;
    //private String IMAGE_DIRECTORY = "/testfolder";
    private ArrayList imagesEncodedList;
    private String imageEncoded;
    private ArrayList<FileInfo> listPathSelected;
    String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listPathSelected = new ArrayList<>();

        toolBar = findViewById(R.id.toolbar);
        setSupportActionBar(toolBar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        Button btnSelectImage = toolBar.findViewById(R.id.btnSelect);

        btnSelectImage.setOnClickListener(view -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("result", listPathSelected);
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        });

        if (!checkPermissionForWriteExtertalStorage()) {
            try {
                requestPermissionForWriteExtertalStorage();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        String[] storages = storages();
        Log.d(TAG, "onCreate: STORAGES " + storages);


        if (storages.length > 1)
        {
            storageFragment = StorageFragment.newInstance(storages);

            FragmentManager fragmentManager = getSupportFragmentManager();

            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.add(R.id.fragmentContainer, storageFragment);
            Log.d(TAG, "onCreate: add fragment ");
            transaction.addToBackStack(null);
            transaction.commitAllowingStateLoss();

        }
        else
        {
            String root = Environment.getExternalStorageDirectory().getAbsolutePath();
            FolderFragment folderFragment = FolderFragment.newInstance(root);

            addFragment(folderFragment, false);
        }


        CAMERA_IMAGE_BUCKET_NAME = Environment.getExternalStorageDirectory().toString() + "/DCIM/Camera";
        CAMERA_IMAGE_BUCKET_ID = getBucketId(CAMERA_IMAGE_BUCKET_NAME);


        File[] externalStorageFiles = ContextCompat.getExternalFilesDirs(this, null);
        String base = String.format("/Android/data/%s/files", getPackageName());


    }

    void addElement(FileInfo info){
        listPathSelected.add(info);
    }

    private String[] storages() {
        List<String> storages = new ArrayList<>();

        try {
            File[] externalStorageFiles = ContextCompat.getExternalFilesDirs(this, null);

            String base = String.format("/Android/data/%s/files", getPackageName());

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

    public void addFragment(FolderFragment fragment, boolean addToBackStack)
    {
        fragments.push(fragment);

        FragmentManager fragmentManager = getSupportFragmentManager();

        FragmentTransaction transaction = fragmentManager.beginTransaction();

        if (addToBackStack)
        {
            transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right);
        }

        transaction.add(R.id.fragmentContainer, fragment);

        if (addToBackStack)
        {
            transaction.addToBackStack(null);
        }

        transaction.commitAllowingStateLoss();

    }

    private void removeFragment(FolderFragment fragment)
    {
        fragments.pop();

        FragmentManager fragmentManager = getSupportFragmentManager();

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right);
        transaction.remove(fragment);
        transaction.commitAllowingStateLoss();

        if (!fragments.isEmpty())
        {
            FolderFragment topFragment = fragments.peek();
            topFragment.refreshFolder();

        }
    }




    public List<String> getCameraImages(Context context) {
        final String[] projection = {MediaStore.Images.Media.DATA};
        final String selection = MediaStore.Images.Media.BUCKET_ID + " = ?";
        final String[] selectionArgs = {CAMERA_IMAGE_BUCKET_ID};
        final Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null);
        assert cursor != null;
        ArrayList<String> result = new ArrayList<String>(cursor.getCount());
        if (cursor.moveToFirst()) {
            final int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            do {
                final String data = cursor.getString(dataColumn);
                result.add(data);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return result;
    }


    public String getBucketId(String path) {
        return String.valueOf(path.toLowerCase().hashCode());
    }


    public void testBtn(View view) {
        //Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(galleryIntent, "Select picture"), GALLERY);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            if (requestCode == GALLERY) {

                imagesEncodedList = new ArrayList<String>();
                if (data.getData() != null) {

                    Uri mImageUri = data.getData();
                    //Log.d(TAG, "onActivityResult: " + mImageUri);
                    //saveImage(new File(Objects.requireNonNull(getPath(getApplicationContext(), mImageUri))));

                    DocumentsContract.deleteDocument(getContentResolver(), mImageUri);
                    //Log.d(TAG, "onActivityResult: " + getPath(getApplicationContext(), mImageUri));
                } else {
                    if (data.getClipData() != null) {
                        ClipData mClipData = data.getClipData();
                        ArrayList<Uri> mArrayUri = new ArrayList<>();

                        for (int i = 0; i < mClipData.getItemCount(); i++) {
                            ClipData.Item item = mClipData.getItemAt(i);
                            Uri uri = item.getUri();
                            mArrayUri.add(uri);
                        }
                        Log.d(TAG, "Selected Images " + mArrayUri.size());
                        for (int i = 0; i < mArrayUri.size(); i++) {
                            Log.d(TAG, "Selected Images " + i + " " + mArrayUri.get(i));
                        }
                        Log.d(TAG, "onActivityResult: " + getPath(getApplicationContext(), mArrayUri.get(0)));

                        for (int i = 0; i < mArrayUri.size(); i++) {
                            saveImage(new File(Objects.requireNonNull(getPath(getApplicationContext(), mArrayUri.get(i)))));
                        }

                    }
                }
            } else if (requestCode == REQUEST_CODE_STORAGE_ACCESS) {

                if (resultCode == Activity.RESULT_OK) {

                    Uri treeUri = data.getData();
                    Log.d(TAG, "onActivityResult: treeUri" + treeUri);
                    Log.d(TAG, "onActivityResult: treeUri to string" + treeUri.toString());
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("content_url", treeUri.toString());
                    editor.apply();
                    Log.d(TAG, "onActivityResult: SAVED");
                    // Persist URI - this is required for verification of writability.
                    // PreferenceUtil.setSharedPreferenceUri(preferenceKeyUri, treeUri);
                    DocumentFile pickedDir = DocumentFile.fromTreeUri(this, treeUri);
                    grantUriPermission(getPackageName(), treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    getContentResolver().takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    //copyFile(sdCard.toString(), "/File.txt", path + "/new", pickedDir);
                }

            } else {
                Toast.makeText(this, "You haven't picked Image",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                    .show();
            e.printStackTrace();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }


    public String getPath(final Context context, final Uri uri) {


        Log.d(TAG + " File -",
                "Authority: " + uri.getAuthority() +
                        ", Fragment: " + uri.getFragment() +
                        ", Port: " + uri.getPort() +
                        ", Query: " + uri.getQuery() +
                        ", Scheme: " + uri.getScheme() +
                        ", Host: " + uri.getHost() +
                        ", Segments: " + uri.getPathSegments().toString()
        );

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                } else {
                    // Below logic is how External Storage provider build URI for documents
                    // Based on http://stackoverflow.com/questions/28605278/android-5-sd-card-label and https://gist.github.com/prasad321/9852037
                    StorageManager mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);

                    try {
                        Class<?> storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
                        Method getVolumeList = Objects.requireNonNull(mStorageManager).getClass().getMethod("getVolumeList");
                        Method getUuid = storageVolumeClazz.getMethod("getUuid");
                        Method getState = storageVolumeClazz.getMethod("getState");
                        Method getPath = storageVolumeClazz.getMethod("getPath");
                        Method isPrimary = storageVolumeClazz.getMethod("isPrimary");
                        Method isEmulated = storageVolumeClazz.getMethod("isEmulated");

                        Object result = getVolumeList.invoke(mStorageManager);

                        final int length = Array.getLength(result);
                        for (int i = 0; i < length; i++) {
                            Object storageVolumeElement = Array.get(result, i);
                            //String uuid = (String) getUuid.invoke(storageVolumeElement);

                            final boolean mounted = Environment.MEDIA_MOUNTED.equals(getState.invoke(storageVolumeElement)) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(getState.invoke(storageVolumeElement));

                            //if the media is not mounted, we need not get the volume details
                            if (!mounted) continue;

                            //Primary storage is already handled.
                            if ((Boolean) isPrimary.invoke(storageVolumeElement) && (Boolean) isEmulated.invoke(storageVolumeElement))
                                continue;

                            String uuid = (String) getUuid.invoke(storageVolumeElement);

                            if (uuid != null && uuid.equals(type)) {
                                String res = getPath.invoke(storageVolumeElement) + "/" + split[1];
                                return res;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

            } else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }


    public String saveImage(File myFile) {
        // myBitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        Log.d(TAG, "saveImage: PATH " + myFile.getPath());
        //String[] path = myFile.getPath().split("/");
        Log.d(TAG, "saveImage: NEW PATH " + useSplitToRemoveLastPart(myFile.getPath()));

        //File wallpaperDirectory = new File(Environment.getExternalStorageDirectory() + IMAGE_DIRECTORY);
        File wallpaperDirectory = new File(useSplitToRemoveLastPart(myFile.getPath()));
        Log.d(TAG, "saveImage: exists " + wallpaperDirectory.exists());

        if (!wallpaperDirectory.exists()) {
            Log.d(TAG, "saveImage: mkdirs " + wallpaperDirectory.mkdirs());
        }

        try {
            //File f = new File(wallpaperDirectory, fileName);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE_ACCESS);
            //File f = new Compressor(this).setDestinationDirectoryPath(wallpaperDirectory.getPath()).compressToFile(myFile, myFile.getName());
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String url = prefs.getString("content_url", "");
            Log.d(TAG, "saveImage: URL " + url);
            File f = new Compressor(this).setDestinationDirectoryPath(url + "/download").compressToFile(myFile, myFile.getName());
            // Files.setLastModifiedTime(myFile.getPath(), new FileTime(484984984));
            Log.d(TAG, "saveImage: " + f.setLastModified(System.currentTimeMillis() - 21802245000L));
            Log.d(TAG, "saveImage: " + System.currentTimeMillis());
            Log.d(TAG, "saveImage: " + f.lastModified());
            //f.createNewFile();
            //FileOutputStream fo = new FileOutputStream(f);
            //MediaScannerConnection.scanFile(this,
            //         new String[]{f.getPath()},
            //        new String[]{"image/jpeg"}, null);
            //fo.close();
            Log.d(TAG, "File Saved--->" + f.getAbsolutePath());

            return f.getAbsolutePath();
        } catch (FileNotFoundException e) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                startActivityForResult(intent, REQUEST_CODE_STORAGE_ACCESS);
            }
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


    public void requestPermissionForWriteExtertalStorage() {
        try {
            int READ_STORAGE_PERMISSION_REQUEST_CODE = 1;
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, READ_STORAGE_PERMISSION_REQUEST_CODE);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String useSplitToRemoveLastPart(String str) {
        String[] arr = str.split("/");
        String result = "";
        if (arr.length > 0) {
            result = str.substring(0, str.lastIndexOf("/" + arr[arr.length - 1]));
        }
        return result;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


        } else {
            Toast.makeText(getApplicationContext(), "Додатку необхіден доступ для зчитування файлів", Toast.LENGTH_LONG).show();

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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void triggerStorageAccessFramework() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(intent, REQUEST_CODE_STORAGE_ACCESS);
    }


}
