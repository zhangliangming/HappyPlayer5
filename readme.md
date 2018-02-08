# 项目分支代码声明 #
branch-master：该分支代码是我与DyncKathline、liupeng110参与开发，该APP的界面和功能与我之前写的有较大的差别。
branch-zlm：该分支代码是我个人开发的（我会不定期同步最新的代码），如需要查看我之前写的代码，请切换分支即可。
> Bugly.init(getApplicationContext(), Constant.BUGLY_APPID, false);
这句代码是用于应用崩溃时收集crash，具体可以可以自行查看：https://bugly.qq.com/docs/user-guide/instruction-manual-android-upgrade/?v=20180115122747

# 更新日志 #
- 2018-02-08:  
1.对AudioInfoDB增加recent和likes字段来区分最近和喜欢，解决了添加最近导致插入重复数据  
2.修复了异常结束APP导致状态不正确的问题  
3.修复了添加喜欢，数据不能正常显示  
- 2018-02-07(@zlm):修复旋转界面时，文字波浪晃动的问题（开启硬件加速）；修复弹出窗口里面的播放列表动画问题
- 2018-02-06: 修正了扫描部分出现的bug
- 2018-01-27(@zlm)：修复主界面底部的右滑显示的双行歌词的换行问题
- 2018-01-27(@zlm)：优化SwipeoutLayout、SwipeBackLayout、SlidingMenuLayout和OverScrollView的事件分发和冲突。
- 2018-01-26(@lp):修正几处子线程更新ui的bug,替换几处new Thread()为ThreadUtil
- 2018-01-25：增加音乐文件过滤功能
- 2018-01-24(@zlm)：修复歌手写真界面点击下载按钮，触发多行歌词转双行歌词的问题
- 2018-01-24：  
1.修复了点击退出APP导致播放状态没有改变的bug  
2.在“我的页面”隐藏了退出APP按钮  
3.增加定时播放功能  
4.修复了线程中开启线程的问题  
- 2018-01-23：  
1.使用线程池替代之前的线程  
2.修复了显示锁屏界面  
- 2018-01-23：  
1.在splash页面增加了权限判断  
2.修复android8.0出现Context.startForegroundService() did not then call Service.startForeground()  
- 2018-01-23(@lp):之前退出会黑一下,改为全局退出方式 需要退出的地方调用 HPApplication.getInstance().exit();
- 2018-01-22(@zlm)：修复歌曲多个歌手时，获取歌手名称的首字母出错的问题。
- 2018-01-22：  
1.优化了SwipeBackLayout.java不允许滑动时，没有阴影  
2.修复了按返回键，播放列表布局不能隐藏的问题  
3.修复了点击换肤崩溃  
- 2018-01-22(@zlm)：去掉歌手写真界面的酷狗背景图片
- 2018-01-22(@zlm)：修复播放歌曲时，界面无更新问题：修复AudioPlayerService无被调用的问题。
- 2018-01-22：对SwipeBackLayout.java也增加是否允许滑动
- 2018-01-22：  
1.使用Dialog替换歌词界面的菜单设置和歌词详情（原因是直接使用会有卡顿）  
2.尝试解决android8.0：java.lang.IllegalStateException
      Not allowed to start service Intent { cmp=bear.love.peach/.service.AudioPlayerService }: app is in background uid UidRecord  
3.优化了按住item导致能看见上一个页面的问题  
- 2018-01-22(@zlm)：
- 加大歌词行的移动时间，让歌词移动动画更流畅，有需要的小伙伴也可以根据当前歌词的行数（因为歌词换行导致动画移动不流畅）适当地动态增加移动的时间，可以使移动动画更流畅。
- 添加自定义的锁屏界面，其中部分手机（如：我的小米2s，android 5.0），需要到权限设置界面设置“锁屏显示”权限(注：歌曲只有在播放时，才会显示锁屏界面)，实现图片预览：
![](https://i.imgur.com/hZlES1d.png)
- 添加悬浮窗口权限判断，直接跳转到权限设置页面（桌面歌词暂时没实现）。
- 引用别人项目工具类，主要用于判断各种手机的权限设置页面，项目地址如下：https://github.com/SenhLinsh/Utils-Everywhere.git

- 2018-01-21：好高兴这个项目可以邀请到liupeng110参与开发
- 2018-01-19：把类里面的string字符串提取到string.xml中
- 2018-01-19：广播不能传参的，初级错误
- 2018-01-19(@zlm)：  
1.修复通知栏图标  
2.修复android7.0状态栏半透明的问题  
- 2018-01-19：把之前在application移动的全局方法改回来了，原因是每次启动很慢（暂时不知道原因）
- 2018-01-19：增加bugly异常上报
- 2018-01-19(@zlm)：修复RotateLinearLayout旋转角度在0.x时布局闪烁问题，关闭硬件加速。
- 2018-01-18:
1.去掉直接传递HPApplication作为参数
2.在Mainactivity写了一个权限检查的样例
- 2018-01-18：  
1.替换ijkplayer库  
2.统一版本  
3.删除log4j.jar、nineoldandroids.jar  
4.调整结构  
5.修复点击底部播放打开歌词界面，快速点击物理返回键会重复执行动画  
6.增加耳机线控，单击：播放/暂停 两次：下一曲 三次：上一曲  
7.增加锁屏界面、DrawerLayout布局在MainActivity页面（功能暂未实现）  
8.优化RecycleView显示界面和搜索界面  
- 2018-01-17：好高兴这个项目可以邀请到DyncKathline参与开发，期待DyncKathline可以将播放器更好地完善，也期待更多的爱好者参与开发。
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
