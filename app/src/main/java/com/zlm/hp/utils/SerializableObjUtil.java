package com.zlm.hp.utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Serializable对象处理类
 * Created by zhangliangming on 2017/8/15.
 */

public class SerializableObjUtil {

    /**
     * 保存
     *
     * @param filePath
     * @param object
     */
    public static void saveObj(String filePath, Object object) {
        try {
            FileOutputStream outputStream = new FileOutputStream(filePath);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(object);

            objectOutputStream.close();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 读取
     *
     * @param filePath
     * @return
     */
    public static Object readObj(String filePath) {
        FileInputStream fileInputStream = null;
        ObjectInputStream objectInputStream = null;
        try {
            fileInputStream = new FileInputStream(filePath);
            objectInputStream = new ObjectInputStream(fileInputStream);
            return objectInputStream.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (objectInputStream != null)
                try {
                    objectInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            if (fileInputStream != null)
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return null;
    }
}
