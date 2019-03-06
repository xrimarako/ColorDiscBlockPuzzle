package com.xrimarako.apps.colordiscblockpuzzle;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MyImageView extends android.support.v7.widget.AppCompatImageView {
    //this needs a lot of work
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    RadialGradient radialGradient;

    private Bitmap homeBitmap;
    private Bitmap videoBitmap;
    private Bitmap leaderboardBitmap;
    private Bitmap achievementsBitmap;
    private Bitmap settingsBitmap;
    private Bitmap rateBitmap;
    private Bitmap shareBitmap;
    private Bitmap handBitmap;

    Path playPath;
    Path fastDownPath;

    //Texts and colors of messages
    Rect bounds = new Rect();
    String logoTxt = "COLOR DISC";
    String logoTxt2 = "BLOCK PUZZLE";
    String scoreTxt;
    String bestTxt;
    String linesTxt;
    String newGameTxt = "NEW GAME";
    String continueTxt = "CONTINUE";
    int continueColor = COLOR_GRAY_TWO;
    String linesBonusTxt = "";
    int linesBonusColor;
    String signTxt = "Sign in";
    int signColor = COLOR_BLACK;
    String soundTxt = "Sound ON";
    int soundColor = COLOR_BLUE;

    //Gameplay metrics
    float centerX;
    float centerY;
    float radiusCircleOut2;
    float radiusCircleOut1;
    float radiusCircleZero;
    float radiusCircleOne;
    float radiusCircleTwo;
    float radiusCircleThree;
    float radiusCircleFour;
    float radiusCircleFive;
    float radiusCircleInside;
    float strokeWidth;
    float startingScoreY;
    float angleDev;
    float mainThreadAngleDev = 0;//used only when main thread is working. Otherwise = 0;
    int totalspaces = 20;

    float radiusFastbutton;

    RectF[] ovals;
    RectF[] nextOvals;
    RectF[] runningOvals;
    RectF[] pieceStrokeOvals;

    //Running and next Piece
    Piece nextPiece;
    Piece runningPiece;
    float downSpeed = 0;
    float radiusCircleRunning;

    //Lists of Arcs for every circle
    List<Arc> listCircleOut2 = Collections.synchronizedList(new ArrayList<Arc>());
    List<Arc> listCircleOut1 = Collections.synchronizedList(new ArrayList<Arc>());
    List<Arc> listCircleZero = Collections.synchronizedList(new ArrayList<Arc>());
    List<Arc> listCircleOne = Collections.synchronizedList(new ArrayList<Arc>());
    List<Arc> listCircleTwo = Collections.synchronizedList(new ArrayList<Arc>());
    List<Arc> listCircleThree = Collections.synchronizedList(new ArrayList<Arc>());
    List<Arc> listCircleFour = Collections.synchronizedList(new ArrayList<Arc>());
    List<Arc> listCircleFive = Collections.synchronizedList(new ArrayList<Arc>());

    List<Arc>[] arcListArray = new List[8];

    //Line Achieved
    boolean[] linesAchieved = new boolean[8];
    int linesBlinkingCounter = 0;

    //Colors
    final static int COLOR_BLACK = Color.parseColor("#263238");
    final static int COLOR_WHITE = Color.parseColor("#ECEFF1");
    final static int COLOR_GRAY = Color.parseColor("#90A4AE");
    final static int COLOR_GRAY_TWO = Color.parseColor("#607D8B");//CFD8DC
    //Block pieces COLORS
    final static int COLOR_RED = Color.parseColor("#FF3D00");//
    final static int COLOR_PINK = Color.parseColor("#F50057");//
    final static int COLOR_PINK_TWO = Color.parseColor("#FF4081");//
    final static int COLOR_GREEN = Color.parseColor("#00C853");//
    final static int COLOR_GREEN_TWO = Color.parseColor("#00E676");//
    final static int COLOR_GREEN_THREE = Color.parseColor("#69F0AE");//
    final static int COLOR_BLUE = Color.parseColor("#0091EA");//
    final static int COLOR_BLUE_TWO = Color.parseColor("#00B0FF");//
    final static int COLOR_BLUE_THREE = Color.parseColor("#40C4FF");//
    final static int COLOR_YELLOW = Color.parseColor("#FFEA00");//
    final static int COLOR_YELLOW_TWO = Color.parseColor("#C6FF00");//
    final static int COLOR_ORANGE = Color.parseColor("#FF9100");//
    final static int COLOR_MAGENTA=Color.parseColor("#D500F9");//

    final static int[] COLORS = new int[]{COLOR_RED, COLOR_PINK, COLOR_PINK_TWO, COLOR_GREEN, COLOR_ORANGE,
            COLOR_GREEN_TWO, COLOR_BLUE, COLOR_BLUE_TWO, COLOR_YELLOW, COLOR_YELLOW_TWO, COLOR_MAGENTA};


    //For animation purposes
    private int alphaValue = 255;
    private int alphaDev = -1;


    public MyImageView(Context context) {
        super(context);

        paint.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/caviar_dreams_bold.ttf"));
    }

    public MyImageView(Context context, AttributeSet set) {
        super(context, set);

        paint.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/caviar_dreams_bold.ttf"));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //Metrics and first values
        if (centerX == 0){
            centerX = getWidth()/2f;
            centerY = 1.8f*getHeight()/3f;
            angleDev = 360f/totalspaces;
            strokeWidth = 0.06f*getWidth();
            radiusCircleInside = 2f*strokeWidth;
            radiusCircleFive = radiusCircleInside + 0.5f * strokeWidth;
            radiusCircleFour = radiusCircleFive + 1f * strokeWidth;
            radiusCircleThree = radiusCircleFour + 1f * strokeWidth;
            radiusCircleTwo = radiusCircleThree + 1f * strokeWidth;
            radiusCircleOne = radiusCircleTwo + 1f * strokeWidth;
            radiusCircleZero = radiusCircleOne + 1f * strokeWidth;
            radiusCircleOut1 = radiusCircleZero + 1f * strokeWidth;
            radiusCircleOut2 = radiusCircleOut1 + 1f * strokeWidth;
            radiusCircleRunning = 2f * radiusCircleZero;

            radiusFastbutton = 0.5f * radiusCircleInside;

            startingScoreY = 2f*strokeWidth;

            downSpeed = 0.09375f*strokeWidth;
            ovals = getOvals(centerX, centerY);
            nextOvals = getNextOvals(getWidth()/2f, 3.7f*strokeWidth, 2f*strokeWidth);

            radialGradient = new RadialGradient(centerX, centerY, 3f*radiusCircleZero, COLOR_GRAY, COLOR_GRAY_TWO, Shader.TileMode.MIRROR);

            arcListArray = new List[]{listCircleOut2, listCircleOut1, listCircleZero, listCircleOne, listCircleTwo,
                    listCircleThree, listCircleFour, listCircleFive};
        }

        //Icon paths
        if (playPath==null){
            playPath = new Path();
            playPath.moveTo(centerX-0.3f*radiusCircleInside, centerY-0.5f*radiusCircleInside);
            playPath.lineTo(centerX+0.5f*radiusCircleInside, centerY);
            playPath.lineTo(centerX-0.3f*radiusCircleInside, centerY+0.5f*radiusCircleInside);
            playPath.lineTo(centerX-0.3f*radiusCircleInside, centerY-0.5f*radiusCircleInside);
            playPath.close();

            fastDownPath = new Path();
            fastDownPath.moveTo(centerX-1f*radiusCircleInside, centerY+radiusCircleZero + 2f*strokeWidth);
            fastDownPath.lineTo(centerX+1f*radiusCircleInside, centerY+radiusCircleZero + 2f*strokeWidth);
            fastDownPath.lineTo(centerX, centerY+radiusCircleZero + 3f*strokeWidth);
            fastDownPath.lineTo(centerX-1f*radiusCircleInside, centerY+radiusCircleZero + 2f*strokeWidth);
            fastDownPath.close();
        }

        //Icon bitmaps
        if (homeBitmap==null){
            homeBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.home);
            homeBitmap = Bitmap.createScaledBitmap(homeBitmap, (int)(2f*strokeWidth), (int)(2f*strokeWidth), false);

            videoBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.video_ad);
            videoBitmap = Bitmap.createScaledBitmap(videoBitmap, (int)(2f*strokeWidth), (int)(2f*strokeWidth), false);

            achievementsBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.achievements);
            achievementsBitmap = Bitmap.createScaledBitmap(achievementsBitmap, (int)(1.4f*strokeWidth), (int)(1.4f*strokeWidth), false);

            leaderboardBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.leaderboard);
            leaderboardBitmap = Bitmap.createScaledBitmap(leaderboardBitmap, (int)(1.4f*strokeWidth), (int)(1.4f*strokeWidth), false);

            settingsBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.settings);
            settingsBitmap = Bitmap.createScaledBitmap(settingsBitmap, (int)(1.6f*strokeWidth), (int)(1.6f*strokeWidth), false);

            rateBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.rate);
            rateBitmap = Bitmap.createScaledBitmap(rateBitmap, (int)(1.6f*strokeWidth), (int)(1.6f*strokeWidth), false);

            shareBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.share);
            shareBitmap = Bitmap.createScaledBitmap(shareBitmap, (int)(1.6f*strokeWidth), (int)(1.6f*strokeWidth), false);

            handBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.hand);
            handBitmap = Bitmap.createScaledBitmap(handBitmap, (int)(2f*strokeWidth), (int)(2f*strokeWidth), false);
        }

        //Draw settings screen
        if (MainActivity.showSettings){

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(COLOR_BLACK);
            canvas.drawRect(0, 0, getWidth(), getHeight(), paint);

            canvas.drawBitmap(homeBitmap, 1f*strokeWidth, 1f*strokeWidth, paint);

            paint.setColor(COLOR_GRAY);
            canvas.drawLine(0, 3.5f*strokeWidth, getWidth(), 3.5f*strokeWidth, paint);

            //Text size. the same for all buttons
            paint.setTextSize(1f*strokeWidth);
            //Sound button
            paint.getTextBounds(soundTxt, 0, soundTxt.length(), bounds);
            paint.setColor(COLOR_WHITE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                canvas.drawRoundRect(0.25f*getWidth(), 5.2f*strokeWidth - bounds.height(), 0.75f*getWidth(), 6.8f*strokeWidth,
                        0.2f*strokeWidth, 0.2f*strokeWidth, paint);
            }else{
                canvas.drawRect(0.25f*getWidth(), 5.2f*strokeWidth - bounds.height(), 0.75f*getWidth(), 6.8f*strokeWidth, paint);
            }

            paint.setColor(soundColor);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                canvas.drawRoundRect(0.25f*getWidth() + 0.2f*strokeWidth, 5.4f*strokeWidth - bounds.height(),
                        0.75f*getWidth()-0.2f*strokeWidth, 6.6f*strokeWidth, 0.2f*strokeWidth, 0.2f*strokeWidth, paint);

            }else{
                canvas.drawRect(0.25f*getWidth() + 0.2f*strokeWidth, 5.4f*strokeWidth - bounds.height(),
                        0.75f*getWidth() - 0.2f*strokeWidth, 6.6f*strokeWidth, paint);
            }
            paint.setColor(COLOR_WHITE);
            canvas.drawText(soundTxt, getWidth()/2f - bounds.width()/2f, 6f*strokeWidth, paint);

            //Sign in / out button
            paint.getTextBounds(signTxt, 0, signTxt.length(), bounds);
            paint.setColor(COLOR_WHITE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                canvas.drawRoundRect(0.25f*getWidth(), 8.2f*strokeWidth - bounds.height(), 0.75f*getWidth(), 9.8f*strokeWidth,
                        0.2f*strokeWidth, 0.2f*strokeWidth, paint);
            }else{
                canvas.drawRect(0.25f*getWidth(), 8.2f*strokeWidth - bounds.height(), 0.75f*getWidth(), 9.8f*strokeWidth, paint);
            }

            paint.setColor(signColor);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                canvas.drawRoundRect(0.25f*getWidth() + 0.2f*strokeWidth, 8.4f*strokeWidth - bounds.height(),
                        0.75f*getWidth()-0.2f*strokeWidth, 9.6f*strokeWidth, 0.2f*strokeWidth, 0.2f*strokeWidth, paint);
            }else{
                canvas.drawRect(0.25f*getWidth() + 0.2f*strokeWidth, 8.4f*strokeWidth - bounds.height(),
                        0.75f*getWidth()-0.2f*strokeWidth, 9.6f*strokeWidth, paint);
            }
            paint.setColor(COLOR_WHITE);
            canvas.drawText(signTxt, getWidth()/2f - bounds.width()/2f, 9f*strokeWidth, paint);

            //Line after sound and sign in buttons
            paint.setColor(COLOR_GRAY);
            canvas.drawLine(0, 10.6f*strokeWidth, getWidth(), 10.6f*strokeWidth, paint);

            //Text "Speed"
            paint.getTextBounds("Speed", 0, "Speed".length(), bounds);
            canvas.drawText("Speed", getWidth()/2f - bounds.width()/2f, 11f*strokeWidth+bounds.height(), paint);

            //Normal speed Button
            paint.getTextBounds("Normal", 0, "Normal".length(), bounds);
            paint.setColor(COLOR_WHITE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                canvas.drawRoundRect(0.25f*getWidth(), 13.2f*strokeWidth - bounds.height(), 0.75f*getWidth(), 14.8f*strokeWidth,
                        0.2f*strokeWidth, 0.2f*strokeWidth, paint);
            }else{
                canvas.drawRect(0.25f*getWidth(), 13.2f*strokeWidth - bounds.height(), 0.75f*getWidth(), 14.8f*strokeWidth, paint);
            }

            if (MainActivity.gameSpeed==0) {
                paint.setColor(COLOR_GREEN);
            }else{
                paint.setColor(COLOR_BLACK);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                canvas.drawRoundRect(0.25f*getWidth() + 0.2f*strokeWidth, 13.4f*strokeWidth - bounds.height(),
                        0.75f*getWidth()-0.2f*strokeWidth, 14.6f*strokeWidth, 0.2f*strokeWidth, 0.2f*strokeWidth, paint);
            }else{
                canvas.drawRect(0.25f*getWidth() + 0.2f*strokeWidth, 13.4f*strokeWidth - bounds.height(),
                        0.75f*getWidth()-0.2f*strokeWidth, 14.6f*strokeWidth, paint);
            }
            paint.setColor(COLOR_WHITE);
            canvas.drawText("Normal", getWidth()/2f - bounds.width()/2f, 14f*strokeWidth, paint);

            //Fast speed Button
            paint.getTextBounds("Fast", 0, "Fast".length(), bounds);
            paint.setColor(COLOR_WHITE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                canvas.drawRoundRect(0.25f*getWidth(), 16.2f*strokeWidth - bounds.height(), 0.75f*getWidth(), 17.8f*strokeWidth,
                        0.2f*strokeWidth, 0.2f*strokeWidth, paint);
            }else{
                canvas.drawRect(0.25f*getWidth(), 16.2f*strokeWidth - bounds.height(), 0.75f*getWidth(), 17.8f*strokeWidth, paint);
            }

            if (MainActivity.gameSpeed==1) {
                paint.setColor(COLOR_ORANGE);
            }else{
                paint.setColor(COLOR_BLACK);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                canvas.drawRoundRect(0.25f*getWidth() + 0.2f*strokeWidth, 16.4f*strokeWidth - bounds.height(),
                        0.75f*getWidth()-0.2f*strokeWidth, 17.6f*strokeWidth, 0.2f*strokeWidth, 0.2f*strokeWidth, paint);
            }else{
                canvas.drawRect(0.25f*getWidth() + 0.2f*strokeWidth, 16.4f*strokeWidth - bounds.height(),
                        0.75f*getWidth()-0.2f*strokeWidth, 17.6f*strokeWidth, paint);
            }
            paint.setColor(COLOR_WHITE);
            canvas.drawText("Fast", getWidth()/2f - bounds.width()/2f, 17f*strokeWidth, paint);

            //Pro speed Button
            paint.getTextBounds("Pro", 0, "Pro".length(), bounds);
            paint.setColor(COLOR_WHITE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                canvas.drawRoundRect(0.25f*getWidth(), 19.2f*strokeWidth - bounds.height(), 0.75f*getWidth(), 20.8f*strokeWidth,
                        0.2f*strokeWidth, 0.2f*strokeWidth, paint);
            }else{
                canvas.drawRect(0.25f*getWidth(), 19.2f*strokeWidth - bounds.height(), 0.75f*getWidth(), 20.8f*strokeWidth, paint);
            }

            if (MainActivity.gameSpeed==2) {
                paint.setColor(COLOR_RED);
            }else{
                paint.setColor(COLOR_BLACK);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                canvas.drawRoundRect(0.25f*getWidth() + 0.2f*strokeWidth, 19.4f*strokeWidth - bounds.height(),
                        0.75f*getWidth()-0.2f*strokeWidth, 20.6f*strokeWidth, 0.2f*strokeWidth, 0.2f*strokeWidth, paint);
            }else{
                canvas.drawRect(0.25f*getWidth() + 0.2f*strokeWidth, 19.4f*strokeWidth - bounds.height(),
                        0.75f*getWidth()-0.2f*strokeWidth, 20.6f*strokeWidth, paint);
            }
            paint.setColor(COLOR_WHITE);
            canvas.drawText("Pro", getWidth()/2f - bounds.width()/2f, 20f*strokeWidth, paint);

            //Line after speed buttons
            paint.setColor(COLOR_GRAY);
            canvas.drawLine(0, 21.6f*strokeWidth, getWidth(), 21.6f*strokeWidth, paint);

            //Tutorial button
            paint.setTextSize(1f*strokeWidth);
            paint.setColor(COLOR_WHITE);
            paint.getTextBounds("Show Tutorial", 0, "Show Tutorial".length(), bounds);
            canvas.drawText("Show Tutorial", getWidth()/2f - bounds.width()/2f, 24f*strokeWidth, paint);

            //Privacy button
            paint.setTextSize(0.8f*strokeWidth);
            paint.setColor(COLOR_GRAY);
            canvas.drawText("PRIVACY", 1f*strokeWidth, 27f*strokeWidth, paint);

            //More games button
            paint.getTextBounds("More Games", 0, "More Games".length(), bounds);
            canvas.drawText("More Games", getWidth()-1f*strokeWidth-bounds.width(), 27f*strokeWidth, paint);

            //Line after PRIVACY AND More Games buttons
            paint.setColor(COLOR_GRAY);
            canvas.drawLine(0, 28.6f*strokeWidth, getWidth(), 28.6f*strokeWidth, paint);

            return;
        }

        //Draw score screen
        if (MainActivity.showScore){
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(COLOR_BLACK);
            canvas.drawRect(0, 0, getWidth(), getHeight(), paint);

            //Score
            paint.setTextSize(2.2f*strokeWidth);
            paint.setColor(COLOR_WHITE);
            scoreTxt = ""+MainActivity.score;
            paint.getTextBounds(scoreTxt, 0, scoreTxt.length(), bounds);
            canvas.drawText(scoreTxt, getWidth()/2f-bounds.width()/2f, 0.3f*getHeight(), paint);

            //Game rings
            paint.setTextSize(1f * strokeWidth);
            paint.setColor(COLOR_GRAY);
            linesTxt = "Rings  " + MainActivity.gameRings;
            paint.getTextBounds(linesTxt, 0, linesTxt.length(), bounds);
            canvas.drawText(linesTxt, getWidth()/2f - bounds.width()/2f, 0.3f*getHeight()+2f*strokeWidth, paint);

            //Draw high score best score msg
            if (((MainActivity.score >= MainActivity.bestNormal) && MainActivity.gameSpeed==0) ||
                    ((MainActivity.score >= MainActivity.bestFast) && MainActivity.gameSpeed==1) ||
                    ((MainActivity.score >= MainActivity.bestPro) && MainActivity.gameSpeed==2)){

                paint.setTextSize(1.2f*strokeWidth);
                paint.getTextBounds("New Best Score", 0, "New Best Score".length(), bounds);
                paint.setColor(COLOR_ORANGE);
                canvas.drawText("New Best Score", getWidth()/2f - bounds.width()/2f, 0.3f*getHeight()+4f*strokeWidth, paint);
            }

            //Video continue txt
            if (continueColor == COLOR_BLUE && MainActivity.showContinue && !MainActivity.continueUnlocked) {
                paint.setTextSize(1.4f * strokeWidth);
                paint.getTextBounds(continueTxt, 0, continueTxt.length(), bounds);
                paint.setColor(continueColor);
                canvas.drawRect(0, centerY - 2f * strokeWidth, getWidth(), centerY + 2f * strokeWidth, paint);
                paint.setColor(COLOR_WHITE);
                canvas.drawText(continueTxt, getWidth() / 2f - bounds.width() / 2f, centerY + 0.5f * bounds.height(), paint);

                canvas.drawBitmap(videoBitmap, 1f*strokeWidth, centerY - 1f * strokeWidth, paint);
                canvas.drawBitmap(videoBitmap, getWidth() - 3f*strokeWidth, centerY - 1f * strokeWidth, paint);
            }

            //Main txt
            if (!MainActivity.continueUnlocked) {
                paint.setTextSize(1.4f * strokeWidth);
                paint.getTextBounds(newGameTxt, 0, newGameTxt.length(), bounds);
                paint.setColor(COLOR_GREEN);
                canvas.drawRect(0, centerY + 3f * strokeWidth, getWidth(), centerY + 7f * strokeWidth, paint);
                paint.setColor(COLOR_WHITE);
                canvas.drawText(newGameTxt, getWidth() / 2f - bounds.width() / 2f, centerY + 5f * strokeWidth + 0.5f * bounds.height(), paint);
            }else{
                paint.setTextSize(1.4f * strokeWidth);
                paint.getTextBounds(continueTxt, 0, continueTxt.length(), bounds);
                paint.setColor(COLOR_BLUE);
                canvas.drawRect(0, centerY + 3f * strokeWidth, getWidth(), centerY + 7f * strokeWidth, paint);
                paint.setColor(COLOR_WHITE);
                canvas.drawText(continueTxt, getWidth() / 2f - bounds.width() / 2f, centerY + 5f * strokeWidth + 0.5f * bounds.height(), paint);
            }

            //Draw home bitmap
            canvas.drawBitmap(homeBitmap, 1f*strokeWidth, 1f*strokeWidth, paint);

            return;
        }

        //Draw paused screen.
        if (MainActivity.pause){
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(COLOR_BLACK);
            canvas.drawRect(0, 0, getWidth(), getHeight(), paint);

            //paused txt
            paint.setTextSize(0.8f*strokeWidth);
            paint.getTextBounds("P A U S E D", 0, "P A U S E D".length(), bounds);
            paint.setColor(COLOR_GRAY_TWO);
            canvas.drawText("P A U S E D", getWidth()/2f - bounds.width()/2f, 0.6f*centerY, paint);

            //continue txt
            paint.setTextSize(1.4f*strokeWidth);
            paint.getTextBounds(continueTxt, 0, continueTxt.length(), bounds);
            paint.setColor(COLOR_BLUE);
            canvas.drawRect(0, centerY - 2f*strokeWidth, getWidth(), centerY + 2f*strokeWidth, paint);
            paint.setColor(COLOR_WHITE);
            canvas.drawText(continueTxt, getWidth()/2f - bounds.width()/2f, centerY + bounds.height()/2f, paint);

            //home icon
            canvas.drawBitmap(homeBitmap, 1f*strokeWidth, 1f*strokeWidth, paint);

            return;
        }

        //Draw background color
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(COLOR_BLACK);
        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);

        //Draw circles
        //Draw back black circle//if we use background image
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(COLOR_BLACK);
        canvas.drawCircle(centerX,centerY,radiusCircleZero+strokeWidth/2f,paint);

        //Draw playing circles 6 circles
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidth);

        //Draw arcs in the circles arcs
        for (int i=0; i < arcListArray.length; i++){

            for (Arc arc: arcListArray[i]){

                if (linesAchieved[i]){
                    if (linesBlinkingCounter %10 < 5){
                        paint.setColor(COLOR_BLACK);
                    }else{
                        paint.setColor(COLOR_WHITE);
                    }
                }else {
                    paint.setColor(arc.color);
                }

                if (MainActivity.playing) {
                    canvas.drawArc(ovals[i], arc.start * angleDev, arc.size * angleDev, false, paint);

                }else if (MainActivity.mainWorking){
                    canvas.drawArc(ovals[i], mainThreadAngleDev + arc.start*angleDev, arc.size * angleDev, false, paint);
                }
            }
        }
        //End draw playing circles and arcs inside

        //Draw standard circles with stroke black
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidth/10f);
        paint.setColor(COLOR_BLACK);
        //canvas.drawCircle(centerX, centerY, radiusCircleOut2 + strokeWidth/2f, paint);
        canvas.drawCircle(centerX, centerY, radiusCircleOut1 + strokeWidth/2f, paint);
        canvas.drawCircle(centerX, centerY, radiusCircleZero + strokeWidth/2f, paint);
        canvas.drawCircle(centerX, centerY, radiusCircleOne + strokeWidth/2f, paint);
        canvas.drawCircle(centerX, centerY, radiusCircleTwo + strokeWidth/2f, paint);
        canvas.drawCircle(centerX, centerY, radiusCircleThree + strokeWidth/2f, paint);
        canvas.drawCircle(centerX, centerY, radiusCircleFour + strokeWidth/2f, paint);
        canvas.drawCircle(centerX, centerY, radiusCircleFive + strokeWidth/2f, paint);
        //Draw standard circles with stroke white
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidth/100f);
        paint.setShader(radialGradient);
        canvas.drawCircle(centerX, centerY, radiusCircleOne + strokeWidth/2f, paint);
        canvas.drawCircle(centerX, centerY, radiusCircleTwo + strokeWidth/2f, paint);
        canvas.drawCircle(centerX, centerY, radiusCircleThree + strokeWidth/2f, paint);
        canvas.drawCircle(centerX, centerY, radiusCircleFour + strokeWidth/2f, paint);
        canvas.drawCircle(centerX, centerY, radiusCircleFive + strokeWidth/2f, paint);
        paint.setShader(null);

        //Draw outer circle with more stroke
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidth/20f);
        paint.setColor(COLOR_WHITE);
        canvas.drawCircle(centerX, centerY, radiusCircleZero + strokeWidth/2f, paint);

        //Draw running piece.
        if (MainActivity.playing &&
                linesBlinkingCounter <= 0 &&
                MainActivity.startThreadCounter <= 0) {

            paint.setStyle(Paint.Style.STROKE);

            runningOvals = getRunningOvals(centerX, centerY, radiusCircleRunning);//Here to refresh every time to the new radius
            pieceStrokeOvals = getRunningOvals(centerX, centerY, radiusCircleRunning + 0.5f*strokeWidth);

            paint.setStrokeWidth(strokeWidth);
            paint.setColor(runningPiece.arc2.color);
            canvas.drawArc(runningOvals[0], (runningPiece.arc2.start) * angleDev, runningPiece.arc2.size * angleDev, false, paint);
            paint.setStrokeWidth(strokeWidth/10f);
            paint.setColor(COLOR_BLACK);
            canvas.drawArc(pieceStrokeOvals[0], (runningPiece.arc2.start) * angleDev, runningPiece.arc2.size * angleDev, false, paint);

            paint.setStrokeWidth(strokeWidth);
            paint.setColor(runningPiece.arc3.color);
            canvas.drawArc(runningOvals[1], (runningPiece.arc3.start) * angleDev, runningPiece.arc3.size * angleDev, false, paint);
            paint.setStrokeWidth(strokeWidth/10f);
            paint.setColor(COLOR_BLACK);
            canvas.drawArc(pieceStrokeOvals[1], (runningPiece.arc3.start) * angleDev, runningPiece.arc3.size * angleDev, false, paint);

            paint.setStrokeWidth(strokeWidth);
            paint.setColor(runningPiece.arc4.color);
            canvas.drawArc(runningOvals[2], (runningPiece.arc4.start) * angleDev, runningPiece.arc4.size * angleDev, false, paint);
            paint.setStrokeWidth(strokeWidth/10f);
            paint.setColor(COLOR_BLACK);
            canvas.drawArc(pieceStrokeOvals[2], (runningPiece.arc4.start) * angleDev, runningPiece.arc4.size * angleDev, false, paint);

            paint.setStrokeWidth(strokeWidth);
            paint.setColor(runningPiece.arc5.color);
            canvas.drawArc(runningOvals[3], (runningPiece.arc5.start) * angleDev, runningPiece.arc5.size * angleDev, false, paint);
            paint.setStrokeWidth(strokeWidth/10f);
            paint.setColor(COLOR_BLACK);
            canvas.drawArc(pieceStrokeOvals[3], (runningPiece.arc5.start) * angleDev, runningPiece.arc5.size * angleDev, false, paint);
        }

        //Player has lost/ Drawing lost animation
        if (MainActivity.lostThreadCounter > 0){
            return;
        }

        //Draw lines from center to edges with stroke black and white
        for (int i = 0; i < totalspaces; i++) {
            //Black
            paint.setStrokeWidth(strokeWidth / 10f);
            paint.setColor(COLOR_BLACK);
            canvas.drawLine(centerX, centerY,
                    centerX + (float) (4f * radiusCircleZero * Math.cos(Math.toRadians(mainThreadAngleDev + (i * angleDev)))),
                    centerY + (float) (4f * radiusCircleZero * Math.sin(Math.toRadians(mainThreadAngleDev + (i * angleDev)))), paint);

            //White
            paint.setStrokeWidth(strokeWidth / 200f);
            paint.setShader(radialGradient);

            canvas.drawLine(centerX, centerY,
                    centerX + (float) ((2f*radiusCircleZero + strokeWidth / 2f) * Math.cos(Math.toRadians(mainThreadAngleDev + (i * angleDev)))),
                    centerY + (float) ((2f*radiusCircleZero + strokeWidth / 2f) * Math.sin(Math.toRadians(mainThreadAngleDev + (i * angleDev)))), paint);
            paint.setShader(null);
        }

        //Draw lines achieved bonus txt. After draw lines not to show spaces between letters
        if (linesBlinkingCounter > 0){
            paint.setStyle(Paint.Style.FILL);
            paint.setTextSize(1.5f*strokeWidth);
            paint.setColor(linesBonusColor);
            paint.getTextBounds(linesBonusTxt, 0, linesBonusTxt.length(), bounds);
            canvas.drawText(linesBonusTxt, getWidth()/2f - bounds.width()/2f, 4f*strokeWidth + (linesBlinkingCounter*0.2f*strokeWidth) , paint);
        }

        //Draw inner circle
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidth/20f);
        paint.setColor(COLOR_WHITE);
        canvas.drawCircle(centerX, centerY, radiusCircleInside, paint);

        //Draw inside circle and buttons
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(COLOR_BLACK);
        canvas.drawCircle(centerX,centerY, radiusCircleInside-strokeWidth/100f, paint);

        if (MainActivity.playing) {
            //Draw fast down button
            paint.setColor(runningPiece.arc5.color);
            canvas.drawCircle(centerX, centerY, radiusFastbutton, paint);
            paint.setColor(COLOR_BLACK);
            canvas.drawCircle(centerX, centerY, 0.6f * radiusFastbutton, paint);
            paint.setColor(COLOR_WHITE);
            canvas.drawCircle(centerX, centerY, 0.4f * radiusFastbutton, paint);

        }else if (MainActivity.mainWorking){
            //Draw play path
            paint.setColor(Color.WHITE);
            canvas.drawPath(playPath, paint);
        }
        //End drawing playing circles

        //Draw top screen panel
        if (MainActivity.playing){
            //Draw top screen panel background
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(COLOR_BLACK);
            canvas.drawRect(0, 0, getWidth(), 3f*strokeWidth, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(strokeWidth/10f);
            paint.setColor(COLOR_GRAY);
            canvas.drawLine(0, 3f*strokeWidth, getWidth(), 3f*strokeWidth, paint);

            //Draw next piece.
            if (MainActivity.startThreadCounter <= 0 &&
                    MainActivity.lostThreadCounter <= 0) {

                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(strokeWidth);
                paint.setColor(nextPiece.arc2.color);
                canvas.drawArc(nextOvals[0], (nextPiece.arc2.start) * angleDev, nextPiece.arc2.size * angleDev, false, paint);
                paint.setColor(nextPiece.arc3.color);
                canvas.drawArc(nextOvals[1], (nextPiece.arc3.start) * angleDev, nextPiece.arc3.size * angleDev, false, paint);
                paint.setColor(nextPiece.arc4.color);
                canvas.drawArc(nextOvals[2], (nextPiece.arc4.start) * angleDev, nextPiece.arc4.size * angleDev, false, paint);
                paint.setColor(nextPiece.arc5.color);
                canvas.drawArc(nextOvals[3], (nextPiece.arc5.start) * angleDev, nextPiece.arc5.size * angleDev, false, paint);
            }
        }

        //Draw score and Rings playing
        if (MainActivity.playing) {
            paint.setStyle(Paint.Style.FILL);

            paint.setTextSize(1.2f * strokeWidth);
            paint.setColor(COLOR_WHITE);
            canvas.drawText("" + MainActivity.score, getWidth() / 20f, 1.5f * strokeWidth, paint);

            paint.setTextSize(0.6f * strokeWidth);
            paint.setColor(COLOR_GRAY);
            canvas.drawText("Rings " + MainActivity.gameRings, getWidth() / 20f, 2.5f * strokeWidth, paint);

            //debug
            //canvas.drawText("" + MainActivity.sleepValue, getWidth() / 20f, 4f * strokeWidth, paint);

            //Draw score and best etc not playing
        }else if (MainActivity.mainWorking){
            paint.setStyle(Paint.Style.FILL);

            paint.setTextSize(1.4f * strokeWidth);
            paint.setColor(Color.WHITE);
            scoreTxt = ""+MainActivity.score;
            paint.getTextBounds(scoreTxt, 0, scoreTxt.length(), bounds);
            canvas.drawText(scoreTxt, getWidth()/2f-bounds.width()/2f, startingScoreY, paint);

            paint.setTextSize(0.8f * strokeWidth);
            paint.setColor(COLOR_ORANGE);

            switch (MainActivity.gameSpeed){
                case 0:bestTxt = "Best  " + MainActivity.bestNormal;break;
                case 1:bestTxt = "Best  " + MainActivity.bestFast;break;
                case 2:bestTxt = "Best  " + MainActivity.bestPro;break;
                default:bestTxt = " ";
            }

            paint.getTextBounds(bestTxt, 0, bestTxt.length(), bounds);
            canvas.drawText(bestTxt, getWidth()/2f - bounds.width()/2f, startingScoreY + 1.4f*strokeWidth, paint);

            paint.setTextSize(0.8f * strokeWidth);
            paint.setColor(COLOR_GREEN_THREE);

            switch (MainActivity.gameSpeed){
                case 0:linesTxt = "Best Rings  " + MainActivity.bestRings;break;
                case 1:linesTxt = "Best Rings  " + MainActivity.bestRingsFast;break;
                case 2:linesTxt = "Best Rings  " + MainActivity.bestRingsPro;break;
                default:linesTxt = " ";
            }

            paint.getTextBounds(linesTxt, 0, linesTxt.length(), bounds);
            canvas.drawText(linesTxt, getWidth()/2f - bounds.width()/2f, startingScoreY + 2.6f*strokeWidth, paint);

            paint.setTextSize(1.8f * strokeWidth);
            paint.setColor(COLOR_BLUE_THREE);
            paint.getTextBounds(logoTxt, 0, logoTxt.length(), bounds);
            canvas.drawText(logoTxt, centerX - bounds.width()/2f, startingScoreY + 5.5f*strokeWidth, paint);
            paint.setTextSize(0.7f * strokeWidth);
            paint.setColor(COLOR_GRAY_TWO);
            paint.getTextBounds(logoTxt2, 0, logoTxt2.length(), bounds);
            canvas.drawText(logoTxt2, centerX - bounds.width()/2f, startingScoreY + 6.5f*strokeWidth, paint);

            canvas.drawBitmap(achievementsBitmap, 1.6f*strokeWidth, startingScoreY + 1.2f*strokeWidth, paint);
            canvas.drawBitmap(leaderboardBitmap, getWidth() - 3f*strokeWidth, startingScoreY + 1.2f*strokeWidth, paint);
            canvas.drawBitmap(settingsBitmap, centerX - 4.6f*strokeWidth, centerY + radiusCircleOut1 + 0.5f*strokeWidth, paint);
            canvas.drawBitmap(rateBitmap, centerX - 0.8f*strokeWidth, centerY + radiusCircleOut1 + 0.5f*strokeWidth, paint);
            canvas.drawBitmap(shareBitmap, centerX + 2.8f*strokeWidth, centerY + radiusCircleOut1 + 0.5f*strokeWidth, paint);
        }

        //Draw pause button
        if (MainActivity.playing){
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(0.2f*strokeWidth);
            paint.setColor(COLOR_GRAY);
            canvas.drawLine(18f*getWidth()/20f, 1f*strokeWidth,
                    18f*getWidth()/20f, 2f*strokeWidth, paint);
            canvas.drawLine(18f*getWidth()/20f + 0.6f*strokeWidth, 1f*strokeWidth,
                    18f*getWidth()/20f + 0.6f*strokeWidth, 2f*strokeWidth, paint);
        }

        //show tutorial
        if (MainActivity.playing && MainActivity.tutorialCounter < 10){

            //Draw top screen panel background
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(COLOR_BLACK);
            canvas.drawRect(0, 0, getWidth(), 6f*strokeWidth, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(strokeWidth/10f);
            paint.setColor(COLOR_GRAY);
            canvas.drawLine(0, 3f*strokeWidth, getWidth(), 3f*strokeWidth, paint);
            //Draw how to play msg
            paint.setStyle(Paint.Style.FILL);
            paint.setTextSize(1.2f * strokeWidth);
            paint.setColor(COLOR_WHITE);
            paint.getTextBounds("How to Play", 0, "How to Play".length(), bounds);
            canvas.drawText("How to Play", getWidth()/2f - bounds.width()/2f, 2f*strokeWidth, paint);

            //Draw bottom skip (or lets play) button
            paint.setStyle(Paint.Style.FILL);
            if (MainActivity.tutorialCounter < 6) {
                paint.setColor(COLOR_BLACK);
            }else{
                paint.setColor(COLOR_BLUE);
            }
            canvas.drawRect(0, getHeight()-3f*strokeWidth, getWidth(), getHeight(), paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(strokeWidth/10f);
            paint.setColor(COLOR_WHITE);
            canvas.drawLine(0, getHeight()-3f*strokeWidth, getWidth(), getHeight()-3f*strokeWidth, paint);

            paint.setStyle(Paint.Style.FILL);
            paint.setTextSize(1.2f * strokeWidth);

            if (MainActivity.tutorialCounter < 6) {
                paint.setColor(COLOR_GRAY);
                paint.getTextBounds("Skip and Play", 0, "Skip and Play".length(), bounds);
                canvas.drawText("Skip and Play", getWidth() / 2f - bounds.width() / 2f, getHeight() - 1f * strokeWidth, paint);

            }else{
                paint.setColor(COLOR_WHITE);
                paint.getTextBounds("Got it! Let's Play", 0, "Got it! Let's Play".length(), bounds);
                canvas.drawText("Got it! Let's Play", getWidth() / 2f - bounds.width() / 2f, getHeight() - 1f * strokeWidth, paint);
            }

            //Set alpha values for animations
            alphaValue+=10*alphaDev;
            if (alphaValue==255 || alphaValue==55){
                alphaDev=-alphaDev;
            }

            //Draw tutorial messages in a row
            if (MainActivity.tutorialCounter==0){
                paint.setStyle(Paint.Style.FILL);
                paint.setTextSize(1f * strokeWidth);
                paint.setColor(COLOR_YELLOW);
                paint.setAlpha(alphaValue);
                paint.getTextBounds("Slide to rotate disk", 0, "Slide to rotate disk".length(), bounds);
                canvas.drawText("Slide to rotate disk", getWidth()/2f - bounds.width()/2f, 5f*strokeWidth, paint);

                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(0.2f * strokeWidth);
                paint.setColor(COLOR_YELLOW);
                paint.setAlpha(alphaValue);
                canvas.drawCircle(centerX, centerY, radiusCircleThree+0.7f*strokeWidth, paint);
                canvas.drawBitmap(handBitmap, centerX-1f*strokeWidth, centerY+radiusCircleThree,paint);

            }else if (MainActivity.tutorialCounter==1){
                paint.setStyle(Paint.Style.FILL);
                paint.setTextSize(1f * strokeWidth);
                paint.setColor(COLOR_YELLOW);
                paint.setAlpha(alphaValue);
                paint.getTextBounds("Slide to rotate disk", 0, "Slide to rotate disk".length(), bounds);
                canvas.drawText("Slide to rotate disk", getWidth()/2f - bounds.width()/2f, 5f*strokeWidth, paint);

                paint.setStyle(Paint.Style.FILL);
                paint.setTextSize(0.8f * strokeWidth);
                paint.setColor(COLOR_YELLOW);
                paint.setAlpha(alphaValue);
                paint.getTextBounds("and from the other side", 0, "and from the other side".length(), bounds);
                canvas.drawText("and from the other side", getWidth()/2f - bounds.width()/2f, 8f*strokeWidth, paint);

                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(0.2f * strokeWidth);
                paint.setColor(COLOR_YELLOW);
                paint.setAlpha(alphaValue);
                canvas.drawCircle(centerX, centerY, radiusCircleThree+0.7f*strokeWidth, paint);
                canvas.drawBitmap(handBitmap, centerX-1f*strokeWidth, centerY+radiusCircleThree,paint);

            }else if (MainActivity.tutorialCounter==2){
                paint.setStyle(Paint.Style.FILL);
                paint.setTextSize(1f * strokeWidth);
                paint.setColor(COLOR_YELLOW);
                paint.setAlpha(alphaValue);
                paint.getTextBounds("Single tap anywhere to flip", 0, "Single tap anywhere to flip".length(), bounds);
                canvas.drawText("Single tap anywhere to flip", getWidth()/2f - bounds.width()/2f, 5f*strokeWidth, paint);

            }else if (MainActivity.tutorialCounter==3){
                paint.setStyle(Paint.Style.FILL);
                paint.setTextSize(1f * strokeWidth);
                paint.setColor(COLOR_YELLOW);
                paint.setAlpha(alphaValue);
                paint.getTextBounds("One more time", 0, "One more time".length(), bounds);
                canvas.drawText("One more time", getWidth()/2f - bounds.width()/2f, 5f*strokeWidth, paint);

            }else if (MainActivity.tutorialCounter==4 || MainActivity.tutorialCounter==5){
                paint.setStyle(Paint.Style.FILL);
                paint.setTextSize(1f * strokeWidth);
                paint.setColor(COLOR_YELLOW);
                paint.setAlpha(alphaValue);
                paint.getTextBounds("Tap at the center to speed up", 0, "Tap at the center to speed up".length(), bounds);
                canvas.drawText("Tap at the center to speed up", getWidth()/2f - bounds.width()/2f, 5f*strokeWidth, paint);

                paint.setAlpha(alphaValue);
                canvas.drawBitmap(handBitmap, centerX-1f*strokeWidth, centerY+0.5f*radiusCircleInside,paint);

            }else if (MainActivity.tutorialCounter==6 ){
                paint.setStyle(Paint.Style.FILL);
                paint.setTextSize(1f * strokeWidth);
                paint.setColor(COLOR_YELLOW);
                paint.getTextBounds("Complete Rings", 0, "Complete Rings".length(), bounds);
                canvas.drawText("Complete Rings", getWidth()/2f - bounds.width()/2f, 5f*strokeWidth, paint);

                paint.setTextSize(0.8f * strokeWidth);
                paint.setColor(COLOR_GRAY);
                paint.getTextBounds("Show again", 0, "Show again".length(), bounds);
                canvas.drawText("Show again", getWidth()/2f - bounds.width()/2f, 8f*strokeWidth, paint);

            }

            paint.setAlpha(255);
        }
    }

    private RectF[] getRunningOvals(float centerX, float centerY, float radiusRunning){

        RectF[] ovals = new RectF[4];

        ovals[3] = new RectF(centerX - radiusRunning , centerY - radiusRunning,
                centerX + radiusRunning, centerY + radiusRunning);
        ovals[2] = new RectF(centerX - radiusRunning-strokeWidth, centerY - radiusRunning-strokeWidth,
                centerX + radiusRunning+strokeWidth, centerY + radiusRunning+strokeWidth);
        ovals[1] = new RectF(centerX - radiusRunning-2f*strokeWidth, centerY - radiusRunning-2f*strokeWidth,
                centerX + radiusRunning+2f*strokeWidth, centerY + radiusRunning+2f*strokeWidth);
        ovals[0] = new RectF(centerX - radiusRunning-3f*strokeWidth, centerY - radiusRunning-3f*strokeWidth,
                centerX + radiusRunning+3f*strokeWidth, centerY + radiusRunning+3f*strokeWidth);

        return ovals;
    }

    private RectF[] getOvals(float centerX, float centerY){

        RectF[] ovals = new RectF[8];

        ovals[0] = new RectF(centerX - radiusCircleOut2 , centerY - radiusCircleOut2,
                centerX + radiusCircleOut2, centerY + radiusCircleOut2);
        ovals[1] = new RectF(centerX - radiusCircleOut1 , centerY - radiusCircleOut1,
                centerX + radiusCircleOut1, centerY + radiusCircleOut1);
        ovals[2] = new RectF(centerX - radiusCircleZero, centerY - radiusCircleZero,
                centerX + radiusCircleZero, centerY + radiusCircleZero);
        ovals[3] = new RectF(centerX - radiusCircleOne, centerY - radiusCircleOne,
                centerX + radiusCircleOne, centerY + radiusCircleOne);
        ovals[4] = new RectF(centerX - radiusCircleTwo, centerY - radiusCircleTwo,
                centerX + radiusCircleTwo, centerY + radiusCircleTwo);
        ovals[5] = new RectF(centerX - radiusCircleThree, centerY - radiusCircleThree,
                centerX + radiusCircleThree, centerY + radiusCircleThree);
        ovals[6] = new RectF(centerX - radiusCircleFour, centerY - radiusCircleFour,
                centerX + radiusCircleFour, centerY + radiusCircleFour);
        ovals[7] = new RectF(centerX - radiusCircleFive, centerY - radiusCircleFive,
                centerX + radiusCircleFive, centerY + radiusCircleFive);

        return ovals;
    }

    private RectF[] getNextOvals(float centerX, float centerY, float radius){

        RectF[] ovals = new RectF[4];

        ovals[3] = new RectF(centerX - radius , centerY - radius,
                centerX + radius, centerY + radius);
        ovals[2] = new RectF(centerX - radius-0.5f*strokeWidth, centerY - radius-0.5f*strokeWidth,
                centerX + radius+0.5f*strokeWidth, centerY + radius+0.5f*strokeWidth);
        ovals[1] = new RectF(centerX - radius-2f*0.5f*strokeWidth, centerY - radius-2f*0.5f*strokeWidth,
                centerX + radius+2f*0.5f*strokeWidth, centerY + radius+2f*0.5f*strokeWidth);
        ovals[0] = new RectF(centerX - radius-3f*0.5f*strokeWidth, centerY - radius-3f*0.5f*strokeWidth,
                centerX + radius+3f*0.5f*strokeWidth, centerY + radius+3f*0.5f*strokeWidth);

        return ovals;

    }
}