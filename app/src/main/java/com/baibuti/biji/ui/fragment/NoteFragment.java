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

import com.baibuti.biji.common.interact.InteractInterface;
import com.baibuti.biji.common.interact.InteractStrategy;
import com.baibuti.biji.common.interact.ProgressHandler;
import com.baibuti.biji.common.interact.contract.IGroupInteract;
import com.baibuti.biji.common.interact.contract.INoteInteract;
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
import java.util.Date;
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
        // onInitNoteData();
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

        IGroupInteract groupInteract = InteractStrategy.getInstance().getGroupInteract(getContext());
        ProgressHandler.process(getContext(), "获取分组信息中...", true,
            groupInteract.queryAllGroups(), new InteractInterface<List<Group>>() {
                @Override
                public void onSuccess(List<Group> groups) {
                    // UnsupportedOperationException
                    groups.add(0, Group.AllGroups);
                    Collections.sort(groups);
                    m_group_adapter.setGroupList(groups);
                    m_group_adapter.setCurrentItem(Group.AllGroups);
                    m_group_adapter.notifyDataSetChanged();
                    m_drawerLayout.openDrawer(Gravity.END);
                }

                @Override
                public void onError(String message) {
                    showAlert(getActivity(), "错误", "分组信息获取错误：" + message);
                }

                @Override
                public void onFailed(Throwable throwable) {
                    showAlert(getContext(), "错误", "网络错误：" + throwable.getMessage());
                }
            }
        );
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

        IGroupInteract groupInteract = InteractStrategy.getInstance().getGroupInteract(getContext());
        ProgressHandler.process(getContext(), "分组信息加载中...", true,
            groupInteract.queryAllGroups(), new InteractInterface<List<Group>>() {
                @Override
                public void onSuccess(List<Group> groups) {
                    GroupAdapter groupAdapter = new GroupAdapter(getContext());
                    groupAdapter.setList(groups);

                    showAlert(getContext(), "修改分组",
                        groupAdapter, (d, w) -> {
                            if (note.getGroup().getId() == groups.get(w).getId())
                                return;

                            Date motoUt = note.getUpdateTime();
                            Group motoGroup = note.getGroup();
                            note.setGroup(groups.get(w));
                            note.setUpdateTime(new Date());

                            INoteInteract noteInteract = InteractStrategy.getInstance().getNoteInteract(getContext());
                            ProgressHandler.process(getContext(), "修改笔记分组中...", true,
                                noteInteract.updateNote(note), new InteractInterface<Boolean>() {
                                    @Override
                                    public void onSuccess(Boolean data) {
                                        Collections.sort(pageData.allNotes);
                                        Collections.sort(pageData.showNoteList);
                                        m_note_adapter.notifyDataSetChanged();
                                        showToast(getActivity(), "笔记 \"" + note.getTitle() + "\" 分组修改成功");
                                        d.dismiss();
                                    }

                                    @Override
                                    public void onError(String message) {
                                        note.setGroup(motoGroup);
                                        note.setUpdateTime(motoUt);
                                        showAlert(getActivity(), "错误", message);
                                    }

                                    @Override
                                    public void onFailed(Throwable throwable) {
                                        note.setGroup(motoGroup);
                                        note.setUpdateTime(motoUt);
                                        showAlert(getContext(), "错误", "网络错误：" + throwable.getMessage());
                                    }
                                }
                            );
                        },
                        "返回", null
                    );
                }

                @Override
                public void onError(String message) {
                    showAlert(getContext(), "错误", "分组信息加载错误：" + message);
                }

                @Override
                public void onFailed(Throwable throwable) {
                    showAlert(getContext(), "错误", "网络错误：" + throwable.getMessage());
                }
            }
        );
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

        IGroupInteract groupInteract = InteractStrategy.getInstance().getGroupInteract(getContext());
        ProgressHandler.process(getContext(), "获取分组信息中...", true,
            groupInteract.queryAllGroups(), new InteractInterface<List<Group>>() {
                @Override
                public void onSuccess(List<Group> groups) {
                    Collections.sort(groups);
                    GroupDialog dialog = new GroupDialog(getActivity(), groups, () -> {
                        onInitNoteData();
                        // showToast(getContext(), "分组信息修改成功");
                    });
                    dialog.setCancelable(true);
                    dialog.show();
                }

                @Override
                public void onError(String message) {
                    showAlert(getActivity(), "错误", "分组信息获取错误：" + message);
                }

                @Override
                public void onFailed(Throwable throwable) {
                    showAlert(getContext(), "错误", "网络错误：" + throwable.getMessage());
                }
            }
        );
    }

    // endregion

    // region !!! new edit

    public static final String INT_NOTE_DATA = "note_data";
    public static final String INT_IS_NEW = "is_new";
    public static final String INT_IS_MODIFIED = "is_modified";

    /**
     * 新建笔记
     * TODO 使用 id 传参数
     */
    private void newNote() {

        Note note = new Note();
        IGroupInteract groupInteract = InteractStrategy.getInstance().getGroupInteract(getContext());
        ProgressHandler.process(getContext(), "加载分组信息中...", false,
            groupInteract.queryDefaultGroup(), new InteractInterface<Group>() {
                @Override
                public void onSuccess(Group data) {

                    Intent intent = new Intent(getActivity(), EditNoteActivity.class);
                    intent.putExtra(INT_NOTE_DATA, note);
                    intent.putExtra(INT_IS_NEW, true); // NEW

                    RxActivityResult.on(NoteFragment.this).startIntent(intent) // -> INT_NOTE_DATA, INT_IS_MODIFIED, INT_IS_NEW
                        .subscribe((result) -> {
                            if (result.resultCode() != RESULT_OK)
                                return;

                            ProgressDialog progressDialog = showProgress(getContext(), "加载内容...", false, null);

                            Intent returnIntent = result.data();
                            Note newNote = (Note) returnIntent.getSerializableExtra(INT_NOTE_DATA);
                            boolean isModify = returnIntent.getBooleanExtra(INT_IS_MODIFIED, true);
                            if (isModify) {
                                showToast(getContext(), String.format(Locale.CHINA, "笔记 \"%s\" 新建成功", newNote.getTitle()));
                                pageData.allNotes.add(newNote);
                                pageData.showNoteList.add(newNote);
                                Collections.sort(pageData.allNotes);
                                Collections.sort(pageData.showNoteList);
                                m_note_adapter.notifyDataSetChanged();
                            }

                            progressDialog.dismiss();
                        }, Throwable::printStackTrace).isDisposed();
                }

                @Override
                public void onError(String message) {
                    showAlert(getContext(), "错误", "默认分组获取失败：" + message);
                }

                @Override
                public void onFailed(Throwable throwable) {
                    showAlert(getContext(), "错误", "网络错误：" + throwable.getMessage());
                }
            }
        );
    }

    /**
     * 查看已经存在的笔记
     * TODO 使用 id 传参
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
        INoteInteract noteInteract = InteractStrategy.getInstance().getNoteInteract(getContext());
        ProgressHandler.process(getContext(), "加载数据中...", false,
            noteInteract.queryAllNotes(), new InteractInterface<List<Note>>() {
                @Override
                public void onSuccess(List<Note> data) {
                    pageData.allNotes = data;
                    Collections.sort(pageData.allNotes);

                    pageData.showNoteList.clear();
                    pageData.showNoteList.addAll(pageData.allNotes);
                    Collections.sort(pageData.showNoteList);
                    m_note_adapter.notifyDataSetChanged();

                    m_swipeRefresh.setRefreshing(false);
                }

                @Override
                public void onError(String message) {
                    showAlert(getContext(), "错误", message);
                }

                @Override
                public void onFailed(Throwable throwable) {
                    showAlert(getContext(), "错误", "网络错误：" + throwable.getMessage());
                }
            }
        );
    }

    /**
     * 删除笔记，更新页面
     */
    private void deleteNote(@NonNull Note note) {

        INoteInteract noteInteract = InteractStrategy.getInstance().getNoteInteract(getContext());
        ProgressHandler.process(getContext(), "删除笔记中...", true,
            noteInteract.deleteNote(note.getId()), new InteractInterface<Boolean>() {
                @Override
                public void onSuccess(Boolean data) {
                    pageData.showNoteList.remove(note);
                    m_note_adapter.notifyDataSetChanged();
                    showToast(getActivity(), "删除成功：\"" + note.getTitle() + "\"");
                }

                @Override
                public void onError(String message) {
                    showAlert(getContext(), "错误", message);
                }

                @Override
                public void onFailed(Throwable throwable) {
                    showAlert(getContext(), "错误", "网路错误：" + throwable.getMessage());
                }
            }
        );
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
        INoteInteract noteInteract = InteractStrategy.getInstance().getNoteInteract(getContext());
        ProgressHandler.process(getContext(), "分组搜索中...", true,
            noteInteract.queryNotesByGroupId(group.getId()), new InteractInterface<List<Note>>() {
                @Override
                public void onSuccess(List<Note> notes) {
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

                @Override
                public void onError(String message) {
                    showAlert(getContext(), "错误", message);
                }

                @Override
                public void onFailed(Throwable throwable) {
                    showAlert(getContext(), "错误", "网络错误：" + throwable.getMessage());
                }
            }
        );
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
