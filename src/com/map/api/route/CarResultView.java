/**  
 * Project Name:Android_Route  
 * File Name:CarResultView.java  
 * Package Name:com.map.api.route  
 * Date:2015年6月9日下午2:38:38  
 *  
*/  
  
package com.map.api.route;  

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.map.api.route.RouteTask.ResultEntity;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/**  
 * ClassName:CarResultView <br/>  
 * Function: TODO ADD FUNCTION. <br/>  
 * Reason:   TODO ADD REASON. <br/>  
 * Date:     2015年6月9日 下午2:38:38 <br/>  
 * @author   yiyi.qi  
 * @version    
 * @since    JDK 1.6  
 * @see        
 */
public class CarResultView extends LinearLayout{
	
	
    private TextView mModeNameText;
    
    private TextView mTimeText;
    
    private TextView mDistanceText;
    
    private TextView mDesText;
    
    private ResultEntity mResultEntity;
    
    private AMap mAmap;
    
	public CarResultView(Context context) {
		  
		super(context);  
		initView(context);
		 
	}
	public CarResultView(Context context, AttributeSet attrs) {
		  
		super(context, attrs);  
		initView(context);
	}

	public CarResultView(Context context, AttributeSet attrs, int defStyle) {
		  
		super(context, attrs, defStyle);  
		initView(context);
	}
	
	public void setAMap(AMap amap){
		mAmap=amap;
		
		
	}
	private void initView(Context context){
		LayoutInflater inflater=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view=inflater.inflate(R.layout.car_result_view, null);
		mDistanceText=(TextView) view.findViewById(R.id.distance_txt);
		mTimeText=(TextView) view.findViewById(R.id.time_txt);
		mModeNameText=(TextView) view.findViewById(R.id.mode_name_txt);
		addView(view);
	}
	
	public void setResultEntity(ResultEntity resultEntity){
		mResultEntity=resultEntity;
	}
	
	public void setModeName(String modeName){
		mModeNameText.setText(modeName);
	}
	public void setDistance(float distance){
		mDistanceText.setText(String.format("%.1f公里", distance));
	}
	public void setTime(int time){
		mTimeText.setText(String.format("%d分钟", time));
	}
	
	public void setDesTextView(TextView desTextView){
		mDesText=desTextView;
	}
	
	public void setSelected(boolean isSelected){
		if(isSelected){
			mTimeText.setTextColor(Color.BLUE);
			mDistanceText.setTextColor(Color.BLUE);
			mModeNameText.setBackgroundResource(R.drawable.route_bg_blue);
			if(mResultEntity==null){
				return;
			}
			mResultEntity.mSelecedPolyline.setZIndex(0.7f);
			mAmap.moveCamera(CameraUpdateFactory.newLatLngBounds(mResultEntity.mRouteBounds, 10));
			StringBuffer sBuffer=new StringBuffer();
			sBuffer.append("打车约").append(String.format("%.1f元", mResultEntity.mTaxiCost));
			if(mResultEntity.mTollCost>0){
				sBuffer.append(String.format(",过路费约%.1f元",mResultEntity.mTollCost));
			};
			
			
			mDesText.setText(sBuffer.toString());
		}
		else{
			mTimeText.setTextColor(Color.BLACK);
			mDistanceText.setTextColor(Color.BLACK);
			mModeNameText.setBackgroundResource(R.drawable.check_nomal);
			if(mResultEntity==null){
				return;
			}
			mResultEntity.mSelecedPolyline.setZIndex(0.3f);
		}
	}
	
	
	

}
  
