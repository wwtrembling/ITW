package ideal.type.worldcup.sax;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class QuestionListItem implements Parcelable {
	public int mUid;
	public String uesrImg; // 사용자 이미지
	public String mName;
	public int qUid;
	public int qType;
	public String qTitle;
	public int qRound;
	public int qHits;
	public String qImageUrl;
	public Bitmap userProfileImg;

	public QuestionListItem() {

	}

	public static final Parcelable.Creator<QuestionListItem> CREATOR = new Parcelable.Creator<QuestionListItem>() {
		public QuestionListItem createFromParcel(Parcel in) {
			// return new QuestionListItem(in);
			return new QuestionListItem();
		}

		public QuestionListItem[] newArray(int size) {
			return new QuestionListItem[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flag) {
		dest.writeInt(mUid);
		dest.writeString(uesrImg);
		dest.writeString(mName);
		dest.writeInt(qUid);
		dest.writeInt(qType);
		dest.writeString(qTitle);
		dest.writeInt(qRound);
		dest.writeInt(qHits);
		dest.writeString(qImageUrl);
	}
}
