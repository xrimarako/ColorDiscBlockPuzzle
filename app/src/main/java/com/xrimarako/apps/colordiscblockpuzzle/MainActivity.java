package com.xrimarako.apps.colordiscblockpuzzle;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ads.consent.ConsentInfoUpdateListener;
import com.google.ads.consent.ConsentInformation;
import com.google.ads.consent.ConsentStatus;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.games.Games;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private MyImageView imageView;
    Random random = new Random();

    Thread mainThread;
    Thread gameThread;

    //General values
    private int onCreateTimes = 0;
    private int gamesPlayed = 0;

    //User touch values
    float touchX = -1;
    float touchY = -1;

    //Used by ImageView
    static boolean mainWorking = false;
    static boolean playing = false;
    static boolean pause = false;
    static boolean showScore = false;
    static boolean showContinue = false;
    static boolean continueUnlocked = false;
    static boolean showSettings = false;
    static int lostThreadCounter = 0;
    static int startThreadCounter = 0;
    static int gameSpeed = 0;
    ///end used by ImageView


    private int validationCounter = -1;//Wait for user's last move

    //Used from game thread
    private long sleepValue = 53;
    private long lastSleepValue = 50;
    //Used from main thread
    private long mainSleepValue = 75;
    int pos0 = 0;
    int pos1 = 0;
    int pos2 = 0;
    int pos3 = 0;
    int pos4 = 0;
    int pos5 = 0;

    //Game scores
    static int score = 0;
    static int bestNormal = 0;
    static int bestFast = 0;
    static int bestPro = 0;
    static int bestRings = 0;
    static int bestRingsFast = 0;
    static int bestRingsPro = 0;
    static int gameRings = 0;
    static int totalRings = 0;
    static int totalDoubles = 0;
    static int totalTriples = 0;

    //Gameplay help values
    //threshold to rotate Arc for 1 position
    float rotateThreshold;
    //Values used to change piece logic
    long changePieceTime;
    float changePieceX;
    float changePieceY;
    //Values not to give the same piece many times
    int lastUsedID = 0;
    int lastUsedTimes = 0;

    //Tutorial values
    static int tutorialCounter = 0;
    private int tutorialControlValue = 0;

    //sound
    //MediaPlayer mediaPlayer;//debug
    private SoundPool soundPool;
    private boolean soundLoaded;
    private boolean soundEnabled;
    private int rotateSound;
    private int pieceSuitsSound;
    private int lineSound;
    private int loseSound;
    private int changeSound;

    //Google Play Services Achievements Leaderboard sign in
    private GoogleSignInClient mGoogleSignInClient = null;
    private GoogleSignInAccount acount = null;
    //Achievements vars
    private boolean oneDouble = false;
    private boolean oneTriple = false;

    //Ads admob
    //////////admob app id : ca-app-pub-8020361952935930~7065376116
    static boolean non_personalized = false;
    private long lastAdShownTime = 0;
    private long adInterval;
    private InterstitialAd mInterstitialAd;
    private RewardedVideoAd mRewardedVideoAd;

    //Firebase analytics
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //GDPR code check
        checkGDPR();

        //Set ad time intervals. More for non personalized
        if (non_personalized){
            adInterval = 120000;
        }else{
            adInterval = 300000;
        }
        lastAdShownTime = System.currentTimeMillis();

        //Increase on create times and send analytics
        onCreateTimes++;
        sendAnalytics("create");

        //import Settings of game
        importSettings();

        //Ads admob init
        //Test acount: ca-app-pub-3940256099942544~3347511713
        MobileAds.initialize(this, "ca-app-pub-3940256099942544~3347511713");

        //Ads admob Interstitial
        mInterstitialAd = new InterstitialAd(this);
        //test ads : ca-app-pub-3940256099942544/1033173712
        mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        loadInterstitial();

        // Ads admob rewarded video
        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);
        mRewardedVideoAd.setRewardedVideoAdListener(new RewardedVideoAdListener() {
            @Override
            public void onRewardedVideoAdLoaded() {
                imageView.continueColor = MyImageView.COLOR_BLUE;
            }

            @Override
            public void onRewardedVideoAdOpened() {

            }

            @Override
            public void onRewardedVideoStarted() {

            }

            @Override
            public void onRewardedVideoAdClosed() {
                //Load new Ad
                imageView.continueColor = MyImageView.COLOR_GRAY_TWO;//debug
                loadRewardedVideoAd();
            }

            @Override
            public void onRewarded(RewardItem rewardItem) {

                //Player rewarded
                continueUnlocked = true;
                imageView.invalidate();
            }

            @Override
            public void onRewardedVideoAdLeftApplication() {

            }

            @Override
            public void onRewardedVideoAdFailedToLoad(int i) {

            }

            @Override
            public void onRewardedVideoCompleted() {

            }
        });

        loadRewardedVideoAd();
        //End rewarded video


        //Firebase analytics instance
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        //Google Play Services sign in client
        mGoogleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);

        imageView=(MyImageView)this.findViewById(R.id.my_image_view);

        imageView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent event) {

                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:

                        if (playing && tutorialCounter < 10){
                            //Skip and Play button or Got it! Let's play button
                            if (event.getY() > imageView.getHeight() - 3f*imageView.strokeWidth){

                                tutorialCounter = 10;
                                initFirstValues(true);

                                return false;

                            //Show again (tutorial button)
                            }else if (event.getY() > 6f*imageView.strokeWidth &&
                                    event.getY() < 9f*imageView.strokeWidth){

                                if (tutorialCounter >= 6) {
                                    tutorialCounter = 0;
                                    tutorialControlValue = 0;
                                    initFirstValues(true);
                                }

                                return false;
                            }

                        }

                        //Show Settings
                        if (showSettings){
                            //Home Button
                            if (event.getX() < 4f*imageView.strokeWidth &&
                                    event.getY() < 4f*imageView.strokeWidth){

                                playSound(rotateSound);
                                showSettings = false;

                            //Show tutorial button
                            }else if (event.getY() > 22.2f*imageView.strokeWidth &&
                                    event.getY() < 24.8f*imageView.strokeWidth &&
                                    event.getX() > 0.3f*imageView.getWidth() &&
                                    event.getX() < 0.7f*imageView.getWidth()){

                                playSound(rotateSound);
                                showSettings = false;
                                tutorialCounter = 0;
                                tutorialControlValue = 0;
                                new ThreadSwitcher().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "toGame");

                            //Privacy and more games buttons
                            }else if (event.getY() > 25.2f*imageView.strokeWidth &&
                                    event.getY() < 27.8f*imageView.strokeWidth){

                                ///Privacy from settings
                                if (event.getX() < 0.4f*imageView.getWidth()) {
                                    playSound(rotateSound);
                                    if (ConsentInformation.getInstance(MainActivity.this).isRequestLocationInEeaOrUnknown()){
                                        //Start GDPR Activity
                                        MainActivity.this.startActivity(new Intent(MainActivity.this, GDPRActivity.class));
                                    }else {
                                        showPrivacy();
                                    }

                                    //show more games
                                }else if (event.getX() > 0.6f*imageView.getWidth()){

                                    playSound(rotateSound);
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    try {
                                        intent.setData(Uri.parse("market://dev?id=7787743340322022353"));
                                        MainActivity.this.startActivity(intent);

                                    } catch (Exception e) {
                                        try {
                                            intent.setData(Uri.parse("https://play.google.com/store/apps/dev?id=7787743340322022353"));
                                            MainActivity.this.startActivity(intent);
                                        } catch (Exception ex) {
                                            showToastMsg("  Page not found  ");
                                        }
                                    }
                                }

                                //All other Buttons
                            }else if (event.getX() > 0.25f*imageView.getWidth() &&
                                    event.getX() < 0.75f*imageView.getWidth()){

                                //Sound button
                                if (event.getY() > 4.2f*imageView.strokeWidth &&
                                        event.getY() < 6.8f*imageView.strokeWidth){
                                    soundEnabled=!soundEnabled;
                                    if (soundEnabled) {
                                        playSound(rotateSound);
                                        imageView.soundTxt = "Sound ON";
                                        imageView.soundColor = MyImageView.COLOR_BLUE;
                                    }else{
                                        imageView.soundTxt = "Sound OFF";
                                        imageView.soundColor = MyImageView.COLOR_BLACK;
                                    }

                                    //Sign in / Sign out button
                                }else if (event.getY() > 7.2f*imageView.strokeWidth &&
                                        event.getY() < 9.8f*imageView.strokeWidth){

                                    playSound(rotateSound);
                                    //Sign in / out
                                    if (acount==null){
                                        signIn();
                                    }else{
                                        signOut();
                                    }

                                    //Normal speed button
                                }else if (event.getY() > 12.2f*imageView.strokeWidth &&
                                        event.getY() < 14.8f*imageView.strokeWidth){

                                    if (gameSpeed != 0) {
                                        playSound(rotateSound);
                                        gameSpeed = 0;
                                        score = 0;
                                    }

                                    //Fast speed button
                                }else if (event.getY() > 15.2f*imageView.strokeWidth &&
                                        event.getY() < 17.8f*imageView.strokeWidth){

                                    if (gameSpeed != 1) {
                                        playSound(rotateSound);
                                        gameSpeed = 1;
                                        score = 0;
                                    }

                                    //Pro speed button
                                }else if (event.getY() > 18.2f*imageView.strokeWidth &&
                                        event.getY() < 20.8f*imageView.strokeWidth){

                                    if (gameSpeed != 2) {
                                        playSound(rotateSound);
                                        gameSpeed = 2;
                                        score = 0;
                                    }
                                }
                            }
                            return false;
                        }

                        //Show score screen
                        if (showScore){
                            //Home Button
                            if (event.getX() < 4f*imageView.strokeWidth &&
                                    event.getY() < 4f*imageView.strokeWidth){

                                playSound(rotateSound);
                                showInterstitial();//Ads admob
                                lostThreadCounter = 1;
                                gameThread.interrupt();


                                //Continue (rewarded video) button clicked
                            }else if ((showContinue && !continueUnlocked) &&
                                    event.getY() > imageView.centerY - 2f*imageView.strokeWidth &&
                                    event.getY() < imageView.centerY + 2f*imageView.strokeWidth){

                                if (mRewardedVideoAd.isLoaded()) {
                                    playSound(rotateSound);
                                    mRewardedVideoAd.show();
                                }

                                //New Game Button clicked
                            }else if (event.getY() > imageView.centerY + 3f*imageView.strokeWidth &&
                                    event.getY() < imageView.centerY + 7f*imageView.strokeWidth){

                                playSound(rotateSound);
                                lostThreadCounter = 0;
                                sendAnalytics("new_game_started");
                                //New Game
                                if (!continueUnlocked) {
                                    initFirstValues(true);
                                    //playBackgroundSong();//debug
                                    gameThread.interrupt();

                                //Continue after rewarded video
                                }else {
                                    showContinue = false;
                                    continueUnlocked = false;
                                    //playBackgroundSong();//debug
                                    initFirstValues(false);
                                    gameThread.interrupt();
                                }
                            }

                            return false;
                        }

                        //Paused screen
                        if (pause){
                            //Home button clicked
                            if (event.getX() < 4f*imageView.strokeWidth &&
                                    event.getY() < 4f*imageView.strokeWidth){

                                playSound(rotateSound);
                                refreshScores();
                                showInterstitial();
                                new ThreadSwitcher().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "toMain");

                            //Continue button clicked
                            }else if (event.getY() > imageView.centerY - 2f*imageView.strokeWidth &&
                                    event.getY() < imageView.centerY + 2f*imageView.strokeWidth){
                                playSound(rotateSound);
                                //playBackgroundSong();//debug
                                pause = false;
                            }
                            return false;
                        }

                        //Playing // Buttons for playing
                        if (playing){
                            //playing
                            //Button Fast down
                            if (checkDistanceFromCenter(event.getY(), event.getX()) < 1.7f*imageView.radiusFastbutton) {

                                lastSleepValue = sleepValue;
                                sleepValue = 5;
                                return true;

                            //Button Pause
                            }else if (event.getX() > 17f*imageView.getWidth()/20f &&
                                    event.getY() < 4f*imageView.strokeWidth){
                                playSound(rotateSound);
                                //stopBackgroundSong();//debug
                                pause = true;
                                return false;

                            //Init touch X and Y
                            }else {
                                touchX = event.getX();
                                touchY = event.getY();

                                //Change Piece logic. Fired when ACTION_UP
                                if (checkDistanceFromCenter(event.getY(),event.getX()) > imageView.radiusCircleFour){
                                    changePieceTime = System.currentTimeMillis();
                                    changePieceX = event.getX();
                                    changePieceY = event.getY();
                                }
                                return true;
                            }

                            //Not playing
                        }else if (mainWorking){
                            //Button Start Playing game
                            if (checkDistanceFromCenter(event.getY(), event.getX()) < imageView.radiusCircleInside) {
                                playSound(rotateSound);
                                new ThreadSwitcher().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "toGame");
                                return false;

                            //Rest of circle. Refresh main thread
                            }else if (checkDistanceFromCenter(event.getY(), event.getX()) < imageView.radiusCircleZero){
                                for (List arcList: imageView.arcListArray){//debug check if null
                                    arcList.clear();
                                }
                                return false;

                            //Buttons at the bottom
                            }else if (event.getY() > imageView.centerY + imageView.radiusCircleZero){

                                //Settings button
                                if (event.getX() > imageView.centerX - 5.4f*imageView.strokeWidth &&
                                        event.getX() < imageView.centerX - 1.8f*imageView.strokeWidth){

                                    playSound(rotateSound);
                                    showSettings = true;

                                //Rate button
                                }else if (event.getX() >= imageView.centerX - 1.8f*imageView.strokeWidth &&
                                        event.getX() < imageView.centerX + 1.8f*imageView.strokeWidth){

                                    playSound(rotateSound);
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    try {
                                        intent.setData(Uri.parse("market://details?id=com.xrimarako.apps.colordiscblockpuzzle"));
                                        MainActivity.this.startActivity(intent);

                                    } catch (Exception e) {
                                        try {
                                            intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.xrimarako.apps.colordiscblockpuzzle"));
                                            MainActivity.this.startActivity(intent);
                                        } catch (Exception ex) {
                                            showToastMsg("  App not found  ");
                                        }
                                    }

                                //Share Button
                                }else if (event.getX() >+ imageView.centerX + 1.8f*imageView.strokeWidth &&
                                        event.getX() < imageView.centerX + 5.4f*imageView.strokeWidth){
                                    //Share logic
                                    playSound(rotateSound);
                                    Intent intent = new Intent(Intent.ACTION_SEND);
                                    intent.setType("text/plain");
                                    String text=String.format("Check out this amazing Circle Block Puzzle %n%n%s","https://play.google.com/store/apps/details?id=com.xrimarako.apps.colordiscblockpuzzle");
                                    intent.putExtra(Intent.EXTRA_TEXT, text);

                                    try {
                                        startActivity(Intent.createChooser(intent, "Share with friends"));
                                    } catch (android.content.ActivityNotFoundException ex) {

                                    }
                                }

                            //Buttons at the top. Leaderboard - Achievements
                            }else if (event.getY() > imageView.startingScoreY + 1.2f*imageView.strokeWidth &&
                                    event.getY() < imageView.startingScoreY + 3.2f*imageView.strokeWidth){

                                //Achievements Button
                                if (event.getX() > 1.6f*imageView.strokeWidth &&
                                        event.getX() < 3.6f*imageView.strokeWidth){

                                    playSound(rotateSound);
                                    showAchievements();

                                //Leaderboard Button
                                }else if (event.getX() > imageView.getWidth() - 3f*imageView.strokeWidth &&
                                        event.getX() < imageView.getWidth() - 1f*imageView.strokeWidth){

                                    playSound(rotateSound);
                                    showLeaderBoard();

                                }
                                return false;
                            }
                        }

                        return false;

                    case MotionEvent.ACTION_UP:

                        //Change Piece button (when finger up)
                        if (System.currentTimeMillis() - changePieceTime < 300 &&
                                checkDistanceFromPoint(event.getY(),event.getX(), changePieceY, changePieceX) < 0.5f*rotateThreshold){

                            if (imageView.runningPiece!=null &&
                                    checkIfPieceSuits(getFlipPiece(imageView.runningPiece.getPieceID()), imageView.radiusCircleRunning)) {
                                playSound(changeSound);
                                imageView.runningPiece = getFlipPiece(imageView.runningPiece.getPieceID());
                                imageView.invalidate();

                                if (tutorialCounter == 2 || tutorialCounter == 3){
                                    tutorialCounter++;
                                }
                            }
                        }

                        touchX = -1;
                        touchY = -1;
                        sleepValue = lastSleepValue;//Used if user has clicked button fast

                        return false;

                    case MotionEvent.ACTION_MOVE:

                        if (!playing){
                            return false;
                        }

                        //When no piece is inside not to play sound
                        if (imageView.listCircleFive.isEmpty()){
                            return false;
                        }

                        //If lost game
                        if (lostThreadCounter > 0){
                            return false;
                        }

                        //If user taps on fast down button
                        if (checkDistanceFromCenter(event.getY(), event.getX()) < imageView.radiusCircleInside) {
                            return false;
                        }

                        //Control values logic
                        if ((event.getX() - touchX < -rotateThreshold && event.getY() > imageView.centerY) ||

                                (checkDistanceFromCenter(event.getY(), event.getX()) < imageView.radiusCircleZero
                                        && event.getY() - touchY < -rotateThreshold && event.getX() < imageView.centerX) ||

                                (event.getX() - touchX >   rotateThreshold && event.getY() < imageView.centerY) ||

                                (checkDistanceFromCenter(event.getY(), event.getX()) < imageView.radiusCircleZero &&
                                        event.getY() - touchY >   rotateThreshold && event.getX() > imageView.centerX)) {

                            //If piece entered check if can go left
                            if (imageView.runningPiece.entered){
                                if (!canGoLeftOrRight(imageView.runningPiece, -1)){
                                    return true;
                                }
                            }

                            playSound(rotateSound);

                            //Increase the values of the Arcs in the lists to show moving left
                            for (List<Arc> list: imageView.arcListArray){

                                for (Arc arc: list){
                                    arc.start++;
                                    if (tutorialCounter == 0 || tutorialCounter==1){
                                        if (tutorialControlValue < 0){
                                            tutorialControlValue = 0;
                                        }
                                        tutorialControlValue++;
                                        if (tutorialControlValue == 5){
                                            tutorialCounter++;
                                        }
                                    }
                                    if (arc.start >= imageView.totalspaces){
                                        arc.start -= imageView.totalspaces;
                                    }
                                }
                            }

                            touchX = event.getX();
                            touchY = event.getY();


                        } else if ((event.getX() - touchX < -  rotateThreshold && event.getY() < imageView.centerY) ||

                                (checkDistanceFromCenter(event.getY(), event.getX()) < imageView.radiusCircleZero &&
                                        event.getY() - touchY < -  rotateThreshold && event.getX() > imageView.centerX) ||

                                (event.getX() - touchX >   rotateThreshold && event.getY() > imageView.centerY) ||

                                (checkDistanceFromCenter(event.getY(), event.getX()) < imageView.radiusCircleZero &&
                                        event.getY() - touchY >   rotateThreshold && event.getX() < imageView.centerX)) {

                            if (imageView.runningPiece.entered){
                                if (!canGoLeftOrRight(imageView.runningPiece, 1)){
                                    return true;
                                }
                            }

                            playSound(rotateSound);

                            //Decrease the values of the Arcs in the lists to show moving right
                            for (List<Arc> list: imageView.arcListArray){
                                for (Arc arc: list){
                                    arc.start--;

                                    if (tutorialCounter == 0 || tutorialCounter==1){
                                        if (tutorialControlValue > 0){
                                            tutorialControlValue = 0;
                                        }
                                        tutorialControlValue--;
                                        if (tutorialControlValue == -5){
                                            tutorialCounter++;
                                        }
                                    }
                                    if (arc.start <= 0){
                                        arc.start += imageView.totalspaces;
                                    }
                                }
                            }

                            touchX = event.getX();
                            touchY = event.getY();
                        }
                        //End of control values logic

                        return true;
                }
                return false;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        //if acount is null don t sign in. Later when user clicks on 1 of 2 icons (Leader-Achiev)
        if (GoogleSignIn.getLastSignedInAccount(this)!=null ){
            acount = GoogleSignIn.getLastSignedInAccount(this);
            imageView.signTxt = "Sign Out";
            imageView.signColor = MyImageView.COLOR_PINK;
            //to show msg Achievement unlocked msg
            Games.getGamesClient(MainActivity.this, acount).setViewForPopups(findViewById(R.id.container_pop_up));
        }
    }

    @Override
    public void onResume(){
        //Ads admob
        mRewardedVideoAd.resume(this);

        super.onResume();

        setPaddings();

        // Load the sounds
        if (soundPool == null) {
            loadSounds();
        }
        //For buttons of imageView
        if (soundEnabled){
            imageView.soundTxt = "Sound ON";
            imageView.soundColor = MyImageView.COLOR_BLUE;
        }else{
            imageView.soundTxt = "Sound OFF";
            imageView.soundColor = MyImageView.COLOR_BLACK;
        }

        //Includes Game is paused and show score
        if (playing){
            return;
        }

        //If not working start main thread
        imageView.centerX = 0; //Recalculate all image view metrics
        if (!mainWorking && !playing){//when showScore playing = true;
            startMainThread();
        }
    }

    @Override
    public void onBackPressed(){

        //Includes showScore and pause
        if (playing){
            return;
        }

        //Go back from settings screen to main screen
        if (showSettings){
            showSettings = false;
            return;
        }

        super.onBackPressed();
    }

    @Override
    protected void onStop() {
        super.onStop();

        //Release music
        /*if (mediaPlayer !=null){
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }*/

        //Release sound effects
        if (soundPool!=null) {
            soundPool.release();
            soundPool = null;
        }

        //Pause game if playing (Dont pause if showing score)
        if (playing && !showScore){
            pause = true;
            return;
        }

        //Stop Main Thread if working
        if (mainWorking){
            mainThread.interrupt();
            mainWorking = false;
        }
    }

    @Override
    protected void onPause() {
        //Ads admob
        mRewardedVideoAd.pause(this);

        super.onPause();

        //Save settings //save data
        saveSettings();
    }

    @Override
    public void onDestroy(){
        //Ads admob
        mRewardedVideoAd.destroy(this);
        super.onDestroy();

        playing = false;
        mainWorking = false;
    }

    private void setPaddings(){
        //Metrics to determine padding for flagL
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

        float totalWidth = displaymetrics.widthPixels;
        float totalHeight = displaymetrics.heightPixels;

        float widthFinal=totalWidth;

        LinearLayout imageL=(LinearLayout)this.findViewById(R.id.imageL);

        if (totalWidth/totalHeight > 0.5625f){
            widthFinal=0.5625f*totalHeight;
        }

        int paddingLeft_Right=(int)((totalWidth-widthFinal)/2);
        imageL.setPadding(paddingLeft_Right, 0, paddingLeft_Right, 0);

        int screenLayout = getResources().getConfiguration().screenLayout;
        screenLayout &= Configuration.SCREENLAYOUT_SIZE_MASK;

        switch (screenLayout) {
            case Configuration.SCREENLAYOUT_SIZE_SMALL:
                rotateThreshold = (totalWidth-paddingLeft_Right)/10f;
                break;
            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
                rotateThreshold = (totalWidth-paddingLeft_Right)/15f;
                break;
            case Configuration.SCREENLAYOUT_SIZE_LARGE:
                rotateThreshold = (totalWidth-paddingLeft_Right)/20f;
                break;
            case Configuration.SCREENLAYOUT_SIZE_XLARGE: //is API >= 9
                rotateThreshold = (totalWidth-paddingLeft_Right)/30f;
                break;
            default:
                rotateThreshold = (totalWidth-paddingLeft_Right)/15f;
                break;
        }
    }

    private class ThreadSwitcher extends AsyncTask<String, Void, String>{

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected String doInBackground(String... msg) {

            if (msg[0].equalsIgnoreCase("toGame")){
                mainThread.interrupt();
                //playBackgroundSong();//debug

            }else if (msg[0].equalsIgnoreCase("toMain")){
                gameThread.interrupt();
            }

            return msg[0];
        }

        @Override
        protected void onPostExecute(String result) {

            if (result.equalsIgnoreCase("toGame")){

                initFirstValues(true);
                //Increase games played and send analytics
                gamesPlayed++;
                sendAnalytics("game_started");

                startGameThread();

            }else if (result.equalsIgnoreCase("toMain")){

                for (List<Arc> list: imageView.arcListArray){
                    if (list!=null) {
                        list.clear();
                    }
                }

                validationCounter = -1;
                mainSleepValue = 75;
                pause = false;

                startMainThread();
            }
        }
    }

    private void startMainThread(){

        mainThread = new Thread(new MainRunnable());
        mainThread.start();
    }

    public class MainRunnable implements Runnable{

        @Override
        public void run() {

            mainWorking = true;

            while (mainWorking){

                //Sleep before end
                try{
                    Thread.sleep(mainSleepValue);
                }catch (InterruptedException e){
                    //new ThreadSwitcher().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "toGame");
                    break;
                }

                //Inside UI thread to avoid concurrent exception
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (!allListsAreFull()) {//debug || true
                            mainSleepValue = 75;

                            int size = 1 + random.nextInt(4);
                            Arc arc = new Arc(pos0, size, MyImageView.COLORS[random.nextInt(MyImageView.COLORS.length)]);
                            imageView.listCircleZero.add(arc);
                            pos0 += size;

                            size = 1 + random.nextInt(4);
                            arc = new Arc(pos1, size, MyImageView.COLORS[random.nextInt(MyImageView.COLORS.length)]);
                            imageView.listCircleOne.add(arc);
                            pos1 += size;

                            size = 1 + random.nextInt(4);
                            arc = new Arc(pos2, size, MyImageView.COLORS[random.nextInt(MyImageView.COLORS.length)]);
                            imageView.listCircleTwo.add(arc);
                            pos2 += size;

                            size = 1 + random.nextInt(4);
                            arc = new Arc(pos3, size, MyImageView.COLORS[random.nextInt(MyImageView.COLORS.length)]);
                            imageView.listCircleThree.add(arc);
                            pos3 += size;

                            size = 1 + random.nextInt(4);
                            arc = new Arc(pos4, size, MyImageView.COLORS[random.nextInt(MyImageView.COLORS.length)]);
                            imageView.listCircleFour.add(arc);
                            pos4 += size;

                            size = 1 + random.nextInt(4);
                            arc = new Arc(pos5, size, MyImageView.COLORS[random.nextInt(MyImageView.COLORS.length)]);
                            imageView.listCircleFive.add(arc);
                            pos5 += size;

                        } else {
                            mainSleepValue = 50;
                            imageView.mainThreadAngleDev++;
                            if (imageView.mainThreadAngleDev >= 360) {
                                imageView.mainThreadAngleDev -= 360;
                            }
                        }

                        //Invalidate image
                        if (!playing){
                            imageView.invalidate();
                        }
                    }
                });

            }//End of while

            mainWorking = false;
            imageView.mainThreadAngleDev = 0; //Ensure the right angle of the arcs
        }
    }

    private void startGameThread(){

        gameThread = new Thread(new GameRunnable());
        gameThread.start();
    }

    public class GameRunnable implements Runnable{

        @Override
        public void run() {

            playing = true;

            while (playing){

                //Player lost. Showing Lost Thread
                while (lostThreadCounter > 0){

                    try{
                        Thread.sleep(30);
                    }catch (InterruptedException e){}
                    //Ending Lost Thread
                    //Waiting to accept continue
                    if (lostThreadCounter == 2){

                        if (!showScore){

                            try{
                                Thread.sleep(1000);
                                showScore = true;
                                imageView.centerX = 0;//Recalculate strokewidth and other values

                            }catch (InterruptedException e){}

                        }else {

                            if (!showContinue){
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        //Ads admob show Interstitial
                                        showInterstitial();
                                    }
                                });
                            }

                            try {
                                Thread.sleep(10000);
                            } catch (InterruptedException e) {
                            }
                        }

                    //Game finished. Back to main
                    }else if (lostThreadCounter == 1){
                        playing = false;
                        showScore = false;
                        continueUnlocked = false;
                        lostThreadCounter = 0;
                        new ThreadSwitcher().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "toMain");
                        break;

                    }else if (lostThreadCounter == 30-1){

                        //stopBackgroundSong();//debug
                        playSound(loseSound);
                        refreshScores();

                        lostThreadCounter--;

                    }else{
                        imageView.strokeWidth += 0.15f*imageView.strokeWidth;
                        lostThreadCounter--;
                    }

                    //Refresh image to losing thread
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            imageView.invalidate();
                        }
                    });

                    continue;
                }
                //End Lost Thread

                //Check if game ended and break outer while
                if (!playing){
                    break;
                }

                //Player clicked continue button. Don t show score
                if (showScore){
                    showScore = false;
                }

                //Small Thread removing pieces after continue selected
                while (startThreadCounter-- > 0){

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            imageView.listCircleOut1.clear();
                            imageView.listCircleOut2.clear();
                            for (List arcList: imageView.arcListArray){
                                if (!arcList.isEmpty()) {
                                    arcList.remove(0);
                                }
                            }
                            imageView.invalidate();
                        }
                    });

                    //Break if lists are empty
                    if (allListsAreEmpty()){
                        startThreadCounter = 0;
                        break;
                    }

                    //Remove all and break at the end
                    if (startThreadCounter == 1){
                        for (List arcList: imageView.arcListArray){
                            arcList.clear();
                        }
                    }

                    try{
                        Thread.sleep(30);
                    }catch (InterruptedException e){}

                    continue;
                }

                //Main Sleep
                try{
                    Thread.sleep(sleepValue);
                    //Means fast button selected
                    if (sleepValue == 5){
                        score++;
                        //Tutorial values
                        if (tutorialCounter == 4){
                            tutorialCounter++;
                        }
                    }

                }catch (InterruptedException e){
                    break;
                }

                if (pause){
                    //Refresh image to show pause
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            imageView.invalidate();
                        }
                    });
                    continue;
                }

                //Draw lines achieved
                while (imageView.linesBlinkingCounter-- > 0){

                    try{
                        Thread.sleep(50);
                    }catch (InterruptedException e){}

                    //Refresh image to show line done
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            imageView.invalidate();
                        }
                    });

                    if (imageView.linesBlinkingCounter <= 0){
                        //Run on UI to avoid concurrent modification exception
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                for (int i=0; i<imageView.linesAchieved.length; i++){
                                    if (imageView.linesAchieved[i]){
                                        clearAfterLineAchieved(i);
                                    }
                                    imageView.linesAchieved[i] = false;
                                }
                            }
                        });
                        break;
                    }

                    continue;
                }
                //End draw lines achieved

                //Increase running piece Y value// Don t go down if waiting validation. Wait for the counter
                if (validationCounter < 0 && tutorialCounter > 6) {
                    imageView.radiusCircleRunning -= imageView.downSpeed;

                }else if (tutorialCounter == 2 || tutorialCounter == 3){
                    imageView.radiusCircleRunning = 1.8f*imageView.radiusCircleTwo;

                }else if (tutorialCounter == 4){
                    if (validationCounter < 0) {
                        imageView.radiusCircleRunning -= 0.2f * imageView.downSpeed;
                    }
                    if (validationCounter==0){
                        validationCounter++;
                    }

                }else if (tutorialCounter == 5){
                    if (validationCounter < 0) {
                        if (sleepValue == 5) {
                            imageView.radiusCircleRunning -= imageView.downSpeed;
                        } else {
                            imageView.radiusCircleRunning -= 0.2f * imageView.downSpeed;
                        }
                    }else{
                        validationCounter++;
                    }

                }else if (tutorialCounter == 6){
                    continue;

                }else{
                    validationCounter++;
                }

                //Invalidate image
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        //Refreshing image view
                        imageView.invalidate();

                        //Piece has entered the circle //inside UIThread to avoid ConcurrentModificationException

                        //Piece reached the end of circle 5
                        if (imageView.radiusCircleRunning <= imageView.radiusCircleInside + 0.5f * imageView.strokeWidth) {

                            if (validationCounter < 0) {
                                validationCounter = 0;
                            }

                            if (validationCounter > 5) {
                                putArcsInLists(6);
                                initNextPiece();
                            }

                        }else if (imageView.radiusCircleRunning > imageView.radiusCircleOut2 + 1f*imageView.strokeWidth){

                            if (validationCounter > 5) {
                                validationCounter = -1;
                            }

                        }else {

                            if (imageView.radiusCircleRunning <= imageView.radiusCircleOut2){
                                imageView.runningPiece.entered = true;
                            }

                            if (!checkIfPieceSuits(imageView.runningPiece, imageView.radiusCircleRunning)){

                                if (validationCounter < 0) {
                                    validationCounter = 0;
                                }

                                if (validationCounter > 5) {
                                    if (imageView.radiusCircleRunning <= imageView.radiusCircleFour){
                                        putArcsInLists(5);
                                    }else if (imageView.radiusCircleRunning <= imageView.radiusCircleThree){
                                        putArcsInLists(4);
                                    }else if (imageView.radiusCircleRunning <= imageView.radiusCircleTwo){
                                        putArcsInLists(3);
                                    }else if (imageView.radiusCircleRunning <= imageView.radiusCircleOne){
                                        putArcsInLists(2);
                                    }else if (imageView.radiusCircleRunning <= imageView.radiusCircleZero){
                                        putArcsInLists(1);

                                    }else if (imageView.radiusCircleRunning > imageView.radiusCircleZero){
                                        lostThreadCounter = 30;
                                    }

                                    if (lostThreadCounter <= 0) {
                                        initNextPiece();
                                    }
                                }

                            } else {
                                validationCounter = -1;
                            }
                        }
                    }
                });
            }//End of while

            playing = false;
        }
    }

    private void initFirstValues(boolean start){

        if (start) {
            score = 0;
            gameRings = 0;
            //reset speed
            switch (gameSpeed){
                case 0:sleepValue = lastSleepValue = 53;break;
                case 1:sleepValue = lastSleepValue = 45;break;
                case 2:sleepValue = lastSleepValue = 40;break;
            }

            showContinue = true;

            startThreadCounter = 30;//For starting animation

            if (tutorialCounter == 0){
                startThreadCounter = 0;
                for (List<Arc> list: imageView.arcListArray){
                    list.clear();
                }
                imageView.listCircleFive.add(new Arc(15, 3, MyImageView.COLOR_GREEN));
                imageView.listCircleFour.add(new Arc(15, 1, MyImageView.COLOR_GREEN));
            }

        }else{
            startThreadCounter = 30;//For starting animation
        }

        //For Achievements
        oneDouble = false;
        oneTriple = false;

        validationCounter = -1;//init to start correctly
        imageView.mainThreadAngleDev = 0;//Reset for lines to take the start positions


        if (tutorialCounter == 0) {
            imageView.runningPiece = getPiece(4);
        }else {
            imageView.runningPiece = getPiece(-1);
        }

        imageView.radiusCircleRunning = 2f*imageView.radiusCircleZero;
        imageView.nextPiece = getPiece(-1);
    }

    private void initNextPiece(){

        validationCounter = -1;//refresh every new piece

        imageView.runningPiece = imageView.nextPiece;
        imageView.radiusCircleRunning = 2f*imageView.radiusCircleZero;
        imageView.nextPiece = getPiece(-1);
        sleepValue = lastSleepValue;
    }

    private void putArcsInLists(int lastLine){

        //Play sound
        playSound(pieceSuitsSound);

        if (tutorialCounter==5){
            tutorialCounter++;
        }

        switch (lastLine){
            case 6:
                imageView.listCircleZero.add(imageView.runningPiece.arc0);
                imageView.listCircleOne.add(imageView.runningPiece.arc1);
                imageView.listCircleTwo.add(imageView.runningPiece.arc2);
                imageView.listCircleThree.add(imageView.runningPiece.arc3);
                imageView.listCircleFour.add(imageView.runningPiece.arc4);
                imageView.listCircleFive.add(imageView.runningPiece.arc5);
                break;
            case 5:
                imageView.listCircleOut1.add(imageView.runningPiece.arc0);
                imageView.listCircleZero.add(imageView.runningPiece.arc1);
                imageView.listCircleOne.add(imageView.runningPiece.arc2);
                imageView.listCircleTwo.add(imageView.runningPiece.arc3);
                imageView.listCircleThree.add(imageView.runningPiece.arc4);
                imageView.listCircleFour.add(imageView.runningPiece.arc5);
                break;
            case 4:
                imageView.listCircleOut2.add(imageView.runningPiece.arc0);
                imageView.listCircleOut1.add(imageView.runningPiece.arc1);
                imageView.listCircleZero.add(imageView.runningPiece.arc2);
                imageView.listCircleOne.add(imageView.runningPiece.arc3);
                imageView.listCircleTwo.add(imageView.runningPiece.arc4);
                imageView.listCircleThree.add(imageView.runningPiece.arc5);
                break;
            case 3:
                imageView.listCircleOut2.add(imageView.runningPiece.arc1);
                imageView.listCircleOut1.add(imageView.runningPiece.arc2);
                imageView.listCircleZero.add(imageView.runningPiece.arc3);
                imageView.listCircleOne.add(imageView.runningPiece.arc4);
                imageView.listCircleTwo.add(imageView.runningPiece.arc5);
                break;
            case 2:
                imageView.listCircleOut2.add(imageView.runningPiece.arc2);
                imageView.listCircleOut1.add(imageView.runningPiece.arc3);
                imageView.listCircleZero.add(imageView.runningPiece.arc4);
                imageView.listCircleOne.add(imageView.runningPiece.arc5);
                break;
            case 1:
                imageView.listCircleOut2.add(imageView.runningPiece.arc3);
                imageView.listCircleOut1.add(imageView.runningPiece.arc4);
                imageView.listCircleZero.add(imageView.runningPiece.arc5);
                break;
        }

        //Increase score
        score += imageView.runningPiece.getScore();

        //Check if lines achieved. After putting arcs into circles
        imageView.linesAchieved = linesAchieved();
        for (boolean lineAchieved: imageView.linesAchieved){

            if (lineAchieved){
                imageView.linesBlinkingCounter = 20;
                playSound(lineSound);

                break;
            }
        }
    }

    private void clearAfterLineAchieved(int lineNo){

        //Check if line completed
        if (lineNo == 2){
            imageView.listCircleZero.clear();
            imageView.listCircleZero.addAll(imageView.listCircleOut1);
            imageView.listCircleOut1.clear();
            imageView.listCircleOut1.addAll(imageView.listCircleOut2);
            imageView.listCircleOut2.clear();
        }
        if (lineNo == 3){
            imageView.listCircleOne.clear();
            imageView.listCircleOne.addAll(imageView.listCircleZero);
            imageView.listCircleZero.clear();
            imageView.listCircleZero.addAll(imageView.listCircleOut1);
            imageView.listCircleOut1.clear();
            imageView.listCircleOut1.addAll(imageView.listCircleOut2);
            imageView.listCircleOut2.clear();
        }

        if (lineNo == 4){
            imageView.listCircleTwo.clear();
            imageView.listCircleTwo.addAll(imageView.listCircleOne);
            imageView.listCircleOne.clear();
            imageView.listCircleOne.addAll(imageView.listCircleZero);
            imageView.listCircleZero.clear();
            imageView.listCircleZero.addAll(imageView.listCircleOut1);
            imageView.listCircleOut1.clear();
            imageView.listCircleOut1.addAll(imageView.listCircleOut2);
            imageView.listCircleOut2.clear();
        }

        if (lineNo == 5){
            imageView.listCircleThree.clear();
            imageView.listCircleThree.addAll(imageView.listCircleTwo);
            imageView.listCircleTwo.clear();
            imageView.listCircleTwo.addAll(imageView.listCircleOne);
            imageView.listCircleOne.clear();
            imageView.listCircleOne.addAll(imageView.listCircleZero);
            imageView.listCircleZero.clear();
            imageView.listCircleZero.addAll(imageView.listCircleOut1);
            imageView.listCircleOut1.clear();
            imageView.listCircleOut1.addAll(imageView.listCircleOut2);
            imageView.listCircleOut2.clear();
        }

        if (lineNo == 6){
            imageView.listCircleFour.clear();
            imageView.listCircleFour.addAll(imageView.listCircleThree);
            imageView.listCircleThree.clear();
            imageView.listCircleThree.addAll(imageView.listCircleTwo);
            imageView.listCircleTwo.clear();
            imageView.listCircleTwo.addAll(imageView.listCircleOne);
            imageView.listCircleOne.clear();
            imageView.listCircleOne.addAll(imageView.listCircleZero);
            imageView.listCircleZero.clear();
            imageView.listCircleZero.addAll(imageView.listCircleOut1);
            imageView.listCircleOut1.clear();
            imageView.listCircleOut1.addAll(imageView.listCircleOut2);
            imageView.listCircleOut2.clear();
        }

        if (lineNo == 7){
            imageView.listCircleFive.clear();
            imageView.listCircleFive.addAll(imageView.listCircleFour);
            imageView.listCircleFour.clear();
            imageView.listCircleFour.addAll(imageView.listCircleThree);
            imageView.listCircleThree.clear();
            imageView.listCircleThree.addAll(imageView.listCircleTwo);
            imageView.listCircleTwo.clear();
            imageView.listCircleTwo.addAll(imageView.listCircleOne);
            imageView.listCircleOne.clear();
            imageView.listCircleOne.addAll(imageView.listCircleZero);
            imageView.listCircleZero.clear();
            imageView.listCircleZero.addAll(imageView.listCircleOut1);
            imageView.listCircleOut1.clear();
            imageView.listCircleOut1.addAll(imageView.listCircleOut2);
            imageView.listCircleOut2.clear();
        }
    }

    private void refreshScores(){

        switch (gameSpeed){
            case 0:
                if (gameRings > bestRings){
                    bestRings = gameRings;
                }
                if (score > bestNormal){
                    bestNormal = score;
                }else{
                    break;
                }
                if (acount!=null) {
                    Games.getLeaderboardsClient(MainActivity.this, acount)
                            .submitScore(getString(R.string.leader_id_normal), score);
                }
                break;
            case 1:
                if (gameRings > bestRingsFast){
                    bestRingsFast = gameRings;
                }
                if (score > bestFast){
                    bestFast = score;
                }else{
                    break;
                }
                if (acount!=null) {
                    Games.getLeaderboardsClient(MainActivity.this, acount)
                            .submitScore(getString(R.string.leader_id_fast), score);
                }
                break;
            case 2:
                if (gameRings > bestRingsPro){
                    bestRingsPro = gameRings;
                }
                if (score > bestPro){
                    bestPro = score;
                }else{
                    break;
                }
                if (acount!=null) {
                    Games.getLeaderboardsClient(MainActivity.this, acount)
                            .submitScore(getString(R.string.leader_id_pro), score);
                }
                break;
        }

        //Achievements
        refreshAchievements();

        //Analytics
        sendAnalytics("speed_" + gameSpeed);
        sendAnalytics("rings_" + gameRings);
    }

    private void refreshAchievements(){

        //Achievements increment and unlock
        if (gameRings >= 5){
            setAchievementsSteps(getString(R.string.ach_single_five_rings_id), -1);
        }

        if (gameRings >= 10){
            setAchievementsSteps(getString(R.string.ach_single_ten_rings_id), -1);
        }

        if (gameRings >= 20){
            setAchievementsSteps(getString(R.string.ach_single_twenty_rings_id), -1);
        }

        if (gameRings >= 50){
            setAchievementsSteps(getString(R.string.ach_single_fifty_rings_id), -1);
        }

        if (gameRings >= 100){
            setAchievementsSteps(getString(R.string.ach_single_hundred_rings_id), -1);
        }

        if (totalRings > 0) {
            setAchievementsSteps(getString(R.string.ach_first_ring_id), -1);
        }
        setAchievementsSteps(getString(R.string.ach_ten_rings_id), totalRings);
        setAchievementsSteps(getString(R.string.ach_hundred_rings_id), totalRings);
        setAchievementsSteps(getString(R.string.ach_thousand_rings_id), totalRings);

        if (totalDoubles > 0) {
            setAchievementsSteps(getString(R.string.ach_first_double_id), -1);
        }
        setAchievementsSteps(getString(R.string.ach_ten_double_id), totalDoubles);
        setAchievementsSteps(getString(R.string.ach_hundred_double_id), totalDoubles);

        if (totalTriples > 0) {
            setAchievementsSteps(getString(R.string.ach_first_triple_id), -1);
        }
        setAchievementsSteps(getString(R.string.ach_ten_triple_id), totalTriples);
        setAchievementsSteps(getString(R.string.ach_hundred_triple_id), totalTriples);

        if (oneDouble && oneTriple){
            setAchievementsSteps(getString(R.string.ach_single_double_triple_id), -1);
        }

    }

    private void refreshSpeeds(){

        switch (gameSpeed){

            case 0:
                if (gameRings <= 5){
                    sleepValue = lastSleepValue = 53;

                }else if (gameRings < 10){
                    sleepValue = lastSleepValue = 48;

                }else if (gameRings < 40){
                    if (gameRings %5==0){
                        lastSleepValue-=2;
                        sleepValue = lastSleepValue;
                    }

                }else{
                    if (gameRings %5==0){
                        lastSleepValue-=1;
                        sleepValue = lastSleepValue;
                    }
                }
                break;

            case 1:
                if (gameRings <= 5){
                    sleepValue = lastSleepValue = 45;

                }else if (gameRings < 10){
                    sleepValue = lastSleepValue = 41;

                }else if (gameRings < 40){
                    if (gameRings %5==0){
                        lastSleepValue-=2;
                        sleepValue = lastSleepValue;
                    }

                }else{
                    if (gameRings %5==0){
                        lastSleepValue-=1;
                        sleepValue = lastSleepValue;
                    }
                }
                break;

            case 2:
                if (gameRings <= 5){
                    sleepValue = lastSleepValue = 40;

                }else if (gameRings < 10){
                    sleepValue = lastSleepValue = 37;

                }else if (gameRings < 40){
                    if (gameRings %5==0){
                        lastSleepValue-=2;
                        sleepValue = lastSleepValue;
                    }

                }else{
                    if (gameRings %5==0){
                        lastSleepValue-=1;
                        sleepValue = lastSleepValue;
                    }
                }
                break;
        }
    }

    private boolean[] linesAchieved(){

        int rings = 0;
        boolean[] linesAchieved = new boolean[8];

        //Check if line completed
        if (checkIfLine(0)){
            rings++;
            linesAchieved[2] = true;
        }
        if (checkIfLine(1)){
            rings++;
            linesAchieved[3] = true;
        }

        if (checkIfLine(2)){
            rings++;
            linesAchieved[4] = true;
        }

        if (checkIfLine(3)){
            rings++;
            linesAchieved[5] = true;
        }

        if (checkIfLine(4)){
            rings++;
            linesAchieved[6] = true;
        }

        if (checkIfLine(5)){
            rings++;
            linesAchieved[7] = true;
        }

        switch (rings){
            case 1:
                score += 500;
                imageView.linesBonusColor = MyImageView.COLOR_WHITE;
                imageView.linesBonusTxt = "Ring 500";
                break;
            case 2:
                score += 2000;
                totalDoubles++;
                oneDouble = true;
                imageView.linesBonusColor = MyImageView.COLOR_YELLOW_TWO;
                imageView.linesBonusTxt = "Double 2000";
                break;
            case 3:
                score += 5000;
                totalTriples++;
                oneTriple = true;
                imageView.linesBonusColor = MyImageView.COLOR_PINK_TWO;
                imageView.linesBonusTxt = "Triple 5000";
                break;
        }

        for (int i=0; i<rings; i++){
            gameRings++;
            refreshSpeeds();
        }
        totalRings +=rings;

        return linesAchieved;
    }

    private boolean allListsAreFull(){

        boolean allFull = true;

        for (int i=2; i<imageView.arcListArray.length; i++){
            if (!isListFull(imageView.arcListArray[i])){
                return false;
            }
        }

        return allFull;
    }

    private boolean allListsAreEmpty(){

        for (List arcLIst: imageView.arcListArray){
            if (!arcLIst.isEmpty()){
                return false;
            }
        }

        return true;
    }

    private boolean isListFull(List<Arc> list){

        if (list == null){
            return false;
        }

        int totalList=0;
        for (Arc arc: list){
            totalList+=arc.size;
        }

        if (totalList >= imageView.totalspaces){
            return true;
        }

        return false;

    }

    private boolean checkIfLine(int lineID){

        switch (lineID){
            case 0:
                return isListFull(imageView.listCircleZero);

            case 1:
                return isListFull(imageView.listCircleOne);

            case 2:
                return isListFull(imageView.listCircleTwo);

            case 3:
                return isListFull(imageView.listCircleThree);

            case 4:
                return isListFull(imageView.listCircleFour);

            case 5:
                return isListFull(imageView.listCircleFive);

        }

        return false;
    }

    private boolean checkIfPasses(int arcID, int listID, Piece runningPiece){

        switch (arcID){
            case 0:
                switch (listID){
                    case -2:
                        for (Arc arc: imageView.listCircleOut2){
                            if (arc.hasConflict(runningPiece.arc0)){
                                return false;
                            }
                        }
                        break;
                    case -1:
                        for (Arc arc: imageView.listCircleOut1){
                            if (arc.hasConflict(runningPiece.arc0)){
                                return false;
                            }
                        }
                        break;
                    case 0:
                        for (Arc arc: imageView.listCircleZero){
                            if (arc.hasConflict(runningPiece.arc0)){
                                return false;
                            }
                        }
                        break;
                    case 1:
                        for (Arc arc: imageView.listCircleOne){
                            if (arc.hasConflict(runningPiece.arc0)){
                                return false;
                            }
                        }
                        break;
                    case 2:
                        for (Arc arc: imageView.listCircleTwo){
                            if (arc.hasConflict(runningPiece.arc0)){
                                return false;
                            }
                        }
                        break;
                    case 3:
                        for (Arc arc: imageView.listCircleThree){
                            if (arc.hasConflict(runningPiece.arc0)){
                                return false;
                            }
                        }
                        break;
                    case 4:
                        for (Arc arc: imageView.listCircleFour){
                            if (arc.hasConflict(runningPiece.arc0)){
                                return false;
                            }
                        }
                        break;
                    case 5:
                        for (Arc arc: imageView.listCircleFive){
                            if (arc.hasConflict(runningPiece.arc0)){
                                return false;
                            }
                        }
                        break;
                }
                break;
            case 1:
                switch (listID){
                    case -2:
                        for (Arc arc: imageView.listCircleOut2){
                            if (arc.hasConflict(runningPiece.arc1)){
                                return false;
                            }
                        }
                        break;
                    case -1:
                        for (Arc arc: imageView.listCircleOut1){
                            if (arc.hasConflict(runningPiece.arc1)){
                                return false;
                            }
                        }
                        break;
                    case 0:
                        for (Arc arc: imageView.listCircleZero){
                            if (arc.hasConflict(runningPiece.arc1)){
                                return false;
                            }
                        }
                        break;
                    case 1:
                        for (Arc arc: imageView.listCircleOne){
                            if (arc.hasConflict(runningPiece.arc1)){
                                return false;
                            }
                        }
                        break;
                    case 2:
                        for (Arc arc: imageView.listCircleTwo){
                            if (arc.hasConflict(runningPiece.arc1)){
                                return false;
                            }
                        }
                        break;
                    case 3:
                        for (Arc arc: imageView.listCircleThree){
                            if (arc.hasConflict(runningPiece.arc1)){
                                return false;
                            }
                        }
                        break;
                    case 4:
                        for (Arc arc: imageView.listCircleFour){
                            if (arc.hasConflict(runningPiece.arc1)){
                                return false;
                            }
                        }
                        break;
                    case 5:
                        for (Arc arc: imageView.listCircleFive){
                            if (arc.hasConflict(runningPiece.arc1)){
                                return false;
                            }
                        }
                        break;
                }
                break;

            case 2:
                switch (listID){
                    case -2:
                        for (Arc arc: imageView.listCircleOut2){
                            if (arc.hasConflict(runningPiece.arc2)){
                                return false;
                            }
                        }
                        break;
                    case -1:
                        for (Arc arc: imageView.listCircleOut1){
                            if (arc.hasConflict(runningPiece.arc2)){
                                return false;
                            }
                        }
                        break;
                    case 0:
                        for (Arc arc: imageView.listCircleZero){
                            if (arc.hasConflict(runningPiece.arc2)){
                                return false;
                            }
                        }
                        break;
                    case 1:
                        for (Arc arc: imageView.listCircleOne){
                            if (arc.hasConflict(runningPiece.arc2)){
                                return false;
                            }
                        }
                        break;
                    case 2:
                        for (Arc arc: imageView.listCircleTwo){
                            if (arc.hasConflict(runningPiece.arc2)){
                                return false;
                            }
                        }
                        break;
                    case 3:
                        for (Arc arc: imageView.listCircleThree){
                            if (arc.hasConflict(runningPiece.arc2)){
                                return false;
                            }
                        }
                        break;
                    case 4:
                        for (Arc arc: imageView.listCircleFour){
                            if (arc.hasConflict(runningPiece.arc2)){
                                return false;
                            }
                        }
                        break;
                    case 5:
                        for (Arc arc: imageView.listCircleFive){
                            if (arc.hasConflict(runningPiece.arc2)){
                                return false;
                            }
                        }
                        break;
                }
                break;

            case 3:
                switch (listID){
                    case -2:
                        for (Arc arc: imageView.listCircleOut2){
                            if (arc.hasConflict(runningPiece.arc3)){
                                return false;
                            }
                        }
                        break;
                    case -1:
                        for (Arc arc: imageView.listCircleOut1){
                            if (arc.hasConflict(runningPiece.arc3)){
                                return false;
                            }
                        }
                        break;
                    case 0:
                        for (Arc arc: imageView.listCircleZero){
                            if (arc.hasConflict(runningPiece.arc3)){
                                return false;
                            }
                        }
                        break;
                    case 1:
                        for (Arc arc: imageView.listCircleOne){
                            if (arc.hasConflict(runningPiece.arc3)){
                                return false;
                            }
                        }
                        break;
                    case 2:
                        for (Arc arc: imageView.listCircleTwo){
                            if (arc.hasConflict(runningPiece.arc3)){
                                return false;
                            }
                        }
                        break;
                    case 3:
                        for (Arc arc: imageView.listCircleThree){
                            if (arc.hasConflict(runningPiece.arc3)){
                                return false;
                            }
                        }
                        break;
                    case 4:
                        for (Arc arc: imageView.listCircleFour){
                            if (arc.hasConflict(runningPiece.arc3)){
                                return false;
                            }
                        }
                        break;
                    case 5:
                        for (Arc arc: imageView.listCircleFive){
                            if (arc.hasConflict(runningPiece.arc3)){
                                return false;
                            }
                        }
                        break;
                }
                break;

            case 4:
                switch (listID){
                    case -2:
                        for (Arc arc: imageView.listCircleOut2){
                            if (arc.hasConflict(runningPiece.arc4)){
                                return false;
                            }
                        }
                        break;
                    case -1:
                        for (Arc arc: imageView.listCircleOut1){
                            if (arc.hasConflict(runningPiece.arc4)){
                                return false;
                            }
                        }
                        break;
                    case 0:
                        for (Arc arc: imageView.listCircleZero){
                            if (arc.hasConflict(runningPiece.arc4)){
                                return false;
                            }
                        }
                        break;
                    case 1:
                        for (Arc arc: imageView.listCircleOne){
                            if (arc.hasConflict(runningPiece.arc4)){
                                return false;
                            }
                        }
                        break;
                    case 2:
                        for (Arc arc: imageView.listCircleTwo){
                            if (arc.hasConflict(runningPiece.arc4)){
                                return false;
                            }
                        }
                        break;
                    case 3:
                        for (Arc arc: imageView.listCircleThree){
                            if (arc.hasConflict(runningPiece.arc4)){
                                return false;
                            }
                        }
                        break;
                    case 4:
                        for (Arc arc: imageView.listCircleFour){
                            if (arc.hasConflict(runningPiece.arc4)){
                                return false;
                            }
                        }
                        break;
                    case 5:
                        for (Arc arc: imageView.listCircleFive){
                            if (arc.hasConflict(runningPiece.arc4)){
                                return false;
                            }
                        }
                        break;
                }
                break;

            case 5:
                switch (listID){
                    case -2:
                        for (Arc arc: imageView.listCircleOut2){
                            if (arc.hasConflict(runningPiece.arc5)){
                                return false;
                            }
                        }
                        break;
                    case -1:
                        for (Arc arc: imageView.listCircleOut1){
                            if (arc.hasConflict(runningPiece.arc5)){
                                return false;
                            }
                        }
                        break;
                    case 0:
                        for (Arc arc: imageView.listCircleZero){
                            if (arc.hasConflict(runningPiece.arc5)){
                                return false;
                            }
                        }
                        break;
                    case 1:
                        for (Arc arc: imageView.listCircleOne){
                            if (arc.hasConflict(runningPiece.arc5)){
                                return false;
                            }
                        }
                        break;
                    case 2:
                        for (Arc arc: imageView.listCircleTwo){
                            if (arc.hasConflict(runningPiece.arc5)){
                                return false;
                            }
                        }
                        break;
                    case 3:
                        for (Arc arc: imageView.listCircleThree){
                            if (arc.hasConflict(runningPiece.arc5)){
                                return false;
                            }
                        }
                        break;
                    case 4:
                        for (Arc arc: imageView.listCircleFour){
                            if (arc.hasConflict(runningPiece.arc5)){
                                return false;
                            }
                        }
                        break;
                    case 5:
                        for (Arc arc: imageView.listCircleFive){
                            if (arc.hasConflict(runningPiece.arc5)){
                                return false;
                            }
                        }
                        break;
                }
                break;
        }

        return true;
    }

    private boolean canGoLeftOrRight(Piece piece, int left){//left=-1 right=+1

        Arc arc0 = new Arc(piece.getArcs()[0].start + left, piece.getArcs()[0].size, MyImageView.COLOR_BLACK);
        Arc arc1 = new Arc(piece.getArcs()[1].start + left, piece.getArcs()[1].size, MyImageView.COLOR_BLACK);
        Arc arc2 = new Arc(piece.getArcs()[2].start + left, piece.getArcs()[2].size, MyImageView.COLOR_BLACK);
        Arc arc3 = new Arc(piece.getArcs()[3].start + left, piece.getArcs()[3].size, MyImageView.COLOR_BLACK);
        Arc arc4 = new Arc(piece.getArcs()[4].start + left, piece.getArcs()[4].size, MyImageView.COLOR_BLACK);
        Arc arc5 = new Arc(piece.getArcs()[5].start + left, piece.getArcs()[5].size, MyImageView.COLOR_BLACK);

        Piece testPiece = new Piece(arc0, arc1, arc2, arc3, arc4, arc5, piece.getPieceID(), 270);

        if (validationCounter >= 0){
            return checkIfPieceSuits(testPiece, imageView.radiusCircleRunning + 1f*imageView.strokeWidth);

        }else {
            return checkIfPieceSuits(testPiece, imageView.radiusCircleRunning) &&
                    checkIfPieceSuits(testPiece, imageView.radiusCircleRunning + 1f * imageView.strokeWidth);
        }
    }

    private boolean checkIfPieceSuits(Piece piece, float radius) {

        //Piece has entered the circle //debug//inside UIThread to avoid ConcurrentModificationException
        //Piece reached the  start of circle 5
        if (radius <= imageView.radiusCircleFour) {

            if (checkIfPasses(5, 5, piece) && checkIfPasses(4, 4, piece) && checkIfPasses(3, 3, piece) &&
                    checkIfPasses(2, 2, piece) && checkIfPasses(1, 1, piece) && checkIfPasses(0, 0, piece)) {
                return true;
            }

            //Piece reached the start of circle 4
        } else if (radius <= imageView.radiusCircleThree) {

            if (checkIfPasses(5, 4, piece) && checkIfPasses(4, 3, piece) && checkIfPasses(3, 2, piece) &&
                    checkIfPasses(2, 1, piece) && checkIfPasses(1, 0, piece) && checkIfPasses(0, -1, piece)) {
                return true;
            }

            //Piece reached the start of circle 3
        } else if (radius <= imageView.radiusCircleTwo) {

            if (checkIfPasses(5, 3, piece) && checkIfPasses(4, 2, piece) && checkIfPasses(3, 1, piece) &&
                    checkIfPasses(2, 0, piece) && checkIfPasses(1, -1, piece) && checkIfPasses(0, -2, piece)) {
                return true;
            }

            //Piece reached the start of circle 2
        } else if (radius <= imageView.radiusCircleOne) {

            if (checkIfPasses(5, 2, piece) && checkIfPasses(4, 1, piece) && checkIfPasses(3, 0, piece) &&
                    checkIfPasses(2, -1, piece) && checkIfPasses(1, -2, piece)) {
                return true;
            }

            //Piece reached the start of circle 1
        } else if (radius <= imageView.radiusCircleZero) {

            if (checkIfPasses(5, 1, piece) && checkIfPasses(4, 0, piece) && checkIfPasses(3, -1, piece) && checkIfPasses(2, -2, piece)) {
                return true;
            }

            //Piece reached the start of circle 0
        } else if (radius <= imageView.radiusCircleOut1) {

            if (checkIfPasses(5, 0, piece) && checkIfPasses(4, -1, piece) && checkIfPasses(3, -2, piece)) {
                return true;
            }

            //Piece reached the start of circle -1
        } else if (radius <= imageView.radiusCircleOut2) {

            if (checkIfPasses(5, -1, piece) && checkIfPasses(4, -2, piece)) {
                return true;
            }


            ////Piece reached the start of circle -2
        } else if (radius <= imageView.radiusCircleOut2 + 1f*imageView.strokeWidth) {

            if (checkIfPasses(5, -2, piece)) {
                return true;
            }

        }else if (radius > imageView.radiusCircleOut2 + 1f*imageView.strokeWidth) {

            return true;
        }

        return false;

    }

    private Piece getFlipPiece(int ID){

        switch (ID){
            case 0:
            case 1:
                return getPiece(8);
            case 2:
                return getPiece(2);
            case 3:
                return getPiece(14);
            case 4:
                return getPiece(16);
            case 5:
                return getPiece(17);
            case 6:
                return getPiece(13);
            case 7:
                return getPiece(9);
            case 8:
                return getPiece(1);
            case 9:
                return getPiece(7);
            case 10:
                return getPiece(11);
            case 11:
                return getPiece(10);
            case 12:
                return getPiece(15);
            case 13:
                return getPiece(6);
            case 14:
                return getPiece(3);
            case 15:
                return getPiece(12);
            case 16:
                return getPiece(4);
            case 17:
                return getPiece(5);
            case 18:
                return getPiece(19);
            case 19:
                return getPiece(18);
            case 20:
                return getPiece(21);
            case 21:
                return getPiece(20);

        }
        return null;
    }

    private Piece getPiece(int ID){

        int quarter = 3*imageView.totalspaces/4;

        Arc arc0 = new Arc (0, 0, MyImageView.COLOR_BLACK);
        Arc arc1 = new Arc (0, 0, MyImageView.COLOR_BLACK);
        Arc arc2 = new Arc (0, 0, MyImageView.COLOR_BLACK);
        Arc arc3 = new Arc (0, 0, MyImageView.COLOR_BLACK);
        Arc arc4 = new Arc (0, 0, MyImageView.COLOR_BLACK);
        Arc arc5 = new Arc (0, 0, MyImageView.COLOR_BLACK);

        if (ID < 0){
            ID = random.nextInt(22);
            //Code not to give the same piece many times
            while (lastUsedTimes > 2 && (ID == lastUsedID || ID == getFlipPiece(lastUsedID).getPieceID())){
                ID = random.nextInt(22);
                if (ID!=lastUsedID){
                    lastUsedTimes=0;
                    break;
                }
            }
            if (ID==lastUsedID || ID == getFlipPiece(lastUsedID).getPieceID()){
                lastUsedTimes++;
            }
            lastUsedID = ID;
        }

        //////////////////debug
        //ID = 0;
        //////////////////////

        switch (ID){

            case 0://2 possibilities for line
            case 1:
                arc3 = new Arc (quarter-1, 1, MyImageView.COLOR_BLUE);
                arc4 = new Arc (quarter-1, 1, MyImageView.COLOR_BLUE);
                arc5 = new Arc (quarter-1, 1, MyImageView.COLOR_BLUE);
                return new Piece(arc0, arc1, arc2, arc3, arc4, arc5, 1, 270);
            case 2:
                arc4 = new Arc (quarter-1, 2, MyImageView.COLOR_MAGENTA);
                arc5 = new Arc (quarter-1, 2, MyImageView.COLOR_MAGENTA);
                return new Piece(arc0, arc1, arc2, arc3, arc4, arc5, 2, 270);
            case 3:
                arc4 = new Arc (quarter-2, 3, MyImageView.COLOR_ORANGE);
                arc5 = new Arc (quarter-1, 1, MyImageView.COLOR_ORANGE);
                return new Piece(arc0, arc1, arc2, arc3, arc4, arc5, 3, 270);
            case 4:
                arc4 = new Arc (quarter-2, 2, MyImageView.COLOR_RED);
                arc5 = new Arc (quarter-1, 2, MyImageView.COLOR_RED);
                return new Piece(arc0, arc1, arc2, arc3, arc4, arc5, 4, 270);
            case 5:
                arc5 = new Arc (quarter-1, 2, MyImageView.COLOR_BLUE_THREE);
                return new Piece(arc0, arc1, arc2, arc3, arc4, arc5, 5, 270);
            case 6:
                arc4 = new Arc (quarter, 1, MyImageView.COLOR_GREEN_THREE);
                arc5 = new Arc (quarter-2, 3, MyImageView.COLOR_GREEN_THREE);
                return new Piece(arc0, arc1, arc2, arc3, arc4, arc5, 6, 270);
            case 7:
                arc4 = new Arc (quarter-1, 2, MyImageView.COLOR_PINK_TWO);
                arc5 = new Arc (quarter-1, 1, MyImageView.COLOR_PINK_TWO);
                return new Piece(arc0, arc1, arc2, arc3, arc4, arc5, 7, 270);
            case 8:
                arc5 = new Arc (quarter-2, 3, MyImageView.COLOR_BLUE);
                return new Piece(arc0, arc1, arc2, arc3, arc4, arc5, 8, 270);
            case 9:
                arc4 = new Arc (quarter-1, 2, MyImageView.COLOR_PINK_TWO);
                arc5 = new Arc (quarter, 1, MyImageView.COLOR_PINK_TWO);
                return new Piece(arc0, arc1, arc2, arc3, arc4, arc5, 9, 270);
            case 10:
                arc4 = new Arc (quarter-1, 1, MyImageView.COLOR_PINK);
                arc5 = new Arc (quarter-1, 2, MyImageView.COLOR_PINK);
                return new Piece(arc0, arc1, arc2, arc3, arc4, arc5, 10, 270);
            case 11:
                arc4 = new Arc (quarter, 1, MyImageView.COLOR_PINK);
                arc5 = new Arc (quarter-1, 2, MyImageView.COLOR_PINK);
                return new Piece(arc0, arc1, arc2, arc3, arc4, arc5, 11, 270);
            case 12:
                arc4 = new Arc (quarter-2, 3, MyImageView.COLOR_GREEN);
                arc5 = new Arc (quarter-2, 1, MyImageView.COLOR_GREEN);
                return new Piece(arc0, arc1, arc2, arc3, arc4, arc5, 12, 270);
            case 13:
                arc4 = new Arc (quarter-2, 1, MyImageView.COLOR_GREEN_THREE);
                arc5 = new Arc (quarter-2, 3, MyImageView.COLOR_GREEN_THREE);
                return new Piece(arc0, arc1, arc2, arc3, arc4, arc5, 13, 270);
            case 14:
                arc4 = new Arc (quarter-1, 1, MyImageView.COLOR_ORANGE);
                arc5 = new Arc (quarter-2, 3, MyImageView.COLOR_ORANGE);
                return new Piece(arc0, arc1, arc2, arc3, arc4, arc5, 14, 270);
            case 15:
                arc4 = new Arc (quarter-2, 3, MyImageView.COLOR_GREEN);
                arc5 = new Arc (quarter, 1, MyImageView.COLOR_GREEN);
                return new Piece(arc0, arc1, arc2, arc3, arc4, arc5, 15, 270);
            case 16:
                arc4 = new Arc (quarter-1, 2, MyImageView.COLOR_RED);
                arc5 = new Arc (quarter-2, 2, MyImageView.COLOR_RED);
                return new Piece(arc0, arc1, arc2, arc3, arc4, arc5, 16, 270);
            case 17:
                arc4 = new Arc (quarter-1, 1, MyImageView.COLOR_BLUE_THREE);
                arc5 = new Arc (quarter-1, 1, MyImageView.COLOR_BLUE_THREE);
                return new Piece(arc0, arc1, arc2, arc3, arc4, arc5, 17, 270);
            case 18:
                arc3 = new Arc (quarter-1, 2, MyImageView.COLOR_YELLOW_TWO);
                arc4 = new Arc (quarter-1, 1, MyImageView.COLOR_YELLOW_TWO);
                arc5 = new Arc (quarter-1, 1, MyImageView.COLOR_YELLOW_TWO);
                return new Piece(arc0, arc1, arc2, arc3, arc4, arc5, 18, 270);
            case 19:
                arc3 = new Arc (quarter-1, 2, MyImageView.COLOR_YELLOW_TWO);
                arc4 = new Arc (quarter, 1, MyImageView.COLOR_YELLOW_TWO);
                arc5 = new Arc (quarter, 1, MyImageView.COLOR_YELLOW_TWO);
                return new Piece(arc0, arc1, arc2, arc3, arc4, arc5, 19, 270);
            case 20:
                arc3 = new Arc (quarter, 1, MyImageView.COLOR_YELLOW);
                arc4 = new Arc (quarter, 1, MyImageView.COLOR_YELLOW);
                arc5 = new Arc (quarter-1, 2, MyImageView.COLOR_YELLOW);
                return new Piece(arc0, arc1, arc2, arc3, arc4, arc5, 20, 270);
            case 21:
                arc3 = new Arc (quarter-1, 1, MyImageView.COLOR_YELLOW);
                arc4 = new Arc (quarter-1, 1, MyImageView.COLOR_YELLOW);
                arc5 = new Arc (quarter-1, 2, MyImageView.COLOR_YELLOW);
                return new Piece(arc0, arc1, arc2, arc3, arc4, arc5, 21, 270);

        }

        return null;
    }

    //Distance from center of screen
    private double checkDistanceFromCenter(double y, double x){

        double distance = Math.sqrt(Math.pow(y - imageView.centerY, 2) + Math.pow(x - imageView.centerX, 2));

        return distance;
    }

    //Distance from specific point
    private double checkDistanceFromPoint(double y, double x, double y1, double x1){

        double distance = Math.sqrt(Math.pow(y - y1, 2) + Math.pow(x - x1, 2));

        return distance;
    }

    //Button methods
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //1996 = Sign In
        if (requestCode == 1996) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // The signed in account is stored in the result.
                acount = result.getSignInAccount();
                imageView.signTxt = "Sign Out";
                imageView.signColor = MyImageView.COLOR_PINK;

                //to show msg Achievement unlocked msg
                Games.getGamesClient(MainActivity.this, acount).setViewForPopups(findViewById(R.id.container_pop_up));

            } else {
                imageView.signTxt = "Sign In";
                imageView.signColor = MyImageView.COLOR_BLACK;
                showToastMsg("  Sign in error  " + resultCode);
            }
        }
    }

    //Sign in Play Services
    public void signIn() {
        //Sign in Google Play services
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 1996);
    }

    //Sign out Play services
    public void signOut() {
        //Sign in Google Play services
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // at this point, the user is signed out.
                        imageView.signTxt = "Sign in";
                        imageView.signColor = MyImageView.COLOR_BLACK;
                        acount = null;
                    }
                });

    }

    //Show Leaderboard
    public void showLeaderBoard(){

        String leaderboardId;
        switch (gameSpeed){
            case 1: leaderboardId = getString(R.string.leader_id_fast);break;
            case 2: leaderboardId = getString(R.string.leader_id_pro);break;
            default:leaderboardId = getString(R.string.leader_id_normal);
        }

        if (acount==null) {
            signIn();

        }else {
            Games.getLeaderboardsClient(this, acount)
                    .getLeaderboardIntent(leaderboardId)
                    .addOnSuccessListener(new OnSuccessListener<Intent>() {
                        @Override
                        public void onSuccess(Intent intent) {
                            try {
                                startActivityForResult(intent, 1997);
                            }catch (Exception e){
                                showToastMsg("  Leaderboard Error  ");
                            }
                        }
                    });

        }
    }

    //Set Achievements steps and unlock
    private void setAchievementsSteps(String achievementID, int steps){

        if (acount == null){
            return;
        }

        if (steps > 0){
            Games.getAchievementsClient(this, acount)
                    .setSteps(achievementID, steps);

        }else{
            Games.getAchievementsClient(this, acount)
                    .unlock(achievementID);
        }
    }

    //Show achievements
    public void showAchievements(){

        if (acount == null) {
            signIn();

        }else {
            Games.getAchievementsClient(this, acount)
                    .getAchievementsIntent()
                    .addOnSuccessListener(new OnSuccessListener<Intent>() {
                        @Override
                        public void onSuccess(Intent intent) {
                            try {
                                startActivityForResult(intent, 1998);
                            }catch (Exception e){
                                showToastMsg("  Achievements Error  ");
                            }

                        }
                    });
        }
    }

    //Firebase Analytics
    private void sendAnalytics(String msg){

        try {
            Bundle params = new Bundle();
            //params.putString("msg: ",msg);
            mFirebaseAnalytics.logEvent(msg, params);

        }catch (Exception | Error e){}
    }

    //Ads Admob
    //Ads admob Load rewarded video
    private void loadRewardedVideoAd() {
        //debug test ads
        //Test ads : ca-app-pub-3940256099942544/5224354917
        mRewardedVideoAd.loadAd("ca-app-pub-3940256099942544/5224354917",
                new AdRequest.Builder().build());
    }

    //Ads admob load interstitial
    private void loadInterstitial() {

        if (non_personalized) {
            Bundle extras = new Bundle();
            extras.putString("npa", "1");

            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice("3398579E571E41F5C60D9766CBC83390")
                    .addNetworkExtrasBundle(AdMobAdapter.class, extras)
                    .build();
            mInterstitialAd.loadAd(adRequest);

        }else{
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice("3398579E571E41F5C60D9766CBC83390")
                    .build();
            mInterstitialAd.loadAd(adRequest);
        }
    }

    //Ads admob show interstitial
    private void showInterstitial(){

        if (System.currentTimeMillis()-lastAdShownTime > adInterval){
            if (mInterstitialAd.isLoaded()) {
                mInterstitialAd.show();
                lastAdShownTime = System.currentTimeMillis();
            }
        }
    }

    //Show toast messages
    private void showToastMsg(String msg){

        Toast toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        View view = toast.getView();
        view.setBackgroundColor(Color.BLACK);
        TextView text = (TextView) view.findViewById(android.R.id.message);
        text.setBackgroundColor(Color.BLACK);
        toast.show();
    }

    //Load the sounds
    private void loadSounds(){
        try {
            soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
            soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
                @Override
                public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                    soundLoaded = true;
                }
            });

            rotateSound = soundPool.load(this, R.raw.rotate, 1);
            pieceSuitsSound = soundPool.load(this, R.raw.piece, 1);
            lineSound = soundPool.load(this, R.raw.line, 1);
            loseSound = soundPool.load(this, R.raw.lose, 1);
            changeSound = soundPool.load(this, R.raw.change, 1);

        }catch (Exception | Error e){}
    }

    //Play sounds
    private int playSound(int soundID){

        if (!soundEnabled || !soundLoaded || soundPool==null){
            return -1;
        }
        return soundPool.play(soundID, 0.50f, 0.50f, 1, 0, 1f);

    }

    //Debug background music
    /*private void playBackgroundSong(){

        if (soundEnabled) {
            if (mediaPlayer!=null && !mediaPlayer.isPlaying()){
                mediaPlayer.start();

            }else {
                mediaPlayer = MediaPlayer.create(this, R.raw.background);
                mediaPlayer.setVolume(0.5f, 0.5f);
                mediaPlayer.setLooping(true);
                mediaPlayer.start();
            }
        }
    }

    private void stopBackgroundSong(){

        if (mediaPlayer!=null && mediaPlayer.isPlaying()){
            mediaPlayer.pause();
        }
    }*/
    //////////////debug

    //GDPR Privacy
    private void checkGDPR(){

        ConsentInformation consentInformation = ConsentInformation.getInstance(this);

        //////////////debug//////////////////
        //consentInformation.addTestDevice("3398579E571E41F5C60D9766CBC83390");
        //consentInformation.setDebugGeography(DebugGeography.DEBUG_GEOGRAPHY_EEA);
        //consentInformation.setDebugGeography(DebugGeography.DEBUG_GEOGRAPHY_NOT_EEA);
        ///////////////////////debug//////////////

        String[] publisherIds = {"pub-8020361952935930"};//debug
        consentInformation.requestConsentInfoUpdate(publisherIds, new ConsentInfoUpdateListener() {
            @Override
            public void onConsentInfoUpdated(ConsentStatus consentStatus) {

                if (consentStatus == ConsentStatus.UNKNOWN){
                    //Consent is unknown (normally first time)//Start GDPR Activity
                    if (ConsentInformation.getInstance(MainActivity.this).isRequestLocationInEeaOrUnknown()){

                        MainActivity.this.startActivity(new Intent(MainActivity.this, GDPRActivity.class));
                    }

                }else if (consentStatus == ConsentStatus.NON_PERSONALIZED){
                    MainActivity.non_personalized = true;

                }else if (consentStatus == ConsentStatus.PERSONALIZED){
                    MainActivity.non_personalized = false;
                }
            }

            @Override
            public void onFailedToUpdateConsentInfo(String errorDescription) {

            }
        });
    }

    //Show privacy policy on web
    public void showPrivacy(){

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

    //Settings Save and import
    private void saveSettings(){

        SharedPreferences.Editor editor = getSharedPreferences("settings", MODE_PRIVATE).edit();
        editor.putInt("create", onCreateTimes);
        editor.putInt("games", gamesPlayed);
        editor.putInt("best", bestNormal);
        editor.putInt("bestfast", bestFast);
        editor.putInt("bestpro", bestPro);
        editor.putInt("bestlines", bestRings);
        editor.putInt("bestlinesfast", bestRingsFast);
        editor.putInt("bestlinespro", bestRingsPro);
        editor.putInt("lines", totalRings);
        editor.putInt("doubles", totalDoubles);
        editor.putInt("triples", totalTriples);
        editor.putBoolean("sound", soundEnabled);
        editor.putInt("speed", gameSpeed);
        editor.putInt("tutorial",tutorialCounter);
        editor.apply();

    }

    //Import Settings
    private void importSettings(){

        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        onCreateTimes = prefs.getInt("create", 0);
        gamesPlayed = prefs.getInt("games", 0);
        bestNormal = prefs.getInt("best", 0);
        bestFast = prefs.getInt("bestfast", 0);
        bestPro = prefs.getInt("bestpro", 0);
        bestRings = prefs.getInt("bestlines", 0);
        bestRingsFast = prefs.getInt("bestlinesfast", 0);
        bestRingsPro = prefs.getInt("bestlinespro", 0);
        totalRings = prefs.getInt("lines", 0);
        totalDoubles = prefs.getInt("doubles", 0);
        totalTriples = prefs.getInt("triples", 0);
        soundEnabled = prefs.getBoolean("sound", true);
        gameSpeed = prefs.getInt("speed", 0);
        tutorialCounter = prefs.getInt("tutorial", 0);
        if (tutorialCounter < 10){//In case user exited app while showing tutorial
            tutorialCounter = 0;
        }
    }
}
