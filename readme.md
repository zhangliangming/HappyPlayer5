# 重要声明 #
由于学习交流时间关闭，乐乐音乐6.0不再提供源码下载，乐乐相关的代码及开源的控件仅供于学习用途。

# 简介 #
乐乐音乐6.0主要是基于ijkplayer框架开发的Android音乐播放器，它支持多种音频格式和动感歌词及制作动感歌词、翻译歌词和音译歌词，以及MV功能。

# 运行环境 #
android5.0及以上

# 音频格式 #
目前用ijkplayer来测试如下音频格式，均可正常播放：aac，amr，ape，flac，m4r，mmf，mp2，mp3，ogg，wav，wma，wv

# 歌词格式 #
- lrc：普通歌词、 krc：酷狗歌词 、ksc：卡拉OK歌词
- hrc：happy lyrics歌词，乐乐音乐自定义的动感歌词格式，可准确到歌词每个字。
- 网易云API歌词：该歌词只适用于通过api获取歌词，文件保存格式为：lrcwy。其中动感歌词和lrc歌词只能选其中一种，支持翻译歌词
- 注：其中krc和hrc歌词支持音译和翻译歌词

# 项目中抽出来的开源控件 #

- [Subtitle（字幕库）](https://github.com/zhangliangming/Subtitle)
- [SwipeBackLayout（右滑动关闭界面）](https://github.com/zhangliangming/SwipeBackLayout.git)
- [RotateLayout（旋转界面）](https://github.com/zhangliangming/RotateLayout.git)
- [SeekBar（进度条）](https://github.com/zhangliangming/SeekBar.git)
- [HPLyrics（动感歌词解析和歌词显示库：支持音译和翻译歌词）](https://github.com/zhangliangming/HPLyrics.git)
- [HPAudio（音频解析库：支持wav、mp3、ape、ogg、wv和flac）](https://github.com/zhangliangming/HPAudio.git)
- [Android仿酷狗SlidingMenuLayout界面实现](https://github.com/zhangliangming/SlidingMenuLayout.git)
- [ijkplayer依赖包（支持无损）](https://github.com/zhangliangming/Player.git)

# 第三方控件/框架 #

- bugly：崩溃日志收集
- [SwitchButton：开关按钮](https://github.com/zcweng/SwitchButton.git "SwitchButton")
- [LeakCanary：日志泄露分析](https://github.com/square/leakcanary "LeakCanary")
- [Utils-Everywhere：适配打开权限设置界面](https://github.com/SenhLinsh/Utils-Everywhere.git "Utils-Everywhere")
- [greenDAO：sqlite数据库框架](https://github.com/greenrobot/greenDAO "greenDAO")
- [jjdxm_dialogui：弹出窗口框架](https://github.com/jjdxmashl/jjdxm_dialogui/ "jjdxm_dialogui")
- [LRecyclerView：RecyclerView上拉加载更多，下拉刷新](https://github.com/jdsjlzx/LRecyclerView "LRecyclerView")

# 更多效果图(试用安装包不定期更新) #

[更多效果图，点击此次查看:https://pan.baidu.com/s/1YkZssna3TO9hKAOXwvxXiw 提取码: dgtc](https://pan.baidu.com/s/1YkZssna3TO9hKAOXwvxXiw)

# 部分效果预览 #

![MV](https://i.imgur.com/ZxVROaD.png)

![制作音译歌词](https://i.imgur.com/s7MUO7T.png)

![制作翻译歌词](https://i.imgur.com/eXpTpkd.png)

![制作动感歌词](https://i.imgur.com/PKW9rc6.png)

![歌词制作器](https://i.imgur.com/Ao93nFw.png)

![歌词转换器](https://i.imgur.com/gmQAXnJ.png)

![新歌](https://i.imgur.com/4PzRnNz.png)

![写真、歌词](https://i.imgur.com/DtIgAde.png)

![锁屏/锁屏歌词](https://i.imgur.com/WmazYtx.png)

![桌面歌词](https://i.imgur.com/TWb9KO8.png)

# 客户端试用 #

[更多版本客户端，点击此次查看:https://pan.baidu.com/s/1YkZssna3TO9hKAOXwvxXiw 提取码: dgtc](https://pan.baidu.com/s/1YkZssna3TO9hKAOXwvxXiw)

# 更新日志 #

## 2019-01-24 ##
- 修复本地歌曲播放报找不到文件路径的问题

## 2019-01-23 ##
- 添加mv字幕搜索和显示功能

## 2019-01-15 ##
- 引入Subtitle库

## 2018-01-06 ##
- 添加简单MV界面

## 2018-01-01 ##
- 添加制作动感歌词、翻译歌词和音译歌词功能

## 2018-12-31 ##
- 添加制作歌词界面、修复全面屏情况下，输入法遮挡底部布局的问题

## 2018-12-30 ##
- 添加歌词转换器

## 2018-12-22 ##
- 添加下载页面、添加列表更多菜单功能、添加歌曲搜索功能

## 2018-12-16 ##
- 添加最近和喜欢歌曲列表

## 2018-12-08 ##
- 添加锁屏歌词
- 修复通用跳转权限设置页面

## 2018-12-02 ##
- 添加桌面歌词

## 2018-11-27 ##
- 添加通知栏

## 2018-11-25 ##
- 添加歌词搜索、定时关闭

## 2018-11-18 ##
- 线控
- 修复歌手写真、写真搜索

## 2018-11-13 ##
- 添加歌词字体大小修改及颜色修改

## 2018-09-24 ##
- 添加新歌、排行和歌单列表

## 2018-08-27 ##
- 修复状态栏半透明的问题
- 修复全面屏的问题


## 2018-08-19 ##
- 添加设置和关于页面
- 添加弹出窗口

## 2018-08-18 ##
- HttpClient添加https支持(忽略证书)
- 添加greenDAO框架处理数据库

## 2018-08-13 ##
- 添加了SlidingMenuLayout（侧边栏，viewpager）
- 添加了酷狗的api
- 优化了单个任务多个线程下载代码

# 传送门 #

- [ijkplayer开源框架](https://github.com/Bilibili/ijkplayer "ijkplayer开源框架")
- [Hrc/hrcx歌词制作器（播放器）](https://github.com/zhangliangming/HappyPlayer-PC.git "Hrc/Hrcx歌词制作器（播放器）")
- [浅谈动感歌词](http://zhangliangming.github.io/ "浅谈动感歌词")
- [Kugou-api](https://github.com/ecitlm/Kugou-api "Kugou-api")
- [各大音乐软件API](https://messoer.github.io/mess-api-doc/ "各大音乐软件API")

# 声明 #
仅用于学习用途

# License #

Apache 2.0. See the [LICENSE](https://github.com/zhangliangming/HappyPlayer5/blob/happy_player6/LICENSE) file for details.

# 捐赠 #
如果该项目对您有所帮助，欢迎您的赞赏

- 微信

![](https://i.imgur.com/hOs6tPn.png)

- 支付宝

![](https://i.imgur.com/DGB9Lq0.png)
