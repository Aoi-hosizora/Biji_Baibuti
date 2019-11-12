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

import com.baibuti.biji.model.dao.DaoStrategyHelper;
import com.baibuti.biji.model.dto.ServerException;
import com.baibuti.biji.service.auth.AuthManager;
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
import com.baibuti.biji.util.filePathUtil.SaveNameUtil;
import com.baibuti.biji.util.otherUtil.LayoutUtil;
import com.baibuti.biji.util.imgTextUtil.SearchUtil;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.wyt.searchbox.SearchFragment;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import me.kareluo.imaging.IMGEditActivity;

import static android.app.Activity.RESULT_OK;

public class NoteFragment extends BaseFragment implements IContextHelper {

    private View view;

    @BindView(R.id.note_list)
    private RecyclerViewEmptySupport m_noteListView;

    @BindView(R.id.id_NoteFrag_nav_GroupList)
    private ListView m_groupListView;

    @BindView(R.id.note_fabmenu)
    private FloatingActionsMenu m_fabMenu;

    // SearchFragment.newInstance()
    private SearchFragment m_searchFragment;

    @BindView(R.id.note_listsrl)
    private SwipeRefreshLayout m_swipeRefresh;

    @BindView(R.id.NoteFrag_Toolbar)
    private Toolbar m_toolbar;

    @BindView(R.id.id_noteFrag_drawer_layout)
    private DrawerLayout m_drawerLayout;

    // new Dialog(getContext(), R.style.BottomDialog)
    private Dialog m_notePopupMenu;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (null != view) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (null != parent)
                parent.removeView(view);
        } else {
            view = inflater.inflate(R.layout.fragment_note, container, false);
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

        // List Empty View
        m_noteListView.setEmptyView(view.findViewById(R.id.note_emptylist));

        // Swipe Refresh
        m_swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        m_swipeRefresh.setOnRefreshListener(this::onInitNoteData);

        // Toolbar
        m_toolbar.setTitle("所有笔记");
        m_toolbar.inflateMenu(R.menu.notefragment_actionbar);
        m_toolbar.setNavigationIcon(R.drawable.tab_menu);
        m_toolbar.setNavigationOnClickListener((View view) -> {
            MainActivity activity = (MainActivity) getActivity();
            if (activity != null) activity.openNavMenu();
        });

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
                back.setVisibility(View.VISIBLE);
            }

            @Override
            public void onMenuCollapsed() {
                back.setVisibility(View.GONE);
            }
        });

        // Right Nav
        m_drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        NavigationView m_navigationView = view.findViewById(R.id.id_noteFrag_Right_nav);
        m_navigationView.setNavigationItemSelectedListener((@NonNull MenuItem item) -> false);
        LayoutUtil.setNavigationViewWidth(getActivity(), m_navigationView, 2.0 / 3);

        ImageButton m_groupMgrBackButton = view.findViewById(R.id.id_NoteFrag_nav_BackButton);
        m_groupMgrBackButton.setOnClickListener(v -> m_drawerLayout.closeDrawer(Gravity.END));

        GroupRadioAdapter groupAdapter = new GroupRadioAdapter(getContext());
        groupAdapter.setOnRadioButtonClickListener(this::GroupRadio_Clicked);
        m_groupListView.setAdapter(groupAdapter);

        // List Adapter
        m_noteListView.addItemDecoration(new SpacesItemDecoration(0));
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        m_noteListView.setLayoutManager(layoutManager);

        NoteAdapter noteAdapter = new NoteAdapter(getContext());
        noteAdapter.setOnItemClickListener(this::NoteItem_Clicked);
        noteAdapter.setOnItemLongClickListener(this::NoteItem_LongClicked);
        m_noteListView.setAdapter(noteAdapter);

        // PageData
        onInitNoteData();
    }

    @Override
    public boolean onBackPressed() {

        // 是否打开 Drawer
        if (m_drawerLayout.isDrawerOpen(Gravity.END)) {
            m_drawerLayout.closeDrawer(Gravity.END);
            return true;
        }

        // 是否展开 Fab
        if (m_fabMenu.isExpanded()) {
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

    /**
     * 工具栏 搜索
     */
    @OnItemSelected(R.id.action_search)
    private void ToolbarSearch_Clicked() {
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null)
            m_searchFragment.show(activity.getSupportFragmentManager(), com.wyt.searchbox.SearchFragment.TAG);
    }

    /**
     * 工具栏 分组
     */
    @OnItemSelected(R.id.action_group)
    private void ToolbarGroup_Clicked() {
        m_drawerLayout.openDrawer(Gravity.END);
    }

    /**
     * 工具栏 返回
     */
    @OnItemSelected(R.id.action_search_group_back)
    private void ToolbarBack_Clicked() {
        toNormal();
    }

    /**
     * Tab 新建笔记
     */
    @OnClick(R.id.note_edit)
    private void NewNoteFab_Clicked() {
        m_fabMenu.collapse();
        newNote();
    }

    /**
     * Tab OCR 拍照
     */
    @OnClick(R.id.note_photo)
    private void OCRFab_Clicked() {
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
        m_notePopupMenu.setOnCancelListener((dialog) -> pageData.longClickedNote = null);

        TextView label = root.findViewById(R.id.id_NoteFrag_PopupMenu_Label);
        label.setText(String.format(Locale.CHINA, "当前选中：%s", note.getTitle()));

        root.findViewById(R.id.id_NoteFrag_PopupMenu_ViewNote).setOnClickListener((view) -> ViewNotePopup_Clicked());
        root.findViewById(R.id.id_NoteFrag_PopupMenu_ChangeGroup).setOnClickListener((view) -> ChangeGroupPopup_Clicked());
        root.findViewById(R.id.id_NoteFrag_PopupMenu_DeleteNote).setOnClickListener((view) -> DeleteNotePopup_Clicked());
        root.findViewById(R.id.id_NoteFrag_PopupMenu_Cancel).setOnClickListener((view) -> m_notePopupMenu.cancel());

        m_notePopupMenu.show();
    }

    /**
     * 弹出菜单 打开笔记
     */
    private void ViewNotePopup_Clicked() {
        m_notePopupMenu.cancel();
        Note note = pageData.longClickedNote;
        if (note == null) return;

        openViewNote(note);
    }

    /**
     * 弹出菜单 修改分组
     */
    private void ChangeGroupPopup_Clicked() {
        m_notePopupMenu.cancel();
        Note note = pageData.longClickedNote;
        if (note == null) return;

        ProgressDialog progressDialog = showProgress(getContext(), "分组信息加载中...", false, null);
        try {
            List<Group> groups = DaoStrategyHelper.getInstance().getGroupDao(getContext()).queryAllGroups();
            progressDialog.dismiss();
            GroupAdapter groupAdapter = new GroupAdapter(getContext());
            groupAdapter.setList(groups);

            showAlert(getContext(), "修改分组", groupAdapter,
                (d, w) -> note.setGroup(groups.get(w)));

            DaoStrategyHelper.getInstance().getNoteDao(getContext()).updateNote(note);
            DaoStrategyHelper.getInstance().getNoteDao(getContext()).updateNote(note);

            NoteAdapter adapter = (NoteAdapter) m_noteListView.getAdapter();
            for (Note n : pageData.allNotes)
                if (n.getId() == note.getId()) {
                    pageData.allNotes.set(pageData.allNotes.indexOf(n), note);
                    return;
                }
            adapter.setNoteList(pageData.allNotes);
            adapter.notifyDataSetChanged();

        } catch (ServerException ex) {
            showAlert(getContext(), "错误", "分组信息加载错误：" + ex.getMessage());
        }

    }

    /**
     * 弹出菜单 删除笔记
     */
    private void DeleteNotePopup_Clicked() {
        m_notePopupMenu.cancel();
        Note note = pageData.longClickedNote;
        if (note == null) return;

        showAlert(getContext(), "删除笔记", "确定删除笔记 \"" + note.getTitle() + "\" 吗？",
            "删除", (d, w) -> deleteNote(note),
            "取消", null);
    }

    // endregion

    // region Right Nav Group

    /**
     * 右边栏 分组管理
     */
    @OnClick(R.id.id_NoteFrag_nav_GroupMgrButton)
    private void GroupMgrButton_Click() {
        if (pageData.pageState == PageState.GROUPING) {
            showAlert(getContext(), "分组管理", "进行分组管理需要退出分组显示，是否继续？",
                "继续", (DialogInterface dialog, int which) -> ShowGroupDialog(),
                "取消", null);
        }
        else
            ShowGroupDialog();
    }

    /**
     * 右边栏 选中列表
     */
    private void GroupRadio_Clicked(int position) {
        GroupRadioAdapter adapter = (GroupRadioAdapter) m_groupListView.getAdapter();
        groupNote(adapter.getList().get(position));
    }


    /**
     * 显示分组管理对话框
     */
    private void ShowGroupDialog() {
        m_drawerLayout.closeDrawer(Gravity.END);
        ProgressDialog progressDialog = showProgress(getContext(), "分组信息加载中...", false, null);

        GroupDialog dialog = new GroupDialog(getActivity(), new GroupDialog.OnUpdateGroupListener() {

            @Override
            public void onLoaded() {
                new Handler().postDelayed(progressDialog::dismiss, 100);
            }

            @Override
            public void onUpdated() {
                onInitNoteData();
            }
        });

        dialog.setCancelable(false);
        dialog.show();
    }

    // endregion

    // region List PageData

    /**
     * 单击列表项
     */
    private void NoteItem_Clicked(View v, Note note) {
        openViewNote(note);
    }

    /**
     * 长按列表项
     */
    private boolean NoteItem_LongClicked(View v, Note note) {
        pageData.longClickedNote = note;
        showPopupMenu(note);
        return true;
    }

    /**
     * 当前页面状态
     */
    private enum PageState {
        NORMAL, SEARCHING, GROUPING
    }

    /**
     * 当前页面数据
     */
    private class PageData {
        List<Note> allNotes = null;
        PageState pageState = PageState.NORMAL;

        Note longClickedNote = null;
    }

    // endregion

    // region !!! new edit

    /*
    1. 新建笔记:
        传递 INT_NOTE_DATA | INT_IS_NEW: true ->
        Edit Act -> 返回 INT_NOTE_DATA | INT_IS_NEW: true
        View Act -> 返回 INT_NOTE_DATA | INT_IS_NEW: true -> 结束

    2. 浏览笔记:
        传递 INT_NOTE_DATA | INT_IS_NEW: true ->
        View Act -> 返回 INT_NOTE_DATA | INT_IS_NEW: false | INT_IS_MODIFIED: ? -> 结束
     */

    private static final int REQ_NEW_NOTE_INTENT = 0;
    private static final int REQ_OPEN_NOTE_INTENT = 1;
    private static final int REQ_OCR_TAKE_PHOTO = 2;
    private static final int REQ_OCR_EDIT_PHOTO = 3;

    public static final String INT_NOTE_DATA = "note_data";
    public static final String INT_IS_NEW = "is_new";
    public static final String INT_IS_MODIFIED = "is_modified";

    /**
     * 活动返回
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_NEW_NOTE_INTENT: // 新笔记返回 -> 浏览
                if (resultCode == RESULT_OK) {
                    Note note = (Note) data.getSerializableExtra(INT_NOTE_DATA);
                    openViewNote(note, true);
                }
                break;
            case REQ_OPEN_NOTE_INTENT: // 笔记浏览返回 -> Toast
                if (resultCode == RESULT_OK) {
                    boolean isNew = data.getBooleanExtra(INT_IS_NEW, true);
                    boolean isModified = data.getBooleanExtra(INT_IS_MODIFIED, true);
                    Note note = (Note) data.getSerializableExtra(INT_NOTE_DATA);

                    if (isNew) {
                        // 新笔记
                        showToast(getContext(), "笔记 \"" + note.getTitle() + "\" 新建成功");
                        NoteAdapter adapter = (NoteAdapter) m_noteListView.getAdapter();
                        List<Note> notes = adapter.getNoteList();
                        notes.add(note);
                        adapter.setNoteList(notes);
                        adapter.notifyDataSetChanged();

                    } else if (isModified) {
                        // 旧的更新过的笔记
                        showToast(getContext(), "笔记 \"" + note.getTitle() + "\" 更新成功");
                        NoteAdapter adapter = (NoteAdapter) m_noteListView.getAdapter();
                        List<Note> notes = adapter.getNoteList();

                        for (Note n : notes)
                            if (n.getId() == note.getId()) {
                                notes.set(notes.indexOf(n), note);
                                return;
                            }

                        adapter.setNoteList(notes);
                        adapter.notifyDataSetChanged();
                    }
                }
                break;
            case REQ_OCR_TAKE_PHOTO: // OCR 拍照 (OCRTakePhoto)
                // https://zhidao.baidu.com/question/1638421275428158220.html
                if (resultCode == RESULT_OK)
                    OCREditPhoto(data.getData()); // photoUri
                break;
            case REQ_OCR_EDIT_PHOTO: // OCR 剪辑 (OCREditPhoto)
                if (resultCode == RESULT_OK)
                    OpenOCRActivity(data.getData()); // editedUri
                break;
        }
    }

    /**
     * 新建笔记
     */
    private void newNote() {
        Note note = new Note();
        try {
            Group defGroup = DaoStrategyHelper.getInstance().getGroupDao(getContext()).queryDefaultGroup();
            note.setGroup(defGroup);
        } catch (ServerException ex) {
            showAlert(getContext(), "错误", "获取默认分组错误：" + ex.getMessage());
            return;
        }

        Intent intent = new Intent(getActivity(), EditNoteActivity.class);
        intent.putExtra(INT_NOTE_DATA, note);
        intent.putExtra(INT_IS_NEW, true); // NEW

        startActivityForResult(intent, REQ_NEW_NOTE_INTENT);
    }

    /**
     * 查看笔记
     * @param isNew 是否为新笔记 (newNote)
     */
    private void openViewNote(@NonNull Note note, boolean isNew) {
        Intent intent = new Intent(getActivity(), ViewNoteActivity.class);
        intent.putExtra(INT_NOTE_DATA, note);
        intent.putExtra(INT_IS_NEW, isNew);
        startActivityForResult(intent, REQ_OPEN_NOTE_INTENT);
    }

    /**
     * 查看已经存在的笔记
     */
    private void openViewNote(@NonNull Note note) {
        openViewNote(note, false);
    }

    // endregion

    // region PageData initData setContent delete

    /**
     * 当前页面 状态与数据
     */
    private PageData pageData;

    /**
     * 初始化与刷新 加载数据
     */
    private void onInitNoteData() {
        pageData = new PageData();
        ProgressDialog progressDialog = showProgress(getContext(), "加载数据中...", false, null);
        try {
            pageData.allNotes = DaoStrategyHelper.getInstance().getNoteDao(getContext()).queryAllNotes();
            progressDialog.dismiss();
        } catch (ServerException ex) {
            progressDialog.dismiss();
            showAlert(getContext(), "错误", "数据加载错误：" + ex.getMessage(),
                "重试", (d, w) -> onInitNoteData(), "确定", null);
        }
    }

    /**
     * 更新页面显示为 noteList
     */
    private void setListContent(List<Note> noteList) {
        NoteAdapter adapter = (NoteAdapter) m_noteListView.getAdapter();
        if (adapter == null) return;

        adapter.setNoteList(noteList);
        adapter.notifyDataSetChanged();
    }

    /**
     * 删除笔记，更新页面
     */
    private void deleteNote(@NonNull Note note) {
        try {
            DaoStrategyHelper.getInstance().getNoteDao(getContext()).deleteNote(note.getId());
            NoteAdapter adapter = (NoteAdapter) m_noteListView.getAdapter();
            adapter.getNoteList().remove(note);
            adapter.notifyDataSetChanged();
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
        pageData.pageState = PageState.GROUPING;

        // 页面显示处理
        m_toolbar.getMenu().findItem(R.id.action_search_group_back).setVisible(true); // 显示返回
        m_toolbar.getMenu().findItem(R.id.action_search).setVisible(true); // 显示搜索
        m_toolbar.getMenu().findItem(R.id.action_group).setVisible(false); // 隐藏分组

        m_swipeRefresh.setEnabled(false); // 禁止刷新
        m_fabMenu.setVisibility(View.GONE); // 隐藏 Fab
        m_drawerLayout.setEnabled(false); // 禁止分组

        m_toolbar.setTitle("\"" + group.getName() + "\" 的分组结果");

        // 数据处理
        try {
            List<Note> notes = DaoStrategyHelper.getInstance().getNoteDao(getContext()).queryNotesByGroupId(group.getId());
            setListContent(notes);
        } catch (ServerException ex) {
            showAlert(getContext(), "错误", "数据加载错误：" + ex.getMessage());
        }
    }

    /**
     * 搜索显示
     */
    private void searchNote(String searchingStr) {
        pageData.pageState = PageState.SEARCHING;

        // 页面显示处理
        m_toolbar.getMenu().findItem(R.id.action_search_group_back).setVisible(true); // 显示返回
        m_toolbar.getMenu().findItem(R.id.action_search).setVisible(false); // 隐藏搜索
        m_toolbar.getMenu().findItem(R.id.action_group).setVisible(true); // 允许分组

        m_drawerLayout.setEnabled(true); // 允许分组
        m_swipeRefresh.setEnabled(false); // 禁止刷新
        m_fabMenu.setVisibility(View.GONE); // 隐藏 Fab

        m_toolbar.setTitle("\"" + searchingStr + "\" 的搜索结果");

        // 数据处理
        List<Note> searchResult =
            SearchUtil.getSearchItems(pageData.allNotes.toArray(new Note[0]), searchingStr);
        setListContent(searchResult);
    }

    /**
     * 正常状态，非搜索非分组
     */
    private void toNormal() {
        pageData.pageState = PageState.NORMAL;

        // 页面显示处理
        m_toolbar.getMenu().findItem(R.id.action_search_group_back).setVisible(false); // 隐藏返回
        m_toolbar.getMenu().findItem(R.id.action_search).setVisible(true); // 允许搜索
        m_toolbar.getMenu().findItem(R.id.action_group).setVisible(true); // 允许分组

        m_swipeRefresh.setEnabled(true); // 允许刷新
        m_fabMenu.setVisibility(View.VISIBLE); // 显示 Fab
        m_drawerLayout.setEnabled(true); // 允许分组
        m_toolbar.setTitle("所有笔记");

        // 数据处理
        setListContent(pageData.allNotes);
    }

    // endregion

    // region OCR

    /*
        拍照 (生成 photoUri) ->
        编辑 (生成 editedUri) ->
        识别 (删除 photoUri) -> 保留 editedUri
     */

    /**
     * 文字识别之前 拍照
     */
    private void OCRTakePhoto() {

        String photoPath = SaveNameUtil.getImageFileName(SaveNameUtil.SaveType.PHOTO);

        // 7.0 调用系统相机拍照不再允许使用 Uri 方式，应该替换为 FileProvider
        Uri photoUri = AppPathUtil.getUriByPath(getContext(), photoPath);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);

        try {
            startActivityForResult(intent, REQ_OCR_TAKE_PHOTO);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            showAlert(getContext(), "错误", "打开设备摄像机错误，请检查。");
        }
    }

    /**
     * 文字识别之前 编辑
     */
    private void OCREditPhoto(Uri photoUri) {
        String imgPath = AppPathUtil.getFilePathByUri(getContext(), photoUri);
        if (imgPath == null || imgPath.isEmpty())
            showAlert(getContext(), "错误", "从相册获取的图片或拍照得到的图片不存在，请重试。");
        else {
            String editedPath = SaveNameUtil.getImageFileName(SaveNameUtil.SaveType.EDITED);

            Intent intent = new Intent(getActivity(), IMGEditActivity.class);
            intent.putExtra(IMGEditActivity.INT_IMAGE_URI, photoUri);
            intent.putExtra(IMGEditActivity.INT_IMAGE_SAVE_URI, editedPath);

            startActivityForResult(intent, REQ_OCR_EDIT_PHOTO);
        }
    }

    /**
     * 拍照 -> 编辑 -> 识别
     */
    private void OpenOCRActivity(@Nullable Uri editedUri) {
        // TODO delete photoUri

        if (editedUri == null) {
            showAlert(getContext(), "错误", "编辑得到的图片不存在，请重试。");
            return;
        }

        Intent intent = new Intent(getContext(), OCRActivity.class);
        intent.putExtra(OCRActivity.INT_IMAGE_PATH, editedUri.toString());
        startActivity(intent);
    }

    // endregion
}
