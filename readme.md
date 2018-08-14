# 项目分支代码声明 #
branch-master：该分支代码是我与DyncKathline、liupeng110参与开发，该APP的界面和功能与我之前写的有较大的差别，该分支代码的功能目前不是好稳定，如有需要可切换分支，待到该分支稳定下来后，我会设置该分支为默认的branch。

branch-zlm：该分支代码是我个人开发的，目前功能稳定，代码会一直更新、优化和修复bug。

 happy_player6：该分支代码是乐乐第六版本。

# 后期将会修复、优化和新开发的功能 #

- 引入线程池，统一管理线程、多进程、多线程考虑线程安全问题
- 优化网络请求
- 修改MVP模式
- 引用多一些好的第三方开源框架
- 分析内存泄露
- 自定义锁屏界面修改为系统锁屏界面

# 项目中抽出来的开源控件 #

- [SwipeBackLayout（右滑动关闭界面）](https://github.com/zhangliangming/SwipeBackLayout.git)
- [RotateLayout（旋转界面）](https://github.com/zhangliangming/RotateLayout.git)
- [SeekBar（进度条）](https://github.com/zhangliangming/SeekBar.git)
- [HPLyrics（动感歌词解析和歌词显示库）](https://github.com/zhangliangming/HPLyrics.git)
- [HPAudio（音频解析库）](https://github.com/zhangliangming/HPAudio.git)
- [Android仿酷狗SlidingMenuLayout界面实现](https://github.com/zhangliangming/SlidingMenuLayout.git)
- [ijkplayer依赖包（支持无损）](https://github.com/zhangliangming/Player.git)


# 更新日志 #
- 2018-08-11
- ijkplayer修改为依赖引用
- 优化歌词

- 2018-06-18
- 添加SlidingMenuLayout控件，暂时未整合到该项目

- 2018-06-08
- 修复转换歌词的路径


- 2018-06-02
- 添加注册码，试用结束后，直接关闭应用（不提示）

- 2018-05-23
- 优化桌面歌词
- 初步将系统文件管理器修改为自定义文件管理器

- 2018-05-19
- 代码混淆
- 修复混淆

- 2018-05-18
- 修改桌面歌词颜色及锁定歌词相关代码
- 添加时间关闭功能，暂时不支持界面设置时间功能。

- 2018-05-13
- 修改桌面字体

- 2018-05-12
- 修复删除功能及隐藏删除播放列表功能
- 添加桌面歌词功能，实现桌面歌词锁定、解锁、歌词窗口移动
- 预览图
- ![](https://i.imgur.com/0k1hjIZ.png)

- ![](https://i.imgur.com/ewkQfpB.png)

- ![](https://i.imgur.com/480Odxp.png)

- ![](https://i.imgur.com/MP26VRK.png)

- ![](https://i.imgur.com/V35IUF1.png)

- ![](https://i.imgur.com/NVCaoH0.png)


- 2018-05-11
- 修复扫描重复歌曲问题
- 下载任务、本地歌曲、喜欢和最近添加删除功能

- 2018-05-06
- 歌词搜索界面优化
- 歌词视图修改为引用自定义view
- 去掉部分activity的硬件加速
- 2018-05-05
- 添加歌词刷新时间接口
- 依赖包添加代码混淆
- 音频解析库添加ogg、wv和修改wav解析
- 2018-05-01
- 修复进度条快进时位置回滚问题
- 修复[android4.4(api19)无法安装，也无法真机、模拟机调试](https://github.com/zhangliangming/HappyPlayer5/issues/18)
- 2018-04-30
- 独立音频解析库，目前只支持wav,mp3,flac和ape格式文件获取歌曲头文件信息
- 2018-04-29
- 修复获取wav格式音频文件的时间长度不正确的问题
- 参考博客：[Android音频开发](https://github.com/Jhuster/AudioDemo)
- 参考博客：[WAVE PCM soundfile format](http://soundfile.sapp.org/doc/WaveFormat/)
- 参考博客：[各种WAV文件头格式](http://www.xuebuyuan.com/840670.html)
- 2018-04-22
- 歌词view替换成TextureView来实现，关于TextureView、Surfaceview的相关注意事项，可参考：[HPLyrics（动感歌词解析和歌词显示库）](https://github.com/zhangliangming/HPLyrics.git)
- 修复快进后播放状态出错的问题
- 缩短启动页的停留赶时间
- 2018-04-02：修复网络歌曲制作歌词功能。
- 2018-04-01
- PC版制作动感歌词功能移植到APP。
- 具体参考博客地址：[Android动感歌词制作器（支持翻译和音译歌词）](http://zhangliangming.github.io/Android%E5%8A%A8%E6%84%9F%E6%AD%8C%E8%AF%8D%E5%88%B6%E4%BD%9C%E5%99%A8-%E6%94%AF%E6%8C%81%E7%BF%BB%E8%AF%91%E5%92%8C%E9%9F%B3%E8%AF%91%E6%AD%8C%E8%AF%8D/)
- 入口

![](https://i.imgur.com/vxX4zYZ.png)
![](https://i.imgur.com/umR6y6z.png)


- 动感歌词制作


![](https://i.imgur.com/R7leu8E.png)


注：制作动感歌词时，需要先播放歌曲，并且选中（RadioButton被选中，并且歌词出现红边框）要敲打的行歌词，然后便可以敲打。歌曲读到【字】，对应要敲打到该字，这样才可以保证歌曲与歌词几步，完成后，边框会变成蓝色，全部完成后，才可以进入预览视图。


- 翻译歌词制作


![](https://i.imgur.com/YyGfWSy.png)


注：制作翻译歌词时，点击item列表，便会弹出输入翻译编辑框，只需要在输入框输入内容，便完成该行对应的翻译歌词，可不填写。



- 音译歌词制作



![](https://i.imgur.com/pkhbA4g.png)



注：制作音译歌词时，点击item列表，便会弹出输入音译编辑框，输入音译歌词时，输入框下有01/04的进度提示，并且输入对应的【字】后，已经完成的【字】会变成蓝色，然后点击空格，进行分隔，不过这里为了兼容krc歌词格式（经常出现一个字包含多个歌词文字的情况），这里使用“∮”符号来替代空格。



- 2018-03-25：
- 添加歌词转换器工具，支持ksc、krc和hrc歌词间的相互转换及转换成lrc歌词。
- 效果图如下：

![](https://i.imgur.com/00IPgmp.png)

![](https://i.imgur.com/rcwZtqF.png)

![](https://i.imgur.com/WEKFkY7.png)

- 添加歌词生成图片功能，本想生成视频的，手机太渣生成太耗时，就放弃了。
- 生成图片后，想用图片生成视频PC软件来生成视频，结果图片数量太多，软件都崩溃了，真是自作死。

- 2018-03-15：修复进入多行歌词界面，报错的问题
- 2018-03-14：ijkplayer修改为引入libs下的so文件，支持无损歌曲；修复多行歌词未读时渐变的问题，修复最后一个字渐变出错的问题
- 2018-03-10：修复网络请求时间和进入启动时慢的问题；歌曲播放时，歌词卡顿不流畅的问题，只要是因为优化歌词之后，有时还是不能确保100ms刷新一次造成的，暂时无法解决。
- 2018-03-06：修复应用关闭后，通知栏没有消失的问题
- 2018-03-04
- 歌词优化，具体可参考：[Android仿酷狗动感歌词（支持翻译和音译歌词）显示效果](http://zhangliangming.github.io/Android%E4%BB%BF%E9%85%B7%E7%8B%97%E5%8A%A8%E6%84%9F%E6%AD%8C%E8%AF%8D-%E6%94%AF%E6%8C%81%E7%BF%BB%E8%AF%91%E5%92%8C%E9%9F%B3%E8%AF%91%E6%AD%8C%E8%AF%8D-%E6%98%BE%E7%A4%BA%E6%95%88%E6%9E%9C/)
- 歌词解析和歌词显示独立成一个开源库
- 添加对lrc歌词的解析和显示，双行歌词暂时不支持lrc歌词格式的显示
- 多行歌词添加对翻译歌词变动感歌词的显示，只支持动感歌词格式的翻译歌词，其效果如下：

![](https://i.imgur.com/Is96x66.png)

- 双行歌词的颜色，只要参考酷狗PC版的桌面歌词颜色
- 双行歌词添加翻译歌词和音译歌词显示，点击底部可对歌词进行切换，效果如下：
- 
![](https://i.imgur.com/bEqWNJl.png)

![](https://i.imgur.com/8BDHX3L.png)

- 2018-02-21：SeekBar弄成开源控件项目，主要使用jitpack来实现maven库，并修改为自定义view的方式来实现
- 2018-02-21：删除nineoldandroids.jar
- 2018-02-13：RotateLayout界面抽出来，弄成开源控件项目。主要使用jitpack来实现maven库
- 2018-02-12：SwipeBackLayout界面抽出来，弄成开源控件项目。主要使用jitpack来实现maven库
- 2018-02-07：布局文件优化、旋转界面优化（添加硬件加速和硬件加速带来的图标闪烁问题）
- 2018-02-06：修改搜索页面为可右滑关闭、部分jar包修改为gradle方式引入
- 2018-02-05：修复和优化SwipeBackLayout、SlidingMenuLayout、SwipeOutLayout
- 2018-02-02：RotateLinearLayout中setRotation时，因为把硬件加速关闭了[#6](https://github.com/zhangliangming/HappyPlayer5/issues/6 "RotateLinearLayout旋转角度在0.x时LrcActivity页面layout_lrc_playbar布局会闪烁")，导致旋转动画时，界面上的文字出现了晃动,这个问题我会在后期修复,目前能想到的方法是：到时开启硬件启动，然后优化LrcActivity的页面布局。
- 2018-02-01：优化SwipeBackLayout
- 2018-01-31：最近看了一下事件分发和冲突，感觉旋转界面和多行歌词的代码，可以优化为如下的事件分发图，大家也可以尝试一下修改一下：
![](https://i.imgur.com/60rKre3.png)
- 2018-01-30：事件分发和冲突博客：[Android事件分发机制详解：史上最全面、最易懂](https://www.jianshu.com/p/38015afcdb58)
- 2018-01-27：修复主界面底部的右滑显示的双行歌词的换行问题。
- 2018-01-27：优化SwipeoutLayout、SwipeBackLayout、SlidingMenuLayout和OverScrollView的事件分发和冲突。
- 2018-01-24：修复歌手写真界面，点击下载按钮触发多行歌词转换双行歌词的问题
- 2018-01-22：修改应用的主题样式；修复item点击透明问题；修复拼音类处理获取歌手拼音时，歌手字符串中存在符号，导致奔溃的错误
- 2018-01-22：修复layout view的动画
- 2018-01-22：去掉歌手写真界面的酷狗背景图片；程序异常关闭后，关闭所有界面；修复程序关闭后，通知栏还存在的问题。
- 2018-01-22：加大歌词行的移动时间，让歌词移动动画更流畅，有需要的小伙伴也可以根据当前歌词的行数（因为歌词换行导致动画移动不流畅）适当地动态增加移动的时间，可以使移动动画更流畅。
- 2018-01-21：添加悬浮窗口的权限判断和直接跳转设置界面（桌面歌词功能暂时没实现）；锁屏歌词界面，因为部分小米手机（如：我的小米2s，android 5.0），需要到权限设置界面设置“锁屏显示”权限(注：歌曲播放时，才会显示锁屏界面)。
- 2018-01-19
- 引用别人项目工具类，主要用于判断各种手机的权限设置页面，项目地址如下：[Utils-Everywhere](https://github.com/SenhLinsh/Utils-Everywhere.git)
- 第一次点击锁屏时，跳转到权限选择页面。
- 新添加锁屏界面，预览如下：
![](https://i.imgur.com/hZlES1d.png)


- 优化耳机线控类
- 修复RotateLinearLayout旋转角度在0.x时布局闪烁问题，我把硬件加速关闭后，在模拟器上面，就正常了，下次页面绘制时闪动时，可以考虑是否开启了硬件加速。
- 2018-01-18：暂时简单修复RotateLinearLayout旋转角度在0.x时布局闪烁。
- 2018-01-16
- RotateLinearLayout旋转角度在0.x时布局闪烁暂时找不到原因，暂无法解决。
- 修复通知栏图标问题
- 修复android7.0状态栏不能透明的问题
- 2018-01-14：修复RotateLinearLayout旋转角度在0.x时LrcActivity页面layout_lrc_playbar布局会闪烁。注：只在模拟器上面测试通过，真机没试过。
- 2018-01-09：修复Android O系统时，通知栏报failed to post notification on channel....的问题
- 2018-01-08：启动时，添加文件的读写权限判断。简单修复android6.0以上版本的权限问题。
- 2017-12-25：优化歌词解析和生成类，修复部分歌词解析乱码的问题。
- 2017-11-12
- 更新gradle环境为gradle-4.1-all，as为3.0正式版
- 添加最近看到别人总结的酷狗api的项目地址，有兴趣的小伙伴可以直接到该项目查看酷狗的相关api哦，感觉api还是比较全面的：[Kugou-api](https://github.com/ecitlm/Kugou-api)
- 2017-09-20：因为读取歌词的时候，将歌词格式编码都设置为utf-8,所以解析之前的歌词时，会造成乱码问题，到时有乱码，只要将utf-8编码修改一下即可。
- 2017-09-19
- 修复歌词快进
- 添加krc歌词修改翻译歌词和音译歌词功能
- 优化hrc歌词，支持krc歌词转换、翻译歌词和音译歌词
- 2017-09-18
- 支持翻译歌词和音译歌词
- 新增歌曲下载功能
- 图片预览
- 音译歌词

![](https://i.imgur.com/WPaPfRT.jpg)



- 翻译歌词

![](https://i.imgur.com/mw7myD8.jpg)

# 简介 #
乐乐音乐5.0主要是基于ijkplayer框架开发的Android音乐播放器，它支持多种音频格式和动感歌词，界面高仿酷狗。

# 开发环境 #
- android studio 3.0（正式版）
- gradle-4.1-all
- 小米2s手机
- android 5.0系统

# 音频格式 #
目前用ijkplayer来测试如下音频格式，均可正常播放：aac，amr，ape，flac，m4r，mmf，mp2，mp3，ogg，wav，wma，wv

# 歌词格式 #
- lrc
- krc：酷狗歌词
- ksc：卡拉OK歌词
- hrc：happy lyrics歌词，乐乐音乐自定义的动感歌词格式，可准确到歌词每个字。

# 功能 #
- 底部播放菜单固定，具体参考SlidingMenu和结合Fragment来实现
- 底部播放菜单左右滑动显示/关闭双行歌词
- 歌词界面旋转移动和关闭
- 动感歌词、多行歌词、歌词快进、歌词时间微调、歌词换行、字体大小、歌词颜色切换和歌词透明度上下渐变 
- 多行歌词平滑移动，快速流畅滑动。具体使用Scroller来实现。注：Scroller只做动画，不要用来移动view
- 窗口右滑关闭
- 歌曲边缓存边播放

# 效果图 #
- 主界面

![](https://i.imgur.com/BeTErXg.png)
![](https://i.imgur.com/RC79uMU.png)
![](https://i.imgur.com/vFU57au.png)
![](https://i.imgur.com/t4Lq4J0.png)
![](https://i.imgur.com/W33afTx.png)
![](https://i.imgur.com/U2AdVMC.png)
![](https://i.imgur.com/Y3pUvMQ.png)
![](https://i.imgur.com/TFaCRc3.png)

- 歌手写真界面

![](https://i.imgur.com/NjdUuEp.png)
![](https://i.imgur.com/YjVxcnR.png)
![](https://i.imgur.com/p1Emexh.png)
![](https://i.imgur.com/FL7OiCq.png)
![](https://i.imgur.com/cFqiFf7.png)
![](https://i.imgur.com/CZtMA6F.png)


# 传送门 #

- [ijkplayer开源框架](https://github.com/Bilibili/ijkplayer "ijkplayer开源框架")
- [Hrc/hrcx歌词制作器（播放器）](https://github.com/zhangliangming/HappyPlayer-PC.git "Hrc/Hrcx歌词制作器（播放器）")
- [Krc、Ksc、Hrc/hrcx歌词解析器](https://github.com/zhangliangming/LyricsAnalyze.git "krc、ksc、hrc/hrcx歌词解析器")
- [浅谈动感歌词](http://zhangliangming.github.io/ "浅谈动感歌词")

# 声明 #
仅用于学习用途

# 捐赠 #
如果该项目对您有所帮助，欢迎您的赞赏

- 微信

![](https://i.imgur.com/e3hERHh.png)

- 支付宝

![](https://i.imgur.com/29AcEPA.png)
