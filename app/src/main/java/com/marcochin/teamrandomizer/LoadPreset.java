package com.marcochin.teamrandomizer;

import java.util.ArrayList;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.marcochin.teamrandomizer.dialogs.LoadSaveChangesDialog;
import com.marcochin.teamrandomizer.dialogs.LoadSavePresetAsDialog;

public class LoadPreset extends Activity implements OnClickListener{
	private TextView tvEmptyPreset, tvCurrentPreset;
	private RadioButton[] radio;
	private RadioGroup rgPresetGroup;
	private int yGravity = 270;
	private int gravityType = Gravity.TOP;
	private int checkedRadio;
	private ArrayList<String> playerList;
	private boolean changesMade;

	private AdView avLoadAd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE); //get rid of action bar at top
		setContentView(R.layout.loadpreset);
		initializeViews();
		loadPresets();
		
		tvCurrentPreset.setText(getIntent().getStringExtra("currentPreset"));
		playerList = getIntent().getStringArrayListExtra("playerList");
		changesMade = getIntent().getBooleanExtra("changesMade", false);
	}
	
	@Override
	protected void onResume() {
		super.onResume();

        if(avLoadAd != null)
            avLoadAd.resume();

		//make sure phone has google play services installed
		/*int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
		if(resultCode != ConnectionResult.SUCCESS){
			GooglePlayServicesUtil.getErrorDialog(resultCode, this, 1);
		}*/
		
	}

    @Override
    protected void onPause() {
        if(avLoadAd != null)
            avLoadAd.pause();

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if(avLoadAd != null) {
            avLoadAd.removeAllViews();
            avLoadAd.destroy();
        }

        super.onDestroy();
    }

    public void initializeViews(){
		Button bLoadPreset = (Button)findViewById(R.id.bLoadPreset);
		Button bViewPreset = (Button)findViewById(R.id.bViewPreset);
		tvEmptyPreset = (TextView)findViewById(R.id.tvEmptyPresets);
		tvCurrentPreset = (TextView)findViewById(R.id.tvLoadCurrrentPreset);
		rgPresetGroup = (RadioGroup)findViewById(R.id.rgLoadPreset);
		
		bLoadPreset.setOnClickListener(this);
		bViewPreset.setOnClickListener(this);
		
		//add ad!
	    // Look up the AdView as a resource and load a request.
	    avLoadAd = (AdView) this.findViewById(R.id.avLoadAd);
	    AdRequest adRequest = new AdRequest.Builder().build();
	    avLoadAd.loadAd(adRequest);
		
	}
	
	public void loadPresets(){
		String[] presetNames;
		DbHelper db = new DbHelper(this);
		db.open(); //open db
		presetNames = db.getPresetNames(); //execute cmd
		if(presetNames.length == 0){
			tvEmptyPreset.setVisibility(View.VISIBLE);
			return;
		}
		radio = new RadioButton[presetNames.length];
		for(int i=0; i<radio.length; i++){
			radio[i] = new RadioButton(this);
			radio[i].setText(presetNames[i]);
			radio[i].setId(i);
			radio[i].setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.rtg_text_size));
			rgPresetGroup.addView(radio[i]);
		}
		db.close(); //close db
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub	
		checkedRadio = rgPresetGroup.getCheckedRadioButtonId();
		
		//return if nothing is checked
		if(checkedRadio < 0){
			showPickAPresetToast();
			return;
		}
					
		switch(v.getId()){
		case R.id.bLoadPreset:
			/*if(tvCurrentPreset.getText().toString().equals(radio[checkedRadio].getText().toString())){
				finish();//finish if preset is already loaded
			}*/
			if(changesMade){
				FragmentManager manager = getFragmentManager();
				LoadSaveChangesDialog loadscd = new LoadSaveChangesDialog();
				loadscd.show(manager, "newscd"); //adds fragment to the manager
			} else{
				loadPresetInMain();
			}
			break;
			
		case R.id.bViewPreset:
			String presetName = radio[checkedRadio].getText().toString();
			
			Intent data = new Intent(this, ViewPreset.class);
			data.putExtra("presetName", presetName); //add preset name to intent
			startActivityForResult(data, 2);

			break;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		
		if(resultCode == RESULT_OK){
			switch(requestCode){
			case 2:
				if(changesMade){
					FragmentManager manager = getFragmentManager();
					LoadSaveChangesDialog loadscd = new LoadSaveChangesDialog();
					loadscd.show(manager, "newscd"); //adds fragment to the manager
				} else{
					loadPresetInMain();
				}
				break;
			}
		}
	}
	
	public void loadPresetInMain(){
		String presetName = radio[checkedRadio].getText().toString();
		
		Intent data = new Intent();		
		data.putExtra("presetName", presetName); //add preset name to intent
		
		DbHelper db = new DbHelper(this);
		db.open();
		data.putStringArrayListExtra("playerNames", db.getPlayersByPreset(presetName)); //add ArrList of players names to intent
		db.close();
		
		setResult(RESULT_OK, data); //set the result which is preset name and players			
		
		Toast t = Toast.makeText(getApplicationContext(), "Preset loaded.", Toast.LENGTH_SHORT);
		t.setGravity(gravityType, 0, yGravity);
		t.show();
		finish();
		
	}
	
	public void savePreset(){
		if(getIntent().getBooleanExtra("presetLoaded", false)){
			DbHelper db = new DbHelper(this);
			db.open();
			db.updatePreset(tvCurrentPreset.getText().toString(), (ArrayList<UpdateObj>)getIntent().getSerializableExtra("updateList"));
			db.close();
			Toast t = Toast.makeText(getApplicationContext(), "Preset saved.", Toast.LENGTH_SHORT);
			t.setGravity(gravityType, 0, yGravity);
			t.show();
			
			loadPresetInMain(); //load preset after saving current preset
		} else {
			FragmentManager manager = getFragmentManager();
			LoadSavePresetAsDialog loadSavePreset = new LoadSavePresetAsDialog();
			loadSavePreset.show(manager, "loadSavePreset"); //adds fragment to the manager
		}
	}
	
	public ArrayList<String> getPlayersArrList(){
		return playerList;
	}
	
	public String getCurrentPreset(){
		return tvCurrentPreset.getText().toString();
	}
	
	private void showPickAPresetToast(){
		Toast t = Toast.makeText(getApplicationContext(), "Please pick a preset.", Toast.LENGTH_SHORT);
		t.setGravity(gravityType, 0, yGravity);
		t.show();
	}
}
