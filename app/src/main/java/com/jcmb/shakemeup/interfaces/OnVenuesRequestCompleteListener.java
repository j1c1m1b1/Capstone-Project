package com.jcmb.shakemeup.interfaces;

import org.json.JSONObject;

/**
 * @author Julio Mendoza on 1/20/16.
 */
public interface OnVenuesRequestCompleteListener {

    void onSuccess(JSONObject jsonObject);

    void onFail();
}
