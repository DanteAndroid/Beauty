package com.dante.girl.picture;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.FrameLayout;

import com.dante.girl.R;
import com.dante.girl.base.BaseActivity;
import com.dante.girl.base.Constants;
import com.dante.girl.model.DataBase;
import com.dante.girl.model.Image;
import com.dante.girl.utils.SpUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import io.realm.RealmChangeListener;

@TargetApi(Build.VERSION_CODES.KITKAT)
public class ViewerActivity extends BaseActivity implements RealmChangeListener {
    private static final String TAG = "DetailActivity";
    private static final int SYSTEM_UI_SHOW = View.SYSTEM_UI_FLAG_LAYOUT_STABLE;

    private static final int SYSTEM_UI_HIDE = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_FULLSCREEN;

    @BindView(R.id.pager)
    ViewPager pager;
    @BindView(R.id.container)
    FrameLayout container;

    private DetailPagerAdapter adapter;
    private int currentPosition;
    private String type;
    private List<Image> images;
    private boolean isSystemUiShown = true;
    private int position;

    @Override
    protected void onPause() {
        SpUtil.save(type + Constants.VIEW_POSITION, currentPosition);
        super.onPause();
    }

    @Override
    protected int initLayoutId() {
        return R.layout.activity_detail;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void initViews(@Nullable Bundle savedInstanceState) {
        super.initViews(savedInstanceState);
        supportPostponeEnterTransition();
        position = getIntent().getIntExtra(Constants.POSITION, 0);
        currentPosition = position;
        List<Fragment> fragments = new ArrayList<>();

        if (DataBase.noCache()) {
            images = getIntent().getParcelableArrayListExtra(Constants.DATA);
        } else {
            type = getIntent().getStringExtra(Constants.TYPE);
            images = DataBase.findImages(realm, type);
        }

        for (int i = 0; i < images.size(); i++) {
            fragments.add(ViewerFragment.newInstance(images.get(i)));
        }
        adapter = new DetailPagerAdapter(getSupportFragmentManager(), fragments);
        pager.setAdapter(adapter);
        pager.setCurrentItem(position);
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                currentPosition = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        supportFinishAfterTransition();
    }

    @Override
    public void supportFinishAfterTransition() {
//        EventBus.getDefault().post(new MessageEvent(currentPosition));
        if (isPositionChanged()) {
            finish();
        } else {
            super.supportFinishAfterTransition();
        }
    }


    private boolean isPositionChanged() {
        return position != currentPosition;
    }

    public String currentUrl() {
        return images.get(currentPosition).url;
    }

    public void toggleSystemUI() {
        if (isSystemUiShown) {
            hideSystemUi();
        } else {
            showSystemUi();
        }
    }

    public void showSystemUi() {
        pager.setSystemUiVisibility(SYSTEM_UI_SHOW);
        isSystemUiShown = true;
    }

    public void hideSystemUi() {
        pager.setSystemUiVisibility(SYSTEM_UI_HIDE);
        isSystemUiShown = false;
    }

    @Override
    public void onChange(Object element) {
        adapter.notifyDataSetChanged();
    }


    private static class DetailPagerAdapter extends FragmentStatePagerAdapter {
        private List<Fragment> fragments;

        DetailPagerAdapter(FragmentManager fm, List<Fragment> fragments) {
            super(fm);
            this.fragments = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
//
//        ViewerFragment getCurrent() {
//            return (ViewerFragment) adapter.instantiateItem(pager, pager.getCurrentItem());
//        }
    }
}
