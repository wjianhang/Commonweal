package com.goldenratio.commonweal.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import com.goldenratio.commonweal.R;
import com.goldenratio.commonweal.bean.Help;
import com.goldenratio.commonweal.bean.Help_Top;

import java.io.Serializable;
import java.util.List;

import c.b.BP;
import cn.bmob.push.BmobPush;
import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobInstallation;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

public class SplashActivity extends Activity {

    private List<Help> mHelpLlist;
    private List<Help_Top> mHelpTopList;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


        String libName = "bmob"; // 库名, 注意没有前缀lib和后缀.so
        System.loadLibrary(libName);

        //初始化Bmob
        Bmob.initialize(this, "727a409235aab18ae7b1e1f3933c9a64");
        // 使用推送服务时的初始化操作
        BmobInstallation.getCurrentInstallation().save1();
        // 启动推送服务
        BmobPush.startWork(this);
        BP.init(this, "727a409235aab18ae7b1e1f3933c9a64");

        initData();
        isFirst();

//        startMain();
    }

    private void startMain() {
        //延时来启动主界面,postDelayed方法是在设定的时间后执行Runnable中的run()
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                intent = new Intent(SplashActivity.this,MainActivity.class);
                intent.putExtra("help", (Serializable) mHelpLlist);
                intent.putExtra("top", (Serializable) mHelpTopList);
                startActivity(intent);
                finish();
            }
        },3000);
    }

    /**
     * 预加载项目页面的数据
     */
    private void initData() {
        BmobQuery<Help> bmobQuery = new BmobQuery<>();
        bmobQuery.order("-createdAt");
        bmobQuery.findObjects(new FindListener<Help>() {
            @Override
            public void done(List<Help> list, BmobException e) {
                if (e == null) {
                    mHelpLlist = list;
                } else {
                    mHelpLlist = null;
                }
            }
        });
        BmobQuery<Help_Top> bmobQueryTop = new BmobQuery<>();
        bmobQueryTop.order("-createdAt");
        bmobQueryTop.findObjects(new FindListener<Help_Top>() {
            @Override
            public void done(List<Help_Top> list, BmobException e) {
                if (e == null) {
                    mHelpTopList = list;
                } else {
                    mHelpTopList = null;
                }
            }

        });

    }

    public void isFirst() {
        boolean isFirst;
        SharedPreferences pref = getSharedPreferences("first",Activity.MODE_PRIVATE);
        isFirst = pref.getBoolean("status",true);
        if (isFirst){
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean("status",false);
            editor.apply();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    intent = new Intent(SplashActivity.this,MainActivity.class);
                    intent.putExtra("help", (Serializable) mHelpLlist);
                    intent.putExtra("top", (Serializable) mHelpTopList);
                    Intent intent1 = new Intent(SplashActivity.this,GuideActivity.class);
                    startActivity(intent1);
                }
            },3000);

        }else {
            startMain();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        startActivity(intent);
        finish();
    }
}
