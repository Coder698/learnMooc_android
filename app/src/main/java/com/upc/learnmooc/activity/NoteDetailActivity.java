package com.upc.learnmooc.activity;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.upc.learnmooc.R;
import com.upc.learnmooc.domain.NoteDetail;
import com.upc.learnmooc.global.GlobalConstants;
import com.upc.learnmooc.utils.ToastUtils;
import com.upc.learnmooc.utils.UserInfoCacheUtils;

import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * 笔记详情
 * Created by Explorer on 2016/4/9.
 */
public class NoteDetailActivity extends BaseActivity {

	private ListView mListView;
	private String mUrl;
	private String courseName;
	private ArrayList<NoteDetail.NoteContent> noteContent;
	private SweetAlertDialog pDialog;
	private ListViewAdapter mAdapter;
	private String url;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.note_detail_activity);
		initViews();
		initData();
	}


	@Override
	public void initViews() {
		mListView = (ListView) findViewById(R.id.lv_note_detail);

	}

	private void initData() {
		mUrl = GlobalConstants.GET_NOTE_DETAIL;
		url = GlobalConstants.REMOVE_NOTE;
		courseName = getIntent().getStringExtra("courseName");
		getDataFromServer();
	}

	private void getDataFromServer() {
		HttpUtils httpUtils = new HttpUtils();
		httpUtils.configCurrentHttpCacheExpiry(5 * 1000);
		httpUtils.configTimeout(1000 * 5);
		//GET参数为用户id 和 courseId
		RequestParams params = new RequestParams();
		params.addQueryStringParameter("userId", UserInfoCacheUtils.getLong(NoteDetailActivity.this, "id", 0) + "");
		params.addQueryStringParameter("courseName", courseName);

		httpUtils.send(HttpRequest.HttpMethod.GET, mUrl, params, new RequestCallBack<String>() {
			@Override
			public void onSuccess(ResponseInfo<String> responseInfo) {
				parseData(responseInfo.result);
			}

			@Override
			public void onFailure(HttpException e, String s) {
				e.printStackTrace();
			}
		});
	}

	private void parseData(String result) {
		Gson gson = new Gson();
		NoteDetail noteDetail = gson.fromJson(result, NoteDetail.class);
		noteContent = noteDetail.noteDetail;
		if (noteContent != null) {
			mAdapter = new ListViewAdapter();
			mListView.setAdapter(mAdapter);
		}

	}


	class ListViewAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return noteContent.size();
		}

		@Override
		public Object getItem(int position) {
			return noteContent.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = View.inflate(NoteDetailActivity.this, R.layout.item_note_detail_listview, null);
				holder = new ViewHolder();
				holder.tvTime = (TextView) convertView.findViewById(R.id.tv_note_time);
				holder.tvContent = (TextView) convertView.findViewById(R.id.tv_note_content);
				holder.ivDel = (ImageView) convertView.findViewById(R.id.iv_del);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			final NoteDetail.NoteContent noteContentData = noteContent.get(position);
			holder.tvTime.setText(noteContentData.getTime().substring(0,16));
			holder.tvContent.setText(noteContentData.getContent());
			holder.ivDel.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showDeleteDialog(position);
				}
			});

			return convertView;
		}
	}

	/**
	 * 显示删除提示的弹窗
	 */
	protected void showDeleteDialog(final int position) {
		pDialog = new SweetAlertDialog(this)
				.setTitleText("真的要删掉我吗?")
				.setContentText("~T_T~")
				.setConfirmText("真的")
				.setCancelText("逗你的");
		//取消
		pDialog.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
			@Override
			public void onClick(SweetAlertDialog sweetAlertDialog) {
				pDialog.dismiss();
			}
		});

		//删除
		pDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
			@Override
			public void onClick(SweetAlertDialog sweetAlertDialog) {
				pDialog.dismiss();
				// 删除对应的note记录
				HttpUtils httpUtils = new HttpUtils();
				httpUtils.configCurrentHttpCacheExpiry(5 * 1000);
				httpUtils.configTimeout(1000 * 5);
				// noteId
				RequestParams params = new RequestParams();
				params.addQueryStringParameter("noteId", noteContent.get(position).getNoteId() + "");

				httpUtils.send(HttpRequest.HttpMethod.GET, url, params, new RequestCallBack<String>() {
					@Override
					public void onSuccess(ResponseInfo<String> responseInfo) {
						if (responseInfo.result.equals("success")) {
							noteContent.remove(position);
							mAdapter.notifyDataSetChanged();
						}

						ToastUtils.showToastShort(NoteDetailActivity.this, responseInfo.result);
					}

					@Override
					public void onFailure(HttpException e, String s) {
						e.printStackTrace();
						ToastUtils.showToastShort(NoteDetailActivity.this, "删除异常 请重试");
					}
				});

			}
		});
		pDialog.show();

	}

	static class ViewHolder {
		public TextView tvTime;
		public TextView tvContent;
		public ImageView ivDel;
	}

}
