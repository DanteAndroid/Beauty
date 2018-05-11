package com.dante.girl.model;

import android.graphics.Bitmap;
import android.support.v4.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.target.Target;
import com.dante.girl.net.NetService;

import java.util.Date;
import java.util.concurrent.ExecutionException;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Image object for save in Realm database.
 */
public class Image extends RealmObject {
    private static final String TAG = "Image";

    public int id;
    public String type;//Gank or DB
    public Date publishedAt;
    public String info;
    public String title;
    @PrimaryKey
    public String url;
    public int width;
    public int height;
    public boolean isLiked;
    //A区的图片所在的网址
    public String referer = "";
    public boolean big;
    public int totalPage;

    public Image(String url) {
        this.url = url;
    }

    public Image() {
    }

    public Image(String url, String type) {
        this.url = url;
        this.type = type;
    }

    public Image(String url, String type, int id) {
        this.url = url;
        this.type = type;
        this.id = id;
    }

    public static Image getFixedImage(Fragment context, Image image, String type) throws ExecutionException, InterruptedException {
        image.setType(type);
        Bitmap bitmap;
        bitmap = getBitmap(context, image);
        image.setWidth(bitmap.getWidth());
        image.setHeight(bitmap.getHeight());
        return image;
    }

    public static void prefetch(Fragment context, final Image image, String type, SizeReadyCallback callback) {
        image.setType(type);
        Glide.with(context)
                .load(image.url)
                .preload()
                .getSize(callback);
    }

    public static Bitmap getBitmap(Fragment context, Image image) throws InterruptedException, ExecutionException {
        GlideUrl glideUrl = new GlideUrl(image.url, new LazyHeaders.Builder()
                .addHeader("Referer", image.referer)
                .addHeader("User-Agent", NetService.AGENT).build());
        return Glide.with(context)
                .asBitmap()
                .load(glideUrl)
                .submit(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                .get();
    }

    public int getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }

    public void setReferer(String referer) {
        this.referer = referer;
    }

    public Date getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Date publishedAt) {
        this.publishedAt = publishedAt;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }
}
