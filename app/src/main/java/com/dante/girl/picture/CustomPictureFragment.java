package com.dante.girl.picture;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.transition.TransitionManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chad.library.adapter.base.loadmore.LoadMoreView;
import com.dante.girl.MainActivity;
import com.dante.girl.R;
import com.dante.girl.base.Constants;
import com.dante.girl.model.DataBase;
import com.dante.girl.model.Image;
import com.dante.girl.net.API;
import com.dante.girl.net.DataFetcher;
import com.dante.girl.utils.SpUtil;
import com.dante.girl.utils.UiUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import io.realm.RealmResults;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

import static com.dante.girl.net.API.TYPE_A_ANIME;
import static com.dante.girl.net.API.TYPE_A_FULI;
import static com.dante.girl.net.API.TYPE_A_HENTAI;
import static com.dante.girl.net.API.TYPE_A_UNIFORM;
import static com.dante.girl.net.API.TYPE_A_YSJ;
import static com.dante.girl.net.API.TYPE_A_ZATU;
import static com.dante.girl.net.API.TYPE_DB_BREAST;
import static com.dante.girl.net.API.TYPE_DB_BUTT;
import static com.dante.girl.net.API.TYPE_DB_LEG;
import static com.dante.girl.net.API.TYPE_DB_RANK;
import static com.dante.girl.net.API.TYPE_DB_SILK;
import static com.dante.girl.net.API.TYPE_HIDE;
import static com.dante.girl.net.API.TYPE_MZ_INNOCENT;
import static com.dante.girl.net.API.TYPE_MZ_JAPAN;
import static com.dante.girl.net.API.TYPE_MZ_SEXY;
import static com.dante.girl.net.API.TYPE_MZ_TAIWAN;

/**
 * Custom appearance of picture list fragment
 */

public class CustomPictureFragment extends PictureFragment {
    private static final String TAG = "CustomPictureFragment";
    private static final int BUFFER_SIZE = 4;
    boolean inPicturePost;
    boolean isPostSite;
    private boolean firstPage;
    private int size;
    private int old;
    private int error;
    private boolean isGank;
    private boolean isDB;
    private boolean isMZ;

    public static CustomPictureFragment newInstance(String type) {
        Bundle args = new Bundle();
        args.putString(Constants.TYPE, type);
        CustomPictureFragment fragment = new CustomPictureFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public void addInfo(String info) {
        this.info = info;
        inPicturePost = true;
    }

    @Override
    protected void initViews() {
        super.initViews();
        imageType = TextUtils.isEmpty(info) ? baseType : info;
    }

    @Override
    protected void onImageClicked(View view, int position) {
        if (isFetching) {
            return;
        }
        if (isPostSite && !inPicturePost) {
            startPost(getImage(position));
            return;
        }
        super.onImageClicked(view, position);
    }

    @Override
    protected int initAdapterLayout() {
        int layout = R.layout.picture_item;
        switch (baseType) {
            case TYPE_MZ_INNOCENT:
            case TYPE_MZ_JAPAN:
            case TYPE_MZ_SEXY:
            case TYPE_MZ_TAIWAN:
                isPostSite = true;
                layout = R.layout.post_item;
                if (inPicturePost) {
                    layout = R.layout.picture_item_fixed;
                }
                break;
            case TYPE_A_ANIME:
            case TYPE_A_FULI:
            case TYPE_A_HENTAI:
            case TYPE_A_ZATU:
            case TYPE_A_UNIFORM:
            case TYPE_A_YSJ:
                isPostSite = true;
                layout = R.layout.post_item_a;
                if (inPicturePost) {
                    layout = R.layout.picture_item_fixed;
                }
                break;
        }
        return layout;
    }

    @Override
    public void fetch() {
        if (isFetching) {
            Log.e(TAG, "fetch: isFetching. return");
            return;
        }
        firstPage = page <= 1;
        DataFetcher fetcher;
        Observable<List<Image>> source;
        switch (baseType) {
            case TYPE_DB_BREAST:
            case TYPE_DB_BUTT:
            case TYPE_DB_LEG:
            case TYPE_DB_SILK:
            case TYPE_DB_RANK:
                isDB = true;
                url = API.DB_BASE;
                fetcher = new DataFetcher(url, imageType, refresh ? 1 : page);
                source = fetcher.getDouban();
                break;

            case TYPE_A_ANIME:
            case TYPE_A_FULI:
            case TYPE_A_HENTAI:
            case TYPE_A_ZATU:
            case TYPE_A_UNIFORM:
            case TYPE_A_YSJ:
                url = API.A_BASE;
                fetcher = new DataFetcher(url, imageType, refresh ? 1 : page);
                source = inPicturePost ? fetcher.getPicturesOfPost(url, info) : fetcher.getPosts(url);
                break;
            case TYPE_MZ_INNOCENT:
            case TYPE_MZ_JAPAN:
            case TYPE_MZ_SEXY:
            case TYPE_MZ_TAIWAN:
                url = API.MZ_BASE;
                isMZ = true;
                fetcher = new DataFetcher(url, imageType, refresh ? 1 : page);
                Log.d(TAG, "fetch: " + inPicturePost);
                source = inPicturePost ? fetcher.getPicturesOfPost(url, info) : fetcher.getPosts(url);
                break;
            case TYPE_HIDE:
                source = Observable.empty();
                TextView empty = new TextView(getActivity());
                empty.setText(R.string.type_hide_hint);
                empty.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
                adapter.setEmptyView(empty);
                break;
            default://imageType = 0, 代表GANK
                url = API.GANK;
                isGank = true;
                fetcher = new DataFetcher(url, imageType, refresh ? 1 : page);
                source = fetcher.getGank();
                break;
        }
        fetchImages(source);
    }

    //预加载Image，然后刷新列表
    protected void fetchImages(final Observable<List<Image>> source) {
        subscription = source
                .observeOn(Schedulers.io())
                .filter(images -> {
//                    if (inPicturePost && images.size() == 0) {
//                        context.runOnUiThread(() -> {
//                            UiUtils.showSnackLong(rootView, R.string.no_picture_maybe_need_login);
//                            adapter.loadMoreEnd();
//                        });
//                    }
                    return images.size() > 0;
                })
                .flatMap(Observable::from)
                .map(image -> {
                    if ((isGank | isDB) && image.width == 0) {
                        try {
                            image = Image.getFixedImage(this, image, imageType);
                            if (!isGank && !DataBase.hasImage(image.url) && image.publishedAt == null) {
                                Date date = new Date();
                                if (page <= 1) {
                                    //如果是刷新，则把新图片的日期都往前设10s
                                    Image i = new Image();
                                    i.setPublishedAt(date);
                                    RealmResults<Image> images = DataBase.findImages(null, imageType);
                                    Date d = images.first(i).getPublishedAt();
                                    if (d != null) {
                                        d.setTime(d.getTime() - 10000);
                                    }
                                    date = d;
                                }
                                image.setPublishedAt(date);
                            }
                        } catch (ExecutionException | InterruptedException e) {
                            Log.d(TAG, "fetchImages: " + e.getMessage());
                        }
                        if (inPicturePost) {
                            image.big = true;
                        }
                    }
                    return image;
                })
                .buffer(BUFFER_SIZE)
                .compose(applySchedulers())
                .subscribe(new Subscriber<List<Image>>() {
                    int oldSize;
                    List<Image> newData;

                    @Override
                    public void onStart() {
                        if (noCache) {
                            newData = new ArrayList<>();
                        } else {
                            oldSize = images.size();
                        }
                    }

                    @Override
                    public void onCompleted() {
                        changeState(false);
                        if (noCache) {
                            if (page > 1) {
                                if (inPicturePost) adapter.loadMoreEnd();
                            } else {
                                log("onCompleted " + data.size() + " " + newData.size() + " " + getNewData(data, newData).size());
                                newData = getNewData(data, newData);
                                if (!newData.isEmpty()) {
                                    adapter.addData(0, newData);
                                }
                            }
                        } else {
                            images = DataBase.findImages(realm, imageType);
                            int add = images.size() - oldSize;
                            if (add > 0) {
                                if (page == 1) {
                                    adapter.setNewData(images);
                                } else {
                                    adapter.notifyItemRangeInserted(oldSize, add);
                                }
                            } else {
                                if (page > 1 && inPicturePost) adapter.loadMoreEnd();
                            }
                        }
                        adapter.setEmptyView(R.layout.empty);
                        if (inPicturePost) {
                            adapter.getEmptyView().findViewById(R.id.empty).setBackgroundColor(getColor(android.R.color.black));
                            TextView textView = adapter.getEmptyView().findViewById(R.id.hint);
                            textView.setTextColor(getColor(android.R.color.secondary_text_dark));
                        }
                        if (!refresh) {
                            page++;
                        }
                        adapter.loadMoreComplete();
                        if (refresh) refresh = false;
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (refresh) refresh = false;
                        changeState(false);
                        error++;
                        if (isMaxPage()) {
                            adapter.loadMoreEnd(true);
                        } else {
                            adapter.loadMoreFail();
                            if (error > 2) {
                                UiUtils.showSnackLong(rootView, R.string.net_error);
                            } else {
                                UiUtils.showSnack(rootView, R.string.load_fail);
                            }
                        }
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(List<Image> list) {
                        if (noCache) {
                            if (refresh) {
                                newData.addAll(list);
                            } else {
                                adapter.addData(list);
                            }
                        } else {
                            log(" save ");
                            DataBase.save(realm, list);
                        }
                    }
                });
        compositeSubscription.add(subscription);
    }

    public List<Image> getNewData(List<Image> old, List<Image> list) {
        if (old.isEmpty()) return list;
        List<Image> newData = new ArrayList<>();
        boolean contains = false;
        for (Image image : list) {
            for (int i = 0; i < old.size(); i++) {
                if (old.get(i).url.equals(image.url)) {
                    contains = true;
                }
            }
            if (!contains) {
                newData.add(image);
            }
        }
        return newData;
    }

    @Override
    protected void onCreateView() {
        super.onCreateView();
        if (isPostSite) recyclerView.setBackgroundColor(getColor(R.color.black_back));
    }

//    @Override
//    public void onDestroyView() {
//        if (adapter.isLoading()) {
//            adapter.loadMoreComplete();
//        }
//        super.onDestroyView();
//    }

    public boolean isMaxPage() {
        if (noCache) {
            return !data.isEmpty() && page > data.get(0).totalPage && data.get(0).totalPage > 0;
        }
        return !images.isEmpty() && page > images.first().totalPage && images.first().totalPage > 0;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);//fragment被show或者hide时调用
        if (!hidden && context != null) {
            setupToolbar();
            ((MainActivity) context).changeDrawer(!inPicturePost);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        ((MainActivity) context).changeDrawer(!inPicturePost);
    }

    private void setupToolbar() {
        //在A区帖子中，改变toolbar的样式
        AppBarLayout.LayoutParams p = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
        p.setScrollFlags(0);
        if (inPicturePost) {
            ((MainActivity) context).changeNavigator(false);
            context.setToolbarTitle(title);
        } else {
//            p.setScrollFlags(SCROLL_FLAG_SCROLL | SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED);
            ((MainActivity) context).changeNavigator(true);
            context.setToolbarTitle(((MainActivity) context).getCurrentMenuTitle());
        }
        toolbar.setLayoutParams(p);
        toolbar.setOnClickListener(v -> recyclerView.smoothScrollToPosition(0));
    }

    private void startPost(Image image) {
        FragmentTransaction transaction = context.getSupportFragmentManager().beginTransaction();
        CustomPictureFragment fragment = CustomPictureFragment.newInstance(imageType);
        fragment.addInfo(image.info);//帖子地址，也是imageType
        fragment.setTitle(image.title);//用于toolbar的标题
        transaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right
                , android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        transaction.hide(this);
        transaction.add(R.id.container, fragment, "aPost");
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (!noCache) {
            SpUtil.save(imageType + Constants.PAGE, page);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void initData() {
        super.initData();
        setupToolbar();
        TransitionManager.beginDelayedTransition((ViewGroup) rootView);
        if (isPostSite || inPicturePost) {
            adapter.setLoadMoreView(new LoadMoreView() {
                @Override
                public int getLayoutId() {
                    return R.layout.loading;
                }

                @Override
                protected int getLoadingViewId() {
                    return R.id.loading;
                }

                @Override
                protected int getLoadFailViewId() {
                    return R.id.loadFailed;
                }

                @Override
                protected int getLoadEndViewId() {
                    return R.id.loadEnd;
                }
            });
        }
        if (noCache) {
            fetch();
            changeState(true);
        } else {
            page = SpUtil.getInt(imageType + Constants.PAGE, 1);
            if (images.isEmpty()) {
                fetch();
                changeState(true);
            }
        }
        setLoadMore();
    }

    @Override
    public void changeState(boolean fetching) {
        isFetching = fetching;
        changeRefresh(isFetching);
    }

    private void setLoadMore() {
        adapter.setOnLoadMoreListener(() -> {
            if (!isFetching) {
                if (isMaxPage()) {
                    adapter.loadMoreEnd();
                } else {
                    fetch();
                    isFetching = true;
                }
            }

        }, recyclerView);
    }

//    @Override
//    public void onChange(RealmResults<Image> collection, OrderedCollectionChangeSet changeSet) {
//        // `null`  means the async query returns the first time.
//        if (isDB) {
//            return;
//        }
//        if (changeSet == null) {
//            adapter.notifyDataSetChanged();
//            return;
//        }
//        // For deletions, the adapter has to be notified in reverse order.
//        OrderedCollectionChangeSet.Range[] deletions = changeSet.getDeletionRanges();
//        for (int i = deletions.length - 1; i >= 0; i--) {
//            OrderedCollectionChangeSet.Range range = deletions[i];
//            adapter.notifyItemRangeRemoved(range.startIndex, range.length);
//        }
//
//        OrderedCollectionChangeSet.Range[] insertions = changeSet.getInsertionRanges();
//        for (OrderedCollectionChangeSet.Range range : insertions) {
//            adapter.notifyItemRangeInserted(range.startIndex, range.length);
//            if (page == 1) {
//                recyclerView.scrollToPosition(0);
//            }
//        }
//
////        OrderedCollectionChangeSet.Range[] modifications = changeSet.getChangeRanges();
////        for (OrderedCollectionChangeSet.Range range : modifications) {
////            log("no notifyItemRangeChanged " + range.startIndex + " to " + range.length);
////            adapter.notifyItemRangeChanged(range.startIndex, range.length);
////        }
//    }
}
