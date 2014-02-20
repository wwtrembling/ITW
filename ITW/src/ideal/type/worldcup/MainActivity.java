package ideal.type.worldcup;

import ideal.type.worldcup.inc.ITWApplication;
import ideal.type.worldcup.inc.SharedPreference;
import ideal.type.worldcup.lib.GetUrlImageCacheFile;
import ideal.type.worldcup.lib.NetworkITW;
import ideal.type.worldcup.sax.QuestionListItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Request;
import com.facebook.Request.GraphUserListCallback;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphUser;

public class MainActivity extends Activity {
	private boolean isResumed = false;
	private ProgressDialog pd = null;
	private Intent intent;
	private ArrayList<QuestionListItem> qList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
	}

	@Override
	protected void onResume() {
		super.onResume();
		isResumed = true;
		
		//############ 헷갈리지 말것..이하는 인증을 완료한 후에 갱신되는 데이터임. 인증 받을때는 session의 callback 을 사용하게 됨
		//세션데이터는 존재하고 사용자 정보도 있는 경우
		Session session= Session.getActiveSession();
		if(session!=null && ITWApplication.USERNAME!=null){ //인증을 받은 후에 화면갱신을 하여 질문 데이터를 새로 받아올 경우
			Button btn = (Button) findViewById(R.id.main_loginout);
			btn.setText("  " + ITWApplication.USERNAME + "  Log out");
			pd= new ProgressDialog(this);
			pd.setTitle(getString(R.string.loading_msg));
			pd.setCancelable(false);
			pd.show();
			getQuestionList();
		}
		//세션 데이터는 존재하지마 사용자 정보는 없음 > 이후 자동으로 callback 함수가 불리워 지면서 처리
		else if(session!=null && ITWApplication.USERNAME==null){ //인증을 받는 중임!!!
			
		}
		
		//############ 위와는 관계 없으나 onResume이되면서 동작함 세션 데이터가 부재함, 앱을 켰을때 여기로 홈(인증을 받았는지 않받았는지 모르는 상태)
		else{
			//자.. shared prefrerence 에 데이터가 있던 얘기는 session 함수가 있다는 예기지~~
			String mName=SharedPreference.getString(this, "mName");
			if(mName==null){
				pd= new ProgressDialog(this);
				pd.setCancelable(false);
				pd.setTitle(getString(R.string.loading_msg));
				pd.show();
				setUIbyAuth(false);
			}else{
				Session.openActiveSession(this, false, callback);
			}
		}
		initEvent();
	}

	// 로그인 UI 세팅
	private void setUIbyAuth(boolean flag) {
		if (flag == true) {
			initUserData(); // 사용자 정보 받아오고 나서 질문 받아오기
		} else {
			Button btn = (Button) findViewById(R.id.main_loginout);
			btn.setText("  Log in");
			Drawable icon = getApplicationContext().getResources().getDrawable(R.drawable.icn_facebook);
			icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
			btn.setCompoundDrawables(icon, null, null, null);
			getQuestionList(); // 질문 받아오기
		}
	}

	@SuppressWarnings("deprecation")
	private Session.StatusCallback callback = new Session.StatusCallback() {
		@Override
		public void call(Session session, SessionState state, Exception exception) {
			Log.e("pkch","called!");
			if (isResumed) {
				if (state.isOpened()) { // show authorized UI
					Log.e("pkch", "onSessionStateChanges state.isOpened()");
					if (pd == null) {
						pd = new ProgressDialog(MainActivity.this);
						pd.setTitle(getString(R.string.loading_msg));
						pd.setCancelable(false);
						pd.show();
					}
					Request.executeMeRequestAsync(session, new Request.GraphUserCallback() {
						@Override
						public void onCompleted(GraphUser user, Response response) {
							if (user != null) {
								Log.e("pkch","onCompleted :"+user.getName());
								Button btn = (Button) findViewById(R.id.main_loginout);
								btn.setText("  " + user.getName() + "  Log out");
								ITWApplication.USERFID = user.getId();
								ITWApplication.USERNAME = user.getName();
								setUIbyAuth(true);
							}
							else {
								Log.e("pkch","onCompleted : No Response");
								clearLocalVariables();
								setUIbyAuth(false);
							}
						}
					});
				} else if (state.isClosed()) { // show non-authorized UI
					Log.e("pkch", "onSessionStateChanges state.isClosed()");
					clearLocalVariables();
					setUIbyAuth(false);
				}
			}
		}
	};

	@Override
	protected void onPause() {
		super.onPause();
		isResumed = false;
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (pd != null) {
			pd.dismiss();
			pd = null;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
	}

	private void initEvent() {
		((Button) findViewById(R.id.main_loginout)).setOnClickListener(btnListener);
		((Button) findViewById(R.id.main_go)).setOnClickListener(btnListener);
		((Button) findViewById(R.id.main_rank)).setOnClickListener(btnListener);
		((Button) findViewById(R.id.main_question)).setOnClickListener(btnListener);
	}

	private void setMainQuestionUI() {
		if (qList == null || qList.size() == 0) return;
		ListView mqrLv = (ListView) findViewById(R.id.main_lv);
		mqrLv.setAdapter(new QuestionListBaseAdapter(this));
		mqrLv.setDivider(null);
		mqrLv.invalidate();
		mqrLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> pv, View v, int pos, long arg3) {
				intent = new Intent(MainActivity.this, GoViewActivity.class);
				ViewHolder vh = (ViewHolder) v.getTag();
				String[] str = vh.qTitle.getTag().toString().split("\\^");
				intent.putExtra("qUid", "" + str[0]);
				intent.putExtra("qRound", "" + str[1]);
				intent.putExtra("qTitle", "" + vh.qTitle.getText());
				startActivity(intent);
				overridePendingTransition(R.anim.anim_action_in, R.anim.anim_main_out);
			}
		});
	}

	// 사용자 데이터 세팅
	@SuppressWarnings("deprecation")
	private void initUserData() {
		/*
		 * 1. 사용자 이름/아이디/프로필사진 2. 사용자 사진 등록 3. 친구들 이름/아이디/프로필사진
		 */
		// Log.e("pkch", "Main initUserData");
		Session session = Session.getActiveSession();
		Request.executeMyFriendsRequestAsync(session, new GraphUserListCallback() {
			@Override
			public void onCompleted(List<GraphUser> users, Response response) {
				GraphObject graphObject = response.getGraphObject();
				if (graphObject != null) {
					JSONObject jsonObject = graphObject.getInnerJSONObject();
					try {
						JSONArray array = jsonObject.getJSONArray("data");
						StringBuffer sb = new StringBuffer();
						for (int i = 0; i < array.length(); i++) {
							JSONObject object = (JSONObject) array.get(i);
							sb.append(object.getString("id") + ",");
						}
						SharedPreference.setString(MainActivity.this, "friendmIds", sb.toString()); // 친구
						Log.e("pkch", "Main initUserData Friend Data");
						if (pd == null) {
							pd = new ProgressDialog(MainActivity.this);
							pd.setCancelable(false);
							pd.show();
						}
						// 친구들 데이터를 가지고 온다.
						HashMap map = new HashMap<String, String>();
						map.put("c", ITWApplication.chkUserProfile);
						map.put("mId", ITWApplication.USERFID);
						map.put("mName", ITWApplication.USERNAME);
						map.put("friendsId", sb.toString());
						NetworkITW nt = new NetworkITW(map, mainHandler);
						nt.execute();
					} catch (JSONException e) {
						e.printStackTrace();
					}
				} else {
					Toast.makeText(MainActivity.this, getString(R.string.friend_loading_fail), Toast.LENGTH_SHORT).show();
					clearLocalVariables();
					setUIbyAuth(false);
					if (pd != null) {
						pd.dismiss();
						pd = null;
					}
				}
			}
		});
	}

	Handler mainHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			String c = (String) msg.getData().get("c");
			String status = msg.getData().getString("status");
			if (status == null || !status.equals("1")) {
				if (pd != null) {
					pd.dismiss();
					pd = null;
				}
				return;
			}

			if (c.equals(ITWApplication.chkUserProfile)) {
				Log.e("pkch", "Main initUserData Friend Data finished!");
				String mUid, mFriendsUids, mName;
				mUid = (String) msg.getData().getString("mUid");
				mName = (String) msg.getData().getString("mName");
				mFriendsUids = (String) msg.getData().getString("mFriendsUids");
				SharedPreference.setString(MainActivity.this, "mUid", mUid);
				SharedPreference.setString(MainActivity.this, "mName", mName);
				SharedPreference.setString(MainActivity.this, "friendsmUids", mFriendsUids);
				ITWApplication.USERID = mUid;
				ITWApplication.FUIDS = mFriendsUids;
				getQuestionList();
			} else if (c.equals(ITWApplication.getQuestionList)) {
				qList = msg.getData().getParcelableArrayList("qList");
				setMainQuestionUI();
				if (pd != null) {
					pd.dismiss();
					pd = null;
				}
				testMethod();
			}
		}
	};

	// TEST
	private void testMethod() {
		
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
			this.iv = iv;
			this.cacheFlag = cacheFlag;
			this.pos = pos;
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
			if (bmp != null) {
				int tagPos = Integer.parseInt(iv.getTag().toString());
				if (tagPos == pos) {
					iv.setImageBitmap(bmp);
					Animation myFadeInAnimation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_imgv);
					iv.startAnimation(myFadeInAnimation);
					qList.get(pos).userProfileImg=bmp;
				}
			}
		}
	}
	
	private void getQuestionList() {
		// 질문 데이터를 가지고 온다
		HashMap map = new HashMap<String, String>();
		map.put("c", ITWApplication.getQuestionList);
		map.put("mUid", ITWApplication.USERID);
		map.put("mFriendsUids", ITWApplication.FUIDS);
		NetworkITW nt = new NetworkITW(map, mainHandler);
		nt.execute();
	}

	class QuestionListBaseAdapter extends BaseAdapter {
		private Context c;
		public QuestionListBaseAdapter(Context c) {
			this.c = c;
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
			ViewHolder vh = null;
			// 껍데기가 없을 경우에는 껍데기 생성
			if (convertView == null) {
				convertView = View.inflate(c, R.layout.main_question_row, null);
				vh = new ViewHolder();
				vh.qTitle = (TextView) convertView.findViewById(R.id.mqr_title);
				vh.mName = (TextView) convertView.findViewById(R.id.mqr_name);
				vh.mProfileIv = (ImageView) convertView.findViewById(R.id.mqr_profile);
				convertView.setTag(vh);
			}
			vh = (ViewHolder) convertView.getTag();// 껌데기 재 생성
			QuestionListItem qi = qList.get(position);
			vh.qTitle.setText(qi.qTitle);
			vh.qTitle.setTag(qi.qUid + "^" + qi.qRound);
			vh.mName.setText(qi.mName);
			if (qList.get(position).uesrImg != null) {
				if(qList.get(position).userProfileImg==null){
					vh.mProfileIv.setImageResource(R.drawable.worldmap);
					vh.mProfileIv.setTag(position); // ImageView에 position 을 태깅시킴
					BitmapHandlerMain bhm = new BitmapHandlerMain(qList.get(position).uesrImg, vh.mProfileIv, true, position);
					bhm.execute();
				}else{
					vh.mProfileIv.setImageBitmap(qList.get(position).userProfileImg);
				}
			}
			return convertView;
		}
	}

	class ViewHolder {
		public TextView qTitle, mName;
		public ImageView mProfileIv, tmpIv;
	}

	View.OnClickListener btnListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.main_go:
				intent = new Intent(MainActivity.this, GoActivity.class);
				startActivity(intent);
				overridePendingTransition(R.anim.anim_action_in, R.anim.anim_main_out);
				break;
			case R.id.main_rank:
				intent = new Intent(MainActivity.this, RankActivity.class);
				startActivity(intent);
				overridePendingTransition(R.anim.anim_action_in, R.anim.anim_main_out);
				break;
			case R.id.main_question:
				if (ITWApplication.USERNAME != null) {
					intent = new Intent(MainActivity.this, QuestionActivity.class);
					startActivity(intent);
					overridePendingTransition(R.anim.anim_action_in, R.anim.anim_main_out);
				} else {
					Toast.makeText(MainActivity.this, getString(R.string.need_to_fb_login), Toast.LENGTH_SHORT).show();
				}
				break;
			case R.id.main_loginout:
				Session session = Session.getActiveSession();
				if (session != null && session.isOpened()) { // try to logout
					Log.e("pkch", " try to logout");
					if (pd == null) {
						pd = new ProgressDialog(MainActivity.this);
						pd.setTitle(getString(R.string.loading_msg));
						pd.setCancelable(false);
						pd.show();
					}
					clearLocalVariables();
					setUIbyAuth(false);
				} else { // try to login
					Log.e("pkch", " try to login");
					Session.openActiveSession(MainActivity.this, true, callback);
				}
				break;
			}
		}
	};

	// SharedPreference 데이터 삭제 및 Application 데이터 갱신
	private void clearLocalVariables() {
		SharedPreference.remove(this, "mId");
		SharedPreference.remove(this, "friendmIds");
		SharedPreference.remove(this, "mUid");
		SharedPreference.remove(this, "mName");
		SharedPreference.remove(this, "friendsmUids");

		ITWApplication.USERFID = null;
		ITWApplication.USERID = null;
		ITWApplication.USERNAME = null;
		ITWApplication.FUIDS = null;
		ITWApplication.FFIDS = null;
		Session session = Session.getActiveSession();
		if (session != null)
			session.closeAndClearTokenInformation();
	}
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
	}

	@Override
	public void finish() {
		super.finish();
	}
}
