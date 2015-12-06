package com.peppersquare.rssps;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.peppersquare.rssps.Adapters.RAdapter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class ScrollingActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RAdapter mAdapter;
    private ArrayList<HashMap<String, String>> resultItems = new ArrayList<>();
    private String mFeedUrl = "http://feeds.feedburner.com/techcrunch/android?format=xml";
    private ProgressDialog progressDialog;
    private boolean isLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_scrolling);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Rss Feed Url: " + mFeedUrl, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new RAdapter(this, resultItems);
        mRecyclerView.setAdapter(mAdapter);

        getDataFromWeb();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_exit:
                finish();
                break;
            case R.id.refresh:
                if (!isLoading) {
                    getDataFromWeb();
                } else {
                    Toast.makeText(ScrollingActivity.this, "Please wait..", Toast.LENGTH_SHORT).show();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public class GetRssClass extends AsyncTask<Void, Void, ArrayList<HashMap<String, String>>> {
        String mUrl;

        public GetRssClass(String url) {
            this.mUrl = url;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog();
        }

        @Override
        protected ArrayList<HashMap<String, String>> doInBackground(Void... voids) {
            ArrayList<HashMap<String, String>> result = new ArrayList<>();
            try {
                URL url = new URL(mUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(15000);
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = connection.getInputStream();
                    result = parseXML(inputStream);
                } else {
                    throw new Exception("error");
                }
            } catch (Exception e) {
                Log.d("Exception", e.getMessage());
            }
            return result;
        }


        @Override
        protected void onPostExecute(ArrayList<HashMap<String, String>> result) {
            super.onPostExecute(result);
            Log.d("result ", String.valueOf(result));
            hideProgressDialog();
            int before = resultItems.size();
            resultItems.addAll(result);
            mAdapter.notifyItemRangeInserted(before, result.size());
            mRecyclerView.invalidate();
        }

        private ArrayList<HashMap<String, String>> parseXML(InputStream inputStream)
                throws ParserConfigurationException, IOException, SAXException {

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(inputStream);
            Element element = document.getDocumentElement();

            NodeList itemlist = element.getElementsByTagName("item");
            NodeList itemChildren;

            Node currentItem;
            Node currentChild;

            ArrayList<HashMap<String, String>> items = new ArrayList<>();
            HashMap<String, String> currentMap;

            int imgCount = 0;

            for (int i = 0; i < itemlist.getLength(); i++) {

                currentItem = itemlist.item(i);
                itemChildren = currentItem.getChildNodes();

                currentMap = new HashMap<>();

                for (int j = 0; j < itemChildren.getLength(); j++) {

                    currentChild = itemChildren.item(j);

                    if (currentChild.getNodeName().equalsIgnoreCase("title")) {
                        // Log.d("Title", String.valueOf(currentChild.getTextContent()));
                        currentMap.put("title", currentChild.getTextContent());
                    }

                    if (currentChild.getNodeName().equalsIgnoreCase("description")) {
                        // Log.d("description", String.valueOf(currentChild.getTextContent()));
                        currentMap.put("description", currentChild.getTextContent());
                    }

                    if (currentChild.getNodeName().equalsIgnoreCase("pubDate")) {
                        // Log.d("Title", String.valueOf(currentChild.getTextContent()));
                        currentMap.put("pubDate", currentChild.getTextContent());
                    }

                    if (currentChild.getNodeName().equalsIgnoreCase("feedburner:origLink")) {
                        currentMap.put("origLink", currentChild.getTextContent());
                    }

                    if (currentChild.getNodeName().equalsIgnoreCase("media:thumbnail")) {
                        imgCount++;

                        if (imgCount == 1) {
                            currentMap.put("imageUrl", currentChild.getAttributes().item(0).getTextContent());
                        }
                    }
                }
                if (currentMap != null && !currentMap.isEmpty()) {
                    items.add(currentMap);
                }
                imgCount = 0;
            }

            return items;

        }
    }

    private void showProgressDialog() {
        isLoading = true;
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Fetching data from web..");
        progressDialog.show();
    }

    private void hideProgressDialog() {
        isLoading = false;
        if (progressDialog != null) {
            progressDialog.cancel();
        }
    }

    private void getDataFromWeb() {
        GetRssClass fetchRss = new GetRssClass(mFeedUrl);
        fetchRss.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
