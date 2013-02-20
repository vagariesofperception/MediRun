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

public class MediActivity extends Activity implements TextView.OnEditorActionListener {
	private TextView mediDate;
	private TextView mediDateVal;

	private EditText mediMins;
	private TextView mediMinText;
	private Button updateMediBtn;

	private int currentYear, currentMonth, currentDay;
	private int currentMediMins;
	private Date currentDate;

	public final String logTag = "MediActivity";
	//public HashMap<Date, Integer> mediMap;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_medi);

		if (mediDate == null)
			mediDate = (TextView) findViewById(R.id.mediDate);

		if (mediDateVal == null)
			mediDateVal = (TextView) findViewById(R.id.mediDateVal);

		if (mediMins == null)
			mediMins = (EditText) findViewById(R.id.mediMins);

		if (mediMinText == null)
			mediMinText = (TextView) findViewById(R.id.mediMinText);

		mediMins.setOnEditorActionListener(this);

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
		
		if (intent.hasExtra(MediRunMainActivity.EXISTING_MEDIMINS))
		{
			String preExistingMins = intent.getStringExtra(MediRunMainActivity.EXISTING_MEDIMINS);
			mediMins.setText(preExistingMins);
		}
		
		//mediMap = (HashMap) intent.getSerializableExtra(MediRunMainActivity.MEDIMAP);

		Log.i(logTag, "onCreate:" +  String.valueOf(currentMonth) + "/" + 
				String.valueOf(currentDay) + "/" + String.valueOf(currentYear));
		Date d = null;
		Calendar cal = GregorianCalendar.getInstance();
		cal.set(currentYear, currentMonth, currentDay);
		d = cal.getTime();
		currentDate = d;
		String strDate = DateFormat.format("MMMM dd, yyyy", d).toString();
		mediDateVal.setText(strDate);


	}

	public void updateMediButtonClick(View view) {
		Log.i(logTag, "updateMediButtonClick called!");
        Intent intent = new Intent(this, MediRunMainActivity.class);
        intent.putExtra(MediRunMainActivity.CURRENT_MEDI_DATE, currentDate);
        intent.putExtra(MediRunMainActivity.CURRENT_MEDI_MINS, String.valueOf(currentMediMins));
        
        // Activity finished ok, return the data
        setResult(RESULT_OK, intent);
        finish();
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

		boolean handled = false;
		if (actionId == EditorInfo.IME_ACTION_DONE) {
			String s = v.getText().toString();
			Log.i(logTag, "Medi Mins:" + s + " on " + String.valueOf(currentMonth) + "/" + 
					String.valueOf(currentDay) + "/" + String.valueOf(currentYear));
			try {
			currentMediMins = Integer.parseInt(s);
			}
			catch (NumberFormatException e) {
				currentMediMins = 0;
			}
			//mediMap.put(currentDate, Integer.decode(s));
		}
		return handled;
	}
}
