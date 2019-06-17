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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.Format;
import java.util.ArrayList;

public class FormatActivity extends AppCompatActivity {

    private TableLayout table;
    private ArrayList<FileInfo> listOfFiles;
    private ThumbnailLoader thumbnailLoader;
    private String TAG = ".CompressActivity";
    private EditText editTextSaveFolder;
    private String folderSaveName;
    private ProgressDialog dialog;
    private int compressErrors = 0;
    private Bitmap.CompressFormat format = Bitmap.CompressFormat.JPEG;
    private String formatName = ".jpeg";
    private TextView textViewCntImages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_format);

        this.thumbnailLoader = new ThumbnailLoader(this.getResources());

        String SAVE_DIRECTORY = "/formatfolder";
        File saveDirectory = new File(Environment.getExternalStorageDirectory() + SAVE_DIRECTORY);
        folderSaveName = saveDirectory.getPath();

        dialog = new ProgressDialog(FormatActivity.this);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setTitle("Зачекайте");
        dialog.setMessage("Триває обробка зображень");

        editTextSaveFolder = findViewById(R.id.editTextSaveFolder);
        editTextSaveFolder.setText(saveDirectory.getPath());
        editTextSaveFolder.setOnClickListener(view -> btnSelectFolder());

        textViewCntImages = findViewById(R.id.textViewCntImage);

        Button btnCompress = findViewById(R.id.btnCompress);
        btnCompress.setOnClickListener(v -> {
            dialog.show();
            Thread t = new Thread(this::format);
            t.start();
        });

        RadioGroup radioGroup = findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener((radioGroup1, i) -> {
            switch (radioGroup1.getCheckedRadioButtonId()) {
                case R.id.radioButtonJPEG:
                    format = Bitmap.CompressFormat.JPEG;
                    formatName = ".jpeg";
                    break;
                case R.id.radioButtonPNG:
                    format = Bitmap.CompressFormat.PNG;
                    formatName = ".png";
                    break;
                case R.id.radioButtonWEBP:
                    format = Bitmap.CompressFormat.WEBP;
                    formatName = ".webp";
                    break;
                default:
                    break;

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
                alert.showDialog(FormatActivity.this, getApplicationContext().getString(R.string.text_wait2, listOfFiles.size()), getApplicationContext().getString(R.string.text_errors, compressErrors), "", "");
            }
            return true;
        }
    });

    void format() {
        saveFolder();
        File myFile;
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        for (int i = 0; i < listOfFiles.size(); i++) {
            myFile = listOfFiles.get(i).getFIle();

            try {
                File formatFile = new File(folderSaveName, myFile.getName().substring(0, myFile.getName().lastIndexOf(".")) + formatName);

                Bitmap bitmap = BitmapFactory.decodeFile(myFile.getAbsolutePath(), bmOptions);
                FileOutputStream out = new FileOutputStream(formatFile);

                bitmap.compress(format, 100, out);

                out.flush();
                out.close();

                if (!formatFile.setLastModified(myFile.lastModified())) {
                    if (!formatFile.setLastModified(myFile.lastModified()))
                        Log.d(TAG, "compress: ERROR setLastModified");
                }

            } catch (Exception e) {
                compressErrors++;
                e.printStackTrace();
            }

            handler.sendEmptyMessage(i + 1);


        }
    }

    void saveFolder() {
        File f = new File(folderSaveName);
        if (!f.exists()) {
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
                TableRow row = new TableRow(FormatActivity.this);
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
            textViewCntImages.setText(getString(R.string.text_size, listOfFiles.size()));

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
            if (requestCode == 11) {
                folderSaveName = data.getStringExtra("resultNameFolder");
                editTextSaveFolder.setText(folderSaveName);

            }
        }
    }


}
