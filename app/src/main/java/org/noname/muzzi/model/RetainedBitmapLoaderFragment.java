package org.noname.muzzi.model;

import android.app.Fragment;
import android.os.Bundle;

import org.noname.muzzi.utils.BitmapLoader;

public class RetainedBitmapLoaderFragment extends Fragment {
    private final BitmapLoader mBitmapLoader;

    public RetainedBitmapLoaderFragment() {
        mBitmapLoader = new BitmapLoader(getActivity());
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public BitmapLoader getBitmapLoader() {
        return mBitmapLoader;
    }

}
