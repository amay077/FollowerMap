package com.amay077.android;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import twitter4j.GeoLocation;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Tweet;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.index.ItemVisitor;
import com.vividsolutions.jts.index.SpatialIndex;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

public class LoadingTask extends AsyncTask<Void, Float, List<GeoPointWithInfo>> {
	// fields -----------------------------------------------------------------
	private MapView mapview = null;
	private Progressable progressable = null;
	private Map<String, GeoPointWithInfo> addrGeoMap = null;
	private Map<Integer, Bitmap> userImageMap = null;
	private Geocoder geoCoder = null;
    private Twitter twitter = new TwitterFactory().getInstance();//("amay077", "xxxxx");
    private SpatialIndex spIndex = null;

	// ctor -------------------------------------------------------------------
	public LoadingTask(MapView mapview, Progressable progressable,
			Map<String, GeoPointWithInfo> addrGeoMap, Map<Integer, Bitmap> userImageMap, SpatialIndex spIndex) {
		this.mapview = mapview;
		this.progressable = progressable;
		this.addrGeoMap = addrGeoMap;
		this.userImageMap = userImageMap;
		this.spIndex = spIndex;
	}

	// setter/getter ----------------------------------------------------------
	// overrides --------------------------------------------------------------
	@Override
	protected List<GeoPointWithInfo> doInBackground(Void... params) {
		Log.d(this.getClass().toString(), "doInBackground");

		onProgressUpdate(0f);

		try {
			GeoPoint geoCenter = this.mapview.getMapCenter();

			final List<GeoPointWithInfo> results = new Vector<GeoPointWithInfo>();
			final Set<Long> idSet = new HashSet<Long>();

			// twitter
	        try {
	        	double halfLongSpan = mapview.getLongitudeSpan() / 2d;
	        	double halfLatSpan = mapview.getLatitudeSpan() / 2d;
	        	Envelope envSearch = new Envelope(
	        			geoCenter.getLongitudeE6() - halfLongSpan, geoCenter.getLongitudeE6() + halfLongSpan,
	        			geoCenter.getLatitudeE6() - halfLatSpan, geoCenter.getLatitudeE6() + halfLatSpan);
	        	spIndex.query(envSearch, new ItemVisitor() {

					@Override
					public void visitItem(Object obj) {
						GeoPointWithInfo gp = (GeoPointWithInfo)obj;
						if (!idSet.contains(gp.tweet.getId())) {
							results.add(gp);
							idSet.add(gp.tweet.getId());
						}
					}
				});

	        	double radius = (mapview.getLongitudeSpan() / 1E6 / 4) * 111.325d;
	            Query query = new Query();
	            query.setGeoCode(new GeoLocation(geoCenter.getLatitudeE6() / 1E6, geoCenter.getLongitudeE6() / 1E6),
	            		radius, Query.KILOMETERS);
	            query.setRpp(10);

	            Date yesterday = new Date();
	            yesterday.setTime(yesterday.getTime() - 86400000L);
	            query.setSince(new SimpleDateFormat("yyyy-MM-dd").format(yesterday));
	            query.setLang("ja");
	            QueryResult result = twitter.search(query);
	            List<Tweet> tweets = result.getTweets();
	            Log.d(this.getClass().toString(), String.valueOf(tweets.size()) + " tweets.");
	            int i = 0;

	            /*while (result.getTweets().size() > 0)*/ {
		            for (Tweet tweet : tweets) {
		            	if (isCancelled()) {
							return null;
						}

						if (idSet.contains(tweet.getId())) {
							continue;
						}

		            	GeoPointWithInfo geoP = null;
		            	if (this.addrGeoMap.containsKey(tweet.getLocation())) {
			            	geoP = this.addrGeoMap.get(tweet.getLocation());
		            	} else {
			            	geoP = getLatLongFromTweetAndProfile(tweet);
		            	}

		            	if (geoP != null) {

		            		float[] distances = new float[3];
		        			Location.distanceBetween(geoCenter.getLatitudeE6() / 1E6, geoCenter.getLongitudeE6() / 1E6,
		        					geoP.getLatitudeE6() / 1E6, geoP.getLongitudeE6() / 1E6, distances);

		        			if (distances[0] <= (radius * 1000)) {
			            		Bitmap icon = getProfileIcon(tweet);
			            		geoP.profileImage = icon;
			            		geoP.userName = tweet.getFromUser();
			            		geoP.latestText = tweet.getText();
			            		geoP.tweet = tweet;

			            		results.add(geoP);
			            		spIndex.insert(new Envelope(
			            				geoP.getLongitudeE6()-1, geoP.getLongitudeE6()+1,
			            				geoP.getLatitudeE6()-1, geoP.getLatitudeE6()+1),
			            				geoP);
		        			}
						}
						this.addrGeoMap.put(tweet.getLocation(), geoP); // 取得できなかったやつもキャッシュしとく
						i++;
					}

		            // 次のページを検索
		            //query.setPage(result.getPage() + 1);
		            //result = twitter.search(query);

				}

	        } catch (Exception e) {
	        	e.printStackTrace();
	        }

			onProgressUpdate(100f);
			return results;

		} catch (Exception e) {
			e.printStackTrace();
		}
		onProgressUpdate(100f);
		return null;
	}

	@Override
	protected void onProgressUpdate(Float... values) {
		Log.d(this.getClass().toString(), "onProgressUpdate - " + String.valueOf(values[0]) + " %");
		super.onProgressUpdate(values);
		//if (this.progressable != null) { this.progressable.onProgress(values[0]); }
	}

	@Override
	protected void onPreExecute() {
		Log.d(this.getClass().toString(), "onPreExecute");

		super.onPreExecute();

		if (this.progressable != null) { this.progressable.onProgress(0f); }
	}

	@Override
	protected void onPostExecute(List<GeoPointWithInfo> result) {
		Log.d(this.getClass().toString(), "onPostExecute");

		super.onPostExecute(result);

		if (this.progressable != null) { this.progressable.onProgress(100f); }

		if (result != null) {
			this.mapview.invalidate();
		}
	}

	@Override
	protected void onCancelled() {
		Log.d(this.getClass().toString(), "onCancelled");
		super.onCancelled();
	}


	// public methods ---------------------------------------------------------

	// private methods --------------------------------------------------------


	private GeoPointWithInfo getLatLongFromTweetAndProfile(Tweet tweet) {
        //Log.d(this.getClass().toString(), tweet.getFromUser() + ":" + tweet.getText());
        GeoLocation loc = tweet.getGeoLocation();
        if (loc != null) {
            return new GeoPointWithInfo((int)(loc.getLatitude() * 1E6), (int)(loc.getLongitude() * 1E6));
		} else {
			Log.d(this.getClass().toString(), tweet.getFromUser() + ":" + tweet.getLocation());
			try {
				String[] buf = tweet.getLocation().split("[:,]");
				if (buf.length == 2) {
	                return new GeoPointWithInfo((int)(Double.valueOf(buf[0]) * 1E6), (int)(Double.valueOf(buf[1]) * 1E6));
				} else if (buf.length == 3) {
	                return new GeoPointWithInfo((int)(Double.valueOf(buf[1]) * 1E6), (int)(Double.valueOf(buf[2]) * 1E6));
				} else {
					throw new Exception();
				}
			} catch (Exception e) {
				try {
					if (geoCoder == null) {
						geoCoder = new Geocoder(this.mapview.getContext(), Locale.JAPAN);
					}

					List<Address> list = geoCoder.getFromLocationName(tweet.getLocation(), 1);
					for (Address address : list) {
						Log.d(this.getClass().toString(), address.toString());
						GeoPointWithInfo geoP = new GeoPointWithInfo((int)(address.getLatitude() * 1E6), (int)(address.getLongitude() * 1E6));
		                return geoP;
					}
				} catch (Exception ignore) {
					ignore.printStackTrace();
				}
			}
		}

        return null;
	}

	private Bitmap getProfileIcon(Tweet tweet) {
		try {
			if (userImageMap.containsKey(tweet.getFromUserId())) {
				return userImageMap.get(tweet.getFromUserId());
			}

			URL url = new URL(tweet.getProfileImageUrl());
            InputStream is = url.openStream();

            Bitmap img = BitmapFactory.decodeStream(is);
            userImageMap.put(tweet.getFromUserId(), img);
            return img;

            //		new BitmapDrawable(BitmapFactory.decodeStream(is)));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

}
