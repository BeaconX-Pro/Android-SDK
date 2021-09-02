package com.moko.beaconxpro;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;

import com.elvishew.xlog.LogConfiguration;
import com.elvishew.xlog.LogLevel;
import com.elvishew.xlog.XLog;
import com.elvishew.xlog.flattener.PatternFlattener;
import com.elvishew.xlog.printer.AndroidPrinter;
import com.elvishew.xlog.printer.Printer;
import com.elvishew.xlog.printer.file.FilePrinter;
import com.elvishew.xlog.printer.file.naming.ChangelessFileNameGenerator;
import com.moko.beaconxpro.utils.IOUtils;
import com.moko.ble.lib.log.ClearLogBackStrategy;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import es.dmoral.toasty.Toasty;

public class BaseApplication extends Application {

    private static final String TAG = "mokoBeaconXPro";
    private static final String LOG_FILE = "mokoBeaconXPro.txt";
    private static final String LOG_FOLDER = "mokoBeaconXPro";
    public static String PATH_LOGCAT;

    @Override
    public void onCreate() {
        super.onCreate();
        Toasty.Config.getInstance().apply();
        initXLog();
        Thread.setDefaultUncaughtExceptionHandler(new BTUncaughtExceptionHandler());
    }

    private void initXLog() {
        // 初始化Xlog
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            // 优先保存到SD卡中
            PATH_LOGCAT = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + LOG_FOLDER;
        } else {
            // 如果SD卡不存在，就保存到本应用的目录下
            PATH_LOGCAT = getFilesDir().getAbsolutePath() + File.separator + LOG_FOLDER;
        }
        Printer filePrinter = new FilePrinter.Builder(PATH_LOGCAT)
                .fileNameGenerator(new ChangelessFileNameGenerator(LOG_FILE))
                .backupStrategy(new ClearLogBackStrategy())
                .flattener(new PatternFlattener("{d yyyy-MM-dd HH:mm:ss} {l}/{t}: {m}"))
                .build();
        LogConfiguration config = new LogConfiguration.Builder()
                .tag(TAG)
                .logLevel(LogLevel.ALL)
                .build();
        XLog.init(config, new AndroidPrinter(), filePrinter);
    }

    public class BTUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
        private static final String LOGTAG = "BTUncaughtExceptionHandler";

        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            // 读取stacktrace信息
            final Writer result = new StringWriter();
            final PrintWriter printWriter = new PrintWriter(result);
            ex.printStackTrace(printWriter);
            StringBuffer errorReport = new StringBuffer();
            // 获取packagemanager的实例
            PackageManager packageManager = getPackageManager();
            // getPackageName()是你当前类的包名，0代表是获取版本信息
            PackageInfo packInfo = null;
            try {
                packInfo = packageManager.getPackageInfo(getPackageName(), 0);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            if (packInfo != null) {
                String version = packInfo.versionName;
                errorReport.append(version);
                errorReport.append("\r\n");
            }
            errorReport.append(result.toString());
            IOUtils.setCrashLog(errorReport.toString());
            XLog.e("uncaughtException errorReport=" + errorReport);
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }
}
