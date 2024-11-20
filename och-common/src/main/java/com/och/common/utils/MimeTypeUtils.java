package com.och.common.utils;

import com.och.common.enums.FileTypeEnum;

/**
 * 媒体类型工具类
 *
 * @author danmo
 */
public class MimeTypeUtils {

    public static final String[] IMAGE_EXTENSION = {"bmp", "gif", "jpg", "jpeg", "png"};

    public static final String [] FILE_EXTENSION = {"doc","pdf", "docx", "xls", "xlsx", "ppt", "pptx", "html", "htm", "txt", "pdf", "xmind","rar", "zip", "gz", "bz2"};


    public static final String[] MEDIA_EXTENSION = {"mp3", "wav", "wma", "wmv", "mid", "avi", "mpg",
            "asf", "rm", "rmvb"};

    public static final String[] VIDEO_EXTENSION = {"mp4", "avi", "rmvb"};

    public static final String[] DEFAULT_ALLOWED_EXTENSION = {
            // 图片
            "bmp", "gif", "jpg", "jpeg", "png",
            // word excel powerpoint
            "doc", "docx", "xls", "xlsx", "ppt", "pptx", "html", "htm", "txt",
            // 压缩文件
            "rar", "zip", "gz", "bz2",
            // 视频格式
            "mp4", "avi", "rmvb",
            //音频
            "wav", "mp3",
            // pdf
            "pdf"};

    public static FileTypeEnum getFileType(String extension)
    {
        for (String imageType : IMAGE_EXTENSION) {
            if (imageType.equalsIgnoreCase(extension)){
                return FileTypeEnum.IMAGE;
            }
        }

        for (String fileType : FILE_EXTENSION) {
            if (fileType.equalsIgnoreCase(extension)){
                return FileTypeEnum.FILE;
            }
        }

        for (String mediaType : MEDIA_EXTENSION) {
            if (mediaType.equalsIgnoreCase(extension)){
                return FileTypeEnum.VOICE;
            }
        }

        for (String videoType : VIDEO_EXTENSION) {
            if (videoType.equalsIgnoreCase(extension)){
                return FileTypeEnum.VIDEO;
            }
        }
        return FileTypeEnum.OTHER;
    }

}
