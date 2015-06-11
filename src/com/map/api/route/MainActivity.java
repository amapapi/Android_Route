package com.map.api.route;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.services.route.RouteSearch;
import com.map.api.route.RouteTask.OnDriveSearchListener;
import com.map.api.route.RouteTask.ResultEntity;

public class MainActivity extends Activity implements OnClickListener {

	// 上边行车、公交、步行的是三个按钮
	private ImageView mCarButton;
	private ImageView mBusButton;
	private ImageView mFootButton;

	private CarResultView mDistanceResultView;
	private CarResultView mNormalResultView;
	private CarResultView mJamResultView;

	private TextView mRouteDesText;

	private TextView mNaviButton;

	private MapView mMapView;
	private AMap mAmap;

	private TextView mPrefenceButton;
	private static LatLng mFromPoint = new LatLng(39.989614, 116.481763);
	private static LatLng mEndPoint = new LatLng(38.983456, 115.3154950);// new
																			// LatLng(39.983456,
																			// 116.3154950);

	private static final int SINGLE_RESULT = 1;
	private static final int ALL_RESULT = 2;

	private ProgressDialog mDialog;

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SINGLE_RESULT:
				ResultEntity resultEntity = (ResultEntity) msg.obj;
				switch (resultEntity.mDriveMode) {
				case RouteSearch.DrivingAvoidCongestion:
					if (resultEntity.errorCode != 0) {
						mJamResultView.setVisibility(View.GONE);
						return;
					}
					mJamResultView.setVisibility(View.VISIBLE);
					mJamResultView.setModeName("避免拥堵");
					mJamResultView.setTime(resultEntity.mTime);
					mJamResultView.setDistance(resultEntity.mDistance);
					mJamResultView.setResultEntity(resultEntity);

					break;
				case RouteSearch.DrivingDefault:
					if (resultEntity.errorCode != 0) {
						mNormalResultView.setVisibility(View.GONE);
						return;
					}
					mNormalResultView.setVisibility(View.VISIBLE);
					mNormalResultView.setModeName("常规");
					mNormalResultView.setTime(resultEntity.mTime);
					mNormalResultView.setDistance(resultEntity.mDistance);
					mNormalResultView.setResultEntity(resultEntity);
					break;
				case RouteSearch.DrivingShortDistance:
					if (resultEntity.errorCode != 0) {
						mDistanceResultView.setVisibility(View.GONE);
						return;
					}
					mDistanceResultView.setVisibility(View.VISIBLE);
					mDistanceResultView.setModeName("距离短");
					mDistanceResultView.setTime(resultEntity.mTime);
					mDistanceResultView.setDistance(resultEntity.mDistance);
					mDistanceResultView.setResultEntity(resultEntity);
					break;

				}
				break;
			case ALL_RESULT:
				mJamResultView.setSelected(true);
				mNormalResultView.setSelected(false);
				mDistanceResultView.setSelected(false);
				mNaviButton.setVisibility(View.VISIBLE);
				mDialog.dismiss();
				break;

			}

		}

	};

	private OnDriveSearchListener mOnDriveSearchListener = new OnDriveSearchListener() {

		@Override
		public void onSuccess(ResultEntity[] resultEntities) {
			for (ResultEntity resultEntity : resultEntities) {
				if (resultEntity != null) {

					Message msg = mHandler.obtainMessage();
					msg.what = SINGLE_RESULT;
					msg.obj = resultEntity;
					mHandler.sendMessage(msg);
					if (resultEntity.errorCode == 0) {
						Polyline polyline = mAmap
								.addPolyline(resultEntity.mRouteOptions);
						 
						resultEntity.mNormalenPolyline = polyline;
						Polyline selectedPolyline = mAmap
								.addPolyline(resultEntity.mSelectedOptions);
						resultEntity.mSelecedPolyline = selectedPolyline;
					}
				}

			}

			Message msg = mHandler.obtainMessage();
			msg.what = ALL_RESULT;
			mHandler.sendMessage(msg);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mCarButton = (ImageView) findViewById(R.id.car_image);
		mBusButton = (ImageView) findViewById(R.id.bus_image);
		mFootButton = (ImageView) findViewById(R.id.foot_image);
		mPrefenceButton = (TextView) findViewById(R.id.prefence_button);
		mDistanceResultView = (CarResultView) findViewById(R.id.distance_view);
		mJamResultView = (CarResultView) findViewById(R.id.jam_view);
		mNormalResultView = (CarResultView) findViewById(R.id.normal_view);
		mRouteDesText = (TextView) findViewById(R.id.road_des);
		mNaviButton = (TextView) findViewById(R.id.navi_button);
		mMapView = (MapView) findViewById(R.id.map);
		mMapView.onCreate(savedInstanceState);
		mAmap = mMapView.getMap();

		mAmap.addMarker(new MarkerOptions()
				.icon(

				BitmapDescriptorFactory.fromBitmap(BitmapFactory
						.decodeResource(getResources(), R.drawable.route_start))

				).anchor(0.5f, 0.5f).position(mFromPoint));

		mAmap.addMarker(new MarkerOptions()
				.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
						.decodeResource(getResources(), R.drawable.route_end)))
				.anchor(0.5f, 0.5f).position(mEndPoint));
		mDistanceResultView.setAMap(mAmap);
		mJamResultView.setAMap(mAmap);
		mNormalResultView.setAMap(mAmap);
		mDistanceResultView.setDesTextView(mRouteDesText);
		mJamResultView.setDesTextView(mRouteDesText);
		mNormalResultView.setDesTextView(mRouteDesText);
		mDialog = new ProgressDialog(this);
		mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mDialog.setIndeterminate(false);
		// mDialog.setCancelable(true);
		mDialog.setMessage("线路规划中");
		mDialog.show();

		mCarButton.setOnClickListener(this);
		mBusButton.setOnClickListener(this);
		mFootButton.setOnClickListener(this);
		mPrefenceButton.setOnClickListener(this);
		mDistanceResultView.setOnClickListener(this);
		mJamResultView.setOnClickListener(this);
		mNormalResultView.setOnClickListener(this);
		RouteTask routeTask = RouteTask.getInstance();
		routeTask.setDriveSearchListener(mOnDriveSearchListener);
		routeTask.search(getApplicationContext(), mFromPoint, mEndPoint);

	}

	protected void onResume() {
		super.onResume();
		mMapView.onResume();
	}

	protected void onPause() {
		super.onPause();
		mMapView.onPause();
	}

	protected void onDestroy() {
		super.onDestroy();
		mMapView.onDestroy();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.car_image:
			break;
		case R.id.foot_image:
			break;
		case R.id.bus_image:
			break;
		case R.id.prefence_button:
			break;
		case R.id.distance_view:
			mDistanceResultView.setSelected(true);
			mJamResultView.setSelected(false);
			mNormalResultView.setSelected(false);
			break;
		case R.id.normal_view:
			mDistanceResultView.setSelected(false);
			mJamResultView.setSelected(false);
			mNormalResultView.setSelected(true);
			break;
		case R.id.jam_view:
			mDistanceResultView.setSelected(false);
			mJamResultView.setSelected(true);
			mNormalResultView.setSelected(false);
			break;
		}

	}
}
