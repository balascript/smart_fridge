package edu.scu.smartfridge;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

import az.plainpie.PieView;
import az.plainpie.animation.PieAngleAnimation;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
    private static final int MY_DATA_CHECK_CODE = 1011;
    private TextToSpeech tts;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };
    RequestQueue queue;
    TextView Status;
    Button Refill;
    PieView pieView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);
        hide();
        Status = (TextView)this.findViewById(R.id.percentage);
        Refill= (Button) this.findViewById(R.id.refill_btn);
        Status.setText("Loading...");

        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        pieView = (PieView) findViewById(R.id.pieView);
        pieView.setPercentageBackgroundColor(Color.argb(255,109, 165, 255));
        pieView.setMainBackgroundColor(Color.argb(255,218, 218, 218));

        queue = Volley.newRequestQueue(this);

        Refill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("https://www.amazon.com/Dairy-Pure-Reduced-Pasteurized-Gallon/dp/B00CIJAE0C/ref=sr_1_1_f_f_it?s=amazonfresh&ie=UTF8&qid=1481276542&sr=1-1&ppw=fresh&keywords=milk");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });


        String token = FirebaseInstanceId.getInstance().getToken();
        // Log and toast
        String msg = getString(R.string.msg_token_fmt, token);
        Log.d("FCM TAG", msg);



    }
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                // success, create the TTS instance
                tts = new TextToSpeech(MainActivity.this, this);
            } else {
                // missing data, install it
                Intent installIntent = new Intent();
                installIntent.setAction(
                        TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installIntent);
            }
        }
    }

    private void updateMilkStatus() {
        pieView.setVisibility(View.VISIBLE);
        String url = "http://api.humandroid.us/milk-api/getMilkStatus";

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.v("response received",response.toString());
                        try {
                            Double level= response.getJSONObject("data").getDouble("liters");
                            Double capacity= response.getJSONObject("data").getDouble("capacity");

                            if(level<0){
                                //the bottle is taken
                                pieView.setPercentage(0);
                                pieView.setMainBackgroundColor(Color.argb(255,244, 86, 66));
                                PieAngleAnimation animation = new PieAngleAnimation(pieView);
                                animation.setDuration(5000); //This is the duration of the animation in millis
                                pieView.startAnimation(animation);
                                Status.setText("No Bottle");
                                Refill.setBackgroundResource(R.drawable.buttonshape_red);
                                speakOut("There is not Bottle in the Fridge. You may want to order one.");

                            }
                            else if(level==0){
                                //placed an empty bottle
                                pieView.setPercentage(0);
                                pieView.setMainBackgroundColor(Color.argb(255,244, 86, 66));
                                PieAngleAnimation animation = new PieAngleAnimation(pieView);
                                animation.setDuration(5000); //This is the duration of the animation in millis
                                pieView.startAnimation(animation);
                                Status.setText("Empty !!!");
                                Refill.setBackgroundResource(R.drawable.buttonshape_red);
                                speakOut("There is just an empty bottle I think. You may want to order one.");
                            }
                            else{
                                //properlevel
                                pieView.setMainBackgroundColor(Color.argb(255,218, 218, 218));
                                double current_level= level/capacity;
                                current_level*=100;
                                if(current_level<=25){
                                    Refill.setBackgroundResource(R.drawable.buttonshape_red);
                                    pieView.setPercentageBackgroundColor(Color.argb(255,244, 158, 66));
                                    speakOut("You are running low on Milk. You may want to order one soon.");
                                }
                                else{
                                    Refill.setBackgroundResource(R.drawable.buttonshape);
                                    pieView.setPercentageBackgroundColor(Color.argb(255,109, 165, 255));
                                    speakOut("The current level is "+ (int)current_level +" percentage");
                                }

                                pieView.setPercentage((float) current_level);
                                PieAngleAnimation animation = new PieAngleAnimation(pieView);
                                animation.setDuration(5000); //This is the duration of the animation in millis
                                pieView.startAnimation(animation);
                                Status.setText("");


                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {


                    }
                });
        queue.add(jsObjRequest);
    }
    private void speakOut(String s) {
        if(tts!=null)
        tts.speak(s, TextToSpeech.QUEUE_FLUSH, null,"speak_milk_status");
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }
    @Override
    protected void onPause() {
        // TODO Auto-generated method stub

        if(tts != null){

            tts.stop();
            tts.shutdown();
        }
        super.onPause();
    }
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }



    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }



    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    @Override
    public void onInit(int i) {
        tts.setLanguage(Locale.US);
        updateMilkStatus();
    }
}
