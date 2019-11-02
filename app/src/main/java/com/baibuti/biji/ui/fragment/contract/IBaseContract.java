package com.baibuti.biji.ui.fragment.contract;

public interface IBaseContract {

    interface View <T> {
        void setPresenter(T presenter);
    }

    interface Presenter {
        void onStart();
    }
}
