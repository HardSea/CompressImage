package com.hillywave.compressimage;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import id.zelory.compressor.Compressor;

public class RotationActivity extends AppCompatActivity implements View.OnClickListener {
    private TableLayout table;
    private ArrayList<FileInfo> listOfFiles;
    private ThumbnailLoader thumbnailLoader;
    private String TAG = ".CompressActivity";
    private String SAVE_DIRECTORY = "/rotatefolder";
    private int angle = 0;
    private EditText editTextSaveFolder;
    private String folderSaveName;
    private ArrayList<FileInfo> listSelected;
    private ProgressDialog dialog;
    private RadioButton rb4;
    private static int HORIZONTAL_MIRROR = 1111;
    private static int VERTICAL_MIRROR = 2222;
    private int compressErrors = 0;
    private TextView textViewCntImages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rotation);

        LinearLayout mainLayout = findViewById(R.id.mainLayout);
        this.thumbnailLoader = new ThumbnailLoader(this.getResources());

        File saveDirectory = new File(Environment.getExternalStorageDirectory() + SAVE_DIRECTORY);
        folderSaveName = saveDirectory.getPath();

        dialog = new ProgressDialog(RotationActivity.this);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setTitle(getApplicationContext().getString(R.string.text_wait));
        dialog.setMessage(getApplicationContext().getString(R.string.text_wait4));

        editTextSaveFolder = findViewById(R.id.editTextSaveFolder);
        editTextSaveFolder.setText(saveDirectory.getPath());
        editTextSaveFolder.setOnClickListener(view -> btnSelectFolder());

        RadioButton rb1 = findViewById(R.id.radioButton);
        rb1.setOnClickListener(this);
        RadioButton rb2 = findViewById(R.id.radioButton2);
        rb2.setOnClickListener(this);
        RadioButton rb3 = findViewById(R.id.radioButton3);
        rb3.setOnClickListener(this);
        RadioButton rb4 = findViewById(R.id.radioButton4);
        rb4.setOnClickListener(this);
        RadioButton rb5 = findViewById(R.id.radioButton5);
        rb5.setOnClickListener(this);
        RadioButton rb6 = findViewById(R.id.radioButton6);
        rb6.setOnClickListener(this);

        Button btnCompress = findViewById(R.id.btnCompress);
        btnCompress.setOnClickListener(v -> {
            dialog.show();
            Thread t = new Thread(this::rotate);
            t.start();
        });

        textViewCntImages = findViewById(R.id.textViewCntImage);

        rb4 = findViewById(R.id.radioButton4);
        SeekBar seekBar = findViewById(R.id.seekBar);
        RadioButton finalRb = rb4;
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                angle = seekBar.getProgress();
                finalRb.setText((seekBar.getProgress() - 360) + "");
                angle = seekBar.getProgress();

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
                dialog.setMessage(getApplicationContext().getString(R.string.text_wait3, msg.what, listOfFiles.size()));
            else if ((msg.what + 1) >= listOfFiles.size()) {
                dialog.dismiss();
                ViewDialog alert = new ViewDialog();
                alert.showDialog(RotationActivity.this, getApplicationContext().getString(R.string.text_files) + listOfFiles.size(), getApplicationContext().getString(R.string.text_errors, compressErrors), "", "");
            }
            return true;
        }
    });

    void rotate() {
        saveFolder();
        File myFile;
        Matrix matrix = new Matrix();

        if (angle != HORIZONTAL_MIRROR && angle != VERTICAL_MIRROR) {
            matrix.postRotate(angle);
        } else if (angle == VERTICAL_MIRROR) {
            matrix.preScale(-1.0f, 1.0f);
        } else {
            matrix.preScale(1.0f, -1.0f);
        }

        OutputStream os;
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        for (int i = 0; i < listOfFiles.size(); i++) {
            myFile = listOfFiles.get(i).getFIle();
            try {

                Bitmap bitmap = BitmapFactory.decodeFile(myFile.getAbsolutePath(), bmOptions);
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), true);

                Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);

                File rotateFile = new File(folderSaveName, myFile.getName());

                os = new FileOutputStream(rotateFile);
                rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);

                os.flush();
                os.close();


                if (!rotateFile.setLastModified(myFile.lastModified())) {
                    if (!rotateFile.setLastModified(myFile.lastModified()))
                        Log.d(TAG, "compress: ERROR setLastModified");
                }

            } catch (Exception e) {
                compressErrors++;
                e.printStackTrace();
            }
            handler.sendEmptyMessage(i + 1);


        }
    }

    void saveFolder(){
        File f = new File(folderSaveName);
        if (!f.exists()){
            f.mkdir();
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
                TableRow row = new TableRow(RotationActivity.this);
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
             textViewCntImages.setText(getApplicationContext().getString(R.string.text_size, listOfFiles.size()));

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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                assert data != null;
                listSelected = (ArrayList<FileInfo>) data.getSerializableExtra("result");
            } else if (requestCode == 11) {
                folderSaveName = data.getStringExtra("resultNameFolder");
                editTextSaveFolder.setText(folderSaveName);

            }
        }
    }

    @Override
    public void onClick(View view) {
        clearChecked();
        setInvisibleLayoutAngle();
        RadioButton rb = (RadioButton) view;

        switch (view.getId()) {
            case R.id.radioButton:
                rb.setChecked(true);
                angle = 90;
                break;
            case R.id.radioButton2:
                rb.setChecked(true);
                angle = 180;
                break;
            case R.id.radioButton3:
                rb.setChecked(true);
                angle = -90;

                break;
            case R.id.radioButton4:
                setVisibleLayoutAngle();
                rb.setChecked(true);

                break;
            case R.id.radioButton5:
                rb.setChecked(true);
                angle = HORIZONTAL_MIRROR;

                break;
            case R.id.radioButton6:
                rb.setChecked(true);
                angle = VERTICAL_MIRROR;

                break;
            default:
                break;


        }
    }

    private void setVisibleLayoutAngle() {
        LinearLayout.LayoutParams paramsVisible = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        LinearLayout.LayoutParams paramsInvisible = new LinearLayout.LayoutParams(0, 0);
        LinearLayout layoutAngle = findViewById(R.id.layout_selectAngle);
        LinearLayout layoutMirror = findViewById(R.id.layout_selectMirror);
        layoutAngle.setLayoutParams(paramsVisible);
        layoutMirror.setLayoutParams(paramsInvisible);

    }

    private void setInvisibleLayoutAngle() {
        LinearLayout.LayoutParams paramsVisible = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        LinearLayout.LayoutParams paramsInvisible = new LinearLayout.LayoutParams(0, 0);
        LinearLayout layoutAngle = findViewById(R.id.layout_selectAngle);
        LinearLayout layoutMirror = findViewById(R.id.layout_selectMirror);
        layoutAngle.setLayoutParams(paramsInvisible);
        layoutMirror.setLayoutParams(paramsVisible);

    }

    private void clearChecked() {
        RadioButton rb1 = findViewById(R.id.radioButton);
        RadioButton rb2 = findViewById(R.id.radioButton2);
        RadioButton rb3 = findViewById(R.id.radioButton3);
        RadioButton rb4 = findViewById(R.id.radioButton4);
        RadioButton rb5 = findViewById(R.id.radioButton5);
        RadioButton rb6 = findViewById(R.id.radioButton6);
        rb1.setChecked(false);
        rb2.setChecked(false);
        rb3.setChecked(false);
        rb4.setChecked(false);
        rb5.setChecked(false);
        rb6.setChecked(false);
    }


}
