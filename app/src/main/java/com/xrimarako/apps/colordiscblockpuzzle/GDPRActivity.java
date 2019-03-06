package com.xrimarako.apps.colordiscblockpuzzle;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ads.consent.ConsentInformation;
import com.google.ads.consent.ConsentStatus;

public class GDPRActivity extends AppCompatActivity {
    private Button persB;
    private Button nonPersB;
    private int consent = -1; //Update this when user clicks to pers or non pers buttons

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gdpr);

        TextView analyticsTV = findViewById(R.id.analyticsTV);
        analyticsTV.setMovementMethod(new ScrollingMovementMethod());
        analyticsTV.setText(getText(R.string.privacy_explanation));

        persB = findViewById(R.id.persB);
        persB.setText(getText(R.string.privacy_personalized_button));
        nonPersB = findViewById(R.id.nonPersB);
        nonPersB.setText(getText(R.string.privacy_non_personalized_button));

        if (MainActivity.non_personalized) {
            persB.setBackgroundResource(R.drawable.round_transparent);
            nonPersB.setBackgroundResource(R.drawable.round_green);
        }
    }

    @Override
    public void onBackPressed() {
        exitGdpr(null);
    }

    public void showPrivacy(View v) {

        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.policy_url))));
        } catch (Exception e) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.policy_url_alt))));
            } catch (ActivityNotFoundException ex) {
                Toast.makeText(this, "link not found", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void setPersonalized(View v) {
        setContentInformation(true);
    }

    public void setNonPersonalized(View v) {
        setContentInformation(false);
    }

    private void setContentInformation(boolean personalized) {
        persB.setBackgroundResource(personalized ? R.drawable.round_green : R.drawable.round_transparent);
        nonPersB.setBackgroundResource(personalized ? R.drawable.round_transparent : R.drawable.round_green);

        ConsentInformation.getInstance(this)
                .setConsentStatus(personalized ? ConsentStatus.PERSONALIZED : ConsentStatus.NON_PERSONALIZED);

        MainActivity.non_personalized = !personalized;
        consent = personalized ? 0 : 1;
    }

    public void exitGdpr(View v) {
        ConsentInformation.getInstance(this).setConsentStatus(ConsentStatus.UNKNOWN);
        moveTaskToBack(true);
    }

    public void startMainActivity(View v) {
        //Means no button pressed at least one time. So set default (Personalized Ads)
        if (consent < 0) setPersonalized(null);
        this.finish();
    }
}
