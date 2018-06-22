package www.hqu.edu.cn.lxb.stepcounter.DataBase;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import www.hqu.edu.cn.lxb.stepcounter.R;

public class History extends Activity {
   public static SQLiteDatabase myDB;
    Button ok = null;
    ListView listView;
    int steps =0;
    EditText editText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout);
      /*  File file1 = new File(this.getFilesDir().toString()+"/my.db3");
        SQLiteDatabase.deleteDatabase(file1);
        */

        InitDB(myDB);
        listView = (ListView)findViewById(R.id.listview);
        editText= (EditText)findViewById(R.id.edit) ;
        ok = (Button)findViewById(R.id.ok);
        Cursor cursor = myDB.rawQuery("select * from step order by _id desc limit 0,20 ",null);
       //// inflateList(cursor);

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    String titile =editText.getText().toString();
                    insertData(myDB,titile,steps);
                    steps ++;
                    Cursor cursor = myDB.rawQuery("select * from step order by _id desc limit 0,20 ",null);
                    inflateList(cursor);
                //    getSteps(myDB,1234);
                updateSteps(myDB,"9999",9999);
                getSteps(myDB,"9999");
            }
        });
    }

    public   void insertData(SQLiteDatabase sqLiteDatabase,String date,int steps){
       // sqLiteDatabase.execSQL("insert into step values(null,?,?) ",new Object[]{date,steps});
        ContentValues values = new ContentValues();
        values.put("steps",steps);
        values.put("date",date);
        sqLiteDatabase.insert("step","_id",values);
    }

    public void InitDB(SQLiteDatabase sqLiteDatabase){
        myDB = SQLiteDatabase.openOrCreateDatabase(this.getFilesDir().toString()+"/my.db3",null);
        sqLiteDatabase.execSQL("create table step( _id integer "+"primary key autoincrement,"+"date text, " +
                " steps text)");
    }

    @Override
   public void onDestroy(){
        System.out.println("OnDestroy");
        super.onDestroy();
        if(myDB != null &&myDB.isOpen())
            myDB.close();

   }


   private void inflateList(Cursor cursor){
        cursor.moveToLast();
       SimpleCursorAdapter adapter = new SimpleCursorAdapter(History.this,R.layout.item,cursor,new String[]{"date","steps"},new int []{R.id.tv_date,R.id.tv_step}
       , CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

       listView.setAdapter(adapter);
   }

  //
    public int getSteps(SQLiteDatabase sqLiteDatabase,String date){
       // sqLiteDatabase.
        int k  = -1;
        Cursor cursor = sqLiteDatabase.rawQuery("select * from step where date ="+date,null);
        while(cursor.moveToNext()){
             k = cursor.getInt(2);
            System.out.println(k);
        }
        return k;
    }
    public  void  updateSteps(SQLiteDatabase sqLiteDatabase,String date,int updateSteps){
        ContentValues values = new ContentValues();
        values.put("steps",updateSteps);
        sqLiteDatabase.update("step",values,"date = ?", new String[]{date});



    }
    public void Close(SQLiteDatabase sqLiteDatabase){
        if(sqLiteDatabase != null &&sqLiteDatabase.isOpen())
            sqLiteDatabase.close();
    }
}
