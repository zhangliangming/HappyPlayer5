package com.zlm.hp.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

/**
 * Created by KathLine on 2017/12/12.
 */

public class GlideLoader {

    private Object mImageUrlObj;
    private  RequestOptions mRequestOptions;

    public static GlideLoader init() {
        return new GlideLoader();
    }

    private GlideLoader(){}

    public GlideLoader load(@Nullable Object url) {
        mImageUrlObj = url;
        return this;
    }

    public GlideLoader applyDefault(int placeholderId, int errorId) {
        mRequestOptions = new RequestOptions()
                .centerCrop()
                .priority(Priority.HIGH)
                .diskCacheStrategy(DiskCacheStrategy.ALL);
        if(placeholderId != 0) {
            mRequestOptions.placeholder(placeholderId);
        }
        if(errorId != 0) {
            mRequestOptions.error(errorId);
        }
        return this;
    }

    public GlideLoader applyDefault(int errorId) {
        return applyDefault(0, errorId);
    }

    public GlideLoader applyDefault() {
        return applyDefault(0, 0);
    }

    /**
     * 例子
     * <pre>
     RequestOptions options = new RequestOptions()
     .centerCrop()
     .priority(Priority.HIGH)
     .diskCacheStrategy(DiskCacheStrategy.ALL);
     * </pre>
     * @param requestOptions
     * @return
     */
    public GlideLoader apply(@NonNull RequestOptions requestOptions) {
        mRequestOptions = requestOptions;
        return this;
    }

    public GlideLoader bitmapTransform(Transformation<Bitmap> transformation) {
        apply(RequestOptions.bitmapTransform(transformation));
        return this;
    }


    /**
     * 最后调用
     * @param context
     * @param listener
     * @return
     */
    public RequestBuilder<Drawable> listener(Context context, RequestListener<Drawable> listener) {
        return Glide.with(context)
                .load(mImageUrlObj)
                .apply(mRequestOptions)
                .listener(listener);
    }

    /**
     * 最后调用
     * @param imageView
     * @return
     */
    public Target<Drawable> into(ImageView imageView) {
        return Glide.with(imageView.getContext())
                .load(mImageUrlObj)
                .apply(mRequestOptions)
                .into(imageView);
    }

    public Target<Drawable> into(Context context, Target<Drawable> target){
        return Glide.with(context)
                .load(mImageUrlObj)
                .apply(mRequestOptions)
                .into(target);
    }

}
