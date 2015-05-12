package com.marcochin.teamrandomizer.dialogs;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.marcochin.teamrandomizer.DbHelper;
import com.marcochin.teamrandomizer.R;
import com.marcochin.teamrandomizer.TeamRandomizer;

public class SavePresetAsDialog extends DialogFragment implements OnClickListener{

	private EditText etPresetName;
	TeamRandomizer tr;
	@Override
	public void onAttach(Activity activity) { //the activity holding the fragment will be passed in here
		// TODO Auto-generated method stub
		super.onAttach(activity);
		tr = (TeamRandomizer) activity;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		getDialog().getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		getDialog().setTitle("Save Preset As...");
		
		View view = inflater.inflate(R.layout.saveasdialog, null);
		Button bCancelSave = (Button) view.findViewById(R.id.bCancelSave);
		Button bSavePreset = (Button) view.findViewById(R.id.bSavePresetAs);
		etPresetName = (EditText) view.findViewById(R.id.etSavePreset);
		
		bCancelSave.setOnClickListener(this);
		bSavePreset.setOnClickListener(this);
		return view;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int yGravity = 270;
		int gravityType = Gravity.TOP;
		String presetName;
		
		if(v.getId() == R.id.bCancelSave){
			
			dismiss();
			
		} else if(v.getId() == R.id.bSavePresetAs){
			
			presetName = etPresetName.getText().toString(); //trim presetname
			
			DbHelper db = new DbHelper(getActivity());
			db.open(); //open, create, close
			//creates the entry in the conditional statement, passes preset name and the string arraylist
			
			try{
				if(presetName.isEmpty()){
					Toast t = Toast.makeText(getActivity(), "Please enter a name!", Toast.LENGTH_SHORT);
					t.setGravity(gravityType, 0, yGravity);
					t.show();
					return;
				} else if(presetName.length() > 26){
					Toast t = Toast.makeText(getActivity(), "Name must be 1-26 characters.", Toast.LENGTH_SHORT);
					t.setGravity(gravityType, 0, yGravity);
					t.show();
					return; //exit if duplicate names
				}
				else if(presetName.contains("'")){
					Toast t = Toast.makeText(getActivity(), "Preset name cannot contain the symbol ' .", Toast.LENGTH_SHORT);
					t.setGravity(gravityType, 0, yGravity);
					t.show();
					return;
				}
				else if(db.createEntry(presetName, tr.getPlayersArrList()) == false){
					Toast t = Toast.makeText(getActivity(), "Preset name already exists.", Toast.LENGTH_SHORT);
					t.setGravity(gravityType, 0, yGravity);
					t.show();
					etPresetName.setText("");
					return;
				} else{
					Toast t = Toast.makeText(getActivity(), "Preset saved.", Toast.LENGTH_SHORT);
					t.setGravity(gravityType, 0, yGravity);
					t.show();
					
					if(tr.isRedirectedFromNew()){ //if redirected from the new button
						tr.clearAll();
						tr.setRedirectFromNew(false); //set it back to false
					} else{				
						tr.setCurrentPresetText(presetName);
						tr.setPresetLoaded(true);
						tr.initializeUpdateList();
					}
				}
			}catch(Exception e){
				Toast t = Toast.makeText(getActivity(), "Invalid Name.", Toast.LENGTH_SHORT);
				t.setGravity(gravityType, 0, yGravity);
				t.show();
				return;
			}
			db.close();
			dismiss();		
		}
	}
}
