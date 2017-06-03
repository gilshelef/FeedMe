package com.gilshelef.feedme.util;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.gilshelef.feedme.R;

public class AppCompatMaterialAlertDialog extends DialogFragment {

    private Activity activity;
    private LayoutInflater inflater;

    private TextView tv_my_textview;

    private String textToShow;
    private OnAction listener;

    public static AppCompatMaterialAlertDialog getInstance() {
        return new AppCompatMaterialAlertDialog();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.activity = getActivity();
        this.inflater = activity.getLayoutInflater();
        this.listener = (OnAction) activity;
        this.textToShow = getString(R.string.donation_expired_msg);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.custom_layout, null);

        initDialogUi(v);

        final AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.AppCompatAlertDialogStyle);
        TextView title = Util.buildTitleView(getContext(), getString(R.string.donation_expired_title));
        builder.setCustomTitle(title);
        builder.setCancelable(false);
        builder.setPositiveButton(activity.getString(R.string.update),
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    listener.onUpdate();
                    dismiss();
                }
            });
        builder.setNegativeButton(activity.getString(R.string.cancel),
        new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.onCancel();
                dismiss();
            }
        });
        builder.setView(v);
        return builder.create();
    }



    private void initDialogUi(View root) {
        tv_my_textview = (TextView) root.findViewById(R.id.tv_my_textview);
        tv_my_textview.setText(textToShow);
    }


    public interface OnAction {
        void onUpdate();
        void onCancel();
    }
}
