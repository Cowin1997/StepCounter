package www.hqu.edu.cn.lxb.stepcounter;


import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.sqlite.SQLiteDatabase;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TimeUtils;
import android.view.View;
import android.widget.Button;

import android.widget.RadioGroup;
import android.widget.TextView;


import java.text.SimpleDateFormat;
import java.util.Calendar;

import www.hqu.edu.cn.lxb.stepcounter.DataBase.DateBase;
import www.hqu.edu.cn.lxb.stepcounter.Services.StepService;
import www.hqu.edu.cn.lxb.stepcounter.Services.StepService2;
import www.hqu.edu.cn.lxb.stepcounter.Services.orentationService;

import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;

public class MainActivity extends AppCompatActivity {
    TextView et; //步数显示
    Button begin; // 开始按钮
    Button finish;//结束按钮
    Button clear; // 清零按钮
    Button history;//查看历史步数按钮
    StepService.MyBinder myBinder;
    StepService2.MyBinder myBinder2;
    orentationService.MyorentationBinder myorentationBinder;
    //算法单选框按钮
    RadioGroup radioGroup;
    int Checkid;
    //服务是否启动
    boolean ServiceStart= false;
    //是否清零
    boolean isclear = false;
    //当前方向
    TextView nowdir;
     MyHandler myHandler = new MyHandler();
     TextView beginTime;
     TextView finishTime;
    DateBase DBUtil ;
    SQLiteDatabase myDB ;
    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if(Checkid == R.id.radiobutton1)
                myBinder = (StepService.MyBinder)service;
            if(Checkid==R.id.radiobutton2)
         myBinder2 = (StepService2.MyBinder)service;
         myHandler.sendEmptyMessage(0x1234);
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {}
    };
    ServiceConnection conn2 = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myorentationBinder = (orentationService.MyorentationBinder)service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainactivity_main);
       DBUtil = new DateBase(this.getFilesDir().toString()+"/my.db3");

        myDB = DBUtil.getDB();
        System.out.println(this.getFilesDir());
        /**
         * 获取UI
         */
        et = (TextView) findViewById(R.id.stepview);
        if(DBUtil.getSteps(myDB,(new SimpleDateFormat("yyyy/MM/dd")).format(Calendar.getInstance().getTime()))>-1)
            et.setText(String.valueOf(DBUtil.getSteps(myDB,(new SimpleDateFormat("yyyy/MM/dd")).format(Calendar.getInstance().getTime()))));
        begin = (Button)findViewById(R.id.beginbutton) ;
        finish = (Button)findViewById(R.id.finishbutton);
        radioGroup = (RadioGroup)findViewById(R.id.rg);
        finish.setEnabled(false);
        clear = (Button)findViewById(R.id.clearstep) ;
        Checkid = R.id.radiobutton1;
        Checkid = radioGroup.getCheckedRadioButtonId();
        nowdir = (TextView)findViewById(R.id.dirnow);

        beginTime = (TextView) findViewById(R.id.begintime);
        finishTime = (TextView)findViewById(R.id.finishtime);
        history = (Button)findViewById(R.id.history);

        history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this,History.class);

                    if(ServiceStart &&Checkid==R.id.radiobutton1) DBUtil.InsertorUpdate(myDB,(new SimpleDateFormat("yyyy/MM/dd")).format(Calendar.getInstance().getTime()),myBinder.getStep());
                if(ServiceStart &&Checkid==R.id.radiobutton2) DBUtil.InsertorUpdate(myDB,(new SimpleDateFormat("yyyy/MM/dd")).format(Calendar.getInstance().getTime()),myBinder2.getStep());
                    startActivity(intent);
            }
        });











        //步数清零按钮
       clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Checkid = radioGroup.getCheckedRadioButtonId();
                isclear = true;
                if(Checkid == R.id.radiobutton1 &&ServiceStart==true)
                   myBinder.reset();
               if(Checkid==R.id.radiobutton2&&ServiceStart==true)
                 myBinder2.reset();
               else{
                   DBUtil.updateSteps(myDB,(new SimpleDateFormat("yyyy/MM/dd")).format(Calendar.getInstance().getTime()),0);
                   et.setText(DBUtil.getSteps(myDB,(new SimpleDateFormat("yyyy/MM/dd")).format(Calendar.getInstance().getTime()))+"");
               }
            }
        });

        radioGroup = (RadioGroup)findViewById(R.id.rg);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(ServiceStart == true)
                finish.callOnClick();
                Checkid = checkedId;

            }
        });
        //开始按钮
        begin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ServiceStart = true;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                      if(Checkid == R.id.radiobutton1)
                        setupService();
                      if(Checkid==R.id.radiobutton2)
                          setupService2();
                    }
                }).start();
                beginTime.setText((new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss")).format(Calendar.getInstance().getTime()));
                finishTime.setText("");
                finish.setEnabled(true);
            }
        });
       //结束计步按钮
        finish.setOnClickListener(new View.OnClickListener() {
            Intent intent;
            @Override
            public void onClick(View v) {
                ServiceStart =false;
                Intent direintent = new Intent(MainActivity.this, orentationService.class);
                //取消服务绑定
                if (Checkid ==R.id.radiobutton1) {
                    intent = new Intent(MainActivity.this, StepService.class);
                    unbindService(conn);
                }
                if(Checkid ==R.id.radiobutton2 ) {
                    intent = new Intent(MainActivity.this, StepService2.class);
                    unbindService(conn);
                }
                unbindService(conn2);
               stopService(intent);
                stopService(direintent);
                finish.setEnabled(false);
                SimpleDateFormat df = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
                Calendar calendar = Calendar.getInstance();
                finishTime.setText(df.format(calendar.getTime()));

            }
        });

    }

    /**
     * 开启服务
     */
    private void setupService(){
        Intent intent = new Intent(this, StepService.class);
        Intent direintent = new Intent(this,orentationService.class);

        bindService(direintent,conn2,0);
        bindService(intent, conn, 0);
        startService(intent);
        startService(direintent);

    }
    private void setupService2(){
        Intent intent = new Intent(this, StepService2.class);
        Intent direintent = new Intent(this,orentationService.class);
        startService(intent);
        startService(direintent);
        bindService(direintent,conn2,0);
        bindService(intent, conn, 0);

    }

   // Handle 更新UI
    public class MyHandler extends Handler {
      //  String last ="";
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0x1234:
                    if(Checkid == R.id.radiobutton1&& ServiceStart ==true ) {
                        et.setText("" + myBinder.getStep());
                        nowdir.setText(myorentationBinder.getdire());


                    }
                    if(Checkid == R.id.radiobutton2 && ServiceStart ==true ) {
                        et.setText("" + myBinder2.getStep());
                            nowdir.setText(myorentationBinder.getdire());
                    }
                    if(isclear ==true) {
                        et.setText("0");
                        isclear =false;
                    }
                    sendEmptyMessageDelayed(0x1235,500);
                break;
                case 0x1235:
                    sendEmptyMessage(0x1234);
                    break;
            }
        }
    }

    @Override
    protected void onResume() {
        //应用返回栈顶时，读取今天的数据，因为后台运行中是不断对数据库的步数进行更新的
        if(DBUtil.getSteps(myDB,(new SimpleDateFormat("yyyy/MM/dd")).format(Calendar.getInstance().getTime()))>-1)  et.setText(String.valueOf(DBUtil.getSteps(myDB,(new SimpleDateFormat("yyyy/MM/dd")).format(Calendar.getInstance().getTime()))));
        System.out.println("Activity is OnResume!!");
        super.onResume();
    }

    @Override
    protected void onPause() {
        //应用到后台的时候，需要即时对步数进行更新
        if(ServiceStart &&Checkid==R.id.radiobutton1) DBUtil.InsertorUpdate(myDB,(new SimpleDateFormat("yyyy/MM/dd")).format(Calendar.getInstance().getTime()),myBinder.getStep());
        if(ServiceStart &&Checkid==R.id.radiobutton2) DBUtil.InsertorUpdate(myDB,(new SimpleDateFormat("yyyy/MM/dd")).format(Calendar.getInstance().getTime()),myBinder2.getStep());
        System.out.println("Activity is OnPause!!");
        super.onPause();
    }

    @Override
    protected void onStop() {
        if(ServiceStart &&Checkid==R.id.radiobutton1) DBUtil.InsertorUpdate(myDB,(new SimpleDateFormat("yyyy/MM/dd")).format(Calendar.getInstance().getTime()),myBinder.getStep());
        if(ServiceStart &&Checkid==R.id.radiobutton2) DBUtil.InsertorUpdate(myDB,(new SimpleDateFormat("yyyy/MM/dd")).format(Calendar.getInstance().getTime()),myBinder2.getStep());
        System.out.println("Activity is OnStop!!");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        //退出时如果服务还在运行，则自动调用结束计步对应的功能
        if(ServiceStart)
        finish.callOnClick();
        //关闭数据库
        if(myDB != null &&myDB.isOpen())
            myDB.close();
        System.out.println("Activity is OnDestory!!");
        super.onDestroy();
    }
}
