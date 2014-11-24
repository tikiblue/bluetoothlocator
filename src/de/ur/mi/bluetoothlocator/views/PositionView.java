package de.ur.mi.bluetoothlocator.views;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class PositionView extends View {

	private double x, y;
	private float radius = 5;
	private ArrayList<PercentageClickListener> listeners = new ArrayList<PercentageClickListener>();
	
	public PositionView(Context context) {
		super(context);
		x = -1000;
		y = -1000;
	}
	
	public PositionView(Context context, AttributeSet set) {
		super(context, set);
	}
	
	public void setX(double x){
		this.x = x;
        invalidate();
	}
	
	public void setY(double y){
		this.y = y;
        invalidate();
	}

	@Override
	public boolean performClick() {
		return super.performClick();
	}
	
	@Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_UP){
        		performClick();
            	onClick(event);
        }
        
        if(event.getAction() == MotionEvent.ACTION_DOWN)return true;
        else return super.onTouchEvent(event);
	}
	
	private void onClick(MotionEvent event){
		float xPos = event.getX();
		float yPos = event.getY();
		if(xPos<0 || xPos>getWidth())return;
		if(yPos<0 || yPos>getHeight())return;
		float xPercentage = 100*xPos/getWidth();
		float yPercentage = 100*yPos/getHeight();
		performPercentageClick(xPercentage, yPercentage);
	}
	
	public void addPercentageClickListener(PercentageClickListener l){
		listeners.add(l);
	}
	
	private void performPercentageClick(float xPercentage, float yPercentage) {
		for(PercentageClickListener l : listeners){
			l.onPercentageClicked(xPercentage, yPercentage);
		}
	}

	@Override
    protected void onDraw(Canvas c) {
        super.onDraw(c);
        Paint p = new Paint();
        p.setColor(Color.GRAY);
        p.setAntiAlias(true);
        
        c.drawRect(new RectF(0,0,getWidth(),getHeight()), p);
        float xPos = (float)x*getWidth()/100;
        float yPos = (float)y*getHeight()/100;
        p.setColor(Color.RED);
        c.drawCircle(xPos, yPos, radius, p);
	}
	
}
