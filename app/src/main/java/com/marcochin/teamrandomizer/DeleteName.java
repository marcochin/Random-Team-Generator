package com.marcochin.teamrandomizer;

import java.util.ArrayList;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class DeleteName extends Activity implements OnCheckedChangeListener, OnClickListener{
	private ArrayList<String> playerList;
	private ArrayList<UpdateObj> updateList;
	private CheckBox[] ch;
	private LinearLayout llayout;
	private Button selectAll;
	private TextView tvTotalPlayers;
    private AdView avDelNameAd;

    @SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE); //get rid of action bar at top
		setContentView(R.layout.deletename);
		llayout = (LinearLayout)findViewById(R.id.layout_checkBoxes);
		
		playerList = getIntent().getStringArrayListExtra("playerList");
		
		if(getIntent().getSerializableExtra("updateList") != null){ //may return null
			updateList = (ArrayList<UpdateObj>)getIntent().getSerializableExtra("updateList");
		}
		
		ch = new CheckBox[playerList.size()];

		initializeViews();	
	}
	
	@Override
	protected void onResume() {
		super.onResume();

        if(avDelNameAd !=null)
            avDelNameAd.resume();
		//make sure phone has google play services installed
		/*int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
		if(resultCode != ConnectionResult.SUCCESS){
			GooglePlayServicesUtil.getErrorDialog(resultCode, this, 1);
		}*/
		
	}

    @Override
    protected void onPause() {
        if(avDelNameAd !=null)
            avDelNameAd.pause();

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if(avDelNameAd !=null) {
            avDelNameAd.removeAllViews();
            avDelNameAd.destroy();
        }

        super.onDestroy();
    }

    public void initializeViews(){
		for (int i = 0; i < playerList.size(); i++) 
		{
			    ch[i] = new CheckBox(this);
			    ch[i].setOnCheckedChangeListener(this);
			    ch[i].setId(i);
			    ch[i].setText(playerList.get(i));
			    ch[i].setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.rtg_text_size));
			    llayout.addView(ch[i]);
		}
		tvTotalPlayers = (TextView)findViewById(R.id.tvDelNameTotalPlayers);
		TextView tvCurrentPreset = (TextView)findViewById(R.id.tvDelNameCurrentPreset);
		selectAll = (Button)findViewById(R.id.bSelectAll);
		Button delete = (Button) findViewById(R.id.bDelete);
		
		tvTotalPlayers.setText(Integer.toString(playerList.size()));
		tvCurrentPreset.setText(getIntent().getStringExtra("currentPreset"));
		selectAll.setOnClickListener(this);
		delete.setOnClickListener(this);
		
		//add ad!
	    // Look up the AdView as a resource and load a request.
	    avDelNameAd = (AdView) this.findViewById(R.id.avDelNameAd);
	    AdRequest adRequest = new AdRequest.Builder().build();
	    avDelNameAd.loadAd(adRequest);
	}
	
	@Override
	public void onCheckedChanged(CompoundButton view, boolean isChecked) {
        for(CheckBox cb: ch){
            //if even one checkbox is unchecked set it back to "Select All"
            if(!cb.isChecked()) {
                selectAll.setText("Select All");
                return;
            }
        }
        selectAll.setText("Deselect All");
	}

	@Override
	public void onClick(View v) {

		switch(v.getId()){
		case R.id.bSelectAll:
			if(selectAll.getText().toString().equals("Select All")){
				for(CheckBox c : ch){
					c.setChecked(true);
				}
				selectAll.setText("Deselect All"); //if select all change to deselect all
				
			} else if(selectAll.getText().toString().equals("Deselect All")){
				for(CheckBox c : ch){
					c.setChecked(false);
				}
				selectAll.setText("Select All"); //if deselect all change to select all
			}
			break;
		case R.id.bDelete:
			int yGravity = 270;
			int gravityType = Gravity.TOP;
			Boolean fatalityBool = false;
			for(CheckBox c : ch){
				if(c.isChecked() && c.getVisibility() == CheckBox.VISIBLE){
					playerList.remove(c.getText().toString());
					tvTotalPlayers.setText(Integer.toString(playerList.size())); //update total
					
					if(updateList!=null){
						updateList.add(new UpdateObj("delete", c.getText().toString()));//add to updatelist if updatelist was passed in to delete activity
					}		
					
					llayout.removeView(c); //remove checkbox from view
					c.setVisibility(View.GONE);
					
					if(!fatalityBool){
						fatalityBool = true;
					}
				}
			}
			
			if(fatalityBool){
				Toast t = Toast.makeText(getApplicationContext(), "Name deleted.", Toast.LENGTH_SHORT);
				t.setGravity(gravityType, 0, yGravity);
				t.show();
				
				/*Thread thread = new Thread(new FatalitySound());
				thread.start(); //plays fatality sound	*/		
			}
			
			if(llayout.getChildCount() == 0){
				returnDataToMain();
				finish();
			}
			break;
		}
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		returnDataToMain();
		super.onBackPressed(); //super does what the default back button does
	}
	
	public void returnDataToMain(){
		Intent returnToMain = new Intent();
		returnToMain.putStringArrayListExtra("playerList", playerList);
		if(updateList != null){
			returnToMain.putExtra("updateList", updateList);
		}
		//returns information back to previous class to. From startActivityForResult to onActivityResult
		setResult(RESULT_OK, returnToMain); //result code and intent
	}
	
	/*public class FatalitySound implements Runnable{
		SoundPool sp;
		int fatality = 0;
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				sp = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
				fatality = sp.load(getApplicationContext(), R.raw.fatality, 1); // load this sound file
				
				if (fatality != 0) { //FATALITY!
					sp.play(fatality, 5, 1, 0, 0, 1); // hover over play for documentation
				}
				
				Thread.sleep(2500);
				sp.release();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}*/
}
