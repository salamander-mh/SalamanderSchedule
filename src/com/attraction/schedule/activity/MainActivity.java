package com.attraction.schedule.activity;

import java.sql.SQLException;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import com.attraction.schedule.R;
import com.attraction.schedule.db.Lesson;
import com.attraction.schedule.db.LessonDao;
import com.attraction.schedule.tool.ParseUtil;
import com.attraction.schedule.view.OnComponentAddedCompletedListener;
import com.attraction.schedule.view.Timetable;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;

public class MainActivity extends Activity implements OnComponentAddedCompletedListener {
	@Bind(R.id.tv_main_settings)
	TextView tvSettings;
	@Bind(R.id.timetable_main)
	Timetable timetable;
	private static int FETCH = 0;
	LessonDao lessonDao = null;
	List<Lesson> lessons = null;
	private static final int GET_LESSONS = 2;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ButterKnife.bind(this);
	}
	
	@Override
	public void complete() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					lessonDao = new LessonDao(MainActivity.this);
					lessons = lessonDao.queryForAll();
					handler.sendEmptyMessage(GET_LESSONS);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
	
	@SuppressLint("HandlerLeak")
	Handler handler = new Handler() {
		 public void handleMessage(Message msg){
			 switch (msg.what) {
			case GET_LESSONS:
				timetable.addLessons(lessons);
				break;
			default:
				break;
			}
	     } 
	};
	
	@OnClick(R.id.tv_main_settings)
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_main_settings:
			Intent intent = new Intent(this, ImportActivity.class);
			startActivityForResult(intent, FETCH);
			break;
		default:
			break;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO 自动生成的方法存根
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == FETCH) {
			if(resultCode == ImportActivity.FETCH_SUCCESS) {
				String html = data.getStringExtra("html");
				ParseUtil util = new ParseUtil();
				lessons = util.parseLesson(html);
				timetable.addLessons(lessons);
				try {
					lessonDao.addLessons(lessons);
				} catch (SQLException e) {
					// TODO 自动生成的 catch 块
					e.printStackTrace();
				}
			}
		}
	}
}
