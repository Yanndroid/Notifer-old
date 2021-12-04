package de.dlyt.yanndroid.notifer.preferences;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import de.dlyt.yanndroid.notifer.R;
import de.dlyt.yanndroid.oneui.preference.Preference;
import de.dlyt.yanndroid.oneui.preference.PreferenceViewHolder;
import de.dlyt.yanndroid.oneui.view.Tooltip;

public class TipCardPreference extends Preference {
    private Context mContext;
    private TipsCardListener mTipsCardListener;
    private int mTextColor;

    public TipCardPreference(Context context) {
        this(context, null);
    }

    public TipCardPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TipCardPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        setSelectable(false);
        setLayoutResource(R.layout.tip_card_preference);
        mTextColor = ContextCompat.getColor(context, R.color.tips_card_view_item_color);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder preferenceViewHolder) {
        super.onBindViewHolder(preferenceViewHolder);

        ((TextView) preferenceViewHolder.itemView.findViewById(android.R.id.title)).setTextColor(mTextColor);
        ((TextView) preferenceViewHolder.itemView.findViewById(android.R.id.summary)).setTextColor(mTextColor);

        Tooltip.setTooltipText(preferenceViewHolder.itemView.findViewById(R.id.tips_cancel_button), mContext.getString(R.string.sesl_cancel));
        preferenceViewHolder.itemView.findViewById(R.id.tips_cancel_button).setOnClickListener(view -> mTipsCardListener.onCancelClicked(view));
        preferenceViewHolder.itemView.setOnClickListener(view -> mTipsCardListener.onViewClicked(view));
    }

    public void setTipsCardListener(TipsCardListener listener) {
        mTipsCardListener = listener;
    }

    public interface TipsCardListener {
        void onCancelClicked(View view);

        void onViewClicked(View view);
    }

}