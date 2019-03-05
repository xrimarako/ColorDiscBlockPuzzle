package com.xrimarako.apps.colordiscblockpuzzle;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;

public class SplashImageView extends  android.support.v7.widget.AppCompatImageView {

    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    RectF logoOval;
    // RectF logoOval2;
    //RectF logoOval3;
    Rect bounds = new Rect();

    static int counter=0;

    int textColor = MyImageView.COLOR_BLUE_THREE;
    int backColor = MyImageView.COLOR_BLACK;

    public SplashImageView(Context context) {
        super(context);
        paint.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/caviar_dreams_bold.ttf"));
        counter=0;
    }

    public SplashImageView(Context context, AttributeSet set) {
        super(context, set);
        paint.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/caviar_dreams_bold.ttf"));
        counter=0;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (logoOval == null){
            logoOval = getOval();
            //logoOval2 = getOval2();
            //logoOval3 = getOval3();
        }

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(backColor);
        canvas.drawRect(0,0,getWidth(),getHeight(),paint);

        paint.setTextSize(1.8f * 0.06f*getWidth());
        paint.setColor(textColor);
        paint.getTextBounds("COLOR DISC", 0, "COLOR DISC".length(), bounds);
        canvas.drawText("COLOR DISC", getWidth()/2f - bounds.width()/2f, 2f*0.06f*getWidth() + 5.5f*0.06f*getWidth(), paint);
        paint.setTextSize(0.7f * 0.06f*getWidth());
        paint.setColor(MyImageView.COLOR_GRAY_TWO);
        paint.getTextBounds("BLOCK PUZZLE", 0, "BLOCK PUZZLE".length(), bounds);
        canvas.drawText("BLOCK PUZZLE", getWidth()/2f - bounds.width()/2f, 2f*0.06f*getWidth() + 6.5f*0.06f*getWidth(), paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(0.06f*getWidth());

        if (counter > 0){
            paint.setColor( MyImageView.COLOR_BLUE_THREE);
            canvas.drawArc(logoOval, 230, 80, false, paint);
        }


        if (counter > 1){
            paint.setColor( MyImageView.COLOR_ORANGE);
            canvas.drawArc(logoOval, 320, 80, false, paint);
        }


        if (counter > 2){
            paint.setColor( MyImageView.COLOR_GREEN_TWO);
            canvas.drawArc(logoOval, 50, 80, false, paint);
        }


        if (counter > 3){
            paint.setColor( MyImageView.COLOR_PINK);
            canvas.drawArc(logoOval, 140, 80, false, paint);
        }

        /*if (counter > 0){
            paint.setColor( MyImageView.COLOR_YELLOW_TWO);
            canvas.drawArc(logoOval2, 230, 80, false, paint);
        }

        if (counter > 1){
            paint.setColor( MyImageView.COLOR_RED);
            canvas.drawArc(logoOval2, 320, 80, false, paint);
        }

        if (counter > 2){
            paint.setColor( MyImageView.COLOR_BLUE);
            canvas.drawArc(logoOval2, 50, 80, false, paint);
        }

        if (counter > 3){
            paint.setColor( MyImageView.COLOR_GREEN);
            canvas.drawArc(logoOval2, 140, 80, false, paint);
        }

        if (counter > 8){
            paint.setColor( MyImageView.COLOR_PINK_TWO);
            canvas.drawArc(logoOval3, 230, 80, false, paint);
        }

        if (counter > 9){
            paint.setColor( MyImageView.COLOR_BLUE_TWO);
            canvas.drawArc(logoOval3, 320, 80, false, paint);
        }

        if (counter > 10){
            paint.setColor( MyImageView.COLOR_MAGENTA);
            canvas.drawArc(logoOval3, 50, 80, false, paint);
        }

        if (counter > 11){
            paint.setColor( MyImageView.COLOR_ORANGE);
            canvas.drawArc(logoOval3, 140, 80, false, paint);
        }*/


        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(this.getWidth()/10f);
        paint.setColor(textColor);
        paint.getTextBounds("xrimarako", 0, "xrimarako".length(), bounds);
        canvas.drawText("xrimarako", getWidth()/2f - bounds.width()/2f, 12f*getHeight()/13f, paint);
    }

    private RectF getOval(){

        return new RectF(getWidth()/2f - getWidth()/6f, getHeight()/2f - getWidth()/6f,
                getWidth()/2f + getWidth()/6f, getHeight()/2f + getWidth()/6f);
    }

    /*private RectF getOval2(){

        return new RectF(getWidth()/2f - getWidth()/4f, getHeight()/2f - getWidth()/4f,
                getWidth()/2f + getWidth()/4f, getHeight()/2f + getWidth()/4f);
    }

    private RectF getOval3(){

        return new RectF(getWidth()/2f - getWidth()/12f, getHeight()/2f - getWidth()/12f,
                getWidth()/2f + getWidth()/12f, getHeight()/2f + getWidth()/12f);
    }*/
}
