package com.example.debt;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Toast;

public class AddPersonActivity extends ActionBarActivity {

	private EditText edit_name;
	private EditText edit_phone;
	private CheckBox Isnew;
	private EditText event_detail;
	private EditText event_time;
	private EditText event_money;
	private SQLiteDatabase db;
	private RadioButton radio_out;//+
	private RadioButton radio_in;//-
	private ImageButton search_person;
	private ImageButton search_phone;
	
	protected void onActivityResult(int requestCode,int resultCode,Intent intent){
		super.onActivityResult(requestCode, resultCode, intent);
		if(resultCode == Activity.RESULT_OK){
			Uri uri=intent.getData();
			ContentResolver cr=getContentResolver();//得到ContentResolver对象
			Cursor cursor=cr.query(uri, null, null, null, null);//取得开始光标
			switch(requestCode){
			case 0:
				while(cursor.moveToNext()){
					String name=cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
					String ID=cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
					Cursor phone=cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, 
		                    null, 
		                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + ID, 
		                    null, 
		                    null);
					while(phone.moveToNext()){
						String PhoneNumber=phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
						edit_name.setText(name);
						edit_phone.setText(PhoneNumber);
					}
				}
				break;
			case 1:
				while(cursor.moveToNext()){
					String ID=cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
					Cursor phone=cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, 
		                    null, 
		                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + ID, 
		                    null, 
		                    null);
					while(phone.moveToNext()){
						String PhoneNumber=phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
						edit_phone.setText(PhoneNumber);
					}
				}
				break;
			default:
				break;
			}
		
		}
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_person_activity);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		edit_name=(EditText) findViewById(R.id.edit_name);
		edit_phone=(EditText) findViewById(R.id.edit_phone);
		event_detail=(EditText) findViewById(R.id.event_detail);
		event_time=(EditText) findViewById(R.id.event_time);
		event_money=(EditText) findViewById(R.id.event_money);
		Isnew=(CheckBox) findViewById(R.id.checkBox_new);
		radio_out=(RadioButton) findViewById(R.id.radio_out);
		radio_in=(RadioButton) findViewById(R.id.radio_in);
		search_person=(ImageButton) findViewById(R.id.imageBt_person);
		search_phone=(ImageButton) findViewById(R.id.imageBt_phone);
		
		db=SQLiteDatabase.openOrCreateDatabase(this.getFilesDir().toString()+"/debt.db", null);
		Isnew.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				LinearLayout layout=(LinearLayout) findViewById(R.id.new_event_layout);
				if(Isnew.isChecked())
					layout.setVisibility(View.VISIBLE);
				else
					layout.setVisibility(View.GONE);
			}
		});
		
		search_person.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				Intent intent=new Intent(Intent.ACTION_PICK,android.provider.ContactsContract.Contacts.CONTENT_URI);
				startActivityForResult(intent,0);
			}
			
		});
		
		search_phone.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				Intent intent=new Intent(Intent.ACTION_PICK,android.provider.ContactsContract.Contacts.CONTENT_URI);
				startActivityForResult(intent,1);
			}
			
		});
		
		event_time.setOnTouchListener(new OnTouchListener(){

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction()==MotionEvent.ACTION_DOWN){
					AlertDialog.Builder builder = new AlertDialog.Builder(AddPersonActivity.this);
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
			String TABLE_NAME=edit_name.getText().toString();
			if(TABLE_NAME.equals("")){
				Toast.makeText(AddPersonActivity.this, "请输入有效信息", Toast.LENGTH_SHORT).show();
			}
			else{
			String new_table="create table if not exists "+TABLE_NAME+"(_id integer primary key autoincrement,event_detail text,event_time text,event_money float)";
			db.execSQL(new_table);
			float money=0;
			if(!event_money.getText().toString().equals("")){
				money=Float.parseFloat(event_money.getText().toString());
				if(radio_in.isChecked())
					money=0-money;
			}
			ContentValues value_p = new ContentValues();
			value_p.put("name", edit_name.getText().toString());
			value_p.put("money", money);
			value_p.put("phone_number",edit_phone.getText().toString());
			db.insert("person", null, value_p);
			if(Isnew.isChecked()){
				//插入新债务
				ContentValues values = new ContentValues();
				values.put("event_detail", event_detail.getText().toString());
				values.put("event_time", event_time.getText().toString());
				values.put("event_money", money);
				db.insert(TABLE_NAME, null, values);
			}
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
