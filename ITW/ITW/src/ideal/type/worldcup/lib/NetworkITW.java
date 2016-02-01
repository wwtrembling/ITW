package ideal.type.worldcup.lib;

import ideal.type.worldcup.inc.ITWApplication;
import ideal.type.worldcup.sax.CommentItem;
import ideal.type.worldcup.sax.ImagesItem;
import ideal.type.worldcup.sax.QuestionListItem;
import ideal.type.worldcup.sax.QuestionViewItem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/*
 * 값을 받아서 POST 전송/응답함
 * */
public class NetworkITW extends AsyncTask<String, Void, String> {
	private HashMap map;
	private Handler handler;
	private StringBuffer sb;
	private StringBuilder buff;
	private String command;

	public NetworkITW(HashMap map, Handler handler) {
		map.put("deviceID", ITWApplication.DEVICE_ID);
		this.map = map;
		this.handler = handler;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		sb = new StringBuffer();
		Set<Entry<String, String>> set = map.entrySet();
		Iterator<Entry<String, String>> it = set.iterator();
		while (it.hasNext()) {
			Map.Entry<String, String> e = (Map.Entry<String, String>) it.next();
			sb.append(e.getKey()).append("=").append(e.getValue()).append("&");
			if(e.getKey().equals("c")) this.command = e.getValue();
		}
	}

	@Override
	protected String doInBackground(String... params) {
		try {
			URL url = new URL(ITWApplication.ACTION_URL);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDefaultUseCaches(false);
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

			PrintWriter pw = new PrintWriter(new OutputStreamWriter(conn.getOutputStream(), "UTF-8"));
			pw.write(sb.toString());
			pw.flush();

			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			buff = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null) {
				buff.append(line);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void parseXml( String inStr, Bundle bundle) {
		try {
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlPullParser parser = factory.newPullParser();
			parser.setInput(new StringReader(inStr));
			int parserEvent = parser.getEventType();
			String tagName = null;
			while (parserEvent != XmlPullParser.END_DOCUMENT) {
				switch (parserEvent) {
				case XmlPullParser.START_DOCUMENT:
					break;
				case XmlPullParser.START_TAG:
					tagName = parser.getName();
					break;
				case XmlPullParser.END_TAG:
					break;
				case XmlPullParser.TEXT:
					bundle.putCharSequence(tagName, parser.getText());
					break;
				}
				parserEvent = parser.next();
			}
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void parseQuestionListXml(String inStr, Bundle bundle) {
		try {
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlPullParser parser = factory.newPullParser();
			parser.setInput(new StringReader(inStr));
			int parserEvent = parser.getEventType();
			String tagName = null;
			ArrayList<QuestionListItem> arrQi = null;
			QuestionListItem qi = null;
			while (parserEvent != XmlPullParser.END_DOCUMENT) {
				switch (parserEvent) {
				case XmlPullParser.START_DOCUMENT:
					break;
				case XmlPullParser.START_TAG:
					tagName = parser.getName();
					if (tagName.equals("lists")) {
						arrQi = new ArrayList<QuestionListItem>();
					} else if (tagName.equals("row")) {
						qi = new QuestionListItem();
					}
					break;
				case XmlPullParser.END_TAG:
					tagName = parser.getName();
					if (tagName.equals("lists")) {
						bundle.putParcelableArrayList("qList", arrQi);
					} else if (tagName.equals("row")) {
						arrQi.add(qi);
						qi = null;
					}
					break;
				case XmlPullParser.TEXT:
					String val = parser.getText();
					if (tagName.equals("status"))
						bundle.putCharSequence(tagName, val);
					else if (tagName.equals("mUid"))
						qi.mUid = Integer.parseInt(val);
					else if (tagName.equals("uesrImg"))
						qi.uesrImg = val;
					else if (tagName.equals("mName"))
						qi.mName = val;
					else if (tagName.equals("qUid"))
						qi.qUid = Integer.parseInt(val);
					else if (tagName.equals("qType"))
						qi.qType = Integer.parseInt(val);
					else if (tagName.equals("qTitle"))
						qi.qTitle = val;
					else if (tagName.equals("qRound"))
						qi.qRound = Integer.parseInt(val);
					else if (tagName.equals("qHits"))
						qi.qHits = Integer.parseInt(val);
					else if (tagName.equals("qImageUrl"))
						qi.qImageUrl = val;
					break;
				}
				parserEvent = parser.next();
			}
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void parseQuestionDataXml(String inStr, Bundle bundle) {
		try {
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlPullParser parser = factory.newPullParser();
			parser.setInput(new StringReader(inStr));
			int parserEvent = parser.getEventType();
			String tagName = null;
			ArrayList<QuestionViewItem> arrQi = null;
			QuestionViewItem qi = null;
			while (parserEvent != XmlPullParser.END_DOCUMENT) {
				switch (parserEvent) {
				case XmlPullParser.START_DOCUMENT:
					break;
				case XmlPullParser.START_TAG:
					tagName = parser.getName();
					if (tagName.equals("lists")) {
						arrQi = new ArrayList<QuestionViewItem>();
					} else if (tagName.equals("row")) {
						qi = new QuestionViewItem();
					}
					break;
				case XmlPullParser.END_TAG:
					tagName = parser.getName();
					if (tagName.equals("lists")) {
						bundle.putParcelableArrayList("qData", arrQi);
					} else if (tagName.equals("row")) {
						arrQi.add(qi);
						qi = null;
					}
					break;
				case XmlPullParser.TEXT:
					String val = parser.getText();
					if (tagName.equals("status"))
						bundle.putCharSequence(tagName, val);
					else if (tagName.equals("iUid"))
						qi.iUid = Integer.parseInt(val);
					else if (tagName.equals("iHits"))
						qi.iHits = Integer.parseInt(val);
					else if (tagName.equals("cCount"))
						qi.cCount = Integer.parseInt(val);
					else if (tagName.equals("iUrl"))
						qi.iUrl = val;
					break;
				}
				parserEvent = parser.next();
			}
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void parseCommentResultXml(String inStr, Bundle bundle){
		try {
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlPullParser parser = factory.newPullParser();
			parser.setInput(new StringReader(inStr));
			int parserEvent = parser.getEventType();
			String tagName = null;
			ArrayList<CommentItem> arrQi = null;
			CommentItem qi = null;
			while (parserEvent != XmlPullParser.END_DOCUMENT) {
				switch (parserEvent) {
				case XmlPullParser.START_DOCUMENT:
					break;
				case XmlPullParser.START_TAG:
					tagName = parser.getName();
					if (tagName.equals("lists")) {
						arrQi = new ArrayList<CommentItem>();
					} else if (tagName.equals("row")) {
						qi = new CommentItem();
					}
					break;
				case XmlPullParser.END_TAG:
					tagName = parser.getName();
					if (tagName.equals("lists")) {
						bundle.putParcelableArrayList("cList", arrQi);
					} else if (tagName.equals("row")) {
						arrQi.add(qi);
						qi = null;
					}
					break;
				case XmlPullParser.TEXT:
					String val = parser.getText();
					if (tagName.equals("status"))
						bundle.putCharSequence(tagName, val);
					else if (tagName.equals("cUid"))
						qi.cUid = Integer.parseInt(val);
					else if (tagName.equals("mUid"))
						qi.mUid = Integer.parseInt(val);
					else if (tagName.equals("comment"))
						qi.comment = val;
					else if (tagName.equals("mName"))
						qi.mName = val;
					break;
				}
				parserEvent = parser.next();
			}
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void parseUserImages(String inStr, Bundle bundle){
		try {
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlPullParser parser = factory.newPullParser();
			parser.setInput(new StringReader(inStr));
			int parserEvent = parser.getEventType();
			String tagName = null;
			ArrayList<ImagesItem> arrQi = null;
			ImagesItem qi = null;
			while (parserEvent != XmlPullParser.END_DOCUMENT) {
				switch (parserEvent) {
				case XmlPullParser.START_DOCUMENT:
					break;
				case XmlPullParser.START_TAG:
					tagName = parser.getName();
					if (tagName.equals("lists")) {
						arrQi = new ArrayList<ImagesItem>();
					} else if (tagName.equals("row")) {
						qi = new ImagesItem();
					}
					break;
				case XmlPullParser.END_TAG:
					tagName = parser.getName();
					if (tagName.equals("lists")) {
						bundle.putParcelableArrayList("iList", arrQi);
					} else if (tagName.equals("row")) {
						arrQi.add(qi);
						qi = null;
					}
					break;
				case XmlPullParser.TEXT:
					String val = parser.getText();
					if (tagName.equals("status"))
						bundle.putCharSequence(tagName, val);
					else if (tagName.equals("iUid"))
						qi.iUid = Integer.parseInt(val);
					else if (tagName.equals("iUrl"))
						qi.iUrl = val;
					break;
				}
				parserEvent = parser.next();
			}
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		Bundle bundle = new Bundle();
		bundle.putCharSequence("c", command);
		
		String[] debugArr={};
		for(int i=0;i<debugArr.length;i++){
			if(command.equals(debugArr[i])) Log.e("pkch",debugArr[i]+" result >> \\n"+buff.toString()+"\\n\\n");
		}
		
		// ParseXml
		if (buff != null) {
			boolean flag = Pattern.matches("(.)*status(.)*", buff.toString());
			if (flag == true) {
				if (command.equals(ITWApplication.getQuestionList)) {
					parseQuestionListXml(buff.toString(), bundle);
				} else if (command.equals(ITWApplication.getQuestionData)) {
					parseQuestionDataXml(buff.toString(), bundle);
				}
				else if(command.equals(ITWApplication.getGoResult)){
					parseCommentResultXml(buff.toString(), bundle);
				}
				else if(command.equals(ITWApplication.getUserImages)){
					parseUserImages(buff.toString(), bundle);
				}
				else {
					parseXml(buff.toString(), bundle);
				}
			} else {
				Log.e("pkch", "error buffer :" + buff.toString());
			}
		}
		Message msg = new Message();
		msg.setData(bundle);
		handler.sendMessage(msg);
	}

}
