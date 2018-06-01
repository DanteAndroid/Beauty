package com.dante.girl.picture;


import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.widget.FrameLayout;

import com.dante.girl.R;
import com.dante.girl.base.BaseFragment;
import com.dante.girl.base.Constants;
import com.dante.girl.utils.SpUtil;

import butterknife.BindView;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

/**
 * All fragments have recyclerView & swipeRefresh must implement this.
 */
public abstract class RecyclerFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {
    @BindView(R.id.list)
    RecyclerView recyclerView;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefresh;
    boolean refresh;   //whether is first time to enter fragment
    String imageType;               // imageType of recyclerView's content
    int lastPosition;       //last visible position
    int firstPosition;      //first visible position
    Subscription subscription;
    CompositeSubscription compositeSubscription = new CompositeSubscription();
    @BindView(R.id.progress)
    FrameLayout progress;

    @Override
    protected int initLayoutId() {
        return R.layout.fragment_recycler;
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState == null) {
            //restoring position when reentering app.
            lastPosition = SpUtil.getInt(imageType + Constants.POSITION);
            if (lastPosition > 0) {
                log("restore lastPosition", lastPosition);
                recyclerView.smoothScrollToPosition(lastPosition);
            }
        }
    }

    @Override
    public void onDestroy() {
        compositeSubscription.unsubscribe();
        super.onDestroy();
    }

    @Override
    protected void initViews() {
        recyclerView.setHasFixedSize(true);
        swipeRefresh.setColorSchemeColors(getColor(R.color.colorPrimary),
                getColor(R.color.colorPrimaryDark), getColor(R.color.colorAccent));

        swipeRefresh.setOnRefreshListener(this);
    }

    @Override
    protected void initData() {

    }

    @Override
    public void onResume() {
        super.onResume();
        int viewPosition = SpUtil.getInt(imageType + Constants.VIEW_POSITION);
        if (viewPosition > 0) {
            recyclerView.smoothScrollToPosition(viewPosition);
            SpUtil.remove(imageType + Constants.VIEW_POSITION);
        }
    }

    public void changeRefresh(final boolean refreshState) {
        if (null != swipeRefresh) {
            swipeRefresh.setRefreshing(refreshState);
        }
    }

    public int getColor(int resId) {
        return ResourcesCompat.getColor(getResources(), resId, null);
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

}
