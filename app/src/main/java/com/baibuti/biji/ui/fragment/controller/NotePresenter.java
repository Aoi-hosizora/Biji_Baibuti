package com.baibuti.biji.ui.fragment.controller;

import com.baibuti.biji.ui.fragment.contract.INoteContract;

public class NotePresenter implements INoteContract.Presenter {

    private final INoteContract.View m_View;

    public NotePresenter(INoteContract.View noteView) {
        m_View = noteView;
        m_View.setPresenter(this);
    }

    @Override
    public void onStart() {

    }

    @Override
    public void handleData() {

    }
}
