package ideal.type.worldcup;

import ideal.type.worldcup.inc.ITWApplication;
import ideal.type.worldcup.lib.GetUrlImageCacheFile;
import ideal.type.worldcup.lib.NetworkITW;
import ideal.type.worldcup.sax.CommentItem;
import ideal.type.worldcup.sax.QuestionViewItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

public class GoViewActivity extends Activity implements OnClickListener {
	//Default 
	private Intent intent;
	private AdView adView;
	private ProgressDialog pd = null;
	private ImageView iv1, iv2;
	private ArrayList<QuestionViewItem> qDataArr;
	private ArrayList<CommentItem> cList;
	private int deviceW, deviceH, density;
	private int firstUid = 0, secondUid = 0;
	private String qUid, qTitle, qRound;
	private CommentBaseAdapter commentAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_goview);
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		density = (int) metrics.density;
		deviceW = metrics.widthPixels;
		deviceH = metrics.heightPixels;
		iv1 = (ImageView) findViewById(R.id.goview_iv1);
		iv2 = (ImageView) findViewById(R.id.goview_iv2);
	}

	@Override
	protected void onResume() {
		super.onResume();
		pd = new ProgressDialog(this);
		initAd(); // 광고 보여주기
		initEvent(); // 이벤트 생성

		// 새로 고침 되었을 경우에... sourcely created 되는 view 삭제
		View v = findViewById(R.id.goview_medaliv);
		if (v != null) ((ViewManager) v.getParent()).removeView(v);
		v = findViewById(R.id.goview_commenv);
		if (v != null) ((ViewManager) v.getParent()).removeView(v);

		// 질문 데이터 받아오기
		intent = getIntent();
		qUid = intent.getStringExtra("qUid");
		qRound = intent.getStringExtra("qRound");
		qTitle = intent.getStringExtra("qTitle");
		
		//Log.e("pkch",qUid+","+qRound+","+qTitle);
		if (qUid == null || qTitle == null) {
			Toast.makeText(this, getString(R.string.question_not_good), Toast.LENGTH_SHORT).show();
			onBackPressed();
			return;
		}
		if(pd==null) pd = new ProgressDialog(this);
		pd.setTitle(getString(R.string.question_loading));
		pd.setCancelable(false);
		pd.show();

		((TextView) findViewById(R.id.goview_title)).setText(qTitle);
		HashMap map = new HashMap<String, String>();
		map.put("c", ITWApplication.getQuestionData);
		map.put("mUid", ITWApplication.USERID + "");
		map.put("qUid", qUid + "");
		NetworkITW network = new NetworkITW(map, goviewHandler);
		network.execute();
	}

	Handler goviewHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			super.handleMessage(msg);
			String c = msg.getData().getString("c");
			String status = msg.getData().getString("status");
			if (status != null && status.equals("1")) {
				// loading 질문 데이터
				if (c.equals(ITWApplication.getQuestionData)) {
					String imgBaseUrl = msg.getData().getString("imgBaseUrl");
					qDataArr = msg.getData().getParcelableArrayList("qData");
					int qRoundInteger= Integer.parseInt(qRound);
					if (qDataArr == null || qDataArr.size()<qRoundInteger) {
						Toast.makeText(GoViewActivity.this, getString(R.string.question_not_enough_photo), Toast.LENGTH_SHORT).show();
						if(pd!=null) pd.dismiss();
						onBackPressed();
						return;
					}
					Collections.shuffle(qDataArr);
					
					//round 에 맞춰서 다시 생성함
					int intQround=Integer.parseInt(qRound);
					if(qDataArr.size()>intQround){
						int fromIndex= qDataArr.size()-1;
						int untilIndex= intQround;
						for(int i=fromIndex; i>=untilIndex;i--){
							qDataArr.remove(i);
						}
					}
					//Log.e("pkch","intQround : "+intQround+" , size : "+qDataArr.size());
					curQdataPosition = 0;
					setImageViews(0);
				}
				// 결과 저장
				else if (c.equals(ITWApplication.putGoResult)) {
					//Log.e("pkch", "question save done!");
				}
				//comment 조회
				else if (c.equals(ITWApplication.getGoResult)) {
					cList = msg.getData().getParcelableArrayList("cList");
					setCommentUI();
				}
				//comment 등록
				else if(c.equals(ITWApplication.registGoResult)){
					EditText edt= (EditText)findViewById(R.id.cmt_reg_edt);
					int cUid = Integer.parseInt(msg.getData().getString("cUid"));
					if(edt!=null && cUid>0) {
						String cmt= edt.getText().toString();
						edt.setText("");
						//글을 갱신시킴
						CommentItem ci= new CommentItem();
						ci.cUid=cUid;
						ci.mName=ITWApplication.USERNAME;
						ci.mUid=Integer.parseInt(ITWApplication.USERID);
						ci.comment= cmt;
						cList.add(0, ci);
						updateCommentListView();
						Toast.makeText(GoViewActivity.this, R.string.comment_reg_complete_msg, Toast.LENGTH_SHORT).show();
					}
				}
				//comment 삭제
				else if(c.equals(ITWApplication.removeGoResult)){
					//삭제 성공시...
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
						updateCommentListView();
						Toast.makeText(GoViewActivity.this, R.string.comment_remove_complete_msg, Toast.LENGTH_SHORT).show();
					}else Toast.makeText(GoViewActivity.this, R.string.comment_remove_failed_msg, Toast.LENGTH_SHORT).show();
				}
				if (pd != null)
					pd.dismiss();
			}else{
				Toast.makeText(GoViewActivity.this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
			}
		};
	};
	
	private void updateCommentListView(){
		ListView lv= (ListView)findViewById(R.id.cmt_lv);
		if(lv!=null){
			commentAdapter = new CommentBaseAdapter(this, cList);
			lv.setAdapter(null);
			lv.setAdapter(commentAdapter);
		}
	}

	private int curQdataPosition = 0, lastSideNum = 0;

	// 댓글 URL 세팅
	private void setCommentUI() {
		int side = lastSideNum;
		RelativeLayout imageRelativeLayout = null;
		Animation fadeInAnimtion = AnimationUtils.loadAnimation(GoViewActivity.this, R.anim.anim_imgv);
		ImageView iv = new ImageView(this);
		iv.setId(R.id.goview_medaliv);// id setting
		Bitmap medalBmp = BitmapFactory.decodeResource(getResources(), R.drawable.icn_gold_3);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(medalBmp.getWidth() * density, medalBmp.getHeight() * density);

		// 메달 보이기
		int relativeId = 0, imageviewId = 0;
		ImageView imageview = null, removeImageview = null;
		if (side == 3) {
			relativeId = R.id.goview_lrl1; // 선택한 RelativeLayout
			imageviewId = R.id.goview_iv1; // 선택한 ImageView ID
			imageview = iv1; // 선택한 ImageView
			removeImageview = iv2; // 삭제 시켜야 할 imageview
		} else if (side == 4) {
			relativeId = R.id.goview_lrl2;
			imageviewId = R.id.goview_iv2;
			imageview = iv2;
			removeImageview = iv1;
		}
		imageRelativeLayout = (RelativeLayout) findViewById(relativeId);
		imageRelativeLayout.addView(iv);
		params.addRule(RelativeLayout.BELOW, imageviewId);
		params.setMargins(20, 20 - imageview.getHeight(), 0, 0);
		iv.setLayoutParams(params);
		iv.setImageBitmap(medalBmp);
		iv.startAnimation(fadeInAnimtion);

		// ### 댓글 보이기
		ViewGroup vg = (ViewGroup) removeImageview.getParent();
		vg.removeAllViews();// 불필요한 뷰 삭제하기
		LayoutInflater inflter = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		RelativeLayout commentRl = (RelativeLayout) inflter.inflate(R.layout.comment_list, vg, false);
		vg.addView(commentRl);

		// 로그인 정보가 없을 경우에는 댓글을 달거나 삭제할 수 없음
		if (ITWApplication.USERID == null) ((RelativeLayout) commentRl.findViewById(R.id.cmt_top_rl)).setVisibility(View.GONE);
		ListView lv= (ListView)commentRl.findViewById(R.id.cmt_lv);
		commentAdapter=new CommentBaseAdapter(this, cList);
		lv.setAdapter(commentAdapter);
		((Button)findViewById(R.id.cmt_reg_btn)).setOnClickListener(GoViewActivity.this); //regist 버튼 등록
		((Button)findViewById(R.id.goview_rankbtn)).setOnClickListener(GoViewActivity.this); // RANK 이동 버튼
		
		// Progressbar 닫기
		if (pd != null)
			pd.dismiss();
	}
	
	//Comment Base Adapter
	class CommentBaseAdapter extends BaseAdapter {
		private Context c;
		private ArrayList<CommentItem> cList;
		private LayoutInflater inflater;
		private String mUid;
		public CommentBaseAdapter(Context c, ArrayList<CommentItem> cList){
			this.mUid=ITWApplication.USERID;
			this.c=c;
			this.cList=cList;
			this.inflater=LayoutInflater.from(c);
		}
		
		@Override
		public View getView(int position, View v, ViewGroup parent) {
			if(v==null){
				v= inflater.inflate(R.layout.comment_row, null);
			}
			int cmt_mUid= cList.get(position).mUid;
			final int cmt_cUid= cList.get(position).cUid;
			String cmt_String= cList.get(position).comment;
			String cmt_mName=cList.get(position).mName;
			((TextView)v.findViewById(R.id.comment_title_txt)).setText(cmt_String);
			((TextView)v.findViewById(R.id.comment_name_txt)).setText(cmt_mName);
			//로그인한 mUid와 삭제 대상 uId가 같을경우 삭제 활성화
			if(mUid!=null && mUid.equals(cmt_mUid+"")) {
				ImageView iv= (ImageView)v.findViewById(R.id.comment_remove_iv);
				iv.setVisibility(View.VISIBLE);
				iv.setTag(cmt_cUid+"");
				iv.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						AlertDialog.Builder ad= new AlertDialog.Builder(GoViewActivity.this);
						ad.setMessage(R.string.comment_remove_alert_msg)
						.setPositiveButton(getString(R.string.confirm_msg),	new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								HashMap cmap = new HashMap<String, String>();
								cmap.put("c", ITWApplication.removeGoResult);
								cmap.put("cUid",cmt_cUid+"");
								cmap.put("mUid",ITWApplication.USERID);
								NetworkITW cmtNetwork= new NetworkITW(cmap, goviewHandler);
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
			return v;
		}
		
		@Override
		public long getItemId(int position) {
			return 0;
		}
		
		@Override
		public Object getItem(int pos) {
			return cList.get(pos);
		}
		
		@Override
		public int getCount() {
			return cList.size();
		}
	};

	private void setImageViews(int side) {
		adView.loadAd(new AdRequest()); // ad reload
		BitmapUrlHandlerGoview bh1, bh2;
		Animation myFadeInAnimation;

		// 처음들어왔을 경우
		if (curQdataPosition == 0) {
			bh1 = new BitmapUrlHandlerGoview(qDataArr.get(curQdataPosition).iUrl, findViewById(R.id.goview_iv1), true, 1, qDataArr.get(curQdataPosition).iUid);
			bh2 = new BitmapUrlHandlerGoview(qDataArr.get(curQdataPosition + 1).iUrl, findViewById(R.id.goview_iv2), true, 2, qDataArr.get(curQdataPosition + 1).iUid);
			bh1.execute();
			bh2.execute();
			curQdataPosition = curQdataPosition + 2;
		} else if (side == 3 || side == 4) {
			// side 3:왼쪽클릭, 4:우측클릭
			if (side == 3) {
				// 끝났을 경우
				if ((curQdataPosition + 1) > qDataArr.size()) {
					firstUid = Integer.parseInt(iv1.getTag().toString());
					secondUid = Integer.parseInt(iv2.getTag().toString());
					iv2.setImageBitmap(null);
					myFadeInAnimation = AnimationUtils.loadAnimation(GoViewActivity.this, R.anim.anim_imgv_rlr);
					iv2.startAnimation(myFadeInAnimation);
					selectFinished(side);
					return;
				}
				// 일반
				else {
					myFadeInAnimation = AnimationUtils.loadAnimation(GoViewActivity.this, R.anim.anim_imgv_rlr);
					iv2.startAnimation(myFadeInAnimation);
					String iUrl = qDataArr.get(curQdataPosition).iUrl;
					bh2 = new BitmapUrlHandlerGoview(iUrl, findViewById(R.id.goview_iv2), true, 2, qDataArr.get(curQdataPosition).iUid);
					bh2.execute();
				}
			} else if (side == 4) {
				// 끝났을 경우
				if ((curQdataPosition + 1) > qDataArr.size()) {
					firstUid = Integer.parseInt(iv2.getTag().toString());
					secondUid = Integer.parseInt(iv1.getTag().toString());
					iv1.setImageBitmap(null);
					myFadeInAnimation = AnimationUtils.loadAnimation(GoViewActivity.this, R.anim.anim_imgv_llr);
					iv1.startAnimation(myFadeInAnimation);
					selectFinished(side);
					return;
				}
				// 일반
				else {
					myFadeInAnimation = AnimationUtils.loadAnimation(GoViewActivity.this, R.anim.anim_imgv_llr);
					iv1.startAnimation(myFadeInAnimation);
					String iUrl = qDataArr.get(curQdataPosition).iUrl;
					bh1 = new BitmapUrlHandlerGoview(iUrl, findViewById(R.id.goview_iv1), true, 1, qDataArr.get(curQdataPosition).iUid);
					bh1.execute();
				}
			}
			curQdataPosition++;
		}
	}

	// 선택이 끝났을 경우
	private void selectFinished(int side) {

		if(pd==null) pd = new ProgressDialog(this);
		pd.setTitle(getString(R.string.loading_msg));
		pd.setCancelable(false);
		pd.show();

		// 이미지 이벤트 취소
		cancleEvent();

		// 결과 데이터 반영
		lastSideNum = side;
		HashMap map = new HashMap<String, String>();
		map.put("c", ITWApplication.putGoResult);
		map.put("mUid", ITWApplication.USERID + "");
		map.put("qUid", qUid + "");
		map.put("iUid1", firstUid + "");
		map.put("iUid2", secondUid + "");
		NetworkITW network = new NetworkITW(map, goviewHandler);
		network.execute();

		// 댓글 데이터 받아오기
		HashMap mapComment = new HashMap<String, String>();
		mapComment.put("c", ITWApplication.getGoResult);
		mapComment.put("qUid", "" + qUid);
		mapComment.put("iUid", "" + firstUid);
		NetworkITW networkComment = new NetworkITW(mapComment, goviewHandler);
		networkComment.execute();

	}

	private void initEvent() {
		iv1.setOnClickListener(this);
		iv2.setOnClickListener(this);
	}

	private void cancleEvent() {
		iv1.setOnClickListener(null);
		iv2.setOnClickListener(null);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.goview_iv1:
			setImageViews(3);
			break;
		case R.id.goview_iv2:
			setImageViews(4);
			break;
		case R.id.cmt_reg_btn:
			String cmtTxt= ((EditText)findViewById(R.id.cmt_reg_edt)).getText().toString();
			if(cmtTxt==null || cmtTxt.equals("")) {
				AlertDialog.Builder ab= new AlertDialog.Builder(this);
				ab.setCancelable(false);
				ab.setMessage(R.string.comment_reg_empty_msg);
				ab.setPositiveButton(getString(R.string.confirm_msg), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {	
						dialog.dismiss();
					}
				});
				ab.show();
			}else{
				HashMap cmap= new HashMap<String, String>();
				cmap.put("c", ITWApplication.registGoResult);
				cmap.put("mUid", ITWApplication.USERID+"");
				cmap.put("qUid", qUid+"");
				cmap.put("iUid",firstUid+"");
				cmap.put("comment", cmtTxt);
				if(pd==null) pd= new ProgressDialog(this);
				pd.setCancelable(false);
				pd.setTitle(getString(R.string.loading_msg)
);
				pd.show();
				NetworkITW ntw= new NetworkITW(cmap, goviewHandler);
				ntw.execute();
			}
			break;
		case R.id.goview_rankbtn:
			intent= new Intent(GoViewActivity.this, RankActivity.class);
			intent.putExtra("qUid", qUid);
			startActivity(intent);
			overridePendingTransition(R.anim.anim_main_in, R.anim.anim_action_out);
			finish();
			break;
		}
	}

	class BitmapUrlHandlerGoview extends AsyncTask<String, Void, String> {
		private String imgUrl;
		private View v;
		private Bitmap bmp;
		private boolean cacheFlag;
		private GetUrlImageCacheFile gcf;
		private int side;
		private int iUid;

		public BitmapUrlHandlerGoview(String imgUrl, View v, boolean cacheFlag, int side, int iUid) {
			this.imgUrl = imgUrl;
			this.v = v;
			this.cacheFlag = cacheFlag;
			this.side = side;
			this.iUid = iUid;
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
			if (bmp == null) return;
			int bw = bmp.getWidth() * density;
			int bh = bmp.getHeight() * density;

			Bitmap newBmp = Bitmap.createScaledBitmap(bmp, bw, bh, true);
			ImageView iv = (ImageView) v;
			iv.setTag(iUid);
			iv.setImageBitmap(newBmp);
			Animation myFadeInAnimation = null;
			if (side == 1) {
				myFadeInAnimation = AnimationUtils.loadAnimation(GoViewActivity.this, R.anim.anim_imgv_lrl);
				iv.startAnimation(myFadeInAnimation);
			} else if (side == 2) {
				myFadeInAnimation = AnimationUtils.loadAnimation(GoViewActivity.this, R.anim.anim_imgv_rrl);
				iv.startAnimation(myFadeInAnimation);
			}
			if (pd != null && pd.isShowing())
				pd.dismiss();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
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

	private void initAd() {
		adView = new AdView(this, AdSize.BANNER, ITWApplication.MY_AD_UNIT_ID);
		LinearLayout layout = (LinearLayout) findViewById(R.id.go_btmLl);
		adView.loadAd(new AdRequest());
		layout.addView(adView);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		intent = new Intent(this, GoActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.anim_main_in, R.anim.anim_action_out);
		finish();
	}
}
