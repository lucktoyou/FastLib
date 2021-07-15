package com.fastlib.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.View;

/**
 * 屏幕相关工具集
 * 
 * @author Administrator
 * 
 */
public class ScreenUtil {

	private ScreenUtil() {
	}

	/**
	 * 获取某视图宽高
	 * @param view
	 * @return
	 */
	public static Point getSize(View view){
		int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
		int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
		view.measure(w, h);
		return new Point(view.getMeasuredWidth(),view.getMeasuredHeight());
	}

	/**
	 * 获得屏幕宽度
	 * @return 屏幕宽度
	 */
	public static int getScreenWidth(){
		DisplayMetrics outMetrics = Resources.getSystem().getDisplayMetrics();
		return outMetrics.widthPixels;
	}

	/**
	 * 获得屏幕高度
	 * @return 屏幕高度
	 */
	public static int getScreenHeight() {
		DisplayMetrics outMetrics =Resources.getSystem().getDisplayMetrics();
		return outMetrics.heightPixels;
	}

	/**
	 * 获取屏幕尺寸
	 * @return 屏幕宽高
	 */
	public static Point getScreenSize(){
		return new Point(getScreenWidth(),getScreenHeight());
	}

	/**
	 * 获得状态栏的高度
	 * 
	 * @param context
	 * @return
	 */
	public static int getStatusHeight(Context context) {
		int statusHeight = -1;
		try {
			Class<?> clazz = Class.forName("com.android.internal.R$dimen");
			Object object = clazz.newInstance();
			int height = Integer.parseInt(clazz.getField("status_bar_height").get(object).toString());
			statusHeight = context.getResources().getDimensionPixelSize(height);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return statusHeight;
	}

	/**
	 * 获取当前屏幕截图，包含状态栏
	 * 
	 * @param activity
	 * @return
	 */
	public static Bitmap snapShotWithStatusBar(Activity activity) {
		View view = activity.getWindow().getDecorView();
		view.setDrawingCacheEnabled(true);
		view.buildDrawingCache();
		Bitmap bmp = view.getDrawingCache();
		int width = getScreenWidth();
		int height = getScreenHeight();
		Bitmap bp = null;
		bp = Bitmap.createBitmap(bmp, 0, 0, width, height);
		view.destroyDrawingCache();
		return bp;
	}

	/**
	 * 获取当前屏幕截图，不包含状态栏
	 * 
	 * @param activity
	 * @return
	 */
	public static Bitmap snapShotWithoutStatusBar(Activity activity) {
		View view = activity.getWindow().getDecorView();
		view.setDrawingCacheEnabled(true);
		view.buildDrawingCache();
		Bitmap bmp = view.getDrawingCache();
		Rect frame = new Rect();
		activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
		int statusBarHeight = frame.top;

		int width = getScreenWidth();
		int height = getScreenHeight();
		Bitmap bp = null;
		bp = Bitmap.createBitmap(bmp, 0, statusBarHeight, width, height - statusBarHeight);
		view.destroyDrawingCache();
		return bp;
	}
}
