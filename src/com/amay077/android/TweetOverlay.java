package com.amay077.android;

import java.util.List;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Shader.TileMode;
import android.os.AsyncTask.Status;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class TweetOverlay extends Overlay
	implements OnMapEventListener {

	// fields -----------------------------------------------------------------
	private Paint fillPaint = new Paint();
	private Paint penPaint = new Paint();
	private Paint imagePaint = new Paint();
	private Paint textPaint = new Paint();
	private MapEventDetector mapEventDetector = new MapEventDetector(this);
	private LoadingTask loadingTask = null;
	private TaskFactory taskFactory = null;
	private List<GeoPointWithInfo> taskResult = null;

	// ctor -------------------------------------------------------------------
	public TweetOverlay(TaskFactory taskFactory) {
		 this.taskFactory = taskFactory;

		 fillPaint.setColor(Color.BLACK);
		 fillPaint.setStyle(Style.FILL);
		 fillPaint.setShadowLayer(3f, 3f, 3f, Color.BLACK);

		 penPaint.setColor(Color.BLACK);
		 penPaint.setStrokeWidth(1f);
		 penPaint.setStyle(Style.STROKE);

		 textPaint.setColor(Color.BLACK);
		 textPaint.setTextSize(16f);
		 textPaint.setTextAlign(Align.CENTER);
		 textPaint.setAntiAlias(true);

	}

	// setter/getter ----------------------------------------------------------

	// overrides --------------------------------------------------------------
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		Log.d(this.getClass().toString(), "draw");
		super.draw(canvas, mapView, shadow);
		mapEventDetector.onDrawEvent(mapView);
		if (shadow) { return; }

		try {
			if ((this.loadingTask != null) && (this.loadingTask.getStatus() == Status.FINISHED) && (this.loadingTask.get() != null)) {
				this.taskResult = this.loadingTask.get();
			}
			if (this.taskResult == null) { return; }

 			Projection proj = mapView.getProjection();
			Point p = new Point();

			for (GeoPointWithInfo geoPoint : taskResult) {
				 proj.toPixels(geoPoint, p);
				 if (geoPoint.profileImage == null) {
					 canvas.drawCircle(p.x, p.y, 10f, fillPaint);
				 } else {
					 Rect r = new Rect(p.x - (geoPoint.profileImage.getWidth() / 2),
							 p.y - (geoPoint.profileImage.getHeight() / 2),
							 p.x + (geoPoint.profileImage.getWidth() / 2),
							 p.y + (geoPoint.profileImage.getHeight() / 2));
					 canvas.drawBitmap(geoPoint.profileImage,
							 new Rect(0, 0, geoPoint.profileImage.getWidth(), geoPoint.profileImage.getHeight()),
							 r, imagePaint);
					 canvas.drawRect(r, penPaint);
				 }

				 Rect bounds = new Rect();
				 textPaint.getTextBounds(geoPoint.userName, 0, geoPoint.userName.length(), bounds);
				 bounds.offset(p.x - (bounds.width() / 2), p.y);
				 bounds.inset(-3, -3); // 膨らます
				 bounds.offset(0, -(geoPoint.profileImage.getHeight() / 2));

				 LinearGradient gradient = new LinearGradient(bounds.left, bounds.top, bounds.left, bounds.bottom, Color.WHITE, Color.LTGRAY, TileMode.CLAMP);
				 fillPaint.setShader(gradient);
				 canvas.drawRect(bounds, fillPaint);
				 canvas.drawRect(bounds, penPaint);

				 canvas.drawText(geoPoint.userName, p.x, p.y-(geoPoint.profileImage.getHeight() / 2), textPaint);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onTap(GeoPoint geopoint, MapView mapview) {
		Log.d(this.getClass().toString(), "onTap");

		loadingTask = (LoadingTask) this.taskFactory.CreateTask();
		loadingTask.execute();

		return super.onTap(geopoint, mapview);
	}

	@Override
	public void onMapCenterChanged(MapView mapview) {
		Log.d(this.getClass().toString(), "onMapCenterChanged");
		loadingTask = (LoadingTask) this.taskFactory.CreateTask();
		loadingTask.execute();
	}

	@Override
	public void onZoomLevelChanged(MapView mapview, int beforeZoomLevel) {
		Log.d(this.getClass().toString(), "onZoomLevelChanged");

		// 拡大の場合は何もしない
		if (beforeZoomLevel < mapview.getZoomLevel()) {
			return;
		}

		loadingTask = (LoadingTask) this.taskFactory.CreateTask();
		loadingTask.execute();
	}

	// public methods ---------------------------------------------------------
	// private methods --------------------------------------------------------
}
