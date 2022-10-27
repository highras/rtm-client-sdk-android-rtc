package com.highras.videoudp;

import org.json.JSONException;

/**
 * @author fengzi
 * @date 2021/12/28 20:44
 */
public interface IHttpCallback {
    void onSuccess(String data) throws JSONException;

    void onError(int code, String msg);
}
