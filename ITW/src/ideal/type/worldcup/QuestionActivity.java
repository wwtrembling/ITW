package ideal.type.worldcup;

import ideal.type.worldcup.inc.ITWApplication;
import ideal.type.worldcup.lib.GetUrlImageCacheFile;
import ideal.type.worldcup.lib.NetworkITW;
import ideal.type.worldcup.lib.Utils;
import ideal.type.worldcup.sax.ImagesItem;
import ideal.type.worldcup.sax.QuestionListItem;
import ideal.type.worldcup.sax.QuestionViewItem;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
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
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
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

public class QuestionActivity extends Activity implements OnClickListener {
	// Default Variables
	private Intent intent;
	private AdView adView;
	private ProgressDialog pd;
	private HashMap map;
	private NetworkITW ntw;
	private int density, newWidth, newHeight;

	// UI Variables
	private ImageView questionIv, cancleIv, informationIv;
	private ListView questionListView, imageListView;
	private Spinner roundSpinner, typeSpinner;
	private TextView userInfoTxt;
	private ArrayList<QuestionListItem> qList;
	private ArrayList<ImagesItem> iList;
	private ArrayList<Integer> icheckedList;
	private ArrayList<QuestionViewItem> qDataArr;
	private QuestionQListAdapter qListAdapter;
	private QuestionImageListAdapter qiListAdapter;
	private boolean userAndImageFlag, qListAndDataFlag; // 사용자정보&이미지리스트,
														// 사용자질문&질문상세 데이타들이 로딩
														// 됐는지 확인하는 flag
	private int mLevel, mSlot, mPoint, qUid;
	private int currentQuestionPosition;

	private boolean noResumeChk; // onResumg check

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_question);
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		density = (int) metrics.density;
		newWidth = metrics.widthPixels;
		newHeight = metrics.heightPixels;
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (noResumeChk == true)
			return;

		// 권한이 없는 경우
		if (ITWApplication.USERID == null) {
			Toast.makeText(this, getString(R.string.need_to_fb_login), Toast.LENGTH_SHORT).show();
			onBackPressed();
			return;
		}

		// ProgressDialog init
		pd = new ProgressDialog(this);
		pd.setTitle(getString(R.string.loading_msg));
		pd.setCancelable(false);
		pd.show();

		// 변수 초기화
		qList = new ArrayList<QuestionListItem>();
		iList = new ArrayList<ImagesItem>();
		icheckedList = new ArrayList<Integer>();
		qDataArr = new ArrayList<QuestionViewItem>();
		currentQuestionPosition = 0;
		qUid = 0;
		userAndImageFlag = false; // 사용자&이미지리스트 flag 초기화
		qListAndDataFlag = false; // 질문 리스트&상세정보 flag 초기화

		// default setting
		initAd();
		initEvent();

		// 사용자 데이터 받아오기
		map = new HashMap<String, String>();
		map.put("c", ITWApplication.getUserInfo);
		map.put("mUid", ITWApplication.USERID);
		ntw = new NetworkITW(map, questionHandler);
		ntw.execute();

		// 질문 리스트 받아오기
		map = new HashMap<String, String>();
		map.put("c", ITWApplication.getQuestionList);
		map.put("mUid", "" + ITWApplication.USERID);
		map.put("onlymUid", "2");
		ntw = new NetworkITW(map, questionHandler);
		ntw.execute();
	}

	Handler questionHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			String c = msg.getData().getString("c");
			String status = msg.getData().getString("status");
			if (status != null) {
				// 사용자 데이터 받아오기
				if (c.equals(ITWApplication.getUserInfo) && status.equals("1")) {
					mLevel = Integer.parseInt(msg.getData().getString("mLevel"));
					mSlot = Integer.parseInt(msg.getData().getString("mSlot"));
					mPoint = Integer.parseInt(msg.getData().getString("mPoint"));
					userInfoTxt.setText(ITWApplication.USERNAME + "  Lv:" + mLevel + "  Slot:" + mSlot); // 사용자
																											// 정보
					// 사용자 이미지 리스트 받아오기
					map = null;
					map = new HashMap<String, String>();
					map.put("c", ITWApplication.getUserImages);
					map.put("mUid", ITWApplication.USERID);
					map.put("order", "1");
					ntw = new NetworkITW(map, questionHandler);
					ntw.execute();
				}
				// 사용자 이미지 리스트
				else if (c.equals(ITWApplication.getUserImages) && status.equals("1")) {
					userAndImageFlag = true;
					iList = msg.getData().getParcelableArrayList("iList");
					// 이미지 리스트 출력
					for (int i = iList.size(); i < mSlot; i++) {
						iList.add(new ImagesItem()); // 빈것을 내버려둠
					}
					qiListAdapter = new QuestionImageListAdapter(QuestionActivity.this, iList);
					imageListView.setAdapter(qiListAdapter);

					if (userAndImageFlag && qListAndDataFlag)
						setQuestionDataUi();
				}
				// 질문 리스트
				else if (c.equals(ITWApplication.getQuestionList) && status.equals("1")) {
					qList = msg.getData().getParcelableArrayList("qList");

					// 질문 리스트 출력
					qListAdapter = new QuestionQListAdapter(QuestionActivity.this, qList);
					questionListView.setAdapter(qListAdapter);
					questionListView.setOnItemClickListener(questionItemListener);

					// 존재하는 질문 데이터가 있을 경우 : 상세 질문 데이터 받아오기
					if (qList != null && qList.size() > 0 && qList.get(0).qUid > 0) {
						// 첫번째 데이터 click
						qUid = qList.get(0).qUid;
						map = new HashMap<String, String>();
						map.put("c", ITWApplication.getQuestionData);
						map.put("mUid", ITWApplication.USERID);
						map.put("qUid", qUid + "");
						ntw = new NetworkITW(map, questionHandler);
						ntw.execute();
					}
					// 존재하는 질문 데이터가 없을 경우 질문을 받아오
					else {
						if (pd != null) {
							pd.dismiss();
							pd = null;
						}
						qListAndDataFlag = false;
						AlertDialog.Builder ab = new AlertDialog.Builder(QuestionActivity.this);
						LayoutInflater inflater = getLayoutInflater();
						View abView = inflater.inflate(R.layout.question_registquestion_popup, null);
						((EditText) abView.findViewById(R.id.qr_popup_question_edt)).setHint(getString(R.string.question_fregist_popup_msg));
						ab.setCancelable(false);
						ab.setView(abView).setNegativeButton(" No ", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								onBackPressed();
							}
						}).setPositiveButton(" Confirm ", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// 안내 메시지 출력
								showInformation();

								EditText edt = (EditText) ((AlertDialog) dialog).findViewById(R.id.qr_popup_question_edt);
								String comment = edt.getText().toString();
								map = new HashMap<String, String>();
								map.put("c", ITWApplication.registQuestion);
								map.put("mUid", ITWApplication.USERID);
								map.put("qType", "2");
								map.put("qRound", "4");
								map.put("qTitle", comment);
								ntw = new NetworkITW(map, questionHandler);
								ntw.execute();
							}
						});
						ab.show();
					}
				}
				// 질문 상세 데이터
				else if (c.equals(ITWApplication.getQuestionData) && status.equals("1")) {
					qListAndDataFlag = true;
					qDataArr = msg.getData().getParcelableArrayList("qData");
					icheckedList = null;
					icheckedList = new ArrayList<Integer>();
					if (qDataArr != null) {
						for (int i = 0; i < qDataArr.size(); i++) {
							icheckedList.add(qDataArr.get(i).iUid);
						}
					}
					if (userAndImageFlag && qListAndDataFlag) {
						setQuestionDataUi();
					}
				}
				// 질문 등록
				else if (c.equals(ITWApplication.registQuestion) && status.equals("1")) {
					if (qList == null)
						qList = new ArrayList<QuestionListItem>();
					QuestionListItem qli = new QuestionListItem();
					qli.qUid = Integer.parseInt(msg.getData().getString("qUid"));
					qli.qHits = 0;
					qli.qRound = Integer.parseInt(msg.getData().getString("qRound"));
					qli.qType = Integer.parseInt(msg.getData().getString("qType"));
					qli.qTitle = msg.getData().getString("qTitle");
					qList.add(0, qli);
					
					// 처음 등록
					if (qListAndDataFlag == false) {
						qListAndDataFlag = true;
						if (userAndImageFlag && qListAndDataFlag)
							setQuestionDataUi();
					}
					// 일반 질문 등록
					else {
						Log.e("pkch","일반 질문 등록");
						qListAdapter = new QuestionQListAdapter(QuestionActivity.this, qList);
						questionListView.setAdapter(qListAdapter);
						questionListView.setOnItemClickListener(questionItemListener);

						icheckedList=null;
						icheckedList= new ArrayList<Integer>();
						qiListAdapter.notifyDataSetChanged();
						imageListView.invalidate();
					}
				}
				// 질문 수정
				else if (c.equals(ITWApplication.modifyQuestion)) {
					if (status.equals("1"))
						Toast.makeText(QuestionActivity.this, getString(R.string.question_modify_msg1), Toast.LENGTH_SHORT).show();
					else if (status.equals("2"))
						Toast.makeText(QuestionActivity.this, getString(R.string.question_modify_msg2), Toast.LENGTH_SHORT).show();
					if (pd != null) {
						pd.dismiss();
						pd = null;
					}
				}
				// 질문 삭제
				else if (c.equals(ITWApplication.removeQuestion) && status.equals("1")) {
					intent = new Intent(QuestionActivity.this, QuestionActivity.class);
					startActivity(intent);
					overridePendingTransition(0, 0);
					finish();
				}
				// 이미지 삭제
				else if (c.equals(ITWApplication.removeImage) && status.equals("1")) {
					// 사용자 이미지 리스트 다시 받아오기
					map = null;
					map = new HashMap<String, String>();
					map.put("c", ITWApplication.getUserImages);
					map.put("mUid", ITWApplication.USERID);
					ntw = new NetworkITW(map, questionHandler);
					ntw.execute();
				}
			} else {
				Toast.makeText(QuestionActivity.this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
			}
		};
	};

	private OnItemClickListener questionItemListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> pv, View v, int pos, long arg3) {
			// 상이한 행을 선택했을 경우에만 데이터를 로딩함
			if (currentQuestionPosition != pos) {
				currentQuestionPosition = pos;
				qListAdapter.notifyDataSetChanged();
				// qUid 데이터를 받아옴
				QlistViewHolder qhv = (QlistViewHolder) v.getTag();
				int qUid = Integer.parseInt(qhv.tv1.getTag().toString());
				pd = new ProgressDialog(QuestionActivity.this);
				pd.setMessage(getString(R.string.loading_msg));
				pd.setCancelable(false);
				pd.show();

				// 질문 데이터를 갱신
				map = new HashMap<String, String>();
				map.put("c", ITWApplication.getQuestionData);
				map.put("qUid", "" + qUid);
				map.put("mUid", ITWApplication.USERID);
				ntw = new NetworkITW(map, questionHandler);
				ntw.execute();
			}
		}
	};

	// 사용자 정보, 사용자 이미지, 질문 데이터를 모두 받아왔을 경우 저장하는 데이터
	private void setQuestionDataUi() {
		int round = qList.get(currentQuestionPosition).qRound;
		int type = qList.get(currentQuestionPosition).qType;

		// Round Spinner 세팅
		ArrayList<String> roundArr = new ArrayList<String>();
		roundArr.add("4 ROUND");
		roundArr.add("8 ROUND");
		roundArr.add("16 ROUND");
		roundArr.add("32 ROUND");
		ArrayAdapter<String> roundSpinAdapter = new ArrayAdapter<String>(QuestionActivity.this, android.R.layout.simple_spinner_item, roundArr);
		roundSpinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		roundSpinner.setPrompt(getString(R.string.question_choose_round));
		roundSpinner.setAdapter(roundSpinAdapter);
		// selector
		int roundSelectIndex = 0;
		switch (round) {
		case 4:
			roundSelectIndex = 0;
			break;
		case 8:
			roundSelectIndex = 1;
			break;
		case 16:
			roundSelectIndex = 2;
			break;
		case 32:
			roundSelectIndex = 3;
			break;
		}
		roundSpinner.setSelection(roundSelectIndex);

		// Type Spinner 세팅
		ArrayList<String> typeArr = new ArrayList<String>();
		typeArr.add(getString(R.string.question_choose_type1));
		typeArr.add(getString(R.string.question_choose_type2));
		ArrayAdapter<String> typeSpinAdtapter = new ArrayAdapter<String>(QuestionActivity.this, android.R.layout.simple_spinner_item, typeArr);
		typeSpinAdtapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		typeSpinner.setPrompt(getString(R.string.question_choose_type));
		typeSpinner.setAdapter(typeSpinAdtapter);
		// selector
		int typeSelectIndex = 0;
		switch (type) {
		case 1:
			typeSelectIndex = 0;
			break;
		case 2:
			typeSelectIndex = 1;
			break;
		default:
			typeSelectIndex = 1;
			break;
		}
		typeSpinner.setSelection(typeSelectIndex);
		/*
		 * typeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
		 * 
		 * @Override public void onItemSelected(AdapterView<?> pv, View v, int
		 * pos, long arg3) { if (pos == 1) { if (mLevel < 40) {
		 * Toast.makeText(QuestionActivity.this,
		 * getString(R.string.question_type_warning),
		 * Toast.LENGTH_SHORT).show(); typeSpinner.setSelection(0); } } }
		 * 
		 * @Override public void onNothingSelected(AdapterView<?> arg0) { } });
		 */

		// 질문 데이터를 수정함
		qiListAdapter.notifyDataSetChanged();

		// Spinner dismiss
		if (pd != null) {
			pd.dismiss();
			pd = null;
		}
	}

	// 질문 Adapter
	class QuestionQListAdapter extends BaseAdapter {
		private ArrayList<QuestionListItem> qList;
		private Context c;
		private LayoutInflater inflater;

		public QuestionQListAdapter(Context c, ArrayList<QuestionListItem> qList) {
			this.c = c;
			this.qList = qList;
			this.inflater = LayoutInflater.from(c);
		}

		@Override
		public int getCount() {
			return qList.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return qList.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			QlistViewHolder vh = null;
			if (convertView == null) {
				vh = new QlistViewHolder();
				convertView = View.inflate(c, R.layout.main_question_row, null);
				vh.iv1 = (ImageView) convertView.findViewById(R.id.mqr_profile);
				vh.iv2 = (ImageView) convertView.findViewById(R.id.mqr_tempiv);
				vh.iv3 = (ImageView) convertView.findViewById(R.id.mqr_tempiv2);
				vh.tv1 = (TextView) convertView.findViewById(R.id.mqr_title);
				vh.rl = (RelativeLayout) convertView.findViewById(R.id.mqr_rl);
				convertView.setTag(vh);
			}
			vh = (QlistViewHolder) convertView.getTag();
			vh.tv1.setText(qList.get(position).qTitle);
			vh.tv1.setTag(qList.get(position).qUid); // tv1의 tag에 qUid 를 넣음

			// 데이터 입력
			if (position == currentQuestionPosition) {
				vh.rl.setBackgroundColor(getResources().getColor(R.color.lv_pressed_color)); // 선택한색보이기
				vh.iv1.setVisibility(View.VISIBLE);
				vh.iv2.setVisibility(View.VISIBLE);
				vh.iv3.setVisibility(View.VISIBLE);
				vh.iv1.setBackgroundResource(R.drawable.ic_edit);
				vh.iv2.setBackgroundResource(R.drawable.ic_cancle);
				vh.iv3.setBackgroundResource(R.drawable.ic_save);

				// 첫번째 질문일 경우에는 삭제가 불가하도록 함
				final int vhqUid = qList.get(position).qUid;
				final String vhqTitle = qList.get(position).qTitle;
				final int vhqPosition = position;
				vh.iv1.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						AlertDialog.Builder ab = new AlertDialog.Builder(QuestionActivity.this);
						LayoutInflater inflater = getLayoutInflater();
						View abView = inflater.inflate(R.layout.question_registquestion_popup, null);
						EditText abEdText = ((EditText) abView.findViewById(R.id.qr_popup_question_edt));
						abEdText.setText(vhqTitle);
						abEdText.setHint(getString(R.string.question_regist_popup_msg));
						ab.setCancelable(false);
						ab.setView(abView).setNegativeButton(getString(R.string.cancle_msg), new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {

							}
						}).setPositiveButton(getString(R.string.confirm_msg), new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								EditText edt = (EditText) ((AlertDialog) dialog).findViewById(R.id.qr_popup_question_edt);
								String comment = edt.getText().toString();
								qList.get(vhqPosition).qTitle = comment;
								qListAdapter.notifyDataSetChanged();
							}
						});
						ab.show();
					}
				});
				vh.iv2.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						AlertDialog.Builder ab = new AlertDialog.Builder(QuestionActivity.this);
						ab.setCancelable(false);
						ab.setTitle(getString(R.string.confirm_remove_question)).setNegativeButton(getString(R.string.cancle_msg), new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {

							}
						}).setPositiveButton(getString(R.string.confirm_msg), new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								removeQuestion();
							}
						});
						ab.show();
					}
				});
				vh.iv3.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						modifyQuestion();
					}
				});
			} else {
				vh.rl.setBackgroundColor(getResources().getColor(R.color.lv_default_color)); // 미선택한색보이기
				vh.iv1.setVisibility(View.INVISIBLE);
				vh.iv2.setVisibility(View.INVISIBLE);
				vh.iv3.setVisibility(View.INVISIBLE);
			}
			return convertView;
		}
	}

	// 질문 삭제
	private void removeQuestion() {
		pd = new ProgressDialog(this);
		pd.setTitle(getString(R.string.loading_msg));
		pd.setCancelable(false);
		pd.show();

		map = new HashMap<String, String>();
		map.put("c", ITWApplication.removeQuestion);
		map.put("qUid", "" + qList.get(currentQuestionPosition).qUid);
		map.put("mUid", ITWApplication.USERID);
		ntw = new NetworkITW(map, questionHandler);
		ntw.execute();
	}

	// 질문 수정
	private void modifyQuestion() {
		pd = new ProgressDialog(this);
		pd.setTitle(getString(R.string.loading_msg));
		// pd.setCancelable(false);
		pd.show();

		// 질문 데이터 받아오기
		int qType = 0, qRound = 0;
		String _qRound = roundSpinner.getSelectedItem().toString();
		if (_qRound.equals("4 ROUND"))
			qRound = 4;
		else if (_qRound.equals("8 ROUND"))
			qRound = 8;
		else if (_qRound.equals("16 ROUND"))
			qRound = 16;
		else if (_qRound.equals("32 ROUND"))
			qRound = 32;
		String _qType = typeSpinner.getSelectedItem().toString();
		if (_qType.equals(getString(R.string.question_choose_type1)))
			qType = 1;
		else if (_qType.equals(getString(R.string.question_choose_type2)))
			qType = 2;

		// 제목 받아오기
		String qTitle = qList.get(currentQuestionPosition).qTitle;
		int qUid = qList.get(currentQuestionPosition).qUid;

		// 저장된 이미지 데이터
		String iUids = "";
		for (int i = 0; i < icheckedList.size(); i++) {
			iUids += icheckedList.get(i).intValue() + ",";
		}

		map = new HashMap<String, String>();
		map.put("c", ITWApplication.modifyQuestion);
		map.put("qUid", "" + qUid);
		map.put("mUid", ITWApplication.USERID);
		map.put("qType", "" + qType);
		map.put("qRound", "" + qRound);
		map.put("qTitle", qTitle);
		map.put("iUids", iUids);
		ntw = new NetworkITW(map, questionHandler);
		ntw.execute();
	}

	class QlistViewHolder {
		ImageView iv1, iv2, iv3;
		TextView tv1;
		RelativeLayout rl;
	}

	// 이미지 Adapter
	class QuestionImageListAdapter extends BaseAdapter {
		private ArrayList<ImagesItem> iList;
		private Context c;
		private LayoutInflater inflater;

		public QuestionImageListAdapter(Context c, ArrayList<ImagesItem> iList) {
			this.c = c;
			this.iList = iList;
			this.inflater = LayoutInflater.from(c);
		}

		@Override
		public int getCount() {
			int size = (int) Math.ceil((float) iList.size() / 3.0);
			return size;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder vh = null;
			if (convertView == null) {
				convertView = View.inflate(c, R.layout.question_question_row, null);
				vh = new ViewHolder();
				vh.rl1 = (RelativeLayout) convertView.findViewById(R.id.qq_rl1);
				vh.rl2 = (RelativeLayout) convertView.findViewById(R.id.qq_rl2);
				vh.rl3 = (RelativeLayout) convertView.findViewById(R.id.qq_rl3);
				vh.iv1 = (ImageView) convertView.findViewById(R.id.qq_iv1);
				vh.iv2 = (ImageView) convertView.findViewById(R.id.qq_iv2);
				vh.iv3 = (ImageView) convertView.findViewById(R.id.qq_iv3);
				vh.ck1 = (CheckBox) convertView.findViewById(R.id.qq_checkbox1);
				vh.ck2 = (CheckBox) convertView.findViewById(R.id.qq_checkbox2);
				vh.ck3 = (CheckBox) convertView.findViewById(R.id.qq_checkbox3);
				convertView.setTag(vh);
			}
			vh = (ViewHolder) convertView.getTag();
			int curIndex = position * 3;

			String a="";
			for (int i = 0; i < icheckedList.size(); i++) {
				a+=","+icheckedList.get(i).intValue();
			}
			Log.e("pkch", "out:"+a);
			// slot 갯수 초과 했을 경우
			vh.rl1.setOnClickListener(null);
			vh.iv1.setOnClickListener(null);
			if (iList.size() <= curIndex) {
				vh.rl1.setVisibility(View.INVISIBLE);
			}
			// 데이터가 존재할 경우
			else if (iList.get(curIndex).iUrl != null) {
				vh.rl1.setVisibility(View.VISIBLE);
				vh.rl1.setBackgroundResource(android.R.color.transparent);
				vh.ck1.setTag("" + iList.get(curIndex).iUid);
				boolean flag1 = false;
				for (int i = 0; i < icheckedList.size(); i++) {
					if (icheckedList.get(i).intValue() == iList.get(curIndex).iUid) {
						flag1 = true;
						break;
					}
				}
				vh.ck1.setChecked(flag1);
				// checkbox 확인
				final int iUid1 = iList.get(curIndex).iUid;
				vh.ck1.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						if (isChecked) {
							boolean flag = false;
							for (int i = 0; i < icheckedList.size(); i++) {
								if (icheckedList.get(i).intValue() == iUid1) {
									flag = true;
								}
							}
							if (flag == false)
								icheckedList.add(iUid1);
						} else {
							for (int i = 0; i < icheckedList.size(); i++) {
								if (icheckedList.get(i).intValue() == iUid1) {
									icheckedList.remove(i);
								}
							}
						}
						Log.e("pkch",isChecked+" : "+iUid1);
					}
				});
				vh.ck1.setVisibility(View.VISIBLE);
				vh.iv1.setTag("" + iList.get(curIndex).iUid);
				vh.iv1.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						removeImage(iUid1);
					}
				});
				BitmapHandlerQuestion bhm = new BitmapHandlerQuestion(iList.get(curIndex).iUrl, vh.iv1, true, iList.get(curIndex).iUid);
				bhm.execute();
			}
			// 데이터가 존재하지 않을 경우
			else {
				vh.rl1.setVisibility(View.VISIBLE);
				vh.rl1.setBackgroundResource(R.drawable.dot_border);
				vh.rl1.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						registImage();
					}
				});
				vh.iv1.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						registImage();
					}
				});
				vh.iv1.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_photo));
				vh.ck1.setVisibility(View.INVISIBLE);
			}
			curIndex++;

			// slot 갯수 초과 했을 경우
			vh.rl2.setOnClickListener(null);
			vh.iv2.setOnClickListener(null);
			if (iList.size() <= curIndex) {
				vh.rl2.setVisibility(View.INVISIBLE);
			}
			// 데이터가 존재할 경우
			else if (iList.get(curIndex).iUrl != null) {
				vh.rl2.setVisibility(View.VISIBLE);
				vh.rl2.setBackgroundResource(android.R.color.transparent);
				vh.ck2.setTag("" + iList.get(curIndex).iUid);
				vh.ck2.setVisibility(View.VISIBLE);
				boolean flag1 = false;
				for (int i = 0; i < icheckedList.size(); i++) {
					if (icheckedList.get(i).intValue() == iList.get(curIndex).iUid) {
						flag1 = true;
						break;
					}
				}
				vh.ck2.setChecked(flag1);
				// checkbox 확인
				final int iUid2 = iList.get(curIndex).iUid;
				vh.ck2.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						if (isChecked) {
							boolean flag = false;
							for (int i = 0; i < icheckedList.size(); i++) {
								if (icheckedList.get(i).intValue() == iUid2) {
									flag = true;
								}
							}
							if (flag == false)
								icheckedList.add(iUid2);
						} else {
							for (int i = 0; i < icheckedList.size(); i++) {
								if (icheckedList.get(i).intValue() == iUid2) {
									icheckedList.remove(i);
								}
							}
						}
						Log.e("pkch",isChecked+" : "+iUid2);
					}
				});
				vh.iv2.setTag("" + iList.get(curIndex).iUid);
				vh.iv2.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						removeImage(iUid2);
					}
				});
				BitmapHandlerQuestion bhm = new BitmapHandlerQuestion(iList.get(curIndex).iUrl, vh.iv2, true, iList.get(curIndex).iUid);
				bhm.execute();
			}
			// 데이터가 존재하지 않을 경우
			else {
				vh.rl2.setVisibility(View.VISIBLE);
				vh.rl2.setBackgroundResource(R.drawable.dot_border);
				vh.rl2.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						registImage();
					}
				});
				vh.iv2.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						registImage();
					}
				});
				vh.iv2.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_photo));
				vh.ck2.setVisibility(View.INVISIBLE);
			}
			curIndex++;

			// slot 갯수 초과 했을 경우
			vh.rl3.setOnClickListener(null);
			vh.iv3.setOnClickListener(null);
			if (iList.size() <= curIndex) {
				vh.rl3.setVisibility(View.INVISIBLE);
			}
			// 데이터가 존재할 경우
			else if (iList.get(curIndex).iUrl != null) {
				vh.rl3.setVisibility(View.VISIBLE);
				vh.rl3.setBackgroundResource(android.R.color.transparent);
				vh.ck3.setTag("" + iList.get(curIndex).iUid);
				vh.ck3.setVisibility(View.VISIBLE);
				boolean flag1 = false;
				for (int i = 0; i < icheckedList.size(); i++) {
					if (icheckedList.get(i).intValue() == iList.get(curIndex).iUid) {
						flag1 = true;
						break;
					}
				}
				vh.ck3.setChecked(flag1);
				// checkbox 확인
				final int iUid3 = iList.get(curIndex).iUid;
				vh.ck3.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						if (isChecked) {
							boolean flag = false;
							for (int i = 0; i < icheckedList.size(); i++) {
								if (icheckedList.get(i).intValue() == iUid3) {
									flag = true;
								}
							}
							if (flag == false){
								icheckedList.add(iUid3);
								Log.e("pkch",iUid3+" added ");
							}
						} else {
							for (int i = 0; i < icheckedList.size(); i++) {
								if (icheckedList.get(i).intValue() == iUid3) {
									Log.e("pkch",icheckedList.get(i).intValue()+" removed");
									icheckedList.remove(i);
								}
							}
						}
					}
				});
				vh.iv3.setTag("" + iList.get(curIndex).iUid);
				vh.iv3.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						removeImage(iUid3);
					}
				});
				BitmapHandlerQuestion bhm = new BitmapHandlerQuestion(iList.get(curIndex).iUrl, vh.iv3, true, iList.get(curIndex).iUid);
				bhm.execute();
			}
			// 데이터가 존재하지 않을 경우
			else {
				vh.rl3.setVisibility(View.VISIBLE);
				vh.rl3.setBackgroundResource(R.drawable.dot_border);
				vh.rl3.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						registImage();
					}
				});
				vh.iv3.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						registImage();
					}
				});
				vh.iv3.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_photo));
				vh.ck3.setVisibility(View.INVISIBLE);
			}
			return convertView;
		}

	}

	class ViewHolder {
		RelativeLayout rl1, rl2, rl3;
		ImageView iv1, iv2, iv3;
		CheckBox ck1, ck2, ck3;
	}

	class BitmapHandlerQuestion extends AsyncTask<String, Void, String> {
		String imgUrl;
		ImageView iv;
		boolean cacheFlag;
		Bitmap bmp;
		GetUrlImageCacheFile gcf;
		int iUid;

		public BitmapHandlerQuestion(String imgUrl, ImageView iv, boolean cacheFlag, int iUid) {
			this.imgUrl = imgUrl;
			this.iv = iv;
			this.cacheFlag = cacheFlag;
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
			if (bmp == null)
				return;
			if (iv.getTag().toString().equals("" + iUid)) {
				iv.setImageBitmap(bmp);
				Animation myFadeInAnimation = AnimationUtils.loadAnimation(QuestionActivity.this, R.anim.anim_imgv);
				iv.startAnimation(myFadeInAnimation);
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// 앨범 선택함
		if (requestCode == ITWApplication.Q_IMAGE_LOAD_FROM_SD) {
			noResumeChk = true; // onresume을 실행하지 않음
			if (resultCode == RESULT_OK) {
				String imgData = data.getData().toString();
				String imgPath = Utils.getRealPathFromURI(this, Uri.parse(imgData));
				if (imgPath == null)
					return;
				int chkIndex = imgPath.lastIndexOf("https://");
				if (chkIndex >= 0) {
					Toast.makeText(QuestionActivity.this, getString(R.string.q_img_type_error), Toast.LENGTH_SHORT).show();
					return;
				}

				// 이미지 업로딩
				if (pd == null) {
					pd = new ProgressDialog(this);
					pd.setTitle(getString(R.string.image_uploading));
					pd.setCancelable(false);
					pd.show();
				}

				// 이미지 업로딩~~
				map = new HashMap<String, String>();
				map.put("mUid", "" + ITWApplication.USERID);
				ImageUploadTask task = new ImageUploadTask(ITWApplication.registImage, imgPath, map);
				task.execute();
			}
		}
	}

	class ImageUploadTask extends AsyncTask<String, Void, String> {
		private String imgPath;
		private HashMap<String, String> map;
		private HashMap<String, String> resultMap;
		private String command;
		private Handler handler;

		public ImageUploadTask(String command, String imgPath, HashMap<String, String> map) {
			this.command = command;
			this.imgPath = imgPath;
			this.map = map;
			this.handler = handler;
		}

		@Override
		protected String doInBackground(String... params) {
			HttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(ITWApplication.ACTION_URL);
			File file = new File(imgPath);
			FileBody fileBody = new FileBody(file);
			MultipartEntity mEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
			mEntity.addPart("imgData", fileBody);
			try {
				mEntity.addPart("c", new StringBody(command));
				mEntity.addPart("mUid", new StringBody(ITWApplication.USERID));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			httpPost.setEntity(mEntity);

			// execute Http Post request
			HttpResponse response;
			try {
				response = httpClient.execute(httpPost);
				HttpEntity resEntity = response.getEntity();
				if (resEntity != null) {
					String responseStr = EntityUtils.toString(resEntity).trim();
					resultMap = Utils.parseXmltoMap(responseStr);
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			if (pd != null) {
				pd.dismiss();
				pd = null;
			}
			if (resultMap != null && resultMap.get("status").toString().equals("1")) {
				// 등록이 정상적으로 완료 될 경우 onresume 실행
				Toast.makeText(QuestionActivity.this, getString(R.string.q_img_upload_success), Toast.LENGTH_SHORT).show();
				noResumeChk = false;
				onResume();
			} else {
				// 실패 하였을 경우 실패 메시지 활용
				Toast.makeText(QuestionActivity.this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
			}
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.question_registquestion_iv:
			// 질문 등록하기~~
			AlertDialog.Builder ab = new AlertDialog.Builder(QuestionActivity.this);
			LayoutInflater inflater = getLayoutInflater();
			View abView = inflater.inflate(R.layout.question_registquestion_popup, null);
			((EditText) abView.findViewById(R.id.qr_popup_question_edt)).setHint(getString(R.string.question_regist_popup_msg));
			ab.setCancelable(false);
			ab.setView(abView).setNegativeButton(getString(R.string.cancle_msg), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {

				}
			}).setPositiveButton(getString(R.string.confirm_msg), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					EditText edt = (EditText) ((AlertDialog) dialog).findViewById(R.id.qr_popup_question_edt);
					String comment = edt.getText().toString();
					map = new HashMap<String, String>();
					map.put("c", ITWApplication.registQuestion);
					map.put("mUid", ITWApplication.USERID);
					map.put("qType", "2");
					map.put("qRound", "4");
					map.put("qTitle", comment);
					ntw = new NetworkITW(map, questionHandler);
					ntw.execute();
				}
			});
			ab.show();
			break;
		case R.id.question_cancle_iv:
			AlertDialog.Builder ab2 = new AlertDialog.Builder(QuestionActivity.this);
			ab2.setCancelable(false);
			ab2.setTitle(getString(R.string.question_out)).setNegativeButton(getString(R.string.cancle_msg), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			}).setPositiveButton(getString(R.string.confirm_msg), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					onBackPressed();
				}
			});
			ab2.show();
			break;
		case R.id.question_information_iv:
			showInformation();
			break;
		}
	}

	private void removeImage(final int iUid) {
		AlertDialog.Builder ab = new AlertDialog.Builder(QuestionActivity.this);
		ab.setCancelable(false);
		ab.setTitle(getString(R.string.warning_remove_img_msg)).setNegativeButton(getString(R.string.cancle_msg), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

			}
		}).setPositiveButton(getString(R.string.confirm_msg), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (pd != null) {
					pd = new ProgressDialog(QuestionActivity.this);
					pd.setCancelable(false);
					pd.show();
				}
				map = new HashMap<String, String>();
				map.put("c", ITWApplication.removeImage);
				map.put("mUid", ITWApplication.USERID);
				map.put("iUid", iUid + "");
				ntw = new NetworkITW(map, questionHandler);
				ntw.execute();
			}
		});
		ab.show();
	}

	private void showInformation() {
		((LinearLayout) findViewById(R.id.question_content_ll)).setVisibility(View.GONE);
		ViewPager vp = (ViewPager) findViewById(R.id.help_viewpager);
		vp.setVisibility(View.VISIBLE);
		vp.setAdapter(new HelpViewPagerAdpater(this));
		/*
		 * Toast.makeText(QuestionActivity.this,
		 * getString(R.string.question_first_msg1), Toast.LENGTH_LONG).show();
		 * Toast.makeText(QuestionActivity.this,
		 * getString(R.string.question_first_msg2), Toast.LENGTH_LONG).show();
		 * Toast.makeText(QuestionActivity.this,
		 * getString(R.string.question_first_msg3), Toast.LENGTH_LONG).show();
		 * Toast.makeText(QuestionActivity.this,
		 * getString(R.string.question_first_msg4), Toast.LENGTH_LONG).show();
		 * Toast.makeText(QuestionActivity.this,
		 * getString(R.string.question_first_msg5), Toast.LENGTH_LONG).show();
		 * Toast.makeText(QuestionActivity.this,
		 * getString(R.string.question_first_msg6), Toast.LENGTH_LONG).show();
		 */
	}

	class HelpViewPagerAdpater extends PagerAdapter {
		int[] arr = { R.drawable.h1_1, R.drawable.h1_2, R.drawable.h1_3, R.drawable.h2_1, R.drawable.h2_2, R.drawable.h3_1, R.drawable.h3_2, R.drawable.h3_3, R.drawable.h3_4, R.drawable.h4_1,
				R.drawable.h4_2, R.drawable.h4_3, R.drawable.h5_1 };
		int[] strArr = { R.string.h1_1, R.string.h1_2, R.string.h1_3, R.string.h2_1, R.string.h2_2, R.string.h3_1, R.string.h3_2, R.string.h3_3, R.string.h3_4, R.string.h4_1, R.string.h4_2,
				R.string.h4_3, R.string.h5_1 };
		Context c;

		public HelpViewPagerAdpater(Context c) {
			this.c = c;
		}

		@Override
		public int getCount() {
			return arr.length;
		}

		@Override
		public Object instantiateItem(ViewGroup pager, int position) {
			View v = View.inflate(c, R.layout.question_help_row, null);
			Bitmap bmp = BitmapFactory.decodeResource(getResources(), arr[position]);
			((ImageView) v.findViewById(R.id.qhr_iv)).setImageBitmap(bmp);
			((ImageView) v.findViewById(R.id.qhr_cancle_iv)).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					((LinearLayout) findViewById(R.id.question_content_ll)).setVisibility(View.VISIBLE);
					ViewPager vp = (ViewPager) findViewById(R.id.help_viewpager);
					vp.setVisibility(View.GONE);
				}
			});
			TextView tv = (TextView) v.findViewById(R.id.qhr_tv);
			tv.setText(strArr[position]);
			((ViewPager) pager).addView(v, 0);
			return v;
		}

		@Override
		public void destroyItem(View pager, int position, Object view) {
			((ViewPager) pager).removeView((View) view);
		}

		@Override
		public boolean isViewFromObject(View pager, Object obj) {
			return pager == obj;
		}

	}

	private void registImage() {
		// 이미지 올리기
		intent = new Intent(Intent.ACTION_PICK);
		intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
		startActivityForResult(intent, ITWApplication.Q_IMAGE_LOAD_FROM_SD);
	}

	// 이벤트 초기화
	private void initEvent() {
		questionIv = (ImageView) findViewById(R.id.question_registquestion_iv);
		cancleIv = (ImageView) findViewById(R.id.question_cancle_iv);
		informationIv = (ImageView) findViewById(R.id.question_information_iv);
		roundSpinner = (Spinner) findViewById(R.id.question_round_spinner);
		typeSpinner = (Spinner) findViewById(R.id.question_type_spinner);
		questionListView = (ListView) findViewById(R.id.question_quesiton_lv);
		imageListView = (ListView) findViewById(R.id.question_image_lv);
		userInfoTxt = (TextView) findViewById(R.id.question_userinfo_txt);

		questionIv.setOnClickListener(this);
		cancleIv.setOnClickListener(this);
		informationIv.setOnClickListener(this);
	}

	// 광고 초기화
	private void initAd() {
		adView = new AdView(this, AdSize.BANNER, ITWApplication.MY_AD_UNIT_ID);
		LinearLayout layout = (LinearLayout) findViewById(R.id.question_btmLl);
		adView.loadAd(new AdRequest());
		layout.addView(adView);
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

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		intent = new Intent(this, MainActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.anim_main_in, R.anim.anim_action_out);
		finish();
	}
}
