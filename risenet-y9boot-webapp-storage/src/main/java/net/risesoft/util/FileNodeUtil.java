package net.risesoft.util;

public class FileNodeUtil {

    public static Integer fileTypeConvert(String type) {
        Integer fileType = 0;
        String photo = "jpg,jpeg,png,gif,ico,svg,webp,cdr,bmp";
        String word = "doc,docx,xls,xlsx,ppt,pptx,wps,pdf,txt";
        String other = "exe,java,vue,js,css,xml,json,htm,html";
        String pakeage = "rar,zip,war";
        String video = "mp4,avi";
        String music = "mp3,wma,flac,aac,mmf,amr,m4a,m4r,ogg,mp2,wav,wv";

        if (photo.contains(type.toLowerCase())) {
            fileType = 1;
        } else if (word.contains(type.toLowerCase())) {
            fileType = 2;
        } else if (video.contains(type.toLowerCase())) {
            fileType = 3;
        } else if (music.contains(type.toLowerCase())) {
            fileType = 4;
        } else if (pakeage.contains(type.toLowerCase())) {
            fileType = 5;
        } else {
            fileType = 6;
        }
        return fileType;
    }

}
