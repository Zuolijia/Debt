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

	private int slidePosition;           //当前滑动的ListView位置
	private int downX;                   //手指按下的x坐标
	private int downY;                   //手指按下的y坐标
	private int screenWidth;             //屏幕宽度
	private View itemView;               //ListView的item
	private Scroller scroller;           //滑动类
	private static final int SNAP_VELOCITY=600;
	private VelocityTracker velocityTracker;  //速度追踪对象
	private boolean isSlide=false;        //是否响应滑动，默认不响应
	private int mTouchSlop;               //认为是用户滑动的最小距离
	private RemoveListener mRemoveListener;//移除item后的回调接口
	private RemoveDirection removeDirection;//用来知识item画出屏幕的方向，像左或者向右
	
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
	
	//设置滑动删除回调接口
	public void setRemoveListener (RemoveListener removeListener){
		this.mRemoveListener=removeListener;
	}

	//分发时间，主要做的是判断点击的是哪个item,以及通过postDelayed来设置响应左右滑动
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		switch(ev.getAction()){
		case MotionEvent.ACTION_DOWN:{
			addVelocityTracker(ev);
			
			//假如scroller滚动还没结束，我们直接返回
			if(!scroller.isFinished()){
				return super.dispatchTouchEvent(ev);
			}
			downX=(int) ev.getX();
			downY=(int) ev.getY();
			
			slidePosition=pointToPosition(downX,downY);
			
			//无效的Position,不做任何处理
			if(slidePosition==AdapterView.INVALID_POSITION){
				return super.dispatchTouchEvent(ev);
			}
			
			//获取我们点击的item view
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
	
	//向右滑动，getScrollX()返回的是左边缘的距离，就是以View左边缘为原点到开始滑动的距离
	//所以向右边缘滑动为负值
	private void scrollRight(){
		removeDirection = RemoveDirection.RIGHT;
		final int delta=(screenWidth+itemView.getScrollX());
		scroller.startScroll(itemView.getScrollX(), 0, -delta, 0, Math.abs(delta));
		postInvalidate();//刷新itemView
	}

	//向左滑动
	private void scrollLeft(){
		removeDirection=RemoveDirection.LEFT;
		final int delta=(screenWidth-itemView.getScrollX());
		//调用startScroll方法来设置一些滚动的参数
		scroller.startScroll(itemView.getScrollX(), 0, delta, 0, Math.abs(delta));
		postInvalidate();//刷新itemView
	}
	
	//根据手指滚动itemView的距离来判断是滚到到开始位置还是向左或者向右滚动
	private void scrollByDistanceX(){
		//如果向左滚动的距离大于屏幕的二分之一，就删除
		if(itemView.getScrollX()>=screenWidth/2){
			scrollLeft();
		}
		else if(itemView.getScrollX()<=-screenWidth/2){
			scrollRight();
		}else{
			//滚回原始位置
			itemView.scrollTo(0,0);
		}
	}
	
	//处理拖动ListView item的逻辑
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
				//手指拖动itemView滚动，deltaX不大于0向左滚动，小于0向右滚动
				itemView.scrollBy(deltax, 0);
				return true;                //拖动的时候ListView不滚动
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
				//手指离开的时候就不响应左右滚动
				isSlide=false;
				break;				
			}
		}
		return super.onTouchEvent(ev);
	}

	@Override
	public void computeScroll() {
		//调用startScroll的时候scroller.computeScrollOffset返回true
		if(scroller.computeScrollOffset()){
			//让ListView item根据当前的滚动偏移量进行滚动
			itemView.scrollTo(scroller.getCurrX(), scroller.getCurrY());
			postInvalidate();
			//滚动动画结束的时候调用回调接口
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
	
	//添加用户的速度跟踪器
	private void addVelocityTracker(MotionEvent ev){
		if(velocityTracker==null){
			velocityTracker=VelocityTracker.obtain();
		}
		velocityTracker.addMovement(ev);
	}

	//移除用户速度跟踪器
	private void recycleVelocityTracker(){
		if(velocityTracker!=null){
			velocityTracker.recycle();
			velocityTracker=null;
		}
	}
	
	//获取x方向的滑动速度，大于0向右滑动，反之向左
	private int getScrollVelocity(){
		velocityTracker.computeCurrentVelocity(1000);
		int velocity=(int) velocityTracker.getXVelocity();
		return velocity;
	}
	
	//当listView item滑出屏幕，回调这个接口
	public interface RemoveListener{
		public void removeItem(RemoveDirection direction,int position);
	}
}
