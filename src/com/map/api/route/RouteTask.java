package com.map.api.route;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
 
import android.util.Log;

import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
 
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.DriveStep;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.RouteSearch.DriveRouteQuery;
import com.amap.api.services.route.RouteSearch.FromAndTo;
 

public class RouteTask {

	// 常规模式，速度优先
	private static final int DEFAULT_MODE = 1;
	// 距离最短模式
	private static final int DISTANCE_MODE = 2;
	// 躲避拥堵模式
	private static final int JAM_Mode = 3;

	private static final int ON_SUCCESS = 14;

	private int mSearchMode = 0;

	private LatLng mFromPoint;
	private LatLng mToPoint;

	private static RouteTask mRouteTask;

 

	private ResultEntity[] mResultEntities = new ResultEntity[3];

	private OnDriveSearchListener mOnDriveSearchListener;
	
	private static ExecutorService threads=Executors.newFixedThreadPool(3);

	static final int ExceptionCode=1;
	class ResultEntity {
		int mDriveMode;
		float mTaxiCost;
		float mDistance;
		int mTime;
		float mTollCost;
		String mRouteDes;
		PolylineOptions mRouteOptions;
		PolylineOptions mSelectedOptions;
		Polyline mNormalenPolyline;
		Polyline mSelecedPolyline;
		LatLngBounds mRouteBounds;
		int errorCode=0;

	}

	public interface OnDriveSearchListener {

		public void onSuccess(ResultEntity[] resultEntities);

	}

	public void setDriveSearchListener(
			OnDriveSearchListener onDriveSearchListener) {
		mOnDriveSearchListener = onDriveSearchListener;
	}

	private OnDriveRouteSearchListener mOnDriveRouteSearchListener = new OnDriveRouteSearchListener() {

		@Override
		public void onRouteSearcheSuccess(DriveRouteResult driveResult,
				int driveMode, int mode) {
			
			ResultEntity resultEntity = new ResultEntity();
			mResultEntities[mode - 1] = resultEntity;
			resultEntity.mTaxiCost = driveResult.getTaxiCost();
			resultEntity.mDriveMode = driveMode;
			List<DrivePath> drivePaths = driveResult.getPaths();
			PolylineOptions normalRouteOptions = new PolylineOptions();
			PolylineOptions selectRouteOptions=new PolylineOptions();
			
			LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
			List<LatLng> points=new ArrayList<LatLng>();
			//解析路径构造路线的点集
			points.add(mFromPoint);
			boundsBuilder.include(mFromPoint);
			 
			if (drivePaths.size() > 0) {
				DrivePath drivePath = drivePaths.get(0);
				 
				resultEntity.mTollCost=drivePath.getTolls();
				resultEntity.mDistance =  drivePath.getDistance() / 1000;
				resultEntity.mTime = (int) (drivePath.getDuration() / 60);
				List<DriveStep> steps = drivePath.getSteps();
		 
				if (steps != null) {
					for (DriveStep driveStep : steps) {
						driveStep.getDistance();
						List<LatLonPoint> stepPoints = driveStep.getPolyline();
						for (LatLonPoint latlonPoint : stepPoints) {
							LatLng latlng = new LatLng(
									latlonPoint.getLatitude(),
									latlonPoint.getLongitude());
							points.add(latlng);
							boundsBuilder.include(latlng);
						}
 
					}
				}

			}
			points.add(mToPoint);
			boundsBuilder.include(mToPoint);
			
			normalRouteOptions.addAll(points);
			selectRouteOptions.addAll(points);
		
			normalRouteOptions.zIndex(0.5f)
					.width(35)
					.setDottedLine(false)
					.setUseTexture(true)
					.setCustomTexture(
							BitmapDescriptorFactory
									.fromResource(R.drawable.road_normal));
		
			
			selectRouteOptions.zIndex(0.1f)	.width(35)
			.setDottedLine(false)
			.setUseTexture(true) 
			.setCustomTexture(
					BitmapDescriptorFactory
							.fromResource(R.drawable.road_select));
			resultEntity.mRouteOptions = normalRouteOptions;
			resultEntity.mSelectedOptions=selectRouteOptions;
			resultEntity.mRouteBounds=boundsBuilder.build();
			synchronized (this) {
				mSearchMode = mSearchMode | (1 << mode);			
				if (mSearchMode == ON_SUCCESS) {
					mSearchMode = 0;
					if (mOnDriveSearchListener != null) {
						mOnDriveSearchListener.onSuccess(mResultEntities);
					}
				}
				} 
			
		}

		@Override
		public void onRouteSearchFailure(int driveMode, int mode) {
			ResultEntity resultEntity = new ResultEntity();
			mResultEntities[mode - 1] = resultEntity;
			resultEntity.mDriveMode = driveMode;
			resultEntity.errorCode=ExceptionCode;
			synchronized (this) {
				mSearchMode = mSearchMode | (1 << mode);			
				if (mSearchMode == ON_SUCCESS) {
					mSearchMode = 0;
					if (mOnDriveSearchListener != null) {
						mOnDriveSearchListener.onSuccess(mResultEntities);
					}
				}
				} 
		}
	};

	public static RouteTask getInstance() {
		if (mRouteTask == null) {
			mRouteTask = new RouteTask();
		}
		return mRouteTask;
	}

	public void setFromPoint(LatLng fromPoint) {
		mFromPoint = fromPoint;
	}

	public void setEndPoint(LatLng toPoint) {
		mToPoint = toPoint;
	}

	public void search(Context context) {
		RouteModeTask.searchRoute(context, RouteSearch.DrivingDefault,
				mFromPoint, mToPoint, mOnDriveRouteSearchListener);
		RouteModeTask.searchRoute(context, RouteSearch.DrivingAvoidCongestion,
				mFromPoint, mToPoint, mOnDriveRouteSearchListener);
		RouteModeTask.searchRoute(context, RouteSearch.DrivingShortDistance,
				mFromPoint, mToPoint, mOnDriveRouteSearchListener);
	}

	public void search(Context context, LatLng fromPoint, LatLng toPoint) {
		mFromPoint = fromPoint;
		mToPoint = toPoint;
		search(context);

	}

	private interface OnDriveRouteSearchListener {

		public void onRouteSearcheSuccess(DriveRouteResult driveResult,
				int driveMode, int mode);

		public void onRouteSearchFailure(int driveMode, int mode);
	}

	private static class RouteModeTask {
		// 常规模式，速度优先
		private static RouteSearch mDefaultSearch;
		// 距离最短模式
		private static RouteSearch mDistanceSearch;
		// 躲避拥堵模式
		private static RouteSearch mJamSearch;
 

		public static void searchRoute(Context context, final int driveMode,
				final LatLng fromPoint,final LatLng endPoint,
				 final OnDriveRouteSearchListener listener) {
			RouteSearch routeSearch = null;
		 int flag =0;

			switch (driveMode) {
			case RouteSearch.DrivingDefault:
				if (mDefaultSearch == null) {
					mDefaultSearch = new RouteSearch(context);
					flag =DEFAULT_MODE;
 
				}
				routeSearch = mDefaultSearch;
				 
				break;
			case RouteSearch.DrivingAvoidCongestion:
				if (mJamSearch == null) {
					mJamSearch = new RouteSearch(context);
					flag =JAM_Mode;
 

				}
				routeSearch = mJamSearch;
				 
				break;

			case RouteSearch.DrivingShortDistance:
				if (mDistanceSearch == null) {
					mDistanceSearch = new RouteSearch(context);
					flag =DISTANCE_MODE;
 
 			}
				routeSearch = mDistanceSearch;
				 
				break;
			}
			
			final RouteSearch search=routeSearch;
			final int flagMode=flag;
 
			threads.submit(new Runnable() {
				
				@Override
				public void run() {
					
					if (search != null  ) {
						FromAndTo fromAndTo = new FromAndTo(new LatLonPoint(
								fromPoint.latitude, fromPoint.longitude),
								new LatLonPoint(endPoint.latitude, endPoint.longitude));

						DriveRouteQuery driveRouteQuery = new DriveRouteQuery(
								fromAndTo, driveMode, null, null, "");
						try {
						DriveRouteResult driveResult=	search.calculateDriveRoute(driveRouteQuery);
						listener.onRouteSearcheSuccess(driveResult, driveMode, flagMode);
						} catch (AMapException e) {

							e.printStackTrace();  
							listener.onRouteSearchFailure(driveMode, flagMode);
						}
					}
					
				}
			});
			
			
			
		}

 
	}

}
