package base.lyrics.utils;

/**
 * @author zhangliangming
 */
public class TimeUtils {
    /**
     * @param timeString 时间字符串 00:00.00/00:00.000
     * @return
     * @功能 将时间字符串转换成整数
     */
    public static int parseInteger(String timeString) {
        timeString = timeString.replace(":", ".");
        timeString = timeString.replace(".", "@");
        String timedata[] = timeString.split("@");
        if (timedata.length == 3) {
            int m = Integer.parseInt(timedata[0]); // 分
            int s = Integer.parseInt(timedata[1]); // 秒
            int ms = 0;
            //部分lrc歌词，只精确到10倍毫秒
            if (timedata[2].length() == 3) {
                ms = Integer.parseInt(timedata[2]); // 毫秒
            } else {
                ms = Integer.parseInt(timedata[2]) * 10; // 毫秒
            }

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
     * 毫秒转时间字符串
     *
     * @param msecTotal
     * @return 00:00.000
     */
    public static String parseMMSSFFFString(int msecTotal) {
        int msec = msecTotal % 1000;
        msecTotal /= 1000;
        int minute = msecTotal / 60;
        int second = msecTotal % 60;
        minute %= 60;
        return String.format("%02d:%02d.%03d", minute, second, msec);
    }

    /**
     * 毫秒转时间字符串
     *
     * @param msecTotal
     * @return 00:00.00
     */
    public static String parseMMSSFFString(int msecTotal) {
        int msec = msecTotal % 1000 / 10;
        msecTotal /= 1000;
        int minute = msecTotal / 60;
        int second = msecTotal % 60;
        minute %= 60;
        return String.format("%02d:%02d.%02d", minute, second, msec);
    }

    /**
     * 毫秒转时间字符串
     *
     * @param msecTotal
     * @return 00:00
     */
    public static String parseMMSSString(int msecTotal) {
        msecTotal /= 1000;
        int minute = msecTotal / 60;
        int second = msecTotal % 60;
        minute %= 60;
        return String.format("%02d:%02d", minute, second);
    }
}
