package de.dlyt.yanndroid.notifer.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;

import de.dlyt.yanndroid.notifer.EnabledAppsActivity;
import de.dlyt.yanndroid.notifer.service.NotificationListener;
import de.dlyt.yanndroid.oneui.dialog.ClassicColorPickerDialog;

public class ColorPickerDialog {

    private ClassicColorPickerDialog colorPickerDialog;
    private AppInfoListItem appInfoListItem;
    private EnabledAppsActivity.AppsAdapter.ViewHolder holder;

    @SuppressLint("RestrictedApi")
    public ColorPickerDialog(Context context) {
        super();
        colorPickerDialog = new ClassicColorPickerDialog(context, i -> {
            if (appInfoListItem != null && holder != null) {

                if (appInfoListItem.color != i) {
                    appInfoListItem.color = i;
                    NotificationListener.enabled_packages.put(appInfoListItem.packageName, i);

                    colorPickerDialog.getColorPicker().getRecentColorInfo().initRecentColorInfo(new int[]{i});

                    GradientDrawable drawable = (GradientDrawable) context.getDrawable(de.dlyt.yanndroid.oneui.R.drawable.color_picker_preference_preview).mutate();
                    drawable.setColor(i);
                    holder.appColor.setImageDrawable(drawable);
                }

            }
        });
    }

    public void show(EnabledAppsActivity.AppsAdapter.ViewHolder holder, AppInfoListItem appInfoListItem) {
        this.holder = holder;
        this.appInfoListItem = appInfoListItem;
        this.colorPickerDialog.setNewColor(appInfoListItem.color);
        this.colorPickerDialog.show();
    }
}
