/**
 * Main Activity
 * 
 * Copyright (C) 2012-2013 Dieter Adriaenssens
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * @author Dieter Adriaenssens <ruleant@users.sourceforge.net>
 */
package org.ruleant.ariadne;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.ruleant.ariadne.LocationService.LocationBinder;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

/**
 * Main Activity class
 * 
 * @author Dieter Adriaenssens <ruleant@users.sourceforge.net>
 */
public class MainActivity extends Activity {
	/**
	 * Interface to LocationService instance
	 */
	LocationService mService;
	/**
	 * Connection state with LocationService
	 */
	boolean mBound = false;
	/**
	 * Name of the LocationProvider
	 */
	private String mProviderName = "";
	/**
	 * Current Location
	 */
	private Location mCurrentLocation = null;
	/**
	 * Previously stored Location
	 */
	private Location mStoredLocation = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	protected void onStart() {
		super.onStart();
		// Bind to LocalService
		Intent intent = new Intent(this, LocationService.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	public void onStop() {
		super.onStop();
		// Unbind from the service
		if (mBound) {
			unbindService(mConnection);
			mBound = false;
		}
	}

	/**
	 * Called when the user clicks the Renew provider button
	 *
	 * @param View view Button that was clicked
	 *
	 * @return void
	 */
	public void renewProvider(View view) {
		if (mBound) {
			mProviderName = mService.getLocationProvider();
		} else {
			mProviderName = "";
		}
		refreshDisplay();
	}

	/**
	 * Called when the user clicks the Renew location button
	 *
	 * @param View view Button that was clicked
	 *
	 * @return void
	 */
	public void renewLocation(View view) {
		if (mBound) {
			mCurrentLocation = mService.getLocation();
		} else {
			mCurrentLocation = null;
		}
		refreshDisplay();
	}

	/**
	 * Called when the user clicks the Store Location menu item
	 *
	 * @param MenuItem item MenuItem object that was clicked
	 *
	 * @return void
	 */
	public void storeLocation(MenuItem item) {
		if (mBound) {
			mService.storeCurrentLocation();
			mStoredLocation = mService.getStoredLocation();
		} else {
			mStoredLocation = null;
		}
		refreshDisplay();
	}

	/**
	 * refresh display
	 * 
	 * Refresh the values of Location Provider, Location, ...
	 * 
	 * @return void
	 */
	private void refreshDisplay() {
		// Refresh locationProvider
		TextView tv_provider = (TextView) findViewById(R.id.textView_LocationProvider);
		String providerText = getResources().getString(R.string.location_provider) + ": ";
		if (mProviderName.isEmpty()) {
			providerText += getResources().getString(R.string.none);
		} else {
			providerText += mProviderName;
		}
		tv_provider.setText(providerText);

		// Refresh Location
		TextView tv_location = (TextView) findViewById(R.id.textView_Location);
		String locationText = getResources().getString(R.string.location) + ":\n";
		if (mCurrentLocation == null) {
			locationText += getResources().getString(R.string.unknown);
		} else {
			// Format location
			locationText += getResources().getString(R.string.latitude) + ": ";
			locationText += mCurrentLocation.getLatitude() + "°\n";
			locationText += getResources().getString(R.string.longitude) + ": ";
			locationText += mCurrentLocation.getLongitude() + "°\n";
			if (mCurrentLocation.hasAltitude()) {
				locationText += getResources().getString(R.string.altitude) + ": ";
				locationText += mCurrentLocation.getAltitude() + "m\n";
			}
			if (mCurrentLocation.hasBearing()) {
				locationText += getResources().getString(R.string.bearing) + ": ";
				locationText += mCurrentLocation.getBearing() + "°\n";
			}
			if (mCurrentLocation.hasSpeed()) {
				locationText += getResources().getString(R.string.speed) + ": ";
				locationText += mCurrentLocation.getSpeed() + "m/s\n";
			}
			if (mCurrentLocation.hasAccuracy()) {
				locationText += getResources().getString(R.string.accuracy) + ": ";
				locationText += mCurrentLocation.getAccuracy() + "m\n";
			}

			// Format Timestamp
			Date date = new Date(mCurrentLocation.getTime());
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss:SSSz");
			locationText += getResources().getString(R.string.timestamp) + ": ";
			locationText += formatter.format(date) + "\n\n";

			// raw
			locationText += getResources().getString(R.string.raw) + ": ";
			locationText += mCurrentLocation.toString();
		}
		tv_location.setText(locationText);

		// Refresh Stored Location
		TextView tv_StoredLocation = (TextView) findViewById(R.id.textView_StoredLocation);
		String storedLocationText = getResources().getString(R.string.stored_location) + ":\n";
		if (mStoredLocation == null) {
			storedLocationText += getResources().getString(R.string.unknown);
		} else {
			// Format location
			storedLocationText += getResources().getString(R.string.latitude) + ": ";
			storedLocationText += mStoredLocation.getLatitude() + "°\n";
			storedLocationText += getResources().getString(R.string.longitude) + ": ";
			storedLocationText += mStoredLocation.getLongitude() + "°\n";

			// raw
			storedLocationText += getResources().getString(R.string.raw) + ": ";
			storedLocationText += mStoredLocation.toString();
		}
		tv_StoredLocation.setText(storedLocationText);
	}

	/**
	 * Defines callbacks for service binding, passed to bindService()
	 */
	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			// We've bound to LocalService, cast the IBinder and get LocalService instance
			LocationBinder binder = (LocationBinder) service;
			mService = binder.getService();
			mBound = true;
			mProviderName = mService.getLocationProvider();
			if (! mProviderName.isEmpty()) {
				mCurrentLocation = mService.getLocation();
				mStoredLocation = mService.getStoredLocation();
				refreshDisplay();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mBound = false;
		}
	};
}
