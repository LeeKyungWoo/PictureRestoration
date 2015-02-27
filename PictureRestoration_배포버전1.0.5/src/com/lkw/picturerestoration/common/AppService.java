package com.lkw.picturerestoration.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.Looper;

import com.lkw.picturerestoration.MainActivity;

@SuppressLint("DefaultLocale")
public class AppService extends Service implements Runnable{
	private static final String TAG = "AppService";
	private Thread thread = null;						//파일 감지 스레드
	private boolean bFlag = true;
	private ArrayList<String> originalList = new ArrayList<String>();
	private ArrayList<String> copyList = new ArrayList<String>();
	private ArrayList<String> newList = new ArrayList<String>();
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		PrjUtils.debug(TAG, "onCreate");
	}

	@Override
	public void onDestroy() {
		if(thread != null){
			bFlag = false;
			thread.interrupt();
			thread = null;
		}
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		PrjUtils.debug(TAG, "onStartCommand");
		if(thread == null){
			thread = new Thread(this);
			thread.start();
		}
		return START_REDELIVER_INTENT;
	}

	//파일 감지 thread
	@Override
	public void run() {
		Looper.prepare();
		while(bFlag) {
			try {		
				Thread.sleep(1000 * 60);
				ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
				List<RunningTaskInfo> Info = am.getRunningTasks(1);
				ComponentName topActivity = Info.get(0).topActivity;
				String topactivityname = topActivity.getPackageName();
				PrjUtils.debug(TAG, "최상위 패키지 : "+topactivityname);
				if(!topactivityname.equals("com.lkw.picturerestoration")){
					comparisonFile();
					copyList.clear();
					originalList.clear();
					MainActivity.copyFileList.clear();
					MainActivity.originalFileList.clear();		
				}
			} catch (InterruptedException e) {
				PrjUtils.debug(TAG, "InterruptedException");
				e.printStackTrace();
				bFlag = false;
				stopSelf();
			}
		}
		Looper.loop();	
	}
	
	/**
	 * 원본파일과 복사된파일 비교하기
	 */
	private void comparisonFile() {
		MainActivity.copyListFiles = (new File(MainActivity.STRSAVEPATH).listFiles());
		for (File file : MainActivity.copyListFiles) {
			if(file.isDirectory()){
				//폴더이면
				if(!file.getName().equals(".thumbnails")){
					File[] list = file.listFiles();
					for (File filelist : list) {
						if (filelist.getName().endsWith(".jpg")|| filelist.getName().endsWith(".png")|| filelist.getName().endsWith(".GIF")) {
							//MainActivity.copyFileList.add(file.getName() +"/"+ filelist.getName());
							copyList.add(file.getName() +"/"+ filelist.getName());
							PrjUtils.debug(TAG, "복사된파일=== " +file.getName() + "/"+ filelist.getName());
						}
					}
				}
			}else{
				if (file.getName().endsWith(".jpg")|| file.getName().endsWith(".png")|| file.getName().endsWith(".GIF")) {
					//MainActivity.copyFileList.add(file.getName());
					copyList.add(file.getName());
					PrjUtils.debug(TAG, "복사된파일=== " + file.getName());
				}
			}
		}
		MainActivity.originalListFiles = (new File(MainActivity.STRSAVEPATH2).listFiles());
		for (File file : MainActivity.originalListFiles) {
			if(file.isDirectory()){
				//폴더이면
				if(!file.getName().equals(".thumbnails")){
					File[] list = file.listFiles();
					for (File filelist : list) {
						if (filelist.getName().endsWith(".jpg")|| filelist.getName().endsWith(".png")|| filelist.getName().endsWith(".GIF")) {
							//MainActivity.originalFileList.add(file.getName() +"/"+ filelist.getName());
							originalList.add(file.getName() +"/"+ filelist.getName());
							PrjUtils.debug(TAG, "원본파일=== " +file.getName() + "/"+ filelist.getName());
						}
					}
				}
			}else{
				if (file.getName().endsWith(".jpg")|| file.getName().endsWith(".png")|| file.getName().endsWith(".GIF")) {
					//MainActivity.originalFileList.add(file.getName());
					originalList.add(file.getName());
					PrjUtils.debug(TAG, "원본파일=== " + file.getName());
				}
			}
		}
		for (int i = 0; i < originalList.size(); i++) {
			if (!copyList.contains(originalList.get(i).toString())) {
				if (originalList.get(i).endsWith(".jpg")|| originalList.get(i).endsWith(".png")|| originalList.get(i).endsWith(".GIF")) {
					PrjUtils.debug(TAG, originalList.get(i).toString()
							+ "는 새로 추가된 파일입니다.");
					newList.add(originalList.get(i).toString());
				}
			}
		}
		if(newList.size() != 0){
			//새로 촬영된 파일이 있으면 백업한다
			newFileAdd();
			newList.clear();
		}
		PrjUtils.debug(TAG, "원본파일 갯수 : " + originalList.size() + "/ 복사된파일 갯수 : " + copyList.size());
	}
	
	/**
	 * 새로 촬영된 파일 추가 백업
	 * 
	 * @param target
	 * 
	 */
	public void newFileAdd() {
		for (int i = 0; i < newList.size(); i++) { // 새로운파일갯수 만큼 반복
			PrjUtils.debug(TAG, "새로 추가된 파일  : "+ newList.get(i).toString());
			// 복사 대상이 되는 파일 생성
			File sourceFile = new File(MainActivity.STRSAVEPATH2 + newList.get(i).toString());
			// 스트림, 채널 선언
			FileInputStream inputStream = null;
			FileOutputStream outputStream = null;
			FileChannel fcin = null;
			FileChannel fcout = null;
			try {
				// 스트림 생성
				inputStream = new FileInputStream(sourceFile);
				outputStream = new FileOutputStream(MainActivity.STRSAVEPATH + newList.get(i).toString());

				// 채널 생성
				fcin = inputStream.getChannel();
				fcout = outputStream.getChannel();
				// 채널을 통한 스트림 전송
				long size = fcin.size();
				PrjUtils.debug(TAG, "In File Size :" + size);
				PrjUtils.debug(TAG, "복사본파일경로 :" + sourceFile);
				fcin.transferTo(0, size, fcout); // 파일의 처음 부터 끝까지, fcout 으로 전송

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				// 자원 해제
				try {
					if(fcout != null){
						fcout.close();			
					}
				} catch (IOException ioe) {
				}
				try {
					if(fcin != null){
						fcin.close();
					}
				} catch (IOException ioe) {
				}
				try {
					if(outputStream != null){
						outputStream.close();
					}
				} catch (IOException ioe) {
				}
				try {
					if(inputStream != null){
						inputStream.close();
					}
				} catch (IOException ioe) {
				}
			}
		}
	}

}
