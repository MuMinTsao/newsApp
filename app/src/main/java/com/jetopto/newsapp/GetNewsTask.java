package com.jetopto.newsapp;

import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import android.app.ProgressDialog;
import android.content.Context;
import com.jetopto.newsapp.AsyncTaskResult;

/**
 * Created by lalalilalat on 2016/12/22.
 */

public class GetNewsTask extends AsyncTask<URL, Integer, ArrayList> {
    //public AsyncTaskResult<ArrayList> newsResult = null;
    private final String TAG = "NewsAsyncTask";
    int mItemNum = 100;
    SQLiteDatabase newsDB;
    private ProgressDialog mProgressDialog;
    private Context mContext;

    public AsyncTaskResult<ArrayList> newsResult = null;

    public GetNewsTask(Context context) {
        this.mContext = context;
    }

    @Override
    protected void onPreExecute() {
        Log.d(TAG, "+++ onPreExecute +++");
        mProgressDialog = new ProgressDialog(this.mContext);
        mProgressDialog.setMessage("Loading...");
        mProgressDialog.setCancelable(false);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.show();
    }

    String urlGetInfo(URL url) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String result = "";

        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            InputStream in = urlConnection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(in));
            int data = reader.read();
            while (data != -1) {
                char current = (char) data;
                result += current;
                data = reader.read();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "failed";
        }
        Log.d(TAG, result);
        return result;
    }

    @Override
    protected ArrayList doInBackground(URL... urls) {
        Log.d(TAG, "+++ doInBackground +++");
        String Webinfo = null;
        URL url;
        int length = 0;

        String [] SpliteWeb = null;

        Webinfo = urlGetInfo(urls[0]);

        SpliteWeb = Webinfo.split(", ");
        length = SpliteWeb.length;
        if (length > mItemNum)
            length = mItemNum;
        for (int i = 0; i < length; i++) {
            SpliteWeb[i] = SpliteWeb[i].replaceAll("\\[ ","");
            SpliteWeb[i] = SpliteWeb[i].replaceAll(" \\]","");
            Log.d(TAG, "AfterSplite["+ i + "]=" + SpliteWeb[i] + " ==========================");
            try {
                url = new URL("https://hacker-news.firebaseio.com/v0/item/" + SpliteWeb[i] + ".json?print=pretty");
                Webinfo = urlGetInfo(url);
                JSONObject jsonObject = new JSONObject(Webinfo);
                String titleInfo = jsonObject.getString("title");
                //Log.d(TAG, "title content=" + titleInfo);
                try {
                    String net = jsonObject.getString("url");
                    //Log.d(TAG, "url content=" + net);
                    newsDB.execSQL("INSERT INTO newsTable (title, url) VALUES (\"" + titleInfo + "\", \"" + net + "\")");
                } catch (Exception e) {
                    newsDB.execSQL("INSERT INTO newsTable (title, url) VALUES (\"" + titleInfo + "\", \" NULL \")");
                    e.printStackTrace();
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            publishProgress((int) ((i / (float) length) * 100));
        }

        Cursor c = newsDB.rawQuery("SELECT * FROM newsTable", null);
        int titleIndex = c.getColumnIndex("title");
        //int urlIndex = c.getColumnIndex("url");
        c.moveToFirst();

        int i = 0;
        ArrayList titles = new ArrayList();

        while ( c != null && i < mItemNum) {
            //Log.d(TAG, i + " item");
            //Log.d(TAG, i + " title content=" + c.getString(titleIndex));
            //Log.d(TAG, i + " url content=" + c.getString(urlIndex));
            i++;
            titles.add(c.getString(titleIndex));
            c.moveToNext();
        }

        return titles;
    }

    protected void onProgressUpdate(Integer... progress)
    {
        //Log.d(TAG, "Current progress=" + progress[0]);
        mProgressDialog.setProgress(progress[0]);
    }

    @Override
    protected void onPostExecute(ArrayList result) {
        Log.d(TAG, "+++ onPostExecute +++");
        this.newsResult.taskFinish(result);
        mProgressDialog.dismiss();
    }
}