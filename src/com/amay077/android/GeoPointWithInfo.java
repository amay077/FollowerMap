package com.amay077.android;

import twitter4j.Tweet;
import android.graphics.Bitmap;

import com.google.android.maps.GeoPoint;

public class GeoPointWithInfo extends GeoPoint {

	public Bitmap profileImage = null;
	public String userName = null;
	public String latestText = null;
	public Tweet tweet = null;

	public GeoPointWithInfo(int latitudeE6, int longitudeE6) {
		super(latitudeE6, longitudeE6);
	}

}
