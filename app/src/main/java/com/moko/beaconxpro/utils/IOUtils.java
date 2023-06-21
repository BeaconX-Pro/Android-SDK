package com.moko.beaconxpro.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;

import com.elvishew.xlog.XLog;
import com.moko.beaconxpro.BaseApplication;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import androidx.annotation.RequiresApi;

public class IOUtils {
    public static final String CRASH_FILE = "crash_log.txt";

    /**
     * 判断SDCard是否存在 [当没有外挂SD卡时，内置ROM也被识别为存在sd卡]
     *
     * @return
     */
    public static boolean isSdCardExist() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }

    /**
     * 获取SD卡根目录路径
     *
     * @return
     */
    public static String getSdCardPath() {
        boolean exist = isSdCardExist();
        String sdpath = "";
        if (exist) {
            sdpath = BaseApplication.PATH_LOGCAT;
        }
        return sdpath;

    }

    /**
     * 获取默认的文件路径
     *
     * @param context
     * @return
     */
    public static String getDefaultFilePath(Context context) {
        String filepath = "";
        File file = new File(BaseApplication.PATH_LOGCAT, CRASH_FILE);
        try {
            if (file.exists()) {
                filepath = file.getAbsolutePath();
            } else {
                file.createNewFile();
                filepath = file.getAbsolutePath();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return filepath;
    }

    /**
     * 获取指定的文件路径
     *
     * @return
     */
    public static String getFilePath(String fileName) {
        String filepath = "";
        File file = new File(BaseApplication.PATH_LOGCAT, fileName);
        try {
            if (file.exists()) {
                filepath = file.getAbsolutePath();
            } else {
                file.createNewFile();
                filepath = file.getAbsolutePath();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return filepath;
    }

    public static boolean isFileExist(String filePath) {
        if (!isSdCardExist()) {
            return false;
        }
        File file = new File(filePath);
        if (file.exists()) {
            return true;
        }
        return false;
    }

    public static void drawableToFile(Bitmap bitmap, String filePath) {
        if (bitmap == null)
            return;

        try {
            File file = new File(filePath);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 获取文件内容
     *
     * @return
     */
//    public static String getCrashLog() {
//        try {
//            File file = new File(getDefaultFilePath(null));
//            if (!file.exists()) {
//                file.createNewFile();
//            }
//            FileInputStream is = new FileInputStream(file);
//            byte[] b = new byte[is.available()];
//            is.read(b);
//            String result = new String(b);
//            return result;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return "";
//    }

    /**
     * 写文件内容
     *
     * @param info
     * @param context
     */
    public static void setCrashLog(String info, Context context) {
        if (!isSdCardExist()) {
            return;
        }
        try {
            File file = new File(getDefaultFilePath(context));
            if (!file.exists()) {
                file.createNewFile();
            }
            StringBuffer buffer = new StringBuffer();
            // 记录时间
            buffer.append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                    .format(Calendar.getInstance().getTime()));
            buffer.append("\r\n");
            // 记录机型
            buffer.append(android.os.Build.MODEL);
            buffer.append("\r\n");
            // 记录版本号
            buffer.append(android.os.Build.VERSION.RELEASE);
            buffer.append("\r\n");
            buffer.append(info);
            FileOutputStream fos = new FileOutputStream(file, true);
            fos.write(buffer.toString().getBytes());
            fos.write("\r\n".getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 保存文件到公共集合目录
     *
     * @param context
     * @param displayName：显示的文件名字
     * @return 返回插入数据对应的uri
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static void queryAndDeleteFile(Context context, String displayName) {
        Uri external = MediaStore.Downloads.EXTERNAL_CONTENT_URI;
        ContentResolver resolver = context.getContentResolver();
        String[] projection = new String[]{MediaStore.DownloadColumns._ID};
        String selection = MediaStore.DownloadColumns.DISPLAY_NAME + " LIKE ?";
        String[] args = new String[]{"%" + displayName + "%"};
        Cursor cursor = resolver.query(external, projection, selection, args, null);
        Uri fileUri = null;
        if (cursor != null) {
            while (cursor.moveToNext()) {
                fileUri = ContentUris.withAppendedId(external, cursor.getLong(0));
                resolver.delete(fileUri, null, null);
            }
            cursor.close();
        }
    }

    /**
     * 保存文件到公共集合目录
     *
     * @param context
     * @return 返回插入数据对应的uri
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static Uri insertDownloadFile(Context context, File file) {
        // 保存前先检查文件是否已存在
        queryAndDeleteFile(context, file.getName());
        ContentValues values = new ContentValues();
        values.put(MediaStore.DownloadColumns.DISPLAY_NAME, file.getName());
        values.put(MediaStore.DownloadColumns.TITLE, file.getName());
        values.put(MediaStore.DownloadColumns.MIME_TYPE, "*/*");
        values.put(MediaStore.DownloadColumns.RELATIVE_PATH, "Download/mokoBeaconXPro");
        Uri external = MediaStore.Downloads.EXTERNAL_CONTENT_URI;
        Uri uri = null;
        ContentResolver cr = context.getContentResolver();
        FileOutputStream fos = null;
        FileInputStream fis = null;
        try {
            uri = cr.insert(external, values);
            if (uri == null) {
                return null;
            }
            byte[] buffer = new byte[1024];
            ParcelFileDescriptor parcelFileDescriptor = cr.openFileDescriptor(uri, "w");
            fos = new FileOutputStream(parcelFileDescriptor.getFileDescriptor());
            fis = new FileInputStream(file.getAbsoluteFile());
            while (true) {
                int numRead = fis.read(buffer);
                if (numRead == -1) {
                    break;
                }
                fos.write(buffer, 0, numRead);
            }
            fos.flush();
        } catch (Exception e) {
            XLog.e("Failed to insert download file", e);
            if (uri != null) {
                cr.delete(uri, null, null);
                uri = null;
            }
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException e) {
                XLog.e("fail in close: " + e.getCause());
            }
        }
        return uri;
    }
}
