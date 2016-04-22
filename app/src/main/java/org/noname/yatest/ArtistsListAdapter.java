package org.noname.yatest;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.noname.yatest.model.App;
import org.noname.yatest.model.Artist;

import java.util.List;

/*
*   A ListView adapter class, Nothing else
*
* */
public class ArtistsListAdapter extends ArrayAdapter<Artist> {
    // class fields
    private Context mContext;
    private int mElementLayoutId;
    private List<Artist> mArtistsList;
    private LayoutInflater mLayoutInflater;

    // get all the necessary data on adapter instantiation
    public ArtistsListAdapter(Context context, int elementLayoutId, List<Artist> objects) {
        super(context, elementLayoutId, objects);
        this.mContext = context;
        this.mElementLayoutId = elementLayoutId;
        this.mArtistsList = objects;
        if (context != null) {
            mLayoutInflater = LayoutInflater.from(context);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(mElementLayoutId, null);
        }
        ImageView image = (ImageView) convertView.findViewById(R.id.imageViewArtistSmall);
        TextView name = (TextView) convertView.findViewById(R.id.nameTextView);
        TextView info = (TextView) convertView.findViewById(R.id.infoTextView);
        TextView genre = (TextView) convertView.findViewById(R.id.genreTextView);
        Artist artist = getItem(position);
        name.setText(artist.getName());
        genre.setText(artist.getGenres());
        info.setText(String.format(mContext.getResources().getString(R.string.info),
                artist.getAlbumsNum(), artist.getTrackNum()));
        // ask BitmapLoader to load image by URI from the Intent.
        App.mBitmapLoader.loadBitmap(artist.getSmallImageLink(), image, 200, 200);
        return convertView;
    }

    @Override
    public Artist getItem(int position) {
        return mArtistsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return mArtistsList.size();
    }
}