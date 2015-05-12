package com.marcochin.teamrandomizer;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.AddFloatingActionButton;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.marcochin.teamrandomizer.dialogs.NewSaveChangesDialog;
import com.marcochin.teamrandomizer.dialogs.NumberOfTeamsDialog;
import com.marcochin.teamrandomizer.dialogs.SavePresetAsDialog;

public class TeamRandomizer extends Activity implements OnClickListener, OnTouchListener{

	private TextView tvTotal, tvEmpty, tvCurrentPreset;
	private EditText etName;
	private ScrollView sv;
	private LinearLayout llPlayerList;
	private ArrayList<String> playerArrList;
	private ArrayList<UpdateObj>updateList;
	//private SoundPool sp;
	//private int finish_him = 0;
	private boolean presetLoaded, changesMade, redirectFromNew;
	private int yGravity = 270;
	private int gravityType = Gravity.TOP;
	private String numberOfteamsPrevious;

	private static boolean keyboardHidden = true;
	private int etNameYCoordinate;
    private AdView avHomeAd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//get rid of action bar at top
		requestWindowFeature(Window.FEATURE_NO_TITLE); //get rid of action bar at top

		//stops keyboard from automatically appearing, and prevents layout from shifting when keyboard is visible(sorta)
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

		setContentView(R.layout.teamrand);
		initializeViews();
		playerArrList  = new ArrayList<String>();

		/*sp = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
		finish_him = sp.load(this, R.raw.finish_him, 1); // load this sound file*/

		//make sure phone has google play services installed
		/*int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
		if(resultCode != ConnectionResult.SUCCESS){
			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 1);
			dialog.show();
		}*/
	}

	@Override
	protected void onResume() {
		super.onResume();

        if(avHomeAd != null)
            avHomeAd.resume();
	}

	@Override
	protected void onPause() {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(etName.getWindowToken(), 0);

        if(avHomeAd != null)
            avHomeAd.pause();

		super.onPause();
	}

	@Override
	protected void onDestroy() {
        if(avHomeAd != null) {
            avHomeAd.removeAllViews();
            avHomeAd.destroy();
        }

		super.onDestroy();

		/*if (sp != null) {
            sp.release();
            sp = null;
        }*/
	}

	public void initializeViews(){
		tvTotal = (TextView)findViewById(R.id.tvTotalPlayers);
		tvEmpty = (TextView)findViewById(R.id.tvPlayerList);
		tvCurrentPreset= (TextView)findViewById(R.id.tvCurrentPreset);
		etName = (EditText)findViewById(R.id.etPlayerName);
		sv = (ScrollView)findViewById(R.id.svScroll);
		llPlayerList = (LinearLayout)findViewById(R.id.llPlayerList);

		final Button delName = (Button)findViewById(R.id.bDelName);
		final Button presetOptions = (Button)findViewById(R.id.bPresetOptions);
		final ViewGroup rlLabels = (ViewGroup)findViewById(R.id.rlLabels);
        AddFloatingActionButton addActionButton = (AddFloatingActionButton)findViewById(R.id.add_action_button);

		//Makes presetOptions button same size as delName button
		delName.post(new Runnable() {
			@Override
			public void run() {
				Rect rectf = new Rect();
				delName.getLocalVisibleRect(rectf);

				LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)presetOptions.getLayoutParams();
				params.width = rectf.width();
				params.height = rectf.height();

				presetOptions.setLayoutParams(params);
			}
		});

		//Listener for when the user hits the enter key on softkeyboard
		etName.setOnEditorActionListener(new TextView.OnEditorActionListener(){
		    public boolean onEditorAction(TextView exampleView, int actionId, KeyEvent event){
		        if((actionId == EditorInfo.IME_ACTION_DONE
		            || actionId == EditorInfo.IME_NULL
		            || event.getKeyCode() == KeyEvent.KEYCODE_ENTER)
		            && event.getAction() == KeyEvent.ACTION_UP){

		        	addName();
		        }

		        return true;
		    }
		});

        //get the starting coordinates of etName before it shifts
		etName.post(new Runnable() {
			@Override
			public void run() {
				int[] coordinates = {0, 0};
		        etName.getLocationInWindow(coordinates);
		        etNameYCoordinate = coordinates[1];
			}
		});

		//Listener to detect if keyboard is shown or not
		final View decorView = this.getWindow().getDecorView();
		decorView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
		    @Override
		    public void onGlobalLayout() {
		    	//Calculations to detect if keyboard is present or not
		        Rect rect = new Rect();
		        decorView.getWindowVisibleDisplayFrame(rect);
		        int displayHeight = rect.bottom - rect.top;
		        int height = decorView.getHeight();

		        boolean keyboardHiddenTemp = (double)displayHeight / height > 0.8 ;

		        if (keyboardHiddenTemp != keyboardHidden) {
		            keyboardHidden = keyboardHiddenTemp;

		            if (!keyboardHidden) {
		            	//keyboard shown

		                sv.postDelayed(new Runnable() {

							@Override
							public void run() {
								int[] coordinates = {0, 0};
						        etName.getLocationInWindow(coordinates);
                                //beginning coordinates subtract the new shifted coordinates is the amount shifted
						        int shiftInLayoutByKeyboard = etNameYCoordinate - coordinates[1];

						        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)rlLabels.getLayoutParams();
				                params.setMargins(0, shiftInLayoutByKeyboard, 0, 0);
				                rlLabels.setLayoutParams(params);


						        sv.post(new Runnable() {

									@Override
									public void run() {
						                //scrolls the scrollview to the bottom
										sv.fullScroll(View.FOCUS_DOWN);
									}
								});
							}
						}, 225);

		            } else {
		            	//keyboard hidden
		            	RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)rlLabels.getLayoutParams();
		                params.setMargins(0, 0, 0, 0);
		                rlLabels.setLayoutParams(params);

		            }
		        }
		    }
		});

		delName.setOnClickListener(this);
		presetOptions.setOnClickListener(this);
        findViewById(R.id.bRandomize).setOnClickListener(this);
        addActionButton.setOnClickListener(this);

        findViewById(R.id.rlRoot).setOnTouchListener(this);
		sv.setOnTouchListener(this);

		//add ad!
	    // Look up the AdView as a resource and load a request.
        avHomeAd = (AdView) this.findViewById(R.id.avHomeAd);
	    AdRequest adRequest = new AdRequest.Builder().build();
	    avHomeAd.loadAd(adRequest);
	}

	//diff from addNameToScreen(), this is the event from hitting the add button.
	//addNameToScreen is called from coming back from another activity and it needs to refresh the list due to some change
	private void addName(){
		//--------------------------------Update List
		String name;
		//get name in EditText
		name = etName.getText().toString().trim();

		//if name is empty display toast and return
		if(name.isEmpty()){
			Toast t = Toast.makeText(getApplicationContext(), "Please enter a name!", Toast.LENGTH_SHORT);
			t.setGravity(gravityType, 0, yGravity);
			t.show();
			etName.setText(""); //reset edittext
			return;
		}else if(name.contains("~") || name.contains("'")){
			Toast t = Toast.makeText(getApplicationContext(), "Name cannot contain the symbols ~ or ' .", Toast.LENGTH_SHORT);
			t.setGravity(gravityType, 0, yGravity);
			t.show();
			return;
		}else if(playerArrList.contains(name)){
			Toast t = Toast.makeText(getApplicationContext(), "Please enter a unique name.", Toast.LENGTH_SHORT);
			t.setGravity(gravityType, 0, yGravity);
			t.show();
			return; //exit if duplicate names
		} else if(name.length() > 26){
			Toast t = Toast.makeText(getApplicationContext(), "Name must be 1-26 characters.", Toast.LENGTH_SHORT);
			t.setGravity(gravityType, 0, yGravity);
			t.show();
			return; //exit if duplicate names
		}

		//--------------------------------Update Total

		int total = Integer.parseInt(tvTotal.getText().toString());
		total ++;
		tvTotal.setText(Integer.toString(total));

		//---------------------------------------------
		if(!isChangesMade()){
			setChangesMade(true); //set changes made to true on add
		}

		//always add to playerList, only add to updateList if preset is loaded for more efficient saves
		playerArrList.add(name);
		if(isPresetLoaded()){ //adds to update List
			updateList.add(new UpdateObj("add", name));
		}
		//if name is valid add a number to it
		name = Integer.toString(total) + ". "+ name;

		//remove the tvEmpty
		llPlayerList.removeView(tvEmpty);

		addNameToScreen(name);

		//reset editText to blank after adding a name
		etName.setText("");

        sv.post(new Runnable() {

            @Override
            public void run() {
                //scrolls the scrollview to the bottom
                sv.fullScroll(View.FOCUS_DOWN);
            }
        });
	}

	@Override
	public void onClick(View v) {

		// TODO Auto-generated method stub
		switch(v.getId()){
            case R.id.add_action_button:
                addName();
                break;

            case R.id.bDelName:
                if(playerArrList.size()>0){

                    /*if (finish_him != 0) { //FINISH HIM!
                        sp.play(finish_him, 1, 1, 0, 0, 1); // hover over play for documentation
                    }*/

                    Intent i = new Intent(this, DeleteName.class);
                    i.putStringArrayListExtra("playerList", playerArrList);
                    i.putExtra("currentPreset", tvCurrentPreset.getText().toString());
                    if(isPresetLoaded()){ //if preset is loaded pass in updatelist
                        i.putExtra("updateList", updateList);
                    }
                    startActivityForResult(i, 1); //0 is default request code
                }else{
                    Toast t = Toast.makeText(getApplicationContext(), "Nothing to delete.", Toast.LENGTH_SHORT);
                    t.setGravity(gravityType, 0, yGravity);
                    t.show();
                }
                break;

            case R.id.bRandomize:
                if(playerArrList.size() >= 2){
                    FragmentManager manager = getFragmentManager();
                    NumberOfTeamsDialog notd = new NumberOfTeamsDialog();
                    notd.show(manager, "notd"); //adds fragment to the manager
                }else{
                    Toast t = Toast.makeText(getApplicationContext(), "Please enter at least 2 players.", Toast.LENGTH_SHORT);
                    t.setGravity(gravityType, 0, yGravity);
                    t.show();
                }
                break;

            case R.id.bPresetOptions:
                openOptionsMenu();
                break;
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {

		int eventID = event.getAction();
		switch(eventID){

		//Hide soft KeyBoard when touching background
		case MotionEvent.ACTION_DOWN:
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(etName.getWindowToken(), 0);
			return true;
		}

		return false;
	}

	@Override
	public void openOptionsMenu() {
	    super.openOptionsMenu();
	    Configuration config = getResources().getConfiguration();
	    if ((config.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) > Configuration.SCREENLAYOUT_SIZE_LARGE) {
	        int originalScreenLayout = config.screenLayout;
	        config.screenLayout = Configuration.SCREENLAYOUT_SIZE_LARGE;
	        super.openOptionsMenu();
	        config.screenLayout = originalScreenLayout;
	    } else {
	        super.openOptionsMenu();
	    }
	}

	@Override
	public boolean onCreateOptionsMenu(android.view.Menu menu) {//called once on load to load menu aka setting button
		// TODO Auto-generated method stub
		super.onCreateOptionsMenu(menu);
		MenuInflater blowUp = getMenuInflater();
		blowUp.inflate(R.menu.teamrand_menu, menu); //converts(inflates) the xml to a menu object
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) { //will call this when you click an menu item
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case R.id.newPreset:
			if(changesMade){
				openSaveNewChangesDialog();
			} else{
				clearAll();
			}

			break;

		case R.id.loadPreset:
			Intent i = new Intent(this, LoadPreset.class);
			i.putExtra("changesMade", isChangesMade());
			i.putExtra("presetLoaded", isPresetLoaded());
			i.putExtra("updateList", updateList);
			i.putStringArrayListExtra("playerList", playerArrList);
			i.putExtra("currentPreset", tvCurrentPreset.getText().toString());
			startActivityForResult(i, 2); //0 is default request code
			break;

		case R.id.savePresetAs:
			openSavePresetAsDialog();
			break;

		case R.id.savePreset:
			savePreset();
			break;

		case R.id.deletePreset:
			Intent in = new Intent(this, DeletePreset.class);
			if(isPresetLoaded()){
				in.putExtra("currentPreset", tvCurrentPreset.getText().toString());
			}
			startActivityForResult(in, 3); //0 is default request code
			break;
		}
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);

		if(resultCode == RESULT_OK){

			switch(requestCode){
				//Bundle basket = data.getExtras();
			case 1://1 if deleteName
				if(playerArrList.size() == data.getStringArrayListExtra("playerList").size()){
					return; //exit if playerList size did not change
				}

				if(!isChangesMade()){
					setChangesMade(true); //set changes made to true on delete
				}

				playerArrList = data.getStringArrayListExtra("playerList");
				if(data.getSerializableExtra("updateList") != null){
					updateList = (ArrayList<UpdateObj>)data.getSerializableExtra("updateList");
				}

				populatePlayerList();
				break;
			case 2://load preset
				tvCurrentPreset.setText(data.getStringExtra("presetName"));
				playerArrList = data.getStringArrayListExtra("playerNames");

				populatePlayerList();

				setPresetLoaded(true); //make sure preset is loaded so we can start up initializeupdatelist
				initializeUpdateList();
				break;
			case 3: //delete Preset
				if(data.getBooleanExtra("currPresetDeleted", false)){
					clearAll();
				}
				break;
			}
		}
	}

	public void populatePlayerList(){
		llPlayerList.removeAllViews();

		if(playerArrList.size() == 0){
			llPlayerList.addView(tvEmpty);
			tvTotal.setText(Integer.toString(playerArrList.size()));
			setChangesMade(false);
			return; //exit if playerArrList is 0
		}
		int i=1;
		for(String nameString : playerArrList){
			//add name to players list
			String name = Integer.toString(i) + ". " + nameString;
			addNameToScreen(name);
			i++;
		}
		//tvList.setText(revisedList); //set the tv to the new List after revision(delete/update)
		tvTotal.setText(Integer.toString(playerArrList.size()));
	}

	public void initializeUpdateList(){ //must initialize update if you load a preset
		if(isPresetLoaded() == true){
			updateList = new ArrayList<UpdateObj>();
		}
	}

	public void clearAll(){
		setPresetLoaded(false);
		tvCurrentPreset.setText("None");
		playerArrList.clear();
		updateList = null;
		populatePlayerList();
	}

	private void openSavePresetAsDialog(){
		FragmentManager manager = getFragmentManager();
		SavePresetAsDialog savepreset = new SavePresetAsDialog();
		savepreset.show(manager, "savepreset"); //adds fragment to the manager
	}

	public void openSaveNewChangesDialog(){
		FragmentManager manager = getFragmentManager();
		NewSaveChangesDialog newscd = new NewSaveChangesDialog();
		newscd.show(manager, "newscd"); //adds fragment to the manager
	}

	public void savePreset(){
		if(isPresetLoaded()){
			DbHelper db = new DbHelper(this);
			db.open();
			db.updatePreset(tvCurrentPreset.getText().toString(), updateList);
			db.close();
			Toast t = Toast.makeText(getApplicationContext(), "Preset saved.", Toast.LENGTH_SHORT);
			t.setGravity(gravityType, 0, yGravity);
			t.show();

			updateList.clear();
			setChangesMade(false); //when saved = blank slate
		} else {
			openSavePresetAsDialog();
		}
	}

	public void addNameToScreen(String name){
		//add name to players list
		TextView tvNewName = new TextView(this);
		tvNewName.setText(name.replaceFirst("\\s", "\u00A0")); //replace first space with unicode no break space);
		tvNewName.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.rtg_text_size));
		llPlayerList.addView(tvNewName);
	}

	public void setChangesMade(boolean changesMade){
		this.changesMade = changesMade;
	}

	public boolean isChangesMade(){
		return changesMade;
	}

	public void setRedirectFromNew (boolean redirectFromNew){
		this.redirectFromNew = redirectFromNew;
	}

	public boolean isRedirectedFromNew(){
		return redirectFromNew;
	}

	public ArrayList<String> getPlayersArrList(){
		return playerArrList;
	}

	public void setCurrentPresetText(String currentPreset){
		tvCurrentPreset.setText(currentPreset);
	}

	public boolean isPresetLoaded() {
		return presetLoaded;
	}

	public void setPresetLoaded(boolean presetLoaded) {
		this.presetLoaded = presetLoaded;
		setChangesMade(false); //if presets is loaded there are no new changes
	}

	public String getCurrentPreset(){
		return tvCurrentPreset.getText().toString();
	}

	public String getTotalPlayer(){
		return tvTotal.getText().toString();
	}

	public String getNumberOfteamsPrevious() {
		return numberOfteamsPrevious;
	}

	public void setNumberOfteamsPrevious(String numberOfteamsPrevious) {
		this.numberOfteamsPrevious = numberOfteamsPrevious;
	}

}//TeamRandomizer


