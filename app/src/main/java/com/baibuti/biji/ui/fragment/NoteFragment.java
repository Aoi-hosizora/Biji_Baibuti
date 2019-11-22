package com.baibuti.biji.ui.fragment;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.baibuti.biji.common.interact.InteractStrategy;
import com.baibuti.biji.model.dao.DbStatusType;
import com.baibuti.biji.common.interact.contract.IGroupInteract;
import com.baibuti.biji.common.interact.contract.INoteInteract;
import com.baibuti.biji.model.dto.ServerException;
import com.baibuti.biji.common.auth.AuthManager;
import com.baibuti.biji.ui.IContextHelper;
import com.baibuti.biji.ui.activity.MainActivity;
import com.baibuti.biji.model.po.Group;
import com.baibuti.biji.model.po.Note;
import com.baibuti.biji.ui.activity.EditNoteActivity;
import com.baibuti.biji.ui.activity.OCRActivity;
import com.baibuti.biji.ui.activity.ViewNoteActivity;
import com.baibuti.biji.ui.adapter.GroupAdapter;
import com.baibuti.biji.ui.adapter.GroupRadioAdapter;
import com.baibuti.biji.ui.adapter.NoteAdapter;
import com.baibuti.biji.R;
import com.baibuti.biji.ui.dialog.GroupDialog;
import com.baibuti.biji.ui.widget.listView.RecyclerViewEmptySupport;
import com.baibuti.biji.ui.widget.listView.SpacesItemDecoration;
import com.baibuti.biji.util.filePathUtil.AppPathUtil;
import com.baibuti.biji.util.filePathUtil.FileNameUtil;
import com.baibuti.biji.util.otherUtil.LayoutUtil;
import com.baibuti.biji.util.imgTextUtil.SearchUtil;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.wyt.searchbox.SearchFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.kareluo.imaging.IMGEditActivity;
import rx_activity_result2.RxActivityResult;

import static android.app.Activity.RESULT_OK;

public class NoteFragment extends BaseFragment implements IContextHelper {

    private View view;

    private NoteAdapter m_note_adapter;
    private GroupRadioAdapter m_group_adapter;

    @BindView(R.id.note_fabmenu)
    FloatingActionsMenu m_fabMenu;

    private SearchFragment m_searchFragment;

    @BindView(R.id.note_listsrl)
    SwipeRefreshLayout m_swipeRefresh;

    @BindView(R.id.NoteFrag_Toolbar)
    Toolbar m_toolbar;

    @BindView(R.id.id_noteFrag_drawer_layout)
    DrawerLayout m_drawerLayout;

    private Dialog m_notePopupMenu;

    /**
     * 当前页面状态
     */
    private enum PageState {
        NORMAL, SEARCHING, GROUPING
    }

    /**
     * 当前页面数据
     */
    private static class PageData {

        /**
         * 当前页面显示的内容
         * FIX: 删除了 setContent()
         */
        List<Note> showNoteList = new ArrayList<>();

        List<Note> allNotes = new ArrayList<>();

        PageState pageState = PageState.NORMAL;
    }

    /**
     * 当前页面 状态与数据
     */
    private PageData pageData = new PageData();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (null != view) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (null != parent)
                parent.removeView(view);
        } else {
            view = inflater.inflate(R.layout.fragment_note, container, false);
            ButterKnife.bind(this, view);

            initView();

            AuthManager.getInstance().addLoginChangeListener(new AuthManager.OnLoginChangeListener() {
                @Override
                public void onLogin(String username) {
                    onInitNoteData();
                }

                @Override
                public void onLogout() {
                    onInitNoteData();
                }
            });
        }
        return view;
    }

    /**
     * inflate 后 初始化界面
     */
    private void initView() {

        // Toolbar
        m_toolbar.setTitle("所有笔记");
        m_toolbar.inflateMenu(R.menu.note_frag_action);
        m_toolbar.setNavigationIcon(R.drawable.tab_menu);
        m_toolbar.setNavigationOnClickListener((View view) -> {
            MainActivity activity = (MainActivity) getActivity();
            if (activity != null) activity.openNavMenu();
        });
        m_toolbar.setPopupTheme(R.style.popup_theme);
        m_toolbar.setOnMenuItemClickListener(menuItemClickListener);

        // Swipe Refresh
        m_swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        m_swipeRefresh.setOnRefreshListener(this::onInitNoteData);

        // SearchFrag
        m_searchFragment = SearchFragment.newInstance();
        m_searchFragment.setAllowReturnTransitionOverlap(true);
        m_searchFragment.setOnSearchClickListener((String keyword) -> {
            if (!keyword.trim().isEmpty())
                searchNote(keyword);
        });

        // Fab
        View back = view.findViewById(R.id.note_fabmenu_back);
        back.setOnClickListener(v -> m_fabMenu.collapse());
        m_fabMenu.setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu.OnFloatingActionsMenuUpdateListener() {

            @Override
            public void onMenuExpanded() {
                new Handler().postDelayed(() -> back.setVisibility(View.VISIBLE), 100);
            }

            @Override
            public void onMenuCollapsed() {
                new Handler().postDelayed(() -> back.setVisibility(View.GONE), 100);
            }
        });

        // Note List
        RecyclerViewEmptySupport m_noteListView = view.findViewById(R.id.note_list);
        m_noteListView.setEmptyView(view.findViewById(R.id.note_emptylist));

        m_noteListView.addItemDecoration(new SpacesItemDecoration(0));
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        m_noteListView.setLayoutManager(layoutManager);

        m_note_adapter = new NoteAdapter(getContext());
        m_note_adapter.setNoteList(pageData.showNoteList);
        m_note_adapter.setOnItemClickListener((v, note) -> openViewNote(note));
        m_note_adapter.setOnItemLongClickListener((v, note) -> {
            showPopupMenu(note);
            return true;
        });
        m_noteListView.setAdapter(m_note_adapter);

        // Right Nav
        m_drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        NavigationView m_navigationView = view.findViewById(R.id.id_noteFrag_Right_nav);
        m_navigationView.setNavigationItemSelectedListener((@NonNull MenuItem item) -> false);
        LayoutUtil.setNavigationViewWidth(getActivity(), m_navigationView, 2.0 / 3);

        ImageButton m_groupMgrBackButton = view.findViewById(R.id.id_NoteFrag_nav_BackButton);
        m_groupMgrBackButton.setOnClickListener(v -> m_drawerLayout.closeDrawer(Gravity.END));

        // Group List
        ListView m_groupListView = view.findViewById(R.id.id_NoteFrag_nav_GroupList);

        m_group_adapter = new GroupRadioAdapter(getContext());
        m_group_adapter.setOnRadioButtonClickListener(this::GroupRadio_Clicked);
        m_groupListView.setAdapter(m_group_adapter);

        // PageData
        onInitNoteData();
    }

    @Override
    public boolean onBackPressed() {

        // 是否打开 Drawer
        if (m_drawerLayout != null && m_drawerLayout.isDrawerOpen(Gravity.END)) {
            m_drawerLayout.closeDrawer(Gravity.END);
            return true;
        }

        // 是否展开 Fab
        if (m_fabMenu != null && m_fabMenu.isExpanded()) {
            m_fabMenu.collapse();
            return true;
        }

        // 是否处于搜索或分组
        if (pageData.pageState != PageState.NORMAL) {
            toNormal();
            return true;
        }

        return false;
    }

    /*

    1. 页面状态：正常，搜索，分组
    2. 一个列表：setListContent
    3. 提前设置好 Adapter

     */

    // region Toolbar Fab Popup

    Toolbar.OnMenuItemClickListener menuItemClickListener = (item) -> {
        switch (item.getItemId()) {
            case R.id.action_search:
                ToolbarSearch_Clicked();
                break;
            case R.id.action_group:
                ToolbarGroup_Clicked();
                break;
            case R.id.action_search_group_back:
                ToolbarBack_Clicked();
                break;
        }
        return true;
    };

    /**
     * 工具栏 搜索
     */
    private void ToolbarSearch_Clicked() {
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null)
            m_searchFragment.show(activity.getSupportFragmentManager(), com.wyt.searchbox.SearchFragment.TAG);
    }

    /**
     * 工具栏 分组
     */
    private void ToolbarGroup_Clicked() {
        boolean[] cancel = new boolean[] { false };
        ProgressDialog progressDialog = showProgress(getContext(), "获取分组信息中...", true, v -> cancel[0] = true);
        try {
            List<Group> groups = InteractStrategy.getInstance().getGroupInteract(getContext()).queryAllGroups();
            if (cancel[0]) return;

            groups.add(0, Group.AllGroups);
            Collections.sort(groups);
            m_group_adapter.setGroupList(groups);
            m_group_adapter.setCurrentItem(Group.AllGroups);
            m_group_adapter.notifyDataSetChanged();

            progressDialog.dismiss();
            m_drawerLayout.openDrawer(Gravity.END);
        } catch (ServerException ex) {
            progressDialog.dismiss();
            ex.printStackTrace();
            showAlert(getActivity(), "错误", "分组信息获取错误：" + ex.getMessage());
        }
    }

    /**
     * 工具栏 返回
     */
    private void ToolbarBack_Clicked() {
        toNormal();
    }

    /**
     * Tab 新建笔记
     */
    @OnClick(R.id.note_edit)
    void NewNoteFab_Clicked() {
        m_fabMenu.collapse();
        newNote();
    }

    /**
     * Tab OCR 拍照
     */
    @OnClick(R.id.note_photo)
    void OCRFab_Clicked() {
        m_fabMenu.collapse();
        OCRTakePhoto();
    }

    /**
     * 弹出菜单
     */
    private void showPopupMenu(Note note) {
        Context context = getContext();
        if (context == null) return;

        m_notePopupMenu = new Dialog(context, R.style.BottomDialog);
        LinearLayout root = LayoutUtil.initPopupMenu(getContext(), m_notePopupMenu, R.layout.popup_note_long_click_note);
        // m_notePopupMenu.setOnCancelListener((dialog) -> pageData.longClickedNote = null);

        TextView label = root.findViewById(R.id.id_NoteFrag_PopupMenu_Label);
        label.setText(String.format(Locale.CHINA, "当前选中：%s", note.getTitle()));

        root.findViewById(R.id.id_NoteFrag_PopupMenu_ViewNote).setOnClickListener((view) -> ViewNotePopup_Clicked(note));
        root.findViewById(R.id.id_NoteFrag_PopupMenu_ChangeGroup).setOnClickListener((view) -> ChangeGroupPopup_Clicked(note));
        root.findViewById(R.id.id_NoteFrag_PopupMenu_DeleteNote).setOnClickListener((view) -> DeleteNotePopup_Clicked(note));
        root.findViewById(R.id.id_NoteFrag_PopupMenu_Cancel).setOnClickListener((view) -> m_notePopupMenu.cancel());

        m_notePopupMenu.show();
    }

    /**
     * 弹出菜单 打开笔记
     */
    private void ViewNotePopup_Clicked(Note note) {
        m_notePopupMenu.cancel();
        if (note == null) return;

        openViewNote(note);
    }

    /**
     * 弹出菜单 修改分组
     */
    private void ChangeGroupPopup_Clicked(Note note) {
        m_notePopupMenu.cancel();
        if (note == null) return;

        ProgressDialog progressDialog = showProgress(getContext(), "分组信息加载中...", false, null);
        try {
            List<Group> groups = InteractStrategy.getInstance().getGroupInteract(getContext()).queryAllGroups();
            progressDialog.dismiss();
            GroupAdapter groupAdapter = new GroupAdapter(getContext());
            groupAdapter.setList(groups);

            showAlert(getContext(), "修改分组",
                groupAdapter, (d, w) -> {
                    Group motoGroup = note.getGroup();
                    note.setGroup(groups.get(w));
                    try {
                        INoteInteract noteDao = InteractStrategy.getInstance().getNoteInteract(getContext());
                        if (noteDao.updateNote(note) == DbStatusType.SUCCESS) {
                            m_note_adapter.notifyDataSetChanged();
                            showToast(getActivity(), "笔记 \"" + note.getTitle() + "\" 分组修改成功");
                            d.dismiss();
                            return;
                        }
                        showAlert(getActivity(), "错误", "笔记分组修改错误。");
                    } catch (ServerException ex) {
                        ex.printStackTrace();
                        showAlert(getActivity(), "错误", "笔记分组修改错误：" + ex.getMessage());
                    }
                    note.setGroup(motoGroup);
                    d.dismiss();
                },
                "返回", null
            );
        } catch (ServerException ex) {
            showAlert(getContext(), "错误", "分组信息加载错误：" + ex.getMessage());
        }

    }

    /**
     * 弹出菜单 删除笔记
     */
    private void DeleteNotePopup_Clicked(Note note) {
        m_notePopupMenu.cancel();
        if (note == null) return;

        showAlert(getContext(), "删除笔记", "确定删除笔记 \"" + note.getTitle() + "\" 吗？",
            "删除", (d, w) -> deleteNote(note),
            "取消", null);
    }

    // endregion

    // region Right Nav Group

    /**
     * 右边栏 选中列表
     */
    private void GroupRadio_Clicked(int position) {
        m_drawerLayout.closeDrawer(Gravity.END);
        groupNote(m_group_adapter.getGroupList().get(position));
    }

    /**
     * 右边栏 分组管理
     */
    @OnClick(R.id.id_NoteFrag_nav_GroupMgrButton)
    void GroupMgrButton_Click() {
        if (pageData.pageState == PageState.GROUPING) {
            showAlert(getContext(), "分组管理", "进行分组管理需要退出分组显示，是否继续？",
                "继续", (DialogInterface dialog, int which) -> ShowGroupDialog(),
                "取消", null);
        }
        else
            ShowGroupDialog();
    }

    /**
     * !!! 显示分组管理对话框
     */
    private void ShowGroupDialog() {
        m_drawerLayout.closeDrawer(Gravity.END);
        ProgressDialog progressDialog = showProgress(getContext(), "分组信息加载中...", false, null);
        try {
            IGroupInteract groupDao = InteractStrategy.getInstance().getGroupInteract(getActivity());
            List<Group> groups = groupDao.queryAllGroups();
            Collections.sort(groups);

            progressDialog.dismiss();
            GroupDialog dialog = new GroupDialog(getActivity(), groups, this::onInitNoteData);
            dialog.setCancelable(true);
            dialog.show();

        } catch (ServerException ex) {
            progressDialog.dismiss();
            ex.printStackTrace();
            showAlert(getActivity(), "错误", "分组信息加载失败。");
        }
    }

    // endregion

    // region !!! new edit

    public static final String INT_NOTE_DATA = "note_data";
    public static final String INT_IS_NEW = "is_new";
    public static final String INT_IS_MODIFIED = "is_modified";

    /**
     * 新建笔记
     */
    private void newNote() {
        Note note = new Note();
        try {
            Group defGroup = InteractStrategy.getInstance().getGroupInteract(getContext()).queryDefaultGroup();
            note.setGroup(defGroup);
        } catch (ServerException ex) {
            showAlert(getContext(), "错误", "获取默认分组错误：" + ex.getMessage());
            return;
        }

        Intent intent = new Intent(getActivity(), EditNoteActivity.class);
        intent.putExtra(INT_NOTE_DATA, note);
        intent.putExtra(INT_IS_NEW, true); // NEW

        RxActivityResult.on(this).startIntent(intent) // -> INT_NOTE_DATA, INT_IS_MODIFIED, INT_IS_NEW
            .subscribe((result) -> {
                if (result.resultCode() != RESULT_OK)
                    return;

                ProgressDialog progressDialog = showProgress(getContext(), "加载内容...", false, null);

                Intent returnIntent = result.data();
                Note newNote = (Note) returnIntent.getSerializableExtra(INT_NOTE_DATA);
                boolean isModify = returnIntent.getBooleanExtra(INT_IS_MODIFIED, true);
                if (isModify) {
                    showToast(getContext(), String.format(Locale.CHINA, "笔记 \"%s\"新建成功", newNote.getTitle()));
                    pageData.allNotes.add(newNote);
                    pageData.showNoteList.add(newNote);
                    m_note_adapter.notifyDataSetChanged();
                }

                progressDialog.dismiss();
            }, Throwable::printStackTrace).isDisposed();
    }

    /**
     * 查看已经存在的笔记
     */
    private void openViewNote(@NonNull Note note) {
        Intent intent = new Intent(getActivity(), ViewNoteActivity.class);
        intent.putExtra(INT_NOTE_DATA, note);
        intent.putExtra(INT_IS_NEW, false);

        RxActivityResult.on(this).startIntent(intent) // -> INT_NOTE_DATA, INT_IS_MODIFIED, INT_IS_NEW
            .subscribe((result) -> {
                if (result.resultCode() != RESULT_OK)
                    return;

                ProgressDialog progressDialog = showProgress(getContext(), "加载内容...", false, null);

                Intent returnIntent = result.data();
                Note modifiedNote = (Note) returnIntent.getSerializableExtra(INT_NOTE_DATA);
                boolean isModify = returnIntent.getBooleanExtra(INT_IS_MODIFIED, true);
                if (isModify) {
                    showToast(getContext(), "笔记 \"" + modifiedNote.getTitle() + "\" 更新成功");
                    for (Note note1 : pageData.showNoteList) {
                        if (note1.getId() == modifiedNote.getId()) {

                            int idx = pageData.allNotes.indexOf(note1);
                            if (idx != -1) pageData.allNotes.set(idx, modifiedNote);
                            idx = pageData.showNoteList.indexOf(note1);
                            if (idx != -1) pageData.showNoteList.set(idx, modifiedNote);

                            Collections.sort(pageData.allNotes);
                            Collections.sort(pageData.showNoteList);
                            break;
                        }
                    }
                    m_note_adapter.notifyDataSetChanged();
                }

                progressDialog.dismiss();
            }, Throwable::printStackTrace).isDisposed();
    }

    // endregion

    // region PageData initData setContent delete

    /**
     * 初始化与刷新 加载数据
     */
    private void onInitNoteData() {
        ProgressDialog progressDialog = showProgress(getContext(), "加载数据中...", false, null);
        try {
            pageData.allNotes = InteractStrategy.getInstance().getNoteInteract(getContext()).queryAllNotes();
            Collections.sort(pageData.allNotes);

            pageData.showNoteList.clear();
            pageData.showNoteList.addAll(pageData.allNotes);
            Collections.sort(pageData.showNoteList);
            m_note_adapter.notifyDataSetChanged();

            m_swipeRefresh.setRefreshing(false);
            progressDialog.dismiss();
        } catch (ServerException ex) {
            ex.printStackTrace();
            m_swipeRefresh.setRefreshing(false);
            progressDialog.dismiss();
            showAlert(getContext(), "错误", "数据加载错误：" + ex.getMessage(),
                "重试", (d, w) -> onInitNoteData(), "确定", null);
        }
    }

    /**
     * 删除笔记，更新页面
     */
    private void deleteNote(@NonNull Note note) {
        try {
            INoteInteract noteDao = InteractStrategy.getInstance().getNoteInteract(getContext());

            // int idx = adapter.getNoteList().indexOf(note);
            if (noteDao.deleteNote(note.getId()) != DbStatusType.SUCCESS) {
                showAlert(getActivity(), "错误", "删除笔记错误。");
                return;
            }
            pageData.showNoteList.remove(note);
            m_note_adapter.notifyDataSetChanged();
            showToast(getActivity(), "删除成功：\"" + note.getTitle() + "\"");
        } catch (ServerException ex) {
            showAlert(getContext(), "错误", "删除笔记错误：" + ex.getMessage());
        }
    }

    // endregion

    // region Note Group Search

    /**
     * 分组显示
     */
    private void groupNote(Group group) {

        if (group == Group.AllGroups) {
            toNormal();
            return;
        }

        boolean[] cancel = new boolean[] { false };
        ProgressDialog progressDialog = showProgress(getActivity(), "分组搜索中...",
            true, (v) -> cancel[0] = true);

        // 数据处理
        try {
            List<Note> notes = InteractStrategy.getInstance().getNoteInteract(getContext()).queryNotesByGroupId(group.getId());
            if (!cancel[0] && notes != null) {

                progressDialog.dismiss();
                m_toolbar.setTitle(String.format(Locale.CHINA, "\"%s\" 分组结果 (共 %d 项)", group.getName(), notes.size()));
                if (notes.size() != 0)
                    showToast(getActivity(), "共找到 " + notes.size() + " 项");
                else
                    showToast(getActivity(), "没有找到内容");

                pageData.showNoteList.clear();
                pageData.showNoteList.addAll(notes);
                Collections.sort(pageData.showNoteList);
                m_note_adapter.notifyDataSetChanged();

                pageData.pageState = PageState.GROUPING;

                // 页面显示处理
                m_toolbar.getMenu().findItem(R.id.action_search_group_back).setVisible(true); // 显示返回
                m_toolbar.getMenu().findItem(R.id.action_search).setVisible(true); // 显示搜索
                m_toolbar.getMenu().findItem(R.id.action_group).setVisible(false); // 隐藏分组

                m_swipeRefresh.setEnabled(false); // 禁止刷新
                m_fabMenu.setVisibility(View.GONE); // 隐藏 Fab
                m_drawerLayout.setEnabled(false); // 禁止分组

            }
        } catch (ServerException ex) {
            showAlert(getContext(), "错误", "数据加载错误：" + ex.getMessage());
        }
    }

    /**
     * 搜索显示
     */
    private void searchNote(String searchingStr) {

        boolean[] cancel = new boolean[] { false };
        ProgressDialog progressDialog = showProgress(getActivity(), "搜索中...",
            true, (v) -> cancel[0] = true);

        // 数据处理
        List<Note> searchResult =
            SearchUtil.getSearchItems(pageData.allNotes.toArray(new Note[0]), searchingStr);

        if (!cancel[0]) {
            progressDialog.dismiss();
            m_toolbar.setTitle(String.format(Locale.CHINA, "\"%s\" 的搜索结果 (共 %d 项)", searchingStr, searchResult.size()));
            if (searchResult.size() != 0)
                showToast(getActivity(), "共找到 " + searchResult.size() + " 项");
            else
                showToast(getActivity(), "没有找到内容");

            pageData.showNoteList.clear();
            pageData.showNoteList.addAll(searchResult);
            Collections.sort(pageData.showNoteList);
            m_note_adapter.notifyDataSetChanged();

            pageData.pageState = PageState.SEARCHING;

            // 页面显示处理
            m_toolbar.getMenu().findItem(R.id.action_search_group_back).setVisible(true); // 显示返回
            m_toolbar.getMenu().findItem(R.id.action_search).setVisible(false); // 隐藏搜索
            m_toolbar.getMenu().findItem(R.id.action_group).setVisible(true); // 允许分组

            m_drawerLayout.setEnabled(true); // 允许分组
            m_swipeRefresh.setEnabled(false); // 禁止刷新
            m_fabMenu.setVisibility(View.GONE); // 隐藏 Fab
        }
    }

    /**
     * 正常状态，非搜索非分组
     */
    private void toNormal() {
        ProgressDialog progressDialog = showProgress(getContext(), "返回加载数据中...", false, null);

        try {
            INoteInteract noteDao = InteractStrategy.getInstance().getNoteInteract(getContext());
            pageData.allNotes.clear();
            pageData.allNotes.addAll(noteDao.queryAllNotes());
        } catch (ServerException ex) {
            progressDialog.dismiss();
            ex.printStackTrace();
            showAlert(getContext(), "错误", "获取数据错误：" + ex.getMessage());
            return;
        }

        pageData.pageState = PageState.NORMAL;

        // 页面显示处理
        m_toolbar.getMenu().findItem(R.id.action_search_group_back).setVisible(false); // 隐藏返回
        m_toolbar.getMenu().findItem(R.id.action_search).setVisible(true); // 允许搜索
        m_toolbar.getMenu().findItem(R.id.action_group).setVisible(true); // 允许分组

        m_swipeRefresh.setEnabled(true); // 允许刷新
        m_fabMenu.setVisibility(View.VISIBLE); // 显示 Fab
        m_drawerLayout.setEnabled(true); // 允许分组
        m_toolbar.setTitle("所有笔记");

        progressDialog.dismiss();

        // 数据处理
        pageData.showNoteList.clear();
        pageData.showNoteList.addAll(pageData.allNotes);
        Collections.sort(pageData.showNoteList);
        m_note_adapter.notifyDataSetChanged();
    }

    // endregion

    // region OCR

    /**
     * 文字识别
     *
     * 拍照 (生成 photoUri) ->
     * 编辑 (生成 editedUri) ->
     * 识别 (删除 photoUri) -> 保留 editedUri
     */
    private void OCRTakePhoto() {
        String photoPath = FileNameUtil.getImageFileName(FileNameUtil.SaveType.PHOTO);

        // 7.0 调用系统相机拍照不再允许使用 Uri 方式，应该替换为 FileProvider
        Uri photoUri = AppPathUtil.getUriByPath(getContext(), photoPath);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);

        RxActivityResult.on(this).startIntent(intent) // photoUri -> 打开相机 -> returnUri(imgPath)
            .subscribe((result) -> {
                if (result.resultCode() != RESULT_OK) return;
                Uri returnUri = result.data().getData();
                String imgPath;
                if (returnUri == null || (imgPath = AppPathUtil.getFilePathByUri(getContext(), returnUri)) == null || imgPath.isEmpty()) {
                    showAlert(getContext(), "错误", "从相册获取的图片或拍照得到的图片不存在，请重试。");
                    return;
                }
                String editedPath = FileNameUtil.getImageFileName(FileNameUtil.SaveType.EDITED); // 编辑的文件名

                Intent intent1 = new Intent(getActivity(), IMGEditActivity.class);
                intent1.putExtra(IMGEditActivity.INT_IMAGE_URI, photoUri);
                intent1.putExtra(IMGEditActivity.INT_IMAGE_SAVE_URI, editedPath);
                RxActivityResult.on(this).startIntent(intent1) // editedPath -> 编辑 -> editedUri
                    .subscribe((result1) -> {
                        if (result1.resultCode() != RESULT_OK) return;
                        AppPathUtil.deleteFile(imgPath); // 删除原始拍照图片
                        Uri editedUri = result1.data().getData();
                        if (editedUri == null)
                            showAlert(getContext(), "错误", "编辑得到的图片不存在，请重试。");
                        else {
                            Intent intent2 = new Intent(getContext(), OCRActivity.class);
                            intent2.putExtra(OCRActivity.INT_IMAGE_PATH, editedUri.toString());
                            startActivity(intent2);
                        }
                    }).isDisposed();

            }, (throwable) -> {
                throwable.printStackTrace();
                showAlert(getContext(), "错误", "打开设备摄像机错误，请检查。");
            }).isDisposed();
    }
    // endregion
}
