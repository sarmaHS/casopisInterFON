package com.android.casopisinterfon.interfon.activity.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.casopisinterfon.interfon.ArticlesAdapter;
import com.android.casopisinterfon.interfon.DummyData;
import com.android.casopisinterfon.interfon.R;
import com.android.casopisinterfon.interfon.activity.ArticleViewActivity;
import com.android.casopisinterfon.interfon.data.DataManager;
import com.android.casopisinterfon.interfon.internet.NetworkManager;
import com.android.casopisinterfon.interfon.internet.events.ListDownloadedEvent;
import com.android.casopisinterfon.interfon.model.Article;
import com.android.casopisinterfon.interfon.model.Category;
import com.android.casopisinterfon.interfon.utils.EndlessRecyclerViewScrollListener;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;


public class ArticlesFragment extends Fragment implements ArticlesAdapter.ItemClickedCallbackInterface {

    public static final String POSITION_ARG = "page_position";

    private DataManager mDataManager;
    private NetworkManager mNetManager;
    private ArticlesAdapter mAdapter;
    private LinearLayoutManager linearLayoutManager;
    private EndlessRecyclerViewScrollListener scrollListener;
    private int mFragPosition;

    private SwipeRefreshLayout srRootView;

    public ArticlesFragment() {
    }

    public static ArticlesFragment getInstance(int position) {
        Bundle b = new Bundle();
        b.putInt(POSITION_ARG, position);

        // Initialize new fragment
        ArticlesFragment fragment = new ArticlesFragment();
        fragment.setArguments(b);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDataManager = DataManager.getInstance();
        mNetManager = NetworkManager.getInstance(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        srRootView = (SwipeRefreshLayout) inflater.inflate(R.layout.content_view, container, false);
        srRootView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (mFragPosition != 0)
                    mNetManager.downloadArticles(0, true, Category.getCategory(mFragPosition));
                else
                    mNetManager.downloadArticles(0, true, null);

            }
        });

        // Setup list view and it's mAdapter
        RecyclerView rv = (RecyclerView) srRootView.findViewById(R.id.rvArticles);
        mAdapter = new ArticlesAdapter(this);
        rv.setAdapter(mAdapter);
        linearLayoutManager = new LinearLayoutManager(getContext());
        rv.setLayoutManager(linearLayoutManager);
        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, final int totalItemsCount, RecyclerView view) {
                final int curSize = mAdapter.getItemCount();
                mNetManager.downloadArticles(0, true, Category.getCategory(mFragPosition));
                view.post(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.notifyItemRangeInserted(curSize, mDataManager.getArticlesForPosition(mFragPosition).size() -1);
                    }
                });
            }
        };
       rv.addOnScrollListener(scrollListener);

        // Set mAdapter data
        Bundle a = getArguments();
        mFragPosition = a.getInt(POSITION_ARG);
        mAdapter.setData(mDataManager.getArticlesForPosition(mFragPosition));
        return srRootView;
    }

    @Override
    public void onItemClicked(long articleId) {
        Intent intent = new Intent(getContext(), ArticleViewActivity.class);
        intent.putExtra(ArticleViewActivity.EXTRA_ARTICLE_ID, articleId);
        startActivity(intent);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onListDownloadEvent(ListDownloadedEvent event) {
        mAdapter.setData(mDataManager.getArticlesForPosition(mFragPosition));
        srRootView.setRefreshing(false);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        // Try refreshing data if it's downloaded
        mAdapter.setData(mDataManager.getArticlesForPosition(mFragPosition));
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

}
