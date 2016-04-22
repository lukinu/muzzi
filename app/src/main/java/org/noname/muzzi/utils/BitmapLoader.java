package org.noname.muzzi.utils;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

import org.noname.muzzi.R;
import org.noname.muzzi.model.RetainedBitmapCacheFragment;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

/*
*   A class responsible for providing Bitmap images to a specified Views.
*   Image is specified as URL and can be either recalled from the image cache,
*   or downloaded from Internet.
* */
public class BitmapLoader {

    private static final String BMP_LDR_TAG = "bitmap_loader";
    private static final String BMP_CACHE_TAG = "bitmap_cache_fragment";
    private LruCache<Integer, Bitmap> mMemoryCache;
    private Bitmap mPlaceHolderBitmap;
    private Context mContext;
    int x = 0;
    int y = 0;

    // The constructor gets Context as a parameter cause we've chosen to let it operate on Views directly
    public BitmapLoader(Context context) {
        this.mContext = context;
        // get RetainedFragment, storing our image cache, or create it newly
        FragmentManager fragmentManager = ((Activity) mContext).getFragmentManager();
        final RetainedBitmapCacheFragment retainedBitmapCacheFragment =
                (RetainedBitmapCacheFragment) fragmentManager.findFragmentByTag(BMP_CACHE_TAG);
        if (retainedBitmapCacheFragment != null) {
            mMemoryCache = retainedBitmapCacheFragment.getMemoryCache();
        } else {
            final RetainedBitmapCacheFragment bitmapCacheFragment = new RetainedBitmapCacheFragment();
            fragmentManager.beginTransaction()
                    .add(bitmapCacheFragment, BMP_CACHE_TAG)
                    .commit();
            mMemoryCache = bitmapCacheFragment.getMemoryCache();
        }
        // assign a dumb image
        mPlaceHolderBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_portrait_black_48dp);
    }

    // puts image into cache
    public void addBitmapToMemoryCache(Integer key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    // gets image from cache by key
    public Bitmap getBitmapFromMemCache(Integer key) {
        return mMemoryCache.get(key);
    }

    // tries to find image in cache and set it into View. If not found - runs async download task
    public void loadBitmap(String imageUrl, ImageView imageView, int x, int y) {
        this.x = x;
        this.y = y;
        final Integer imageKey = imageUrl.hashCode();
        final Bitmap bitmap = getBitmapFromMemCache(imageKey);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
            Log.d(BMP_LDR_TAG, "Loaded bitmap from cache");
        } else {
            // check if the image isn't already been downloaded
            if (cancelPotentialWork(imageUrl, imageView)) {
                final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
                // set drawable to View and start async downloading task
                final AsyncDrawable asyncDrawable =
                        new AsyncDrawable(mContext.getResources(), mPlaceHolderBitmap, task);
                imageView.setImageDrawable(asyncDrawable);
                imageView.setTag(imageKey);
                task.execute(imageUrl);
            }
        }
    }

    /*
    *   A class to hold a weak reference to a loader task
    *   to let GC deal with it later
    * */
    static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap,
                             BitmapWorkerTask bitmapWorkerTask) {
            super(res, bitmap);
            bitmapWorkerTaskReference =
                    new WeakReference<>(bitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
    }

    // check if the same downloading is already going on
    public static boolean cancelPotentialWork(String url, ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final String bitmapUrl = bitmapWorkerTask.mUrl;
            if (!bitmapUrl.equals(url)) {
                // Cancel previous task
                bitmapWorkerTask.cancel(true);
            } else {
                // The same work is already in progress
                return false;
            }
        }
        // No task associated with the ImageView, or an existing task was cancelled
        return true;
    }

    // get loading tasks's reference from its associated View
    private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    /*
    *   Async task class to do the job of getting image from Internet
    * */
    public class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {

        private static final int IO_BUFFER_SIZE = 0xFFFF;
        private String mUrl = "";
        private final WeakReference<ImageView> mImageViewReference;

        public BitmapWorkerTask(ImageView imageView) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            mImageViewReference = new WeakReference<>(imageView);
        }

        // Load and decode image in background.
        @Override
        protected Bitmap doInBackground(String... params) {
            // getting images from web
            this.mUrl = params[0];
            HttpURLConnection urlConnection = null;
            InputStream inputStream = null;
            Bitmap bitmap = null;
            try {
                URL url = new URL(mUrl);
                urlConnection = (HttpURLConnection) url.openConnection();
                inputStream = urlConnection.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (inputStream != null) {
                // get image from the stream and scale it appropriately
                try {
                    bitmap = decodeSampledBitmapFromStream(inputStream, x, y);
                } catch (Exception e) {
                    Log.e("Error decoding image ", e.toString());
                } finally {
                    urlConnection.disconnect();
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return bitmap;
        }

        // scale the image from stream
        public Bitmap decodeSampledBitmapFromStream(InputStream inputStream,
                                                    int reqWidth, int reqHeight) {
            // First decode with inJustDecodeBounds=true to check dimensions
            byte[] bytes = new byte[0];
            try {
                bytes = getBytesFromInputStream(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }

            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
        }

        //  read stream to a byte array
        public byte[] getBytesFromInputStream(InputStream is) throws IOException {
            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                byte[] bytes = new byte[IO_BUFFER_SIZE];
                for (int len; (len = is.read(bytes)) != -1; )
                    os.write(bytes, 0, len);
                return os.toByteArray();
            }
        }

        // calculate scaling ratio
        public int calculateInSampleSize(
                BitmapFactory.Options options, int reqWidth, int reqHeight) {
            // Raw height and width of image
            final int height = options.outHeight;
            final int width = options.outWidth;
            int inSampleSize = 1;

            if (height > reqHeight || width > reqWidth) {

                final int halfHeight = height / 2;
                final int halfWidth = width / 2;

                // Calculate the largest inSampleSize value that is a power of 2 and keeps both
                // height and width larger than the requested height and width.
                while ((halfHeight / inSampleSize) > reqHeight
                        && (halfWidth / inSampleSize) > reqWidth) {
                    inSampleSize *= 2;
                }
            }
            return inSampleSize;
        }

        // set image to View finally
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (mImageViewReference != null && bitmap != null) {
                final ImageView imageView = mImageViewReference.get();
                if (imageView != null) {
                    // dont' forget to put it into cache for future use
                    addBitmapToMemoryCache((Integer) imageView.getTag(), bitmap);
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }
}