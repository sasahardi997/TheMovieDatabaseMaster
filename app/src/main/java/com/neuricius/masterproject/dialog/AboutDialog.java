package com.neuricius.masterproject.dialog;

import android.content.Context;
import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;

import com.neuricius.masterproject.R;

public class AboutDialog extends AlertDialog.Builder {

    public AboutDialog(Context context) {
        super(context);

        setTitle(R.string.drawer_about);
        setMessage(R.string.dialog_about_message);
        setCancelable(false);

        setPositiveButton(R.string.dialog_about_yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        setNegativeButton(R.string.dialog_about_no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
    }


    public AlertDialog prepareDialog(){
        AlertDialog dialog = create();
        dialog.setCanceledOnTouchOutside(false);

        return dialog;
    }

}
