package com.attraction.schedule.fragment;

import java.sql.SQLException;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import com.attraction.schedule.R;
import com.attraction.schedule.activity.ImportActivity;
import com.attraction.schedule.db.Lesson;
import com.attraction.schedule.db.LessonDao;
import com.attraction.schedule.tool.ParseUtil;
import com.attraction.schedule.view.OnComponentAddedCompletedListener;
import com.attraction.schedule.view.Timetable;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class ScheduleFragment extends BaseFragment implements OnComponentAddedCompletedListener {
	@Bind(R.id.tv_main_settings)
	TextView tvSettings;
	private Timetable timetable;
	private static int FETCH = 3;
	LessonDao lessonDao = null;
	List<Lesson> lessons = null;
	private static final int GET_LESSONS = 2;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if(rootView == null) {
			rootView = (ViewGroup)inflater.inflate(R.layout.fragment_schedule, container, false);
			ButterKnife.bind(this, rootView);
			LinearLayout.LayoutParams params = 
					new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			timetable = new Timetable(getActivity(), this);
			timetable.setLayoutParams(params);
			rootView.addView(timetable);
		} else {
			this.removeParent();
		}
		return rootView;
	}

	
	@Override
	public void complete() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					lessonDao = new LessonDao(getActivity());
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
			Intent intent = new Intent(getActivity(), ImportActivity.class);
			startActivityForResult(intent, FETCH);
			break;
		default:
			break;
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
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
