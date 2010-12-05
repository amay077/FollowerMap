package com.amay077.android.followermap;

import java.util.Timer;
import java.util.TimerTask;

import net.geohex.GeoHex;

import com.amay077.android.followermap.R;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;

import android.content.Intent;
import android.graphics.Point;
import android.hardware.GeomagneticField;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends MapActivity {

    private static final int MENU_ID_START = (Menu.FIRST + 1);
    private static final int MENU_ID_CONFIG = (Menu.FIRST + 2);


    private MapView mapview = null;
    private MyLocationOverlay myLocOverlay = null;
    private GeoHexOverlay watchHexOverlay = new GeoHexOverlay();
    private String currentWatchArea = "";

    private Handler handler = new Handler();
    private AudioManager mAudio = null;
	private Vibrator mVibrator = null;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mapview = (MapView)findViewById(R.id.mapView);
        mapview.setBuiltInZoomControls(true);

        mapview.getOverlays().add(watchHexOverlay);
        myLocOverlay = new MyLocationOverlayEx(this, mapview);
        mapview.getOverlays().add(myLocOverlay);

        mAudio = (AudioManager) getSystemService(this.AUDIO_SERVICE);
        mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
    }

    private void enabledMyLocation() {
    	while (true) {
            try {
        		myLocOverlay.enableMyLocation();
                //myLocOverlay.enableCompass();
        		return;
    		} catch (Exception e) {
    			SystemClock.sleep(2000);
    		}
    	}
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

    private boolean theFirst = true;
    private boolean sended = false;

	private void startWatchTimer() {

		enabledMyLocation();


		theFirst = true;

		Timer watchTimer = new Timer();

		watchTimer.schedule(new TimerTask() {

			@Override
			public void run() {

				// 1. 現在位置を取得
				final GeoPoint point = myLocOverlay.getMyLocation();

				Log.d("startWatchTimer", point != null ? String.valueOf(point.getLatitudeE6()) : "point is null");

				if (point == null) {
					return;
				}

				if (theFirst) {
					theFirst = false;
					handler.post(new Runnable() {
						public void run() {
							mapview.getController().animateTo(point);
						}
					});
				}

				boolean found = false;
				boolean isEscape = false;
				for (String geoHexCode : watchHexOverlay.getSelectedGeoHexCodes().keySet()) {

					// 監視する GeoHex を取得
					GeoHex.Zone watchZone = GeoHex.decode(geoHexCode);

					// 監視する GeoHex と同じレベルで、現在位置の GeoHex を取得
					// ※同じレベルにすることで Code の一致でエリア内判定をする。
					final GeoHex.Zone currentZone = GeoHex.getZoneByLocation(point.getLatitudeE6() / 1E6,
							point.getLongitudeE6() / 1E6, watchZone.level);

					if (watchZone.code.equals(currentZone.code)) {
						found = true;

						if (currentWatchArea == currentZone.code) {
							return;
						}

						currentWatchArea = currentZone.code;

						// Notify!
						// 4. ヒットしたらマナーモードにする
						Log.d("startWatchTimer", "found! - GeoHex code : " + currentZone.code);

						handler.post(new Runnable() {

							public void run() {
//								Toast.makeText(MainActivity.this, "found! - GeoHex code : " + currentZone.code, Toast.LENGTH_SHORT).show();
								Toast.makeText(MainActivity.this, "通知エリアに入りました！", Toast.LENGTH_SHORT).show();
								mAudio.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
								mAudio.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
								mVibrator.vibrate(1000);

								if (!sended) {
									sendMail();
									sended = true;
								}

							}

							private void sendMail() {
								Intent it = new Intent();
								it.setAction(Intent.ACTION_SENDTO);
								String to = "my-wife@home.net";
								it.setData(Uri.parse("mailto:" + to));
								it.putExtra(Intent.EXTRA_SUBJECT, "今、帰宅中");
								it.putExtra(Intent.EXTRA_TEXT, "ほげ");
								startActivity(it);							}

						});
					}
				}

//				if (!found && currentWatchArea != "") {
//					handler.post(new Runnable() {
//
//						public void run() {
//							Toast.makeText(MainActivity.this, "通知エリアを離脱しました！", Toast.LENGTH_SHORT).show();
//
//						}
//					});
//				}
			}
		}, 0, 1000); // 1秒ごと

	}
}
