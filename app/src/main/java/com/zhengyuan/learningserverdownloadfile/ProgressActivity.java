package com.zhengyuan.learningserverdownloadfile;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ProgressBar;

/**
 * Created by 林亮 on 2018/11/19
 */

public class ProgressActivity extends Activity {
    private ProgressBar progressBar;
    private MyAsyncTask myAsyncTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.progress);
        progressBar = (ProgressBar) findViewById(R.id.progress);
        myAsyncTask = new MyAsyncTask();
        //启动异步任务的处理
        myAsyncTask.execute();
    }

    //AsyncTask是基于线程池进行实现的,当一个线程没有结束时,后面的线程是不能执行的.
    @Override
    protected void onPause() {
        super.onPause();
        if (myAsyncTask != null && myAsyncTask.getStatus() == AsyncTask.Status.RUNNING) {
            //cancel方法只是将对应的AsyncTask标记为cancelt状态,并不是真正的取消线程的执行.
            myAsyncTask.cancel(true);
        }
    }

    class MyAsyncTask extends AsyncTask<Void, Integer, Void> {
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            //通过publishProgress方法传过来的值进行进度条的更新.
            //更新ui进程中的UI
            progressBar.setProgress(values[0]);
        }

        @Override
        protected Void doInBackground(Void... params) {
            //使用for循环来模拟进度条的进度.
            for (int i = 0; i < 100; i++) {
                //如果task是cancel状态,则终止for循环,以进行下个task的执行.
                if (isCancelled()) {
                    break;
                }
                //调用publishProgress方法将自动触发onProgressUpdate方法来进行进度条的更新.
                publishProgress(i);
                try {
                    //通过线程休眠模拟耗时操作
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }
}
