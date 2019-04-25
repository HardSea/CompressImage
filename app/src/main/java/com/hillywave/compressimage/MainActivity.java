package com.hillywave.compressimage;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.lang.reflect.Type;
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
        Button btnMultiFunc = findViewById(R.id.btnMultiFunc);
        btnMultiFunc.setOnClickListener(this);

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnCompress:
                Intent i = new Intent(this, SelectFilesActivity.class);
                startActivityForResult(i, 1);

                break;
            case R.id.btnRotate:
                Intent i2 = new Intent(this, CompressActivity.class);
                startActivityForResult(i2, 2);

                break;
            case R.id.btnFormat:
                Intent i3 = new Intent(this, SelectFilesActivity.class);
                startActivityForResult(i3, 3);

                break;
            case R.id.btnWatermark:
                Intent i4 = new Intent(this, SelectFilesActivity.class);
                startActivityForResult(i4, 4);

                break;
            case R.id.btnMultiFunc:
                Intent i5 = new Intent(this, SelectFilesActivity.class);
                startActivityForResult(i5, 5);

                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case 1:
                    ArrayList<FileInfo> listSelected;
                    assert data != null;
                    listSelected = (ArrayList<FileInfo>) data.getSerializableExtra("result");
                    Intent intent = new Intent(this, CompressActivity.class);
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
