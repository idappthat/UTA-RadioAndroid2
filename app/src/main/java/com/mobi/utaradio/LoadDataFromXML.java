package com.mobi.utaradio;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.net.URL;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Random;


import android.support.v7.graphics.Palette;
import android.util.Log;

import android.widget.ImageView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.mobi.utaradio.util.Blur;

/**
 * Created by Zedd on 9/26/2014.
 * This class handles getting the song information such as title and singer
 * it also handles getting album art
 * Status ALMOST done
 */
public class LoadDataFromXML extends AsyncTask<String, Integer, String> {

    //the xmlParser saves all its data on the content handler, thats where we're getting it from
    private HandlingXMLStuff hxl;

    @Override
    protected String doInBackground(String... url) {
        XmlParser xp = new XmlParser();
        hxl = new HandlingXMLStuff();
        xp.doInBackground(url[0], hxl);
        return "";    //dummy return
    }

    @Override
    protected void onPostExecute(String result) {
        String song = hxl.getSong();
        String artist = hxl.getArtist();
        String album = hxl.getAlbum();
        boolean getNewData = false;

        //check if data is different, then get the album art
        if (song != null && MainFragment.musicTitle.getText()!= null && !song.equals(MainFragment.musicTitle.getText().toString())) {
            getNewData = true;
        } else if (artist != null && MainFragment.musicArtist.getText()!= null && !artist.equals(MainFragment.musicArtist.getText().toString())) {
            getNewData = true;
        } else if (album != null && MainFragment.musicAlbum.getText()!= null && !album.equals(MainFragment.musicAlbum.getText().toString())) {
            getNewData = true;
        }

        if (getNewData) { //ok we got a new song

            //we publish the rating to the data base
            MainFragment.publishRating();
            //reset the like status
            MainFragment.resetTrackLiked();
            //set to the new values
            MainFragment.musicTitle.setText(song);
            MainFragment.musicArtist.setText(artist);
            MainFragment.musicAlbum.setText(album);
            //debug info
            Log.d("USER", "We got song: " + song + " by: " + artist);
            //get a new album image
            //new getArtData().execute(song, artist, album);
            retrieveAlbumArt(MainFragment.musicTitle.getContext(), getArtURL(song, artist, album));

        } else {
            Log.e("USER", "No new data was acquired");
        }
    }


    private class HandlingXMLStuff extends DefaultHandler {

        private String songNameString;
        private String artistNameString;
        private String albumNameString;

        private boolean songName = false;
        private boolean artistName = false;
        private boolean albumName = false;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

            if (localName.equalsIgnoreCase("title")) {
                songName = true;
            } else if (localName.equals("artist")) {
                artistName = true;
            } else if (localName.equals("Album")) {
                albumName = true;
            }

        }

        @Override
        public void characters(char[] ch, int start, int length)
                throws SAXException {
            if (songName) {
                songName = false;
                songNameString = new String(ch, start, length);

            } else if (artistName) {

                artistName = false;
                artistNameString = new String(ch, start, length);

            } else if (albumName) {

                albumName = false;
                albumNameString = new String(ch, start, length);

            }
        }

        public String getSong() {
            return this.songNameString;
        }

        public String getAlbum() {
            return this.albumNameString;
        }

        public String getArtist() {
            return this.artistNameString;
        }

    }


    public class XmlParser {

        public String information;

        public String getInfo() {
            return this.information;
        }

        public void doInBackground(String URL, ContentHandler XMLHandler) {

            try {

                URL website = new URL(URL);

                SAXParserFactory spf = SAXParserFactory.newInstance();
                SAXParser sp = spf.newSAXParser();
                XMLReader xr = sp.getXMLReader();
                HandlingXMLStuff xmlHandler = (HandlingXMLStuff) XMLHandler;
                xr.setContentHandler(xmlHandler);
                xr.parse(new InputSource(website.openStream()));
            } catch (Exception e) {
                Log.d("USER", e.toString());
            }

        }

    }

    private void retrieveAlbumArt(Context context, String url) {
        RequestQueue queue = Volley.newRequestQueue(context);

        JsonObjectRequest artRequest = new JsonObjectRequest(Request.Method.GET,
                url, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject jsonObject) {
                try {
                    String url = jsonObject.get("url").toString();
                    if(!url.equals("false")) {
                        DownloadImageTask downloadImage = new DownloadImageTask(MainFragment.musicAlbumImage);
                        downloadImage.execute(url);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("JSON ERROR", "ERROR: " + e.toString());
                    doAlbumArtErrors();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e("ART API", "ERROR: " + volleyError.toString());
                doAlbumArtErrors();
            }
        });

        queue.add(artRequest);
    }

    /**
     * Get the URL to query Thad's Heroku app
     *
     * @author Cameron
     */
    private String getArtURL(String song, String artist, String album) {
        Uri.Builder builder = new Uri.Builder()
                .scheme("http")
                .authority("album-art-engine.herokuapp.com")
                .appendPath("getAA");

                /* Check for not null parameters */
        if(song != null && artist != null) {
            builder.appendQueryParameter("song_name", song);
            builder.appendQueryParameter("artist_name", artist);
        }
        if(album != null)
            builder.appendQueryParameter("album_name", album);

        return builder.build().toString();
    }

    private void doAlbumArtErrors() {
        MainFragment.musicAlbumImage.setImageResource(R.drawable.vinyl_records);
        //Enable on touch rotation of the album art
        MainFragment.allowAlbumImageRoation = true;

        //adding a random hue to background
        Resources res = MainFragment.lLayout.getContext().getResources();
        MainFragment.lLayout.setBackgroundDrawable(res.getDrawable(R.drawable.album_blur_original));
    }


    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap image = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                image = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("DEBUG", e.getMessage());
                bmImage.setImageResource(R.drawable.vinyl_records);
            }
            return image;
        }

        protected void onPostExecute(Bitmap result) {
            //set the album art to the new image
            bmImage.setImageBitmap(result);
            //set the background to the new color

            //disable album art on touch rotation
            MainFragment.allowAlbumImageRoation = false;

            Bitmap blured = Blur.fastblur(bmImage.getContext(), result, 25);
            Drawable d = new BitmapDrawable(bmImage.getContext().getResources(), blured);
            MainFragment.lLayout.setBackgroundDrawable(d);
        }
    }
}