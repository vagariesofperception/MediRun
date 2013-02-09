package fun.app.medirun;




import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class MediActivity extends Activity {
	private TextView mediDate;
	private TextView mediDateVal;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_medi);

		if (mediDate == null)
			mediDate = (TextView) findViewById(R.id.mediDate);

		if (mediDateVal == null)
			mediDateVal = (TextView) findViewById(R.id.mediDateVal);

		Intent intent = getIntent();
		int year = Integer.parseInt(intent.getStringExtra(MediRunMainActivity.YEAR));
		int month = Integer.parseInt(intent.getStringExtra(MediRunMainActivity.MONTH));
		int day = Integer.parseInt(intent.getStringExtra(MediRunMainActivity.DAY));

		Date d = null;
		Calendar cal = GregorianCalendar.getInstance();
		cal.set(year, month, day);
		d = cal.getTime();
		String strDate = DateFormat.format("MMMM dd, yyyy", d).toString();
		mediDateVal.setText(strDate);
	}

	public void updateMediButtonClick(View view) {
		
	}
}
