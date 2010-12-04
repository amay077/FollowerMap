package com.amay077.android.followermap;

import java.util.Timer;
import java.util.TimerTask;

import com.amay077.android.followermap.R;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends MapActivity {

    private static final int MENU_ID_START = (Menu.FIRST + 1);
    private static final int MENU_ID_CONFIG = (Menu.FIRST + 2);


    private MapView mapview = null;
    private MyLocationOverlay myLocOverlay = null;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mapview = (MapView)findViewById(R.id.mapView);
        mapview.setBuiltInZoomControls(true);

        mapview.getOverlays().add(new GeoHexOverlay());
        myLocOverlay = new MyLocationOverlay(this, mapview);
        mapview.getOverlays().add(myLocOverlay);
}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	   // オプションメニューが最初に呼び出される時に1度だけ呼び出されます
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // メニューアイテムを追加します
        menu.add(Menu.NONE, MENU_ID_START, Menu.NONE, "開始");
        menu.add(Menu.NONE, MENU_ID_CONFIG, Menu.NONE, "設定");
        return super.onCreateOptionsMenu(menu);
    }

//    // オプションメニューが表示される度に呼び出されます
//    @Override
//    public boolean onPrepareOptionsMenu(Menu menu) {
//        menu.findItem(MENU_ID_CONFIG).setVisible(visible);
//        visible = !visible;
//        return super.onPrepareOptionsMenu(menu);
//    }

    // オプションメニューアイテムが選択された時に呼び出されます
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean ret = true;
        switch (item.getItemId()) {
        default:
            ret = super.onOptionsItemSelected(item);
            break;
        case MENU_ID_START:
        	Toast.makeText(this, "監視を開始！", Toast.LENGTH_SHORT).show();
        	startWatchTimer();

            ret = true;
            break;
        case MENU_ID_CONFIG:
        	Toast.makeText(this, "設定画面を表示", Toast.LENGTH_SHORT).show();
            ret = true;
            break;
        }
        return ret;
    }

	private void startWatchTimer() {
		Timer watchTimer = new Timer();

		watchTimer.schedule(new TimerTask() {

			@Override
			public void run() {

				GeoPoint point = myLocOverlay.getMyLocation();

				Log.d("startWatchTimer", String.valueOf(point.getLatitudeE6()));

			}
		}, 0, 5000);

	}
}
