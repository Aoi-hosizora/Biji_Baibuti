package com.baibuti.biji.ui.fragment.contract;

public interface INoteContract {

    interface View extends IBaseContract.View<Presenter> {
        void showAlert();
    }

    interface Presenter extends IBaseContract.Presenter {
        void handleData();
    }
}
