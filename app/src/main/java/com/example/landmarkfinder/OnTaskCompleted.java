package com.example.landmarkfinder;

import org.json.JSONException;
import org.json.JSONObject;

public interface OnTaskCompleted {
    void onTaskCompleted(JSONObject jsonObject) throws JSONException;
}
