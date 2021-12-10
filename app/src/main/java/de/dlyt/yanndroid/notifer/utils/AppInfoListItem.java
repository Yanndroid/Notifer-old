package de.dlyt.yanndroid.notifer.utils;

import static de.dlyt.yanndroid.notifer.service.NotificationListener.enabled_packages;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import androidx.palette.graphics.Palette;

public class AppInfoListItem {

    public String label, packageName;
    public Drawable icon;
    public boolean checked, userApp;
    public int color, def_color = 0;

    public AppInfoListItem(Context context, PackageInfo packageInfo) {
        PackageManager packageManager = context.getPackageManager();
        this.packageName = packageInfo.packageName;
        this.label = packageManager.getApplicationLabel(packageInfo.applicationInfo).toString();
        this.icon = packageManager.getApplicationIcon(packageInfo.applicationInfo);
        this.checked = enabled_packages.containsKey(this.packageName);
        this.userApp = ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0);

        if (this.checked) this.color = enabled_packages.get(this.packageName);
    }

    public int loadDominantColor(Context context) {
        if (this.def_color != 0) return this.def_color;
        return this.def_color = getAppDominantColor(context);
    }

    private int getAppDominantColor(Context context) {
        try {
            Drawable appIconDrawable = context.getPackageManager().getApplicationIcon(this.packageName);
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
