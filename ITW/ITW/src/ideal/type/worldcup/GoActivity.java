package ideal.type.worldcup;

import ideal.type.worldcup.inc.ITWApplication;
import ideal.type.worldcup.lib.GetUrlImageCacheFile;
import ideal.type.worldcup.lib.NetworkITW;
import ideal.type.worldcup.sax.QuestionListItem;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

public class GoActivity extends Activity implements OnClickListener {

	// Default Variables
	private Intent intent;
	private AdView adView;
	private ProgressDialog pd = null;
	private ArrayList<QuestionListItem> qOriginList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_go);
	}

	@Override
	protected void onResume() {
		super.onResume();
		pd = new ProgressDialog(this);
		initAd(); // 광고 보여주기
		initEvent(); // 이벤트 생성

		// Question 데이터 받아오기
		getQuestionList();
	}

	// 질문 데이터를 받아옴
	Handler goHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			String c = msg.getData().getString("c");
			if (c.equals(ITWApplication.getQuestionList)) {
				ArrayList<QuestionListItem> qList = msg.getData().getParcelableArrayList("qList");
				if (qList == null) {
					qList = new ArrayList<QuestionListItem>();
					return;
				}

				// Setting Question Item Select Type
				ArrayList<String> tSpinAl = new ArrayList<String>();
				tSpinAl.add(getString(R.string.type_all));
				tSpinAl.add(getString(R.string.type_public));
				tSpinAl.add(getString(R.string.type_friend));
				tSpinAl.add(getString(R.string.type_me));
				ArrayAdapter<String> aadapter = new ArrayAdapter<String>(GoActivity.this, android.R.layout.simple_spinner_item, tSpinAl);
				aadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				Spinner tSpinner = (Spinner) findViewById(R.id.go_typeSpinner);
				tSpinner.setPrompt(getString(R.string.select_your_question));
				tSpinner.setAdapter(aadapter);
				tSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> av, View v, int pos, long arg3) {
						ArrayList<QuestionListItem> qList = new ArrayList<QuestionListItem>();
						if (pos == 0) {
							qList = qOriginList;
						} else {
							QuestionListItem item = null;
							for (int i = 0; i < qOriginList.size(); i++) {
								item = qOriginList.get(i);
								if (pos == 1 && item.qType == 2)
									qList.add(item);
								else if (pos == 2 && ITWApplication.FUIDS != null) {
									String[] fUids = ITWApplication.FUIDS.split("\\,");
									if (fUids != null) {
										for (int j = 0; j < fUids.length; j++) {
											if (fUids[j].equals("" + item.mUid))
												qList.add(item);
										}
									}
								} else if (pos == 3 && ITWApplication.USERID != null && ITWApplication.USERID.equals("" + item.mUid)) {
									qList.add(item);
								}
							}
						}
						setMainQuestionUI(qList);
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {
					}
				});

				// Setting Question List
				qOriginList = qList;
				setMainQuestionUI(qList);
				if (pd != null) {
					pd.dismiss();
					pd = null;
				}
			}
		}
	};

	private void setMainQuestionUI(ArrayList<QuestionListItem> qList) {
		if (qList == null || qList.size() == 0) qList = new ArrayList<QuestionListItem>();
		ListView mqrLv = (ListView) findViewById(R.id.go_qlv);
		mqrLv.setAdapter(new QuestionListBaseAdapter(this, qList));
		mqrLv.setDivider(null);
		mqrLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> pv, View v, int pos, long arg3) {
				TextView titleTxt = (TextView) v.findViewById(R.id.mqr_title);
				String[] str=titleTxt.getTag().toString().split("\\^");
				String qUid = str[0];
				String qRound= str[1];
				String qImageUrl = str[2]; //질문 이미지
				String qTitle = titleTxt.getText().toString();
				ImageView tmpIv = (ImageView) findViewById(R.id.go_qimg);
				tmpIv.setImageBitmap(null);
				adView.loadAd(new AdRequest()); // ad reload
				if (qUid!=null) {
					((TextView) findViewById(R.id.go_qTitle)).setText(qTitle);
					((TextView) findViewById(R.id.go_qTitle)).setTag(qRound);
					tmpIv.setTag(qUid);
					BitmapHandlerMain bhm = new BitmapHandlerMain(qImageUrl, tmpIv, true, Integer.parseInt(qUid));
					bhm.execute();
				}
			}
		});
	}

	class QuestionListBaseAdapter extends BaseAdapter {
		private Context c;
		private ArrayList<QuestionListItem> qList;
		private LayoutInflater inflater;

		public QuestionListBaseAdapter(Context c, ArrayList<QuestionListItem> qList) {
			this.c = c;
			this.inflater = LayoutInflater.from(c);
			this.qList = qList;
		}

		@Override
		public int getCount() {
			return qList.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder vh=null;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.main_question_row, null);
				vh= new ViewHolder();
				vh.qTitle= (TextView)convertView.findViewById(R.id.mqr_title);
				vh.mName = (TextView)convertView.findViewById(R.id.mqr_name);
				vh.mProfileIv= (ImageView)convertView.findViewById(R.id.mqr_profile);
				convertView.setTag(vh);
			}
			vh=(ViewHolder)convertView.getTag();// 껌데기 재 생성
			QuestionListItem qi= this.qList.get(position);
			vh.qTitle.setText(qi.qTitle);
			vh.qTitle.setTag(qi.qUid+"^"+qi.qRound+"^"+qi.qImageUrl);
			vh.mName.setText(qi.mName);
			vh.mProfileIv.setImageResource(R.drawable.worldmap);
			if (qi.uesrImg != null) {
				vh.mProfileIv.setTag(position); //ImageView에 position 을 태깅시킴
				BitmapHandlerMain bhm = new BitmapHandlerMain(qi.uesrImg, vh.mProfileIv, true, position);
				bhm.execute();
			}
			return convertView;
		}
	}

	class ViewHolder {
		public TextView qTitle, mName;
		public ImageView mProfileIv, tmpIv;
	}

	class BitmapHandlerMain extends AsyncTask<String, Void, String> {
		private String imgUrl;
		private ImageView iv;
		private Bitmap bmp;
		private GetUrlImageCacheFile gcf;
		private boolean cacheFlag;
		private int pos;
		
		public BitmapHandlerMain(String imgUrl, ImageView iv, boolean cacheFlag, int pos) {
			this.imgUrl = imgUrl;
			this.iv= iv;
			this.cacheFlag = cacheFlag;
			this.pos=pos;
		}

		@Override
		protected String doInBackground(String... params) {
			gcf = new GetUrlImageCacheFile(imgUrl, cacheFlag);
			bmp = gcf.getBmp();
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (bmp != null){
				int tagPos=Integer.parseInt(iv.getTag().toString());
				if(tagPos==pos){
					iv.setImageBitmap(bmp);
					Animation myFadeInAnimation = AnimationUtils.loadAnimation(GoActivity.this, R.anim.anim_imgv);
					iv.startAnimation(myFadeInAnimation);
				}
			}
		}
	}

	private void getQuestionList() {
		pd = null;
		pd = new ProgressDialog(this);
		pd.setTitle(getString(R.string.question_loading));
		pd.setCancelable(false);
		pd.show();
		// 질문 데이터를 가지고 온다
		HashMap map = new HashMap<String, String>();
		map.put("c", ITWApplication.getQuestionList);
		map.put("mUid", ITWApplication.USERID);
		map.put("mFriendsUids", ITWApplication.FUIDS);
		NetworkITW nt = new NetworkITW(map, goHandler);
		nt.execute();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		if (pd != null)
			pd = null;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (adView != null)
			adView.destroy();
	}

	private void initEvent() {
		((ImageView) findViewById(R.id.go_cancleIv)).setOnClickListener(this);
		((ImageView) findViewById(R.id.go_qimg)).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.go_cancleIv:
			onBackPressed();
			break;
		case R.id.go_qimg:
			ImageView iv = (ImageView) findViewById(R.id.go_qimg);
			if (iv.getTag() == null)
				Toast.makeText(this, getString(R.string.question_any_question), Toast.LENGTH_SHORT).show();
			else {
				String qUid = iv.getTag().toString();
				String qTitle = ((TextView) findViewById(R.id.go_qTitle)).getText().toString();
				String qRound = ((TextView) findViewById(R.id.go_qTitle)).getTag().toString();
				if (qUid == null || qTitle == null) {
					Toast.makeText(this, getString(R.string.question_not_good), Toast.LENGTH_SHORT).show();
					break;
				}
				intent = new Intent(GoActivity.this, GoViewActivity.class);
				intent.putExtra("qUid", ""+qUid);
				intent.putExtra("qTitle", ""+qTitle);
				intent.putExtra("qRound", ""+qRound);
				startActivity(intent);
				overridePendingTransition(R.anim.anim_action_in, R.anim.anim_main_out);
				finish();
			}
			break;
		}
	}

	private void initAd() {
		adView = new AdView(this, AdSize.BANNER, ITWApplication.MY_AD_UNIT_ID);
		LinearLayout layout = (LinearLayout) findViewById(R.id.go_btmLl);
		adView.loadAd(new AdRequest());
		layout.addView(adView);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		intent = new Intent(this, MainActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.anim_main_in, R.anim.anim_action_out);
		finish();
	}
}
