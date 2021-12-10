package de.dlyt.yanndroid.notifer.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.RadioButton;

import androidx.appcompat.app.AppCompatActivity;

import de.dlyt.yanndroid.notifer.R;
import de.dlyt.yanndroid.oneui.dialog.AlertDialog;

public class FilterDialog {

    private Context mContext;
    private AlertDialog alertDialog;

    private RadioButton dApps_all, dApps_user, dApps_system, dState_all, dState_checked, dState_unchecked;

    @SuppressLint("RestrictedApi")
    public FilterDialog(Context context, Boolean app_boolean, Boolean state_boolean, DialogListener dialogListener) {
        mContext = context;
        final View dLayout = ((AppCompatActivity) context).getLayoutInflater().inflate(R.layout.dialog_filter, null);

        alertDialog = new AlertDialog.Builder(context)
                .setTitle("Filter")
                .setView(dLayout)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Ok", (dialog, which) -> dialogListener.onOk(dApps_all.isChecked() ? null : dApps_user.isChecked(), dState_all.isChecked() ? null : dState_checked.isChecked()))
                .create();

        dApps_all = dLayout.findViewById(R.id.dialog_filter_apps_all);
        dApps_user = dLayout.findViewById(R.id.dialog_filter_apps_user);
        dApps_system = dLayout.findViewById(R.id.dialog_filter_apps_system);
        dState_all = dLayout.findViewById(R.id.dialog_filter_state_all);
        dState_checked = dLayout.findViewById(R.id.dialog_filter_state_checked);
        dState_unchecked = dLayout.findViewById(R.id.dialog_filter_state_unchecked);

        if (app_boolean == null) dApps_all.setChecked(true);
        else {
            if (app_boolean) dApps_user.setChecked(true);
            else dApps_system.setChecked(true);
        }

        if (state_boolean == null) dState_all.setChecked(true);
        else {
            if (state_boolean) dState_checked.setChecked(true);
            else dState_unchecked.setChecked(true);
        }
    }

    public void show() {
        alertDialog.show();
    }


    public interface DialogListener {
        void onOk(Boolean app_boolean, Boolean state_boolean);
    }
}
