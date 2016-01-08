package com.example.debt;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.example.slide.SlideListView;
import com.example.slide.SlideListView.RemoveDirection;
import com.example.slide.SlideListView.RemoveListener;

public class MainActivity extends ActionBarActivity implements RemoveListener{
	private SlideListView list_person;
	private SimpleCursorAdapter listItemAdapter;
	private Cursor cursor;
	private Intent intent = new Intent();
	private SQLiteDatabase db;
	private Button add_person;
	
	private void init(){
		db=SQLiteDatabase.openOrCreateDatabase(this.getFilesDir().toString()+"/debt.db", null);
		String debt_table="create table if not exists person(_id integer primary key autoincrement,name text,money float,phone_number text)";
		db.execSQL(debt_table);
		cursor=db.query("person",null,null,null,null,null, null);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		list_person=(SlideListView) findViewById(R.id.list_person);
		add_person=(Button) findViewById(R.id.add_person);
		init();
		
		list_person.setRemoveListener(this);
		
		listItemAdapter = new SimpleCursorAdapter(getApplicationContext(),
				R.layout.list_person_item, cursor, new String[] {"name", "money"},
				new int[] {R.id.textView1,R.id.textView2}, 0);
		list_person.setAdapter(listItemAdapter);
		list_person.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				cursor.moveToPosition(position);
				String table=cursor.getString(cursor.getColumnIndex("name"));
				intent=intent.setClass(MainActivity.this, PersonActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("table", table);
				intent.putExtras(bundle);
				startActivity(intent);
			}
			
		});
		list_person.setOnItemLongClickListener(new OnItemLongClickListener(){
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					final int position, long id) {
				AlertDialog.Builder builder=new Builder(MainActivity.this);
				builder.setMessage("删除该债务人关系？");
				builder.setPositiveButton("确认", new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if(cursor!=null){
							cursor.moveToPosition(position);
							String person_name=cursor.getString(cursor.getColumnIndex("name"));
							String whereClause="name=?";
							String[] whereArgs={person_name};
							db.delete("person", whereClause, whereArgs);
							String del_table="drop table if exists "+person_name;
							db.execSQL(del_table);
							cursor=db.query("person",null,null,null,null,null, null);
							listItemAdapter.changeCursor(cursor);
						}
						dialog.dismiss();
					}	
				});
				builder.setNegativeButton("取消", new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}			
				});
				builder.create().show();
				return false;
			}
			
		});
		add_person.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				//跳转画面到新建债务人
				Intent in=new Intent(MainActivity.this, AddPersonActivity.class);
				startActivity(in);
			}		
		});
	}

	@Override
	protected void onResume() {
		cursor=db.query("person",null,null,null,null,null, null);
		listItemAdapter.changeCursor(cursor);
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_exit) {
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void removeItem(RemoveDirection direction, int position) {
		if(cursor!=null){
			cursor.moveToPosition(position);
			String person_name=cursor.getString(cursor.getColumnIndex("name"));
			String whereClause="name=?";
			String[] whereArgs={person_name};
			db.delete("person", whereClause, whereArgs);
			String del_table="drop table if exists "+person_name;
			db.execSQL(del_table);
			cursor=db.query("person",null,null,null,null,null, null);
			listItemAdapter.changeCursor(cursor);
		}
		switch (direction) {  
		case RIGHT:  
			Toast.makeText(this, "向右删除  "+ position, Toast.LENGTH_SHORT).show();  
			break;  
		case LEFT:  
			Toast.makeText(this, "向左删除  "+ position, Toast.LENGTH_SHORT).show();  
			break;  
		default:  
			break;  
			}  

	}
}
