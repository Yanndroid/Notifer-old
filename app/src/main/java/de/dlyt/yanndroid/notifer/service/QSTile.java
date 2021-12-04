package de.dlyt.yanndroid.notifer.service;

import android.content.Context;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

public class QSTile extends TileService {

    @Override
    public void onClick() {
        super.onClick();
        boolean newBoolean = !isServiceEnabled();
        setChecked(newBoolean);
        getSharedPreferences("de.dlyt.yanndroid.notifer_preferences", Context.MODE_PRIVATE).edit().putBoolean("service_enabled", newBoolean).apply();
        NotificationListener.setEnabled(newBoolean);
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        setChecked(isServiceEnabled());
    }

    private boolean isServiceEnabled() {
        return getSharedPreferences("de.dlyt.yanndroid.notifer_preferences", Context.MODE_PRIVATE).getBoolean("service_enabled", false);
    }

    private void setChecked(boolean checked) {
        Tile tile = getQsTile();
        tile.setState(checked ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        tile.updateTile();
    }
}
