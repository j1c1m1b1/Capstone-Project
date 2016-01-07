package com.jcmb.shakemeup.interfaces;

import org.json.JSONObject;

/**
 * @author Julio Mendoza on 1/5/16.
 */
public interface OnRequestCompleteListener {

    void onSuccess(JSONObject jsonResponse);

    void onFail();
}
