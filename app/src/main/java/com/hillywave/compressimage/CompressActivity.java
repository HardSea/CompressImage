package com.hillywave.compressimage;

import android.app.Activity;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
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
import java.util.ArrayList;

public class CompressActivity extends AppCompatActivity {
    private TableLayout table;
    private LinearLayout mainLayout;
    private ArrayList<FileInfo> listOfFiles;
    private ThumbnailLoader thumbnailLoader;
    private String TAG = ".CompressActivity";
    private String SAVE_DIRECTORY = "/compressfolder";
    public static final int FOLDER = 123;
    public static final int FOLDER_REQUEST = 342;
    private EditText editTextSaveFolder;
    private File saveDirectory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compress);

        mainLayout = findViewById(R.id.mainLayout);
        this.thumbnailLoader = new ThumbnailLoader(this.getResources());

        saveDirectory = new File(Environment.getExternalStorageDirectory() + SAVE_DIRECTORY);

        editTextSaveFolder = findViewById(R.id.editTextSaveFolder);

        editTextSaveFolder.setText(saveDirectory.getPath());

        Spinner spinner = findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String[] choise = getResources().getStringArray(R.array.format_names);
                Toast.makeText(getApplicationContext(), choise[i], Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        TextView textViewCompressQuality = findViewById(R.id.textViewCompressQuality);
        SeekBar seekBar = findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (seekBar.getProgress() == 0)
                    seekBar.setProgress(1);
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
        btnSelectFolder.setOnClickListener(v -> testBtn());

        Log.d(TAG, "onCreate: " + cntRow());

        table = findViewById(R.id.tableLayout);
        //TableLayout.LayoutParams tableParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT);
        //table.setLayoutParams(tableParams);
        paintTable(cntRow(), cntColumn(), listOfFiles.size());

    }

    void paintTable(int rowCount, int columnCount, int size) {
        try {
            int temp = 0;
            for (int i = 0; i < rowCount; i++) {
                TableRow row = new TableRow(CompressActivity.this);
                TableRow.LayoutParams paramsTable = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
                row.setLayoutParams(paramsTable);
                for (int j = 0; j < columnCount; j++) {
                    Log.d(TAG, "paintTable: " + temp);
                    if (temp < size) {
                        ImageView iv = new ImageView(this);
                        int finalTemp = temp;
                        iv.setOnClickListener(view -> {
                            table.removeAllViews();
                            listOfFiles.remove(finalTemp);
                            paintTable(cntRow(), cntColumn(), listOfFiles.size());
                        });
                        thumbnailLoader.load(listOfFiles.get(temp), iv);
                        iv.setLayoutParams(new TableRow.LayoutParams(125, 125));
                        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);

                        row.addView(iv);
                        Log.d(TAG, "paintTable: add view in row");
                        temp++;
                    }
                }
                row.setGravity(Gravity.CENTER);
                table.addView(row);
                Log.d(TAG, "paintTable: add table row");
            }

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

    public void testBtn() {
//       // Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
//        // Intent galleryIntent = new Intent();
//        intent.setType("*");
//        // galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
//        intent.setAction(Intent.ACTION_GET_CONTENT);
//        startActivityForResult(Intent.createChooser(intent, "Select folder"), FOLDER);
        Intent i = new Intent(this, SelectFilesActivity.class);
        startActivityForResult(i, 1);


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                ArrayList<FileInfo> listSelected;
                assert data != null;
                listSelected = (ArrayList<FileInfo>) data.getSerializableExtra("result");
                Intent intent = new Intent(this, CompressActivity.class);
                intent.putExtra("listoffiles", listSelected);
                startActivity(intent);
            }

        } else if (resultCode == Activity.RESULT_CANCELED) {

        }
    }


}
