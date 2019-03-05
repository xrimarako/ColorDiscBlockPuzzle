package com.xrimarako.apps.colordiscblockpuzzle;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ads.consent.ConsentInformation;
import com.google.ads.consent.ConsentStatus;

public class GDPRActivity extends AppCompatActivity {

    Button persB;
    Button nonPersB;
    Button agreeB;

    private int consent = -1; //Update this when user clicks to pers or non pers buttons


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gdpr);

        TextView analyticsTV=(TextView)this.findViewById(R.id.analyticsTV);

        analyticsTV.setMovementMethod(new ScrollingMovementMethod());

        analyticsTV.setText(Html.fromHtml("<br>User's privacy is a very important subject for us. " +
                "So we'd like to inform you what sharing your device data allows us to do:<br>" +
                "&nbsp;&nbsp;&nbsp;>  Analyze your experience and find out what to change in order to improve the game's performance and quality.<br>" +
                "&nbsp;&nbsp;&nbsp;>  Identify any bugs or issues immediately and fix them as soon as possible for a better user's experience.<br><br>" +
                "This app is totally free and contains Ads. Showing <b>personalized Ads</b> helps us keep the number of Ads as less as possible. " +
                "In addition you will be shown more relevant Ads that will most likely win your interest. " +
                "Nevertheless, you can still choose what kind of Ads to be shown.<br><br>" +
                "For the above reasons we use the following platforms: <br>" +
                "&nbsp;&nbsp;&nbsp;> <b>Firebase</b><br>" +
                "&nbsp;&nbsp;&nbsp;> <b>AdMob</b><br><br>" +
                "<b>Keep in mind that you will always be able to change these settings via app's menu.</b><br>"));


        persB=(Button)this.findViewById(R.id.persB);
        persB.setText(Html.fromHtml("<b>Personalized</b><br>Support Us"));
        nonPersB=(Button)this.findViewById(R.id.nonPersB);
        nonPersB.setText(Html.fromHtml("<b>Non-Personalized</b><br>More Ads"));
        agreeB=(Button)this.findViewById(R.id.agreeB);

        if (MainActivity.non_personalized){
            persB.setBackgroundResource(R.drawable.round_transparent);
            nonPersB.setBackgroundResource(R.drawable.round_green);
        }
    }

    @Override
    public void onBackPressed() {
        return;
    }

    public void showPrivacy(View v){

        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://masgamesblog.wordpress.com/privacy-policies/color-disc/")));
            //
        } catch(Exception e) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://masgamesblog.wordpress.com/")));
            }catch (ActivityNotFoundException ex){
                Toast.makeText(this, "link not found", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void setPersonalized(View v){

        persB.setBackgroundResource(R.drawable.round_green);
        nonPersB.setBackgroundResource(R.drawable.round_transparent);

        ConsentInformation.getInstance(this)
                .setConsentStatus(ConsentStatus.PERSONALIZED);

        MainActivity.non_personalized = false;
        consent = 0;
    }

    public void setNonPersonalized(View v){

        persB.setBackgroundResource(R.drawable.round_transparent);
        nonPersB.setBackgroundResource(R.drawable.round_green);

        ConsentInformation.getInstance(this)
                .setConsentStatus(ConsentStatus.NON_PERSONALIZED);

        MainActivity.non_personalized = true;
        consent = 1;
    }

    public void exitGdpr(View v){

        ConsentInformation.getInstance(this)
                .setConsentStatus(ConsentStatus.UNKNOWN);

        moveTaskToBack(true);
    }

    public void startMainActivity(View v) {

        if (consent < 0){
            //Means no button pressed at least one time. So set default (Personalized Ads)
            setPersonalized(null);
        }

        this.finish();
    }
}
