package com.baibuti.biji.Data.Models;

public enum LogModule {
    Mod_Note, Mod_Group, Mod_Star, Mod_File, Mod_Schedule;

    @Override
    public String toString() {
        switch (this) {
            case Mod_Note:
                return UtLog.Log_Note;
            case Mod_Group:
                return UtLog.Log_Group;
            case Mod_Star:
                return UtLog.Log_Star;
            case Mod_File:
                return UtLog.Log_File;
            case Mod_Schedule:
                return UtLog.Log_Schedule;
        }
        return "";
    }
}
