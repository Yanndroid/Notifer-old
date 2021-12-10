package de.dlyt.yanndroid.notifer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import de.dlyt.yanndroid.notifer.preferences.SwitchBarPreference;
import de.dlyt.yanndroid.notifer.preferences.TipCardPreference;
import de.dlyt.yanndroid.notifer.service.NotificationListener;
import de.dlyt.yanndroid.notifer.utils.TestDialog;
import de.dlyt.yanndroid.notifer.utils.Updater;
import de.dlyt.yanndroid.oneui.layout.PreferenceFragment;
import de.dlyt.yanndroid.oneui.layout.ToolbarLayout;
import de.dlyt.yanndroid.oneui.preference.DropDownPreference;
import de.dlyt.yanndroid.oneui.preference.EditTextPreference;
import de.dlyt.yanndroid.oneui.preference.Preference;
import de.dlyt.yanndroid.oneui.preference.PreferenceGroup;
import de.dlyt.yanndroid.oneui.preference.internal.PreferencesRelatedCard;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ToolbarLayout toolbarLayout = findViewById(R.id.toolbar_layout);
        toolbarLayout.setNavigationButtonTooltip(getString(R.string.sesl_navigate_up));
        toolbarLayout.setNavigationButtonOnClickListener(v -> onBackPressed());

        if (savedInstanceState == null)
            getSupportFragmentManager().beginTransaction().replace(R.id.settings, new SettingsFragment()).commit();
    }

    public static class SettingsFragment extends PreferenceFragment {

        private Context mContext;
        private TipCardPreference tipCard;
        private PreferencesRelatedCard mRelatedCard;
        private SwitchBarPreference switchBarPreference;

        private TestDialog testDialog;

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
            mContext = context;
            testDialog = new TestDialog(mContext);
        }

        @Override
        public void onResume() {
            setRelatedCardView();
            super.onResume();
            if (hasNotificationAccess()) hideTipCard();
            switchBarPreference.setEnabled(hasNotificationAccess());
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            getView().setBackgroundColor(getResources().getColor(R.color.item_background_color, mContext.getTheme()));
        }

        @Override
        public void onCreatePreferences(Bundle bundle, String str) {
            addPreferencesFromResource(R.xml.preferences);
        }

        @SuppressLint("RestrictedApi")
        @Override
        public void onCreate(Bundle bundle) {
            super.onCreate(bundle);

            SharedPreferences sharedPreferences = mContext.getSharedPreferences("de.dlyt.yanndroid.notifer_preferences", Context.MODE_PRIVATE);

            switchBarPreference = (SwitchBarPreference) findPreference("service_enabled");
            switchBarPreference.setChecked(sharedPreferences.getBoolean("service_enabled", false));
            switchBarPreference.addOnSwitchChangeListener((switchCompat, isChecked) -> {
                sharedPreferences.edit().putBoolean("service_enabled", isChecked).apply();
                NotificationListener.setEnabled(isChecked);
            });

            tipCard = (TipCardPreference) findPreference("noti_access_tip");
            tipCard.setTipsCardListener(new TipCardPreference.TipsCardListener() {
                @Override
                public void onCancelClicked(View view) {
                    hideTipCard();
                }

                @Override
                public void onViewClicked(View view) {
                    startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
                }
            });


            EditTextPreference ws_ip = (EditTextPreference) findPreference("ws_ip");
            EditTextPreference ws_port = (EditTextPreference) findPreference("ws_port");

            ws_ip.setSummary(ws_ip.getText());
            ws_ip.seslSetSummaryColor(getResources().getColor(R.color.primary_color, mContext.getTheme()));
            ws_ip.setOnPreferenceChangeListener((preference, newValue) -> {
                preference.setSummary(newValue.toString());
                NotificationListener.setAddress(newValue.toString(), ws_port.getText());
                return true;
            });

            ws_port.setSummary(ws_port.getText());
            ws_port.seslSetSummaryColor(getResources().getColor(R.color.primary_color, mContext.getTheme()));
            ws_port.setOnPreferenceChangeListener((preference, newValue) -> {
                preference.setSummary(newValue.toString());
                NotificationListener.setAddress(ws_ip.getText(), newValue.toString());
                return true;
            });

            DropDownPreference color_format = (DropDownPreference) findPreference("color_format");
            color_format.setSummary(color_format.getEntry());
            color_format.seslSetSummaryColor(getResources().getColor(R.color.primary_color, mContext.getTheme()));
            color_format.setOnPreferenceChangeListener((preference, newValue) -> {
                color_format.setValue(newValue.toString());
                preference.setSummary(color_format.getEntry());
                return true;
            });

            findPreference("test_ws").setOnPreferenceClickListener(var1 -> {
                testDialog.show();
                return false;
            });


            Updater.checkForUpdate(mContext, new Updater.UpdateChecker() {
                @Override
                public void updateAvailable(boolean available, String url, String versionName) {
                    Preference about_app = findPreference("about_app");
                    about_app.setWidgetLayoutResource(available ? R.layout.sesl_preference_badge : 0);
                }

                @Override
                public void githubAvailable(String url) {

                }

                @Override
                public void noConnection() {

                }
            });

        }

        private boolean hasNotificationAccess() {
            String enabledNotificationListeners = Settings.Secure.getString(mContext.getContentResolver(), "enabled_notification_listeners");
            return enabledNotificationListeners != null && enabledNotificationListeners.contains(mContext.getPackageName());
        }

        private void hideTipCard() {
            PreferenceGroup parent = getParent(getPreferenceScreen(), tipCard);
            if (parent != null) {
                parent.removePreference(tipCard);
                parent.removePreference(findPreference("tip_space"));
            }
        }

        private void setRelatedCardView() {
            if (mRelatedCard == null) {
                mRelatedCard = createRelatedCard(mContext);
                mRelatedCard.addButton("Notifications", v -> startActivity(new Intent("android.settings.NOTIFICATION_SETTINGS")))
                        .addButton("Notification access", v -> startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")))
                        .addButton("Sounds and vibration", v -> startActivity(new Intent(Settings.ACTION_SOUND_SETTINGS)))
                        .show(this);
            }
        }

    }

}
