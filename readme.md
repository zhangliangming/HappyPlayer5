# 更新日志 #
- 2018-01-14：
1.替换ijkplayer库
2.统一版本
3.删除log4j.jar、nineoldandroids.jar
4.调整结构
5.修复点击底部播放打开歌词界面，快速点击物理返回键会重复执行动画
6.增加耳机线控，单击：播放/暂停 两次：下一曲 三次：上一曲
7.增加锁屏界面、DrawerLayout布局在MainActivity页面（功能暂未实现）
8.优化RecycleView显示界面和搜索界面
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
