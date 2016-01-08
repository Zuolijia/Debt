package com.example.slide;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Scroller;

public class SlideListView extends ListView {

	private int slidePosition;           //��ǰ������ListViewλ��
	private int downX;                   //��ָ���µ�x����
	private int downY;                   //��ָ���µ�y����
	private int screenWidth;             //��Ļ���
	private View itemView;               //ListView��item
	private Scroller scroller;           //������
	private static final int SNAP_VELOCITY=600;
	private VelocityTracker velocityTracker;  //�ٶ�׷�ٶ���
	private boolean isSlide=false;        //�Ƿ���Ӧ������Ĭ�ϲ���Ӧ
	private int mTouchSlop;               //��Ϊ���û���������С����
	private RemoveListener mRemoveListener;//�Ƴ�item��Ļص��ӿ�
	private RemoveDirection removeDirection;//����֪ʶitem������Ļ�ķ��������������
	
	public enum RemoveDirection{
		RIGHT,LEFT;
	}
	
	
	public SlideListView(Context context) {
		this(context,null);
	}

	public SlideListView(Context context, AttributeSet attrs) {
		this(context,attrs,0);
	}

	public SlideListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		DisplayMetrics dm = new DisplayMetrics();
		((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(dm);
		screenWidth=dm.widthPixels;
		scroller = new Scroller(context);  
		mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();  
	}
	
	//���û���ɾ���ص��ӿ�
	public void setRemoveListener (RemoveListener removeListener){
		this.mRemoveListener=removeListener;
	}

	//�ַ�ʱ�䣬��Ҫ�������жϵ�������ĸ�item,�Լ�ͨ��postDelayed��������Ӧ���һ���
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		switch(ev.getAction()){
		case MotionEvent.ACTION_DOWN:{
			addVelocityTracker(ev);
			
			//����scroller������û����������ֱ�ӷ���
			if(!scroller.isFinished()){
				return super.dispatchTouchEvent(ev);
			}
			downX=(int) ev.getX();
			downY=(int) ev.getY();
			
			slidePosition=pointToPosition(downX,downY);
			
			//��Ч��Position,�����κδ���
			if(slidePosition==AdapterView.INVALID_POSITION){
				return super.dispatchTouchEvent(ev);
			}
			
			//��ȡ���ǵ����item view
			itemView = getChildAt(slidePosition-getFirstVisiblePosition());
			break;
		}
		case MotionEvent.ACTION_MOVE:{
			if(Math.abs(getScrollVelocity())>SNAP_VELOCITY 
					|| (Math.abs(ev.getX() - downX) > mTouchSlop 
							&& Math.abs(ev.getY()-downY) >mTouchSlop ))
					isSlide=true;
			break;
		}
		case MotionEvent.ACTION_UP:
			recycleVelocityTracker();
			break;
		}
		return super.dispatchTouchEvent(ev);
	}
	
	//���һ�����getScrollX()���ص������Ե�ľ��룬������View���ԵΪԭ�㵽��ʼ�����ľ���
	//�������ұ�Ե����Ϊ��ֵ
	private void scrollRight(){
		removeDirection = RemoveDirection.RIGHT;
		final int delta=(screenWidth+itemView.getScrollX());
		scroller.startScroll(itemView.getScrollX(), 0, -delta, 0, Math.abs(delta));
		postInvalidate();//ˢ��itemView
	}

	//���󻬶�
	private void scrollLeft(){
		removeDirection=RemoveDirection.LEFT;
		final int delta=(screenWidth-itemView.getScrollX());
		//����startScroll����������һЩ�����Ĳ���
		scroller.startScroll(itemView.getScrollX(), 0, delta, 0, Math.abs(delta));
		postInvalidate();//ˢ��itemView
	}
	
	//������ָ����itemView�ľ������ж��ǹ�������ʼλ�û�������������ҹ���
	private void scrollByDistanceX(){
		//�����������ľ��������Ļ�Ķ���֮һ����ɾ��
		if(itemView.getScrollX()>=screenWidth/2){
			scrollLeft();
		}
		else if(itemView.getScrollX()<=-screenWidth/2){
			scrollRight();
		}else{
			//����ԭʼλ��
			itemView.scrollTo(0,0);
		}
	}
	
	//�����϶�ListView item���߼�
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if(isSlide && slidePosition !=AdapterView.INVALID_POSITION){
			requestDisallowInterceptTouchEvent(true);
			addVelocityTracker(ev);
			final int action=ev.getAction();
			int x=(int) ev.getX();
			switch(action){
			case MotionEvent.ACTION_DOWN:
				break;
			case MotionEvent.ACTION_MOVE:
				MotionEvent cancelEvent=MotionEvent.obtain(ev);
				cancelEvent.setAction(MotionEvent.ACTION_CANCEL|(ev.getActionIndex()<< MotionEvent.ACTION_POINTER_INDEX_SHIFT));
				onTouchEvent(cancelEvent);
				int deltax=downX-x;
				downX=x;
				//��ָ�϶�itemView������deltaX������0���������С��0���ҹ���
				itemView.scrollBy(deltax, 0);
				return true;                //�϶���ʱ��ListView������
			case MotionEvent.ACTION_UP:
				int velocityX=getScrollVelocity();
				if(velocityX>SNAP_VELOCITY){
					scrollRight();
				}else if(velocityX<-SNAP_VELOCITY){
					scrollLeft();
				}else{
					scrollByDistanceX();
				}
				recycleVelocityTracker();
				//��ָ�뿪��ʱ��Ͳ���Ӧ���ҹ���
				isSlide=false;
				break;				
			}
		}
		return super.onTouchEvent(ev);
	}

	@Override
	public void computeScroll() {
		//����startScroll��ʱ��scroller.computeScrollOffset����true
		if(scroller.computeScrollOffset()){
			//��ListView item���ݵ�ǰ�Ĺ���ƫ�������й���
			itemView.scrollTo(scroller.getCurrX(), scroller.getCurrY());
			postInvalidate();
			//��������������ʱ����ûص��ӿ�
			if(scroller.isFinished()){
				if(mRemoveListener==null){
					throw new NullPointerException("RemoveListener is null, we should called setRemoveListener()");
				}
				itemView.scrollTo(0, 0);
				mRemoveListener.removeItem(removeDirection, slidePosition);
			}
		}
		super.computeScroll();
	}
	
	//����û����ٶȸ�����
	private void addVelocityTracker(MotionEvent ev){
		if(velocityTracker==null){
			velocityTracker=VelocityTracker.obtain();
		}
		velocityTracker.addMovement(ev);
	}

	//�Ƴ��û��ٶȸ�����
	private void recycleVelocityTracker(){
		if(velocityTracker!=null){
			velocityTracker.recycle();
			velocityTracker=null;
		}
	}
	
	//��ȡx����Ļ����ٶȣ�����0���һ�������֮����
	private int getScrollVelocity(){
		velocityTracker.computeCurrentVelocity(1000);
		int velocity=(int) velocityTracker.getXVelocity();
		return velocity;
	}
	
	//��listView item������Ļ���ص�����ӿ�
	public interface RemoveListener{
		public void removeItem(RemoveDirection direction,int position);
	}
}
