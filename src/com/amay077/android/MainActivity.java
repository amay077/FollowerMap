package com.amay077.android;

import java.util.HashMap;
import java.util.Map;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.vividsolutions.jts.index.SpatialIndex;
import com.vividsolutions.jts.index.quadtree.Quadtree;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.AsyncTask.Status;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;

public class MainActivity extends MapActivity implements Progressable, TaskFactory  {

	private LoadingTask fatTask = null;
	private ProgressBar prgBar = null;
	private MapView mapview = null;
	private Map<String, GeoPointWithInfo> addrGeoMap = new HashMap<String, GeoPointWithInfo>();
	private Map<Integer, Bitmap> userImageMap = new HashMap<Integer, Bitmap>();
	private SpatialIndex spIndex = new Quadtree();

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        prgBar = (ProgressBar)findViewById(R.id.ProgressBar01);

        mapview = (MapView)findViewById(R.id.mapView);
        mapview.setBuiltInZoomControls(true);

        final TweetOverlay overlay = new TweetOverlay(this);
        mapview.getOverlays().add(overlay);
        mapview.getOverlays().add(new GeoHexOverlay());

        Button btn = (Button) findViewById(R.id.Button01);
        btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.d(this.getClass().toString(), "onClick");

				fatTask = CreateTask();
				fatTask.execute();
			}
		});

        btn = (Button) findViewById(R.id.Button02);
        btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.d(this.getClass().toString(), "onCancel");

				if (fatTask != null && fatTask.getStatus() == Status.RUNNING) {
					fatTask.cancel(true);
					fatTask = null;
				}
			}
		});
}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	@Override
	public void onProgress(float percent) {
		prgBar.setVisibility(percent == 100f ? View.INVISIBLE  : View.VISIBLE);
	}

	@Override
	public LoadingTask CreateTask() {
		if (fatTask != null && fatTask.getStatus() == Status.RUNNING) {
			fatTask.cancel(true);
			fatTask = null;
		}

		fatTask = new LoadingTask(mapview, this, addrGeoMap, userImageMap, spIndex);
		return fatTask;
	}
}