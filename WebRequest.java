package Utility;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.payvenue.meterreader.Interface.IVolleyListener;
import com.payvenue.meterreader.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by andrewlaurienrsocia on 19/04/2018.
 */


public class WebRequest {

    Context c;
    private static final String TAG = "WebRequest";

    private RequestListener requestListener;


    public WebRequest(Context c) {
        this.c = c;
    }

    public void sendRequestFound(final String url,final Map<String, String> body,final String myType, final String params, final String param2, final String param3, final IVolleyListener listener) {

        StringRequest request = new StringRequest(Request.Method.POST, url, response -> {
            String resultcode = "";
            String res;
            if (myType.equalsIgnoreCase("FM")) {
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    if (jsonArray.length() > 0) {
                        for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject obj = jsonArray.getJSONObject(i);
                                res = obj.getString("result");
                                if (res.equalsIgnoreCase("0004")) {
                                    resultcode = "0004";
                                } else if(res.equalsIgnoreCase("0001")){
                                    resultcode = "0001";
                                }else if(res.equalsIgnoreCase("0002")){
                                    resultcode = "0002";
                                }else {
                                    String columnID = obj.getString("columnid");
                                    MainActivity.db.updateUploadStatusFoundMeter(MainActivity.db, columnID);
                                    resultcode = "200";
                                }
                        }
                    }

                    Log.e(TAG,"response: "+ response);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(TAG, "JSONException: " + e.getMessage());
                }
            }

            if (myType.equalsIgnoreCase("accounts")) {
                /***
                 *  param2 = duedate
                 *  params = counter
                 * */
                listener.onSuccess(myType, response, params, url, param2);
            } else {
                listener.onSuccess(myType, response, params, resultcode, param3);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "error:" + error.getMessage());
                listener.onFailed(error, myType);
            }
        }){

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return body;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(c).addToRequestQueue(request);
    }

    // update reader table to IsDownloaded
    public void sendRequest(String url) {
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "error : " + error.getMessage());
            }
        });

        VolleySingleton.getInstance(c).addToRequestQueue(request);
    }

    public void sendRequestUpload(final String url, final Map<String, String> body, final String myType) {
        /**
         *      params = reading data in json form
         *      param2 = count of data for uploading from cursor
         *
         * */
        //Log.e("url: ",url);
        
        StringRequest request = new StringRequest(Request.Method.POST, url, response -> {
            try {
                JSONArray jsonArray = new JSONArray(response);
                if (myType.equalsIgnoreCase("uploadData") || myType.equalsIgnoreCase("NotFound")) {
                    try {
                        if (response.length() > 0) {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject obj = jsonArray.getJSONObject(i);
                                String res = obj.getString("result");
                                //Log.e("MRB","result: "+res);
                                if (res.equalsIgnoreCase("0004")) {
                                    requestListener.onRequestListener("0004", "");
                                } else if (res.equalsIgnoreCase("0001")){
                                    requestListener.onRequestListener("0001", "");
                                } else if (res.equalsIgnoreCase("0002")){
                                    requestListener.onRequestListener("0002", "");
                                }else{
                                    MainActivity.db.updateUploadStaus(MainActivity.db, res, "Uploaded", "1");
                                    requestListener.onRequestListener("200","OK");
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> requestListener.onRequestListener("0005", error.getMessage())){

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return body;
            }
        };

        request.setTag("upload");
        //request.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(c).addToRequestQueue(request);
    }


    public interface RequestListener {
        void onRequestListener(String response, String param);
    }

    public void setRequestListener(String url,Map<String, String> body, String cmd,RequestListener listener) {
        this.requestListener = listener;
        sendRequestUpload(url,body, cmd);
    }

    public void setRequestListenerDownload(String url, RequestListener listener) {
        this.requestListener = listener;
        download(url);
    }


    public void download(final String url) {
        final JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, response -> requestListener.onRequestListener(response.toString(), ""), error -> requestListener.onRequestListener("500", error.getMessage()));

        request.setRetryPolicy(new DefaultRetryPolicy(60000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        request.setTag("download");
        VolleySingleton.getInstance(c).addToRequestQueue(request);
    }
}

