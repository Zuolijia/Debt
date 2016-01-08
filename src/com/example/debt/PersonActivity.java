package com.example.debt;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.swipe.SwipeSimpleCursorAdapter;
import com.example.swipe.SwipeSimpleCursorAdapter.Callback;
import com.fortysevendeg.swipelistview.BaseSwipeListViewListener;
import com.fortysevendeg.swipelistview.SwipeListView;

public class PersonActivity extends ActionBarActivity implements Callback{

	private SwipeListView list_event;
	private SwipeSimpleCursorAdapter ItemAdapter;
	private Cursor cursor;
	private SQLiteDatabase db;
	private String table;
	private TextView person_name;
	public static int deviceWidth ;

	private void init_table() {
		db = SQLiteDatabase.openOrCreateDatabase(this.getFilesDir().toString()
				+ "/debt.db", null);
		cursor = db.query(table, null, null, null, null, null, null);
		
		deviceWidth = getResources().getDisplayMetrics().widthPixels;
		list_event.setSwipeMode(SwipeListView.SWIPE_MODE_LEFT);
		list_event.setSwipeActionLeft(SwipeListView.SWIPE_ACTION_REVEAL);
//		list_event.setSwipeActionRight(settings.getSwipeActionRight());
		list_event.setOffsetLeft(deviceWidth * 4 / 7);
//		list_event.setOffsetRight(convertDpToPixel(settings.getSwipeOffsetRight()));
		list_event.setAnimationTime(0);
		list_event.setSwipeOpenOnLongPress(false);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.person_activity);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		list_event = (SwipeListView) findViewById(R.id.list_event);
		person_name = (TextView) findViewById(R.id.person_name);
		

		Bundle bundle = this.getIntent().getExtras();
		table = bundle.getString("table");

		person_name.setText(table);
		person_name.setClickable(true);
		person_name.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				AlertDialog.Builder builder = new Builder(PersonActivity.this);
				LayoutInflater inf = getLayoutInflater();
				View layout = inf.inflate(R.layout.dialog_person, null);
				builder.setTitle("编辑关系人信息");
				builder.setView(layout);
				final EditText name=(EditText) layout.findViewById(R.id.edit_name);
				final EditText phone=(EditText) layout.findViewById(R.id.edit_phone);
				name.setText(table);
				Cursor c = db
						.query("person", new String[] {
								"name", "phone_number" },
								"name=?",
								new String[] { table },
								null, null, null);
				c.moveToFirst();
				final String old_phone=c.getString(c.getColumnIndex("phone_number"));
				phone.setText(old_phone);
				builder.setPositiveButton("确定", new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						if(name.getText().toString().equals(table)&&phone.getText().toString().equals(old_phone)){
							Toast.makeText(PersonActivity.this, "信息未更改", Toast.LENGTH_LONG).show();
						}
						else if(phone.getText().toString().equals(old_phone)){
							db.execSQL("alter table "+table+" rename to "+name.getText().toString());
							//修改总表信息
							ContentValues values = new ContentValues();
							String old_table=table;
							table=name.getText().toString();//转table值
							values.put("name", table);
							db.update("person", values, "name=?",
									new String[] { old_table });
							person_name.setText(table);
						}
						else if(name.getText().toString().equals(table)){
							ContentValues values = new ContentValues();
							table=name.getText().toString();
							values.put("phone_number", phone.getText().toString());
							db.update("person", values, "name=?",
									new String[] { table });
						}
						else{
							db.execSQL("alter table "+table+" rename to "+name.getText().toString());
							//修改总表信息
							ContentValues values = new ContentValues();
							values.put("phone_number", phone.getText().toString());
							db.update("person", values, "name=?",
									new String[] { table });
							ContentValues values2 = new ContentValues();
							String old_table=table;
							table=name.getText().toString();//转table值
							values2.put("name", table);
							db.update("person", values2, "name=?",
									new String[] { old_table });
							person_name.setText(table);
						}
					}				
				});
				builder.setNegativeButton("取消",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,int which) {
								dialog.dismiss();
							}

						});
				builder.create().show();
			}			
		});

		init_table();

		ItemAdapter = new SwipeSimpleCursorAdapter(getApplicationContext(),
				R.layout.list_item, cursor, new String[] { "event_detail",
						"event_time", "event_money" }, new int[] { R.id.event,
						R.id.time, R.id.money }, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER,
						table,db);
		list_event.setAdapter(ItemAdapter);
		ItemAdapter.setCallback(this);
		
		list_event.setSwipeListViewListener(new MySwipeListViewListener());
	}
	
	class MySwipeListViewListener extends BaseSwipeListViewListener{

		@Override
		public void onClickBackView(int position) {
			Toast.makeText(PersonActivity.this, "BackView", Toast.LENGTH_SHORT).show();
			list_event.closeAnimate(position);
			list_event.dismiss(position);
			super.onClickBackView(position);
		}

		@Override
		public void onDismiss(int[] reverseSortedPositions) {
			Toast.makeText(PersonActivity.this, "Dimiss", Toast.LENGTH_SHORT).show();
			super.onDismiss(reverseSortedPositions);
		}
		
	}

	@Override
	protected void onResume() {
		cursor = db.query(table, null, null, null, null, null, null);
		ItemAdapter.changeCursor(cursor);
		super.onResume();
	}

	

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.person_activity_add, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch (id) {
		case android.R.id.home:
			finish();
			break;
		case R.id.action_add:
			Intent in = new Intent(PersonActivity.this,
					AddEventActivity.class);
			Bundle bundle = new Bundle();
			bundle.putString("table", table);
			in.putExtras(bundle);
			startActivity(in);
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void click(View v) {
		String _id=v.getTag().toString();
		
		AlertDialog.Builder builder = new Builder(PersonActivity.this);
		LayoutInflater inf = getLayoutInflater();
		View layout = inf.inflate(R.layout.dialog_event, null);
		builder.setTitle("修改事项信息");
		builder.setView(layout);
		
		final EditText e_detail=(EditText) layout.findViewById(R.id.edit_detail);
		final EditText e_time=(EditText) layout.findViewById(R.id.edit_time);
		final EditText e_money=(EditText) layout.findViewById(R.id.edit_money);
		final RadioButton radio_out=(RadioButton) layout.findViewById(R.id.radio_out);
		final RadioButton radio_in=(RadioButton) layout.findViewById(R.id.radio_in);
		
		final String whereClause = "_id=?";
		final String[] whereArgs = { _id };
		Cursor cc=db.query(table, null, whereClause, whereArgs,null, null, null);
		cc.moveToFirst();
		
		final String detail=cc.getString(1);
		final String time=cc.getString(2);
		final float money=cc.getFloat(3);
		final String money_s=String.valueOf(Math.abs(money));
		
		e_detail.setText(detail);
		e_time.setText(time);
		e_money.setText(money_s);
		
		if(money<0){
			radio_in.setChecked(true);
		}
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int arg1) {
						ContentValues value = new ContentValues();
						value.put("event_detail",e_detail.getText().toString());
						value.put("event_time", e_time.getText().toString());
						float mm=Float.parseFloat(e_money.getText().toString());
						if(radio_in.isChecked())
							mm=0-mm;
						value.put("event_money",mm);
						db.update(table, value, whereClause, whereArgs);						
						
						// 修改总表信息
						Cursor c = db.query("person", new String[] {
										"name", "money" },
										"name=?",
										new String[] { table },
										null, null, null);
						c.moveToFirst();
						float allmoney = c.getFloat(1);
						allmoney=allmoney-money+mm;
						ContentValues values = new ContentValues();
						values.put("money", allmoney);
						db.update("person", values, "name=?",
								new String[] { table });
				cursor = db.query(table, null, null, null, null, null, null);
				ItemAdapter.changeCursor(cursor);
				dialog.dismiss();
			}
			
		});
		
		builder.setNegativeButton("取消", new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int arg1) {
				dialog.dismiss();
			}
			
		});
		builder.create().show();
	}
	
	
}
