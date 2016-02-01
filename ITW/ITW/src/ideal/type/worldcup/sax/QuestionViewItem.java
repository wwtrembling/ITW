package ideal.type.worldcup.sax;

import android.os.Parcel;
import android.os.Parcelable;

public class QuestionViewItem implements Parcelable {
	public int iUid;
	public String iUrl;
	public int iHits;
	public int cCount;
	public String thumbUrl; 
	public QuestionViewItem(){
		
	}
	
	public static final Parcelable.Creator<QuestionViewItem> CREATOR = new Parcelable.Creator<QuestionViewItem>() {
        public QuestionViewItem createFromParcel(Parcel in) {
            //return new QuestionViewItem(in);
        	return new QuestionViewItem();
        }

        public QuestionViewItem[] newArray(int size) {
            return new QuestionViewItem[size];
        }
    };
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(iUid);
		dest.writeString(iUrl);
		dest.writeString(thumbUrl);
	}
}
