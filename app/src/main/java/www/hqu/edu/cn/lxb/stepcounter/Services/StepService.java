package www.hqu.edu.cn.lxb.stepcounter.Services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import www.hqu.edu.cn.lxb.stepcounter.DataBase.DateBase;
import www.hqu.edu.cn.lxb.stepcounter.MainActivity;

import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;


public class StepService extends Service implements SensorEventListener {
    private SensorManager sensorManager;    //传感器管理者
    private StepDetector stepDetector;
    private MyBinder myBinder = new MyBinder();
    private PowerManager.WakeLock mWakeLock;
    // 通知框
    private NotificationCompat.Builder builder;
    NotificationManager mNotificationManager;
    private  int  pre =stepDetector.CURRENT_STEP ;
    Timer timer;
    TimerTask task;


    DateBase DBUtil;
    SQLiteDatabase myDB;

    SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");
    Calendar calendar = Calendar.getInstance();
    String date = df.format(calendar.getTime());



    public class MyBinder extends Binder {
        public int getStep(){
            return stepDetector.CURRENT_STEP;
        }
        public void reset(){
            stepDetector.CURRENT_STEP=0;
            DBUtil.updateSteps(myDB,date,0);
        }
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        //在新的线程中启动计步检测（通过复杂的算法）
        new Thread(new Runnable() {
            @Override
            public void run() {
                //启动步数监测器
                startStepDetector();
            }
        }).start();

        DBUtil= new DateBase(this.getFilesDir().toString()+"/my.db3");
        myDB = DBUtil.getDB();
     //   System.out.println("***********************"+DBUtil.getSteps(myDB,date));
        //服务开启时候先检查今天是否已经有数据记录存在表中，有则更新无则插入
        if(DBUtil.getSteps(myDB,date)>-1) stepDetector.CURRENT_STEP =DBUtil.getSteps(myDB,date);
        DBUtil.InsertorUpdate(myDB,date,stepDetector.CURRENT_STEP);
        stepDetector.CURRENT_STEP =DBUtil.getSteps(myDB,date);
        getLock(this);
        System.out.println(this.getFilesDir().toString()+"/my.db3");
        System.out.println("STEPSERVICE IS ON CREATED!");

    }
    @Override
    public void onStart(Intent intent, int startId) {


        super.onStart(intent, startId);
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        //传感器绑定在检测器上，通过回调实现检测接口的检测器中的该方法，已经自动回调了
        //所以这里可以为空
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onDestroy() {
        DBUtil.InsertorUpdate(myDB,date,stepDetector.CURRENT_STEP);
        DBUtil.Close(myDB);
        System.out.println("*******  SERVICE IS ONDESTROY!!!");
        releaseLock(this);
        stopStepDetector();
        super.onDestroy();
    }
    private void startStepDetector(){
        System.out.println("NEWNEWNENW");
        sensorManager=(SensorManager)this.getSystemService(SENSOR_SERVICE);
        stepDetector =new StepDetector(this);
        Sensor sensor=sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);//使用加速度传感器
        sensorManager.registerListener(stepDetector,sensor,SensorManager.SENSOR_DELAY_UI);

        }

    private void stopStepDetector(){
       sensorManager.unregisterListener(stepDetector);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
     //   updateNotification("今日步数:"+StepDetector.CURRENT_STEP+" 步");

        System.out.println("STEPSERVICE IS ON START!");
        timer = new Timer();
        task = new TimerTask() {
            @Override
            public void run() {
                updateNotification("今日步数:"+StepDetector.CURRENT_STEP+" 步");
                //判断24点过后，新的一天的时候，保存昨天的步数，更新今天的步数；
                if(!date.equals((new SimpleDateFormat("yyyy/MM/dd")).format(Calendar.getInstance().getTime()))){
                    DBUtil.InsertorUpdate(myDB,date,StepDetector.CURRENT_STEP);
                    date=(new SimpleDateFormat("yyyy/MM/dd")).format(Calendar.getInstance().getTime());

                    DBUtil.InsertorUpdate(myDB,date,StepDetector.CURRENT_STEP=0);
                }

            }
        };
        timer.schedule(task,200,4000);
        getLock(this);
        return START_NOT_STICKY;
    }



     synchronized private PowerManager.WakeLock getLock(Context context){

        if(mWakeLock==null){
            PowerManager mgr=(PowerManager)context.getSystemService(Context.POWER_SERVICE);
            mWakeLock=mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,StepService.class.getName());
            mWakeLock.setReferenceCounted(true);
            Calendar c=Calendar.getInstance();
            c.setTimeInMillis((System.currentTimeMillis()));
            int hour =c.get(Calendar.HOUR_OF_DAY);
            if(hour>=23||hour<=6){
                mWakeLock.acquire(5000);
            }else{
                mWakeLock.acquire(300000);
            }
        }
        Log.v("MRSSAGRS","得到了锁");
        return (mWakeLock);
    }



 private PowerManager.WakeLock releaseLock(Context context) {
        if (mWakeLock != null) {
            if (mWakeLock.isHeld()) {
                mWakeLock.release();
                Log.v("MESSAGES", "释放锁");
            }
            mWakeLock = null;
        }
        return (mWakeLock);
    }

    /**
     * 更新通知栏
     */
    private void updateNotification(String updatemsg){
        builder=new NotificationCompat.Builder(this);
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent=PendingIntent.getActivity(this,0,
                intent,0);
        builder.setContentIntent(contentIntent);
        builder.setTicker("StepCounter++");
        builder.setContentTitle("StepCounter++");
        builder.setSmallIcon(android.support.compat.R.drawable.notification_tile_bg);
        //设置不可清除
        builder.setOngoing(true);
        builder.setContentText(updatemsg);
        Notification notification=builder.build();
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotificationManager.notify(0,notification);

    }


}

