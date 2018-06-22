package www.hqu.edu.cn.lxb.stepcounter.Services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

public class orentationService extends Service implements SensorEventListener {
    private SensorManager mSensorManager;

    private Sensor accelerometer; // 加速度传感器
    private Sensor magnetic; // 地磁场传感器

    private String returnorentation;

    private SensorManager sm;


    private float[] accelerometerValues = new float[3];
    private float[] magneticFieldValues = new float[3];

  //  private static final String TAG = "---MainActivity";
    MyorentationBinder myorentationBinder = new MyorentationBinder();
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return myorentationBinder;
    }
     public class MyorentationBinder extends Binder{
        public String getdire(){
            return returnorentation;
        }
     }
    @Override
    public void onCreate() {
        System.out.println("方向服务OnCreate!!!");
        super.onCreate();
        // 实例化传感器管理者
        sm = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetic = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        sm.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sm.registerListener(this, magnetic,SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    public void onDestroy() {
        System.out.println("方向服务OnDestroy!!!");
        sm.unregisterListener(this);
        super.onDestroy();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            magneticFieldValues = event.values;
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            accelerometerValues = event.values;
        calculateOrientation();
    //    System.out.println(returnorentation+"**************************");
}

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    private void calculateOrientation() {
        float[] values = new float[3];
        float[] R = new float[9];
        SensorManager.getRotationMatrix(R, null, accelerometerValues,
                magneticFieldValues);
        SensorManager.getOrientation(R, values);
        values[0] = (float) Math.toDegrees(values[0]);
        if (values[0] >= -5 && values[0] < 5) {
            returnorentation = "正北";
        } else if (values[0] >= 5 && values[0] < 85) {
            returnorentation = "东北";
        } else if (values[0] >= 85 && values[0] <= 95) {
            returnorentation = "正东";
        } else if (values[0] >= 95 && values[0] < 175) {
            returnorentation = "东南";
        } else if ((values[0] >= 175 && values[0] <= 180)
                || (values[0]) >= -180 && values[0] < -175) {
            returnorentation = "正南";
        } else if (values[0] >= -175 && values[0] < -95) {
            returnorentation = "西南";
        } else if (values[0] >= -95 && values[0] < -85) {
            returnorentation = "正西";
        } else if (values[0] >= -85 && values[0] < -5) {
            returnorentation = "西北";
        }
    }

}

