package com.hillywave.compressimage;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
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
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Objects;

public class WatermarkActivity extends AppCompatActivity {

    private TableLayout table;
    private ArrayList<FileInfo> listOfFiles;
    private ThumbnailLoader thumbnailLoader;
    private String TAG = ".WatermarkActivity";
    private String SAVE_DIRECTORY = "/watermarkfolder";
    public static final String LOCATION_SETTING = "location_setting";
    private EditText editTextSaveFolder;
    private String folderSaveName;
    private String watermarkImagePath;
    private ProgressDialog dialog;
    private int compressErrors = 0;
    private Bitmap.CompressFormat format = Bitmap.CompressFormat.JPEG;
    private TextView textViewCntImages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watermark);

        this.thumbnailLoader = new ThumbnailLoader(this.getResources());

        File saveDirectory = new File(Environment.getExternalStorageDirectory() + SAVE_DIRECTORY);
        folderSaveName = saveDirectory.getPath();

        dialog = new ProgressDialog(WatermarkActivity.this);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setTitle(getApplicationContext().getString(R.string.text_wait));
        dialog.setMessage(getApplicationContext().getString(R.string.text_wait2));

        editTextSaveFolder = findViewById(R.id.editTextSaveFolder);
        editTextSaveFolder.setText(saveDirectory.getPath());
        editTextSaveFolder.setOnClickListener(view -> btnSelectFolder());

        textViewCntImages = findViewById(R.id.textViewCntImage);

        Button btnWatermark = findViewById(R.id.btnWatermark);
        btnWatermark.setOnClickListener(v -> {
            dialog.show();
            Thread t = new Thread(this::format);
            t.start();
        });

        Button btnSelectImage = findViewById(R.id.buttonSelectImage);
        btnSelectImage.setOnClickListener(v -> btnSelectWatermarkImage());

        ImageView btnSelectLocation = findViewById(R.id.btnSelectLocation);
        btnSelectLocation.setOnClickListener(v -> showDialogSelectLocation());


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

    void showDialogSelectLocation() {
        LocationDialog dialog = new LocationDialog();
        dialog.showDialog(WatermarkActivity.this);
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if ((msg.what + 1) < listOfFiles.size())
                dialog.setMessage(getApplicationContext().getString(R.string.text_wait3, msg.what, listOfFiles.size()));
            else if ((msg.what + 1) >= listOfFiles.size()) {
                dialog.dismiss();
                ViewDialog alert = new ViewDialog();
                alert.showDialog(WatermarkActivity.this, getApplicationContext().getString(R.string.text_files) + listOfFiles.size(), getApplicationContext().getString(R.string.text_errors, compressErrors), "", "");
            }
            return true;
        }
    });

    void format() {
        saveFolder();
        File myFile;
        SharedPreferences prefs = getApplicationContext().getSharedPreferences(LOCATION_SETTING, Context.MODE_PRIVATE);

        float tPercent = prefs.getFloat("location_T", 5);
        float lPercent = prefs.getFloat("location_L", 5);
        float rPercent = prefs.getFloat("location_R", 5);
        float bPercent = prefs.getFloat("location_B", 5);

        BitmapFactory.Options options = new BitmapFactory.Options();
        Matrix matrix = new Matrix();
        Paint p = new Paint();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        for (int i = 0; i < listOfFiles.size(); i++) {
            myFile = listOfFiles.get(i).getFIle();

            try {
                File formatFile = new File(folderSaveName, myFile.getName());
                FileOutputStream out = new FileOutputStream(formatFile);

                Bitmap bmpOriginal = BitmapFactory.decodeFile(myFile.getAbsolutePath(), options);
                Bitmap watermark = BitmapFactory.decodeFile(watermarkImagePath, options);

                int h, w;
                w = bmpOriginal.getWidth();
                h = bmpOriginal.getHeight();

                float scale = (float) (((float) h * 0.10) / (float) watermark.getHeight());
                matrix.postScale(scale, scale);

                Bitmap tmpBitmap = Bitmap.createBitmap(watermark, 0, 0, watermark.getWidth(), watermark.getHeight(), matrix, true);

                Bitmap resultBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);

                Canvas c = new Canvas(resultBitmap);

                c.drawBitmap(bmpOriginal, 0, 0, null);

                //p.setAlpha(127);
                Log.d(TAG, "format: " + prefs.getString("locationRB", "RB"));

                Log.d(TAG, "format: " + resultBitmap.getHeight());
                Log.d(TAG, "format: " + resultBitmap.getHeight() * (lPercent / 100));

                switch (Objects.requireNonNull(prefs.getString("locationRB", "RB"))) {
                    case "LT":
                        c.drawBitmap(tmpBitmap, (resultBitmap.getWidth() * (lPercent / 100)), resultBitmap.getHeight() * (tPercent / 100), p);

                        break;
                    case "RT":
                        c.drawBitmap(tmpBitmap, (resultBitmap.getWidth() - tmpBitmap.getWidth() - (resultBitmap.getWidth() * (rPercent / 100))), resultBitmap.getHeight() * (tPercent / 100), p);

                        break;
                    case "LB":
                        c.drawBitmap(tmpBitmap, (resultBitmap.getWidth() * (lPercent / 100)), (resultBitmap.getHeight() - tmpBitmap.getHeight() - (resultBitmap.getWidth() * (bPercent / 100))), p);

                        break;
                    case "RB":
                        c.drawBitmap(tmpBitmap, (resultBitmap.getWidth() - tmpBitmap.getWidth() - (resultBitmap.getWidth() * (rPercent / 100))), (resultBitmap.getHeight() - tmpBitmap.getHeight() - (resultBitmap.getWidth() * (bPercent / 100))), p);

                        break;
                    default:
                        c.drawBitmap(tmpBitmap, (resultBitmap.getWidth() - tmpBitmap.getWidth() - (resultBitmap.getWidth() * (rPercent / 100))), (resultBitmap.getHeight() - tmpBitmap.getHeight() - (resultBitmap.getWidth() * (bPercent / 100))), p);

                        break;
                }


                resultBitmap.compress(format, 100, out);

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
                TableRow row = new TableRow(WatermarkActivity.this);
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


    private void btnSelectWatermarkImage() {
        Intent i = new Intent(this, SelectFilesActivity.class);
        i.putExtra("request_code", 41);
        startActivityForResult(i, 41);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 11) {
                folderSaveName = data.getStringExtra("resultNameFolder");
                editTextSaveFolder.setText(folderSaveName);

            } else if (requestCode == 41) {
                watermarkImagePath = data.getStringExtra("resultWatermarkImgName");
                Log.d(TAG, "onActivityResult: " + watermarkImagePath);
                Toast.makeText(getApplicationContext(), watermarkImagePath, Toast.LENGTH_SHORT).show();
                ImageView imageView = findViewById(R.id.watermarkImage);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                Bitmap bitmap = BitmapFactory.decodeFile(watermarkImagePath, options);
                imageView.setImageBitmap(bitmap);

                //     editTextSaveFolder.setText(folderSaveName);
            }
        }
    }

}
