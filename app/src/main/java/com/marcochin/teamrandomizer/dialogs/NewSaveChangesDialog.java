package com.marcochin.teamrandomizer.dialogs;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.marcochin.teamrandomizer.R;
import com.marcochin.teamrandomizer.TeamRandomizer;

public class NewSaveChangesDialog extends DialogFragment implements OnClickListener{
	TeamRandomizer tr;
	
	@Override
	public void onAttach(Activity activity) { //the activity holding the fragment will be passed in here
		// TODO Auto-generated method stub
		super.onAttach(activity);
		tr = (TeamRandomizer) activity;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		//getDialog().setTitle("Save Changes?");
		getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		View view = inflater.inflate(R.layout.savenewchangesdialog, null);
		Button bSNCnosave = (Button) view.findViewById(R.id.bSNCnosave);
		Button bSNCyessave= (Button) view.findViewById(R.id.bSNCyessave);
		TextView tvSaveChangesToCurrPreset = (TextView)view.findViewById(R.id.tvSNCSaveNewChanges) ;
		
		tvSaveChangesToCurrPreset.setText("Save changes to \"" + tr.getCurrentPreset() + "\" ?");
		
		bSNCnosave.setOnClickListener(this);
		bSNCyessave.setOnClickListener(this);
		return view;
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v.getId() == R.id.bSNCnosave){
			tr.clearAll();
			dismiss();	
		} else if(v.getId() == R.id.bSNCyessave){
			//set a boolean that this is 
			tr.setRedirectFromNew(true);
			tr.savePreset();
			dismiss();
		}
	}

}
