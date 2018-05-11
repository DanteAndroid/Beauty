package com.dante.girl.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.app.Fragment;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.TransitionOptions;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.dante.girl.R;
import com.dante.girl.model.Image;
import com.dante.girl.net.NetService;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * loading img encapsulation.
 */
public class Imager {
    private static final String TAG = "Imager";
    private static RequestOptions errorHolder = new RequestOptions().error(R.drawable.error_holder);
    private static RequestOptions placeHolder = new RequestOptions().error(R.drawable.placeholder);

    public static void loadDefer(final Fragment context, Image image, SimpleTarget<Bitmap> target) {
        GlideUrl glideUrl = new GlideUrl(image.url, new LazyHeaders.Builder()
                .addHeader("Referer", image.referer)
                .addHeader("User-Agent", NetService.AGENT).build());
        Glide.with(context)
                .asBitmap()
                .load(glideUrl)
                .apply(placeHolder)
                .transition(BitmapTransitionOptions.withCrossFade())
                .into(target);
    }

    public static void loadWithHeader(final Context context, Image image, ImageView target, RequestListener<Bitmap> listener) {
        boolean useThumb = (image.referer.contains("apic") | image.referer.contains("acg"))
                && image.referer.contains(".htm");
        GlideUrl glideUrl = new GlideUrl(image.url, new LazyHeaders.Builder()
                .addHeader("Referer", image.referer)
//                .addHeader("Cookie","__cfduid=dda069af78d97c98df1e460a45fd486ba1522144406")
                .addHeader("User-Agent", NetService.AGENT).build());
        Glide.with(context)
                .asBitmap()
                .load(glideUrl)
                .thumbnail(useThumb ? 1.0f : 0.6f)
                .apply(errorHolder)
                .listener(listener)
                .transition(new BitmapTransitionOptions().crossFade())
                .into(target);
    }

    public static void loadWithHeader(final Context context, Image image, ImageView target) {
        GlideUrl glideUrl = new GlideUrl(image.url, new LazyHeaders.Builder()
                .addHeader("Referer", image.referer)
                .addHeader("User-Agent", NetService.AGENT).build());

        Glide.with(context)
                .load(glideUrl)
                .apply(errorHolder)
                .into(target);
    }

    public static void load(final Context context, String url, ImageView target) {
        Glide.with(context)
                .load(url)
                .into(target);
    }

    public static void load(Context context, int resourceId, ImageView view) {
        Glide.with(context)
                .load(resourceId)
                .into(view);
    }

    public static void load2() {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request newRequest = chain.request().newBuilder()
                                .addHeader("X-TOKEN", "VAL")
                                .build();
                        return chain.proceed(newRequest);
                    }
                })
                .build();
//
//        Picasso picasso = new Picasso.Builder(context)
//                .downloader(new OkHttp3Downloader(client))
//                .build();
    }

    class MyTransition extends TransitionOptions<MyTransition, Bitmap> {

    }

}
