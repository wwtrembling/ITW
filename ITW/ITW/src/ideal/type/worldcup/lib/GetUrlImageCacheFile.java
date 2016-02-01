package ideal.type.worldcup.lib;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.util.Log;

public class GetUrlImageCacheFile {
	private Bitmap bitmap;
	private boolean cacheFlag;
	public GetUrlImageCacheFile(String imgUrl, boolean cacheFlag){
		this.cacheFlag=cacheFlag;
		
		File root, newDir, newFile=null;
		String targDir, targetFileName, fullFilePath=null;
		
		//if cache is enable
		if(cacheFlag==true){
			root = android.os.Environment.getExternalStorageDirectory();
			targDir=root.getAbsolutePath()+"/ITWimg";
			newDir= new File(targDir);
			newDir.mkdir();
			targetFileName=imgUrl;
			targetFileName=targetFileName.replace("facebook.com", "fc_");
			targetFileName=targetFileName.replace("wwtrembling.cafe24.com", "wc_");
			targetFileName=targetFileName.replace("http://", "");
			targetFileName=targetFileName.replace("/", "");
			fullFilePath=targDir+"/"+targetFileName;
			newFile= new File(fullFilePath);
		}
		
		//if file exists
		if(newFile!=null && newFile.isFile()){
			bitmap= BitmapFactory.decodeFile(fullFilePath);
		}
		//if file does not exist
		else{
			URL url=null;
			try {
				url = new URL(imgUrl);
			    bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
			    if(cacheFlag==true) saveSD(newFile, bitmap); // if cacheFlag is enable
			} catch (MalformedURLException e) {
				//e.printStackTrace();
			} catch (IOException e) {
				//e.printStackTrace();
			}
		}
	}
	
	private void saveSD(File newFile, Bitmap bmp){
		if(bmp==null) return;
		try {
			FileOutputStream fos= new FileOutputStream(newFile);
			BufferedOutputStream bos= new BufferedOutputStream(fos);
			bmp.compress(CompressFormat.JPEG, 100, bos);
		} catch (FileNotFoundException e) {
			//e.printStackTrace();
		}
	}
	
	public Bitmap getBmp(){
		return bitmap;
	}
}