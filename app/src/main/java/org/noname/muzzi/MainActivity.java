package org.noname.muzzi;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.noname.muzzi.model.App;
import org.noname.muzzi.model.Artist;
import org.noname.muzzi.utils.BitmapLoader;
import org.noname.muzzi.utils.JSONLoader;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private List<Artist> mArtists;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        App.mBitmapLoader = new BitmapLoader(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Strict Mode for debugging
        try {
            Class strictModeClass = Class.forName("android.os.StrictMode");
            Class strictModeThreadPolicyClass = Class.forName("android.os.StrictMode$ThreadPolicy");
            Object laxPolicy = strictModeThreadPolicyClass.getField("LAX").get(null);
            Method method_setThreadPolicy = strictModeClass.getMethod("setThreadPolicy", strictModeThreadPolicyClass);
            method_setThreadPolicy.invoke(null, laxPolicy);
        } catch (Exception e) {
        }
        loadArtistsList();
    }

    private void loadArtistsList() {
        mArtists = new ArrayList<>();
        String dataSourceUrl = getString(R.string.json_url);
        new ArtistsJSONParser(dataSourceUrl).execute();
    }

    private void initArtistListView() {
        ListView artistList = (ListView) findViewById(R.id.listViewArtists);
        ArtistsListAdapter artistsListAdapter = new ArtistsListAdapter(this, R.layout.artist_list_item, mArtists);
        artistList.setAdapter(artistsListAdapter);
        artistList.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(this, AboutArtistActivity.class);
        intent.putExtra(AboutArtistActivity.NAME, mArtists.get(position).getName());
        intent.putExtra(AboutArtistActivity.GENRE, mArtists.get(position).getGenres());
        String info = mArtists.get(position).getAlbumsNum() + " albums " +
                mArtists.get(position).getTrackNum() + " tracks";
        intent.putExtra(AboutArtistActivity.INFO, info);
        intent.putExtra(AboutArtistActivity.BIO, mArtists.get(position).getBio());
        intent.putExtra(AboutArtistActivity.BIG_IMAGE_LINK, mArtists.get(position).getBigImageLink());
        this.startActivity(intent);
    }

    public class ArtistsJSONParser extends AsyncTask<Void, Integer, Void> {

        private static final String TAG_ID = "id";
        private static final String TAG_NAME = "name";
        private static final String TAG_GENRES = "genres";
        private static final String TAG_TRACKS = "tracks";
        private static final String TAG_ALBUMS = "albums";
        private static final String TAG_DESC = "description";
        private static final String TAG_COVERS = "cover";
        private static final String TAG_COVER_LINK_SMALL = "small";
        private static final String TAG_COVER_LINK_BIG = "big";

        private String jsonUrl;
        private JSONArray mJSONArtists;

        public ArtistsJSONParser(String dataSourceUrl) {
            this.jsonUrl = dataSourceUrl;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                // Creating JSON Parser instance
                JSONLoader jsonLoader = new JSONLoader();

                // getting JSON string from URL
                mJSONArtists = jsonLoader.getJSONFromUrl(jsonUrl);

                // looping through all artists
                for (int i = 0; i < mJSONArtists.length(); i++) {
                    JSONObject jsonArtist = mJSONArtists.getJSONObject(i);

                    String id = null;
                    String name = null;
                    int tracksNum = 0;
                    int albumsNum = 0;
                    String descr = null;
                    String smallImageUrl = null;
                    String bigImageUrl = null;
                    String genres = null;
                    JSONObject coverImages;
                    JSONArray jsonArrayGenres;

                    // Storing each json item in variable
                    if (jsonArtist.has(TAG_ID)) {
                        id = jsonArtist.getString(TAG_ID);
                    }
                    if (jsonArtist.has(TAG_NAME)) {
                        name = jsonArtist.getString(TAG_NAME);
                    }
                    if (jsonArtist.has(TAG_TRACKS)) {
                        tracksNum = Integer.parseInt(jsonArtist.getString(TAG_TRACKS));
                    }
                    if (jsonArtist.has(TAG_ALBUMS)) {
                        albumsNum = Integer.parseInt(jsonArtist.getString(TAG_ALBUMS));
                    }
                    if (jsonArtist.has(TAG_DESC)) {
                        descr = jsonArtist.getString(TAG_DESC);
                    }
                    if (jsonArtist.has(TAG_GENRES)) {
                        jsonArrayGenres = jsonArtist.getJSONArray(TAG_GENRES);
                        StringBuilder stringBuilder = new StringBuilder();
                        for (int j = 0; j < jsonArrayGenres.length(); j++) {
                            stringBuilder.append(jsonArrayGenres.getString(j));
                            stringBuilder.append(" ");
                        }
                        genres = stringBuilder.toString();
                    }
                    if (jsonArtist.has(TAG_COVERS)) {
                        coverImages = jsonArtist.getJSONObject(TAG_COVERS);
                        if (coverImages.has(TAG_COVER_LINK_SMALL)) {
                            smallImageUrl = coverImages.getString(TAG_COVER_LINK_SMALL);
                        }
                        if (coverImages.has(TAG_COVER_LINK_BIG)) {
                            bigImageUrl = coverImages.getString(TAG_COVER_LINK_BIG);
                        }
                    }
                    mArtists.add(new Artist(id, name, genres, smallImageUrl, bigImageUrl, albumsNum, tracksNum, descr));
                    publishProgress((i / mJSONArtists.length()) * 100);
                }
            } catch (JSONException e) {
                Log.e("json exception ", e.toString());
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            mProgressBar.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mProgressBar.setVisibility(View.GONE);
            initArtistListView();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBar.setVisibility(View.VISIBLE);
        }
    }

}