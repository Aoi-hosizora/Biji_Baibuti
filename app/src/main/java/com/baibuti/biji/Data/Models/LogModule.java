package com.baibuti.biji.Data.Models;

public enum LogModule {
    Mod_Note, Mod_Group, Mod_Star, Mod_FileClass, Mod_Document, Mod_Schedule;

    @Override
    public String toString() {
        switch (this) {
            case Mod_Note:
                return UtLog.Log_Note;
            case Mod_Group:
                return UtLog.Log_Group;
            case Mod_Star:
                return UtLog.Log_Star;
            case Mod_FileClass:
                return UtLog.Log_FileClass;
            case Mod_Document:
                return UtLog.Log_Document;
            case Mod_Schedule:
                return UtLog.Log_Schedule;
        }
        return "";
    }
}
