package com.baibuti.biji.Fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.baibuti.biji.Activity.MainActivity;
import com.baibuti.biji.Activity.ModifyNoteActivity;
import com.baibuti.biji.Activity.ViewModifyNoteActivity;
import com.baibuti.biji.Data.Group;
import com.baibuti.biji.Data.GroupAdapter;
import com.baibuti.biji.Data.Note;
import com.baibuti.biji.Data.NoteAdapter;
import com.baibuti.biji.R;
import com.baibuti.biji.RainbowPalette;
import com.baibuti.biji.View.SpacesItemDecoration;
import com.baibuti.biji.db.GroupDao;
import com.baibuti.biji.db.NoteDao;
import com.baibuti.biji.util.CommonUtil;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import java.util.Collections;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class NoteFragment extends Fragment implements View.OnClickListener {

    private RecyclerView mNoteList;

    private List<Note> NoteList;
    private List<Group> GroupList;

    private com.wyt.searchbox.SearchFragment searchFragment;
//    private SwipeRefreshLayout mSwipeRefresh;
    private SlidingMenu slidingMenu;

    private static final int NOTE_NEW = 0; // new
    private static final int NOTE_UPDATE = 1; // modify

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.notetab, container, false);
        setHasOptionsMenu(true);
        slidingMenu = ((MainActivity)getActivity()).getSlidingMenu();
        mNoteList = view.findViewById(R.id.note_list);
        //添加搜索框
        searchFragment = com.wyt.searchbox.SearchFragment.newInstance();
        searchFragment.setAllowReturnTransitionOverlap(true);
        searchFragment.setOnSearchClickListener(new com.wyt.searchbox.custom.IOnSearchClickListener() {
            @Override
            public void OnSearchClick(String keyword) {
                Toast.makeText(getContext(),"This is note_search", Toast.LENGTH_SHORT).show();
                //添加逻辑处理
            }
        });

//        mSwipeRefresh.setColorSchemeResources(R.color.colorPrimary);
//        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                refreshdata();
//            }
//        });

        initToolbar(view);
        initFloatingActionBar(view);
        initData(); // GetDao & List
        initAdapter();
        initListView();
        return view;
    }

    @Override
    public void onClick(View v) {

    }

    private void initToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.note_toolbar);
        toolbar.inflateMenu(R.menu.notefragment_actionbar);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_search:
                        searchFragment.show(getActivity().getSupportFragmentManager(),com.wyt.searchbox.SearchFragment.TAG);
                        break;
                    case R.id.action_modifygroup:
                        showModifyGroup();
                        break;
                }
                return true;
            }
        });
        toolbar.setNavigationIcon(R.mipmap.ic_launcher);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(slidingMenu != null)
                    slidingMenu.showMenu();
            }
        });
        toolbar.setTitle(R.string.note_header);

        //SimplerSearcherView seacherView = view.findViewById(R.id.note_searcher);
        //seacherView.setOnSearcherClickListener(((MainActivity)getActivity()));
    }

    private void initFloatingActionBar(View view){
        FloatingActionButton mNotePhoto = view.findViewById(R.id.note_photo);
        FloatingActionButton mNoteEdit = view.findViewById(R.id.note_edit);
        mNotePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "This is note_photo",Toast.LENGTH_LONG).show();
                //添加逻辑处理
            }
        });
        mNoteEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addDoc_intent=new Intent(getActivity(),ModifyNoteActivity.class);
                addDoc_intent.putExtra("notedata",new Note("",""));
                addDoc_intent.putExtra("flag",NOTE_NEW); // NEW
                startActivityForResult(addDoc_intent,2); // 2 from FloatingButton

            }
        });
    }

    private NoteAdapter noteAdapter;
    private GroupAdapter groupAdapter;

    private NoteDao noteDao;
    private GroupDao groupDao;

    public void initData() {
        if (noteDao == null) {
            noteDao = new NoteDao(this.getContext());
            groupDao = new GroupDao(this.getContext());
        }

        NoteList = noteDao.queryNotesAll();
        GroupList = groupDao.queryGroupAll();
    }

    public void initAdapter() {
        noteAdapter = new NoteAdapter();
        groupAdapter = new GroupAdapter(getContext(), GroupList);
    }

//    private void refreshdata() {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    Thread.sleep(500);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                getActivity().runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        initDatas();
//                        noteAdapter.notifyDataSetChanged();
//                        mSwipeRefresh.setRefreshing(false);
//                    }
//                });
//            }
//        }).start();
//    }

    public void refreshNoteList() {
        // initData();
        NoteList = noteDao.queryNotesAll();
        Collections.sort(NoteList);
        noteAdapter.notifyDataSetChanged();
    }

    public void refreshGroupList() {
        GroupList = groupDao.queryGroupAll();
        groupAdapter = new GroupAdapter(getContext(), GroupList); // 必要
        groupAdapter.notifyDataSetChanged();
    }


    private int SelectedNoteItem;

    private void initListView() {

        mNoteList.addItemDecoration(new SpacesItemDecoration(0));//设置item间距
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);//竖向列表
        mNoteList.setLayoutManager(layoutManager);

        Collections.sort(NoteList);
        noteAdapter.setmNotes(NoteList);

        mNoteList.setAdapter(noteAdapter);

        noteAdapter.setOnItemClickListener(new NoteAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                SelectedNoteItem = position;

                Intent intent=new Intent(getContext(), ViewModifyNoteActivity.class);
                intent.putExtra("notedata",NoteList.get(position));
                intent.putExtra("flag",NOTE_UPDATE); // UPDATE
                startActivityForResult(intent,1); // 1 from List
            }
        });

        noteAdapter.setOnItemLongClickListener(new NoteAdapter.OnRecyclerViewItemLongClickListener() {
            @Override
            public void onItemLongClick(final View view, final Note note) {

//                final Note notetmp = note;

                AlertDialog deleteAlert = new AlertDialog
                    .Builder(getContext())
                    .setTitle("提示")
                    .setMessage("确定删除笔记 \"" + note.getTitle() + "\" 吗？")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            int ret = noteDao.deleteNote(note.getId());

                            if (ret > 0) {
                                NoteList.remove(note);
                                noteAdapter.notifyDataSetChanged();

                                Snackbar.make(view ,"删除成功", Snackbar.LENGTH_LONG)
                                    .setAction("撤销", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            try {
                                                noteDao.insertNote(note);
                                                NoteList.add(note);
                                                Collections.sort(NoteList);
                                                noteAdapter.notifyDataSetChanged();
                                            } catch (Exception ex) {
                                                ex.printStackTrace();
                                            }
                                            Snackbar.make(view, "已恢复", Snackbar.LENGTH_SHORT).show();
                                        }
                                    }).show();
                            }
                        }
                    })
                    .setNegativeButton("取消", null)
                    .create();

                deleteAlert.show();
            }
        });
    }

    //////////////////////////////////////////////////

    public AlertDialog GroupDialog;
    public AlertDialog.Builder addGroupNamedialog;

    public void showModifyGroup() {
        refreshGroupList();

        GroupDialog = new AlertDialog
                .Builder(getContext())
                .setTitle("笔记分类")//设置对话框的标题
                .setNeutralButton("添加", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ShowAddGroupDialog(null);
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setSingleChoiceItems(groupAdapter, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ShowAddGroupDialog(GroupList.get(which));
                        dialog.dismiss();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        refreshGroupList();
                    }
                }).create();

        GroupDialog.show();
    }

    public void ShowAddGroupDialog(final Group group) {
        View view = getLayoutInflater().inflate(R.layout.modifygroup_addgroup_dialog, null);

        final EditText editText = (EditText) view.findViewById(R.id.id_addgroup_name);
        final TextView colorText = (TextView) view.findViewById(R.id.id_addgroup_colortext);
        final RainbowPalette colorPalette = (RainbowPalette) view.findViewById(R.id.id_addgroup_colorpalettle);

        if (group != null) {
            editText.setText(group.getName());
            colorText.setText("笔记代表颜色："+ group.getColor());
            colorPalette.setColor(CommonUtil.ColorHex_IntEncoding(group.getColor()));
        } else {
            colorText.setText("笔记代表颜色：#FFFFFF");
            colorPalette.setColor(CommonUtil.ColorHex_IntEncoding("#FFFFFF"));
        }

        colorPalette.setOnChangeListen(new RainbowPalette.OnColorChangedListen() {
            @Override
            public void onColorChange(int color) {
            colorText.setText("笔记代表颜色："+ CommonUtil.ColorInt_HexEncoding(color));
            }
        });

        addGroupNamedialog = new AlertDialog
                .Builder(getContext())
                .setTitle("添加笔记类型标签")
                .setView(view)
                .setCancelable(false)
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String newGroupName = editText.getText().toString();
                        int newGroupOrder = 0;
                        String newGroupColor = CommonUtil.ColorInt_HexEncoding(colorPalette.getColor());
                        Log.e("COLOR", newGroupColor);


                        if (newGroupName.isEmpty()) {
                            AlertDialog emptyDialog = new AlertDialog
                                .Builder(getContext())
                                .setTitle("没有输入类型，请补全内容")
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                })
                                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialog) {
                                        ShowAddGroupDialog(group);
                                    }
                                }).create();
                            dialog.dismiss();
                            emptyDialog.show();
                        }
                        else {
                            if (group == null) {
                                Group newGroup = new Group(newGroupName, newGroupOrder, newGroupColor);
                                try {
                                    groupDao.insertGroup(newGroup);
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                            else {
                                group.setName(newGroupName);
                                group.setColor(newGroupColor);
                                group.setOrder(newGroupOrder);
                                try {
                                    groupDao.updateGroup(group);
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                            /// DB Finish
                            dialog.cancel();
                        }
                    }
                });

        if (group != null) {
            addGroupNamedialog.setNeutralButton("删除", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    AlertDialog deleteDialog = new AlertDialog
                        .Builder(getContext())
                        .setTitle("提示")
                        .setMessage("确定要删除类型 " + group.getName() + " 吗")
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ShowAddGroupDialog(group);
                                dialog.dismiss();
                            }
                        })
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    groupDao.deleteGroup(group.getId());
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                                dialog.cancel();
                            }
                        })
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                refreshGroupList();
                                showModifyGroup();
                            }
                        }).create();

                    dialog.dismiss();
                    deleteDialog.show();
                }
            });
        }

        addGroupNamedialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                refreshGroupList();
                showModifyGroup();
            }
        });

        addGroupNamedialog.show();

    }

    //////////////////////////////////////////////////

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1: // FROM LIST
                if (resultCode == RESULT_OK) {
                    if (data.getBooleanExtra("intent_result", true) == true) {
                        Note newnote = (Note) data.getSerializableExtra("modify_note");
                        NoteList.set(SelectedNoteItem, newnote);
                        Collections.sort(NoteList);
                        noteAdapter.notifyDataSetChanged();
                    }
                    break;
                }
            case 2: // ADD
                if (resultCode == RESULT_OK) {
                    if (data.getBooleanExtra("intent_result", true) == true) {
                        Note newnote = (Note) data.getSerializableExtra("modify_note");
                        Toast.makeText(getActivity(), newnote.getTitle(), Toast.LENGTH_SHORT).show();
                        NoteList.add(NoteList.size(), newnote);
                        Collections.sort(NoteList);
                        noteAdapter.notifyDataSetChanged();
                    }
                }
                break;
        }
    }

}
