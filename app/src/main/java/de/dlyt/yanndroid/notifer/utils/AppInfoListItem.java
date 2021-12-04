package de.dlyt.yanndroid.notifer.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import androidx.palette.graphics.Palette;

public class AppInfoListItem {

    public String label, packageName;
    public Drawable icon;
    public boolean checked;
    public int color, def_color;

    public AppInfoListItem(Context context, String label, String packageName, Drawable icon, boolean checked, Integer color) {
        this.label = label;
        this.packageName = packageName;
        this.icon = icon;
        this.checked = checked;
        this.def_color = getAppDominantColor(context, packageName);
        this.color = (color != null) ? color : this.def_color;
    }

    private int getAppDominantColor(Context context, String packageName) {
        try {
            Drawable appIconDrawable = context.getPackageManager().getApplicationIcon(packageName);
            Bitmap appIconBitmap = Bitmap.createBitmap(appIconDrawable.getIntrinsicWidth(), appIconDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(appIconBitmap);
            appIconDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            appIconDrawable.draw(canvas);
            return Palette.from(appIconBitmap).generate().getDominantColor(0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
