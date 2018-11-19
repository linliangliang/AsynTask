package com.zhengyuan.learningserverdownloadfile;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by 林亮 on 2018/11/19
 */

public class DownFileActivity extends Activity implements View.OnClickListener {
    /* SD卡根目录 */
    private File rootDie;
    /* 输出文件名称 */
    private String outFileName = "ldm1.jar";
    /* 进度条对话框 */
    private ProgressDialog pdialog;
    private MyLoadAsyncTask myLoadAsyncTask = new MyLoadAsyncTask();

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.downfile);

        checkAndCreateDir();
        getPermission();//动态获取权限
        findViewById(R.id.file_download_btn).setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.file_download_btn) {
/* 异步下载 */
            myLoadAsyncTask.execute("http://59.175.173.136:9080/plugins/EMPrisonLocation-100.jar");
        }
    }

    //AsyncTask是基于线程池进行实现的,当一个线程没有结束时,后面的线程是不能执行的.
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (myLoadAsyncTask != null && myLoadAsyncTask.getStatus() == AsyncTask.Status.RUNNING) {
            //cancel方法只是将对应的AsyncTask标记为cancelt状态,并不是真正的取消线程的执行.
            myLoadAsyncTask.cancel(true);
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
/* 实例化进度条对话框 */
        pdialog = new ProgressDialog(this);
/* 进度条对话框属性设置 */
        pdialog.setMessage("正在下载中...");
/* 进度值最大100 */
        pdialog.setMax(100);
/* 水平风格进度条 */
        pdialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
/* 无限循环模式 */
        pdialog.setIndeterminate(false);
/* 可取消 */
        pdialog.setCancelable(true);
/* 显示对话框 */
        pdialog.show();
        return pdialog;
    }

    /* 检查sdcard并创建目录文件 */
    private void checkAndCreateDir() {
/* 获取sdcard目录 */
        rootDie = Environment.getExternalStorageDirectory();
/* 新文件的目录 */
        File newFile = new File(rootDie + "/download1/");
        if (!newFile.exists()) {
/* 如果文件不存在就创建目录 */
            newFile.mkdirs();
        }
    }

    /**
     * 动态获取权限
     */
    private void getPermission() {
        PackageManager pm = getPackageManager();
        boolean permission = (PackageManager.PERMISSION_GRANTED ==
                pm.checkPermission("android.permission.WRITE_EXTERNAL_STORAGE", "com.zhengyuan.learningserverdownloadfile"));
        if (permission) {
            Toast.makeText(DownFileActivity.this, "有权限", Toast.LENGTH_SHORT).show();
        } else {
            //版本高于6.0
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.INTERNET}, 12);
            }
        }
        //申请权限
    }


    /* 异步任务，后台处理与更新UI */
    class MyLoadAsyncTask extends AsyncTask<String, String, String> {
        /* 后台线程 */
        @Override
        protected String doInBackground(String... params) {
        /* 所下载文件的URL */
            try {
                URL url = new URL(params[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                /* URL属性设置 */
                conn.setRequestMethod("GET");
                /* URL建立连接 */
                conn.connect();
                /* 下载文件的大小 */
                int fileOfLength = conn.getContentLength();
                /* 每次下载的大小与总下载的大小 */
                int totallength = 0;
                int length = 0;
                /* 输入流 */
                InputStream in = conn.getInputStream();
                /* 输出流 */
                FileOutputStream out = new FileOutputStream(new File(rootDie + "/download1/", outFileName));
                /* 缓存模式，下载文件 */
                byte[] buff = new byte[1024 * 1024];
                while ((length = in.read(buff)) > 0) {
                    totallength += length;
                    String str1 = "" + (int) ((totallength * 100) / fileOfLength);
                    publishProgress(str1);
                    out.write(buff, 0, length);
                }
/* 关闭输入输出流 */
                in.close();
                out.flush();
                out.close();


            } catch (MalformedURLException e) {
// TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
// TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }


        /* 预处理UI线程 */
        @Override
        protected void onPreExecute() {
            showDialog(0);
            super.onPreExecute();
        }


        /* 结束时的UI线程 */
        @Override
        protected void onPostExecute(String result) {
            dismissDialog(0);
            super.onPostExecute(result);
        }

        /* 处理UI线程，会被多次调用,触发事件为publicProgress方法 */
        @Override
        protected void onProgressUpdate(String... values) {
            /* 进度显示 */
            pdialog.setProgress(Integer.parseInt(values[0]));
        }
    }
}
