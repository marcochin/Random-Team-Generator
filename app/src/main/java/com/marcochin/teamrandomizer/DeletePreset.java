package com.marcochin.teamrandomizer;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
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

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.marcochin.teamrandomizer.dialogs.DeleteCurrentPresetDialog;

public class DeletePreset extends Activity implements OnCheckedChangeListener, OnClickListener{
	private String currentPreset;
	private CheckBox[] ch;
	private CheckBox cbCurrPreset;
	private LinearLayout llayout;
	private Button selectAll;
	private TextView tvNoPresets;
	private DbHelper db;
	private Intent i;
	private TextView tvCurrPreset;
	private int yGravity = 270;
	private int gravityType = Gravity.TOP;

    private AdView avDelPresetAd;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); //get rid of action bar at top
		setContentView(R.layout.deletepreset);
					
		initializeViews();
		loadPresets();
		
		i = new Intent();
		if(getIntent().getStringExtra("currentPreset") != null){
			currentPreset = getIntent().getStringExtra("currentPreset");
			tvCurrPreset.setText(currentPreset);
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();

        if(avDelPresetAd != null)
            avDelPresetAd.resume();

		//make sure phone has google play services installed
		/*int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
		if(resultCode != ConnectionResult.SUCCESS){
			GooglePlayServicesUtil.getErrorDialog(resultCode, this, 1);
		}*/
		
	}

    @Override
    protected void onPause() {
        if(avDelPresetAd != null)
            avDelPresetAd.pause();

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if(avDelPresetAd != null) {
            avDelPresetAd.removeAllViews();
            avDelPresetAd.destroy();
        }

        super.onDestroy();
    }

    private void initializeViews(){
		llayout = (LinearLayout)findViewById(R.id.layout_preset_checkBoxes);
		selectAll = (Button)findViewById(R.id.b_preset_SelectAll);
		Button delete = (Button) findViewById(R.id.b_preset_Delete);
		tvCurrPreset = (TextView) findViewById(R.id.tv_preset_current_preset);
		tvNoPresets = (TextView) findViewById(R.id.tvNoPresets);
		
		selectAll.setOnClickListener(this);
		delete.setOnClickListener(this);
		
		//add ad!
	    // Look up the AdView as a resource and load a request.
	    avDelPresetAd = (AdView) this.findViewById(R.id.avDelPresetAd);
	    AdRequest adRequest = new AdRequest.Builder().build();
	    avDelPresetAd.loadAd(adRequest);
	}
	
	private void loadPresets(){
        String[] presetNames;

		db = new DbHelper(this);
		db.open();
		presetNames = db.getPresetNames();
		db.close();

		if(presetNames.length == 0){
			tvNoPresets.setVisibility(View.VISIBLE);
		}
		ch = new CheckBox[presetNames.length];	
		
		for (int i = 0; i < presetNames.length; i++) 
		{
			    ch[i] = new CheckBox(this);
			    ch[i].setOnCheckedChangeListener(this);
			    ch[i].setId(i);
			    ch[i].setText(presetNames[i]);
			    ch[i].setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.rtg_text_size));
			    llayout.addView(ch[i]);
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton view, boolean arg1) {
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
		boolean fatalityBool = false;
		
		switch(v.getId()){
		case R.id.b_preset_SelectAll:
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
		case R.id.b_preset_Delete:	
			db.open();
			db.dbBeginTransaction();
			for(CheckBox c : ch){
				if(c.isChecked() && c.getVisibility() == CheckBox.VISIBLE){
					if(c.getText().toString().equals(currentPreset) && currentPreset != null){
						cbCurrPreset = c;
						FragmentManager manager = getFragmentManager();
						DeleteCurrentPresetDialog delcurr = new DeleteCurrentPresetDialog();
						delcurr.show(manager, "delcurr"); //adds fragment to the manager
					} else{	
						db.deletePreset(c.getText().toString());
						llayout.removeView(c); //remove checkbox from view
						c.setVisibility(View.GONE);
						
						if(!fatalityBool){
							fatalityBool = true;
						}
					}
				}
			}
			
			if(fatalityBool){
				Toast t = Toast.makeText(getApplicationContext(), "Preset deleted.", Toast.LENGTH_SHORT);
				t.setGravity(gravityType, 0, yGravity);
				t.show();
			}
			
			db.dbSetTransactionSuccessful();
			db.dbEndTransaction();
			db.close();
			
			if(llayout.getChildCount() == 0){
				returnDataToMain();
				finish();
			}
			break;
		}
	}
	
	@Override
	public void onBackPressed() {
		returnDataToMain();
		super.onBackPressed(); //super does what the default back button does
	}
	
	public void returnDataToMain(){
		setResult(RESULT_OK, i); //result code and intent
	}
	
	public void setDeleteCurrentPresetExtraTrue(){ //will be called by dialog fragment if he deletes current preset
		i.putExtra("currPresetDeleted", true);
	}
	
	public void deleteCurrentPreset(){
		DbHelper dbh = new DbHelper(this);
		dbh.open();
		dbh.deletePreset(cbCurrPreset.getText().toString());
		dbh.close();
		
		llayout.removeView(cbCurrPreset); //remove checkbox from view
		cbCurrPreset.setVisibility(View.GONE); //visibility to gone
		
		Toast t = Toast.makeText(getApplicationContext(), "Preset deleted.", Toast.LENGTH_SHORT);
		t.setGravity(gravityType, 0, yGravity);
		t.show();
		
		tvCurrPreset.setText("None"); //reset current to none
		setDeleteCurrentPresetExtraTrue();
		
		if(llayout.getChildCount() == 1 && llayout.getChildAt(0).getId() == R.id.tvNoPresets){
			returnDataToMain();
			finish();
		}
	}
}
