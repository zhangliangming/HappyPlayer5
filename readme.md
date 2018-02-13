# 项目分支代码声明 #
branch-master：该分支代码是我与DyncKathline、liupeng110参与开发，该APP的界面和功能与我之前写的有较大的差别，该分支代码的功能目前不是好稳定，如有需要可切换分支，待到该分支稳定下来后，我会设置该分支为默认的branch。

branch-zlm：该分支代码是我个人开发的，目前功能稳定，代码会一直更新、优化和修复bug。

# 后期将会修复、优化和新开发的功能 #
- 歌词搜索界面优化
- 单例优化
- 引入线程池，统一管理线程
- 广播每隔100ms频繁更新歌词界面问题优化（感谢一位大神提供的宝贵建议和方案）
- 桌面歌词
- 优化网络请求
- 多线程考虑线程安全问题
- 修改MVP模式
- 多进程
- 引用多一些好的第三方开源框架
- 分析内存泄露
- 使用一些android特有的数据结构
- 添加歌曲的mv功能
- SeekBar后期修改为自定义view的方式来实现
- SlidingMenu（添加左侧边栏）、歌词解析（动感歌词和lrc歌词格式）和动感歌词显示（支持动感歌词和lrc歌词）到时会优化成开源框架，独立出来
- 歌词分享，歌词文件生成视频（思路暂时如下：歌词文件生成动感歌词图片，再将动感歌词图片合成视频）
- 制作歌词(将pc制作歌词功能移植到app)
- 自定义锁屏界面修改为系统锁屏界面

# 项目中抽出来的开源控件 #
-SwipeBackLayout（右滑动关闭界面）：https://github.com/zhangliangming/SwipeBackLayout.git

-RotateLayout（旋转界面）：https://github.com/zhangliangming/RotateLayout.git

# 更新日志 #
- 2018-02-13：RotateLayout界面抽出来，弄成开源控件项目。主要使用jitpack来实现maven库
- 2018-02-12：SwipeBackLayout界面抽出来，弄成开源控件项目。主要使用jitpack来实现maven库
- 2018-02-07：布局文件优化、旋转界面优化（添加硬件加速和硬件加速带来的图标闪烁问题）
- 2018-02-06：修改搜索页面为可右滑关闭
- 2018-02-06：部分jar包修改为gradle方式引入
- 2018-02-05：修复和优化SwipeBackLayout、SlidingMenuLayout，SwipeoutLayout类修改为：SwipeOutLayout
- 2018-02-02：RotateLinearLayout中setRotation时，因为把硬件加速关闭了（[https://github.com/zhangliangming/HappyPlayer5/issues/6](https://github.com/zhangliangming/HappyPlayer5/issues/6 "RotateLinearLayout旋转角度在0.x时LrcActivity页面layout_lrc_playbar布局会闪烁")），导致旋转动画时，界面上的文字出现了晃动,这个问题我会在后期修复,目前能想到的方法是：到时开启硬件启动，然后优化LrcActivity的页面布局。
- 2018-02-01：优化SwipeBackLayout
- 2018-01-31：最近看了一下事件分发和冲突，感觉旋转界面和多行歌词的代码，可以优化为如下的事件分发图，大家也可以尝试一下修改一下：
![](https://i.imgur.com/60rKre3.png)
- 2018-01-30：事件分发和冲突博客; https://www.jianshu.com/p/38015afcdb58
- 2018-01-30：关于move和up事件，该博客描述得比较好:https://www.jianshu.com/p/e99b5e8bd67b
- 2018-01-27：修复主界面底部的右滑显示的双行歌词的换行问题。
- 2018-01-27：优化SwipeoutLayout、SwipeBackLayout、SlidingMenuLayout和OverScrollView的事件分发和冲突。
- 2018-01-24：修复歌手写真界面，点击下载按钮触发多行歌词转换双行歌词的问题
- 2018-01-22：修改应用的主题样式；修复item点击透明问题；修复拼音类处理获取歌手拼音时，歌手字符串中存在符号，导致奔溃的错误
- 2018-01-22：修复layout view的动画
- 2018-01-22：去掉歌手写真界面的酷狗背景图片；程序异常关闭后，关闭所有界面；修复程序关闭后，通知栏还存在的问题。
- 2018-01-22：加大歌词行的移动时间，让歌词移动动画更流畅，有需要的小伙伴也可以根据当前歌词的行数（因为歌词换行导致动画移动不流畅）适当地动态增加移动的时间，可以使移动动画更流畅。
- 2018-01-21：添加悬浮窗口的权限判断和直接跳转设置界面（桌面歌词功能暂时没实现）；锁屏歌词界面，因为部分小米手机（如：我的小米2s，android 5.0），需要到权限设置界面设置“锁屏显示”权限(注：歌曲播放时，才会显示锁屏界面)。
- 2018-01-19：引用别人项目工具类，主要用于判断各种手机的权限设置页面，项目地址如下：https://github.com/SenhLinsh/Utils-Everywhere.git
- 2018-01-19：第一次点击锁屏时，跳转到权限选择页面。
- 2018-01-19：新添加锁屏界面，预览如下：
![](https://i.imgur.com/hZlES1d.png)
- 2018-01-19：优化耳机线控类
- 2018-01-19：修复RotateLinearLayout旋转角度在0.x时布局闪烁问题，我把硬件加速关闭后，在模拟器上面，就正常了，下次页面绘制时闪动时，可以考虑是否开启了硬件加速。
- 2018-01-18：暂时简单修复RotateLinearLayout旋转角度在0.x时布局闪烁。
- 2018-01-16：RotateLinearLayout旋转角度在0.x时布局闪烁暂时找不到原因，暂无法解决。
- 2018-01-16：修复通知栏图标问题
- 2018-01-16：修复android7.0状态栏不能透明的问题
- 2018-01-14：修复RotateLinearLayout旋转角度在0.x时LrcActivity页面layout_lrc_playbar布局会闪烁。注：只在模拟器上面测试通过，真机没试过。
- 2018-01-09：修复Android O系统时，通知栏报failed to post notification on channel....的问题
- 2018-01-08：启动时，添加文件的读写权限判断。简单修复android6.0以上版本的权限问题。
- 2017-12-25：优化歌词解析和生成类，修复部分歌词解析乱码的问题。
- 2017-11-12：更新gradle环境为gradle-4.1-all，as为3.0正式版
- 2017-11-12：添加最近看到别人总结的酷狗api的项目地址，有兴趣的小伙伴可以直接到该项目查看酷狗的相关api哦，感觉api还是比较全面的：https://github.com/ecitlm/Kugou-api
- 2017-09-20：因为读取歌词的时候，将歌词格式编码都设置为utf-8,所以解析之前的歌词时，会造成乱码问题，到时有乱码，只要将utf-8编码修改一下即可。
- 2017-09-19
- 修复歌词快进
- 添加krc歌词修改翻译歌词和音译歌词功能
- 添加krc歌词转hrcx歌词(暂时支持中文、英文、日文和韩语转换)
- 基于krc歌词，hrc和hrcx歌词添加翻译歌词和音译歌词
- 基于krc歌词，开发研究hrcs歌词（hrcx歌词升级版，完美支持krc歌词转换）
- 2017-09-18
- 支持翻译歌词和音译歌词
- 新增歌曲下载功能
- 图片预览
1. 音译歌词
![](https://i.imgur.com/WPaPfRT.jpg)
2. 翻译歌词
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
- krc：酷狗歌词
- ksc：卡拉OK歌词
- hrc：happy lyrics歌词，乐乐音乐自定义的动感歌词格式，可准确到歌词每个字。
- hrcx：hrc歌词的优化

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


# 安装包和资源文件 #
链接：[http://pan.baidu.com/s/1qXTd8mg](http://pan.baidu.com/s/1qXTd8mg) 密码：x0yz

# 传送门 #

- [ijkplayer开源框架](https://github.com/Bilibili/ijkplayer "ijkplayer开源框架")
- [Hrc/hrcx歌词制作器（播放器）](https://github.com/zhangliangming/HappyPlayer-PC.git "Hrc/Hrcx歌词制作器（播放器）")
- [Krc、Ksc、Hrc/hrcx歌词解析器](https://github.com/zhangliangming/LyricsAnalyze.git "krc、ksc、hrc/hrcx歌词解析器")
- [浅谈动感歌词](http://zhangliangming.github.io/ "浅谈动感歌词")

# 声明 #
仅用于学习用途

# 项目地址 #
[https://github.com/zhangliangming/HappyPlayer5.git](https://github.com/zhangliangming/HappyPlayer5.git)

# 联系方式 #
316257874@qq.com

# 捐赠 #
如果该项目对您有所帮助，欢迎您的赞赏

- 微信

![](https://i.imgur.com/e3hERHh.png)

- 支付宝

![](https://i.imgur.com/29AcEPA.png)
