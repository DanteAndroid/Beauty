package com.dante.girl.picture;

import android.support.design.widget.AppBarLayout;

import com.dante.girl.R;
import com.dante.girl.base.Constants;
import com.dante.girl.model.DataBase;
import com.dante.girl.utils.UiUtils;

/**
 * A picture fragment for users' liked items.
 */

public class FavoriteFragment extends PictureFragment {

//    @Override
//    protected int initAdapterLayout() {
//        return R.layout.picture_item_fixed;
//    }

    @Override
    public void fetch() {
        adapter.notifyDataSetChanged();
        changeState(false);
    }

    @Override
    protected void onCreateView() {
        super.onCreateView();
        AppBarLayout.LayoutParams p = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
        p.setScrollFlags(0);
        toolbar.setLayoutParams(p);
//        setRetainInstance(true);
    }

    @Override
    protected void initData() {
        super.initData();
        imageType = Constants.FAVORITE;
        images = DataBase.findImages(realm, imageType);
        data = realm.copyFromRealm(images);
        adapter.setNewData(images);
        if (images.isEmpty()) {
            if (isHidden()) {
                return;
            }
            UiUtils.showSnack(rootView, R.string.images_empty);
        }
        adapter.setEmptyView(R.layout.empty);
    }


}
