package fun.app.medirun;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.SortedSet;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart.Type;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioRecord.OnRecordPositionUpdateListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub.OnInflateListener;
import android.webkit.WebView.FindListener;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.CalendarView.OnDateChangeListener;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.Toast;

// Difference between Activity and FragmentActivity:
// http://stackoverflow.com/questions/10477997/difference-between-activity-and-fragmentactivity

public class MediRunMainActivity extends FragmentActivity {

	public static final boolean DEBUG_FLAG = false;
	public final static String YEAR = "fun.app.medirun.YEAR";
	public final static String MONTH = "fun.app.medirun.MONTH";
	public final static String DAY = "fun.app.medirun.DAY";
	public final static String MEDIMAP = "fun.app.medirun.MEDIMAP";
	public final static String CURRENT_MEDI_DATE = "fun.app.medirun.CURRENT_MEDI_DATE";
	public final static String CURRENT_MEDI_MINS = "fun.app.medirun.CURRENT_MEDI_MINS";


	public final static String CURRENT_RUN_DATE = "fun.app.medirun.CURRENT_RUN_DATE";
	public final static String CURRENT_RUN_MILES = "fun.app.medirun.CURRENT_RUN_MILES";

	public final static String logTag = "MediRunMainActivity";


	private MediRunDataStore mediRunStore;
	private static final int MEDI_ACTIVITY = 1;
	private static final int RUN_ACTIVITY = 2;

	private GraphicalView mMediChart;
	private GraphicalView mRunChart;

	private XYMultipleSeriesDataset mMediDataset = new XYMultipleSeriesDataset();
	private XYMultipleSeriesRenderer mMediRenderer = new XYMultipleSeriesRenderer();
	private TimeSeries mCurrentMediSeries;
	private XYSeriesRenderer mCurrentMediRenderer;

	private XYMultipleSeriesDataset mRunDataset = new XYMultipleSeriesDataset();
	private XYMultipleSeriesRenderer mRunRenderer = new XYMultipleSeriesRenderer();
	private TimeSeries mCurrentRunSeries;
	private XYSeriesRenderer mCurrentRunRenderer;

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


	private void initMediChart() {
		mCurrentMediSeries = new TimeSeries("Meditation minutes");
		mMediDataset.addSeries(mCurrentMediSeries);
		mCurrentMediRenderer = new XYSeriesRenderer();
		mMediRenderer.addSeriesRenderer(mCurrentMediRenderer);
	}

	private void initRunChart() {
		mCurrentRunSeries = new TimeSeries("Run Miles");
		mRunDataset.addSeries(mCurrentRunSeries);
		mCurrentRunRenderer = new XYSeriesRenderer();
		mRunRenderer.addSeriesRenderer(mCurrentRunRenderer);
	}

	private boolean addMediData() {

		SortedSet<MediRunDataStore.DateIntPair> oList =
				mediRunStore.getMediDataInOrder();
		Iterator<MediRunDataStore.DateIntPair> it = oList.iterator();
		Date minDate=null, maxDate=null;

		if (oList.size() == 0)
			return false;
		mCurrentMediSeries.clear();
		while (it.hasNext())
		{
			Entry<Date, Integer> pair = it.next();
			if (minDate == null)
				minDate = pair.getKey();
			else if (pair.getKey().getTime() < minDate.getTime())
				minDate = pair.getKey();


			Log.i(logTag, "Mx:" + pair.getKey().toString() + " My:" + pair.getValue().doubleValue());
			mCurrentMediSeries.add(pair.getKey(), pair.getValue().doubleValue());


			if (maxDate == null)
				maxDate = pair.getKey();
			else if (maxDate.getTime() < pair.getKey().getTime())
				maxDate = pair.getKey();
		}
		if (minDate == null || maxDate == null)
			return false;
		mMediRenderer.setXAxisMin(minDate.getTime());
		mMediRenderer.setXAxisMax(maxDate.getTime());
		mMediRenderer.setYAxisMin(0.0);
		mCurrentMediRenderer.setColor(Color.RED);
		mCurrentMediRenderer.setPointStyle(PointStyle.DIAMOND);
		mCurrentMediRenderer.setFillPoints(true);
		return true;

	}

	private boolean addRunData() {

		SortedSet<MediRunDataStore.DateDoublePair> oList =
				mediRunStore.getRunDataInOrder();
		Iterator<MediRunDataStore.DateDoublePair> it = oList.iterator();
		Date minDate=null, maxDate=null;
		double maxMiles = -1.0;

		if (oList.size() == 0)
			return false;
		mCurrentRunSeries.clear();
		while (it.hasNext())
		{
			Entry<Date, Double> pair = it.next();
			if (minDate == null)
				minDate = pair.getKey();
			else if (pair.getKey().getTime() < minDate.getTime())
				minDate = pair.getKey();

			if (maxMiles < pair.getValue().doubleValue())
				maxMiles = pair.getValue().doubleValue();

			Log.i(logTag, "Rx:" + pair.getKey().toString() + " Ry:" + pair.getValue().doubleValue());
			mCurrentRunSeries.add(pair.getKey(), pair.getValue().doubleValue());


			if (maxDate == null)
				maxDate = pair.getKey();
			else if (maxDate.getTime() < pair.getKey().getTime())
				maxDate = pair.getKey();
		}
		if (minDate == null || maxDate == null)
			return false;
		mRunRenderer.setXAxisMin(minDate.getTime());
		mRunRenderer.setXAxisMax(maxDate.getTime());
		mRunRenderer.setYAxisMin(0.0);
		if (maxMiles > 0)
			mRunRenderer.setYAxisMax(maxMiles);
		mCurrentRunRenderer.setColor(Color.GREEN);
		mCurrentRunRenderer.setPointStyle(PointStyle.CIRCLE);
		mCurrentRunRenderer.setFillPoints(true);
		return true;

	}

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

		if (mediRunStore == null)	
		{
			mediRunStore = MediRunDataStore.getInstance();
			mediRunStore.bootUpMediData(this);
			mediRunStore.bootUpRunData(this);
		}
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int arg0) {
				Log.i(logTag, "onPageSelected:" + String.valueOf(arg0));
				if (arg0 == 0) {
					refreshOrCreateMediChart();
					refreshOrCreateRunChart();
				}
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub
				Log.i(logTag, "onPageScrolled:" + String.valueOf(arg0) + "," + 
						String.valueOf(arg1) + "," + String.valueOf(arg2));

			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub
				Log.i(logTag, "onPageScrollStateChanged:" + String.valueOf(arg0));

			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();
		//refreshOrCreateMediChart();
		//refreshOrCreateRunChart();
	}

	private void updateMediChart(LinearLayout mcl) {
		if (mMediChart != null)
			mcl.removeView(mMediChart);

		boolean hasData = addMediData();
		mMediChart = ChartFactory.getTimeChartView(
				this,
				mMediDataset, 
				mMediRenderer, 
				null);
		mcl.addView(mMediChart);
	}

	private void updateRunChart(LinearLayout rcl) {
		if (mRunChart != null)
			rcl.removeView(mRunChart);
		boolean hasData = addRunData();
		mRunChart = ChartFactory.getTimeChartView(
				this,
				mRunDataset, 
				mRunRenderer, 
				null);
		rcl.addView(mRunChart);
	}

	private void refreshOrCreateMediChart() {

		LinearLayout mediChartLayout = (LinearLayout)findViewById(R.id.medichart);
		if (mediChartLayout != null) {
			if (mMediChart == null) {
				Log.i(logTag, "mChart is null");
				initMediChart();
				updateMediChart(mediChartLayout);
			} else {
				updateMediChart(mediChartLayout);
				mMediChart.repaint();
			}
		}
	}


	private void refreshOrCreateRunChart() {

		LinearLayout runChartLayout = (LinearLayout)findViewById(R.id.runchart);
		if (runChartLayout != null) {
			if (mRunChart == null) {
				Log.i(logTag, "mChart is null");
				initRunChart();
				updateRunChart(runChartLayout);
			} else {
				updateRunChart(runChartLayout);
				mRunChart.repaint();
			}
		}
		return;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_medi_run_main, menu);
		Log.i(logTag, "Inflated activity_graph!");

		refreshOrCreateMediChart();
		refreshOrCreateRunChart();
		return true;
	}

	public void backUpBtnClick(View v) {

	}

	public void restoreBtnClick(View v) {

	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the primary
	 * sections of the app.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter  {

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

				if (sectionNum == 2) {
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
							getActivity().startActivityForResult(intent, MEDI_ACTIVITY);

						}
					});
				} else {
					calView.setOnDateChangeListener(new OnDateChangeListener() {

						@Override
						public void onSelectedDayChange(CalendarView view, int year, int month,
								int dayOfMonth) {
							if (MediRunMainActivity.DEBUG_FLAG)
								Toast.makeText(getApplicationContext(), ""+dayOfMonth, 0).show();// TODO Auto-generated method stub

							Intent intent = new Intent(getBaseContext(), RunActivity.class);
							intent.putExtra(YEAR, String.valueOf(year));
							intent.putExtra(MONTH, String.valueOf(month));
							intent.putExtra(DAY, String.valueOf(dayOfMonth));
							getActivity().startActivityForResult(intent, RUN_ACTIVITY);

						}
					});

				}
				return calView;
			} // Section 1: Trends
			else {
				View v = inflater.inflate(R.layout.activity_graph, container, false);
				v.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						Log.i(logTag, "Got!");
						// TODO Auto-generated method stub
						if (true) {
							refreshOrCreateMediChart();
							refreshOrCreateRunChart();
						}
					}
				});
				return v;
			}
		}


	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.i(logTag, "onActivityResult called: (Is resultCode RESULT_OK:" 
				+ String.valueOf(resultCode == RESULT_OK) + "), (Request code:" +
				String.valueOf(requestCode) + ")");

		if (resultCode == RESULT_OK && requestCode == MEDI_ACTIVITY) {
			Date currentDate = (Date)data.getSerializableExtra(CURRENT_MEDI_DATE);
			String s = data.getStringExtra(CURRENT_MEDI_MINS);
			Integer currentMins = new Integer(s);
			Log.i(logTag, "onActivityResult: Got current date " + currentDate.toString() + " CurrentMins:" +
					s);
			mediRunStore.appendMediData(currentDate, currentMins);
			//refreshOrCreateMediChart();
		} else if (resultCode == RESULT_OK && requestCode == RUN_ACTIVITY) {
			Date currentDate = (Date)data.getSerializableExtra(CURRENT_RUN_DATE);
			String s = data.getStringExtra(CURRENT_RUN_MILES);
			Double currentMiles = new Double(s);
			Log.i(logTag, "onActivityResult: Got current date " + currentDate.toString() + " CurrentMiles:" +
					s);
			mediRunStore.appendRunData(currentDate, currentMiles);
			//refreshOrCreateRunChart();
		}
	}

	@Override
	protected void onPause() {
		Log.i(logTag, "onPause called");
		//mediRunStore.flushMediRunData();
		super.onPause();
	}
}
