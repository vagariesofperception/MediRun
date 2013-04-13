package fun.app.medirun;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

public class RunActivity extends Activity implements TextView.OnEditorActionListener {
	private TextView runDate;
	private TextView runDateVal;

	private EditText runMiles;
	private TextView runMilesText;
	private Button updateRunBtn;

	private int currentYear, currentMonth, currentDay;
	private double currentRunMiles;
	private Date currentDate;

	public final String logTag = "RunActivity";
	//public HashMap<Date, Integer> mediMap;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_run);

		if (runDate == null)
			runDate = (TextView) findViewById(R.id.runDate);

		if (runDateVal == null)
			runDateVal = (TextView) findViewById(R.id.runDateVal);

		if (runMiles == null)
			runMiles = (EditText) findViewById(R.id.runMiles);

		if (runMilesText == null)
			runMilesText = (TextView) findViewById(R.id.runMilesText);

		runMiles.setOnEditorActionListener(this);

		// This is to prevent automatic popping of keyboard
		// for the edittext
		// http://stackoverflow.com/questions/2496901/android-on-screen-keyboard-auto-popping-up
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

		// Get the date that was clicked and add it to
		// info row
		Intent intent = getIntent();
		currentYear = Integer.parseInt(intent.getStringExtra(MediRunMainActivity.YEAR));
		currentMonth = Integer.parseInt(intent.getStringExtra(MediRunMainActivity.MONTH));
		currentDay = Integer.parseInt(intent.getStringExtra(MediRunMainActivity.DAY));
		
		if (intent.hasExtra(MediRunMainActivity.EXISTING_RUNMILES))
		{
			String preExistingMiles = intent.getStringExtra(MediRunMainActivity.EXISTING_RUNMILES);
			runMiles.setText(preExistingMiles);
		}
		

		Log.i(logTag, "onCreate:" +  String.valueOf(currentMonth) + "/" + 
				String.valueOf(currentDay) + "/" + String.valueOf(currentYear));
		Date d = null;
		Calendar cal = GregorianCalendar.getInstance();
		cal.set(currentYear, currentMonth, currentDay);
		d = cal.getTime();
		currentDate = d;
		String strDate = DateFormat.format("EEE MMMM dd, yyyy", d).toString();
		runDateVal.setText(strDate);


	}

	public void updateRunButtonClick(View view) {
		Log.i(logTag, "updateRunButton called!");
		Intent intent = new Intent(this, MediRunMainActivity.class);
		intent.putExtra(MediRunMainActivity.CURRENT_RUN_DATE, currentDate);
		intent.putExtra(MediRunMainActivity.CURRENT_RUN_MILES, String.valueOf(currentRunMiles));

		// Activity finished ok, return the data
		setResult(RESULT_OK, intent);
		finish();
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

		boolean handled = false;
		if (actionId == EditorInfo.IME_ACTION_DONE) {
			String s = v.getText().toString();
			Log.i(logTag, "Run Miles:" + s + " on " + String.valueOf(currentMonth) + "/" + 
					String.valueOf(currentDay) + "/" + String.valueOf(currentYear));
			try {
				currentRunMiles = Double.parseDouble(s);
			} catch (NumberFormatException e) {
				currentRunMiles = new Double(0.0);
			}
		}
		return handled;
	}
}
