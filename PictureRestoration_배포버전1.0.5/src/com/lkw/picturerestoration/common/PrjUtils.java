	package com.lkw.picturerestoration.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

/**
 * 프로젝트 유틸리티 메소드 저장소 
 * @FileName  : PrjUtils.java
 * @Project     : TabSjimis
 * @Date         : 2011. 12. 7. 
 * @작성자      : WIPIA1

 * @변경이력 :
 * @프로그램 설명 :
 */
public class PrjUtils {

	private static final String TAG = "PrjUtils";	// 로그켓 태그	
	private static boolean isDebug = false; 	    // 개발 종료시 true 로 변경	

	
	public PrjUtils(){		
	}
	
	
	/**
	 * 
	 * @Method Name  : debug
	 * @작성일   : 2011. 12. 7. 
	 * @작성자   : WIPIA1
	 * @변경이력  :
	 * @Method 설명 : 에외 처리 로그켓 보이기 
	 * @param _tag		-- 로그켓 태그 
	 * @param _message	-- 로그 메시지
	 */
	public static void debug(String _tag , String _message){
		if (!isDebug)
		{
			Log.d(_tag , _message);
		}
	}
	
	/**
	 * 
	 * @Method Name  : debug
	 * @작성일   : 2011. 12. 7. 
	 * @작성자   : WIPIA1
	 * @변경이력  :
	 * @Method 설명 : 에외 처리 로그켓 보이기 
	 * @param _tag		-- 로그켓 태그 
	 * @param _message	-- 로그 메시지
	 * @param _e		-- 예외 상황
	 */
	public static void debug(String _tag , String _message , Exception _e){
		if (!isDebug)
		{
			Log.e(_tag, _message, _e);
			
			FileOutputStream fos = null;
			File file = new File(Environment.getExternalStorageDirectory() + "/Log_Exception.txt");
			try{
				if (!file.exists()) {
					file.createNewFile();
				}
				fos = new FileOutputStream(file);
				_message = _message + " : " + _e + "\n";
				fos.write(_message.getBytes());
				fos.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
				
			finally {
		      if (fos != null) {
		    	  
		        try {
		        	fos.close();
		        } catch (IOException e) {
		        	PrjUtils.debug(TAG, "IOException : ", e);
		        }
		      }
		    }
		}
	}
	
	public static void debug_f(String _tag , String _message){
		if (!isDebug)
		{
			Log.d(_tag, _message);
			Log.d(_tag, " Environment.getExternalStorageDirectory() : " + Environment.getExternalStorageDirectory());
			FileOutputStream fos = null;
			File file = new File(Environment.getExternalStorageDirectory() + "/Log_Exception.txt");
			try{
				if (!file.exists()) {
					file.createNewFile();
				}
				fos = new FileOutputStream(file);
				_message = _message + "\n";
				fos.write(_message.getBytes());
				fos.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
				
			finally {
		      if (fos != null) {
		    	  
		        try {
		        	fos.close();
		        } catch (IOException e) {
		        	PrjUtils.debug(TAG, "IOException : ", e);
		        }
		      }
		    }
		}
	}


	/**
	 * 
	 * @Method Name  : callPhone
	 * @작성일   : 2011. 12. 7. 
	 * @작성자   : WIPIA1
	 * @변경이력  :
	 * @Method 설명 : 전화 걸기 
	 * @param _activity
	 * @param telNo
	 */
	public static void callPhone(Activity _activity , String _telNo){
		Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+_telNo));
		_activity.startActivity(intent);
	}
	
	
	/**
	 * 
	 * @Method Name  : callBrowser
	 * @작성일   : 2011. 12. 7. 
	 * @작성자   : WIPIA1
	 * @변경이력  :
	 * @Method 설명 :  브라우저 호출 하기 
	 * @param _activity
	 * @param url
	 */
	public static void callBrowser(Activity _activity , String _url){
		Intent intent = new Intent(Intent.ACTION_VIEW);
		Uri uri = Uri.parse(_url);
		intent.setData(uri);
		_activity.startActivity(intent);
	}

	/**
	 * 
	 * @Method Name  : getSpritString
	 * @작성일   : 2011. 12. 7. 
	 * @작성자   : WIPIA1
	 * @변경이력  :
	 * @Method 설명 : 특수문자 제거하기 
	 * @param data	 -- 전체 문자
	 * @param expression -- 제거할 문자  
	 * @return
	 */
	public static String getSpritString(String _data , String _expression){
		String[] arrDate = _data.split(_expression);
		String rtnStr = "";
		for (int i = 0 ; i < arrDate.length ; i++){
			rtnStr += arrDate[i];
		}
		return rtnStr ; 
	}
	
	/**
	 * 
	 * @Method Name  : getSplitTellNo1
	 * @작성일   : 2011. 12. 7. 
	 * @작성자   : WIPIA1
	 * @변경이력  :
	 * @Method 설명 : 문자 전화 번호 형식으로 변경 하기 
	 * @param noStr
	 * @return
	 */
	public static String[] getSplitTellNo1(String _noStr) {
    	Pattern tellPattern = Pattern.compile( "^(01\\d{1}|02|0505|0502|0506|0\\d{1,2})-?(\\d{3,4})-?(\\d{4})");
        if(_noStr == null) return new String[]{ "", "", ""};
        Matcher matcher = tellPattern.matcher( _noStr);       
        if(matcher.matches()) {
        	return new String[]{ matcher.group( 1), matcher.group( 2), matcher.group( 3)};
        } else {
        	return new String[]{ "", "", ""};
        }
	}
	
	/**
	 * 사진폴더 위치 정하기
	 * @Method Name  : getPicPath
	 * @작성일   : 2012.	 2. 9. 
	 * @작성자   : WIPIA2
	 * @변경이력  :
	 * @Method 설명 : 사진폴더 위치 정하기
	 * @param
	 */
	public static String getPicPath(){		
		String PICFOLDER = "/TabSjimis/";		// sd카드에 저장될 폴더명
		String path = Environment.getExternalStorageDirectory().getAbsolutePath()+PICFOLDER;		
		
		return path;
	}
	
	/**
	 * 사진명 정하기
	 * @Method Name  : setTextSize
	 * @작성일   : 2012.	 2. 9. 
	 * @작성자   : WIPIA2
	 * @변경이력  :
	 * @Method 설명 : 파일명을 만든다
	 * @param fileName
	 */
	public static String getPicName(String fileName){
		Calendar calendar = Calendar.getInstance();
		String picName = "";
		
		picName = String.format(fileName+"_%02d%02d%02d-%02d%02d%02d.jpg",
						calendar.get(Calendar.YEAR)%100,
						calendar.get(Calendar.MONTH)+1,
						calendar.get(Calendar.DAY_OF_MONTH),
						calendar.get(Calendar.HOUR_OF_DAY),
						calendar.get(Calendar.MINUTE),
						calendar.get(Calendar.SECOND));
		
		return picName;
	}
	
	/**
	 * 키보드 나타내기
	 * @Method Name  : showKeyboard
	 * @작성일   : 2012.	 2. 23. 
	 * @작성자   : WIPIA2
	 * @변경이력  :
	 * @Method 설명 : 키보드를 나타낸다.
	 * @param 
	 */
	public static void showKeyboard(Activity _activity){
		InputMethodManager inputMethodManager = (InputMethodManager)_activity.getSystemService(Context.INPUT_METHOD_SERVICE);		
//		inputMethodManager.showSoftInput(wET_txt, 0);
		inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
	}
	
	/**
	 * 키보드 숨기기
	 * @Method Name  : hideKeyboard
	 * @작성일   : 2012.	 2. 23. 
	 * @작성자   : WIPIA2
	 * @변경이력  :
	 * @Method 설명 : 키보드를 숨긴다.
	 * @param wET_txt
	 */
	public static void hideKeyboard(Context context, EditText wET_txt){
		InputMethodManager inputMethodManager = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(wET_txt.getWindowToken(), 0);
	}
	
	/**
	 * 검사항목 텍스트 크기 설정하기 
	 * @Method Name  : setTextSize
	 * @작성일   : 2011. 12. 27. 
	 * @작성자   : WIPIA1
	 * @변경이력  :
	 * @Method 설명 :
	 * @param item
	 */
	public static void setTextSize(TextView item){
		int valueLen = item.getText().toString().length() ; 
		String value = item.getText().toString().trim();
		// 2자일 경우 
		if (valueLen <= 2){
			item.setText(value);
			item.setTextSize(20);
		}
		// 3자일 경우 
		else if (valueLen == 3){
			
			item.setText(value);
			item.setTextSize(15);
		}
		// 4자일 경우 
		else if (valueLen == 4){
			item.setTextSize(15);
			StringBuffer str  = new StringBuffer() ; 
			str.append(value.substring(0, 2));
			str.append("\n");
			str.append(value.substring(2));
			
			item.setText(str.toString());
			str = null ; 
			
		}
		// 5자일 경우 
		else if (valueLen == 5){
			item.setText(value);
			item.setTextSize(13);
		}
		// 6자일 경우 
		else if (valueLen == 6){
			item.setText(value);
			item.setTextSize(12);
		}
		// 7자일 경우 
		else if (valueLen == 7){
			item.setText(value);
			item.setTextSize(9);
		}
		// 8자일 경우 
		else if (valueLen >= 8 && valueLen < 13){
			item.setText(value);
			item.setTextSize(9);
		}
		// 13자일 경우 
		else if (valueLen >= 13 && valueLen < 20){
			item.setText(value);
			item.setTextSize(9);
		}
		// 20자일 경우 
		else if (valueLen >= 20){
			item.setText(value);
			item.setTextSize(9);
		}
		
	}
	
	
	/**
	 * 스캐닝 요청
	 * @Method Name  : requestScaning
	 * @작성일   : 2012. 2. 24. 
	 * @작성자   : WIPIA2
	 * @변경이력  :
	 * @Method 설명 : 이미지를 스캐닝 요청한다.
	 */
	public static void requestScanning(Activity _activity, String path_fileName){
		Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		Uri uri = Uri.parse("file://"+path_fileName);
		intent.setData(uri);
		_activity.sendBroadcast(intent);		
	}
	
	
	/**
	 *
	 * @Method Name  : isMoutedSD() 
	 * @작성일   : 2011. 12. 16. 
	 * @작성자   : WIPIA2
	 * @변경이력  :
	 * @Method 설명 : SD카드가 없을 시 에러 처리한다.
	 */
	public static void isMountedSd(ActivityEx _activity,String path){		
		String ext = Environment.getExternalStorageState();
		if(ext.equals(Environment.MEDIA_MOUNTED) == false){
			_activity.showToast("SD 카드가 반드시 필요합니다.");
			_activity.finish();
			return;
		}
		
		File fRoot = new File(path);
		if(fRoot.exists() == false){
			if(fRoot.mkdir() == false){
				_activity.showToast("사진을 저장할 폴더가 없습니다.");
				_activity.finish();
				return;
			}
		}
		
	}
	
	
	/**
	 * IMEI 값을 가져온다.
	 * @param _activity
	 * @return
	 */
	public static String getSerialNum(Activity _activity){
		TelephonyManager telephonyManager = (TelephonyManager)_activity.getSystemService(Context.TELEPHONY_SERVICE);
		String serialNum = telephonyManager.getDeviceId();		// 디바이스ID
//		String serialNum = android.os.Build.SERIAL;		// 시리얼넘버
		return serialNum;
	}
	
	/**
	 * macAddress 가져온다.
	 * @param _activity
	 * @return
	 */
	public static String getMacAddr(Activity _activity){
		WifiManager wifiManager = (WifiManager)_activity.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();		
		String macAddr = wifiInfo.getMacAddress();
		
		return macAddr;		
	}
	
	/**
	 * macAddress 가져온다.
	 * @param _activity
	 * @return
	 */
	public static String getSsid(Activity _activity){
		WifiManager wifiManager = (WifiManager)_activity.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		String ssid = wifiInfo.getSSID();
		
		return ssid;
	}
		
	/**
	 * 폰번호를 가져온다
	 * @param _activity
	 * @return
	 */
	public static String getPhoneNumber(Activity _activity)
	{
	 TelephonyManager mgr = (TelephonyManager)_activity.getSystemService(Context.TELEPHONY_SERVICE);
	 return mgr.getLine1Number();
	}

	

	
	/**
	 * 입력 문자 바이트 구하기 (한글 3byte 로 처리되는 부분을 2byte 처리)
	 * @param text 입력 문자
	 * @return 입력 문자 바이트
	 */
	public static int getByte(String text){
		try {
			return text.getBytes("EUC-KR").length;
		} catch (UnsupportedEncodingException e) {
			return 1;
		}
	}
	
	
	/**
	 * 현재 일자를 가져온다.
	 * @return
	 */
	public static String getDate() {
		Calendar cal = Calendar.getInstance();
		int yy = cal.get(Calendar.YEAR);
		int mo = cal.get(Calendar.MONTH)+1;
		int dd = cal.get(Calendar.DAY_OF_MONTH);
			
		String yyy = null; 
		String mmo = null;
		String ddd = null;
		
		yyy = "" + yy;
		if(mo < 10) mmo = "0" + mo;
		else mmo = "" + mo;
		if(dd < 10) ddd = "0" + dd;
		else ddd = "" + dd;

		String addDate = "" + yyy + mmo + ddd;
		return addDate;
	}
	
	/**
	 * 현재 시간을 가져온다.
	 * @return
	 */
	public static String getTime() {
		Calendar cal = Calendar.getInstance();
		int hh = cal.get(Calendar.HOUR_OF_DAY);
		int mm = cal.get(Calendar.MINUTE);
		int ss = cal.get(Calendar.SECOND);
			
		String hhh = null;
		String mmm = null;
		String sss = null;
			
		if(hh < 10) hhh = "0" + hh;
		else hhh = "" + hh;
		if(mm < 10) mmm = "0" + mm;
		else mmm = "" + mm;
		if(ss < 10) sss = "0" + ss;
		else sss = "" + ss;

		String addTime = "" + hhh + mmm + sss;
		return addTime;
	}
	
}
