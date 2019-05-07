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
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
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
    private String formatName = ".jpeg";
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
        dialog.setTitle("Зачекайте");
        dialog.setMessage("Триває обробка зображень");

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
                dialog.setMessage("Триває обробка зображень: " + msg.what + "/" + listOfFiles.size());
            else if ((msg.what + 1) >= listOfFiles.size()) {
                dialog.dismiss();
                ViewDialog alert = new ViewDialog();
                alert.showDialog(WatermarkActivity.this, "Опрацьовано файлів: " + listOfFiles.size(), "Всього помилок: " + compressErrors, "", "");
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
                File formatFile = new File(folderSaveName, myFile.getName());

                Bitmap bitmap = BitmapFactory.decodeFile(myFile.getAbsolutePath(), bmOptions);
                FileOutputStream out = new FileOutputStream(formatFile);

                addWatermark(myFile.getAbsolutePath(), watermarkImagePath).compress(format, 100, out);

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

//
//    public Bitmap addWatermark(String photoPath, String watermarkPath) {
//        int w, h;
//        Canvas c;
//        Paint paint;
//        Bitmap bmp, watermark, bmpOriginal;
//
//        SharedPreferences prefs = getApplicationContext().getSharedPreferences(LOCATION_SETTING, Context.MODE_PRIVATE);
//
//
//        Matrix matrix;
//        float scale;
//        RectF r;
//
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
//
//        bmpOriginal = BitmapFactory.decodeFile(photoPath, options);
//        watermark = BitmapFactory.decodeFile(watermarkPath, options);
//
//        w = bmpOriginal.getWidth();
//        h = bmpOriginal.getHeight();
//
//        bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
//
//        paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG | Paint.FILTER_BITMAP_FLAG);
//
//
//        c = new Canvas(bmp);
//        c.drawBitmap(bmpOriginal, 0, 0, paint);
//
//        matrix = new Matrix();
//        scale = (float) (((float) h * 0.10) / (float) watermark.getHeight());
//        matrix.postScale(scale, scale);
//
////        switch (Objects.requireNonNull(prefs.getString("locationRB", "RB"))) {
////            case "LT":
////                r = new RectF(watermark.getWidth(), watermark.getHeight(), 0, 0);
////
////                break;
////            case "RT":
////                r = new RectF(0, watermark.getHeight(), watermark.getWidth(), 0);
////
////                break;
////            case "LB":
////                r = new RectF(watermark.getWidth(), 0, 0, watermark.getHeight());
////
////                break;
////            case "RB":
////                r = new RectF(0, 0, watermark.getWidth(), watermark.getHeight());
////                break;
////            default:
////                r = new RectF(0, 0, watermark.getWidth(), watermark.getHeight());
////                break;
////        }
//
//        r = new RectF(0, 0, watermark.getWidth(), watermark.getHeight());
//        matrix.mapRect(r);
//        switch (Objects.requireNonNull(prefs.getString("locationRB", "RB"))){
//            case "LT":
//                matrix.postTranslate(0, 0);
//                break;
//            case "RT":
//                matrix.postTranslate(w - r.width(), 0);
//                break;
//            case "LB":
//                matrix.postTranslate(w - r.width(), h - r.height());
//                break;
//            case "RB":
//                matrix.postTranslate(w - r.width(), h - r.height());
//                break;
//            default:
//                matrix.postTranslate(w - r.width(), h - r.height());
//                break;
//        }
//        matrix.postTranslate(w - r.width(), h - r.height());
//
//        c.drawBitmap(watermark, matrix, paint);
//        watermark.recycle();
//
//        return bmp;
//    }


    public static Bitmap addWatermark(String photoPath, String watermarkPath) {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        Bitmap bmpOriginal = BitmapFactory.decodeFile(photoPath, options);
        Bitmap watermark = BitmapFactory.decodeFile(watermarkPath, options);

        int h, w;
        w = bmpOriginal.getWidth();
        h = bmpOriginal.getHeight();

        Matrix matrix = new Matrix();
        float scale = (float) (((float) h * 0.10) / (float) watermark.getHeight());
        matrix.postScale(scale, scale);

        Bitmap tmpBitmap = Bitmap.createBitmap(watermark, 0, 0, watermark.getWidth(),watermark.getHeight(), matrix, true);

        Bitmap resultBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);

        Canvas c = new Canvas(resultBitmap);

        c.drawBitmap(bmpOriginal, 0, 0, null);

        Paint p = new Paint();
        //p.setAlpha(127);

        c.drawBitmap(tmpBitmap, 0,0, p);


        return resultBitmap;
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
            textViewCntImages.setText("Кількість: " + listOfFiles.size());

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
