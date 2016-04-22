package org.noname.muzzi;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

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

    private static final String LIST_VIEW_STATE = "listview_state";
    private List<Artist> mArtistsList;
    private ProgressBar mProgressBar;
    private ListView mArtistsListView;
    private Parcelable mViewState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setup UI basics:
        setContentView(R.layout.activity_main);
        // main ListView showing artists list
        mArtistsListView = (ListView) findViewById(R.id.listViewArtists);
        // restore ListView's state if not the first start of activity
        if (savedInstanceState != null) {
            mViewState = savedInstanceState.getParcelable(LIST_VIEW_STATE);
        }
        // progress bar to animate data loading process
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        // use Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.title);
        setSupportActionBar(toolbar);
        // instantiate a class responsible for async image loading and processing
        App.mBitmapLoader = new BitmapLoader(this);

        //Strict Mode, just for debugging
        try {
            Class strictModeClass = Class.forName("android.os.StrictMode");
            Class strictModeThreadPolicyClass = Class.forName("android.os.StrictMode$ThreadPolicy");
            Object laxPolicy = strictModeThreadPolicyClass.getField("LAX").get(null);
            Method method_setThreadPolicy = strictModeClass.getMethod("setThreadPolicy", strictModeThreadPolicyClass);
            method_setThreadPolicy.invoke(null, laxPolicy);
        } catch (Exception e) {
        }
        // begin load data to UI
        loadArtistsList();
    }

    // async load JSON data
    private void loadArtistsList() {
        mArtistsList = new ArrayList<>();
        String dataSourceUrl = getString(R.string.json_url);
        new ArtistsJSONParser(dataSourceUrl).execute();
    }

    // initialize ListView
    private void initArtistListView() {
        ArtistsListAdapter artistsListAdapter = new ArtistsListAdapter(this, R.layout.artist_list_item, mArtistsList);
        mArtistsListView.setAdapter(artistsListAdapter);
        mArtistsListView.setOnItemClickListener(this);
        if (mViewState != null) {
            mArtistsListView.onRestoreInstanceState(mViewState);
        }
    }

    // on ListView item click start new activity
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // instantiate new Intent, put needed data there
        Intent intent = new Intent(this, AboutArtistActivity.class);
        intent.putExtra(AboutArtistActivity.NAME, mArtistsList.get(position).getName());
        intent.putExtra(AboutArtistActivity.GENRE, mArtistsList.get(position).getGenres());
        String info = mArtistsList.get(position).getAlbumsNum() + " albums " +
                mArtistsList.get(position).getTrackNum() + " tracks";
        intent.putExtra(AboutArtistActivity.INFO, info);
        intent.putExtra(AboutArtistActivity.BIO, mArtistsList.get(position).getBio());
        intent.putExtra(AboutArtistActivity.IMAGE_LINK, mArtistsList.get(position).getSmallImageLink());
        // add some activity transition animations
        Bundle animationOptions = ActivityOptions.
                makeCustomAnimation(this, R.anim.slide_in, R.anim.slide_out).toBundle();
        this.startActivity(intent, animationOptions);
    }

    // save ListView's state on configuration changes
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        mViewState = mArtistsListView.onSaveInstanceState();
        outState.putParcelable(LIST_VIEW_STATE, mViewState);
        super.onSaveInstanceState(outState);
    }

    /*
    *  A class responsible for async loading of JSON objects,
    *  for parsing them and storing into the inner List
    */
    public class ArtistsJSONParser extends AsyncTask<Void, Integer, Boolean> {

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
        protected Boolean doInBackground(Void... params) {
            try {
                // Creating JSON Loader instance
                JSONLoader jsonLoader = new JSONLoader();

                // getting JSON by URL as an array
                mJSONArtists = jsonLoader.getJSONFromUrl(jsonUrl);

                if (mJSONArtists != null) {
                    // looping through all the artists in the JSON array, adding them into List
                    for (int i = 0; i < mJSONArtists.length(); i++) {
                        JSONObject jsonArtist = mJSONArtists.getJSONObject(i);
                        // declare artist's data variables
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
                        // instantiate a new Artist, store it into List
                        mArtistsList.add(new Artist(id, name, genres, smallImageUrl, bigImageUrl, albumsNum, tracksNum, descr));
                        // don't forget to publish our work progress
                        publishProgress((i / mJSONArtists.length()) * 100);
                    }
                } else {
                    return false;
                }
            } catch (JSONException e) {
                Log.e("json exception ", e.toString());
            }
            return true;
        }

        // roll the progress bar
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            mProgressBar.setProgress(values[0]);
        }

        // get results and begin to initialize ListView with all the data
        @Override
        protected void onPostExecute(Boolean isSuccess) {
            mProgressBar.setVisibility(View.GONE);
            super.onPostExecute(isSuccess);
            // if some data have been retrieved
            if (isSuccess) {
                initArtistListView();
            }
            // or if a network error occurred in getting data
            else {
                Toast.makeText(getApplicationContext(), getString(R.string.net_error), Toast.LENGTH_LONG).show();
            }
        }

        // show our progressbar first
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBar.setVisibility(View.VISIBLE);
        }
    }

}