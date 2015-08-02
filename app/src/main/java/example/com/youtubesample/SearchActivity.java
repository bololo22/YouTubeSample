package example.com.youtubesample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;
import com.squareup.picasso.Picasso;
import android.os.Handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by Android1 on 7/20/2015.
 */
public class SearchActivity extends Activity {

    private EditText searchInput;
    private ListView videosFound;
    private SwipyRefreshLayout mSwipyRefreshLayout;

    private Handler handler;
    private String wordSearch;
    private String mNextPageToken;

    private ArrayAdapter<VideoItem> mVideoAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mNextPageToken = "";
        searchInput = (EditText)findViewById(R.id.search_input);
        videosFound = (ListView)findViewById(R.id.videos_found);
        mSwipyRefreshLayout = (SwipyRefreshLayout) findViewById(R.id.swipyrefreshlayout);
        handler = new Handler();

        if(savedInstanceState == null) {
            searchInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        wordSearch = v.getText().toString();
                        searchOnYoutube(wordSearch);
                        return false;
                    }
                    return true;
                }
            });
        }else{
            if(wordSearch != null) {
                searchOnYoutube(savedInstanceState.getString("wordSearch"));
                wordSearch = savedInstanceState.getString("wordSearch");
            }
        }

        mVideoAdapter = new ArrayAdapter<VideoItem>(getApplicationContext(), R.layout.video_item, searchResults){

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if(convertView == null){
                    convertView = getLayoutInflater().inflate(R.layout.video_item, parent, false);
                }
                ImageView thumbnail = (ImageView)convertView.findViewById(R.id.video_thumbnail);
                TextView title = (TextView)convertView.findViewById(R.id.video_title);
                TextView description = (TextView)convertView.findViewById(R.id.video_description);

                VideoItem searchResult = getItem(position);

                Picasso.with(getApplicationContext()).load(searchResult.getThumbnailURL()).into(thumbnail);
                title.setText(searchResult.getTitle());
                description.setText(searchResult.getDescription());
                return convertView;
            }
        };

        videosFound.setAdapter(mVideoAdapter);

        mSwipyRefreshLayout.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection direction) {
                mSwipyRefreshLayout.setRefreshing(true);
                if(direction == SwipyRefreshLayoutDirection.BOTTOM && wordSearch != null){
                    searchOnYoutube(wordSearch);
                }else{
                    mSwipyRefreshLayout.setRefreshing(false);
                }
                Log.d("SearchActivity", "Refresh triggered at "
                        + (direction == SwipyRefreshLayoutDirection.TOP ? "top" : "bottom"));
            }
        });

        addClickListener();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if(wordSearch != null){
            outState.putString("wordSearch", wordSearch);
            Log.v("SearchActivity", "wordSearch: " + wordSearch);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        searchInput.setText(savedInstanceState.getString("wordSearch"));
        if(savedInstanceState.getString("wordSearch") != null){
            searchOnYoutube(savedInstanceState.getString("wordSearch"));
        }
    }

    private List<VideoItem> searchResults = new ArrayList<VideoItem>();

    private void searchOnYoutube(final String keywords){
        new Thread(){
            public void run(){
                YoutubeConnector yc = new YoutubeConnector(SearchActivity.this);
                searchResults = yc.search(keywords);
                handler.post(new Runnable(){
                    public void run(){
                        mVideoAdapter.addAll(searchResults);
                        mVideoAdapter.notifyDataSetChanged();
                        mSwipyRefreshLayout.setRefreshing(false);
                    }
                });
            }
        }.start();
    }

    private void addClickListener(){
        videosFound.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> av, View v, int pos,
                                    long id) {
                VideoItem o = (VideoItem) av.getItemAtPosition(pos);
                Intent intent = new Intent(getApplicationContext(), PlayerActivity.class);
                intent.putExtra("VIDEO_ID", o.getId());
                startActivity(intent);
            }

        });
    }

    public void setNextPageToken(String nextPageToken) {
        mNextPageToken = nextPageToken;
    }

    public String getNextPageToken() {
        return mNextPageToken;
    }
}