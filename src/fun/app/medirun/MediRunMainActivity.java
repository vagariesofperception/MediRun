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

	public final static String logTag = "MediRunMainActivity";
	

	private MediRunDataStore mediRunStore;
	private static final int MEDI_ACTIVITY = 1;
	
	private GraphicalView mChart;
	private XYMultipleSeriesDataset mMediDataset = new XYMultipleSeriesDataset();
	private XYMultipleSeriesRenderer mMediRenderer = new XYMultipleSeriesRenderer();
	
	
	private TimeSeries mCurrentMediSeries;
	private XYSeriesRenderer mCurrentMediRenderer;
	
	

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


	private void initChart() {
		mCurrentMediSeries = new TimeSeries("Meditation minutes");
		mMediDataset.addSeries(mCurrentMediSeries);
		mCurrentMediRenderer = new XYSeriesRenderer();
		mMediRenderer.addSeriesRenderer(mCurrentMediRenderer);
	}
	
	private boolean addMediData() {

		SortedSet<MediRunDataStore.Pair> oList =
		     mediRunStore.getMediDataInOrder();
		Iterator<MediRunDataStore.Pair> it = oList.iterator();
		Date minDate=null, maxDate=null;
		
		if (oList.size() == 0)
			return false;
		while (it.hasNext())
		{
            Entry<Date, Integer> pair = it.next();
			if (minDate == null)
				minDate = pair.getKey();
			else if (pair.getKey().getTime() < minDate.getTime())
				minDate = pair.getKey();
			
			
			Log.i(logTag, "x:" + pair.getKey().toString() + " y:" + pair.getValue().doubleValue());
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
		}
		
		
		
	}

	@Override
	public void onResume() {
	   super.onResume();
	  
    
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_medi_run_main, menu);
		Log.i(logTag, "Inflated activity_graph!");
		Button b = (Button)findViewById(R.id.backUpBtn);
		if (b != null)
			Log.i(logTag, "back button found!");
		LinearLayout chartLayout = (LinearLayout)findViewById(R.id.chart);

		 if (chartLayout != null) {
			 Log.i(logTag, "chart is not null");
		    if (mChart == null) {
		    	Log.i(logTag, "mChart is null");
		    	initChart();
		    	boolean hasData = addMediData();
		    	mChart = ChartFactory.getTimeChartView(
		    			this,
		    			mMediDataset, 
		    			mMediRenderer, 
		    		null);
		    	chartLayout.addView(mChart);
		    } else {
		    	mChart.repaint();
		    }
		   }
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
						//intent.putExtra(MEDIMAP, mediMinsDataMap);
						getActivity().startActivityForResult(intent, MEDI_ACTIVITY);

					}
				});
				
				calView.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						Log.i(logTag, "Click registered for calView.OnClickListener!");
						CalendarView cV = (CalendarView) v;
						Date currentDay = new Date(cV.getDate());
						Date minDate = new Date(cV.getMinDate());
						View minView = cV.getChildAt(0);
						Log.i(logTag, "Class:" + minView.getClass().toString());
						
						Log.i(logTag, "Date:" + currentDay.toString() + " Child Count:" + cV.getChildCount()
								 + " Min Date:" + new Date(cV.getMinDate()).toString());
						
						View childView = cV.getChildAt((int)(cV.getDate()- (cV.getMinDate())));
						//View childView = cV.getFocusedChild();
						if (childView != null)
						{
							childView.setOnClickListener(new View.OnClickListener() {
								
								@Override
								public void onClick(View v) {
									// TODO Auto-generated method stub
									Log.i(logTag, "Click registered for child.calView.OnClickListener!");
								}
							});
						}
					}
				});
				calView.callOnClick();
							
			   
				return calView;
			} // Section 1: Trends
			else {
				View v = inflater.inflate(R.layout.activity_graph, container, false);
			
				
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
		}
	}


}
