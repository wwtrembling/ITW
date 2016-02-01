package ideal.type.worldcup.sax;

import android.os.Parcel;
import android.os.Parcelable;

public class ImagesItem implements Parcelable {
	public int iUid;
	public String iUrl;

	public ImagesItem() {
	}

	public static final Parcelable.Creator<ImagesItem> CREATOR = new Creator<ImagesItem>() {
		@Override
		public ImagesItem[] newArray(int size) {
			return new ImagesItem[size];
		}

		@Override
		public ImagesItem createFromParcel(Parcel source) {
			return new ImagesItem();
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
	}
}
