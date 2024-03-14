package Utility;

import android.content.Context;
import android.graphics.Bitmap;
import androidx.collection.LruCache;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

/**
 * Created by andrewlaurienrsocia on 19/04/2018.
 */

public class VolleySingleton {

    private static VolleySingleton volleySingleton;
    private RequestQueue requestQueue;
    private static Context context;
    private ImageLoader imageLoader;


    private VolleySingleton(Context context) {

        this.context = context;
        requestQueue = getRequestQueue();

        imageLoader = new ImageLoader(requestQueue,
                new ImageLoader.ImageCache() {
                    private final LruCache<String, Bitmap>
                            cache = new LruCache<String, Bitmap>(20);

                    @Override
                    public Bitmap getBitmap(String url) {
                        return cache.get(url);
                    }

                    @Override
                    public void putBitmap(String url, Bitmap bitmap) {
                        cache.put(url, bitmap);
                    }
                });

    }


    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            Cache cache = new DiskBasedCache(context.getCacheDir(), 10 * 1024 * 1024);
            Network network = new BasicNetwork(new HurlStack());
            requestQueue = new RequestQueue(cache, network);
            requestQueue = Volley.newRequestQueue(context.getApplicationContext());
            requestQueue.start();

        }

        return requestQueue;
    }

    public static synchronized VolleySingleton getInstance(Context c) {
        if (volleySingleton == null) {
            volleySingleton = new VolleySingleton(c);
        }

        return volleySingleton;

    }

    public <T> void addToRequestQueue(Request<T> req) {
       getRequestQueue().add(req);

//        requestQueue.addRequestFinishedListener(new RequestQueue.RequestFinishedListener<Object>() {
//            @Override
//            public void onRequestFinished(Request<Object> request) {
//                requestQueue.getCache().clear();
//            }
//        });
    }

    public ImageLoader getImageLoader() {
        return imageLoader;
    }


    public void cancellRequest(Object tag) {
        this.requestQueue.cancelAll(tag);
    }

}
