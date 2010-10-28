package com.amay077.android;

import twitter4j.Tweet;
import android.graphics.Bitmap;

import com.google.android.maps.GeoPoint;

public class GeoPointWithInfo extends GeoPoint {

	static public final double NOISE_DIST_DEGREE = (360d / 40077000d) * 50; // 50m は何度？を計算してる

	public Bitmap profileImage = null;
	public String userName = null;
	public String latestText = null;
	public Tweet tweet = null;

	static public GeoPointWithInfo MakeWithNoise(double latitude, double longitude) {
		double noiseLat, noiseLong;
		noiseLat = NOISE_DIST_DEGREE * Math.random();
		noiseLong = NOISE_DIST_DEGREE * Math.random();
		return new GeoPointWithInfo((int)((latitude + noiseLat) * 1E6), (int)((longitude + noiseLong) * 1E6));
	}

	public GeoPointWithInfo(int latitudeE6, int longitudeE6) {
		super(latitudeE6, longitudeE6);
	}

}
