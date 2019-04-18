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

import java.util.Random;


public class Signature extends CobaltAbstractPlugin {

    private static final String TAG = CobaltAbstractPlugin.class.getSimpleName();

    private CobaltFragment fragment;
    private Context context;
    private String mPluginName;
    private int request;
    private static final int DEFAULT_SIZE = 800;

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
            request = new Random().nextInt(254);
            Intent intent = new Intent(context, SignatureActivity.class);
            mPluginName = message.getString(Cobalt.kJSPluginName);

            if ("sign".equals(action)) {
                if (data != null) {
                    int size = data.optInt("size");
                    if (size != 0) {
                        intent.putExtra(SignatureActivity.EXTRA_SIZE, size);
                    }
                }
                else {
                    intent.putExtra(SignatureActivity.EXTRA_SIZE, DEFAULT_SIZE);
                }
                fragment.startActivityForResult(intent, request);
            }
            else if (Cobalt.DEBUG) {
                Log.w(TAG, "onMessage: action '" + action + "' not recognized");
            }

        }
        catch(JSONException exception) {
            if (Cobalt.DEBUG) {
                Log.e(TAG, "onMessage: wrong format, possible issues: \n" +
                        "\t- missing 'data' field or not a object,\n" +
                        "\t- missing 'action' field or not a string,\n");
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
                    callbackData.put("id", id);
                    callbackData.put("picture", base64);
                    fragment.sendPlugin(mPluginName, callbackData);
                } catch (JSONException exception) {
                    exception.printStackTrace();
                }
            } else {
                JSONObject callbackData = new JSONObject();
                fragment.sendPlugin(mPluginName, callbackData);
            }
        }
    }
}
