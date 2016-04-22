package org.noname.muzzi.model;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.LruCache;

/*
*   A class responsible for storing objects
*   that must survive Activity re-creation,
*   namely, image cache object.
*
* */
public class RetainedBitmapCacheFragment extends Fragment {
    private final LruCache<Integer, Bitmap> mMemoryCache;

    public RetainedBitmapCacheFragment() {
        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;
        mMemoryCache = new LruCache<Integer, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(Integer key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    // set it as a RetainInstance
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    // getter to access cache object
    public LruCache<Integer, Bitmap> getMemoryCache() {
        return mMemoryCache;
    }
}