package com.zlm.hp.mp3.lyrics.utils;

/**
 * 
 * @author zhangliangming
 * 
 */
public class TimeUtils {
	/**
	 * @功能 将时间字符串转换成整数
	 * @param timeString
	 *            时间字符串 00:00.00
	 * @return
	 */
	public static int parseInteger(String timeString) {
		timeString = timeString.replace(":", ".");
		timeString = timeString.replace(".", "@");
		String timedata[] = timeString.split("@");
		if (timedata.length == 3) {
			int m = Integer.parseInt(timedata[0]); // 分
			int s = Integer.parseInt(timedata[1]); // 秒
			int ms = Integer.parseInt(timedata[2]); // 毫秒
			int currTime = (m * 60 + s) * 1000 + ms;
			return currTime;
		} else if (timedata.length == 2) {
			int m = Integer.parseInt(timedata[0]); // 分
			int s = Integer.parseInt(timedata[1]); // 秒
			int currTime = (m * 60 + s) * 1000;
			return currTime;
		}
		return 0;
	}

	/**
	 * 时间格式转换
	 * 
	 * @param time
	 * @return
	 */
	public static String parseString(int time) {

		time /= 1000;
		int minute = time / 60;
		// int hour = minute / 60;
		int second = time % 60;
		minute %= 60;
		return String.format("%02d:%02d", minute, second);
	}
}
