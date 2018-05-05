package com.zlm.hp.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.util.LruCache;
import android.view.Display;
import android.view.WindowManager;
import android.widget.ImageView;

import com.zlm.hp.application.HPApplication;
import com.zlm.hp.constants.ResourceConstants;
import com.zlm.hp.db.SongSingerDB;
import com.zlm.hp.libs.widget.CircleImageView;
import com.zlm.hp.model.SongSingerInfo;
import com.zlm.hp.net.api.SearchArtistPicUtil;
import com.zlm.hp.net.api.SearchSingerImgHttpUtil;
import com.zlm.hp.net.entity.SearchArtistPicResult;
import com.zlm.hp.net.entity.SearchSingerImgResult;
import com.zlm.hp.receiver.AudioBroadcastReceiver;
import com.zlm.hp.ui.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import static org.apache.log4j.lf5.util.StreamUtils.getBytes;

/**
 * Created by zhangliangming on 2017/7/30.
 */
public class ImageUtil {

    ;
    // 缓存
    public static LruCache<String, Bitmap> sImageCache = getImageCache();

    /**
     * 初始化图片内存
     */
    private static LruCache<String, Bitmap> getImageCache() {
        // 获取系统分配给每个应用程序的最大内存，每个应用系统分配32M
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int mCacheSize = maxMemory / 8;
        // 给LruCache分配1/8 4M
        LruCache<String, Bitmap> sImageCache = new LruCache<String, Bitmap>(
                mCacheSize) {

            // 必须重写此方法，来测量Bitmap的大小
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight();
            }
        };

        return sImageCache;
    }

    /**
     * 加载歌手图片
     *
     * @param context
     * @param imageView
     * @param singerName
     */
    public static void loadSingerImage(final HPApplication hPApplication, final Context context, final CircleImageView imageView, final String singerName) {
        //多个歌手，则取第一个歌手头像
        String regex = "、";
        String searchSingerName = singerName;
        if (singerName.contains(regex)) {
            searchSingerName = singerName.split(regex)[0];
        }

        final String filePath = ResourceFileUtil.getFilePath(context, ResourceConstants.PATH_SINGER, searchSingerName + File.separator + searchSingerName + ".jpg");
        final String key = filePath.hashCode() + "";
        //如果当前的图片与上一次一样，则不操作
        if (imageView.getTag() != null && imageView.getTag().equals(key)) {
            return;
        }

        //
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.singer_def);
        imageView.setImageDrawable(new BitmapDrawable(bitmap));


        imageView.setTag(key);
        final String finalSearchSingerName = searchSingerName;
        new AsyncTask<String, Integer, Bitmap>() {

            @Override
            protected Bitmap doInBackground(String... params) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Bitmap bitmap = null;
                if (sImageCache.get(key) != null) {
                    bitmap = sImageCache.get(key);
                }
                //从本地文件中获取
                if (bitmap == null && filePath != null) {
                    bitmap = getImageFormFile(filePath, context);
                }
                //从网络上面获取
                if (bitmap == null) {

                    SearchSingerImgResult searchSingerImgResult = SearchSingerImgHttpUtil.searchSingerImg(hPApplication, context, finalSearchSingerName);
                    if (searchSingerImgResult == null) {
                        return null;
                    }
                    String imgUrl = searchSingerImgResult.getImgUrl();
                    bitmap = getImageFormUrl(filePath, imgUrl, context);
                }
                return bitmap;
            }

            @SuppressLint("NewApi")
            @Override
            protected void onPostExecute(Bitmap result) {

                if (result != null) {
                    if (imageView.getTag() != null && imageView.getTag().equals(key)) {
                        imageView.setImageDrawable(new BitmapDrawable(result));
                    }
                    if (sImageCache.get(key) == null) {
                        sImageCache.put(key, result);
                    }

                } else {
                    imageView.setTag(null);
                }
                //发送歌手头像加载成功广播

                Intent downloadingIntent = new Intent(AudioBroadcastReceiver.ACTION_SINGERPICLOADED);
                downloadingIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                context.sendBroadcast(downloadingIntent);

                //
            }

        }.execute("");


    }

    /**
     * 获取转换颜色的图标
     *
     * @param imageView
     * @param resourceId     图片资源id
     * @param translateColor 需要转换成的颜色
     * @return
     */
    public static void getTranslateColorImg(final Context context, final ImageView imageView, final int resourceId, final int translateColor) {

        final String key = resourceId + "";

        new AsyncTask<String, Integer, Bitmap>() {

            @Override
            protected Bitmap doInBackground(String... params) {

                if (sImageCache.get(key) != null) {
                    return sImageCache.get(key);
                }

                Bitmap baseBitmap = BitmapFactory.decodeResource(context.getResources(), resourceId);

                Bitmap defBitmap = Bitmap.createBitmap(baseBitmap.getWidth(),
                        baseBitmap.getHeight(), baseBitmap.getConfig());
                Canvas pCanvas = new Canvas(defBitmap);
                Paint paint = new Paint();
                paint.setDither(true);
                paint.setAntiAlias(true);

                float progressR = Color.red(translateColor) / 255f;
                float progressG = Color.green(translateColor) / 255f;
                float progressB = Color.blue(translateColor) / 255f;
                float progressA = Color.alpha(translateColor) / 255f;

                // 根据SeekBar定义RGBA的矩阵
                float[] src = new float[]{progressR, 0, 0, 0, 0, 0, progressG, 0,
                        0, 0, 0, 0, progressB, 0, 0, 0, 0, 0, progressA, 0};
                // 定义ColorMatrix，并指定RGBA矩阵
                ColorMatrix colorMatrix = new ColorMatrix();
                colorMatrix.set(src);
                // 设置Paint的颜色
                paint.setColorFilter(new ColorMatrixColorFilter(src));
                // 通过指定了RGBA矩阵的Paint把原图画到空白图片上
                pCanvas.drawBitmap(baseBitmap, new Matrix(), paint);


                return defBitmap;
            }

            @SuppressLint("NewApi")
            @Override
            protected void onPostExecute(Bitmap result) {

                if (result != null) {
                    imageView.setImageDrawable(new BitmapDrawable(result));

                    if (sImageCache.get(key) == null) {
                        sImageCache.put(key, result);
                    }
                }
            }
        }.execute("");
    }


    /**
     * 获取通知栏图标
     *
     * @param hPApplication
     * @param context
     * @param singerName
     * @return
     */

    public static Bitmap getNotifiIcon(HPApplication hPApplication, Context context, String singerName) {
        //多个歌手，则取第一个歌手头像
        String regex = "、";
        String searchSingerName = singerName;
        if (singerName.contains(regex)) {
            searchSingerName = singerName.split(regex)[0];
        }

        final String filePath = ResourceFileUtil.getFilePath(context, ResourceConstants.PATH_SINGER, searchSingerName + File.separator + searchSingerName + ".jpg");
        final String key = filePath.hashCode() + "";

        Bitmap bitmap = null;
        if (sImageCache.get(key) != null) {
            bitmap = sImageCache.get(key);
        }
        //从本地文件中获取
        if (bitmap == null && filePath != null) {
            bitmap = getImageFormFile(filePath, context);
        }
        if (sImageCache.get(key) == null && bitmap != null) {
            sImageCache.put(key, bitmap);
        }
        return bitmap;
    }

    /**
     * 获取歌手写真图片
     *
     * @param hPApplication
     * @param context
     * @param hash
     * @param singerName
     * @return
     */
    public static Bitmap getSingerImgBitmap(final HPApplication hPApplication, final Context context, final String hash, final String singerName, String imgUrl, boolean maybeFormNet) {
        final String filePath = ResourceFileUtil.getFilePath(context, ResourceConstants.PATH_SINGER, singerName + File.separator + imgUrl.hashCode() + ".jpg");
        final String key = filePath.hashCode() + "";

        Bitmap bitmap = null;
        if (sImageCache.get(key) != null) {
            bitmap = sImageCache.get(key);
        }
        //从本地文件中获取
        if (bitmap == null && filePath != null) {
            bitmap = getImageFormFile(filePath, context);
        }
        //从网络上面获取
        if (bitmap == null && maybeFormNet) {
            bitmap = getImageFormUrl(filePath, imgUrl, context);
        }

        if (sImageCache.get(key) == null && bitmap != null) {
            sImageCache.put(key, bitmap);
        }
        return bitmap;
    }

    /**
     * 加载歌手写真
     *
     * @param hPApplication
     * @param context
     * @param hash
     */
    public static void loadSingerImg(final HPApplication hPApplication, final Context context, final String hash, final String singerName) {
        new AsyncTaskUtil() {
            @Override
            protected void onPostExecute(Void aVoid) {

                Intent loadedIntent = new Intent(AudioBroadcastReceiver.ACTION_SINGERIMGLOADED);
                loadedIntent.putExtra("hash", hash);
                loadedIntent.putExtra("singerName", singerName);
                loadedIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                context.sendBroadcast(loadedIntent);
            }

            @Override
            protected Void doInBackground(String... strings) {
                WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                Display display = wm.getDefaultDisplay();
                int screensWidth = display.getWidth();
                int screensHeight = display.getHeight();

                String[] singerNameArray = null;
                if (singerName.contains("、")) {

                    String regex = "\\s*、\\s*";
                    singerNameArray = singerName.split(regex);


                } else {
                    singerNameArray = new String[1];
                    singerNameArray[0] = singerName;
                }

                List<SongSingerInfo> list = SongSingerDB.getSongSingerDB(context).getAllSingerImg(singerNameArray, true);
                if (list == null || list.size() == 0) {

                    for (int i = 0; i < singerNameArray.length; i++) {
                        String searchSingerName = singerNameArray[i];
                        if (SongSingerDB.getSongSingerDB(context).getAllImgUrlCount(searchSingerName) > 0) {
                            continue;
                        }
                        List<SearchArtistPicResult> datas = SearchArtistPicUtil.searchArtistPic(hPApplication, context, searchSingerName, screensWidth + "", screensHeight + "", "app");
                        if (datas != null && datas.size() > 0)
                            for (int j = 0; j < datas.size(); j++) {
                                if (j > 3) {
                                    break;
                                }
                                SearchArtistPicResult searchArtistPicResult = datas.get(j);
                                if (!SongSingerDB.getSongSingerDB(context).isExists(hash, searchArtistPicResult.getImgUrl())) {

                                    SongSingerInfo songSingerInfo = new SongSingerInfo();
                                    songSingerInfo.setHash(hash);
                                    songSingerInfo.setImgUrl(searchArtistPicResult.getImgUrl());
                                    songSingerInfo.setSingerName(searchSingerName);

                                    SongSingerDB.getSongSingerDB(context).add(songSingerInfo);

                                    list.add(songSingerInfo);
                                }
                            }
                    }

                }
                //预加载图片
                for (int i = 0; i < list.size(); i++) {
                    SongSingerInfo songSingerInfo = list.get(i);
                    getSingerImgBitmap(hPApplication, context, songSingerInfo.getHash(), songSingerInfo.getSingerName(), songSingerInfo.getImgUrl(), true);
                }

                return super.doInBackground(strings);
            }
        }.execute("");
    }

    /**
     * 加载图片
     *
     * @param context
     * @param imageView 图片视图
     * @param filePath  文件路径
     * @param imgUrl    图片网址
     */
    public static void loadImage(final Context context, final ImageView imageView, final String filePath, final String imgUrl, final int resid) {
        imageView.setBackgroundResource(resid);
        loadImageData(context, imageView, filePath, imgUrl, filePath.hashCode() + "", null);
    }

    /**
     * 加载图片数据
     *
     * @param context
     * @param imageView
     * @param filePath
     * @param imgUrl
     * @param key
     * @return
     */
    private static void loadImageData(final Context context, final ImageView imageView, final String filePath, final String imgUrl, final String key, final ImageLoadCallBack imageLoadCallBack) {

        //1.本地缓存中获取图片
        //2.从本地文件中获取
        //3.从网络中获取图片
        imageView.setTag(key);
        new AsyncTask<String, Integer, Bitmap>() {

            @Override
            protected Bitmap doInBackground(String... params) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Bitmap bitmap = null;
                if (sImageCache.get(key) != null) {
                    bitmap = sImageCache.get(key);
                }
                //从本地文件中获取
                if (bitmap == null && filePath != null) {
                    bitmap = getImageFormFile(filePath, context);
                }
                //从网络上面获取
                if (bitmap == null && imgUrl != null) {
                    bitmap = getImageFormUrl(filePath, imgUrl, context);
                }
                return bitmap;
            }

            @SuppressLint("NewApi")
            @Override
            protected void onPostExecute(Bitmap result) {

                if (result != null) {
                    if (imageView.getTag().equals(key))
                        imageView.setBackground(new BitmapDrawable(result));
                    if (sImageCache.get(key) == null) {
                        sImageCache.put(key, result);
                    }
                }
                if (imageLoadCallBack != null) {
                    imageLoadCallBack.callback(result);
                }
            }

        }.execute("");
    }


    /**
     * @param filePath
     * @param imageUrl
     * @return
     */
    private static Bitmap getImageFormUrl(final String filePath,
                                          String imageUrl, Context context) {
        Bitmap bm = getBitmap(imageUrl, context);
        if (bm != null) {
            final Bitmap bitmap = bm;
            new Thread() {

                @Override
                public void run() {
                    if (filePath != null)
                        saveImage(bitmap, filePath);
                }
            }.start();
        }
        return bm;
    }

    /**
     * 从文件中获取图片
     */
    private static Bitmap getImageFormFile(String filePath, Context context) {
        File imageFile = new File(filePath);
        if (!imageFile.exists()) {
            return null;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;

        /** 这里是获取手机屏幕的分辨率用来处理 图片 溢出问题的。begin */
        DisplayMetrics dm = new DisplayMetrics();
        dm = context.getResources().getDisplayMetrics();
        int displaypixels = dm.widthPixels * dm.heightPixels;

        options.inSampleSize = computeSampleSize(options, -1, displaypixels);
        options.inJustDecodeBounds = false;
        try {
            Bitmap bmp = BitmapFactory.decodeFile(filePath, options);
            return bmp;
        } catch (OutOfMemoryError err) {
            err.printStackTrace();
        }
        return null;
    }


    /**
     * 根据一个网络连接(String)获取bitmap图像
     *
     * @param imageUri
     * @return
     */
    private static Bitmap getBitmap(String imageUri, Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        dm = context.getResources().getDisplayMetrics();
        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;
        int displaypixels = screenWidth / 2 * screenHeight / 2;

        // 显示网络上的图片
        Bitmap bitmap = null;

        try {
            URL myFileUrl = new URL(imageUri);
            HttpURLConnection conn = (HttpURLConnection) myFileUrl
                    .openConnection();
            conn.setDoInput(true);
            conn.connect();
            InputStream is = conn.getInputStream();
            byte[] bytes = getBytes(is);
            BitmapFactory.Options opts = new BitmapFactory.Options();
            // 这3句是处理图片溢出的begin( 如果不需要处理溢出直接 opts.inSampleSize=1;)
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opts);
            opts.inSampleSize = computeSampleSize(opts, -1, displaypixels);
            // end
            opts.inJustDecodeBounds = false;
            bitmap = BitmapFactory
                    .decodeByteArray(bytes, 0, bytes.length, opts);
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return bitmap;
    }

    /**
     * 保存图片到本地
     *
     * @param bm
     * @param filePath
     */
    public static void saveImage(Bitmap bm, String filePath) {
        if (bm == null) {
            return;
        }
        try {
            // 你要存放的文件
            File file = new File(filePath);
            // file文件的上一层文件夹
            File parentFile = new File(file.getParent());
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }

            if (!file.exists()) {
                file.createNewFile();
            }
            OutputStream outStream = new FileOutputStream(file);
            // //10 是压缩率，表示压缩90%; 如果不压缩是100，表示压缩率为0
            bm.compress(Bitmap.CompressFormat.JPEG, 100, outStream);

            outStream.flush();
            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存png图片
     *
     * @param bm
     * @param filePath
     */
    public static boolean savePngImage(Bitmap bm, String filePath) {
        if (bm == null) {
            return false;
        }
        try {
            // 你要存放的文件
            File file = new File(filePath);
            // file文件的上一层文件夹
            File parentFile = new File(file.getParent());
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }

            if (!file.exists()) {
                file.createNewFile();
            }
            OutputStream outStream = new FileOutputStream(file);
            // //10 是压缩率，表示压缩90%; 如果不压缩是100，表示压缩率为0
            bm.compress(Bitmap.CompressFormat.PNG, 100, outStream);

            outStream.flush();
            outStream.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static int computeSampleSize(BitmapFactory.Options options,
                                         int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(options, minSideLength,
                maxNumOfPixels);

        int roundedSize;
        if (initialSize <= 8) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }

        return roundedSize;
    }

    private static int computeInitialSampleSize(BitmapFactory.Options options,
                                                int minSideLength, int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;
        int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math
                .sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(
                Math.floor(w / minSideLength), Math.floor(h / minSideLength));
        if (upperBound < lowerBound) {
            // return the larger one when there is no overlapping zone.
            return lowerBound;
        }
        if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
            return 1;
        } else if (minSideLength == -1) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }


    public interface ImageLoadCallBack {
        void callback(Bitmap bitmap);
    }

}
