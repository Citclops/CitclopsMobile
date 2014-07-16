package com.citclops.widgets;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.Shape;

public class CustomBorderDrawable extends ShapeDrawable {
	Paint fillpaint, strokepaint;
	private static final int WIDTH = 15; 
	public CustomBorderDrawable(Shape s) {
	    super(s);
	    fillpaint = this.getPaint();
	    strokepaint = new Paint(fillpaint);
	    strokepaint.setStyle(Paint.Style.STROKE);
	    strokepaint.setStrokeWidth(WIDTH);
	    strokepaint.setARGB(255, 255, 0, 0);
	     }
	@Override
	protected void onDraw(Shape shape, Canvas canvas, Paint fillpaint) {
	    shape.draw(canvas, fillpaint);
	    shape.draw(canvas, strokepaint);
	}

	public void setFillColour(int c){
		fillpaint.setColor(c);
	}
	public void setStrokeColour(int c){
	    strokepaint.setColor(c);
	}
	public void setStrokeWidth(int w){
	    strokepaint.setStrokeWidth(w);
	}
}
