package com.example.swipe;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import com.example.debt.R;

public class SwipeSimpleCursorAdapter extends SimpleCursorAdapter implements OnClickListener {
	
	private LayoutInflater mInflater ;
	private String TABLE;
	private Cursor m_cursor;
	private SQLiteDatabase db;
	private Callback mCallback;
	
	//自定义接口，用于回调按钮点击事件到PersonActivity
	public interface Callback{
		public void click(View v);
	}

	public SwipeSimpleCursorAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, int flags,String table,SQLiteDatabase db) {
		super(context, layout, c, from, to, flags);
		mInflater=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		TABLE=table;
		m_cursor=c;
		this.db=db;
	}
	
	public void setCallback(Callback callback){
		mCallback=callback;
	}

	@Override
	public void bindView(final View view, final Context context, final Cursor cursor) {	
		
		Button mBackEdit=(Button) view.findViewById(R.id.edit);
		Button mBackDelete=(Button) view.findViewById(R.id.delete);
		view.setTag(cursor.getInt(0));
		mBackDelete.setTag(cursor.getInt(0));
		mBackEdit.setTag(cursor.getInt(0));
		
		super.bindView(view, context, cursor);
		
		mBackEdit.setOnClickListener(this);
		
		mBackDelete.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				String _id=arg0.getTag().toString();
				System.out.println("Delete Id:"+_id);
				
				String whereClause = "_id=?";
				String[] whereArgs = { _id };
				Cursor cc=db.query(TABLE, null, whereClause, whereArgs,null, null, null);
				cc.moveToFirst();
				float money = cc.getFloat(3);
				System.out.println("Delete money:"+money);
				
				db.delete(TABLE, whereClause, whereArgs);
				m_cursor = db.query(TABLE, null, null, null,null, null, null);
				changeCursor(m_cursor);
				// 修改总表信息
				Cursor c = db.query("person", new String[] { "name", "money" },
						"name=?", new String[] { TABLE }, null, null, null);
				c.moveToFirst();
				float allmoney = c.getFloat(1);
				allmoney -= money;
				ContentValues values = new ContentValues();
				values.put("money", allmoney);
				db.update("person", values, "name=?", new String[] { TABLE });
			}
			
		});
		
	}

	//响应按钮点击事件，调用子定义接口，并传入View
	@Override
	public void onClick(View v) {
		mCallback.click(v);
	}
	
}
