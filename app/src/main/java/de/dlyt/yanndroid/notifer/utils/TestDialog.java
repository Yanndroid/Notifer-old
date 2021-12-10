package de.dlyt.yanndroid.notifer.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.icu.util.Calendar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;
import java.util.HashMap;

import de.dlyt.yanndroid.notifer.R;
import de.dlyt.yanndroid.notifer.service.NotificationListener;
import de.dlyt.yanndroid.oneui.dialog.AlertDialog;
import de.dlyt.yanndroid.oneui.dialog.ClassicColorPickerDialog;
import de.dlyt.yanndroid.oneui.view.SeekBar;
import de.dlyt.yanndroid.oneui.view.Switch;

public class TestDialog implements DialogInterface.OnClickListener {

    private Context mContext;
    private AlertDialog alertDialog;
    private ClassicColorPickerDialog colorPickerDialog;
    private int mColor = Color.BLACK;

    private EditText dLabel, dPackage, dTitle, dText, dSubText, dTemplate, dId;
    private Switch dOngoing, dRemoved, dIndeterminate;
    private SeekBar dProgress;
    private ImageView dColor;

    @SuppressLint("RestrictedApi")
    public TestDialog(Context context) {
        mContext = context;
        final View dLayout = ((AppCompatActivity) context).getLayoutInflater().inflate(R.layout.dialog_test_message, null);

        alertDialog = new AlertDialog.Builder(context)
                .setTitle("Test Message")
                .setView(dLayout)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Send", this)
                .create();

        dLabel = dLayout.findViewById(R.id.dialog_test_label);
        dPackage = dLayout.findViewById(R.id.dialog_test_package);
        dTitle = dLayout.findViewById(R.id.dialog_test_title);
        dText = dLayout.findViewById(R.id.dialog_test_text);
        dSubText = dLayout.findViewById(R.id.dialog_test_sub_text);
        dTemplate = dLayout.findViewById(R.id.dialog_test_template);
        dId = dLayout.findViewById(R.id.dialog_test_id);
        dOngoing = dLayout.findViewById(R.id.dialog_test_ongoing);
        dRemoved = dLayout.findViewById(R.id.dialog_test_removed);
        dIndeterminate = dLayout.findViewById(R.id.dialog_test_indeterminate);
        dProgress = dLayout.findViewById(R.id.dialog_test_progress);
        dColor = dLayout.findViewById(R.id.dialog_test_color);

        dOngoing.setOnCheckedChangeListener((buttonView, isChecked) -> dRemoved.setEnabled(!isChecked));
        dRemoved.setOnCheckedChangeListener((buttonView, isChecked) -> dOngoing.setEnabled(!isChecked));
        dIndeterminate.setOnCheckedChangeListener((buttonView, isChecked) -> dProgress.setEnabled(!isChecked));

        colorPickerDialog = new ClassicColorPickerDialog(context, i -> {
            if (mColor != i) {
                colorPickerDialog.getColorPicker().getRecentColorInfo().initRecentColorInfo(new int[]{i});
                final GradientDrawable drawable = (GradientDrawable) context.getDrawable(R.drawable.color_picker_preference_preview).mutate();
                drawable.setColor(i);
                dColor.setImageDrawable(drawable);
                mColor = i;
            }
        });

        GradientDrawable drawable = (GradientDrawable) context.getDrawable(R.drawable.color_picker_preference_preview).mutate();
        drawable.setColor(mColor);
        dColor.setImageDrawable(drawable);
        dColor.setOnClickListener(v -> colorPickerDialog.show());
    }


    public void show() {
        alertDialog.show();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        HashMap<String, Object> output = new HashMap<>();

        output.put("label", dLabel.getText().toString());
        output.put("package", dPackage.getText().toString());

        output.put("id", dId.getText().toString());
        output.put("time", Calendar.getInstance().getTimeInMillis());
        output.put("ongoing", dOngoing.isChecked());
        output.put("template", dTemplate.getText().toString());
        output.put("removed", dRemoved.isChecked());

        output.put("title", dTitle.getText().toString());
        output.put("text", dText.getText().toString());
        output.put("sub_text", dSubText.getText().toString());

        output.put("progress_indeterminate", dIndeterminate.isChecked());
        output.put("progress_max", 100);
        output.put("progress", dProgress.getProgress());


        switch (mContext.getSharedPreferences("de.dlyt.yanndroid.notifer_preferences", Context.MODE_PRIVATE).getString("color_format", "hex")) {
            default:
            case "hex":
                output.put("color", String.format("#%06X", (0xFFFFFF & mColor)));
                break;
            case "rgb":
                Color color = Color.valueOf(mColor);
                output.put("color", Arrays.toString(new int[]{(int) (color.red() * 255), (int) (color.green() * 255), (int) (color.blue() * 255)}));
                break;
            case "hsv":
                float[] hsv_color = new float[3];
                Color.colorToHSV(mColor, hsv_color);
                output.put("color", Arrays.toString(hsv_color));
                break;
            case "int":
                output.put("color", mColor);
                break;
        }

        NotificationListener.sendToWS(output);
    }
}
