package fun.app.medirun;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Type;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.http.ParseException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

// To delete internal data files
// adb -e shell, cd /data/data/your.application.package.name/databases

// Singleton class representing the file having the data
// Using an external file in this store

// JSON refs:
// http://www.mkyong.com/java/json-simple-example-read-and-write-json/
// http://code.google.com/p/json-simple/downloads/detail?name=json-simple-1.1.1.jar&can=2&q=

// GSON refs:
// http://code.google.com/p/google-gson/


// http://stackoverflow.com/questions/5921838/android-write-file
// http://people.bridgewater.edu/~arn002/csci440/android-tutorial.htm#file
// One of the most basic useful functions that a program can perform is reading and writing files.
// In Android, this is almost as simple as it is in Java, with one notable difference: Android has two
// types of files, internal and external. Internal files are only visible to the application that 
// created them, and are used for local storage for one app. External files are stored on the device's 
// SD card and are visible to any apps running on the device, as well as to a PC connected with a USB
// cable. This tutorial will only deal with creating internal files, as there are some extra
// complications involved in accessing an SD card that may or may not be present.

public class MediRunDataStore {
	private static final String mediFileName = "MediData.json";
	private static final String runFileName = "RunData.json";
	private static final String pranFileName = "PranData.json";
	private static final String graphPNGFileName = "MediRunGraph.png";
	private static final String logTag = "MediRunDataStore";
	private static volatile MediRunDataStore instance = null;
	private File mediFile;

	private HashMap<String, Integer> mediMap;
	private HashMap<String, Integer> pranMap;
	private HashMap<String, Double> runMap;
	private Activity mActivity;

	class JsonDateSerializer implements JsonSerializer<Date>
	{
		public JsonElement serialize(Date t, Type type, JsonSerializationContext jsc)
		{
			return new JsonPrimitive(t.getTime());
		}
	}

	class JsonDateDeSerializer implements JsonDeserializer<Date> {
		public Date deserialize(JsonElement json, Type typeOfT,
				JsonDeserializationContext context) throws JsonParseException {
			return new Date(json.getAsLong());
		}
	}

	private MediRunDataStore() {
	}
	

	public boolean bootUpMediData(Activity mainActivity)
	{
		Log.i(logTag, "bootUpMediData called!");
		if (mActivity == null)
			mActivity = mainActivity;
		JsonParser parser = new JsonParser();
		try {
			FileInputStream fis =  mActivity.openFileInput(mediFileName);
			BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
			Object obj = parser.parse(reader);
			JsonObject jsonObject = (JsonObject) obj;
			String jStr = jsonObject.toString();
			Gson gson = new GsonBuilder().create();
			//.registerTypeAdapter(Date.class, new JsonDateDeSerializer()).create();
			Type typeOfHashMap = new TypeToken<Map<String, Integer>>() { }.getType();
			mediMap = gson.fromJson(jStr, typeOfHashMap);
			return true;
		}
		catch (FileNotFoundException e) {
			Log.i(logTag, "mediFileNotFound");
			e.printStackTrace();
		}
		catch (Exception e) {
			Log.i(logTag, "some other exception on medi file open");
			e.printStackTrace();
		}
		if (mediMap == null)
			mediMap = new HashMap<String, Integer>();
		Log.i(logTag, "Size of mediMap:" + String.valueOf(mediMap.size()));
		return false;
	}
	
	public boolean bootUpPranData(Activity mainActivity)
	{
		Log.i(logTag, "bootUpPranData called!");
		if (mActivity == null)
			mActivity = mainActivity;
		JsonParser parser = new JsonParser();
		try {
			FileInputStream fis =  mActivity.openFileInput(pranFileName);
			BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
			Object obj = parser.parse(reader);
			JsonObject jsonObject = (JsonObject) obj;
			String jStr = jsonObject.toString();
			Gson gson = new GsonBuilder().create();
			//.registerTypeAdapter(Date.class, new JsonDateDeSerializer()).create();
			Type typeOfHashMap = new TypeToken<Map<String, Integer>>() { }.getType();
			pranMap = gson.fromJson(jStr, typeOfHashMap);
			return true;
		}
		catch (FileNotFoundException e) {
			Log.i(logTag, "pranFileNotFound");
			e.printStackTrace();
		}
		catch (Exception e) {
			Log.i(logTag, "some other exception on medi file open");
			e.printStackTrace();
		}
		if (pranMap == null)
			pranMap = new HashMap<String, Integer>();
		Log.i(logTag, "Size of pranMap:" + String.valueOf(pranMap.size()));
		return false;
	}

	public boolean bootUpRunData(Activity mainActivity) {

		Log.i(logTag, "bootUpRunData called!");
		if (mActivity == null)
			mActivity = mainActivity;
		JsonParser parser = new JsonParser();
		try {
			FileInputStream fis =  mActivity.openFileInput(runFileName);
			BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
			Object obj = parser.parse(reader);
			JsonObject jsonObject = (JsonObject) obj;
			String jStr = jsonObject.toString();
			Gson gson = new GsonBuilder().create();
			Type typeOfHashMap = new TypeToken<Map<String, Double>>() { }.getType();
			runMap = gson.fromJson(jStr, typeOfHashMap);
			return true;
		}

		catch (FileNotFoundException e) {
			Log.i(logTag, "runFilenotfound");
			e.printStackTrace();
		}
		catch (Exception e) {
			Log.i(logTag, "some other exception on run file open");
			e.printStackTrace();
		}
		if (runMap == null)
			runMap = new HashMap<String, Double>();

		Log.i(logTag, "Size of runMap:" + String.valueOf(runMap.size()));
		return false;
	}

	// in format dd/MM/YY
	private Date getDateForString(String dateStr) {
		String[] tokens = dateStr.split("/");
		Calendar cal = Calendar.getInstance();
		cal.set(Integer.parseInt(tokens[2]),
				Integer.parseInt(tokens[1]),
				Integer.parseInt(tokens[0]), 0, 0, 0);
		return cal.getTime();
	}

	public SortedSet<DateIntPair> getMediDataInOrderInternal() {
		SortedSet<DateIntPair> oList = new 
				TreeSet<DateIntPair>();
		Iterator it = mediMap.entrySet().iterator();
		while (it.hasNext())
		{
			Map.Entry<String, Integer> kv
			= (Map.Entry<String, Integer>)it.next();

			oList.add(new DateIntPair(getDateForString(kv.getKey()),
					kv.getValue()));
		}
		return oList;		
	}

	public SortedSet<DateIntPair> getPranDataInOrderInternal() {
		SortedSet<DateIntPair> oList = new 
				TreeSet<DateIntPair>();
		Iterator it = pranMap.entrySet().iterator();
		while (it.hasNext())
		{
			Map.Entry<String, Integer> kv
			= (Map.Entry<String, Integer>)it.next();

			oList.add(new DateIntPair(getDateForString(kv.getKey()),
					kv.getValue()));
		}
		return oList;		
	}

	
	public boolean doesSortedListHaveDate(SortedSet<DateIntPair> sSet, Date current)
	{
		Calendar cal = Calendar.getInstance();
		cal.setTime(current);
		int cyear = cal.get(Calendar.YEAR);
		int cmonth = cal.get(Calendar.MONTH);
		int cday = cal.get(Calendar.DAY_OF_MONTH);	
		Iterator<DateIntPair> it = sSet.iterator();
		while (it.hasNext())
		{
			DateIntPair p = it.next();
			Date d = p.getKey();
			cal.setTime(d);
			int year = cal.get(Calendar.YEAR);
			int month = cal.get(Calendar.MONTH);
			int day = cal.get(Calendar.DAY_OF_MONTH);
			if (cday == day && cmonth == month && cyear == year)
				return true;
		}
		return false;
	}

	public SortedSet<Date> getSortedSetOfDates(SortedSet<DateIntPair> sset)
	{
		SortedSet<Date> nSet = new TreeSet<Date>();
		Iterator iter = sset.iterator();
		while (iter.hasNext())
		{
			DateIntPair p = (DateIntPair)iter.next();
			nSet.add(p.getKey());
		}
		return nSet;
	}

	public SortedSet<DateIntPair> getMediDataInOrder() {
		SortedSet<DateIntPair> oList = getMediDataInOrderInternal();
		SortedSet<Date> dList = getSortedSetOfDates(oList);

		if (dList.size() == 0)
			return oList;

		Date first  = dList.first();
		Date last = dList.last();

		if (first == last)
			return oList; 

		Calendar cal = Calendar.getInstance();
		cal.setTime(first);
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH);
		int day = cal.get(Calendar.DAY_OF_MONTH);

		Calendar cl = GregorianCalendar.getInstance();
		cl.set(year, month, day, 0, 0, 0);
		Date current = cl.getTime();
		do {
			if (doesSortedListHaveDate(oList, current) == false)
				oList.add(new DateIntPair(current, 0));

			cal.setTime(current);
			year = cal.get(Calendar.YEAR);
			month = cal.get(Calendar.MONTH);
			day = cal.get(Calendar.DAY_OF_MONTH);
			cl.set(year, month, day, 0, 0, 0);
			cl.add(Calendar.DATE, 1);
			current = cl.getTime();
			Log.i(logTag, "Current:" + current.toString());
		} while (current != null && current.before(last));
		return oList;
	}

	public SortedSet<DateIntPair> getPranDataInOrder() {
		SortedSet<DateIntPair> oList = getPranDataInOrderInternal();
		SortedSet<Date> dList = getSortedSetOfDates(oList);

		if (dList.size() == 0)
			return oList;

		Date first  = dList.first();
		Date last = dList.last();

		if (first == last)
			return oList; 

		Calendar cal = Calendar.getInstance();
		cal.setTime(first);
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH);
		int day = cal.get(Calendar.DAY_OF_MONTH);

		Calendar cl = GregorianCalendar.getInstance();
		cl.set(year, month, day, 0, 0, 0);
		Date current = cl.getTime();
		do {
			if (doesSortedListHaveDate(oList, current) == false)
				oList.add(new DateIntPair(current, 0));

			cal.setTime(current);
			year = cal.get(Calendar.YEAR);
			month = cal.get(Calendar.MONTH);
			day = cal.get(Calendar.DAY_OF_MONTH);
			cl.set(year, month, day, 0, 0, 0);
			cl.add(Calendar.DATE, 1);
			current = cl.getTime();
			Log.i(logTag, "Current:" + current.toString());
		} while (current != null && current.before(last));
		return oList;
	}
	
	public SortedSet<DateDoublePair> getRunDataInOrderInternal() {
		SortedSet<DateDoublePair> oList = new 
				TreeSet<DateDoublePair>();
		Iterator it = runMap.entrySet().iterator();
		while (it.hasNext())
		{
			Map.Entry<String, Double> kv
			= (Map.Entry<String, Double>)it.next();

			oList.add(new DateDoublePair(getDateForString(kv.getKey()),
					kv.getValue()));
		}
		return oList;		
	}

	public boolean doesSortedListHaveThisDate(SortedSet<DateDoublePair> sSet, Date current)
	{
		Calendar cal = Calendar.getInstance();
		cal.setTime(current);
		int cyear = cal.get(Calendar.YEAR);
		int cmonth = cal.get(Calendar.MONTH);
		int cday = cal.get(Calendar.DAY_OF_MONTH);	
		Iterator<DateDoublePair> it = sSet.iterator();
		while (it.hasNext())
		{
			DateDoublePair p = it.next();
			Date d = p.getKey();
			cal.setTime(d);
			int year = cal.get(Calendar.YEAR);
			int month = cal.get(Calendar.MONTH);
			int day = cal.get(Calendar.DAY_OF_MONTH);
			if (cday == day && cmonth == month && cyear == year)
				return true;
		}
		return false;
	}

	public SortedSet<Date> getASortedSetOfDates(SortedSet<DateDoublePair> sset)
	{
		SortedSet<Date> nSet = new TreeSet<Date>();
		Iterator iter = sset.iterator();
		while (iter.hasNext())
		{
			DateDoublePair p = (DateDoublePair)iter.next();
			nSet.add(p.getKey());
		}
		return nSet;
	}

	public SortedSet<DateDoublePair> getRunDataInOrder() {
		SortedSet<DateDoublePair> oList = getRunDataInOrderInternal();
		SortedSet<Date> dList = getASortedSetOfDates(oList);

		if (dList.size() == 0)
			return oList;

		Date first  = dList.first();
		Date last = dList.last();

		if (first == last)
			return oList; 

		Calendar cal = Calendar.getInstance();
		cal.setTime(first);
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH);
		int day = cal.get(Calendar.DAY_OF_MONTH);

		Calendar cl = GregorianCalendar.getInstance();
		cl.set(year, month, day, 0, 0, 0);
		Date current = cl.getTime();
		do {
			if (doesSortedListHaveThisDate(oList, current) == false)
				oList.add(new DateDoublePair(current, 0.0));

			cal.setTime(current);
			year = cal.get(Calendar.YEAR);
			month = cal.get(Calendar.MONTH);
			day = cal.get(Calendar.DAY_OF_MONTH);
			cl.set(year, month, day, 0, 0, 0);
			cl.add(Calendar.DATE, 1);
			current = cl.getTime();
			Log.i(logTag, "Current:" + current.toString());
		} while (current != null && current.before(last));
		return oList;
	}

	private Date getDateForYYMMDD(int yy, int mm, int dd) {
		Calendar cl = GregorianCalendar.getInstance();
		cl.set(yy, mm, dd, 0, 0, 0);
		Date current = cl.getTime();
		return current;
	}

	private String getDateAsStringKey(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH);
		int day = cal.get(Calendar.DAY_OF_MONTH);

		String strDate = String.valueOf(day) + "/" + String.valueOf(month) + "/" + String.valueOf(year);
		return strDate;
	}

	// THIS IS FUNDAMENTALLY UNSCALABLE!
	// Figure out a way to append to existing json
	// and not serialize everything on every append
	private boolean saveMapAsJsonToFile(HashMap mMap, String fileName) {
		GsonBuilder gsb = new GsonBuilder();
		Gson gson = gsb.create();
		String json = gson.toJson(mMap);
		Log.i(MediRunMainActivity.logTag, "JSON:" + json);
		try {
			FileOutputStream fos = mActivity.openFileOutput(fileName, Context.MODE_PRIVATE);
			fos.write(json.getBytes());
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private String getMapAsJsonString(HashMap mMap) {
		GsonBuilder gsb = new GsonBuilder();
		Gson gson = gsb.create();
		String json = gson.toJson(mMap);
		return json;
	}

	public boolean appendMediData(Date date, Integer mediMins, Integer pranaMins) {
		String strDate = getDateAsStringKey(date);
		mediMap.put(strDate, mediMins);
		pranMap.put(strDate, pranaMins);
		saveMapAsJsonToFile(mediMap, mediFileName);
		saveMapAsJsonToFile(pranMap, pranFileName);
		return true;
	}

	public boolean appendRunData(Date date, Double miles) {
		String strDate = getDateAsStringKey(date);
		runMap.put(strDate, miles);
		return saveMapAsJsonToFile(runMap, runFileName);
	}

	public void flushMediRunData() {
		saveMapAsJsonToFile(mediMap, mediFileName);
		saveMapAsJsonToFile(pranMap, pranFileName);
		saveMapAsJsonToFile(runMap, runFileName);
	}

	public static MediRunDataStore getInstance() {
		if (instance == null) {
			synchronized (MediRunDataStore.class) {
				if (instance == null)
					instance = new MediRunDataStore();
			}
		}
		return instance;
	}

	public void clearAllData() {
		if (mediMap != null)
			mediMap.clear();
		if (pranMap != null)
			pranMap.clear();
		if (runMap != null)
			runMap.clear();
		saveMapAsJsonToFile(mediMap, mediFileName);
		saveMapAsJsonToFile(pranMap, pranFileName);
		saveMapAsJsonToFile(runMap, runFileName);
	}

	public int getMediMinsForDate(int yy, int mm, int dd) {
		Date d = getDateForYYMMDD(yy, mm, dd);
		String key = getDateAsStringKey(d);
		if (mediMap.containsKey(key))
			return mediMap.get(key).intValue();
		return 0;
	}
	
	public int getPranMinsForDate(int yy, int mm, int dd) {
		Date d = getDateForYYMMDD(yy, mm, dd);
		String key = getDateAsStringKey(d);
		if (pranMap.containsKey(key))
			return pranMap.get(key).intValue();
		return 0;
	}


	public double getRunMilesForDate(int yy, int mm, int dd) {
		Date d = getDateForYYMMDD(yy, mm, dd);
		String key = getDateAsStringKey(d);
		if (runMap.containsKey(key))
			return runMap.get(key).doubleValue();
		return 0.0;
	}

	// Adapted from : 
	// https://github.com/stephendnicholas/Android-Apps/blob/master/Gmail%20Attacher/src/com/stephendnicholas/gmailattach/Utils.java
	private File createJsonFileInCacheUsingMap(Context context, String fileName, HashMap mMap) {
		File cFile = new File(context.getCacheDir() + File.separator + fileName);
		try {
			cFile.createNewFile();
			FileOutputStream oStream = new FileOutputStream(cFile);
			OutputStreamWriter osw = new OutputStreamWriter(oStream);
			PrintWriter pw = new PrintWriter(osw);

			String jsonStr = getMapAsJsonString(mMap);
			pw.print(jsonStr);

			pw.flush();
			pw.close();
		}
		catch (IOException e) {
			Log.i(logTag, "IOException thrown");
		}
		return cFile;
	}

	// Adapted from 
	// http://stackoverflow.com/questions/12986019/how-to-save-relative-layout-as-a-image
	private void createPNGForGraph(Context context, String graphPNGFileName, View rLayout) {
		rLayout.setDrawingCacheEnabled(true);
		Bitmap bitmap = rLayout.getDrawingCache();
		File file = new File(context.getCacheDir() + File.separator + graphPNGFileName);
		try
		{
			file.createNewFile();

			FileOutputStream ostream = new FileOutputStream(file);
			bitmap.compress(CompressFormat.PNG, 10, ostream);                                        
			ostream.close();
			rLayout.invalidate();                           
		} 
		catch (Exception e) 
		{ 
			e.printStackTrace();
		}
		finally
		{
			rLayout.setDrawingCacheEnabled(false);                          
		}
	}

	public void prepareDataForEmail(Context context, View rLayout) {
		createJsonFileInCacheUsingMap(context, mediFileName, mediMap);
		createJsonFileInCacheUsingMap(context, pranFileName, pranMap);
		createJsonFileInCacheUsingMap(context, runFileName, runMap);
		createPNGForGraph(context, graphPNGFileName, rLayout);
	}

	// Adapted from: http://stackoverflow.com/questions/9587559/android-intent-send-an-email-with-attachment
	public void email (Context context, String emailTo, String emailCC, 
			String subject, String emailText)
	{
		try {

			final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND_MULTIPLE);
			emailIntent.setType("text/plain");
			emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, 
					new String[]{emailTo});
			emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
			emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, emailText);

			ArrayList<Uri> uris = new ArrayList<Uri>();

			Uri mediUri = Uri.parse("content://" + MediRunContentProvider.auth + "/" + mediFileName);
			uris.add(mediUri);
			Uri pranUri = Uri.parse("content://" + MediRunContentProvider.auth + "/" + pranFileName);
			uris.add(pranUri);
			Uri runUri = Uri.parse("content://" + MediRunContentProvider.auth + "/" + runFileName);
			uris.add(runUri);
			Uri graphUri = Uri.parse("content://" + MediRunContentProvider.auth + "/" + graphPNGFileName);
			uris.add(graphUri);
			emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
			context.startActivity(emailIntent);
		}
		catch (ActivityNotFoundException ae) {
			Log.i(logTag, "Did not send email! No email activity found");
		}
	}

	final class DateIntPair implements Map.Entry<Date, Integer>, Comparable<DateIntPair> {
		private final Date key;
		private Integer value;

		public DateIntPair(Date key, Integer value) {
			this.key = key;
			this.value = value;
		}

		@Override
		public Date getKey() {
			return key;
		}

		@Override
		public Integer getValue() {
			return value;
		}

		@Override
		public Integer setValue(Integer value) {
			Integer old = this.value;
			this.value = value;
			return old;
		}

		@Override
		public int compareTo(DateIntPair another) {
			if (key.getTime() < another.getKey().getTime())
				return 1;
			else if (key.getTime() > another.getKey().getTime())
				return -1;
			else
				return 0;
		}
	}

	final class DateDoublePair implements Map.Entry<Date, Double>, Comparable<DateDoublePair> {
		private final Date key;
		private Double value;

		public DateDoublePair(Date key, Double value) {
			this.key = key;
			this.value = value;
		}

		@Override
		public Date getKey() {
			return key;
		}

		@Override
		public Double getValue() {
			return value;
		}

		@Override
		public Double setValue(Double value) {
			Double old = this.value;
			this.value = value;
			return old;
		}

		@Override
		public int compareTo(DateDoublePair another) {
			if (key.getTime() < another.getKey().getTime())
				return 1;
			else if (key.getTime() > another.getKey().getTime())
				return -1;
			else
				return 0;
		}
	}


}
