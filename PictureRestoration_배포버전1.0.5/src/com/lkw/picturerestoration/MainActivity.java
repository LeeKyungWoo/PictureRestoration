package com.lkw.picturerestoration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;
import com.lkw.picturerestoration.common.ActivityEx;
import com.lkw.picturerestoration.common.PrjUtils;

@SuppressLint({ "SdCardPath", "NewApi" })
public class MainActivity extends ActivityEx {
    private static final String MY_AD_UNIT_ID = "a1533f91b03084c"; 	//애드몹 키
	private static final String TAG = "MainActivity";
	public static final String STRSAVEPATH = Environment.getExternalStorageDirectory().getAbsolutePath() +"/Android/data/com.lkw.picturerestoration/PictureFile/"; // 생성할폴더
	public static final String STRSAVEPATH2 = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/"; // 원본파일이위치한폴더 (갤러리 를 참조해야할듯)
	private GridView mGridView = null; // 복구된 파일 출력할 그리드뷰
	private ImageAdapter adapter = null;							// 리스트 어댑터
	public static ArrayList<String> originalFileList = new ArrayList<String>(); // 원본디렉토리 파일리스트
	public static ArrayList<String> copyFileList = new ArrayList<String>(); // 복사디렉토리파일리스트
	public static ArrayList<String> restorationList = new ArrayList<String>(); // 복구할수있는파일만담아놓은리스트
	public static ArrayList<Bitmap> bitmapList = new ArrayList<Bitmap>(); // 복구할 파일 비트맵
	public static File[] originalListFiles = null;
	public static File[] copyListFiles = null;
	public static File makeFile = null;
	public ImageViewHolder viewHolder = null;
	public Bitmap mBitmap = null;
	public boolean isCheck[] ;
	public ArrayList<Integer> isCheckPosition = new ArrayList<Integer>();	//복구할 사진 리스트중 체크박스로 선택한 목록들의 position
	public boolean allCheck = false;
	public int mode = 0;				//0 : 복구 , 1 : 영구삭제
	public TextView nodataTv = null;
	public int mCount = 0;				//복구한 갯수
	
	//광고
	public LinearLayout addmobLL = null;
	public AdView adView = null;
	public static Context context= null;
	
 	@Override
	protected void onCreate(Bundle savedInstanceState) {
		PrjUtils.debug(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initComponents();
		new ProgressDialogTask().execute(originalFileList.size());
		originalListFiles = (new File(STRSAVEPATH2).listFiles());
		if(originalListFiles != null){
			Intent it = new Intent("com.lkw.picturerestoration.common.AppService");
			startService(it);
			PrjUtils.debug(TAG, "서비스 시작");
		}else{
			PrjUtils.debug(TAG, "서비스 시작안함");
		}
	}

	public void onBackPressed() {
		AlertDialog(getResources().getString(R.string.exit_title), getResources().getString(R.string.exit_content), getResources().getString(R.string.yes),getResources().getString(R.string.no), new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				dismissDialog();
				finish();
			}
		}, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				dismissDialog();
			}
		});
	}

	@Override
	protected void onDestroy() {
		PrjUtils.debug(TAG, "onDestroy");
		adView.destroy();
		originalFileList.clear();
		copyFileList.clear();
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		PrjUtils.debug(TAG, "onPause");
		super.onPause();
	}

	@Override
	protected void onResume() {
		PrjUtils.debug(TAG, "onResume");
		super.onResume();
		addmobLL.removeAllViews();
		addmobLL.addView(adView); //레이아웃에 넣기
		adView.loadAd(new AdRequest());  // 광고 새로고침
	}
	/**
	 * 초기화면구성
	 */
	private void initComponents() {
		adView = new AdView(MainActivity.this, AdSize.BANNER, MY_AD_UNIT_ID);
		mGridView = (GridView) findViewById(R.id.gridView1);
		nodataTv = (TextView) findViewById(R.id.textView1);
		addmobLL = (LinearLayout) findViewById(R.id.mainLayout);
		getSupportActionBar().setTitle(R.string.actionbar_title);
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		context = getBaseContext();
		Cursor mCursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,  new String[] { android.provider.MediaStore.Images.ImageColumns.DATA },null,null,null);  
//		while (mCursor.moveToNext()) {	
//			// 커서의 각 칼럼 접근
//			PrjUtils.debug(TAG, "mCursor count : "+ mCursor.getColumnCount());
//			PrjUtils.debug(TAG, "mCursor Id : "+ mCursor.getInt(0));
//			PrjUtils.debug(TAG, "mCursor(0) : "+ mCursor.getString(0));
//			mCount++;
//		}
//		PrjUtils.debug(TAG, "mCount: "+ mCount);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		PrjUtils.debug(TAG, "방향 전환");
		super.onConfigurationChanged(newConfig);
		
		switch (newConfig.orientation) {
		case Configuration.ORIENTATION_LANDSCAPE:

			break;

		case Configuration.ORIENTATION_PORTRAIT:
			
			break;
		}
	}

	/**
	 * 파일 백업하기
	 * 
	 * @param source
	 * @param target
	 * 
	 */
	public static void copyFile(String source, String target) {
//		int mCount = 0; 
//		while (LoadingActivity.mCursor.moveToNext()) {	
//			// 커서의 각 칼럼 접근
//			PrjUtils.debug(TAG, "mCursor count : "+ LoadingActivity.mCursor.getColumnCount());
//			PrjUtils.debug(TAG, "mCursor Id : "+ LoadingActivity.mCursor.getInt(0));
//			PrjUtils.debug(TAG, "mCursor(0) : "+ LoadingActivity.mCursor.getString(0));
//			mCount++;
//			String[] arr = LoadingActivity.mCursor.getString(0).split("/");
//			PrjUtils.debug(TAG, "파일명"+arr[arr.length-1]);
//			//PrjUtils.debug(TAG, "경로22 : "+LoadingActivity.mCursor.getString(0).substring(0, LoadingActivity.mCursor.getString(0).lastIndexOf('/'))+"/");
//			String mPath = LoadingActivity.mCursor.getString(0).substring(0, LoadingActivity.mCursor.getString(0).lastIndexOf('/'));
//			String mPath2 = mPath.substring(0, mPath.lastIndexOf('/'))+"/";
//			PrjUtils.debug(TAG, "원본 경로 : "+mPath2);
//			PrjUtils.debug(TAG, "mCount: "+ mCount);
//		}
		originalListFiles = (new File(STRSAVEPATH2).listFiles());
		if(originalListFiles != null){
			for (File file : originalListFiles) {
				if (file.isDirectory()) {
					if(!file.getName().equals(".thumbnails")){
						// 폴더 이면
						PrjUtils.debug(TAG, "" + file.getName() + "는 폴더이다");
						makeFile = makeDirectory(STRSAVEPATH + file.getName());
						FileInputStream inputStream = null;
						FileOutputStream outputStream = null;
						FileChannel fcin = null;
						FileChannel fcout = null;
						File[] list = file.listFiles();
						for (File filelist : list) {
							if (filelist.getName().endsWith(".jpg")|| filelist.getName().endsWith(".png")|| filelist.getName().endsWith(".GIF")) {
								// 복사 대상이 되는 파일 생성
								File sourceFile = new File(STRSAVEPATH2
										+ file.getName()+"/"+filelist.getName());
								PrjUtils.debug(TAG, "sourceFile  :" + sourceFile);
								PrjUtils.debug(TAG, "복사파일경로 :"+ makeFile+"/"+ filelist.getName());
								// 스트림, 채널 선언
								try {
									// 스트림 생성
									inputStream = new FileInputStream(sourceFile); // 원본파일
									outputStream = new FileOutputStream(makeFile+"/"+ filelist.getName()); // 카피파일
		
									// 채널 생성
									fcin = inputStream.getChannel();
									fcout = outputStream.getChannel();
									// 채널을 통한 스트림 전송
									long size = fcin.size();
									PrjUtils.debug(TAG, "In File Size :" + size);
									PrjUtils.debug(TAG, "원본파일경로 :" + sourceFile);
									fcin.transferTo(0, size, fcout); // 파일의 처음 부터 끝까지,fcout 으로 전송 copy
								} catch (Exception e) {
									e.printStackTrace();
								} finally {
									// 자원 해제
									if(fcout != null){
										try {
											fcout.close();
										} catch (IOException ioe) {
											PrjUtils.debug(TAG,"IOException : "+ ioe.getMessage());
										}
									}
									if(fcin != null){
										try {
											fcin.close();
										} catch (IOException ioe) {
											PrjUtils.debug(TAG,"IOException : "+ ioe.getMessage());
										}
									}
									if(outputStream != null){
										try {
											outputStream.close();
										} catch (IOException ioe) {
											PrjUtils.debug(TAG,"IOException : "+ ioe.getMessage());
										}
									}
									if(inputStream != null){
										try {
											inputStream.close();
										} catch (IOException ioe) {
											PrjUtils.debug(TAG,"IOException : "+ ioe.getMessage());
										}
									}		
									 originalFileList.add(file.getName() +"/"+ filelist.getName());
								}
							}
						}
					}
				} else {
					PrjUtils.debug(TAG, "" + file.getName() + "는 폴더가 아니다");
					if (file.getName().endsWith(".jpg")
							|| file.getName().endsWith(".png")
							|| file.getName().endsWith(".GIF")) {
						// 복사 대상이 되는 파일 생성
						File sourceFile = new File(STRSAVEPATH2
								+ file.getName());
						PrjUtils.debug(TAG, "sourceFile  :" + sourceFile);
						PrjUtils.debug(TAG, "복사파일경로 :"+ target + file.getName());
						FileInputStream inputStream = null;
						FileOutputStream outputStream = null;
						FileChannel fcin = null;
						FileChannel fcout = null;
						// 스트림, 채널 선언
						try {
							// 스트림 생성
							inputStream = new FileInputStream(sourceFile); // 원본파일
							outputStream = new FileOutputStream(target + file.getName()); // 카피파일

							// 채널 생성
							fcin = inputStream.getChannel();
							fcout = outputStream.getChannel();
							// 채널을 통한 스트림 전송
							long size = fcin.size();
							PrjUtils.debug(TAG, "In File Size :" + size);
							PrjUtils.debug(TAG, "원본파일경로 :" + sourceFile);
							fcin.transferTo(0, size, fcout); // 파일의 처음 부터 끝까지,fcout 으로 전송 copy
						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							// 자원 해제
							if(fcout != null){
								try {
									fcout.close();
								} catch (IOException ioe) {
									PrjUtils.debug(TAG,"IOException : "+ ioe.getMessage());
								}
							}
							if(fcin != null){
								try {
									fcin.close();
								} catch (IOException ioe) {
									PrjUtils.debug(TAG,"IOException : "+ ioe.getMessage());
								}
							}
							if(outputStream != null){
								try {
									outputStream.close();
								} catch (IOException ioe) {
									PrjUtils.debug(TAG,"IOException : "+ ioe.getMessage());
								}
							}
							if(inputStream != null){
								try {
									inputStream.close();
								} catch (IOException ioe) {
									PrjUtils.debug(TAG,"IOException : "+ ioe.getMessage());
								}
							}	
						originalFileList.add(file.getName());
						}
					}
				}
			}
			PrjUtils.debug(TAG, "원본 파일갯수  :" + originalFileList.size());
		}else{
			PrjUtils.debug(TAG, "DCIM폴더가 존재하지 않습니다!!");
		}
	}

	/**
	 * 파일 복구하기
	 */
	private void restorationFile() {
		PrjUtils.debug(TAG, " restorationList.size() :" +  restorationList.size());
		PrjUtils.debug(TAG, " isCheckPosition.size() :" +  isCheckPosition.size());
		for (int i = 0; i < restorationList.size(); i++) { // 복구할 파일갯수만큼 반복
			if(isCheckPosition.contains(i)){
				mCount ++;
				PrjUtils.debug(TAG, "복구할 파일  : "
						+ restorationList.get(i).toString());
				// 복사 대상이 되는 파일 생성
				File sourceFile = new File(STRSAVEPATH
						+ restorationList.get(i).toString());
				// 스트림, 채널 선언
				FileInputStream inputStream = null;
				FileOutputStream outputStream = null;
				FileChannel fcin = null;
				FileChannel fcout = null;
				try {
					// 스트림 생성
					inputStream = new FileInputStream(sourceFile);
					outputStream = new FileOutputStream(STRSAVEPATH2+ restorationList.get(i).toString());

					// 채널 생성
					fcin = inputStream.getChannel();
					fcout = outputStream.getChannel();
					// 채널을 통한 스트림 전송
					long size = fcin.size();
					PrjUtils.debug(TAG, "In File Size :" + size);
					PrjUtils.debug(TAG, "복사본파일경로 :" + sourceFile);
					PrjUtils.debug(TAG, "복사된 경로는 :" + STRSAVEPATH2+ restorationList.get(i).toString());
					File f = new File(STRSAVEPATH2+ restorationList.get(i).toString());
					fcin.transferTo(0, size, fcout); // 파일의 처음 부터 끝까지, fcout 으로 전송
					refreshSD(context, f);
					f = null;
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					// 자원 해제
					if(fcout != null){
						try {
							fcout.close();
						} catch (IOException ioe) {
							PrjUtils.debug(TAG,"IOException : "+ ioe.getMessage());
						}
					}
					if(fcin != null){
						try {
							fcin.close();
						} catch (IOException ioe) {
							PrjUtils.debug(TAG,"IOException : "+ ioe.getMessage());
						}
					}
					if(outputStream != null){
						try {
							outputStream.close();
						} catch (IOException ioe) {
							PrjUtils.debug(TAG,"IOException : "+ ioe.getMessage());
						}
					}
					if(inputStream != null){
						try {
							inputStream.close();
						} catch (IOException ioe) {
							PrjUtils.debug(TAG,"IOException : "+ ioe.getMessage());
						}
					}	
				}
//				isCheckPosition.remove(i);
//				restorationList.remove(i);
//				bitmapList.remove(i);
			}
		}
		PrjUtils.debug(TAG, "복구한 갯수 : " +  mCount);
    	PrjUtils.debug(TAG, "restorationFile()");
	}
	
	/**
	 * 사진 영구삭제(백업파일 제거)
	 * 
	 */
	public void perfectDeleteFile() {
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				for (int i = 0; i < restorationList.size(); i++) {
					if(isCheckPosition.contains(i)){
						mCount ++;
						PrjUtils.debug(TAG, "삭제할 파일  : "+ restorationList.get(i).toString());
						File sourceFile = new File(STRSAVEPATH
								+ restorationList.get(i).toString());
						deleteFile(sourceFile);
//						isCheckPosition.remove(i);
//						restorationList.remove(i);
//						bitmapList.remove(i);
					}
				}
				PrjUtils.debug(TAG, "삭제한 갯수 : " +  mCount);
		    	PrjUtils.debug(TAG,"setChangeData()");
			}
		});

	}
	 
	/**
	 * 디렉토리 생성
	 * 
	 */
	public static File makeDirectory(String dir_path) {
		File dir = new File(dir_path);
		if (!dir.exists()) { // 원하는 경로에 폴더가 있는지 확인
			dir.mkdirs();
			PrjUtils.debug(TAG, "폴더생성");
		}
		return dir;
	}

	/**
	 * 파일 생성
	 * 
	 * @param dir
	 * @return file
	 */
	public static File makeFile(File dir, String file_path) {
		File file = null;
		boolean isSuccess = false;
		if (dir.isDirectory()) {
			file = new File(file_path);
			if (file != null && !file.exists()) {
				Log.i(TAG, "!file.exists");
				try {
					isSuccess = file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					Log.i(TAG, "파일생성 여부 = " + isSuccess);
				}
			} else {
				Log.i(TAG, "file.exists");
			}
		}
		return file;
	}

	/**
	 * (dir/file) 절대 경로 얻어오기
	 * 
	 * @param file
	 * @return String
	 */
	private String getAbsolutePath(File file) {
		return "" + file.getAbsolutePath();
	}

	/**
	 * (dir/file) 삭제 하기
	 * 
	 * @param file
	 */
	private boolean deleteFile(File file) {
		boolean result;
		if (file != null && file.exists()) {
			file.delete();
			result = true;
		} else {
			result = false;
		}
		return result;
	}

	/**
	 * 파일여부 체크 하기
	 * 
	 * @param file
	 * @return
	 */
	private boolean isFile(File file) {
		boolean result;
		if (file != null && file.exists() && file.isFile()) {
			result = true;
		} else {
			result = false;
		}
		return result;
	}

	/**
	 * 디렉토리 여부 체크 하기
	 * 
	 * @param dir
	 * @return
	 */
	private boolean isDirectory(File dir) {
		boolean result;
		if (dir != null && dir.isDirectory()) {
			result = true;
		} else {
			result = false;
		}
		return result;
	}

	/**
	 * 파일 존재 여부 확인 하기
	 * 
	 * @param file
	 * @return
	 */
	private boolean isFileExist(File file) {
		boolean result;
		if (file != null && file.exists()) {
			result = true;
		} else {
			result = false;
		}
		return result;
	}

	/**
	 * 파일 이름 바꾸기
	 * 
	 * @param file
	 */
	private boolean reNameFile(File file, File new_name) {
		boolean result;
		if (file != null && file.exists() && file.renameTo(new_name)) {
			result = true;
		} else {
			result = false;
		}
		return result;
	}

	/**
	 * 디렉토리에 안에 내용을 보여 준다.
	 * 
	 * @param file
	 * @return
	 */
	private String[] getList(File dir) {
		if (dir != null && dir.exists()) {
			Log.d(TAG, "디렉토리 내용  :" + dir.list().length);
			return dir.list();
		}
		return null;
	}

	/**
	 * 파일에 내용 쓰기
	 * 
	 * @param file
	 * @param file_content
	 * @return
	 */
	private boolean writeFile(File file, byte[] file_content) {
		boolean result;
		FileOutputStream fos;
		if (file != null && file.exists() && file_content != null) {
			try {
				fos = new FileOutputStream(file);
				try {
					fos.write(file_content);
					fos.flush();
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			result = true;
		} else {
			result = false;
		}
		return result;
	}

	/**
	 * 파일 읽어 오기
	 * 
	 * @param file
	 */
	private void readFile(File file) {
		int readcount = 0;
		if (file != null && file.exists()) {
			try {
				FileInputStream fis = new FileInputStream(file);
				readcount = (int) file.length();
				byte[] buffer = new byte[readcount];
				fis.read(buffer);
				for (int i = 0; i < file.length(); i++) {
					Log.d(TAG, "" + buffer[i]);
				}
				fis.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 원본파일과 복사된파일 비교하기
	 */
	private void comparisonFile() {
		copyListFiles = (new File(STRSAVEPATH).listFiles());
		for (File file : copyListFiles) {
			if(file.isDirectory()){
				//폴더이면
				if(!file.getName().equals(".thumbnails")){
					File[] list = file.listFiles();
					for (File filelist : list) {
						if (filelist.getName().endsWith(".jpg")|| filelist.getName().endsWith(".png")|| filelist.getName().endsWith(".GIF")) {
							copyFileList.add(file.getName() +"/"+ filelist.getName());
							PrjUtils.debug(TAG, "1111111복사된파일=== " +file.getName() + "/"+ filelist.getName());
						}
					}
				}
			}else{
				if (file.getName().endsWith(".jpg")|| file.getName().endsWith(".png")|| file.getName().endsWith(".GIF")) {
					copyFileList.add(file.getName());
					PrjUtils.debug(TAG, "22222222복사된파일=== " + file.getName());
				}
			}
		}
		PrjUtils.debug(TAG, "원본파일 갯수 : " + originalFileList.size()
				+ "/ 복사된파일 갯수 : " + copyFileList.size());

		for (int i = 0; i < copyFileList.size(); i++) {
			if (originalFileList.contains(copyFileList.get(i).toString())) {
				PrjUtils.debug(TAG, copyFileList.get(i).toString()
						+ "는 삭제된 파일이아닙니다.");
			} else {
				if (copyFileList.get(i).endsWith(".jpg")|| copyFileList.get(i).endsWith(".png")|| copyFileList.get(i).endsWith(".GIF")) {
					PrjUtils.debug(TAG, copyFileList.get(i).toString()
							+ "는 삭제된 파일입니다.");
					restorationList.add(copyFileList.get(i).toString());
				}
			}
		}
	}

	public void restorationFileOK() {
		runOnUiThread(new Runnable() {		
			@Override
			public void run() {
				AlertDialog("알림", mCount + "개의 사진을 복구 하였습니다",
						"확인", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								mCount = 0;
								isCheckPosition.clear();
								dismissDialog();
							}
						});
			}
		});
	}
	
	private void perfectDeleteFileOK() {
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				AlertDialog("알림", mCount + "개의 사진을 완전히 삭제하였습니다",
						"확인", new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								mCount = 0;
								isCheckPosition.clear();
								dismissDialog();
							}
						});
			}
		});
		
	}

	/**
	 * 데이터 갱신
	 */
	private void setChangeData() {

		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				restorationList.clear();
				bitmapList.clear();
				copyFileList.clear();	
				originalFileList.clear();
				copyFileList.clear();
				originalListFiles = (new File(MainActivity.STRSAVEPATH2).listFiles());
				for (File file : originalListFiles) {
					if(file.isDirectory()){
						//폴더이면
						if(!file.getName().equals(".thumbnails")){
							File[] list = file.listFiles();
							for (File filelist : list) {
								if (filelist.getName().endsWith(".jpg")|| filelist.getName().endsWith(".png")|| filelist.getName().endsWith(".GIF")) {
									originalFileList.add(file.getName() +"/"+ filelist.getName());
									PrjUtils.debug(TAG, "원본파일=== " +file.getName() + "/"+ filelist.getName());
								}
							}
						}
					}else{
						if (file.getName().endsWith(".jpg")|| file.getName().endsWith(".png")|| file.getName().endsWith(".GIF")) {
							originalFileList.add(file.getName());
							PrjUtils.debug(TAG, "원본파일=== " + file.getName());
						}
					}
				}
				comparisonFile();
				PrjUtils.debug(TAG, "originalFileList size : " + originalFileList.size());
				isCheck = null;
				isCheck = new boolean[restorationList.size()];	
				for (int i = 0; i < restorationList.size(); i++) {
					try {
						Uri uri = Uri.fromFile(new File(STRSAVEPATH + restorationList.get(i)));
						Bitmap thumbnail = getPreview(getBaseContext(), uri);
						bitmapList.add(thumbnail);
					} catch (OutOfMemoryError e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				allCheck = false;
				adapter.setAllChecked(false);
				adapter.notifyDataSetChanged();
				
//				isCheck = null;
//				isCheck = new boolean[restorationList.size()];	
//				allCheck = false;
//				adapter.setAllChecked(false);
//				adapter.notifyDataSetChanged();
		    	PrjUtils.debug(TAG,"setChangeData()");
			}
		});
	}
	
	public Bitmap getPreview(Context context, Uri uri) {
	    File image = new File(uri.getPath());
	    
	    BitmapFactory.Options bounds = new BitmapFactory.Options();
	    bounds.inJustDecodeBounds = true;
	    BitmapFactory.decodeFile(image.getPath(), bounds);
	    if ((bounds.outWidth == -1) || (bounds.outHeight == -1)){
	        return null;
	    }
	    int originalSize = (bounds.outHeight > bounds.outWidth) ? bounds.outHeight : bounds.outWidth;
	    BitmapFactory.Options opts = new BitmapFactory.Options();
	    opts.inSampleSize = originalSize / 128; // 몇 배로 줄일지
	    return BitmapFactory.decodeFile(image.getPath(), opts);     
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	 /**
     * 스크린에 맞는 OptionMenu를 화면에 표시하기 위해 준비할 때 호출 된다.
     * 옵션메뉴가 화면에 나타날 때마다 호출 된다.
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
		if(allCheck){
			//모두 선택중이면 모두선택해제 텍스트출력
			menu.getItem(2).setTitle("모두선택헤제");
		}else{
			//모두 선택중이아니면 모두선택 텍스트출력
			menu.getItem(2).setTitle("모두선택");
		}
        return super.onPrepareOptionsMenu(menu);
    }
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_restoration:
			isCheckPosition.clear();
			if (restorationList.size() == 0) { // 복구할 파일이 없으면
				AlertDialog("알림", "복구할 사진이 없습니다.", "확인", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1)  {
						dismissDialog();
					}
				});
			}else {
				for (int i = 0; i < isCheck.length; i++) {
					if(isCheck[i]){
						isCheckPosition.add(i);
					}
				}
				if(isCheckPosition.size() == 0){
					AlertDialog("알림", "복구할 사진을 선택하세요.", "확인", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1)  {
							dismissDialog();
						}
					});
				}else{
					AlertDialog("알림", isCheckPosition.size()+"개의 사진을 복구하시겠습니까?", "예", "아니오", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							mode = 0;
							dismissDialog();
							new ProgressDlgSample(MainActivity.this).execute(isCheckPosition.size());
						}
					}, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							dismissDialog();
						}
					});
				}
			}
			break;

		case R.id.action_delete:
			isCheckPosition.clear();
			if (restorationList.size() == 0) { // 삭제할 파일이 없으면
				AlertDialog("알림", "삭제할 사진이 없습니다.", "확인", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1)  {
						dismissDialog();
					}
				});
			}else {
				for (int i = 0; i < isCheck.length; i++) {
					if(isCheck[i]){
						isCheckPosition.add(i);
					}
				}
				if(isCheckPosition.size() == 0){
					AlertDialog("알림", "삭제할 사진을 선택하세요.", "확인", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1)  {
							dismissDialog();
						}
					});
				}else{
					AlertDialog("알림", isCheckPosition.size()+"개의 사진을 완전히 삭제하시겠습니까?\n주의!! 삭제하시면 더이상 복구하실 수 없습니다!!", "예", "아니오", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							mode = 1;
							dismissDialog();
							new ProgressDlgSample(MainActivity.this).execute(isCheckPosition.size());
						}
					}, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							dismissDialog();
						}
					});
				}
			}
			break;

		case R.id.action_allselect:
			if(!allCheck){
				//모두선택중이 아니면 모두 선택버튼의 기능
				if(restorationList.size() != 0){
					adapter.setAllChecked(true);
					allCheck = true;
					adapter.notifyDataSetChanged();
				}
			}else{
				//모두선택중이면 모두선택해제 기능
				if(restorationList.size() != 0){
					adapter.setAllChecked(false);
					allCheck = false;
					adapter.notifyDataSetChanged();
				}
			}
			break;
			
		case R.id.action_information:
			AlertDialog("어플리케이션 정보", "버전 : "+getAppVersion(), "확인", new DialogInterface.OnClickListener() {			
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dismissDialog();
				}
			});
		
			break;

		}

		return true;
	}

	public class ProgressDlgSample extends AsyncTask<Integer, String, Integer> {
		 
	    private ProgressDialog mDlg;
	    private Context mContext;
	 
	    public ProgressDlgSample(Context context) {
	        mContext = context;
	    }
	 
	    @Override
	    protected void onPreExecute() {
	    	if(mDlg != null){
	    		mDlg.dismiss();
	    		mDlg = null;
			}
	    	mDlg = new ProgressDialog(MainActivity.this);
	        mDlg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	        if(mode == 0){
	        	//복구
		        mDlg.setMessage("복구중입니다!!");
	        }else if(mode == 1){
	        	//영구삭제
	        	mDlg.setMessage("삭제중입니다!!");
	        }
	        runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
			        mDlg.show();	
				}
			});
	 
	        super.onPreExecute();
	        
	    }
	 
	    @Override
	    protected Integer doInBackground(Integer... params) {
	 
	        final int taskCnt = params[0];
	        PrjUtils.debug(TAG, "max  : " + Integer.toString(taskCnt));
	        publishProgress("max", Integer.toString(taskCnt));
	        for (int i = 0; i < taskCnt; ++i) {
	            // 작업이 진행되면서 호출하며 화면의 업그레이드를 담당하게 된다
	        	if(mode == 0){
		            publishProgress("progress", Integer.toString(i),"복구중입니다..");
	        	}else if(mode == 1){
	        		publishProgress("progress", Integer.toString(i),"삭제중입니다..");
	        	}
	        }
	        if(mode == 0){
	        	//복구
				restorationFile(); // 파일복구
				setChangeData();
				restorationFileOK();
	        }else if(mode == 1){
	        	//영구삭제
	        	perfectDeleteFile(); 	  // 파일 영구 삭제
				setChangeData();
				perfectDeleteFileOK();
	        }
	        // 수행이 끝나고 리턴하는 값은 다음에 수행될 onProgressUpdate 의 파라미터가 된다
	        return taskCnt;
	    }
	 
		@Override
	    protected void onProgressUpdate(String... progress) {
	        if (progress[0].equals("progress")) {
	            mDlg.setProgress(Integer.parseInt(progress[1]));
	            mDlg.setMessage(progress[2]);
	        } else if (progress[0].equals("max")) {
	            mDlg.setMax(Integer.parseInt(progress[1]));
	        }
	    }
	 
	    @Override
	    protected void onPostExecute(Integer result) {
	    	if(restorationList.size() == 0){
	    		nodataTv.setVisibility(View.VISIBLE);    		
	    	}
			isCheckPosition.clear();
	        mDlg.dismiss();
	    }
	}
	
	class ProgressDialogTask extends AsyncTask<Integer, String, Integer> {
		ProgressDialog pd;

		// onPostExecute() 함수는 doInBackground() 함수가 종료되면 실행됨
		@Override
		protected void onPostExecute(Integer result) {
			pd.dismiss();
			if (restorationList.size() == 0) { // 복구할 파일이 없으면
				AlertDialog("알림", "복구할 사진이 없습니다.", "확인", new DialogInterface. OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						dismissDialog();
					  	if(restorationList.size() == 0){
				    		nodataTv.setVisibility(View.VISIBLE);    		
				    	}
					}
				});
			} else { // 복구할 파일이 있으면
				// 그리드뷰에 출력
				nodataTv.setVisibility(View.GONE);
				adapter.setItems(restorationList);
				mGridView.setAdapter(adapter);
			}
			super.onPostExecute(result);

		}

		// onPreExecute 함수는 이름대로 excute()로 실행 시 doInBackground() 실행 전에 호출되는 함수
		// 여기서 ProgressDialog 생성 및 기본 세팅하고 show()
		@Override
		protected void onPreExecute() {
			if(pd != null){
				pd.dismiss();
				pd = null;
			}
			pd = new ProgressDialog(MainActivity.this);
			pd.setCancelable(false);
			pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			pd.setMessage("복구할 사진을 찾고있습니다");
			pd.show();
			super.onPreExecute();
		}

		// doInBackground 함수는 excute() 실행시 실행됨
		// 여기서 인수로는 작업개수를 넘겨주었다.
		@Override
		protected Integer doInBackground(Integer... params) {
			final int taskCnt = params[0];
			// 넘겨받은 작업개수를 ProgressDialog의 맥스값으로 세팅하기 위해 publishProgress()로 데이터를 넘겨준다.
			Log.d(TAG, "taskCnt==" + taskCnt); 
			adapter = new ImageAdapter(getBaseContext());
			comparisonFile(); // 파일비교
			for (int i = 0; i < restorationList.size(); i++) {
				try {
					Uri uri = Uri.fromFile(new File(STRSAVEPATH + restorationList.get(i)));
					Bitmap thumbnail = getPreview(getBaseContext(), uri);
					bitmapList.add(thumbnail);
				} catch (OutOfMemoryError e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			// 작업이 끝나고 작업된 개수를 리턴 . onPostExecute()함수의 인수가 됨
			return taskCnt;

		}

		// onProgressUpdate() 함수는 publishProgress() 함수로 넘겨준 데이터들을 받아옴
		@Override
		protected void onProgressUpdate(String... progress) {
			super.onProgressUpdate(progress);
			if (progress[0].equals("progress")) {
				pd.setProgress(Integer.parseInt(progress[1]));
				pd.setMessage(progress[2]);
			} else if (progress[0].equals("max")) {
				pd.setMax(Integer.parseInt(progress[1]));
			}
		}

	}

	
	static class ImageViewHolder {
		ImageView ivImage;
		CheckBox chkImage;
	}
	
	@SuppressLint("InlinedApi")
	class ImageAdapter extends BaseAdapter {
		private ArrayList<String> items;
		private Context mContext;
		private LayoutInflater mLiInflater;
		public ImageAdapter(Context context) {
			setmContext(context);
		}

		public void setItems(ArrayList<String> restorationList) {
			items = restorationList;
			if(items.size() != 0){
				isCheck = new boolean[items.size()];	
			}
		}

		// CheckBox를 모두 선택하는 메서드
        public void setAllChecked(boolean ischeked) {
            int tempSize = isCheck.length;
            for(int a=0 ; a<tempSize ; a++){
            	isCheck[a] = ischeked;
            }
        }
        
		@Override
		public int getCount() {
			PrjUtils.debug(TAG, "size===" + items.size());
			return items.size();
		}

		@Override
		public Object getItem(int position) {
			return items.size();
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@SuppressLint("CutPasteId")
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				viewHolder = new ImageViewHolder();
				convertView = mLiInflater.inflate(R.layout.image_cell, parent, false);
				viewHolder.ivImage = (ImageView) convertView.findViewById(R.id.ivImage);
				viewHolder.chkImage = (CheckBox) convertView.findViewById(R.id.chkImage);
				convertView.setTag(viewHolder);

			}else{
				viewHolder = (ImageViewHolder)convertView.getTag();	
			}
			if(items.size() != 0){
				if (viewHolder.chkImage != null) {
					viewHolder.chkImage.setChecked(false);
					final CheckBox cbox = (CheckBox)(convertView.findViewById(R.id.chkImage));
	                cbox.setChecked(isCheck[position]); 
	                if(allCheck){
	                	//전체선택을 했을시
	                    cbox.setButtonDrawable(R.drawable.btn_check_on);
	                }else{
	                    cbox.setButtonDrawable(R.drawable.btn_check_off);
	                }
	                viewHolder.ivImage.setOnClickListener(new OnClickListener() {
	    				
	    				@Override
	    				public void onClick(View v) {
	    			        if (isCheck[position]) {
//	    			        	PrjUtils.debug(TAG, position + "번째 체크해제");
	                            isCheck[position] = false;
	                            cbox.setButtonDrawable(R.drawable.btn_check_off);
	                        } else {
//	                           	PrjUtils.debug(TAG, position + "번째 체크");
	                            isCheck[position] = true;
	                            cbox.setButtonDrawable(R.drawable.btn_check_on);
	                        }
	    				}
	    			});
	                viewHolder.chkImage.setChecked(isCheck[position]);
	                if (isCheck[position]) {
//	                	PrjUtils.debug(TAG, position + "체크중!!");
                        cbox.setButtonDrawable(R.drawable.btn_check_on);
                    } else {
//                    	PrjUtils.debug(TAG, position + "체크해제중!!");
                        cbox.setButtonDrawable(R.drawable.btn_check_off);
                    }
				}
				viewHolder.ivImage.setImageBitmap(bitmapList.get(position));
//				PrjUtils.debug(TAG, "비트맵 크기 : "+bitmapList.get(position).getRowBytes());
//				PrjUtils.debug(TAG, "경로  : "+ Environment.getExternalStorageDirectory().getAbsolutePath());
			}
			return convertView;
		}

		public Context getmContext() {
			return mContext;
		}

		public void setmContext(Context mContext) {
			this.mContext = mContext;
			mLiInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

	}
}
