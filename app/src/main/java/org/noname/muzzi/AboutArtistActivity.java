package org.noname.muzzi;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import org.noname.muzzi.model.App;

/*
*   An activity class to represent artist's detailed view
*
* */
public class AboutArtistActivity extends AppCompatActivity {
    // constant keys to get data from Intent
    public static final String NAME = "NAME";
    public static final String GENRE = "GENRE";
    public static final String INFO = "INFO";
    public static final String BIO = "BIO";
    public static final String IMAGE_LINK = "IMAGE_LINK";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // initialize UI basics:
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_artist);
        ImageView image = (ImageView) findViewById(R.id.imageViewArtistLarge);
        TextView genre = (TextView) findViewById(R.id.textViewGenre);
        TextView info = (TextView) findViewById(R.id.textViewInfo);
        TextView bio = (TextView) findViewById(R.id.textViewBio);
        // get the Intent and process its data
        Intent intent = this.getIntent();
        genre.setText(intent.getStringExtra(GENRE));
        info.setText(intent.getStringExtra(INFO));
        bio.setText(intent.getStringExtra(BIO));
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarArtistActivity);
        toolbar.setTitle(intent.getStringExtra(NAME));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        Bitmap placeHolderBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_portrait_black_48dp);
        image.setImageBitmap(placeHolderBitmap);
        // ask BitmapLoader to load image by URI from the Intent.
        App.mBitmapLoader.loadBitmap(intent.getStringExtra(IMAGE_LINK), image, 400, 400); //todo: scaling is wrong
        // todo: use cropped upper rectangle of an original image for better view
    }

    // back button at the toolbar
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }
}