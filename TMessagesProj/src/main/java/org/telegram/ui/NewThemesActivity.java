package org.telegram.ui;

import org.telegram.messenger.NotificationCenter;
import org.telegram.ui.ActionBar.BaseFragment;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.Keep;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Cells.BrightnessControlCell;
import org.telegram.ui.Cells.ChatListCell;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.NotificationsCheckCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Cells.ThemeTypeCell;
import org.telegram.ui.Cells.ThemesHorizontalListCell;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.ShareAlert;
import org.telegram.ui.Components.ThemeEditorView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class NewThemesActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate {
    public final static int THEME_TYPE_BASIC = 0;
    public final static int THEME_TYPE_NIGHT = 1;
    public final static int THEME_TYPE_OTHER = 2;

    private NewThemesActivity.ListAdapter listAdapter;
    private RecyclerListView listView;
    @SuppressWarnings("FieldCanBeLocal")
    private LinearLayoutManager layoutManager;
    private ThemesHorizontalListCell firstThemesHorizontalListCell;
    private ThemesHorizontalListCell secondThemesHorizentalCell;
    private ThemesHorizontalListCell thirdThemesHorizentalCell;
    private ArrayList<Theme.ThemeInfo> darkThemes = new ArrayList<>();
    private ArrayList<Theme.ThemeInfo> defaultThemes = new ArrayList<>();
    private int currentType;

    private Theme.ThemeInfo sharingTheme;
    private Theme.ThemeAccent sharingAccent;
    private AlertDialog sharingProgressDialog;
    private ActionBarMenuItem menuItem;

    boolean hasThemeAccents;




    private int themeHeaderRow;
    private int secondHeaderRow;
    private int thirdHeaderRow;


    private int firstThemeListRow;
    private int secondThemeListRow;
    private int thirdThemeListRow;


    private int rowCount;


    private int previousUpdatedType;



    private final static int create_theme = 1;
    private final static int share_theme = 2;
    private final static int edit_theme = 3;
    private final static int reset_settings = 4;


    public NewThemesActivity(int type) {
        super();
        currentType = type;
        updateRows(true);
    }

    private void updateRows(boolean notify) {


        rowCount = 0;



        firstThemeListRow = -1;
        secondThemeListRow = -1;
        thirdThemeListRow = -1;


        themeHeaderRow = -1;
        secondHeaderRow = -1;
        thirdHeaderRow = -1;


        defaultThemes.clear();
        darkThemes.clear();
        for (int a = 0, N = Theme.themes.size(); a < N; a++) {
            Theme.ThemeInfo themeInfo = Theme.themes.get(a);
            if (currentType != THEME_TYPE_BASIC) {
                if (themeInfo.isLight() || themeInfo.info != null && themeInfo.info.document == null) {
                    continue;
                }
            }
            if (themeInfo.pathToFile != null) {
                darkThemes.add(themeInfo);
            } else {
                defaultThemes.add(themeInfo);
            }
        }
        Collections.sort(defaultThemes, (o1, o2) -> Integer.compare(o1.sortIndex, o2.sortIndex));

        if (currentType == THEME_TYPE_BASIC) {



            themeHeaderRow = rowCount++;
            firstThemeListRow = rowCount++;
            secondHeaderRow = rowCount++;
            secondThemeListRow = rowCount++;
            thirdHeaderRow = rowCount++;
            thirdThemeListRow = rowCount++;
            hasThemeAccents = Theme.getCurrentTheme().hasAccentColors();
            if (firstThemesHorizontalListCell != null) {
                firstThemesHorizontalListCell.setDrawDivider(hasThemeAccents);
            }



            if (Theme.selectedAutoNightType != Theme.AUTO_NIGHT_TYPE_NONE) {

                firstThemeListRow = rowCount++;
                secondThemeListRow = rowCount++;
                thirdThemeListRow = rowCount++;
                hasThemeAccents = Theme.getCurrentNightTheme().hasAccentColors();
                if (firstThemesHorizontalListCell != null) {
                    firstThemesHorizontalListCell.setDrawDivider(hasThemeAccents);
                }
                if (hasThemeAccents) {

                }

            }
        }

        if (listAdapter != null) {
            if (currentType != THEME_TYPE_NIGHT || previousUpdatedType == Theme.selectedAutoNightType || previousUpdatedType == -1) {
                if (notify || previousUpdatedType == -1) {
                    if (firstThemesHorizontalListCell != null) {
                        firstThemesHorizontalListCell.notifyDataSetChanged(listView.getWidth());
                        secondThemesHorizentalCell.notifyDataSetChanged(listView.getWidth());
                        thirdThemesHorizentalCell.notifyDataSetChanged(listView.getWidth());
                    }
                    listAdapter.notifyDataSetChanged();
                }

            } else {

                if (previousUpdatedType != Theme.selectedAutoNightType) {
                    for (int a = 0; a < 4; a++) {
                        RecyclerListView.Holder holder = (RecyclerListView.Holder) listView.findViewHolderForAdapterPosition(a);
                        if (holder == null || !(holder.itemView instanceof ThemeTypeCell)) {
                            continue;
                        }
                        ((ThemeTypeCell) holder.itemView).setTypeChecked(a == Theme.selectedAutoNightType);
                    }

                }

            }
        }

        updateMenuItem();
    }

    @Override
    public boolean onFragmentCreate() {

        NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.locationPermissionGranted);
        NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.didSetNewWallpapper);
        NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.themeListUpdated);
        NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.themeAccentListUpdated);
        NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.emojiDidLoad);
        NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.needShareTheme);
        NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.needSetDayNightTheme);
        getNotificationCenter().addObserver(this, NotificationCenter.themeUploadedToServer);
        getNotificationCenter().addObserver(this, NotificationCenter.themeUploadError);
        if (currentType == THEME_TYPE_BASIC) {
            Theme.loadRemoteThemes(currentAccount, true);
            Theme.checkCurrentRemoteTheme(true);
        }
        return super.onFragmentCreate();
    }

    @Override
    public void onFragmentDestroy() {

        super.onFragmentDestroy();

        NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.locationPermissionGranted);
        NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.didSetNewWallpapper);
        NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.themeListUpdated);
        NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.themeAccentListUpdated);
        NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.emojiDidLoad);
        NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.needShareTheme);
        NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.needSetDayNightTheme);
        getNotificationCenter().removeObserver(this, NotificationCenter.themeUploadedToServer);
        getNotificationCenter().removeObserver(this, NotificationCenter.themeUploadError);
        Theme.saveAutoNightThemeConfig();
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.locationPermissionGranted) {
            //updateSunTime(null, true);
        } else if (id == NotificationCenter.didSetNewWallpapper || id == NotificationCenter.emojiDidLoad) {
            if (listView != null) {
                listView.invalidateViews();
            }
        } else if (id == NotificationCenter.themeAccentListUpdated) {
//            if (listAdapter != null && themeAccentListRow != -1) {
//                listAdapter.notifyItemChanged(themeAccentListRow, new Object());
//            }
        } else if (id == NotificationCenter.themeListUpdated) {
            updateRows(true);
        } else if (id == NotificationCenter.themeUploadedToServer) {
            Theme.ThemeInfo themeInfo = (Theme.ThemeInfo) args[0];
            Theme.ThemeAccent accent = (Theme.ThemeAccent) args[1];
            if (themeInfo == sharingTheme && accent == sharingAccent) {
                String link = "https://" + MessagesController.getInstance(currentAccount).linkPrefix + "/addtheme/" + (accent != null ? accent.info.slug : themeInfo.info.slug);
                showDialog(new ShareAlert(getParentActivity(), null, link, false, link, false));
                if (sharingProgressDialog != null) {
                    sharingProgressDialog.dismiss();
                }
            }
        } else if (id == NotificationCenter.themeUploadError) {
            Theme.ThemeInfo themeInfo = (Theme.ThemeInfo) args[0];
            Theme.ThemeAccent accent = (Theme.ThemeAccent) args[1];
            if (themeInfo == sharingTheme && accent == sharingAccent && sharingProgressDialog == null) {
                sharingProgressDialog.dismiss();
            }
        } else if (id == NotificationCenter.needShareTheme) {
            if (getParentActivity() == null || isPaused) {
                return;
            }
            sharingTheme = (Theme.ThemeInfo) args[0];
            sharingAccent = (Theme.ThemeAccent) args[1];
            sharingProgressDialog = new AlertDialog(getParentActivity(), 3);
            sharingProgressDialog.setCanCacnel(true);
            showDialog(sharingProgressDialog, dialog -> {
                sharingProgressDialog = null;
                sharingTheme = null;
                sharingAccent = null;
            });
        } else if (id == NotificationCenter.needSetDayNightTheme) {
            updateMenuItem();
        }
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(false);
        if (AndroidUtilities.isTablet()) {
            actionBar.setOccupyStatusBar(false);
        }
        if (currentType == THEME_TYPE_BASIC) {
            actionBar.setTitle(LocaleController.getString("ChatSettings", R.string.ChatSettings));
            ActionBarMenu menu = actionBar.createMenu();
            menuItem = menu.addItem(0, R.drawable.ic_ab_other);
            menuItem.setContentDescription(LocaleController.getString("AccDescrMoreOptions", R.string.AccDescrMoreOptions));
            menuItem.addSubItem(share_theme, R.drawable.msg_share, LocaleController.getString("ShareTheme", R.string.ShareTheme));
            menuItem.addSubItem(edit_theme, R.drawable.msg_edit, LocaleController.getString("EditThemeColors", R.string.EditThemeColors));
            menuItem.addSubItem(create_theme, R.drawable.menu_palette, LocaleController.getString("CreateNewThemeMenu", R.string.CreateNewThemeMenu));
            menuItem.addSubItem(reset_settings, R.drawable.msg_reset, LocaleController.getString("ThemeResetToDefaults", R.string.ThemeResetToDefaults));
        } else {
           actionBar.setTitle(LocaleController.getString("AutoNightTheme", R.string.AutoNightTheme));
        }

        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                } else if (id == create_theme) {
                    if (getParentActivity() == null) {
                        return;
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                    builder.setTitle(LocaleController.getString("NewTheme", R.string.NewTheme));
                    builder.setMessage(LocaleController.getString("CreateNewThemeAlert", R.string.CreateNewThemeAlert));
                    builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                    builder.setPositiveButton(LocaleController.getString("CreateTheme", R.string.CreateTheme), (dialog, which) -> AlertsCreator.createThemeCreateDialog(NewThemesActivity.this, 0, null, null));
                    showDialog(builder.create());
                } else if (id == share_theme) {
                    Theme.ThemeInfo currentTheme = Theme.getCurrentTheme();
                    Theme.ThemeAccent accent = currentTheme.getAccent(false);
                    if (accent.info == null) {
                        MessagesController.getInstance(currentAccount).saveThemeToServer(accent.parentTheme, accent);
                        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.needShareTheme, accent.parentTheme, accent);
                    } else {
                        String link = "https://" + MessagesController.getInstance(currentAccount).linkPrefix + "/addtheme/" + accent.info.slug;
                        showDialog(new ShareAlert(getParentActivity(), null, link, false, link, false));
                    }
                } else if (id == edit_theme) {
                    Theme.ThemeInfo currentTheme = Theme.getCurrentTheme();
                    Theme.ThemeAccent accent = currentTheme.getAccent(false);
                    presentFragment(new ThemePreviewActivity(currentTheme, false, ThemePreviewActivity.SCREEN_TYPE_ACCENT_COLOR, accent.id >= 100, currentType == THEME_TYPE_NIGHT));
                } else if (id == reset_settings) {
                    if (getParentActivity() == null) {
                        return;
                    }
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(getParentActivity());
                    builder1.setTitle(LocaleController.getString("ThemeResetToDefaultsTitle", R.string.ThemeResetToDefaultsTitle));
                    builder1.setMessage(LocaleController.getString("ThemeResetToDefaultsText", R.string.ThemeResetToDefaultsText));
                    builder1.setPositiveButton(LocaleController.getString("Reset", R.string.Reset), (dialogInterface, i) -> {
                        boolean changed = false;


                        if (firstThemesHorizontalListCell != null) {
                            Theme.ThemeInfo themeInfo = Theme.getTheme("Arctic Blue");
                            Theme.ThemeInfo currentTheme = Theme.getCurrentTheme();
                            if (themeInfo != currentTheme) {
                                themeInfo.setCurrentAccentId(Theme.DEFALT_THEME_ACCENT_ID);
                                Theme.saveThemeAccents(themeInfo, true, false, true, false);
                                firstThemesHorizontalListCell.selectTheme(themeInfo);
                                firstThemesHorizontalListCell.smoothScrollToPosition(0);
                                secondThemesHorizentalCell.selectTheme(themeInfo);
                                secondThemesHorizentalCell.smoothScrollToPosition(0);
                                thirdThemesHorizentalCell.selectTheme(themeInfo);
                                thirdThemesHorizentalCell.smoothScrollToPosition(0);
                            } else if (themeInfo.currentAccentId != Theme.DEFALT_THEME_ACCENT_ID) {
                                NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.needSetDayNightTheme, currentTheme, currentType == THEME_TYPE_NIGHT, null, Theme.DEFALT_THEME_ACCENT_ID);

                            }
                        }
                    });
                    builder1.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                    AlertDialog alertDialog = builder1.create();
                    showDialog(alertDialog);
                    TextView button = (TextView) alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                    if (button != null) {
                        button.setTextColor(Theme.getColor(Theme.key_dialogTextRed2));
                    }
                }
            }
        });

        listAdapter = new NewThemesActivity.ListAdapter(context);

        FrameLayout frameLayout = new FrameLayout(context);
        frameLayout.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        fragmentView = frameLayout;

        listView = new RecyclerListView(context);
        listView.setLayoutManager(layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        listView.setVerticalScrollBarEnabled(false);
        listView.setAdapter(listAdapter);


        ((DefaultItemAnimator) listView.getItemAnimator()).setDelayAnimations(false);
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        return fragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (listAdapter != null) {
            updateRows(true);
        }
    }

    @Override
    protected void onTransitionAnimationEnd(boolean isOpen, boolean backward) {
        if (isOpen) {
            AndroidUtilities.requestAdjustResize(getParentActivity(), classGuid);
            AndroidUtilities.setAdjustResizeToNothing(getParentActivity(), classGuid);
        }
    }

    private void updateMenuItem() {
        if (menuItem == null) {
            return;
        }
        Theme.ThemeInfo themeInfo = Theme.getCurrentTheme();
        Theme.ThemeAccent accent = themeInfo.getAccent(false);
        if (themeInfo.themeAccents != null && !themeInfo.themeAccents.isEmpty() && accent != null && accent.id >= 100) {
            menuItem.showSubItem(share_theme);
            menuItem.showSubItem(edit_theme);
        } else {
            menuItem.hideSubItem(share_theme);
            menuItem.hideSubItem(edit_theme);
        }
        int fontSize = AndroidUtilities.isTablet() ? 18 : 16;
        Theme.ThemeInfo currentTheme = Theme.getCurrentTheme();
        if (SharedConfig.fontSize != fontSize || SharedConfig.bubbleRadius != 10 || !currentTheme.firstAccentIsDefault || currentTheme.currentAccentId != Theme.DEFALT_THEME_ACCENT_ID) {
            menuItem.showSubItem(reset_settings);
        } else {
            menuItem.hideSubItem(reset_settings);
        }
    }







    private class ListAdapter extends RecyclerListView.SelectionAdapter {

        private Context mContext;
        private boolean first = true;

        public ListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getItemCount() {
            return rowCount;
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            int type = holder.getItemViewType();
            return type == 0 || type == 1 || type == 4 || type == 7 || type == 10 || type == 11 || type == 12;
        }

        private void showOptionsForTheme(Theme.ThemeInfo themeInfo) {
            if (getParentActivity() == null || themeInfo.info != null && !themeInfo.themeLoaded || currentType == THEME_TYPE_NIGHT) {
                return;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
            CharSequence[] items;
            int[] icons;
            boolean hasDelete;
            if (themeInfo.pathToFile == null) {
                hasDelete = false;
                items = new CharSequence[]{
                        null,
                        LocaleController.getString("ExportTheme", R.string.ExportTheme)
                };
                icons = new int[]{
                        0,
                        R.drawable.msg_shareout
                };
            } else {
                hasDelete = themeInfo.info == null || !themeInfo.info.isDefault;
                items = new CharSequence[]{
                        LocaleController.getString("ShareFile", R.string.ShareFile),
                        LocaleController.getString("ExportTheme", R.string.ExportTheme),
                        themeInfo.info == null || !themeInfo.info.isDefault && themeInfo.info.creator ? LocaleController.getString("Edit", R.string.Edit) : null,
                        themeInfo.info != null && themeInfo.info.creator ? LocaleController.getString("ThemeSetUrl", R.string.ThemeSetUrl) : null,
                        hasDelete ? LocaleController.getString("Delete", R.string.Delete) : null};
                icons = new int[]{
                        R.drawable.msg_share,
                        R.drawable.msg_shareout,
                        R.drawable.msg_edit,
                        R.drawable.msg_link,
                        R.drawable.msg_delete
                };
            }
            builder.setItems(items, icons, (dialog, which) -> {
                if (getParentActivity() == null) {
                    return;
                }
                if (which == 0) {
                    if (themeInfo.info == null) {
                        MessagesController.getInstance(themeInfo.account).saveThemeToServer(themeInfo, null);
                        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.needShareTheme, themeInfo, null);
                    } else {
                        String link = "https://" + MessagesController.getInstance(currentAccount).linkPrefix + "/addtheme/" + themeInfo.info.slug;
                        showDialog(new ShareAlert(getParentActivity(), null, link, false, link, false));
                    }
                } else if (which == 1) {
                    File currentFile;
                    if (themeInfo.pathToFile == null && themeInfo.assetName == null) {
                        StringBuilder result = new StringBuilder();
                        for (HashMap.Entry<String, Integer> entry : Theme.getDefaultColors().entrySet()) {
                            result.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
                        }
                        currentFile = new File(ApplicationLoader.getFilesDirFixed(), "default_theme.attheme");
                        FileOutputStream stream = null;
                        try {
                            stream = new FileOutputStream(currentFile);
                            stream.write(AndroidUtilities.getStringBytes(result.toString()));
                        } catch (Exception e) {
                            FileLog.e(e);
                        } finally {
                            try {
                                if (stream != null) {
                                    stream.close();
                                }
                            } catch (Exception e) {
                                FileLog.e(e);
                            }
                        }
                    } else if (themeInfo.assetName != null) {
                        currentFile = Theme.getAssetFile(themeInfo.assetName);
                    } else {
                        currentFile = new File(themeInfo.pathToFile);
                    }
                    String name = themeInfo.name;
                    if (!name.endsWith(".attheme")) {
                        name += ".attheme";
                    }
                    File finalFile = new File(FileLoader.getDirectory(FileLoader.MEDIA_DIR_CACHE), FileLoader.fixFileName(name));
                    try {
                        if (!AndroidUtilities.copyFile(currentFile, finalFile)) {
                            return;
                        }
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("text/xml");
                        if (Build.VERSION.SDK_INT >= 24) {
                            try {
                                intent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(getParentActivity(), BuildConfig.APPLICATION_ID + ".provider", finalFile));
                                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            } catch (Exception ignore) {
                                intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(finalFile));
                            }
                        } else {
                            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(finalFile));
                        }
                        startActivityForResult(Intent.createChooser(intent, LocaleController.getString("ShareFile", R.string.ShareFile)), 500);
                    } catch (Exception e) {
                        FileLog.e(e);
                    }
                } else if (which == 2) {
                    if (parentLayout != null) {
                        Theme.applyTheme(themeInfo);
                        parentLayout.rebuildAllFragmentViews(true, true);
                        new ThemeEditorView().show(getParentActivity(), themeInfo);
                    }
                } else if (which == 3) {
                    presentFragment(new ThemeSetUrlActivity(themeInfo, null, false));
                } else {
                    if (getParentActivity() == null) {
                        return;
                    }
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(getParentActivity());
                    builder1.setTitle(LocaleController.getString("DeleteThemeTitle", R.string.DeleteThemeTitle));
                    builder1.setMessage(LocaleController.getString("DeleteThemeAlert", R.string.DeleteThemeAlert));
                    builder1.setPositiveButton(LocaleController.getString("Delete", R.string.Delete), (dialogInterface, i) -> {
                        MessagesController.getInstance(themeInfo.account).saveTheme(themeInfo, null, themeInfo == Theme.getCurrentNightTheme(), true);
                        if (Theme.deleteTheme(themeInfo)) {
                            parentLayout.rebuildAllFragmentViews(true, true);
                        }
                        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.themeListUpdated);
                    });
                    builder1.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                    AlertDialog alertDialog = builder1.create();
                    showDialog(alertDialog);
                    TextView button = (TextView) alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                    if (button != null) {
                        button.setTextColor(Theme.getColor(Theme.key_dialogTextRed2));
                    }
                }
            });
            AlertDialog alertDialog = builder.create();
            showDialog(alertDialog);
            if (hasDelete) {
                alertDialog.setItemColor(alertDialog.getItemsCount() - 1, Theme.getColor(Theme.key_dialogTextRed2), Theme.getColor(Theme.key_dialogRedIcon));
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case 1:
                    view = new TextSettingsCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 2:
                    view = new TextInfoPrivacyCell(mContext);
                    view.setBackgroundDrawable(Theme.getThemedDrawable(mContext, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                    break;
                case 3:
                    view = new ShadowSectionCell(mContext);
                    break;
                case 4:
                    view = new ThemeTypeCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 5:
                    view = new HeaderCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 6:
                    view = new BrightnessControlCell(mContext) {
                        @Override
                        protected void didChangedValue(float value) {
                            int oldValue = (int) (Theme.autoNightBrighnessThreshold * 100);
                            int newValue = (int) (value * 100);
                            Theme.autoNightBrighnessThreshold = value;
                            if (oldValue != newValue) {
//
                                Theme.checkAutoNightThemeConditions(true);
                            }
                        }
                    };
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 7:
                    view = new TextCheckCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;

                case 9:
                    view = new ChatListCell(mContext) {
                        @Override
                        protected void didSelectChatType(boolean threeLines) {
                            SharedConfig.setUseThreeLinesLayout(threeLines);
                        }
                    };
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 10:
                    view = new NotificationsCheckCell(mContext, 21, 64);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 11:
                    first = true;
                    firstThemesHorizontalListCell = new ThemesHorizontalListCell(mContext, currentType, defaultThemes, darkThemes) {
                        @Override
                        protected void showOptionsForTheme(Theme.ThemeInfo themeInfo) {
                            listAdapter.showOptionsForTheme(themeInfo);
                        }

                        @Override
                        protected void presentFragment(BaseFragment fragment) {
                            NewThemesActivity.this.presentFragment(fragment);
                        }

                        @Override
                        protected void updateRows() {
                            NewThemesActivity.this.updateRows(false);
                        }
                    };
                    secondThemesHorizentalCell = new ThemesHorizontalListCell(mContext, currentType, defaultThemes, darkThemes) {
                        @Override
                        protected void showOptionsForTheme(Theme.ThemeInfo themeInfo) {
                            listAdapter.showOptionsForTheme(themeInfo);
                        }

                        @Override
                        protected void presentFragment(BaseFragment fragment) {
                            NewThemesActivity.this.presentFragment(fragment);
                        }

                        @Override
                        protected void updateRows() {
                            NewThemesActivity.this.updateRows(false);
                        }
                    };
                    thirdThemesHorizentalCell = new ThemesHorizontalListCell(mContext, currentType, defaultThemes, darkThemes) {
                        @Override
                        protected void showOptionsForTheme(Theme.ThemeInfo themeInfo) {
                            listAdapter.showOptionsForTheme(themeInfo);
                        }

                        @Override
                        protected void presentFragment(BaseFragment fragment) {
                            NewThemesActivity.this.presentFragment(fragment);
                        }

                        @Override
                        protected void updateRows() {
                            NewThemesActivity.this.updateRows(false);
                        }
                    };
                    firstThemesHorizontalListCell.setDrawDivider(hasThemeAccents);
                    firstThemesHorizontalListCell.setFocusable(false);
                    view = firstThemesHorizontalListCell;
                    view.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, AndroidUtilities.dp(148)));
                    break;

                case 13:
                default:
                    view = firstThemesHorizontalListCell;
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
            }
            return new RecyclerListView.Holder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            switch (holder.getItemViewType()) {
                case 5: {
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    if (position == themeHeaderRow || position == secondHeaderRow || position == thirdHeaderRow) {
                        headerCell.setText(LocaleController.getString("ColorTheme", R.string.ColorTheme));
                    }
                }
//
                case 11: {
                    if (first) {
                        firstThemesHorizontalListCell.scrollToCurrentTheme(listView.getMeasuredWidth(), false);
                        first = false;
                    }
                    break;
                }

            }
        }

        @Override
        public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
            int type = holder.getItemViewType();
            if (type == 4) {
                ((ThemeTypeCell) holder.itemView).setTypeChecked(holder.getAdapterPosition() == Theme.selectedAutoNightType);
            }
            if (type != 2 && type != 3) {
                holder.itemView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
            }
        }

        @Override
        public int getItemViewType(int position) {

//            if (position==themeHeaderRow){
//                return 5;
//            }
           if (position == firstThemeListRow || position == secondThemeListRow || position == thirdThemeListRow) {
                return 11;
            }

            return 1;
        }
    }



    @Override
    public ThemeDescription[] getThemeDescriptions() {
        return new ThemeDescription[]{

                new ThemeDescription(fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_windowBackgroundGray),

                new ThemeDescription(actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_actionBarDefault),
                new ThemeDescription(listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, Theme.key_actionBarDefault),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, Theme.key_actionBarDefaultIcon),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, Theme.key_actionBarDefaultTitle),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, Theme.key_actionBarDefaultSelector),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SUBMENUBACKGROUND, null, null, null, null, Theme.key_actionBarDefaultSubmenuBackground),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SUBMENUITEM, null, null, null, null, Theme.key_actionBarDefaultSubmenuItem),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SUBMENUITEM | ThemeDescription.FLAG_IMAGECOLOR, null, null, null, null, Theme.key_actionBarDefaultSubmenuItemIcon),

                new ThemeDescription(listView, ThemeDescription.FLAG_SELECTOR, null, null, null, null, Theme.key_listSelector),

                new ThemeDescription(listView, 0, new Class[]{View.class}, Theme.dividerPaint, null, null, Theme.key_divider),

                new ThemeDescription(listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{ShadowSectionCell.class}, null, null, null, Theme.key_windowBackgroundGrayShadow),

                new ThemeDescription(listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{TextInfoPrivacyCell.class}, null, null, null, Theme.key_windowBackgroundGrayShadow),
                new ThemeDescription(listView, 0, new Class[]{TextInfoPrivacyCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText4),

                new ThemeDescription(listView, 0, new Class[]{TextSettingsCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText),
                new ThemeDescription(listView, 0, new Class[]{TextSettingsCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteValueText),

                new ThemeDescription(listView, 0, new Class[]{HeaderCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlueHeader),

                new ThemeDescription(listView, 0, new Class[]{TextCheckCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText),
                new ThemeDescription(listView, 0, new Class[]{TextCheckCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_switchTrack),
                new ThemeDescription(listView, 0, new Class[]{TextCheckCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_switchTrackChecked),

                new ThemeDescription(listView, ThemeDescription.FLAG_IMAGECOLOR, new Class[]{BrightnessControlCell.class}, new String[]{"leftImageView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayIcon),
                new ThemeDescription(listView, ThemeDescription.FLAG_IMAGECOLOR, new Class[]{BrightnessControlCell.class}, new String[]{"rightImageView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayIcon),
                new ThemeDescription(listView, 0, new Class[]{BrightnessControlCell.class}, new String[]{"seekBarView"}, null, null, null, Theme.key_player_progressBackground),
                new ThemeDescription(listView, ThemeDescription.FLAG_PROGRESSBAR, new Class[]{BrightnessControlCell.class}, new String[]{"seekBarView"}, null, null, null, Theme.key_player_progress),

                new ThemeDescription(listView, 0, new Class[]{ThemeTypeCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText),
                new ThemeDescription(listView, 0, new Class[]{ThemeTypeCell.class}, new String[]{"checkImage"}, null, null, null, Theme.key_featuredStickers_addedIcon),



                new ThemeDescription(listView, 0, new Class[]{ChatListCell.class}, null, null, null, Theme.key_radioBackground),
                new ThemeDescription(listView, 0, new Class[]{ChatListCell.class}, null, null, null, Theme.key_radioBackgroundChecked),

                new ThemeDescription(listView, 0, new Class[]{NotificationsCheckCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText),
                new ThemeDescription(listView, 0, new Class[]{NotificationsCheckCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText2),
                new ThemeDescription(listView, 0, new Class[]{NotificationsCheckCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_switchTrack),
                new ThemeDescription(listView, 0, new Class[]{NotificationsCheckCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_switchTrackChecked),

                new ThemeDescription(listView, 0, null, null, new Drawable[]{Theme.chat_msgInDrawable, Theme.chat_msgInMediaDrawable}, null, Theme.key_chat_inBubble),
                new ThemeDescription(listView, 0, null, null, new Drawable[]{Theme.chat_msgInSelectedDrawable, Theme.chat_msgInMediaSelectedDrawable}, null, Theme.key_chat_inBubbleSelected),
                new ThemeDescription(listView, 0, null, null, new Drawable[]{Theme.chat_msgInDrawable.getShadowDrawable(), Theme.chat_msgInMediaDrawable.getShadowDrawable()}, null, Theme.key_chat_inBubbleShadow),
                new ThemeDescription(listView, 0, null, null, new Drawable[]{Theme.chat_msgOutDrawable, Theme.chat_msgOutMediaDrawable}, null, Theme.key_chat_outBubble),
                new ThemeDescription(listView, 0, null, null, new Drawable[]{Theme.chat_msgOutDrawable, Theme.chat_msgOutMediaDrawable}, null, Theme.key_chat_outBubbleGradient),
                new ThemeDescription(listView, 0, null, null, new Drawable[]{Theme.chat_msgOutSelectedDrawable, Theme.chat_msgOutMediaSelectedDrawable}, null, Theme.key_chat_outBubbleSelected),
                new ThemeDescription(listView, 0, null, null, new Drawable[]{Theme.chat_msgOutDrawable.getShadowDrawable(), Theme.chat_msgOutMediaDrawable.getShadowDrawable()}, null, Theme.key_chat_outBubbleShadow),
                new ThemeDescription(listView, 0, null, null, null, null, Theme.key_chat_messageTextIn),
                new ThemeDescription(listView, 0, null, null, null, null, Theme.key_chat_messageTextOut),
                new ThemeDescription(listView, 0, null, null, new Drawable[]{Theme.chat_msgOutCheckDrawable}, null, Theme.key_chat_outSentCheck),
                new ThemeDescription(listView, 0, null, null, new Drawable[]{Theme.chat_msgOutCheckSelectedDrawable}, null, Theme.key_chat_outSentCheckSelected),
                new ThemeDescription(listView, 0, null, null, new Drawable[]{Theme.chat_msgOutCheckReadDrawable, Theme.chat_msgOutHalfCheckDrawable}, null, Theme.key_chat_outSentCheckRead),
                new ThemeDescription(listView, 0, null, null, new Drawable[]{Theme.chat_msgOutCheckReadSelectedDrawable, Theme.chat_msgOutHalfCheckSelectedDrawable}, null, Theme.key_chat_outSentCheckReadSelected),
                new ThemeDescription(listView, 0, null, null, new Drawable[]{Theme.chat_msgMediaCheckDrawable, Theme.chat_msgMediaHalfCheckDrawable}, null, Theme.key_chat_mediaSentCheck),
                new ThemeDescription(listView, 0, null, null, null, null, Theme.key_chat_inReplyLine),
                new ThemeDescription(listView, 0, null, null, null, null, Theme.key_chat_outReplyLine),
                new ThemeDescription(listView, 0, null, null, null, null, Theme.key_chat_inReplyNameText),
                new ThemeDescription(listView, 0, null, null, null, null, Theme.key_chat_outReplyNameText),
                new ThemeDescription(listView, 0, null, null, null, null, Theme.key_chat_inReplyMessageText),
                new ThemeDescription(listView, 0, null, null, null, null, Theme.key_chat_outReplyMessageText),
                new ThemeDescription(listView, 0, null, null, null, null, Theme.key_chat_inReplyMediaMessageSelectedText),
                new ThemeDescription(listView, 0, null, null, null, null, Theme.key_chat_outReplyMediaMessageSelectedText),
                new ThemeDescription(listView, 0, null, null, null, null, Theme.key_chat_inTimeText),
                new ThemeDescription(listView, 0, null, null, null, null, Theme.key_chat_outTimeText),
                new ThemeDescription(listView, 0, null, null, null, null, Theme.key_chat_inTimeSelectedText),
                new ThemeDescription(listView, 0, null, null, null, null, Theme.key_chat_outTimeSelectedText),
        };
    }

}
