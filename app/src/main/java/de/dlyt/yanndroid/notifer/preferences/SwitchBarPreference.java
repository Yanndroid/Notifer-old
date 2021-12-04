package de.dlyt.yanndroid.notifer.preferences;

import android.content.Context;
import android.util.AttributeSet;

import de.dlyt.yanndroid.notifer.R;
import de.dlyt.yanndroid.oneui.preference.Preference;
import de.dlyt.yanndroid.oneui.preference.PreferenceViewHolder;
import de.dlyt.yanndroid.oneui.view.SwitchBar;

public class SwitchBarPreference extends Preference {

    private SwitchBar mSwitchBar;
    private Boolean mChecked;
    private SwitchBar.OnSwitchChangeListener mOnSwitchChangeListener;

    public SwitchBarPreference(Context context) {
        this(context, null);
    }

    public SwitchBarPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwitchBarPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayoutResource(R.layout.switch_bar_preference);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder preferenceViewHolder) {
        super.onBindViewHolder(preferenceViewHolder);
        mSwitchBar = preferenceViewHolder.itemView.findViewById(R.id.preference_switch_bar);
        if (mChecked != null) {
            mSwitchBar.setChecked(mChecked);
            mChecked = null;
        }
        if (mOnSwitchChangeListener != null) {
            mSwitchBar.addOnSwitchChangeListener(mOnSwitchChangeListener);
            mOnSwitchChangeListener = null;
        }
    }


    public void setChecked(boolean checked) {
        if (mSwitchBar == null) {
            mChecked = checked;
        } else {
            mSwitchBar.setChecked(checked);
        }
    }

    public void addOnSwitchChangeListener(SwitchBar.OnSwitchChangeListener onSwitchChangeListener) {
        if (mSwitchBar == null) {
            mOnSwitchChangeListener = onSwitchChangeListener;
        } else {
            mSwitchBar.addOnSwitchChangeListener(onSwitchChangeListener);
        }
    }
}
