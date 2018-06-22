package www.hqu.edu.cn.lxb.stepcounter.DataBase;



import android.content.ContentValues;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;





public class DateBase {
    public static SQLiteDatabase myDB;
    public SQLiteDatabase getDB(){
        return myDB;
    }
    public DateBase(String url){
      //  System.out.println(context.getFilesDir().toString()+"/my.db3");
        myDB = SQLiteDatabase.openOrCreateDatabase(url,null);
    }


    //数据插入
    public   void insertData(SQLiteDatabase sqLiteDatabase,String date,int steps){
        // sqLiteDatabase.execSQL("insert into step values(null,?,?) ",new Object[]{date,steps});
            InitDB( sqLiteDatabase);
            ContentValues values = new ContentValues();
            values.put("steps", steps);
            values.put("date", date);
            sqLiteDatabase.insert("step", "_id", values);
            System.out.println("插入数据成功");


    }
//初始化数据库，如果表不存在就建表
    public void InitDB(SQLiteDatabase sqLiteDatabase){
        sqLiteDatabase.execSQL("create table if not exists step ( _id integer "+"primary key autoincrement,"+"date text, " +
                " steps integer)");
    }
//获得对应日期的步数
    public int getSteps(SQLiteDatabase sqLiteDatabase,String date){
        InitDB(sqLiteDatabase);
        // sqLiteDatabase.
        int t  = -1;
        try {
            //getApplicationContext().getFilesDir().toString()+"/my.db3"
            Cursor cursor = sqLiteDatabase.rawQuery("select steps from step where date = ?" , new String[]{date});
            while (cursor.moveToNext()) {
                t = cursor.getInt(0);
                System.out.println(t);
            }
        }
        catch (SQLException e){
            System.out.println("GETSTEPS ERROR");
        }

        return t;

    }
 //更新
    public  void  updateSteps(SQLiteDatabase sqLiteDatabase,String date,int updateSteps){

        ContentValues values = new ContentValues();
        values.put("steps",updateSteps);
        sqLiteDatabase.update("step",values,"date = ?", new String[]{date});
        System.out.println("更新成功！");


    }
    // 关闭数据库
    public void Close(SQLiteDatabase sqLiteDatabase){
        if(sqLiteDatabase != null &&sqLiteDatabase.isOpen())
            sqLiteDatabase.close();
        System.out .println("DATEBASE IS CLOSED");
    }
    //如果不存在就进行插入，存在的话就进行更新
    public void InsertorUpdate(SQLiteDatabase sqLiteDatabase,String date,int updateSteps){
        System.out.println("isexist(sqLiteDatabase,date) is "+isexist(sqLiteDatabase,date));
        System.out.println("( updateSteps > getSteps(sqLiteDatabase,date)) is "+( updateSteps >= getSteps(sqLiteDatabase,date)));
       if(isexist(sqLiteDatabase,date) && ( updateSteps >= getSteps(sqLiteDatabase,date))){
            updateSteps(sqLiteDatabase,date,updateSteps);
        }
       else
           insertData(sqLiteDatabase,date,updateSteps);
    }
    //查询对应日期的数据是否存在
    public boolean isexist(SQLiteDatabase sqLiteDatabase,String date){
        InitDB(sqLiteDatabase);
        Cursor cursor = sqLiteDatabase.query("step",new String[]{"steps"},"date = ?",new String[]{date},null,null,null);
        System.out.println(cursor.getCount()+"*/*/***/*///*/*/*/*/*/*/*/*//*");
        if(cursor.getCount() !=0)
            return true;
        else
            return false;
    }

}
