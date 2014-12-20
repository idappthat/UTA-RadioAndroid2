package com.mobi.utaradio;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
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


import android.support.v7.graphics.Palette;
import android.util.Log;

import android.widget.ImageView;

/**
 * Created by Zedd on 9/26/2014.
 * This class handles getting the song information
 * Status WIP
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
        //set to the new values
        MainFragment.musicTitle.setText(song);
        MainFragment.musicArtist.setText(artist);
        MainFragment.musicAlbum.setText(album);
        boolean getNewData = true;

        //check if data is different, then get the album art
        if (song != null && !song.equals(MainFragment.musicTitle.getText().toString())) {
            getNewData = true;
        } else if (artist != null && !artist.equals(MainFragment.musicArtist.getText().toString())) {
            getNewData = true;
        } else if (album != null && !album.equals(MainFragment.musicAlbum.getText().toString())) {
            getNewData = true;
        }

        if (getNewData) {
            Log.d("USER", "We got song: " + song + " by: " + artist);
            //get a new album image
            new getArtData().execute(song, artist);
        } else {
            Log.d("USER", "No new data was acquired");
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
                //this.information = xmlHandler.getInfomation();

            } catch (Exception e) {
                Log.d("USER", e.toString());
            }

        }

    }

    /**
     * This class is used retrieve the album art for the song.
     * We use last.fm API to get this info
     *
     * @author zedd
     */
    private class getArtData extends AsyncTask<String, Integer, String> {


        private String downloadDocumentFromInternet(String URL) throws Exception {
            //making an http client
            HttpClient httpClient = new DefaultHttpClient();
            //making http post
            HttpPost httpPost = new HttpPost(URL);
            //making an http post request using httpClient and httpPost and saving that to httpResponse
            HttpResponse httpResponse = httpClient.execute(httpPost);
            //getting the entity from the response
            HttpEntity httpEntity = httpResponse.getEntity();
            //making an inputStram from response entity
            InputStream inputStream = httpEntity.getContent();
            //making an input stream reader, also called a BufferedReader
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            //String builder, Duh!
            StringBuilder sb = new StringBuilder();

            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");        //appending each line read to the string builder and adding the new line character at the end of each line
            }
            reader.close();        //closing the reader, good practice
            return sb.toString();    //finally getting the string
        }

        @Override
        protected String doInBackground(String... params) {
            String doc = null;
            String track = params[0];
            String artist = params[1];
//            debugging
//            artist = "coldplay";
//            track = "yellow";

            if (track != null) {
                track = track.replace(" ", "%20");
            }
            if (artist != null) {
                artist = artist.replace(" ", "%20");
            }
            Log.d("DEBUG", track + artist);

            try {
                String url = "http://ws.audioscrobbler.com/2.0/?method=track.getInfo&api_key=7f8d036638619be79d49391d8dbe2d11&artist=" + artist + "&track=" + track + "&format=json";
                Log.d("DEBUG", url);
                doc = downloadDocumentFromInternet(url);
            } catch (Exception e) {
                Log.d("USER", "Error in getArtData: " + e.toString());
            }
            return doc;
        }

        @Override
        protected void onPostExecute(String result) {
            //parse json file
            Log.d("DEBUG", result);
            try {
                //we parse the data here
                JSONObject json = new JSONObject(result);
                JSONObject track = json.getJSONObject("track");
                JSONObject album = track.getJSONObject("album");
                JSONArray image = album.getJSONArray("image");
                JSONObject thirdImage = image.getJSONObject(image.length() - 1);  //always get the last image
                String imageURL = thirdImage.get("#text").toString();
                Log.d("DEBUG", imageURL);
                DownloadImageTask downloadImage = new DownloadImageTask(MainFragment.musicAlbumImage);
                downloadImage.execute(imageURL);
            } catch (JSONException e) {
                Log.d("DEBUG", e.toString());
                //something went wrong
                //set the album art to the vinyl image
                MainFragment.musicAlbumImage.setImageResource(R.drawable.vinyl_records);
                //Enable on touch rotation of the album art
                MainFragment.allowAlbumImageRoation = false;
                //adding a red hue
                MainFragment.lLayout.getBackground().setColorFilter(0xffff0000, PorterDuff.Mode.MULTIPLY);
            }
        }
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

            Palette.generateAsync(result, new Palette.PaletteAsyncListener() {
                public void onGenerated(Palette palette) {
                    // Do something with colors...
                    MainFragment.lLayout.getBackground().setColorFilter(palette.getVibrantColor(0xffff0000), PorterDuff.Mode.MULTIPLY);
                }
            });


        }
    }
}