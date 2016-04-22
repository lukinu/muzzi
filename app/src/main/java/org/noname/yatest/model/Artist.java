package org.noname.yatest.model;

/*
*   A class representing our object - an Artist
*
* */
public class Artist {

    // field variables
    String mId;
    private String mName;
    private String mGenres;
    private String mSmallImageLink;
    private String mBigImageLink;
    private int mAlbumsNum;
    private int mTrackNum;
    private String mBio;

    // that's how will we instantiate our Artist
    public Artist(String id, String name, String genres, String smallImageLink, String bigImageLink,
                  int albumsNum, int tracksNum, String bio) {
        this.mId = id;
        this.mName = name;
        this.mGenres = genres;
        this.mSmallImageLink = smallImageLink;
        this.mBigImageLink = bigImageLink;
        this.mAlbumsNum = albumsNum;
        this.mTrackNum = tracksNum;
        this.mBio = bio;
    }

    // some getters-setters, more or or less usefull
    public String getName() {
        return mName;
    }

    public String getGenres() {
        return mGenres;
    }

    public String getBio() {
        return mBio;
    }

    public String getId() {
        return mId;
    }

    public String getSmallImageLink() {
        return mSmallImageLink;
    }

    public String getBigImageLink() {
        return mBigImageLink;
    }

    public int getAlbumsNum() {
        return mAlbumsNum;
    }

    public int getTrackNum() {
        return mTrackNum;
    }
}
