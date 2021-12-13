package de.dlyt.yanndroid.notifer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import de.dlyt.yanndroid.notifer.utils.Updater;
import de.dlyt.yanndroid.oneui.layout.AboutPage;
import de.dlyt.yanndroid.oneui.utils.ThemeUtil;

public class AboutActivity extends AppCompatActivity {

    private AboutPage about_page;
    private MaterialButton about_github;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new ThemeUtil(this);
        setContentView(R.layout.activity_about);

        about_page = findViewById(R.id.about_page);
        about_github = findViewById(R.id.about_github);

        checkForUpdate();
    }

    private void checkForUpdate() {
        Updater.checkForUpdate(this, new Updater.UpdateChecker() {
            @Override
            public void updateAvailable(boolean available, String url, String versionName) {
                if (available) {
                    about_page.setUpdateState(AboutPage.UPDATE_AVAILABLE);
                    about_page.setUpdateButtonOnClickListener(v -> {
                        about_page.findViewById(R.id.update_button).setEnabled(false);
                        Updater.downloadAndInstall(getBaseContext(), url, versionName);
                    });
                } else {
                    about_page.setUpdateState(AboutPage.NO_UPDATE);
                }
            }

            @Override
            public void githubAvailable(String url) {
                about_github.setVisibility(View.VISIBLE);
                about_github.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))));
            }

            @Override
            public void noConnection() {
                about_page.setUpdateState(AboutPage.NO_CONNECTION);
                about_page.setRetryButtonOnClickListener(v -> {
                    about_page.setUpdateState(AboutPage.LOADING);
                    checkForUpdate();
                });
            }
        });
    }

}
