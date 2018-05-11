package com.dante.girl.picture;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.dante.girl.R;
import com.dante.girl.base.BaseFragment;
import com.dante.girl.base.Constants;
import com.dante.girl.helper.BlurBuilder;
import com.dante.girl.lib.TouchImageView;
import com.dante.girl.model.DataBase;
import com.dante.girl.model.Image;
import com.dante.girl.ui.SettingFragment;
import com.dante.girl.utils.BitmapUtil;
import com.dante.girl.utils.Imager;
import com.dante.girl.utils.Share;
import com.dante.girl.utils.SpUtil;
import com.dante.girl.utils.UiUtils;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.tbruyelle.rxpermissions.RxPermissions;

import butterknife.BindView;
import rx.Observable;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;


/**
 * Photo view fragment.
 */
public class ViewerFragment extends BaseFragment implements View.OnLongClickListener, View.OnClickListener {

    public static final String DONT_HINT = "dont_hint";
    public static final String HINT = "first_hint";
    private static final String TAG = "test";
    @BindView(R.id.image)
    TouchImageView imageView;
    CompositeSubscription tasks = new CompositeSubscription();
    @BindView(R.id.likeBtn)
    LikeButton likeBtn;
    @BindView(R.id.progress)
    ProgressBar progress;
    private ViewerActivity context;
    private Bitmap bitmap;
    private String url;
    private Image image;

    public static ViewerFragment newInstance(String i) {
        ViewerFragment fragment = new ViewerFragment();
        Bundle args = new Bundle();
        args.putString(Constants.URL, i);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int initLayoutId() {
        return R.layout.fragment_viewer;
    }

    @Override
    protected void initViews() {
        context = (ViewerActivity) getActivity();
        url = getArguments().getString(Constants.URL);
        image = DataBase.getByUrl(realm, url);
        ViewCompat.setTransitionName(imageView, url);
        load();
        likeBtn.setLiked(DataBase.getByUrl(realm, url).isLiked);
        likeBtn.setOnLikeListener(new OnLikeListener() {
            @Override
            public void liked(LikeButton likeButton) {
                realm.beginTransaction();
                DataBase.getByUrl(realm, url).setLiked(true);
                realm.commitTransaction();

                if (SpUtil.getBoolean(SettingFragment.LIKE_DOWNLOAD, true)) {
                    save(bitmap);
                }
            }

            @Override
            public void unLiked(LikeButton likeButton) {
                realm.beginTransaction();
                DataBase.getByUrl(realm, url).setLiked(false);
                realm.commitTransaction();
            }
        });
    }


    private void load() {
        Log.d(TAG, "load: " + url);
        Imager.loadDefer(this, image, new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                log("load" + url + " byte counts" + resource.getByteCount());
                bitmap = resource;
                progress.setVisibility(View.GONE);
                imageView.setImageBitmap(resource);
                context.supportStartPostponedEnterTransition();
                likeBtn.animate().setDuration(400).scaleY(1).scaleX(1).start();
            }

        });
    }

    private void showHint() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.hint).
                setMessage(R.string.browse_picture_hint).
                setPositiveButton(R.string.ok, (dialogInterface, i) -> dialogInterface.dismiss()).
                create().show();
    }


    @Override
    protected void initData() {
        imageView.setOnClickListener(this);
        imageView.setOnLongClickListener(this);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public boolean onLongClick(View v) {
        blur(bitmap);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final String[] items = {getString(R.string.share_to), getString(R.string.save_img), getString(R.string.set_wallpaper)};
        builder.setItems(items, (dialog, which) -> {
            if (which == 0) {
                share(bitmap);
            } else if (which == 1) {
                save(bitmap);
            } else if (which == 2) {
                setWallpaper(bitmap);
            }
        }).setOnDismissListener(dialogInterface -> {
            imageView.setImageBitmap(bitmap);
        }).show();
        return true;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void setWallpaper(Bitmap bitmap) {
        WallpaperManager manager = WallpaperManager.getInstance(context.getApplicationContext());
        RxPermissions permissions = new RxPermissions(context);
        Subscription subscription = permissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .map(granted -> BitmapUtil.writeToFile(bitmap))
                .compose(applySchedulers())
                .subscribe(file -> {
                    if (file != null && file.exists()) {
                        Intent intent;
                        intent = manager.getCropAndSetWallpaperIntent(
                                BitmapUtil.getImageContentUri(context, file.getAbsolutePath()));
                        startActivity(intent);
                    } else {
                        UiUtils.showSnack(rootView, R.string.set_wallpaper_failed);
                    }
                }, throwable -> throwable.printStackTrace());
        tasks.add(subscription);
    }

    private void blur(Bitmap bitmap) {
        Subscription subscription = Observable.just(bitmap)
                .map(BlurBuilder::blur)
                .compose(applySchedulers())
                .subscribe(bitmap1 -> {
                    imageView.setImageBitmap(bitmap1);
                });
        tasks.add(subscription);
    }

    private void save(final Bitmap bitmap) {
        RxPermissions permissions = new RxPermissions(context);
        Subscription subscription = permissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .compose(applySchedulers())
                .subscribe(granted -> {
                    if (granted) {
                        if (BitmapUtil.writeToFile(bitmap).exists()) {
                            if (SpUtil.getBoolean(DONT_HINT)) {
                                return;
                            }
                            UiUtils.showSnack(rootView, R.string.save_img_success,
                                    R.string.dont_hint, v -> SpUtil.save(DONT_HINT, true));

                        } else {
                            UiUtils.showSnack(rootView, R.string.save_img_failed);
                        }
                    }
                }, throwable -> throwable.printStackTrace());
        tasks.add(subscription);
    }

    private void share(final Bitmap bitmap) {
        final RxPermissions permissions = new RxPermissions(context);
        Subscription subscription = permissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .map(granted -> {
                    if (granted) {
                        return BitmapUtil.bitmapToUri(bitmap);
                    }
                    return null;

                })
                .compose(applySchedulers())
                .subscribe(uri -> {
                    Share.shareImage(context, uri);
                });
        tasks.add(subscription);
    }

    @Override
    public void onClick(View v) {
        context.supportFinishAfterTransition();
    }

    public View getSharedElement() {
        return imageView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        tasks.unsubscribe();
        Glide.with(this).clear(imageView);
    }

}
