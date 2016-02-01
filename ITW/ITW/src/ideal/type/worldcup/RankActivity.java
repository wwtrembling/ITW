package ideal.type.worldcup;

import ideal.type.worldcup.inc.ITWApplication;
import ideal.type.worldcup.lib.GetUrlImageCacheFile;
import ideal.type.worldcup.lib.NetworkITW;
import ideal.type.worldcup.sax.CommentItem;
import ideal.type.worldcup.sax.QuestionListItem;
import ideal.type.worldcup.sax.QuestionViewItem;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

public class RankActivity extends Activity implements OnClickListener{
	// Default Variables
	private Intent intent;
	private AdView adView;
	private ProgressDialog pd;
	private HashMap map;
	private NetworkITW ntw;
	private int density;

	// Data Array
	private ArrayList<QuestionListItem> qOriginalList;
	private ArrayList<QuestionListItem> qList;
	private ArrayList<QuestionViewItem> qData;
	private ArrayList<CommentItem> cList;
	
	// View Variables
	private ListView qDataLv;
	private ListView qListLv;
	private Spinner typeSpinner;
	private ImageView cancleIv;
	
	// Listview Adapter
	private RankQdataAdapter qDataLvAdapter;
	private RankQlistAdapter qListLvAdapter;
	private RankCListAdapter cListLvAdapter;
	
	// Listvie index
	private int qListCurrentPostion;
	
	// tmp global val
	private int g_iUid;
	private String g_iUrl;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_rank);
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		density = (int) metrics.density;
		qListCurrentPostion=0;
		setVariables();
	}

	private void setVariables(){
		qDataLv= (ListView)findViewById(R.id.qdata_lv);
		qListLv= (ListView)findViewById(R.id.qlist_lv);
		typeSpinner= (Spinner)findViewById(R.id.typeSpinner);
		cancleIv= (ImageView)findViewById(R.id.cancleIv);
		cancleIv.setOnClickListener(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();

		// ProgressDialog init
		pd = new ProgressDialog(this);
		pd.setTitle(getString(R.string.loading_msg)
);
		pd.setCancelable(false);
		pd.show();
		
		initAd(); // initialize Advertisement
		loadQuestionList(); // load Question List
	}
	
	private void loadQuestionList(){
		map =new HashMap<String, String>();
		map.put("c", ITWApplication.getQuestionList);
		map.put("mUid", ITWApplication.USERID);
		map.put("mFriendsUids", ITWApplication.FUIDS);
		ntw = new NetworkITW(map, RankHandler);
		ntw.execute();
	}
	
	//Bitmap 클래스
	class BitmapRankHandler extends AsyncTask<String, Void, String>{
		String imgUrl;
		ImageView iv;
		boolean cacheFlag;
		int chkVal;
		Bitmap bmp;
		public BitmapRankHandler(String imgUlr, ImageView iv, boolean cacheFlag, int chkVal){
			this.imgUrl=imgUlr;
			this.iv=iv;
			this.cacheFlag=cacheFlag;
			this.chkVal=chkVal;
		}
		
		@Override
		protected String doInBackground(String... params) {
			GetUrlImageCacheFile gcf = new GetUrlImageCacheFile(imgUrl, cacheFlag);
			bmp = gcf.getBmp();
			return null;
		}
		
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if(bmp==null) return;
			int cInt= Integer.parseInt(iv.getTag().toString());
			if(cInt==chkVal){
				iv.setImageBitmap(bmp);
				Animation myFadeInAnimation = AnimationUtils.loadAnimation(RankActivity.this, R.anim.anim_imgv);
				iv.startAnimation(myFadeInAnimation);
			}
		}
	}
	
	class RankQlistAdapter extends BaseAdapter{
		private Context c;
		public RankQlistAdapter(Context c){
			this.c=c;
		}
		@Override
		public int getCount() {
			return qList.size();
		}

		@Override
		public Object getItem(int pos) {
			return qList.get(pos);
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		@Override
		public View getView(int pos, View convertView, ViewGroup parent) {
			QlistViewHolder vh=null;
			QuestionListItem item=null;
			if(convertView==null){
				vh= new QlistViewHolder();
				convertView= View.inflate(c, R.layout.main_question_row, null);
				vh.rowRl=(RelativeLayout)convertView.findViewById(R.id.mqr_rl);
				vh.nameTv=(TextView)convertView.findViewById(R.id.mqr_name);
				vh.titleTv= (TextView)convertView.findViewById(R.id.mqr_title);
				vh.profileIv=(ImageView)convertView.findViewById(R.id.mqr_profile);
				convertView.setTag(vh);
			}else{
				vh=(QlistViewHolder)convertView.getTag();
			}
			item= qList.get(pos);
			
			if(pos==qListCurrentPostion) {
				vh.rowRl.setBackgroundColor(getResources().getColor(R.color.lv_pressed_color)); // 선택한색보이기
			}else{
				vh.rowRl.setBackgroundColor(getResources().getColor(R.color.lv_default_color)); // 미선택한색보이기
			}
			vh.nameTv.setText(item.mName);
			vh.titleTv.setText(item.qTitle);
			vh.profileIv.setTag(item.qUid);
			BitmapRankHandler brh= new BitmapRankHandler(item.uesrImg, vh.profileIv, true, item.qUid);
			brh.execute();
			return convertView;
		}
		
	}
	class QlistViewHolder{
		RelativeLayout rowRl;
		TextView titleTv, nameTv;
		ImageView profileIv;
	}
	
	class RankQdataAdapter extends BaseAdapter{
		Context c;
		public RankQdataAdapter(Context c){
			this.c=c;
		}
		@Override
		public int getCount() {
			return qData.size();
		}

		@Override
		public Object getItem(int position) {
			return qData.get(position);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			RankQdataViewHolder vh= null;
			if(convertView==null){
				convertView= View.inflate(c, R.layout.rank_qdata_row, null);
				vh= new RankQdataViewHolder();
				vh.medalIv=(ImageView)convertView.findViewById(R.id.rqr_medal_iv);
				vh.photoIv=(ImageView)convertView.findViewById(R.id.rqr_photo_iv);
				vh.hitsTv=(TextView)convertView.findViewById(R.id.rqr_hits_tv);
				convertView.setTag(vh);
			}
			else{
				vh=(RankQdataViewHolder)convertView.getTag();
			}
			
			QuestionViewItem item= qData.get(position);
			vh.hitsTv.setText("hits : "+item.iHits+" , comment :"+item.cCount);
			vh.photoIv.setTag(item.iUid);
			vh.photoIv.setBackgroundColor(Color.TRANSPARENT);
			switch(position){
			case 0:
				vh.medalIv.setBackgroundResource(R.drawable.icn_gold_3);
				break;
			case 1:
				vh.medalIv.setBackgroundResource(R.drawable.icn_silver_3);
				break;
			case 2:
				vh.medalIv.setBackgroundResource(R.drawable.icn_bronze_3);
				break;
			default :
				vh.medalIv.setBackgroundResource(Color.TRANSPARENT);
				break;
			}
			final int vhiUid= item.iUid;
			final String vhImg= item.iUrl;
			convertView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					pd= new ProgressDialog(RankActivity.this);
					pd.setTitle(getString(R.string.loading_msg));
					pd.show();
					g_iUid=vhiUid;
					g_iUrl=vhImg;
					map = new HashMap<String, String>();
					map.put("c", ITWApplication.getGoResult);
					map.put("qUid", qList.get(qListCurrentPostion).qUid+"");
					map.put("iUid", g_iUid+"");
					ntw= new NetworkITW(map, RankHandler);
					ntw.execute();
				}
			});
			
			BitmapRankHandler brh= new BitmapRankHandler(item.iUrl, vh.photoIv, true, item.iUid);
			brh.execute();
			return convertView;
		}	
	}
	
	class RankQdataViewHolder{
		ImageView photoIv, medalIv;
		TextView hitsTv;
	}
	
	//Comment list Adapter
	class RankCListAdapter extends BaseAdapter{
		Context c;
		public RankCListAdapter(Context c){
			this.c=c;
		}

		@Override
		public int getCount() {
			return cList.size();
		}

		@Override
		public Object getItem(int pos) {
			return cList.get(pos);
		}

		@Override
		public long getItemId(int pos) {
			return 0;
		}

		@Override
		public View getView(int pos, View v, ViewGroup parent) {
			//껍데기 불러오기
			RankCListViewHolder vh=null;
			if(v==null){
				v= View.inflate(c, R.layout.comment_row, null);
				vh= new RankCListViewHolder();
				vh.cmtTv= (TextView)v.findViewById(R.id.comment_title_txt);
				vh.nameTv=(TextView)v.findViewById(R.id.comment_name_txt);
				vh.removeIv=(ImageView)v.findViewById(R.id.comment_remove_iv);
				v.setTag(vh);
			}
			else{
				vh=(RankCListViewHolder) v.getTag();
			}
			//데이터 불러오기
			CommentItem item= cList.get(pos);
			vh.cmtTv.setText(item.comment);
			vh.nameTv.setText(item.mName);
			final int cmt_cUid= item.cUid;
			if(ITWApplication.USERID!=null && ITWApplication.USERID.toString().equals(""+item.mUid)) {
				vh.removeIv.setVisibility(View.VISIBLE);
				vh.removeIv.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {

						AlertDialog.Builder ad= new AlertDialog.Builder(RankActivity.this);
						ad.setMessage(R.string.comment_remove_alert_msg)
						.setPositiveButton(getString(R.string.confirm_msg),	new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								HashMap cmap = new HashMap<String, String>();
								cmap.put("c", ITWApplication.removeGoResult);
								cmap.put("cUid",cmt_cUid+"");
								cmap.put("mUid",ITWApplication.USERID);
								NetworkITW cmtNetwork= new NetworkITW(cmap, RankHandler);
								cmtNetwork.execute();
							}
						})
						.setNegativeButton(getString(R.string.cancle_msg), new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.cancel();
							}
						}).show();
					}
				});
			}
			else {
				vh.removeIv.setVisibility(View.GONE);
				vh.removeIv.setOnClickListener(null);
			}
			
			return v;
		}
	}
	class RankCListViewHolder{
		TextView cmtTv, nameTv;
		ImageView removeIv;
	}
	
	
	Handler RankHandler= new Handler(){
		public void handleMessage(android.os.Message msg) {
			String c = msg.getData().getString("c");
			String status = msg.getData().getString("status");
			if(c.equals(ITWApplication.getQuestionList)){
				if(status.equals("1")){
					qList=msg.getData().getParcelableArrayList("qList");
					qOriginalList=(ArrayList<QuestionListItem>) qList.clone();
					if(qList==null || qList.size()==0) {
						Toast.makeText(RankActivity.this, getString(R.string.rank_noqlist_msg), Toast.LENGTH_SHORT).show();
						stopPd();
					}
					else{
						setupBaseUI(); //기본 UI 세팅
						setupqListUI(); //질문 리스트 UI 세팅
						
						//질문 데이터 받아오기
						int qUid= qList.get(qListCurrentPostion).qUid;
						map= new HashMap<String, String>();
						map.put("c", ITWApplication.getQuestionData);
						map.put("mUid", ITWApplication.USERID);
						map.put("qUid", ""+qUid);
						ntw= new NetworkITW(map, RankHandler);
						ntw.execute();
					}
				}else {
					Toast.makeText(RankActivity.this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
					stopPd();
				}
			}
			else if(c.equals(ITWApplication.getQuestionData)){
				if(status.equals("1")){
					qData=msg.getData().getParcelableArrayList("qData");
					setupqDataUI();
					stopPd();
				}else {
					Toast.makeText(RankActivity.this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
					stopPd();
				}
			}
			else if(c.equals(ITWApplication.getGoResult)){
				if(status.equals("1")){
					cList= msg.getData().getParcelableArrayList("cList");
					setupcListUI();
					stopPd();
				}else {
					Toast.makeText(RankActivity.this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
					stopPd();
				}
			}
			//댓글 삭제
			else if(c.equals(ITWApplication.removeGoResult)){
				if(status.equals("1")){
					int cUid = Integer.parseInt(msg.getData().getString("cUid"));
					int removePosition=-1;
					for(int i=0;i<cList.size();i++){
						if(cList.get(i).cUid==cUid) {
							removePosition=i;
							break;
						}
					}
					if(removePosition>=0) {
						cList.remove(removePosition);
						if(cListLvAdapter!=null) cListLvAdapter.notifyDataSetChanged();
						Toast.makeText(RankActivity.this, R.string.comment_remove_complete_msg, Toast.LENGTH_SHORT).show();
					}else Toast.makeText(RankActivity.this, R.string.comment_remove_failed_msg, Toast.LENGTH_SHORT).show();
					stopPd();
				}else {
					Toast.makeText(RankActivity.this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
					stopPd();
				}
			}
			//댓글 등록
			else if(c.equals(ITWApplication.registGoResult)){
				if(status.equals("1")){
					int cUid = Integer.parseInt(msg.getData().getString("cUid"));
					int qUid = Integer.parseInt(msg.getData().getString("qUid"));
					int iUid = Integer.parseInt(msg.getData().getString("iUid"));
					String  comment = msg.getData().getString("comment");
					if(cUid>0) {
						//글을 갱신시킴
						CommentItem ci= new CommentItem();
						ci.cUid=cUid;
						ci.mName=ITWApplication.USERNAME;
						ci.mUid=Integer.parseInt(ITWApplication.USERID);
						ci.comment= comment;
						cList.add(0, ci);
						if(cListLvAdapter!=null) cListLvAdapter.notifyDataSetChanged();
						Toast.makeText(RankActivity.this, R.string.comment_reg_complete_msg, Toast.LENGTH_SHORT).show();
						stopPd();
					}
				}else {
					Toast.makeText(RankActivity.this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
					stopPd();
				}
			}
		};
	};

	// comment UI 세팅
	private void setupcListUI(){
		AlertDialog.Builder ab = new AlertDialog.Builder(RankActivity.this);
		LayoutInflater inflater = getLayoutInflater();
		final View abView = inflater.inflate(R.layout.rank_comment_pop, null);
		ImageView photo= (ImageView)abView.findViewById(R.id.rank_cmt_photo_iv);
		photo.setTag(g_iUid);
		BitmapRankHandler brh= new BitmapRankHandler(g_iUrl, photo, true, g_iUid);
		brh.execute();
		
		//comment 처리

		//로그인이 되어 있을 경우
		RelativeLayout top_rl=(RelativeLayout)abView.findViewById(R.id.cmt_top_rl);
		if(ITWApplication.USERID!=null && !ITWApplication.USERID.equals("")){
			top_rl.setVisibility(View.VISIBLE);
			Button regBtn=(Button)abView.findViewById(R.id.cmt_reg_btn);
			regBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					EditText cmtEdt=(EditText)abView.findViewById(R.id.cmt_reg_edt);
					String cmtTxt=(cmtEdt).getText().toString();
					if(cmtTxt==null || cmtTxt.equals("")) {
						AlertDialog.Builder ab= new AlertDialog.Builder(RankActivity.this);
						ab.setCancelable(false);
						ab.setMessage(R.string.comment_reg_empty_msg);
						ab.setPositiveButton(getString(R.string.confirm_msg), new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {	
								
							}
						});
						ab.show();
					}else{
						cmtEdt.setText("");
						HashMap cmap= new HashMap<String, String>();
						cmap.put("c", ITWApplication.registGoResult);
						cmap.put("mUid", ITWApplication.USERID+"");
						cmap.put("qUid", qList.get(qListCurrentPostion).qUid+"");
						cmap.put("iUid",g_iUid+"");
						cmap.put("comment", cmtTxt);
						if(pd==null) pd= new ProgressDialog(RankActivity.this);
						pd.setCancelable(false);
						pd.setTitle(getString(R.string.loading_msg));
						pd.show();
						NetworkITW ntw= new NetworkITW(cmap, RankHandler);
						ntw.execute();
					}
				}
			});
		}
		//로그아웃일 경우
		else{
			top_rl.setVisibility(View.GONE);
		}
		
		//comment 리스트
		ListView abLv=(ListView)abView.findViewById(R.id.cmt_lv);
		cListLvAdapter=new RankCListAdapter(this);
		abLv.setAdapter(cListLvAdapter);
		ab.setCancelable(false);
		ab.setView(abView).setPositiveButton(getString(R.string.confirm_msg), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		ab.show();
	}
	
	// 기본 UI세팅 
	private void setupBaseUI(){
		ArrayList<String> tSpinAl = new ArrayList<String>();
		tSpinAl.add(getString(R.string.type_all));
		tSpinAl.add(getString(R.string.type_public));
		tSpinAl.add(getString(R.string.type_friend));
		tSpinAl.add(getString(R.string.type_me));
		ArrayAdapter<String> typeSpinnerAdapter = new ArrayAdapter<String>(RankActivity.this, android.R.layout.simple_spinner_item, tSpinAl);
		typeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		typeSpinner.setAdapter(typeSpinnerAdapter);
		typeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> pv, View v, int pos, long arg3) {
				//전체를 선택
				if (pos == 0) {
					qList = (ArrayList<QuestionListItem>) qOriginalList.clone();
				} else {
					qList=null;
					qList= new ArrayList<QuestionListItem>();
					QuestionListItem item = null;
					for (int i = 0; i < qOriginalList.size(); i++) {
						item = qOriginalList.get(i);
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
					setupqListUI();
				}
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
	}
	
	//우측 질문 리스트
	private void setupqListUI(){
		qListLvAdapter= new RankQlistAdapter(this);
		qListLv.setOnItemClickListener(null);
		qListLv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int pos, long arg3) {
				if(qListCurrentPostion!=pos){
					qListCurrentPostion=pos;
					qListLvAdapter.notifyDataSetChanged();
					
					//우측 데이터를 갱신함~~pkch
					pd= new ProgressDialog(RankActivity.this);
					pd.setTitle(getString(R.string.loading_msg));
					pd.show();
					
					//질문 데이터 받아오기
					int qUid= qList.get(qListCurrentPostion).qUid;
					map= new HashMap<String, String>();
					map.put("c", ITWApplication.getQuestionData);
					map.put("mUid", ITWApplication.USERID);
					map.put("qUid", ""+qUid);
					ntw= new NetworkITW(map, RankHandler);
					ntw.execute();
				}
			}
		});
		qListLv.setAdapter(qListLvAdapter);
	}
	
	//좌측 질문 데이터
	private void setupqDataUI(){
		qDataLvAdapter= new RankQdataAdapter(this);
		qDataLv.setAdapter(qDataLvAdapter);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.cancleIv:
			onBackPressed();
			break;
		}
	}
	
	private void stopPd(){
		if(pd!=null){
			pd.dismiss();
			pd=null;
		}
	}
	@Override
	protected void onPause() {
		super.onPause();
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
		if (adView != null)
			adView.destroy();
	}

	@Override
	public void finish() {
		super.finish();
	}

	// 광고 초기화
	private void initAd() {
		adView = new AdView(this, AdSize.BANNER, ITWApplication.MY_AD_UNIT_ID);
		LinearLayout layout = (LinearLayout) findViewById(R.id.btm_ll);
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
