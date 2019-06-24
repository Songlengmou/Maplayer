package com.duomap.maplayer.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Administrator on 2018-01-18.
 */

public class FileUtils {

    /**
     * 复制文件
     * @param srcFile 原文件
     * @param destFile 目标文件
     * @return
     */
    public static boolean copyFile(String srcFile, String destFile){
        try {
            //判断原文件目录和目标文件目录是否存在，如果不存在，则新建；
            mkDirsFromFile(getDirFromFile(srcFile));
            mkDirsFromFile(getDirFromFile(destFile));

            InputStream streamFrom = new FileInputStream(srcFile);
            OutputStream streamTo = new FileOutputStream(destFile);
            byte buffer[] = new byte[1024];
            int len;
            while ((len = streamFrom.read(buffer)) > 0){
                streamTo.write(buffer, 0, len);
            }
            streamFrom.close();
            streamTo.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 移动文件
     */
    public static boolean moveFile(String srcFile, String destFile){
        if(copyFile(srcFile, destFile)){
            deleteFile(srcFile);
            return true;
        }
        return false;
    }



    /**
     * 删除文件（包括目录）
     * @param file
     */
    public static void deleteFile(File file){
        //如果是目录，删除目录及其子目录及子文件；
        if(file.isDirectory()){
            File[] files = file.listFiles();
            for(File fileDel : files){
                deleteFile(fileDel);
            }
            //如果不执行下面这句，目录下所有文件都删除了，但是还剩下子目录空文件夹
            file.delete();
        } else {
            file.delete();
        }
    }

    public static void deleteFile(String filePath){
        File file = new File(filePath);
        deleteFile(file);
    }

    /**
     * 将路径输入方法中，如果不存在文件夹，将逐级新建文件夹；
     * @param filePath
     */
    public static void mkDirsFromFile(String filePath){
        File temp = new File(filePath);//要保存文件先创建文件夹
        if (!temp.exists()) {
            temp.mkdirs();
        }
    }

    /**
     * 通过文件路径获取所在的目录
     */
    public static String getDirFromFile(String filePath){
        int pos = filePath.lastIndexOf("/");
        return filePath.substring(0, pos+1);
    }
}
