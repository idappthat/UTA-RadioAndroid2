package com.mobi.utaradio;

import android.os.AsyncTask;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import android.os.AsyncTask;
import java.net.URL;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by Zedd on 9/26/2014.
 * This class handles getting the song information
 * Status WIP
 *
 */
public class LoadDataFromXML extends AsyncTask<String, Integer, String> {

    //the xmlParser saves all its data on the content handler, thats where we're getting it from
    private HandlingXMLStuff hxl;

    @Override
    protected String doInBackground(String... url) {

        XmlParser xp = new XmlParser();
        hxl = new HandlingXMLStuff();
        xp.doInBackground(url[0], hxl);
        return "";	//dummy return
    }

    @Override
    protected void onPostExecute(String result) {
        String song = hxl.getSong();
        String artist = hxl.getArtist();
        MainFragment.musicTitle.setText(song);
        MainFragment.musicArtist.setText(artist);


        Log.d("USER", "We got song: " + song + " by: " + artist);
        //new getArtData().execute("");
    }


    private class HandlingXMLStuff extends DefaultHandler {

        private String songNameString;
        private String artistNameString;
        private boolean songName= false;
        private boolean artistName = false;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

            if(localName.equalsIgnoreCase("title"))
            {
                songName = true;
            }
            else if (localName.equals("artist"))
            {
                artistName = true;
            }

        }

        @Override
        public void characters(char[] ch, int start, int length)
                throws SAXException {
            if(songName){
                songName = false;
                songNameString =  new String(ch, start, length) ;

            } else if(artistName){

                artistName = false;
                artistNameString = new String(ch, start, length);

            }
        }

        public String getSong() {
            return this.songNameString;
        }

        public String getArtist() {
            return this.artistNameString;
        }

    }


    public class XmlParser{

        //static final String baseURL = "http://radio.uta.edu/mediaplayer/nowplaying.xml";
        public String information;

        public String getInfo()
        {
            return this.information;
        }

        public void doInBackground(String  URL, ContentHandler XMLHandler) {

            try{

                URL website = new URL( URL );

                SAXParserFactory spf = SAXParserFactory.newInstance();
                SAXParser sp = spf.newSAXParser();
                XMLReader xr = sp.getXMLReader();
                HandlingXMLStuff xmlHandler = (HandlingXMLStuff) XMLHandler;
                xr.setContentHandler(xmlHandler);
                xr.parse(new InputSource(website.openStream()));
                //this.information = xmlHandler.getInfomation();

            }
            catch (Exception e)
            {
                Log.d("USER", e.toString());
            }

        }

    }

    /**
     *  This class is used retrieve the album art for the song.
     *  We use last.fm API to get this info
     * @author zedd
     *
     */
    private class getArtData extends AsyncTask<String, Integer, String>{

        String apiKey;
        String songName;
        String artistName;

        private String downloadDocumentFromInternet(String URL) throws Exception{
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

            //the following section covers converting the input stream into a string

            //making an input stram reader, also called a BufferedReader
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            //String builder, Duh!
            StringBuilder sb = new StringBuilder();

            String line=null;
            while( (line = reader.readLine()) != null)
            {
                sb.append(line + "\n");		//appending each line read to the string builder and adding the new line character at the end of each line
            }
            reader.close();		//closing the reader, good practice
            return sb.toString();	//finally getting the string
        }

        @Override
        protected String doInBackground(String... params) {
            //Map gsonMap;
            String doc = null;
            try {
                doc = downloadDocumentFromInternet("http://ws.audioscrobbler.com/2.0/?method=track.getInfo&api_key=7f8d036638619be79d49391d8dbe2d11&artist=coldplay&track=clocks&format=json");
            } catch (Exception e) {
                Log.d("USER", "Error in getArtData: " + e.toString());
            }
            return doc;
        }

        @Override
        protected void onPostExecute(String result) {
            //parse json file
        }

    }

}