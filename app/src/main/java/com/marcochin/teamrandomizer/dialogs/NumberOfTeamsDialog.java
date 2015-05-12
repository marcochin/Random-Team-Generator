package com.marcochin.teamrandomizer.dialogs;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.marcochin.teamrandomizer.R;
import com.marcochin.teamrandomizer.Randomize;
import com.marcochin.teamrandomizer.TeamRandomizer;

public class NumberOfTeamsDialog extends DialogFragment implements OnClickListener{

	TeamRandomizer tr;
	EditText etNumberOfTeams;
	
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
		
		if(tr.getNumberOfteamsPrevious() != null){ //set number of teams to the previous setting
			etNumberOfTeams.setText(tr.getNumberOfteamsPrevious());
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		getDialog().setTitle("Enter Number of Teams:");
		
		View view = inflater.inflate(R.layout.numberofteamsdialog, null);
		etNumberOfTeams = (EditText) view.findViewById(R.id.etNumberOfTeams);
		Button bGoRandomize = (Button) view.findViewById(R.id.bGoRandomize);
		Button bCancelRandomize = (Button) view.findViewById(R.id.bCancelRandomize);
		
		bGoRandomize.setOnClickListener(this);
		bCancelRandomize.setOnClickListener(this);
		return view;
	}
	
	@Override
	public void onClick(View v) {
		int yGravity = 270;
		int gravityType = Gravity.TOP;
		int numberOfTeams;
		
		if(v.getId() == R.id.bCancelRandomize){
			dismiss();
			return;
		}
		
		try{
			numberOfTeams = Integer.parseInt(etNumberOfTeams.getText().toString());
			if(numberOfTeams < 2){
				Toast t = Toast.makeText(getActivity(), "Please enter at least 2 teams or more.", Toast.LENGTH_SHORT);
				t.setGravity(gravityType, 0, yGravity);
				t.show();
			} else if(numberOfTeams > tr.getPlayersArrList().size()){
				Toast t = Toast.makeText(getActivity(), "The number of teams must be LESS THAN or EQUAL to the number of players.", Toast.LENGTH_LONG);
				t.setGravity(gravityType, 0, yGravity);
				t.show();
			} else {
				if(v.getId() == R.id.bGoRandomize){
					tr.setNumberOfteamsPrevious(Integer.toString(numberOfTeams)); //lets edittext remember the previous entered number
					Intent i = new Intent(getActivity(), Randomize.class);
					i.putStringArrayListExtra("playerArrList", tr.getPlayersArrList());
					i.putExtra("numberOfTeams", numberOfTeams);
					i.putExtra("currentPreset", tr.getCurrentPreset());
					startActivity(i);
					dismiss();
				}
			}
		}
		catch(NumberFormatException e){
			Toast t = Toast.makeText(getActivity(), "Please enter at least 2 teams or more.", Toast.LENGTH_SHORT);
			t.setGravity(gravityType, 0, yGravity);
			t.show();
		}
	}
	
}
