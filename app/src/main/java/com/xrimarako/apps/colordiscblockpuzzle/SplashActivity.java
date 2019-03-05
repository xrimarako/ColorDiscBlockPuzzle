package com.xrimarako.apps.colordiscblockpuzzle;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class SplashActivity extends AppCompatActivity {

    ImageView splashImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        splashImageView=(ImageView)this.findViewById(R.id.splash_image_view);

        startThread();

    }

    @Override
    public void onResume(){
        super.onResume();

        setPaddings();
    }

    private void setPaddings(){
        //Metrics to determine padding for flagL
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

        float totalWidth = displaymetrics.widthPixels;
        float totalHeight = displaymetrics.heightPixels;

        float widthFinal=totalWidth;

        LinearLayout imageL=(LinearLayout)this.findViewById(R.id.splashImageViewL);

        if (totalWidth/totalHeight > 0.5625f){
            widthFinal=0.5625f*totalHeight;
        }

        int paddingLeft_Right=(int)((totalWidth-widthFinal)/2);
        imageL.setPadding(paddingLeft_Right, 0, paddingLeft_Right, 0);

    }

    private void startThread(){


        SplashImageView.counter=0;

        new Thread(new Runnable() {
            @Override
            public void run() {

                //////////////debug
                while (true){

                    try{
                        Thread.sleep(300);/////////////debug
                        //Thread.sleep(20);
                    }catch (Exception e){}

                    SplashImageView.counter++;

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            splashImageView.invalidate();
                        }
                    });

                    if (SplashImageView.counter > 4){
                        //debug
                        /*try{
                            //Thread.sleep(10000);/////////////debug
                        }catch (Exception e){}*/
                        ////
                        break;
                    }
                }

                SplashActivity.this.startActivity(new Intent(SplashActivity.this, MainActivity.class));
                SplashActivity.this.finish();

            }

        }).start();

    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

    }
}
