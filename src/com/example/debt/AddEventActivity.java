package com.example.debt;

import java.util.Calendar;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

public class AddEventActivity extends ActionBarActivity {

	private SQLiteDatabase db;
	private EditText event_detail;
	private EditText event_time;
	private EditText event_money;
	private String table;
	private RadioButton radio_out;
	private RadioButton radio_in;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_event_activity);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		event_detail=(EditText) findViewById(R.id.event_detail);
		event_time=(EditText) findViewById(R.id.event_time);
		event_money=(EditText) findViewById(R.id.event_money);
		radio_out=(RadioButton) findViewById(R.id.radio_out);
		radio_in=(RadioButton) findViewById(R.id.radio_in);
		
		Bundle bundle=this.getIntent().getExtras();
		table=bundle.getString("table");
		
		event_time.setOnTouchListener(new OnTouchListener(){

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction()==MotionEvent.ACTION_DOWN){
					AlertDialog.Builder builder = new AlertDialog.Builder(AddEventActivity.this);
					LayoutInflater inf = getLayoutInflater();
					View view =inf.inflate(R.layout.dialog_date_picker, null);  
					final DatePicker date=(DatePicker) view.findViewById(R.id.datePicker);
					builder.setTitle("选择日期");
					builder.setView(view);
					
					Calendar cal = Calendar.getInstance();  
		            cal.setTimeInMillis(System.currentTimeMillis());  
		            date.init(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH),null);
					
		            builder.setPositiveButton("确定", new DialogInterface.OnClickListener(){

						@Override
						public void onClick(DialogInterface dialog, int which) {
							StringBuffer sb=new StringBuffer();
							sb.append(String.format("%d-%02d-%02d", date.getYear(),
									date.getMonth()+1,date.getDayOfMonth()));
							event_time.setText(sb);
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
				}
				return false;
			}
			
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.add_person_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id=item.getItemId();
		switch(id){
		case R.id.action_finish:
			db=SQLiteDatabase.openOrCreateDatabase(this.getFilesDir().toString()+"/debt.db", null);
			float money=0;
			if(!event_money.getText().toString().equals("")){
				//插入新债务
				money=Float.parseFloat(event_money.getText().toString());
				if(radio_in.isChecked())
					money=0-money;
				ContentValues values = new ContentValues();
				values.put("event_detail", event_detail.getText().toString());
				values.put("event_time", event_time.getText().toString());
				values.put("event_money", money);
				db.insert(table, null, values);
				
				//修改总表
				Cursor c=db.query("person", new String[]{"name","money"}, "name=?", new String[]{table}, null, null, null);
				c.moveToFirst();
				float allmoney=c.getFloat(1);
				allmoney+=money;
				System.out.println("现在总金额："+allmoney);
				ContentValues values_all=new ContentValues();
				values_all.put("money", allmoney);
				db.update("person", values_all, "name=?", new String[]{table});
			}
			else{
				Toast.makeText(AddEventActivity.this, "信息不完整", Toast.LENGTH_SHORT).show();
			}
			finish();
			break;
		case android.R.id.home:
			finish();
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

}
