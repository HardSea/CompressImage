package com.hillywave.compressimage;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class ViewDialog {

    public void showDialog(Activity activity,String cntFiles,  String sizeBefore, String sizeAfter, boolean isComplete) {
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.result_dialog);

        TextView textView1 = dialog.findViewById(R.id.text_dialog1);
        textView1.setText(cntFiles);
        TextView textView2 = dialog.findViewById(R.id.text_dialog2);
        textView2.setText(sizeBefore);
        TextView textView3 = dialog.findViewById(R.id.text_dialog3);
        textView3.setText(sizeAfter);

        Button dialogButton = dialog.findViewById(R.id.btn_dialog);
        dialogButton.setOnClickListener(v -> {
            dialog.dismiss();
            activity.finish();
        });

        dialog.show();

    }
}