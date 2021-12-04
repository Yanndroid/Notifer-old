package de.dlyt.yanndroid.notifer;

import static de.dlyt.yanndroid.notifer.service.NotificationListener.enabled_packages;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.util.SeslRoundedCorner;
import androidx.recyclerview.widget.SortedList;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import de.dlyt.yanndroid.notifer.utils.AppInfoListItem;
import de.dlyt.yanndroid.notifer.utils.ColorPickerDialog;
import de.dlyt.yanndroid.oneui.dialog.ProgressDialog;
import de.dlyt.yanndroid.oneui.layout.ToolbarLayout;
import de.dlyt.yanndroid.oneui.sesl.recyclerview.SeslLinearLayoutManager;
import de.dlyt.yanndroid.oneui.view.RecyclerView;
import de.dlyt.yanndroid.oneui.view.Switch;

public class EnabledAppsActivity extends AppCompatActivity {

    private Context mContext;
    private PackageManager packageManager;
    private ActivityResultLauncher<Intent> activityResultLauncher;

    private ToolbarLayout toolbarLayout;
    private RecyclerView recyclerView;

    private AppsAdapter appsAdapter;
    private List<AppInfoListItem> app_list;
    private String mSearchText;
    private String mColorCode;


    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enabled_apps);
        mContext = this;
        packageManager = getPackageManager();
        toolbarLayout = findViewById(R.id.toolbar_layout);
        recyclerView = findViewById(R.id.app_list);

        initToolbar();

        TypedValue value = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorAccent, value, true);
        mColorCode = "#" + Integer.toHexString(Color.red(value.data)) + Integer.toHexString(Color.green(value.data)) + Integer.toHexString(Color.blue(value.data));

        ProgressDialog progressDialog = new ProgressDialog(mContext);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Loading...");
        progressDialog.show();

        new AsyncTask<Object, Integer, List<AppInfoListItem>>() {
            @Override
            protected List<AppInfoListItem> doInBackground(Object... objects) {
                List<PackageInfo> installedPackages = packageManager.getInstalledPackages(0);
                List<AppInfoListItem> appInfoListItems = new ArrayList<>();

                int app_count = installedPackages.size();
                for (int i = 0; i < app_count; i++) {
                    PackageInfo packageInfo = installedPackages.get(i);
                    /*if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0)*/
                    appInfoListItems.add(new AppInfoListItem(
                            mContext,
                            packageInfo.applicationInfo.loadLabel(packageManager).toString(),
                            packageInfo.packageName,
                            packageInfo.applicationInfo.loadIcon(packageManager),
                            enabled_packages.containsKey(packageInfo.packageName),
                            enabled_packages.get(packageInfo.packageName)));
                    publishProgress(i, app_count);
                }

                appInfoListItems.add(null);
                return appInfoListItems;
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
                progressDialog.setMax(values[1]);
                progressDialog.setProgress(values[0]);
            }

            @Override
            protected void onPostExecute(List<AppInfoListItem> appInfoListItems) {
                app_list = appInfoListItems;
                toolbarLayout.setToolbarMenuItemEnabled(toolbarLayout.getToolbarMenu().findItem(R.id.action_search), true);
                initRecycler();
                progressDialog.dismiss();
            }
        }.execute();
    }

    @Override
    protected void onPause() {
        super.onPause();
        getSharedPreferences("de.dlyt.yanndroid.notifer_preferences", Context.MODE_PRIVATE).edit().putString("enabled_packages", new Gson().toJson(enabled_packages)).apply();
    }

    private void initToolbar() {
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> toolbarLayout.onSearchModeVoiceInputResult(result));

        toolbarLayout.setNavigationButtonTooltip(getString(R.string.sesl_navigate_up));
        toolbarLayout.setNavigationButtonOnClickListener(v -> onBackPressed());
        toolbarLayout.inflateToolbarMenu(R.menu.app_screen_menu);
        toolbarLayout.setOnToolbarMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_search) {
                toolbarLayout.showSearchMode();
            }
        });
        toolbarLayout.setToolbarMenuItemEnabled(toolbarLayout.getToolbarMenu().findItem(R.id.action_search), false);
        toolbarLayout.setSearchModeListener(new ToolbarLayout.SearchModeListener() {
            @Override
            public void onSearchOpened(EditText search_edittext) {

            }

            @Override
            public void onSearchDismissed(EditText search_edittext) {
                search_edittext.setText(null);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (appsAdapter == null) return;
                mSearchText = s.toString();
                appsAdapter.filter();
            }

            @Override
            public void onKeyboardSearchClick(CharSequence s) {

            }

            @Override
            public void onVoiceInputClick(Intent intent) {
                activityResultLauncher.launch(intent);
            }
        });
    }

    private void initRecycler() {
        TypedValue divider = new TypedValue();
        mContext.getTheme().resolveAttribute(android.R.attr.listDivider, divider, true);

        recyclerView.setLayoutManager(new SeslLinearLayoutManager(mContext));
        appsAdapter = new AppsAdapter();
        recyclerView.setAdapter(appsAdapter);

        ItemDecoration decoration = new ItemDecoration();
        recyclerView.addItemDecoration(decoration);
        decoration.setDivider(mContext.getDrawable(divider.resourceId));

        recyclerView.seslSetGoToTopEnabled(true);
        recyclerView.seslSetLastRoundedCorner(true);
        recyclerView.seslSetFillBottomEnabled(true);
        recyclerView.setBackgroundColor(getColor(R.color.item_background_color));
    }


    public class AppsAdapter extends RecyclerView.Adapter<AppsAdapter.ViewHolder> {

        public SortedList<AppInfoListItem> sorted_list = new SortedList<>(AppInfoListItem.class, new SortedList.Callback<AppInfoListItem>() {
            @Override
            public int compare(AppInfoListItem o1, AppInfoListItem o2) {
                if (o1 == null) return 1;
                if (o2 == null) return -1;

                /*if (o1.checked && !o2.checked) return -1;
                if (!o1.checked && o2.checked) return 1;*/

                return o1.label.compareToIgnoreCase(o2.label);
            }

            @Override
            public void onChanged(int position, int count) {
                notifyItemRangeChanged(position, count);
            }

            @Override
            public boolean areContentsTheSame(AppInfoListItem oldItem, AppInfoListItem newItem) {
                return oldItem.label.equals(newItem.label);
            }

            @Override
            public boolean areItemsTheSame(AppInfoListItem item1, AppInfoListItem item2) {
                return item1 == item2;
            }

            @Override
            public void onInserted(int position, int count) {
                notifyItemRangeInserted(position, count);
            }

            @Override
            public void onRemoved(int position, int count) {
                notifyItemRangeRemoved(position, count);
            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
                notifyItemMoved(fromPosition, toPosition);
            }
        });

        private ColorPickerDialog colorPickerDialog;

        public AppsAdapter() {
            super();
            sorted_list.addAll(app_list);
            colorPickerDialog = new ColorPickerDialog(mContext);
        }

        @Override
        public int getItemCount() {
            return sorted_list.size();
        }

        @Override
        public int getItemViewType(final int position) {
            if (sorted_list.get(position) == null) return 1;
            return 0;
        }

        public void filter() {
            sorted_list.beginBatchedUpdates();
            for (int i = 0; i < app_list.size() - 1; i++) {
                final AppInfoListItem appInfoListItem = app_list.get(i);

                if (containsSearch(appInfoListItem.label, appInfoListItem.packageName)) {
                    sorted_list.add(appInfoListItem);
                    AppsAdapter.ViewHolder holder = (ViewHolder) recyclerView.findViewHolderForAdapterPosition(sorted_list.indexOf(appInfoListItem));
                    if (holder != null) {
                        setListItemText(holder, appInfoListItem);
                    }
                } else {
                    sorted_list.remove(appInfoListItem);
                }
            }
            sorted_list.endBatchedUpdates();
            recyclerView.scrollToPosition(0);
        }

        private boolean containsSearch(String s1, String s2) {
            return (s1.toLowerCase().contains(mSearchText.toLowerCase()) || s2.toLowerCase().contains(mSearchText.toLowerCase()));
        }

        @Override
        public AppsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            int resId = 0;

            switch (viewType) {
                case 0:
                    resId = R.layout.app_listview_item;
                    break;
                case 1:
                    resId = R.layout.app_listview_item_spacing;
                    break;
            }

            View view = LayoutInflater.from(parent.getContext()).inflate(resId, parent, false);
            return new AppsAdapter.ViewHolder(view, viewType);
        }

        @Override
        public void onBindViewHolder(AppsAdapter.ViewHolder holder, final int position) {
            if (holder.isItem) {
                final AppInfoListItem appInfoListItem = sorted_list.get(position);
                setItemEnabled(holder, !appInfoListItem.packageName.equals("de.dlyt.yanndroid.notifer"));

                setListItemText(holder, appInfoListItem);
                holder.appIcon.setImageDrawable(appInfoListItem.icon);

                holder.appSwitch.setChecked(appInfoListItem.checked);
                initColorPicker(holder, appInfoListItem);

                holder.itemView.setOnClickListener(v -> holder.appSwitch.toggle());
                holder.appSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    appInfoListItem.checked = isChecked;

                    if (isChecked)
                        enabled_packages.put(appInfoListItem.packageName, appInfoListItem.color = appInfoListItem.def_color);
                    else enabled_packages.remove(appInfoListItem.packageName);

                    initColorPicker(holder, appInfoListItem);

                    //sorted_list.recalculatePositionOfItemAt(position);
                });
            }
        }

        @Override
        public void onViewRecycled(ViewHolder var1) {
            super.onViewRecycled(var1);
            if (var1.appSwitch != null) var1.appSwitch.setOnCheckedChangeListener(null);
            if (var1.appColor != null) var1.appColor.setOnClickListener(null);
        }

        private void setItemEnabled(ViewHolder holder, boolean itemEnabled) {
            holder.itemView.setEnabled(itemEnabled);
            holder.appIcon.setEnabled(itemEnabled);
            holder.appTitle.setEnabled(itemEnabled);
            holder.appPackage.setEnabled(itemEnabled);
            holder.appColor.setEnabled(itemEnabled);
            holder.appSwitch.setEnabled(itemEnabled);
        }

        private void initColorPicker(ViewHolder holder, AppInfoListItem appInfoListItem) {
            if (appInfoListItem.checked) {
                holder.appColor.setVisibility(View.VISIBLE);

                GradientDrawable drawable = (GradientDrawable) mContext.getDrawable(R.drawable.color_picker_preference_preview).mutate();
                drawable.setColor(appInfoListItem.color);
                holder.appColor.setImageDrawable(drawable);

                holder.appColor.setOnClickListener(v -> colorPickerDialog.show(holder, appInfoListItem));
            } else {
                holder.appColor.setVisibility(View.GONE);
                holder.appColor.setOnClickListener(null);
            }
        }


        private void setListItemText(AppsAdapter.ViewHolder holder, AppInfoListItem appInfoListItem) {
            if (holder.appTitle == null || holder.appPackage == null) return;
            if (mSearchText != null && !mSearchText.isEmpty()) {
                setTintedText(holder.appTitle, appInfoListItem.label);
                setTintedText(holder.appPackage, appInfoListItem.packageName);
            } else {
                holder.appTitle.setText(appInfoListItem.label);
                holder.appPackage.setText(appInfoListItem.packageName);
            }
        }

        private void setTintedText(TextView textView, String text) {
            int index = text.toLowerCase().indexOf(mSearchText.toLowerCase());
            if (index == -1) {
                textView.setText(text);
            } else {
                String match = text.substring(index, index + mSearchText.length());
                textView.setText(Html.fromHtml(text.replace(match, "<b><font color=\"" + mColorCode + "\">" + match + "</font></b>"), Html.FROM_HTML_MODE_LEGACY));
            }
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public ImageView appIcon, appColor;
            public TextView appTitle, appPackage;
            public Switch appSwitch;
            boolean isItem;

            ViewHolder(View itemView, int viewType) {
                super(itemView);

                isItem = viewType == 0;

                if (isItem) {
                    appIcon = itemView.findViewById(R.id.list_item_app_icon);
                    appColor = itemView.findViewById(R.id.list_item_app_color);
                    appTitle = itemView.findViewById(R.id.list_item_app_label);
                    appPackage = itemView.findViewById(R.id.list_item_app_package);
                    appSwitch = itemView.findViewById(R.id.list_item_app_switch);
                }
            }
        }
    }


    public class ItemDecoration extends RecyclerView.ItemDecoration {
        private SeslRoundedCorner mSeslRoundedCornerTop;
        private SeslRoundedCorner mSeslRoundedCornerBottom;
        private Drawable mDivider;
        private int mDividerHeight;

        public ItemDecoration() {
            mSeslRoundedCornerTop = new SeslRoundedCorner(mContext, true);
            mSeslRoundedCornerTop.setRoundedCorners(3);
            mSeslRoundedCornerBottom = new SeslRoundedCorner(mContext, true);
            mSeslRoundedCornerBottom.setRoundedCorners(12);
        }

        @Override
        public void seslOnDispatchDraw(Canvas canvas, RecyclerView recyclerView, RecyclerView.State state) {
            super.seslOnDispatchDraw(canvas, recyclerView, state);

            int childCount = recyclerView.getChildCount();
            int width = recyclerView.getWidth();

            for (int i = 0; i < childCount; i++) {
                View childAt = recyclerView.getChildAt(i);
                AppsAdapter.ViewHolder viewHolder = (AppsAdapter.ViewHolder) recyclerView.getChildViewHolder(childAt);
                int y = ((int) childAt.getY()) + childAt.getHeight();

                boolean shallDrawDivider;

                if (recyclerView.getChildAt(i + 1) != null)
                    shallDrawDivider = ((AppsAdapter.ViewHolder) recyclerView.getChildViewHolder(recyclerView.getChildAt(i + 1))).isItem;
                else
                    shallDrawDivider = false;

                if (mDivider != null && viewHolder.isItem && shallDrawDivider) {
                    mDivider.setBounds(140, y, width, mDividerHeight + y);
                    mDivider.draw(canvas);
                }

                if (!viewHolder.isItem) {
                    if (recyclerView.getChildAt(i + 1) != null)
                        mSeslRoundedCornerTop.drawRoundedCorner(recyclerView.getChildAt(i + 1), canvas);
                    if (recyclerView.getChildAt(i - 1) != null)
                        mSeslRoundedCornerBottom.drawRoundedCorner(recyclerView.getChildAt(i - 1), canvas);
                }
            }

            mSeslRoundedCornerTop.drawRoundedCorner(canvas);
        }

        public void setDivider(Drawable d) {
            mDivider = d;
            mDividerHeight = d.getIntrinsicHeight();
            recyclerView.invalidateItemDecorations();
        }
    }
}