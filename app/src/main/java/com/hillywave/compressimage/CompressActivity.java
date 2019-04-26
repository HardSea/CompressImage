package com.hillywave.compressimage;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;


import java.util.ArrayList;

public class CompressActivity extends AppCompatActivity {
    private TableLayout table;
    private LinearLayout mainLayout;
    private ArrayList<FileInfo> listOfFiles;
    private ThumbnailLoader thumbnailLoader;
    private String TAG = ".CompressActivity";
    public static final int FOLDER = 123;
    public static final int FOLDER_REQUEST = 342;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compress);

        mainLayout = findViewById(R.id.mainLayout);
        this.thumbnailLoader = new ThumbnailLoader(this.getResources());

        Intent intent = getIntent();
        listOfFiles = (ArrayList<FileInfo>) intent.getSerializableExtra("listoffiles");

        Button btnSelectFolder = findViewById(R.id.btnSelectFolder);
        btnSelectFolder.setOnClickListener(v -> testBtn());

        int rowCount = (int) Math.ceil((double) listOfFiles.size() / 3);
        Log.d(TAG, "onCreate: " + rowCount);

        table = findViewById(R.id.tableLayout);
        //TableLayout.LayoutParams tableParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT);
        //table.setLayoutParams(tableParams);
        paintTable(rowCount, 3, listOfFiles.size());

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
                        thumbnailLoader.load(listOfFiles.get(temp), iv);
                        row.addView(iv);
                        Log.d(TAG, "paintTable: add view in row");
                        temp++;
                    }
                }
                table.addView(row);
                Log.d(TAG, "paintTable: add table row");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
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
