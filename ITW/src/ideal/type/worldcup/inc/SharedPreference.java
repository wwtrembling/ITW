package ideal.type.worldcup.inc;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreference {

	private final static String prefKey = "idealTypeWorldcup_";

	private static SharedPreferences initSharedPreferences(Context context) {
		return ((Activity) context).getApplicationContext().getSharedPreferences(prefKey, Context.MODE_PRIVATE);
	}

	public static boolean getBoolean(Context context, String key,
			boolean defaultVal) {
		boolean data = false;
		SharedPreferences sp = initSharedPreferences(context);
		data = sp.getBoolean(key, defaultVal);
		return data;
	}

	public static boolean getBoolean(Context context, String key) {
		boolean data = false;
		SharedPreferences sp = initSharedPreferences(context);
		data = sp.getBoolean(key, false);
		return data;
	}

	public static String getString(Context context, String key) {
		String data = null;
		SharedPreferences sp = initSharedPreferences(context);
		data = sp.getString(key, null);
		return data;
	}

	public static String getString(Context context, String key,
			String defaultvalue) {
		String data = null;
		SharedPreferences sp = initSharedPreferences(context);
		data = sp.getString(key, defaultvalue);
		return data;
	}

	public static int getInt(Context context, String key) {
		int data = 0;
		SharedPreferences sp = initSharedPreferences(context);
		data = sp.getInt(key, 0);
		return data;
	}

	public static long getLong(Context context, String key) {
		long data = 0;
		SharedPreferences sp = initSharedPreferences(context);
		data = sp.getLong(key, 0);
		return data;
	}

	public static float getFloat(Context context, String key) {
		float data = 0;
		SharedPreferences sp = initSharedPreferences(context);
		data = sp.getFloat(key, 0);
		return data;
	}

	private static SharedPreferences.Editor getEditor(Context context) {
		SharedPreferences sp = initSharedPreferences(context);
		return sp.edit();
	}

	public static void setBoolean(Context context, String key, boolean value) {
		SharedPreferences.Editor editor = getEditor(context);
		editor.putBoolean(key, value);
		editor.commit();

	}

	public static void setString(Context context, String key, String value) {
		SharedPreferences.Editor editor = getEditor(context);
		editor.putString(key, value);
		editor.commit();

	}

	public static void setInt(Context context, String key, int value) {
		SharedPreferences.Editor editor = getEditor(context);
		editor.putInt(key, value);
		editor.commit();
	}

	public static void setLong(Context context, String key, long value) {
		SharedPreferences.Editor editor = getEditor(context);
		editor.putLong(key, value);
		editor.commit();
	}

	public static void setFloat(Context context, String key, float value) {
		SharedPreferences.Editor editor = getEditor(context);
		editor.putFloat(key, value);
		editor.commit();

	}

	public static void clear(Context context) {
		SharedPreferences.Editor editor = getEditor(context);

		editor.clear();
		editor.commit();
	}

	public static void remove(Context context, String key) {
		SharedPreferences.Editor editor = getEditor(context);
		editor.remove(key);
		editor.commit();
	}

}