package com.hillywave.compressimage;

import android.app.Activity;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;

public class LocationDialog {

    public static final String LOCATION_SETTING = "location_setting";
    RadioButton rb1;
    RadioButton rb2;
    RadioButton rb3;
    RadioButton rb4;


    public void showDialog(Activity activity) {
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_location);

        SharedPreferences sp = activity.getSharedPreferences(LOCATION_SETTING, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        ImageView imageViewPikLocation = dialog.findViewById(R.id.imageViewPickLocation);
        LinearLayout linearLayout = dialog.findViewById(R.id.linearLayout_dialog);

        CheckBox cbAuto = dialog.findViewById(R.id.cb_autoSize);
        cbAuto.setOnClickListener(v -> {
            if (cbAuto.isChecked()) {
                linearLayout.setVisibility(View.INVISIBLE);
            } else {
                linearLayout.setVisibility(View.VISIBLE);
            }
        });

        EditText editTextT = dialog.findViewById(R.id.editText_T);
        EditText editTextL = dialog.findViewById(R.id.editText_L);
        EditText editTextR = dialog.findViewById(R.id.editText_R);
        EditText editTextB = dialog.findViewById(R.id.editText_B);

        rb1 = dialog.findViewById(R.id.radioButton);
        rb2 = dialog.findViewById(R.id.radioButton2);
        rb3 = dialog.findViewById(R.id.radioButton3);
        rb4 = dialog.findViewById(R.id.radioButton4);
        rb1.setOnClickListener(v -> {
            resetRB();
            imageViewPikLocation.setImageResource(R.drawable.ico_cornerslt);
            editTextL.setVisibility(View.VISIBLE);
            editTextT.setVisibility(View.VISIBLE);
            editTextB.setVisibility(View.INVISIBLE);
            editTextR.setVisibility(View.INVISIBLE);
            rb1.setChecked(true);
            editor.putString("location_setting", "LT");
            editor.apply();
        });
        rb2.setOnClickListener(v -> {
            resetRB();
            imageViewPikLocation.setImageResource(R.drawable.ico_cornersrt);
            editTextL.setVisibility(View.INVISIBLE);
            editTextT.setVisibility(View.VISIBLE);
            editTextB.setVisibility(View.INVISIBLE);
            editTextR.setVisibility(View.VISIBLE);
            rb2.setChecked(true);
            editor.putString("location_setting", "RT");
            editor.apply();
        });
        rb3.setOnClickListener(v -> {
            resetRB();
            imageViewPikLocation.setImageResource(R.drawable.ico_cornerslb);
            editTextL.setVisibility(View.VISIBLE);
            editTextT.setVisibility(View.INVISIBLE);
            editTextB.setVisibility(View.VISIBLE);
            editTextR.setVisibility(View.INVISIBLE);
            rb3.setChecked(true);
            editor.putString("location_setting", "LB");
            editor.apply();
        });
        rb4.setOnClickListener(v -> {
            resetRB();
            imageViewPikLocation.setImageResource(R.drawable.ico_cornersrb);
            editTextL.setVisibility(View.INVISIBLE);
            editTextT.setVisibility(View.INVISIBLE);
            editTextB.setVisibility(View.VISIBLE);
            editTextR.setVisibility(View.VISIBLE);
            rb4.setChecked(true);
            editor.putString("location_setting", "RB");
            editor.apply();
        });

        Button dialogButton = dialog.findViewById(R.id.btn_dialog);
        dialogButton.setOnClickListener(v -> {
            dialog.dismiss();
            if (String.valueOf(editTextT.getText()).equals(""))
                editTextT.setText("0");
            else
                editor.putFloat("location_T", Float.parseFloat(String.valueOf(editTextT.getText())));

            if (String.valueOf(editTextL.getText()).equals(""))
                editTextL.setText("0");
            else
                editor.putFloat("location_L", Float.parseFloat(String.valueOf(editTextL.getText())));

            if (String.valueOf(editTextR.getText()).equals(""))
                editTextR.setText("0");
            else
                editor.putFloat("location_R", Float.parseFloat(String.valueOf(editTextR.getText())));

            if (String.valueOf(editTextB.getText()).equals(""))
                editTextB.setText("0");
            else
                editor.putFloat("location_B", Float.parseFloat(String.valueOf(editTextB.getText())));

            editor.putInt("location_optionT", Integer.parseInt(String.valueOf(editTextT.getText())));
            editor.putInt("location_optionL", Integer.parseInt(String.valueOf(editTextL.getText())));
            editor.putInt("location_optionR", Integer.parseInt(String.valueOf(editTextR.getText())));
            editor.putInt("location_optionB", Integer.parseInt(String.valueOf(editTextB.getText())));
            if (rb1.isChecked())
                editor.putString("locationRB", "LT");
            if (rb2.isChecked())
                editor.putString("locationRB", "RT");
            if (rb3.isChecked())
                editor.putString("locationRB", "LB");
            if (rb4.isChecked())
                editor.putString("locationRB", "RB");


            editor.apply();
        });

        dialog.show();
    }

    private void resetRB() {
        rb1.setChecked(false);
        rb2.setChecked(false);
        rb3.setChecked(false);
        rb4.setChecked(false);
    }

}
