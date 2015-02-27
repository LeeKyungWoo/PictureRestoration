package com.lkw.picturerestoration;

import java.io.File;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;

import com.lkw.picturerestoration.common.ActivityEx;
import com.lkw.picturerestoration.common.PrjUtils;

public class LoadingActivity extends ActivityEx {
	private static final String TAG = "LoadingActivity";

	/** Called when the activity is first created. */
	@SuppressLint("SdCardPath")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SharedPreferences prefs = getSharedPreferences("UserAgreementFlag", MODE_PRIVATE);
		boolean mFlag  = prefs.getBoolean("flag", false);
		if(!mFlag){
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					// 이용관련된 팝업을 읽지 않았다면 팝업을 띄운다
					AlertDialog("알림", "본 어플리케이션은 기본 내장카메라로 촬영한 사진을 백업하여 복구하는 방식을 사용합니다. 기본카메라 이외의 카메라로 촬영한 사진, 스크린샷, 카카오톡으로 받은 사진등은 복구하지 못합니다. '확인' 버튼을 누르시면 데이터 백업을 진행합니다.", "확인", new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									SharedPreferences prefs = getSharedPreferences("UserAgreementFlag",
											MODE_PRIVATE);
									SharedPreferences.Editor editor = prefs.edit();
									editor.putBoolean("flag", true);
									editor.commit();
									dismissDialog();
									setContentView(R.layout.activity_loading);
									new LoadingTask().execute(100);
									
								}
							});

				}	
			});

		}else{
			new LoadingTask().execute(100);
		}

	}

	@Override
	public void onBackPressed() {

	}

	class LoadingTask extends AsyncTask<Integer, String, Integer> {
		ProgressDialog pd;
		// onPostExecute() 함수는 doInBackground() 함수가 종료되면 실행됨
		@Override
		protected void onPostExecute(Integer result) {
			pd.dismiss();
			Intent it = new Intent(LoadingActivity.this,
					MainActivity.class);
			callActivity(it);
//			Handler hd = new Handler();
//			hd.postDelayed(new Runnable() {
//
//				@Override
//				public void run() {
//					Intent it = new Intent(LoadingActivity.this,
//							MainActivity.class);
//					callActivity(it);
//				}
//			}, 3000);
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
			pd = new ProgressDialog(LoadingActivity.this);
			pd.setCancelable(false);
			pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			pd.setMessage("사진 탐색중!! 잠시만기다려주세요..");
			pd.show();
			super.onPreExecute();
		}

		// doInBackground 함수는 excute() 실행시 실행됨
		// 여기서 인수로는 작업개수를 넘겨주었다.
		@Override
		protected Integer doInBackground(Integer... params) {
			// final int taskCnt = params[0];
			// 넘겨받은 작업개수를 ProgressDialog의 맥스값으로 세팅하기 위해 publishProgress()로 데이터를
			// 넘겨준다.
			// publishProgress()로 넘기면 onProgressUpdate()함수가 실행된다.
			// publishProgress("max", Integer.toString(taskCnt));
			// 폴더 생성
			File dir = MainActivity.makeDirectory(MainActivity.STRSAVEPATH);

			// 갤러리에서 폴더를 읽지못하게 하기 위해 .nomedia파일생성
			MainActivity.makeFile(dir, MainActivity.STRSAVEPATH + ".nomedia");
			MainActivity.originalFileList.clear();
			MainActivity.restorationList.clear();
			MainActivity.copyFileList.clear();
			File isDir = new File(MainActivity.STRSAVEPATH+"Camera");
			if (!isDir.exists()) {
				// 백업된 파일이 없으면 백업
				MainActivity.copyFile(MainActivity.STRSAVEPATH2,
						MainActivity.STRSAVEPATH);
			}else{
				MainActivity.originalListFiles = (new File(MainActivity.STRSAVEPATH2).listFiles());
				for (File file : MainActivity.originalListFiles) {
					if(file.isDirectory()){
						//폴더이면
						if(!file.getName().equals(".thumbnails")){
							File[] list = file.listFiles();
							for (File filelist : list) {
								if (filelist.getName().endsWith(".jpg")|| filelist.getName().endsWith(".png")|| filelist.getName().endsWith(".GIF")) {
									MainActivity.originalFileList.add(file.getName() +"/"+ filelist.getName());
									PrjUtils.debug(TAG, "원본파일=== " +file.getName() + "/"+ filelist.getName());
								}
							}
						}
					}else{
						if (file.getName().endsWith(".jpg")|| file.getName().endsWith(".png")|| file.getName().endsWith(".GIF")) {
							MainActivity.originalFileList.add(file.getName());
							PrjUtils.debug(TAG, "원본파일=== " + file.getName());
						}
					}
				}
				PrjUtils.debug(TAG, "originalFileList size : " + MainActivity.originalFileList.size());
			}

			// 작업이 끝나고 작업된 개수를 리턴 . onPostExecute()함수의 인수가 됨
			return MainActivity.originalFileList.size();

		}

	}
}
