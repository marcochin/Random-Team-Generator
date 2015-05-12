package com.marcochin.teamrandomizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import android.app.Activity;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class Randomize extends Activity{
    private final String ERROR_TAG = "Randomize";

	private ArrayList<String>playerList;
	private LinearLayout llTeamLeft, llTeamRight;
	//private SoundPool sp;
	//private int roundonefight = 0;
	private int numberOfTeams;
    private Button bRandomizeAgain;

    private AdView avRandomizeAd;
    private Thread randomizeThread;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); //get rid of action bar at top
		
		setContentView(R.layout.randomize);
		playerList = getIntent().getStringArrayListExtra("playerArrList");
		numberOfTeams = getIntent().getIntExtra("numberOfTeams", 2);
		initializeViews();
		
		/*sp = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
		roundonefight = sp.load(this, R.raw.round_one_fight, 1); // load this sound file
		if (roundonefight != 0) { //FINISH HIM!
			sp.play(roundonefight, 1, 1, 0, 0, 1); // hover over play for documentation
		}*/

        startRandomizeThread();
	}
	
	@Override
	protected void onResume() {

		super.onResume();

        if(avRandomizeAd != null)
            avRandomizeAd.resume();

		//make sure phone has google play services installed
		/*int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
		if(resultCode != ConnectionResult.SUCCESS){
			GooglePlayServicesUtil.getErrorDialog(resultCode, this, 1);
		}*/
		
	}
	
	@Override
	protected void onPause() {

        if(avRandomizeAd != null)
            avRandomizeAd.pause();

		super.onPause();
		
		/*if (sp != null) {
            sp.release();
            sp = null;
        }*/
	}

    @Override
    protected void onStop() {

        if(randomizeThread != null)
            randomizeThread.interrupt();

        super.onStop();
    }

    @Override
    protected void onDestroy() {

        if(avRandomizeAd != null) {
            avRandomizeAd.removeAllViews();
            avRandomizeAd.destroy();
        }

        super.onDestroy();
    }

    public void initializeViews(){
		llTeamLeft = (LinearLayout)findViewById(R.id.llTeamLeft);
		llTeamRight = (LinearLayout)findViewById(R.id.llTeamRight);
		((TextView)findViewById(R.id.tvRandomizeCurrentPreset)).setText(getIntent().getStringExtra("currentPreset"));
		((TextView)findViewById(R.id.tvRandomizeTotalPlayers)).setText(Integer.toString(playerList.size()));

        bRandomizeAgain = (Button) findViewById(R.id.bRandomizeAgain);
        bRandomizeAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRandomizeThread();
            }
        });

		//add ad!
	    // Look up the AdView as a resource and load a request.
	    avRandomizeAd = (AdView) this.findViewById(R.id.avRandomizeAd);
	    AdRequest adRequest = new AdRequest.Builder().build();
	    avRandomizeAd.loadAd(adRequest);
	}
	
	public void randomize(){
		int team = 0;
		int playerNumCounter = 1;
		LinearLayout[] llTeam = new LinearLayout[numberOfTeams];
		
		for(int i=0; i<llTeam.length; i++){
			llTeam[i] = new LinearLayout(this);
			llTeam[i].setId(i);
			llTeam[i].setOrientation(LinearLayout.VERTICAL);
			
			TextView tvTeamNum = new TextView(this);
			tvTeamNum.setText(Html.fromHtml("<u><b>Team " + (i+1) +"</b></u>"));
			tvTeamNum.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.rtg_text_size));
			
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
			lp.gravity = Gravity.CENTER_HORIZONTAL;
			
			if(i > 1){ //if not team 1 or team 2 add spacing in between teams
				lp.topMargin = 50;
			}
			
			tvTeamNum.setLayoutParams(lp);//set the paramters to the textview
			
			//adding the linearLayouts to the left scroll view and right scroll views
			if (team == 0){
				llTeamLeft.addView(llTeam[i]);
				llTeam[i].addView(tvTeamNum);
				team = 1;
			} else if(team == 1){
				llTeamRight.addView(llTeam[i]);
				llTeam[i].addView(tvTeamNum);
				team = 0;
			}
		}
		
		team = 0;
		Collections.shuffle(playerList); //randomize arraylist
		
		for(int i=0; i<playerList.size(); i++){
			String playerName = playerList.get(i);
			TextView tvName = new TextView(this);
			tvName.setText((Integer.toString(playerNumCounter) + ". " + playerName).replaceFirst("\\s", "\u00A0"));
			tvName.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.rtg_text_size));
			tvName.setSingleLine();
			tvName.setEllipsize(TextUtils.TruncateAt.END);
			
			if(numberOfTeams > 3 && i == playerList.size()-1 && (team+1)%2.0f != 0){ //if team>3, is last name in list, and need to land on odd team		
				llTeam[numberOfTeams-1].addView(tvName); //if a name makes the teams unaligned add it to the last one
			} else{
				llTeam[team].addView(tvName);
			}
			team++;
			
			if(team == numberOfTeams){
				team = 0;
                //playerNumCounter is the number in "1. Catman 2. Dogfu"
				playerNumCounter++;
			}		
		}
	}

    private void startRandomizeThread(){
        bRandomizeAgain.setVisibility(View.INVISIBLE);

        randomizeThread = new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (Randomize.this) {
                    try {
                        Random rand = new Random();

                        for (int i = 0; i < rand.nextInt(3) + 5; i++) {
                            if (i != 0) {
                                Thread.sleep(330);
                            }

                            //If I use post(), it somehow doesn't run?
                            Randomize.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    llTeamLeft.removeAllViews();
                                    llTeamRight.removeAllViews();
                                    randomize();
                                }
                            });
                        }

                    } catch (InterruptedException e) {
                        Log.e(ERROR_TAG, e.getMessage());
                        Log.e(ERROR_TAG, Log.getStackTraceString(e));
                    } finally {
                        bRandomizeAgain.post(new Runnable() {
                            @Override
                            public void run() {
                                bRandomizeAgain.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                }
            }
        });

        randomizeThread.start();
    }
}
