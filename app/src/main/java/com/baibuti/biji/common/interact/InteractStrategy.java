package com.baibuti.biji.common.interact;

import android.content.Context;

import com.baibuti.biji.common.interact.client.DocClassInteract;
import com.baibuti.biji.common.interact.client.DocumentInteract;
import com.baibuti.biji.common.interact.client.GroupInteract;
import com.baibuti.biji.common.interact.client.NoteInteract;
import com.baibuti.biji.common.interact.client.ScheduleInteract;
import com.baibuti.biji.common.interact.client.SearchItemInteract;
import com.baibuti.biji.common.interact.contract.*;
import com.baibuti.biji.common.interact.server.DocumentNetInteract;
import com.baibuti.biji.common.interact.server.DocClassNetInteract;
import com.baibuti.biji.common.interact.server.GroupNetInteract;
import com.baibuti.biji.common.interact.server.NoteNetInteract;
import com.baibuti.biji.common.interact.server.ScheduleNetInteract;
import com.baibuti.biji.common.interact.server.SearchItemNetInteract;
import com.baibuti.biji.common.auth.AuthManager;

public class InteractStrategy {

    private InteractStrategy() { }

    private static InteractStrategy Instance;

    public static InteractStrategy getInstance() {
        if (Instance == null) {
            Instance = new InteractStrategy();
        }
        return Instance;
    }

    public INoteInteract getNoteInteract(Context context) {
        if (!AuthManager.getInstance().isLogin())
            return new NoteInteract(context);
        else
            return new NoteNetInteract();
    }

    public IGroupInteract getGroupInteract(Context context) {
        if (!AuthManager.getInstance().isLogin())
            return new GroupInteract(context);
        else
            return new GroupNetInteract();
    }

    public ISearchItemInteract getSearchInteract(Context context) {
        if (!AuthManager.getInstance().isLogin())
            return new SearchItemInteract(context);
        else
            return new SearchItemNetInteract();
    }

    public IScheduleInteract getScheduleInteract(Context context) {
        if (!AuthManager.getInstance().isLogin())
            return new ScheduleInteract(context);
        else
            return new ScheduleNetInteract();
    }

    public IDocumentInteract getDocumentInteract(Context context) {
        if (!AuthManager.getInstance().isLogin())
            return new DocumentInteract(context);
        else
            return new DocumentNetInteract();
    }

    public IDocClassInteract getDocClassInteract(Context context) {
        if (!AuthManager.getInstance().isLogin())
            return new DocClassInteract(context);
        else
            return new DocClassNetInteract();
    }
}
