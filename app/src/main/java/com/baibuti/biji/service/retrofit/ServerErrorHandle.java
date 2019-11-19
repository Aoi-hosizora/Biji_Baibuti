package com.baibuti.biji.service.retrofit;

import com.baibuti.biji.model.dto.ResponseDTO;
import com.baibuti.biji.model.dto.ServerException;

public class ServerErrorHandle {

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
    public static ServerException parseErrorMessage(ResponseDTO responseDTO) {
        String message = "未知错误";

        switch (responseDTO.getMessage()) {
            // Global
            case "Request Query Param Error":
            case "Request Route Param Error":
            case "Request From Data Param Error":
            case "Request Raw Json Error":
                message = responseDTO.getMessage();
                break;
            case "Format Error":
                message = "格式错误";
                break;
            case "Token Expired":
                message = "登录过期";
                break;
            case "Token Bad Signature":
                message = "无效的登录";
                break;
            ////////////////////////////////////////////////////
            // Auth
            case "Password Error":
                message = "密码错误";
                break;
            case "User Not Found":
                message = "用户不存在";
                break;
            case "Login Failed":
                message = "登录失败";
                break;
            case "Register Failed":
                message = "注册失败";
                break;
            case "User Existed":
                message = "用户已存在";
                break;
            case "Logout Failed":
                message = "注销失败";
                break;
            ////////////////////////////////////////////////////
            // Note
            case "Note Not Found":
                message = "笔记不存在";
                break;
            case "Note Existed":
                message = "笔记已存在";
                break;
            case "Note Insert Failed":
                message = "新建笔记未知错误";
                break;
            case "Note Update Failed":
                message = "更新笔记未知错误";
                break;
            case "Note Delete Failed":
                message = "删除笔记未知错误";
                break;
            case "Group Not Found":
                message = "笔记分组不存在";
                break;
            case "Group Existed":
                message = "笔记分组已存在";
                break;
            case "Group Insert Failed":
                message = "新建笔记分组未知错误";
                break;
            case "Group Update Failed":
                message = "更新笔记分组未知错误";
                break;
            case "Group Delete Failed":
                message = "删除笔记分组未知错误";
                break;
            case "Group Name Duplicate":
                message = "笔记分组名重复";
                break;
            case "Could Not Update Default Group":
                message = "无法更新默认分组名";
                break;
            case "Could Not Delete Default Group":
                message = "无法删除默认分组";
                break;
            ////////////////////////////////////////////////////
            // Star
            case "StarItem Not Found":
                message = "收藏项不存在";
                break;
            case "StarItem Existed":
                message = "收藏想已存在";
                break;
            case "StarItem Insert Failed":
                message = "新建收藏项错误";
                break;
            case "StarItem Delete Failed":
                message = "删除收藏项错误";
                break;
            ////////////////////////////////////////////////////
            // Schedule
            case "Schedule Not Found":
                message = "课程表不存在";
                break;
            case "Update Schedule Failed":
                message = "更新课程表未知错误";
                break;
            case "Delete Schedule Failed":
                message = "删除课程表未知错误";
                break;
            ////////////////////////////////////////////////////
            // Document
            case "Document Not Found":
                message = "文档不存在";
                break;
            case "Document Existed":
                message = "文档已存在";
                break;
            case "Document Insert Failed":
                message = "新建文档未知错误";
                break;
            case "Document Update Failed":
                message = "更新文档未知错误";
                break;
            case "Document Delete Failed":
                message = "删除文档未知错误";
                break;
            case "Document Class Not Found":
                message = "文档分组不存在";
                break;
            case "Document Class Existed":
                message = "文档分组已存在";
                break;
            case "Document Class Insert Failed":
                message = "新建文档分组未知错误";
                break;
            case "Document Class Update Failed":
                message = "更新文档分组未知错误";
                break;
            case "Document Class Delete Failed":
                message = "删除文档分组未知错误";
                break;
            case "Document Class Name Duplicate":
                message = "文档分组名重复";
                break;
            case "Could Not Update Default Document Class":
                message = "无法更新默认文档分组名";
                break;
            case "Could Not Delete Default Document Class":
                message = "无法删除默认分组";
                break;
            case "File Extension Error":
                message = "上传的文件后缀名不支持，仅支持 " +
                    "[txt, md, pdf, doc, docx, ppt, pptx, xls, xlsx, zip, rar, jpg, png, jpeg, bmp]";
                break;
            case "Save Document Failed":
                message = "保存文档失败";
                break;
            ////////////////////////////////////////////////////
            // Raw
            case "Image Not Found":
                message = "图片不存在";
                break;
            case "Not Support Upload Type":
                message = "不受支持的上传类型";
                break;
            case "Save Image Failed":
                message = "图片上传失败";
                break;
        }

        return new ServerException(
            responseDTO.getCode(),
            message
        );
    }
}
