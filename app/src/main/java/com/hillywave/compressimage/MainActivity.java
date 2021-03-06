package com.hillywave.compressimage;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    String TAG = "MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_screen);

        Button btnCompress = findViewById(R.id.btnCompress);
        btnCompress.setOnClickListener(this);
        Button btnRotate = findViewById(R.id.btnRotate);
        btnRotate.setOnClickListener(this);
        Button btnFormat = findViewById(R.id.btnFormat);
        btnFormat.setOnClickListener(this);
        Button btnWatermark = findViewById(R.id.btnWatermark);
        btnWatermark.setOnClickListener(this);

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnCompress:
                Intent i = new Intent(this, SelectFilesActivity.class);
                i.putExtra("request_code", 1);
                startActivityForResult(i, 1);

                break;
            case R.id.btnRotate:
                Intent i2 = new Intent(this, SelectFilesActivity.class);
                i2.putExtra("request_code", 1);
                startActivityForResult(i2, 2);

                break;
            case R.id.btnFormat:
                Intent i3 = new Intent(this, SelectFilesActivity.class);
                i3.putExtra("request_code", 1);
                startActivityForResult(i3, 3);

                break;
            case R.id.btnWatermark:
                Intent i4 = new Intent(this, SelectFilesActivity.class);
                i4.putExtra("request_code", 1);
                startActivityForResult(i4, 4);

                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case 1:
                    assert data != null;
                    ArrayList<FileInfo> listSelected = (ArrayList<FileInfo>) data.getSerializableExtra("result");
                    Intent intent = new Intent(this, CompressActivity.class);
                    intent.putExtra("listoffiles", listSelected);
                    startActivity(intent);
                    break;

                case 2:
                    assert data != null;
                    listSelected = (ArrayList<FileInfo>) data.getSerializableExtra("result");
                    intent = new Intent(this, RotationActivity.class);
                    intent.putExtra("listoffiles", listSelected);
                    startActivity(intent);
                    break;

                case 3:
                    assert data != null;
                    listSelected = (ArrayList<FileInfo>) data.getSerializableExtra("result");
                    intent = new Intent(this, FormatActivity.class);
                    intent.putExtra("listoffiles", listSelected);
                    startActivity(intent);

                    break;

                case 4:
                    assert data != null;
                    listSelected = (ArrayList<FileInfo>) data.getSerializableExtra("result");
                    intent = new Intent(this, WatermarkActivity.class);
                    intent.putExtra("listoffiles", listSelected);
                    startActivity(intent);

                    break;

                default:
                    break;
            }

        } else if (resultCode == Activity.RESULT_CANCELED) {

        }

    }
}
