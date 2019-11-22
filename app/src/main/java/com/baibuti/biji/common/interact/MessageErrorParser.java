package com.baibuti.biji.common.interact;

import com.baibuti.biji.model.dto.ServerException;
import com.baibuti.biji.model.vo.MessageVO;

public class MessageErrorParser {

    public static final int SUCCESS = 200;
    public static final int UNAUTHORIZED = 401;
    public static final int NOT_FOUND = 404;
    public static final int INTERNAL_SERVER_ERROR = 500;

    public static final int DATABASE_FAILED = 600;
    public static final int HAS_EXISTED = 601;
    public static final int DUPLICATE_FAILED = 602;
    public static final int DEFAULT_FAILED = 603;
    public static final int SAVE_FILE_FAILED = 604;

    public static final int BAD_REQUEST = 400;
    public static final int FORBIDDEN = 403;
    public static final int METHOD_NOT_ALLOWED = 405;
    public static final int NOT_ACCEPTABLE = 406;

    /**
     * 客户端获取请求的错误
     *      InterruptedException
     *      ExecutionException
     */
    public static ServerException getClientError(Throwable throwable) {
        if (throwable instanceof ServerException)
            return (ServerException) throwable;

        return new ServerException(throwable.getMessage());
    }

    /**
     * 服务器端的错误
     */
    static String fromMessageVO(MessageVO messageVO) {

        switch (messageVO.getMessage()) {
            // Global
            case "Request Param Error":
            case "Request Query Param Error":
            case "Request Route Param Error":
            case "Request From Data Param Error":
            case "Request Raw Json Error":
                return messageVO.getMessage();
            case "Format Error":
                return "格式错误";
            case "Token Expired":
                return "登录过期";
            case "Token Bad Signature":
                return "无效的登录";
            ////////////////////////////////////////////////////
            // Auth
            case "Password Error":
                return "密码错误";
            case "User Not Found":
                return "用户不存在";
            case "Login Failed":
                return "登录失败";
            case "Register Failed":
                return "注册失败";
            case "User Existed":
                return "用户已存在";
            case "Logout Failed":
                return "注销失败";
            ////////////////////////////////////////////////////
            // Note
            case "Note Not Found":
                return "笔记不存在";
            case "Note Existed":
                return "笔记已存在";
            case "Note Insert Failed":
                return "新建笔记未知错误";
            case "Note Update Failed":
                return "更新笔记未知错误";
            case "Note Delete Failed":
                return "删除笔记未知错误";
            case "Group Not Found":
                return "笔记分组不存在";
            case "Group Existed":
                return "笔记分组已存在";
            case "Group Insert Failed":
                return "新建笔记分组未知错误";
            case "Group Update Failed":
                return "更新笔记分组未知错误";
            case "Group Delete Failed":
                return "删除笔记分组未知错误";
            case "Group Name Duplicate":
                return "笔记分组名重复";
            case "Could Not Update Default Group":
                return "无法更新默认分组名";
            case "Could Not Delete Default Group":
                return "无法删除默认分组";
            ////////////////////////////////////////////////////
            // Star
            case "StarItem Not Found":
                return "收藏项不存在";
            case "StarItem Existed":
                return "收藏想已存在";
            case "StarItem Insert Failed":
                return "新建收藏项错误";
            case "StarItem Delete Failed":
                return "删除收藏项错误";
            ////////////////////////////////////////////////////
            // Schedule
            case "Schedule Not Found":
                return "课程表不存在";
            case "Update Schedule Failed":
                return "更新课程表未知错误";
            case "Delete Schedule Failed":
                return "删除课程表未知错误";
            ////////////////////////////////////////////////////
            // Document
            case "Document Not Found":
                return "文档不存在";
            case "Document Existed":
                return "文档已存在";
            case "Document Insert Failed":
                return "新建文档未知错误";
            case "Document Update Failed":
                return "更新文档未知错误";
            case "Document Delete Failed":
                return "删除文档未知错误";
            case "Document Class Not Found":
                return "文档分组不存在";
            case "Document Class Existed":
                return "文档分组已存在";
            case "Document Class Insert Failed":
                return "新建文档分组未知错误";
            case "Document Class Update Failed":
                return "更新文档分组未知错误";
            case "Document Class Delete Failed":
                return "删除文档分组未知错误";
            case "Document Class Name Duplicate":
                return "文档分组名重复";
            case "Could Not Update Default Document Class":
                return "无法更新默认文档分组名";
            case "Could Not Delete Default Document Class":
                return "无法删除默认分组";
            case "File Extension Error":
                return "上传的文件后缀名不支持，仅支持 " +
                    "[txt, md, pdf, doc, docx, ppt, pptx, xls, xlsx, zip, rar, jpg, png, jpeg, bmp]";
            case "Save Document Failed":
                return "保存文档失败";
            ////////////////////////////////////////////////////
            // Raw
            case "Image Not Found":
                return "图片不存在";
            case "Not Support Upload Type":
                return "不受支持的上传类型";
            case "Save Image Failed":
                return "图片上传失败";
        }

        return "未知错误";
    }
}
