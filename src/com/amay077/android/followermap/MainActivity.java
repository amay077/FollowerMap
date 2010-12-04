package com.amay077.android.followermap;

import com.amay077.android.followermap.R;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import android.os.Bundle;

public class MainActivity extends MapActivity {

	private MapView mapview = null;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mapview = (MapView)findViewById(R.id.mapView);
        mapview.setBuiltInZoomControls(true);

        mapview.getOverlays().add(new GeoHexOverlay());
}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
}
