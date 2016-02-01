package ideal.type.worldcup.sax;

import android.os.Parcel;
import android.os.Parcelable;

public class CommentItem implements Parcelable {
	public int cUid;
	public int mUid;
	public String comment;
	public String mName;

	public CommentItem() {
	}

	public static final Parcelable.Creator<CommentItem> CREATOR = new Creator<CommentItem>() {
		@Override
		public CommentItem[] newArray(int size) {
			return new CommentItem[size];
		}

		@Override
		public CommentItem createFromParcel(Parcel source) {
			return new CommentItem();
		}
	};

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(cUid);
		dest.writeInt(mUid);
		dest.writeString(comment);
		dest.writeString(mName);
	}
}
