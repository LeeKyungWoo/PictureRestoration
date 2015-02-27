package com.lkw.picturerestoration.common;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.lkw.picturerestoration.MainActivity;
import com.lkw.picturerestoration.R;
@SuppressLint({ "SimpleDateFormat", "Wakelock" })
public class ActivityEx extends SherlockActivity {
	private static final String TAG = "ActivityEx"; // 로그켓 태그
	public static ProgressDialog initProgressDialog = null;
	public static Dialog dialog = null;
	public ApplicationClass applicationClass;
    public AlertDialog.Builder dialogBuilder = null;
    public AlertDialog mPopupDlg = null;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		applicationClass = (ApplicationClass) getApplicationContext();
	}

	@Override
	protected void onResume() {
		super.onResume();

	}

	@Override
	protected void onPause() {
		super.onPause();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	/**
	 * @Method Name : dismissDialog
	 * @Method 설명 : 다이얼로그 닫음
	 */
	public void dismissDialog() {
		if(dialogBuilder != null){
	        dialogBuilder = null;
		}
		if(mPopupDlg != null){
			mPopupDlg.dismiss();
			mPopupDlg = null;
		}
	}

	/**
	 * 프로그래스다이얼로그
	 * 
	 * @param message
	 * @param style
	 */
	public void setInitDataProgress(final String message, final int style) {

		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try {
					if (initProgressDialog != null) {
						if (initProgressDialog.isShowing()) {
							initProgressDialog.dismiss();
						}
						initProgressDialog = null;
					}
					initProgressDialog = new ProgressDialog(ActivityEx.this);
					initProgressDialog.setProgressStyle(style);
					if(isTablet()){
						initProgressDialog.setMessage(Html.fromHtml("<big>"+message+"</big>"));
					}else{
						initProgressDialog.setMessage(message);
					}
					initProgressDialog.setCancelable(false);
					initProgressDialog.show();
					if (!isFinishing()) {
						initProgressDialog.show();
					}
				} catch (Exception e) {
					PrjUtils.debug(TAG, "setInitDataProgress : ", e);
				}
			}
		});
	}

	/**
	 * 
	 * @Method Name : exitProgressDialog
	 * @작성일 : 2011. 12. 8.
	 * @작성자 : WIPIA1
	 * @변경이력 :
	 * @Method 설명 : 프로그래스 다이얼로그 종료
	 */
	public void stopProgressDialog() {
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (initProgressDialog != null) {
					initProgressDialog.dismiss();
				}
			}
		});
	}

	/**
	 * @Method Name : AlertDialog
	 * @작성일 : 2013. 3. 4.
	 * @작성자 : WIPIA1
	 * @변경이력 :
	 * @Method 설명 : 원버튼 다이얼로그
	 * @param strTitle        - title
	 * @param strContent      - 내용
	 * @param btnAText        - 버튼A text
	 * @param onClickListener - 버튼A 리스너
	 */
	public void AlertDialog(final String strTitle, final String strContent, final String btnAText, final DialogInterface.OnClickListener onClickListener) {
		if(dialogBuilder != null){
	        dialogBuilder = null;
		}
		if(mPopupDlg != null){
			mPopupDlg.dismiss();
			mPopupDlg = null;
		}
		runOnUiThread(new Runnable() {

			@Override
			public void run() {			
			    dialogBuilder = new AlertDialog.Builder(ActivityEx.this);
			    dialogBuilder.setTitle(strTitle);
			    dialogBuilder.setMessage(strContent);
			    dialogBuilder.setCancelable(false);		    	
		        dialogBuilder.setPositiveButton(btnAText, onClickListener);
		        mPopupDlg = dialogBuilder.show();

			}
		});

	}
	
	/**
	 * 
	 * @Method Name : AlertDialog
	 * @작성일 : 2013. 3. 4.
	 * @작성자 : WIPIA1
	 * @변경이력 :
	 * @Method 설명 : 투버튼 다이얼로그
	 * @param strTitle        - title
	 * @param strContent      - 내용
	 * @param btnAText        - 버튼A text
	 * @param btnBText        - 버튼B text
	 * @param onClickListener - 버튼A 리스너
	 * @param onClickListener - 버튼B 리스너
	 */
	public void AlertDialog(final String strTitle, final String strContent, final String btnAText,
			final String btnBText,  final DialogInterface.OnClickListener listener1,
			final DialogInterface.OnClickListener listener2) {
		
		if(dialogBuilder != null){
	        dialogBuilder = null;
		}
		if(mPopupDlg != null){
			mPopupDlg.dismiss();
			mPopupDlg = null;
		}

		runOnUiThread(new Runnable() {

			public void run() {		
			    dialogBuilder = new AlertDialog.Builder(ActivityEx.this);
			    dialogBuilder.setTitle(strTitle);
			    dialogBuilder.setMessage(strContent);
			    //dialogBuilder.setCancelable(false);		    	
		        dialogBuilder.setPositiveButton(btnAText, listener1);
		        dialogBuilder.setNegativeButton(btnBText, listener2);
		        mPopupDlg = dialogBuilder.show();
			}
		});
	}
	

	/**
	 * 
	 * @Method Name : showToast
	 * @작성일 : 2011. 12. 7.
	 * @작성자 : WIPIA1
	 * @변경이력 :
	 * @Method 설명 : 토스트 메시지 보이기
	 * @param _context - 사용할 컨텍스트
	 * @param _message - 사용할 메시지
	 */
	public void showToast(final String _message) {
		try {
			this.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					try {
						Toast.makeText(ActivityEx.this, _message,
								Toast.LENGTH_SHORT).show();
					} catch (Exception e) {
						PrjUtils.debug(TAG, "1showToast Exception =>", e);
					}
				}
			});
		} catch (Exception e) {
			PrjUtils.debug(TAG, "showToast Exception =>", e);
		}
	}

	/**
	 * 
	 * @Method Name : callActivity
	 * @작성일 : 2011. 12. 7.
	 * @작성자 : WIPIA1
	 * @변경이력 :
	 * @Method 설명 : 엑티비티 호출 (인텐트 변수 없는 경우 - 일반)
	 * @param _activity
	 * @param _targetClass
	 */
	public void callActivity(Class<?> _targetClass) {
		Intent intent = new Intent(this, _targetClass);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		this.startActivity(intent);
		this.finish();
		intent = null;
	}

	/**
	 * 
	 * @Method Name : callActivity
	 * @작성일 : 2011. 12. 7.
	 * @작성자 : WIPIA1
	 * @변경이력 :
	 * @Method 설명 : 엑티비티 호출 (인텐트 변수 존재)
	 * @param _intent
	 * @param _activity
	 */
	public void callActivity(Intent _intent) {
		_intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		this.startActivity(_intent);
		this.finish();

		// _intent = null ;
	}

	/**
	 * 
	 * @Method Name : killProcess
	 * @작성일 : 2011. 12. 7.
	 * @작성자 : WIPIA1
	 * @변경이력 :
	 * @Method 설명 : 사용 종류후 프로세스 정리 하기
	 * @param _context
	 */
	public void killProcess() {

		int sdkVersion = Integer.parseInt(Build.VERSION.SDK);
		if (sdkVersion < 8) {
			String path = Environment.getExternalStorageDirectory()
					.getAbsolutePath() + "/TabSjimis/";
			this.deleteDir(path); // sd카드 폴더삭제
			ActivityManager am = (ActivityManager) this
					.getSystemService(Context.ACTIVITY_SERVICE);
			am.restartPackage(this.getPackageName());
		} else {
			new Thread(new Runnable() {

				public void run() {
					String path = Environment.getExternalStorageDirectory()
							.getAbsolutePath() + "/TabSjimis/";
					deleteDir(path); // sd카드 폴더삭제

					ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
					String name = getApplicationInfo().processName;
					boolean isThread = true;
					while (isThread) {
						List<RunningAppProcessInfo> list = am
								.getRunningAppProcesses();
						for (RunningAppProcessInfo i : list) {
							if (i.processName.equals(name) == true) {
								if (i.importance >= RunningAppProcessInfo.IMPORTANCE_BACKGROUND) {
									am.restartPackage(getPackageName()); // simple
																			// wrapper
																			// of
																			// killBackgrounProcess
								} else
									Thread.yield();
								break;
							}
						}
						isThread = false;
						Thread.interrupted();
					}
				}
			}, "Process Killer").start();
		}
	}

	/**
	 * sd카드안에 폴더와 파일들을 삭제한다.
	 * 
	 * @param path
	 */
	private void deleteDir(String path) {
		File file = new File(path);
		File[] childFileList = file.listFiles();
		try {
			for (File childFile : childFileList) {
				if (childFile.isDirectory()) {
					deleteDir(childFile.getAbsolutePath()); // 하위폴더가 있을시 루프
				} else {
					childFile.delete(); // 하위 파일 삭제
				}
			}
			file.delete(); // 최상위폴더 삭제
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @Method Name : getAppVersion
	 * @작성일 : 2011. 12. 7.
	 * @작성자 : WIPIA1
	 * @변경이력 :
	 * @Method 설명 : App 버전 가져오기
	 * @param _context
	 * @return
	 */
	public String getAppVersion() {
		try {
			PackageInfo pkgInfo = this.getPackageManager().getPackageInfo(
					this.getPackageName(), 0);
			return pkgInfo.versionName;
		} catch (NameNotFoundException e) {
			return null;
		}
	}

	/**
	 * url bitmap 이미지로 변환
	 * 
	 * @param url
	 * @return
	 */
	public Bitmap getImageBitmap(String url) {
		Bitmap bitmap = null;

		try {
			URL aURL = new URL(url);
			URLConnection conn = aURL.openConnection();
			conn.connect();
			InputStream is = conn.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(is);
			BitmapFactory.Options opt = new BitmapFactory.Options();
			opt.inSampleSize = 4;
			bitmap = BitmapFactory.decodeStream(bis, null, opt);

			bis.close();
			is.close();
		} catch (Exception e) {
			PrjUtils.debug(TAG, "Error getting bitmap", e);
		}

		return bitmap;
	}
	
	/**
	 * byte를 bitmap 이미지로 변환
	 * 
	 * @param byteArray
	 * @return
	 */
	public Bitmap byteArrayToBitmap( byte[] byteArray ) {  
	    Bitmap bitmap = BitmapFactory.decodeByteArray( byteArray, 0, byteArray.length ) ;  
	    PrjUtils.debug(TAG, "Bitmap  : "+bitmap);
	    return bitmap ;  
	}  
	

	public void customFunc(Location loc) {
		Toast.makeText(this, "" + loc.getProvider(), Toast.LENGTH_LONG).show();
	}
	
	/**
	 * 현재 기기가 태블릿인지 태블릿이 아닌지 파악
	 * @return
	 */
    protected boolean isTablet()
    {
        int portrait_width_pixel = Math.min(this.getResources().getDisplayMetrics().widthPixels, this.getResources().getDisplayMetrics().heightPixels);
        int dots_per_virtual_inch = this.getResources().getDisplayMetrics().densityDpi;
        float virutal_width_inch = portrait_width_pixel / dots_per_virtual_inch;
         
        return (virutal_width_inch > 2);
    }
    
	/**
	 * SD카드 REFRESH 킷캣적용
	 * 
	 */
    public void refreshSD(final Context c, final File f) {
    	PrjUtils.debug(TAG,"refreshSD()  경로 : " + MainActivity.STRSAVEPATH2 );
    	runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
		    	new SingleMediaScanner(c, f);
			}
		});
	}
}