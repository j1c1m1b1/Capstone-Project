package com.jcmb.shakemeup.interfaces;

import org.json.JSONObject;

/**
 * @author Julio Mendoza on 12/31/15.
 */
public interface OnPlacesRequestCompleteListener {

    void onSuccess(JSONObject jsonResponse);

    void onFail();
}
