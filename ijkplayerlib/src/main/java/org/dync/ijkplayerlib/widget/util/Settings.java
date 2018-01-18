/*
 * Copyright (C) 2015 Bilibili
 * Copyright (C) 2015 Zhang Rui <bbcallen@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dync.ijkplayerlib.widget.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.dync.ijkplayerlib.R;


public class Settings {
    private Context mAppContext;
    private SharedPreferences mSharedPreferences;

    public static final int PV_PLAYER__Auto = 0;//相当于PV_PLAYER__IjkMediaPlayer
    public static final int PV_PLAYER__AndroidMediaPlayer = 1;
    public static final int PV_PLAYER__IjkMediaPlayer = 2;
    public static final int PV_PLAYER__IjkExoMediaPlayer = 3;

    public static final String Auto_Select = "";
//    public static final String RGB_565 = "RGB 565";
//    public static final String RGB_888 = "RGB 888";
//    public static final String RGBX_8888 = "RGBX 8888";
//    public static final String YV12 = "YV12";
//    public static final String OpenGL_ES2 = "OpenGL ES2";

    public static final String YV12 = "fcc-rv12";
    public static final String RGB_565 = "fcc-rv16";
    public static final String RGB_888 = "fcc-rv24";
    public static final String RGBX_8888 = "fcc-rv32";
    public static final String OpenGL_ES2 = "fcc-es2";

    public Settings(Context context) {
        mAppContext = context.getApplicationContext();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mAppContext);
    }

    public boolean getEnableBackgroundPlay() {
        String key = mAppContext.getString(R.string.pref_key_enable_background_play);
        return mSharedPreferences.getBoolean(key, false);
    }

    public void setEnableBackgroundPlay(boolean value) {
        String key = mAppContext.getString(R.string.pref_key_enable_background_play);
        mSharedPreferences.edit().putBoolean(key, value).apply();
    }

    public int getPlayer() {
        String key = mAppContext.getString(R.string.pref_key_player);
        try {
        int value = mSharedPreferences.getInt(key, 0);
            return value;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * {@link #PV_PLAYER__Auto}、{@link #PV_PLAYER__AndroidMediaPlayer}、
     * {@link #PV_PLAYER__IjkMediaPlayer}、{@link #PV_PLAYER__IjkExoMediaPlayer}
     * @param value  使用上面提供的参数
     */
    public void setPlayer(int value) {
        String key = mAppContext.getString(R.string.pref_key_player);
        mSharedPreferences.edit().putInt(key, value).apply();
    }

    /**
     * 默认硬解码，使用软解码会未播放前切换视频源会卡顿
     * @return
     */
    public boolean getUsingMediaCodec() {
        String key = mAppContext.getString(R.string.pref_key_using_media_codec);
        return mSharedPreferences.getBoolean(key, true);
    }

    public void setUsingMediaCodec(boolean value) {
        String key = mAppContext.getString(R.string.pref_key_using_media_codec);
        mSharedPreferences.edit().putBoolean(key, value).apply();
    }

    public boolean getUsingMediaCodecAutoRotate() {
        String key = mAppContext.getString(R.string.pref_key_using_media_codec_auto_rotate);
        return mSharedPreferences.getBoolean(key, false);
    }

    public void setUsingMediaCodecAutoRotate(boolean value) {
        String key = mAppContext.getString(R.string.pref_key_using_media_codec_auto_rotate);
        mSharedPreferences.edit().putBoolean(key, value).apply();
    }

    public boolean getMediaCodecHandleResolutionChange() {
        String key = mAppContext.getString(R.string.pref_key_media_codec_handle_resolution_change);
        return mSharedPreferences.getBoolean(key, false);
    }

    public void setMediaCodecHandleResolutionChange(boolean value) {
        String key = mAppContext.getString(R.string.pref_key_media_codec_handle_resolution_change);
        mSharedPreferences.edit().putBoolean(key, value).apply();
    }

    public boolean getUsingOpenSLES() {
        String key = mAppContext.getString(R.string.pref_key_using_opensl_es);
        return mSharedPreferences.getBoolean(key, false);
    }

    public void setUsingOpenSLES(boolean value) {
        String key = mAppContext.getString(R.string.pref_key_using_opensl_es);
        mSharedPreferences.edit().putBoolean(key, value).apply();
    }

    public String getPixelFormat() {
        String key = mAppContext.getString(R.string.pref_key_pixel_format);
        return mSharedPreferences.getString(key, "");
    }

    public void setPixelFormat(String value) {
        String key = mAppContext.getString(R.string.pref_key_pixel_format);
        mSharedPreferences.edit().putString(key, value).apply();
    }

    public boolean getEnableNoView() {
        String key = mAppContext.getString(R.string.pref_key_enable_no_view);
        return mSharedPreferences.getBoolean(key, false);
    }

    public void setEnableNoView(boolean value) {
        String key = mAppContext.getString(R.string.pref_key_enable_no_view);
        mSharedPreferences.edit().putBoolean(key, value).apply();
    }

    public boolean getEnableSurfaceView() {
        String key = mAppContext.getString(R.string.pref_key_enable_surface_view);
        return mSharedPreferences.getBoolean(key, false);
    }

    public void setEnableSurfaceView(boolean value) {
        String key = mAppContext.getString(R.string.pref_key_enable_surface_view);
        mSharedPreferences.edit().putBoolean(key, value).apply();
    }

    public boolean getEnableTextureView() {
        String key = mAppContext.getString(R.string.pref_key_enable_texture_view);
        return mSharedPreferences.getBoolean(key, false);
    }

    public void setEnableTextureView(boolean value) {
        String key = mAppContext.getString(R.string.pref_key_enable_texture_view);
        mSharedPreferences.edit().putBoolean(key, value).apply();
    }

    public boolean getEnableDetachedSurfaceTextureView() {
        String key = mAppContext.getString(R.string.pref_key_enable_detached_surface_texture);
        return mSharedPreferences.getBoolean(key, false);
    }

    public void setEnableDetachedSurfaceTextureView(boolean value) {
        String key = mAppContext.getString(R.string.pref_key_enable_detached_surface_texture);
        mSharedPreferences.edit().putBoolean(key, value).apply();
    }

    public boolean getUsingMediaDataSource() {
        String key = mAppContext.getString(R.string.pref_key_using_mediadatasource);
        return mSharedPreferences.getBoolean(key, false);
    }

    public void setUsingMediaDataSource(boolean value) {
        String key = mAppContext.getString(R.string.pref_key_using_mediadatasource);
        mSharedPreferences.edit().putBoolean(key, value).apply();
    }

    public String getLastDirectory() {
        String key = mAppContext.getString(R.string.pref_key_last_directory);
        return mSharedPreferences.getString(key, "/");
    }

    public void setLastDirectory(String path) {
        String key = mAppContext.getString(R.string.pref_key_last_directory);
        mSharedPreferences.edit().putString(key, path).apply();
    }
}
