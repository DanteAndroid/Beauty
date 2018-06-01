package com.dante.girl.model;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
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
public class Image extends RealmObject implements Parcelable {
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


    public static final Parcelable.Creator<Image> CREATOR = new Parcelable.Creator<Image>() {
        @Override
        public Image createFromParcel(Parcel source) {
            return new Image(source);
        }

        @Override
        public Image[] newArray(int size) {
            return new Image[size];
        }
    };

    protected Image(Parcel in) {
        this.id = in.readInt();
        this.type = in.readString();
        long tmpPublishedAt = in.readLong();
        this.publishedAt = tmpPublishedAt == -1 ? null : new Date(tmpPublishedAt);
        this.info = in.readString();
        this.title = in.readString();
        this.url = in.readString();
        this.width = in.readInt();
        this.height = in.readInt();
        this.isLiked = in.readByte() != 0;
        this.referer = in.readString();
        this.big = in.readByte() != 0;
        this.totalPage = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.type);
        dest.writeLong(this.publishedAt != null ? this.publishedAt.getTime() : -1);
        dest.writeString(this.info);
        dest.writeString(this.title);
        dest.writeString(this.url);
        dest.writeInt(this.width);
        dest.writeInt(this.height);
        dest.writeByte(this.isLiked ? (byte) 1 : (byte) 0);
        dest.writeString(this.referer);
        dest.writeByte(this.big ? (byte) 1 : (byte) 0);
        dest.writeInt(this.totalPage);
    }
}
