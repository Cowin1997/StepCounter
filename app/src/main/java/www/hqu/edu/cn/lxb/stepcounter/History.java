package www.hqu.edu.cn.lxb.stepcounter;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.sql.SQLData;

import www.hqu.edu.cn.lxb.stepcounter.DataBase.DateBase;

import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;

public class History extends Activity {
    ListView listView;
    DateBase DataUtil;
    SQLiteDatabase myDB ;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        DataUtil= new DateBase(this.getFilesDir()+"/my.db3");
        myDB = DataUtil.getDB();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_history);
        listView = (ListView)findViewById(R.id.ls);
      DataUtil.InitDB(myDB);
        Cursor cursor = myDB.rawQuery("select * from step order by _id desc limit 0,20 ",null);
        inflateList(cursor);
    }

    @Override
    protected void onResume() {
        listView = (ListView)findViewById(R.id.ls);
        Cursor cursor = myDB.rawQuery("select * from step order by _id desc limit 0,20 ",null);
        inflateList(cursor);
        super.onResume();
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

   public void OnBack(View view){
       Intent intent = new Intent(this,MainActivity.class);
       intent.setFlags(FLAG_ACTIVITY_SINGLE_TOP);
       startActivity(intent);
   }


}
