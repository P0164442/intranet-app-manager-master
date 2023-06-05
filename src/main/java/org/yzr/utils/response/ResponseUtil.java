package org.yzr.utils.response;


import java.util.List;


public class ResponseUtil {
    public static BaseResponse ok() {
        BaseResponse obj = new BaseResponse();
        obj.setCode(0);
        obj.setMsg("SUCCESS");
        return obj;
    }

    public static <T> BaseResponse ok(T data) {
        BaseResponse obj = new BaseResponse();
        obj.setCode(0);
        obj.setMsg("SUCCESS");
        obj.setData(data);
        return obj;
    }

    public static <T> BaseResponse okList(List<T> list) {
        PageData pageData = new PageData();
        pageData.setList(list);
        pageData.setTotal(list.size());
        pageData.setPage(1);
        pageData.setPageSize(list.size());
        pageData.setTotalPage(1);
        return ok(pageData);
    }

    public static BaseResponse fail() {
        BaseResponse obj = new BaseResponse();
        obj.setCode(-1);
        obj.setMsg("ERROR");
        return obj;
    }

    public static BaseResponse fail(int errno, String errmsg) {
        BaseResponse obj = new BaseResponse();
        obj.setCode(errno);
        obj.setMsg(errmsg);
        return obj;
    }

    public static BaseResponse badArgument() {
        return fail(401, "引数が不正です");
    }

    public static BaseResponse badArgumentValue() {
        return fail(402, "パラメータ値が不正です");
    }

    public static BaseResponse unlogin() {
        return fail(501, "ログインしてください");
    }

    public static BaseResponse serious() {
        return fail(502, "システム内部エラー");
    }

    public static BaseResponse unsupport() {
        return fail(503, "システム内部エラー");
    }

    public static BaseResponse updatedDateExpired() {
        return fail(504, "更新データが失効しました。");
    }

    public static BaseResponse updatedDataFailed() {
        return fail(505, "データの更新に失敗しました");
    }

    public static BaseResponse unauthz() {
        return fail(506, "操作権限がありません");
    }
}

