package com.hillywave.compressimage;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;


import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import id.zelory.compressor.Compressor;

import static com.hillywave.compressimage.SelectFilesActivity.REQUEST_CODE_STORAGE_ACCESS;
import static com.hillywave.compressimage.SelectFilesActivity.useSplitToRemoveLastPart;

public class CompressActivity extends AppCompatActivity {
    private TableLayout table;
    private LinearLayout mainLayout;
    private ArrayList<FileInfo> listOfFiles;
    private ThumbnailLoader thumbnailLoader;
    private TextView textViewCntImages;
    private String TAG = ".CompressActivity";
    private String SAVE_DIRECTORY = "/compressfolder";
    public static final int FOLDER = 123;
    public static final int FOLDER_REQUEST = 342;
    private int qualityImage = 80;
    private EditText editTextSaveFolder;
    private Bitmap.CompressFormat compressFormat;
    private String folderSaveName;
    private ArrayList<FileInfo> listSelected;
    private ProgressDialog dialog;
    private long sizeAfter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compress);

        mainLayout = findViewById(R.id.mainLayout);
        this.thumbnailLoader = new ThumbnailLoader(this.getResources());

        File saveDirectory = new File(Environment.getExternalStorageDirectory() + SAVE_DIRECTORY);
        folderSaveName = saveDirectory.getPath();

        dialog = new ProgressDialog(CompressActivity.this);
        dialog.setTitle("Зачекайте");
        dialog.setMessage("Триває обробка зображень");

        editTextSaveFolder = findViewById(R.id.editTextSaveFolder);
        editTextSaveFolder.setText(saveDirectory.getPath());
        editTextSaveFolder.setOnClickListener(view -> btnSelectFolder());

        Spinner spinner = findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String[] choise = getResources().getStringArray(R.array.format_names);
                switch (choise[i]) {
                    case "Стандартний":
                        compressFormat = null;
                        break;
                    case "JPEG":
                        compressFormat = Bitmap.CompressFormat.JPEG;
                        break;
                    case "PNG":
                        compressFormat = Bitmap.CompressFormat.PNG;
                        break;
                    case "WEBP":
                        compressFormat = Bitmap.CompressFormat.WEBP;
                        break;
                    default:
                        break;

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        Button btnCompress = findViewById(R.id.btnCompress);
        btnCompress.setOnClickListener(v -> {
            dialog.show();
            Thread t = new Thread(this::compress);
            t.start();
        });

        textViewCntImages = findViewById(R.id.textViewCntImage);

        TextView textViewCompressQuality = findViewById(R.id.textViewCompressQuality);
        SeekBar seekBar = findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (seekBar.getProgress() == 0)
                    seekBar.setProgress(1);
                qualityImage = seekBar.getProgress();
                textViewCompressQuality.setText(seekBar.getProgress() + "");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        Intent intent = getIntent();
        listOfFiles = (ArrayList<FileInfo>) intent.getSerializableExtra("listoffiles");

        Button btnSelectFolder = findViewById(R.id.btnSelectFolder);
        btnSelectFolder.setOnClickListener(v -> btnSelectFolder());


        table = findViewById(R.id.tableLayout);
        //TableLayout.LayoutParams tableParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT);
        //table.setLayoutParams(tableParams);

        Thread t = new Thread(() -> {
            ArrayList<FileInfo> tempArr = new ArrayList<>();
            for (int i = 0; i < listOfFiles.size(); i++) {
                if (listOfFiles.get(i).isDirectory()) {
                    if (listOfFiles.get(i).hasFiles()) {
                        tempArr.addAll(listOfFiles.get(i).getImages());
                        listOfFiles.remove(listOfFiles.get(i));
                    }
                }
            }
            listOfFiles.addAll(tempArr);
            paintTable();

        });
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if ((msg.what + 1) < listOfFiles.size())
                dialog.setMessage("Триває обробка зображень: " + msg.what + "/" + listOfFiles.size());
            else if ((msg.what + 1) >= listOfFiles.size()){
                dialog.dismiss();
                ViewDialog alert = new ViewDialog();
                alert.showDialog(CompressActivity.this,"Опрацьовано файлів: " + listOfFiles.size(), "Розмір до стиснення: " + calSize(), "Розмір після стиснення: " + calSizeAfter(), true);
            }
            return true;
        }
    });

    void compress() {
        try {
            File myFile;
            for (int i = 0; i < listOfFiles.size(); i++) {
                myFile = listOfFiles.get(i).getFIle();
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE_ACCESS);
                File compressFile;
                if (compressFormat == null) {
                    compressFile = new Compressor(this).setDestinationDirectoryPath(folderSaveName).setQuality(qualityImage).compressToFile(myFile, myFile.getName());
                } else {
                    compressFile = new Compressor(this).setDestinationDirectoryPath(folderSaveName).setQuality(qualityImage).setCompressFormat(compressFormat).compressToFile(myFile, myFile.getName());
                }
                compressFile.setLastModified(myFile.lastModified());
                handler.sendEmptyMessage(i + 1);
                sizeAfter += compressFile.length();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    void paintTable() {
        try {
            int rowCount = cntRow();
            int columnCount = cntColumn();
            int size = listOfFiles.size();
            if (size > 500) {
                size = 500;
            }
            int temp = 0;
            for (int i = 0; i < rowCount; i++) {
                TableRow row = new TableRow(CompressActivity.this);
                TableRow.LayoutParams paramsTable = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
                row.setLayoutParams(paramsTable);
                for (int j = 0; j < columnCount; j++) {
                    if (temp < size) {
                        ImageView iv = new ImageView(this);
                        int finalTemp = temp;
                        iv.setOnClickListener(view -> {
                            Log.d(TAG, "paintTable: " + listOfFiles.get(finalTemp).name());
                            Log.d(TAG, "paintTable: " + listOfFiles.get(finalTemp).path());
                            if (listOfFiles.size() > 1) {
                                table.removeAllViews();
                                listOfFiles.remove(finalTemp);
                                paintTable();
                            }
                        });
                        thumbnailLoader.load(listOfFiles.get(temp), iv);
                        iv.setLayoutParams(new TableRow.LayoutParams(125, 125));
                        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);

                        row.addView(iv);
                        temp++;
                    }
                }
                row.setGravity(Gravity.CENTER);
                table.addView(row);
            }
            textViewCntImages.setText("Кількість: " + listOfFiles.size() + " Загальний розмір: " + calSize());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int cntRow() {
        return (int) Math.ceil((double) listOfFiles.size() / 5);
    }

    private int cntColumn() {
        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        float width = displayMetrics.widthPixels;
        return (int) ((width - 40) / 125);
    }

    public void btnSelectFolder() {
        Intent i = new Intent(this, SelectFilesActivity.class);
        i.putExtra("request_code", 11);
        startActivityForResult(i, 11);

    }

    private String calSizeAfter(){
        SpaceFormatter spaceFormatter = new SpaceFormatter();
        return spaceFormatter.format(sizeAfter);
    }

    private String calSize() {

        SpaceFormatter spaceFormatter = new SpaceFormatter();
        long tempSize = 0;
        for (int i = 0; i < listOfFiles.size(); i++) {
            tempSize += listOfFiles.get(i).getFIle().length();
        }
        return spaceFormatter.format(tempSize);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {

                assert data != null;
                listSelected = (ArrayList<FileInfo>) data.getSerializableExtra("result");
                //Intent intent = new Intent(this, CompressActivity.class);
                //intent.putExtra("listoffiles", listSelected);
                //startActivity(intent);
            } else if (requestCode == 11) {
                // TODO выбрать ссылку для сохранения
                folderSaveName = data.getStringExtra("resultNameFolder");
                editTextSaveFolder.setText(folderSaveName);

            }

        } else if (resultCode == Activity.RESULT_CANCELED) {

        }
    }


}
