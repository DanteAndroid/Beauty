package com.dante.girl.picture;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.graphics.Palette;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.dante.girl.BuildConfig;
import com.dante.girl.R;
import com.dante.girl.lib.RatioImageView;
import com.dante.girl.model.Image;
import com.dante.girl.utils.Imager;

/**
 * Adapter of picture list.
 */

class PictureAdapter extends BaseQuickAdapter<Image, BaseViewHolder> {

    private Fragment fragment;
    private Context context;

    PictureAdapter(int layoutId, Fragment fragment) {
        super(layoutId, null);
        this.fragment = fragment;
//        setHasStableIds(true);
    }

    public static String optimizeTitle(String title) {
        return title.replace("A区：", "").replace("APIC.IN", "").replace("-A区", "").replace("APIC-IN", "")
                .replace("手机壁纸", "").replace("图片", "")
                .replace("下载", "")
                .replace("动漫", "")
                .trim();
    }

    @Override
    protected void convert(final BaseViewHolder holder, final Image image) {
        context = holder.itemView.getContext();
        final ImageView imageView = holder.getView(R.id.picture);
        if (imageView instanceof RatioImageView && image.width != 0) {
            ((RatioImageView) imageView).setOriginalSize(image.width, image.height);
        }
        ViewCompat.setTransitionName(imageView, image.url);

        final View post = holder.getView(R.id.post);
        final TextView title = holder.getView(R.id.title);
        if (post != null) {
            title.setText(optimizeTitle(image.title));
        }
        RequestListener<Bitmap> listener = new RequestListener<Bitmap>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                if (e != null && BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
                return false;
            }

            @Override
            public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                if (post != null) {
                    Palette.from(resource).generate(palette -> {
                        int color = palette.getDarkMutedColor(ContextCompat.getColor(mContext, R.color.cardview_dark_background));
                        title.setBackgroundColor(color);
                        title.setVisibility(View.VISIBLE);
                    });
                }
                return false;
            }
        };
        Imager.loadWithHeader(context, image, imageView, listener);


    }


}
