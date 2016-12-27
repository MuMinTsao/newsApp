package com.jetopto.newsapp;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.Toast;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.content.Context;

import java.util.ArrayList;
import java.net.MalformedURLException;
import java.net.URL;
import android.util.Log;

import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;
import android.os.HandlerThread;
import com.jetopto.newsapp.*;

public class NewsActivity extends AppCompatActivity implements AsyncTaskResult<ArrayList> {

    private final String TAG = "News";
    SQLiteDatabase mEventsDB;
    Toolbar toolbar;
    ArrayAdapter arrayAdapter;
    ListView listView;
    WebView webView;
    ArrayList mNewsList;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (webView.getVisibility() == View.VISIBLE) {
                webView.loadUrl("about:blank");
                webView.setVisibility(View.INVISIBLE);
                listView.setVisibility(View.VISIBLE);
                return false;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    public void refreshNews() {
        URL url;
        webView.setVisibility(View.INVISIBLE);
        listView.setVisibility(View.VISIBLE);

        arrayAdapter.clear();

        try {
            url = new URL("https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return;
        }
        Log.d(TAG, "refreshNews start !!!!!!!!!!!!!!!!!");
        GetNewsTask newsTask = new GetNewsTask(this);
        newsTask.newsResult = this;
        newsTask.newsDB = mEventsDB;
        newsTask.execute(url);
        Log.d(TAG, "refreshNews end !!!!!!!!!!!!!!!!!");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        listView = (ListView) findViewById(R.id.listview);

        webView = (WebView) findViewById(R.id.web);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.setVisibility(View.INVISIBLE);

        mEventsDB = this.openOrCreateDatabase("User", MODE_PRIVATE, null);
        mEventsDB.execSQL("DROP TABLE newsTable");
        mEventsDB.execSQL("CREATE TABLE IF NOT EXISTS newsTable (title varchar, url varchar)");

        arrayAdapter = new ArrayAdapter(NewsActivity.this, android.R.layout.simple_expandable_list_item_1);
        listView.setAdapter(arrayAdapter);

    }

    @Override
    public void taskFinish(ArrayList result)
    {
        Log.d(TAG, "taskFinish !!!!!!!!!!!!!!");
        /*
        for(int i = 0; i < result.size(); i++) {
            Log.d(TAG, "aaaaa item[" + i + "]=" + result.get(i));
        }
        */
        arrayAdapter.addAll(result);
        arrayAdapter.notifyDataSetChanged();
        //arrayAdapter = new ArrayAdapter(NewsActivity.this, android.R.layout.simple_expandable_list_item_1, result);
        //listView.setAdapter(arrayAdapter);

        listView.setVisibility(View.VISIBLE);
        webView.setVisibility(View.INVISIBLE);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView arg0, View arg1, int arg2,
                                    long arg3) {
                // TODO Auto-generated method stub
                ListView listView = (ListView) arg0;
                Toast.makeText(
                        NewsActivity.this,
                        "ID：" + arg3 +
                                "   選單文字："+ listView.getItemAtPosition(arg2).toString(),
                        Toast.LENGTH_LONG).show();

                Cursor c = mEventsDB.rawQuery("SELECT * FROM newsTable", null);
                c.moveToFirst();
                int urlIndex = c.getColumnIndex("url");
                int i = 0;
                for(; i < arg3; i++)
                    c.moveToNext();
                //Log.d(TAG, "12345678 item[" + i + "] url=" + c.getString(urlIndex));

                webView.loadUrl(c.getString(urlIndex));

                listView.setVisibility(View.INVISIBLE);
                webView.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_news, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            refreshNews();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
