package io.kristal.signature;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
    private String callback;
    private int request;
    private static final int DEFAULT_SIZE = 800;

    /*******************************************************************************************************
     * MEMBERS
     *******************************************************************************************************/

    private static Signature sInstance;

    /**************************************************************************************
     * CONSTRUCTORS
     **************************************************************************************/

    public static Signature getInstance()
    {
        if (sInstance == null)
        {
            sInstance = new Signature();
        }
        return sInstance;
    }
    

    public void onMessage(@NonNull CobaltPluginWebContainer webContainer, @NonNull String action,
            @Nullable JSONObject data, @Nullable String callbackChannel)
    {

        if (webContainer.getFragment() != null){
            if(webContainer.getActivity() !=null) {
                fragment = webContainer.getFragment();
                context = webContainer.getActivity();
                callback = callbackChannel;
                request = new Random().nextInt(254);
                Intent intent = new Intent(context, SignatureActivity.class);

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
            else {
                Log.e(TAG, "webContainer activity is null");
            }
        }
        else {
            Log.e(TAG, "webContainer fragment is null");
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
                    Cobalt.publishMessage(callbackData, callback);
                } catch (JSONException exception) {
                    exception.printStackTrace();
                }
            } else {
                JSONObject callbackData = new JSONObject();
                Cobalt.publishMessage(callbackData, callback);
            }
        }
    }
}
