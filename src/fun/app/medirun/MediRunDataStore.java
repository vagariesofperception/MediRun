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
import java.lang.reflect.Type;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

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
	private static final String logTag = "MediRunDataStore";
	private static volatile MediRunDataStore instance = null;
	private File mediFile;

	private HashMap<String, Integer> mediMap;
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
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		catch (ParseException e) {
			e.printStackTrace();
		}
		if (mediMap == null)
			mediMap = new HashMap<String, Integer>();
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

	public SortedSet<Pair> getMediDataInOrderInternal() {
		SortedSet<Pair> oList = new 
				TreeSet<Pair>();
		Iterator it = mediMap.entrySet().iterator();
		while (it.hasNext())
		{
			Map.Entry<String, Integer> kv
			= (Map.Entry<String, Integer>)it.next();

			oList.add(new Pair(getDateForString(kv.getKey()),
					kv.getValue()));
			it.remove();
		}
		return oList;		
	}

	public boolean doesSortedListHaveDate(SortedSet<Pair> sSet, Date current)
	{
		Calendar cal = Calendar.getInstance();
		cal.setTime(current);
		int cyear = cal.get(Calendar.YEAR);
		int cmonth = cal.get(Calendar.MONTH);
		int cday = cal.get(Calendar.DAY_OF_MONTH);	
		Iterator<Pair> it = sSet.iterator();
		while (it.hasNext())
		{
			Pair p = it.next();
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
	
	public SortedSet<Date> getSortedSetOfDates(SortedSet<Pair> sset)
	{
		SortedSet<Date> nSet = new TreeSet<Date>();
		Iterator iter = sset.iterator();
		while (iter.hasNext())
		{
			Pair p = (Pair)iter.next();
			nSet.add(p.getKey());
		}
		return nSet;
	}

	public SortedSet<Pair> getMediDataInOrder() {
		SortedSet<Pair> oList = getMediDataInOrderInternal();
		SortedSet<Date> dList = getSortedSetOfDates(oList);
		
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
				oList.add(new Pair(current, 0));

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

	public boolean appendMediData(Date date, Integer mins) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH);
		int day = cal.get(Calendar.DAY_OF_MONTH);

		String strDate = String.valueOf(day) + "/" + String.valueOf(month) + "/" + String.valueOf(year);
		mediMap.put(strDate, mins);
		// THIS IS FUNDAMENTALLY UNSCALABLE!
		// Figure out a way to append to existing json
		// and not serialize everything on every append
		GsonBuilder gsb = new GsonBuilder();
		//gsb.registerTypeAdapter(Date.class, new JsonDateSerializer());
		Gson gson = gsb.create();
		String json = gson.toJson(mediMap);
		Log.i(MediRunMainActivity.logTag, "JSON:" + json);
		try {
			FileOutputStream fos = mActivity.openFileOutput(mediFileName, Context.MODE_PRIVATE);
			fos.write(json.getBytes());
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
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

	final class Pair implements Map.Entry<Date, Integer>, Comparable<Pair> {
		private final Date key;
		private Integer value;

		public Pair(Date key, Integer value) {
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
		public int compareTo(Pair another) {
			if (key.getTime() < another.getKey().getTime())
				return 1;
			else if (key.getTime() > another.getKey().getTime())
				return -1;
			else
				return 0;
		}
	}


}
