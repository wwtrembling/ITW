package ideal.type.worldcup.inc;

import android.app.Application;

public class ITWApplication extends Application{
	static public final String MY_AD_UNIT_ID = "a152e9b816ea8fb";
	static public final String DEVICE_ID="!asij#j";
	static public final String ACTION_URL="http://wwtrembling.cafe24.com/itw/mobile/itw.php";
	static public final int FB_LOGIN=1000;
	static public final int FB_LOGOUT=1001;
	
	
	static public final int Q_IMAGE_LOAD_FROM_SD=100;
	
	static public String USERFID;
	static public String USERID;
	static public String USERNAME;
	static public String FUIDS;
	static public String FFIDS;
	
	
	//SERVER Communcation FLAG
	static public String chkUserProfile="chkUserProfile";
	static public String getQuestionList="getQuestionList";
	static public String getQuestionData="getQuestionData";
	static public String putGoResult="putGoResult";
	static public String getGoResult="getGoResult";
	static public String registGoResult="registGoResult";
	static public String removeGoResult="removeGoResult";
	
	// question
	static public String registQuestion="registQuestion";			//regist
	static public String modifyQuestion="modifyQuestion";		//modify
	static public String removeQuestion="removeQuestion";		//remove
	
	static public String registImage="registImage";
	static public String removeImage="removeImage";
	static public String getUserImages="getUserImages";
	static public String getUserInfo="getUserInfo";
	static public String registImageToQuestion="registImageToQuestion";
}
