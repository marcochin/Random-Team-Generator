package com.marcochin.teamrandomizer;

import java.util.ArrayList;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import android.app.Activity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ViewPreset extends Activity implements OnClickListener{

    private AdView avViewAd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE); //get rid of action bar at top
		setContentView(R.layout.viewpreset);
		initializeViews();
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();

        if(avViewAd != null)
            avViewAd.resume();

		//make sure phone has google play services installed
		/*int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
		if(resultCode != ConnectionResult.SUCCESS){
			GooglePlayServicesUtil.getErrorDialog(resultCode, this, 1);
		}*/
		
	}

    @Override
    protected void onPause() {
        if(avViewAd != null)
            avViewAd.pause();

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if(avViewAd != null) {
            avViewAd.removeAllViews();
            avViewAd.destroy();
        }

        super.onDestroy();
    }

    private void initializeViews(){
		ArrayList<String> playerNames;
		
		TextView tvViewedPreset = (TextView)findViewById(R.id.tvViewedPreset);
		TextView tvtotalPlayers = (TextView)findViewById(R.id.tvViewTotalPlayers);
		LinearLayout llViewPreset = (LinearLayout)findViewById(R.id.llViewPreset);
		Button bLoadPreset = (Button)findViewById(R.id.bViewPresetLoad);
		
		String presetName = getIntent().getStringExtra("presetName");
		tvViewedPreset.setText(presetName);
		
		DbHelper db = new DbHelper(this);
		db.open();
		playerNames = db.getPlayersByPreset(presetName);
		db.close();
		
		tvtotalPlayers.setText(Integer.toString(playerNames.size()));
		
		if(playerNames.size()==0){
			TextView tvViewName = new TextView(this);
			tvViewName.setText("Empty");
			tvViewName.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.rtg_text_size));
			llViewPreset.addView(tvViewName);
		} else{
			for(int i=0; i<playerNames.size(); i++){
				TextView tvViewName = new TextView(this);
				tvViewName.setText((i + 1) + ". " + playerNames.get(i));
				tvViewName.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.rtg_text_size));
				llViewPreset.addView(tvViewName);
			}
		}
		
		bLoadPreset.setOnClickListener(this);
		
		//add ad!
	    // Look up the AdView as a resource and load a request.
	    AdView avViewAd = (AdView) this.findViewById(R.id.avViewAd);
	    AdRequest adRequest = new AdRequest.Builder().build();
	    avViewAd.loadAd(adRequest);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v.getId() == R.id.bViewPresetLoad){
			setResult(RESULT_OK);
			finish();
		}
	}
}
