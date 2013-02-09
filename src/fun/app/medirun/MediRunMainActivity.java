package fun.app.medirun;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.CalendarView.OnDateChangeListener;
import android.widget.Toast;

// Difference between Activity and FragmentActivity:
// http://stackoverflow.com/questions/10477997/difference-between-activity-and-fragmentactivity

public class MediRunMainActivity extends FragmentActivity {

	public static final boolean DEBUG_FLAG = false;
	public final static String YEAR = "fun.app.medirun.YEAR";
	public final static String MONTH = "fun.app.medirun.MONTH";
	public final static String DAY = "fun.app.medirun.DAY";

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide fragments for each of the
	 * sections. We use a {@link android.support.v4.app.FragmentPagerAdapter} derivative, which will
	 * keep every loaded fragment in memory. If this becomes too memory intensive, it may be best
	 * to switch to a {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_medi_run_main);
		// Create the adapter that will return a fragment for each of the three primary sections
		// of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());


		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_medi_run_main, menu);
		return true;
	}




	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the primary
	 * sections of the app.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int i) {
			Fragment fragment = new DummySectionFragment();
			Bundle args = new Bundle();
			args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, i + 1);
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public int getCount() {
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case 0: return getString(R.string.title_section1).toUpperCase();
			case 1: return getString(R.string.title_section2).toUpperCase();
			case 2: return getString(R.string.title_section3).toUpperCase();
			}
			return null;
		}
	}

	/**
	 * A dummy fragment representing a section of the app, but that simply displays dummy text.
	 */
	public class DummySectionFragment extends Fragment  {
		public DummySectionFragment() {
		}

		public static final String ARG_SECTION_NUMBER = "section_number";

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {

			// Get section number
			Bundle args = getArguments();
			int sectionNum = args.getInt(ARG_SECTION_NUMBER);

			// Section 2: Meditation
			// Section 3: Running
			if (sectionNum != 1) {

				// Create calendar view, can click on it, and it listens to date change
				CalendarView calView = new CalendarView(getActivity());
				calView.setClickable(true);
				calView.setOnDateChangeListener(new OnDateChangeListener() {

					@Override
					public void onSelectedDayChange(CalendarView view, int year, int month,
							int dayOfMonth) {
						if (MediRunMainActivity.DEBUG_FLAG)
							Toast.makeText(getApplicationContext(), ""+dayOfMonth, 0).show();// TODO Auto-generated method stub

						Intent intent = new Intent(getBaseContext(), MediActivity.class);
						intent.putExtra(YEAR, String.valueOf(year));
						intent.putExtra(MONTH, String.valueOf(month));
						intent.putExtra(DAY, String.valueOf(dayOfMonth));
						startActivity(intent);

					}
				});
				return calView;
			} // Section 1: Trends
			else {
				TextView textView = new TextView(getActivity());
				textView.setGravity(Gravity.CENTER);
				textView.setText(new String("Under Construction"));
				return textView;
			}
		}
	}


}
