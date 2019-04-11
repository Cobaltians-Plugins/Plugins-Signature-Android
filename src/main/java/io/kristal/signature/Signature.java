package io.kristal.signature;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.cobaltians.cobalt.Cobalt;
import org.cobaltians.cobalt.fragments.CobaltFragment;
import org.cobaltians.cobalt.plugin.CobaltAbstractPlugin;
import org.cobaltians.cobalt.plugin.CobaltPluginWebContainer;
import org.json.JSONException;
import org.json.JSONObject;



public class Signature extends CobaltAbstractPlugin {

    private static final String TAG = CobaltAbstractPlugin.class.getSimpleName();

    private CobaltFragment fragment;
    private Context context;
    private String callback;
    private int request;

    /*******************************************************************************************************
     * MEMBERS
     *******************************************************************************************************/

    private static Signature sInstance;

    /**************************************************************************************
     * CONSTRUCTORS
     **************************************************************************************/

    public static CobaltAbstractPlugin getInstance(CobaltPluginWebContainer webContainer) {
        if (sInstance == null) {
            sInstance = new Signature();
        }
        return sInstance;
    }

    @Override
    public void onMessage(CobaltPluginWebContainer webContainer, JSONObject message) {
        try {
            Log.d(TAG,"Entering onMessage");
            fragment = webContainer.getFragment();
            context = webContainer.getActivity();
            String action = message.getString(Cobalt.kJSAction);
            JSONObject data = message.getJSONObject(Cobalt.kJSData);
            callback = message.getString(Cobalt.kJSCallback);
            request = callback.hashCode();
            Intent intent = new Intent(context, SignatureActivity.class);

            if ("sign".equals(action)) {
                fragment.startActivityForResult(intent, request);
            }
            else if (Cobalt.DEBUG) {
                Log.w(TAG, "onMessage: action '" + action + "' not recognized");
            }

        }
        catch(JSONException exception) {
            if (Cobalt.DEBUG) {
                Log.e(TAG, "onMessage: wrong format, possible issues: \n" +
                        "\t- missing 'action' field or not a string,\n" +
                        "\t- missing 'data' field or not a object,\n" +
                        "\t- missing 'data.actions' field or not an array,\n" +
                        "\t- missing 'callback' field or not a string.\n");
            }
            exception.printStackTrace();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == request) {
            if (data != null) {
                String id = data.getStringExtra(SignatureActivity.EXTRA_FILEPATH);
                String base64 = data.getStringExtra(SignatureActivity.EXTRA_BASE64);
                try {
                    JSONObject callbackData = new JSONObject();
                    callbackData.put("picture", base64);
                    callbackData.put("id", id);
                    fragment.sendCallback(callback, callbackData);
                } catch (JSONException exception) {
                    exception.printStackTrace();
                }
            } else {
                JSONObject callbackData = new JSONObject();
                fragment.sendCallback(callback, callbackData);
            }
        }
    }
}
