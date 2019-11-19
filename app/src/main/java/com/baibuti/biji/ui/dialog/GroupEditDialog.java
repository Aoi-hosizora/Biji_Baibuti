package com.baibuti.biji.ui.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.baibuti.biji.model.dao.DaoStrategyHelper;
import com.baibuti.biji.model.dao.DbStatusType;
import com.baibuti.biji.model.dao.daoInterface.IGroupDao;
import com.baibuti.biji.model.dao.daoInterface.INoteDao;
import com.baibuti.biji.model.dto.ServerException;
import com.baibuti.biji.model.po.Group;
import com.baibuti.biji.R;
import com.baibuti.biji.model.po.Note;
import com.baibuti.biji.ui.IContextHelper;
import com.baibuti.biji.util.otherUtil.DateColorUtil;
import com.larswerkman.holocolorpicker.ColorPicker;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class GroupEditDialog extends AlertDialog implements IContextHelper {

    private Activity activity;

    @BindView(R.id.id_txt_label)
    TextView m_txt_label;

    @BindView(R.id.id_txt_color)
    TextView m_txt_color;

    @BindView(R.id.id_edt_title)
    EditText m_edt_title;

    @BindView(R.id.id_ColorPicker_Picker)
    ColorPicker m_color_picker;

    @BindView(R.id.id_btn_delete)
    Button m_btn_delete;

    /**
     * 输入的分组，用于判断新建还是更新
     */
    private Group currGroup;

    private OnUpdateGroupListener m_listener;

    public interface OnUpdateGroupListener {

        /**
         * 分组数据修改
         */
        void onUpdated();
    }

    GroupEditDialog(Activity activity, Group currGroup, OnUpdateGroupListener listener) {
        super(activity);
        this.activity = activity;
        this.m_listener = listener;
        this.currGroup = currGroup;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_group_add);
        ButterKnife.bind(this);

        m_color_picker.addSaturationBar(findViewById(R.id.id_ColorPicker_SaturationBar));
        m_color_picker.addSVBar(findViewById(R.id.id_ColorPicker_SVBar));

        m_color_picker.setOnColorChangedListener((int color) ->
            m_txt_color.setText(String.format(Locale.CHINA, "分组颜色：%s", DateColorUtil.ColorInt_HexEncoding(color)))
        );
        m_color_picker.setOnColorSelectedListener((int color) ->
            m_txt_color.setText(String.format(Locale.CHINA, "分组颜色：%s", DateColorUtil.ColorInt_HexEncoding(color)))
        );

        // 处理问题
        Group displayGroup = currGroup;
        if (displayGroup == null) {
            displayGroup = new Group();
            displayGroup.setName("");
        }

        // 界面更新
        m_edt_title.setText(displayGroup.getName());
        m_txt_color.setText(String.format(Locale.CHINA, "分组颜色：%s", displayGroup.getStringColor()));

        m_color_picker.setOldCenterColor(displayGroup.getIntColor());
        m_color_picker.setColor(displayGroup.getIntColor());

        if (currGroup == null) {
            m_txt_label.setText("添加分组");
            // 根据是否为新建分组而显示删除按钮
            m_btn_delete.setVisibility(View.GONE);
        }
        else {
            m_txt_label.setText("修改分组");
            m_btn_delete.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (m_listener != null)
            m_listener.onUpdated();
    }

    /**
     * 修改分组信息提交
     */
    @OnClick(R.id.id_btn_ok)
    void UpdateGroup_Clicked() {

        String newGroupName = m_edt_title.getText().toString();
        if (newGroupName.isEmpty()) {
            showAlert(activity, "错误", "没有输入分组名，请补全内容。");
            return;
        }

        String newGroupColor = DateColorUtil.ColorInt_HexEncoding(m_color_picker.getColor());

        IGroupDao groupDao = DaoStrategyHelper.getInstance().getGroupDao(activity);
        try {
            if (currGroup == null) { // 新建
                DbStatusType status = groupDao.insertGroup(new Group(newGroupName, newGroupColor));
                if (status == DbStatusType.FAILED) {
                    showAlert(activity, "错误", "新建分组错误。");
                    return;
                } else if (status == DbStatusType.DUPLICATED) {
                    showAlert(activity, "错误", "分组名 \"" + newGroupName + "\" 重复，请检查。");
                    return;
                }
            } else { // 更新
                DbStatusType status = groupDao.updateGroup(new Group(currGroup.getId(), newGroupName, currGroup.getOrder(), newGroupColor));
                if (status == DbStatusType.FAILED) {
                    showAlert(activity, "错误", "新建分组错误。");
                    return;
                } else if (status == DbStatusType.DUPLICATED) {
                    showAlert(activity, "错误", "分组名 \"" + newGroupName + "\" 重复，请检查。");
                    return;
                } else if (status == DbStatusType.DEFAULT) {
                    showAlert(activity, "错误", "不允许修改默认分组名。");
                    return;
                }
            }
        } catch (ServerException ex) {
            ex.printStackTrace();
            showAlert(activity, "错误", ex.getMessage());
        }
        dismiss();
    }

    /**
     * 弹出删除判断
     */
    @OnClick(R.id.id_btn_delete)
    void DeleteButton_Clicked() {
        try {
            INoteDao noteDao = DaoStrategyHelper.getInstance().getNoteDao(activity);
            IGroupDao groupDao = DaoStrategyHelper.getInstance().getGroupDao(activity);
            if (currGroup.getId() == groupDao.queryDefaultGroup().getId())
                showAlert(activity, "错误", "无法删除默认分组。");

            List<Note> groupNotes = noteDao.queryNotesByGroupId(currGroup.getId());
            if (groupNotes.isEmpty()) {
                // 不包含笔记
                showAlert(activity,
                    "删除", String.format("是否删除分组 %s？", currGroup.getName()),
                    "删除", (d, w) -> {
                        try {
                            if (groupDao.deleteGroup(currGroup.getId(), false) == DbStatusType.FAILED)
                                showAlert(activity, "错误", "分组删除错误。");
                        } catch (ServerException ex) {
                            ex.printStackTrace();
                            showAlert(activity, "错误", ex.getMessage());
                        }
                    },
                    "返回", null
                );
            } else {

                // 包含笔记
                showAlert(activity,
                    "删除", "该分组有相关联的笔记，是否同时删除？",
                    "删除分组及笔记", (d, w) -> {
                        try {
                            for (Note note : groupNotes)
                                noteDao.deleteNote(note.getId());
                            if (groupDao.deleteGroup(currGroup.getId(), false) == DbStatusType.FAILED)
                                showAlert(activity, "错误", "分组删除错误，已恢复修改。");
                        } catch (ServerException ex) {
                            ex.printStackTrace();
                            showAlert(activity, "错误", ex.getMessage());
                        }
                    },
                    "删除分组并修改为默认分组", (d, w) -> {
                        try {
                            if (groupDao.deleteGroup(currGroup.getId(), true) == DbStatusType.FAILED)
                                showAlert(activity, "错误", "分组删除错误，已恢复修改。");
                        } catch (ServerException ex) {
                            ex.printStackTrace();
                            showAlert(activity, "错误", ex.getMessage());
                        }
                    },
                    "不删除分组", null
                );
            }
        }
        catch (ServerException ex) {
            ex.printStackTrace();
            showAlert(activity, "错误", ex.getMessage());
        }
    }

    /**
     * 退出对话框
     */
    @OnClick(R.id.id_btn_cancel)
    void CancelButton_Clicked() {
        dismiss();
    }
}