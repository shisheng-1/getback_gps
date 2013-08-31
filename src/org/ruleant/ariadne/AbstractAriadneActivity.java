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
 * @package org.ruleant.ariadne
 * @author Dieter Adriaenssens <ruleant@users.sourceforge.net>
 */
package org.ruleant.ariadne;

import org.ruleant.ariadne.LocationService.LocationBinder;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

/**
 * Abstract Ariadne Activity class, contains the common methods
 * to connect to LocationService. Other activities that need LocationService
 * can extend this class.
 *
 * @author Dieter Adriaenssens <ruleant@users.sourceforge.net>
 */
public abstract class AbstractAriadneActivity extends Activity {
    /**
     * Interface to LocationService instance.
     */
    private LocationService mService;
    /**
     * Connection state with LocationService.
     */
    private boolean mBound = false;

    /**
     * Crouton configuration.
     */
    private Configuration croutonConfig;

    /**
     * Inaccurate location crouton.
     */
    private Crouton crInaccurateLocation;

    /**
     * Inaccurate direction crouton.
     */
    private Crouton crInaccurateDirection;

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu;
        // this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.common, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to LocationService
        Intent intent = new Intent(this, LocationService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // create Crouton configuration
        croutonConfig = new Configuration.Builder()
                .setDuration(Configuration.DURATION_INFINITE)
                .build();

        // create inaccurate location crouton
        crInaccurateLocation = Crouton.makeText(this,
                R.string.inaccurate_location, Style.ALERT);
        crInaccurateLocation.setConfiguration(croutonConfig);

        // create inaccurate direction crouton
        crInaccurateDirection = Crouton.makeText(this,
                R.string.inaccurate_direction, Style.INFO);
        crInaccurateDirection.setConfiguration(croutonConfig);
    }

    @Override
    protected final void onDestroy() {
        super.onDestroy();
        // Unbind from the service
        Crouton.cancelAllCroutons();
    }

    /**
     * Called when the user clicks the Store Location menu item.
     * It displays a dialog, where the user confirm or cancel storing
     * the current location.
     *
     * @param item MenuItem object that was clicked
     */
    public final void storeLocation(final MenuItem item) {
        if (mBound && mService.getLocation() == null) {
            Toast.makeText(
                    this,
                    R.string.store_location_disabled,
                    Toast.LENGTH_LONG
                ).show();
            return;
        }

        // Use the Builder class for convenient dialog construction,
        // based on the example on
        // https://developer.android.com/guide/topics/ui/dialogs.html
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_store_location)
               .setPositiveButton(R.string.store_location,
                       new DialogInterface.OnClickListener() {
                   public void onClick(final DialogInterface dialog,
                           final int id) {
                       // store current location and refresh display
                       if (mBound) {
                           mService.storeCurrentLocation();
                       }
                       refreshDisplay();
                   }
               })
               .setNegativeButton(R.string.no,
                       new DialogInterface.OnClickListener() {
                   public void onClick(final DialogInterface dialog,
                           final int id) {
                       // User cancelled the dialog
                   }
               });

        // Create the AlertDialog object and display it
        builder.create().show();
    }

    /**
     * Called when the user clicks the refresh menu item.
     *
     * @param item MenuItem object that was clicked
     */
    public final void refresh(final MenuItem item) {
        if (mBound) {
            mService.updateLocationProvider();
            mService.updateLocation();
        }
        refreshDisplay();
    }

    /**
     * Called when the user clicks the About menu item.
     *
     * @param item MenuItem object that was clicked
     */
    public final void displayAbout(final MenuItem item) {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }

    /**
     * Called when the user clicks the Settings menu item.
     *
     * @param item MenuItem object that was clicked
     */
    public final void displaySettings(final MenuItem item) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    @Override
    public final boolean onOptionsItemSelected(final MenuItem item) {
        // One of the group items (using the onClick attribute) was clicked
        // The item parameter passed here indicates which item it is
        // All other menu item clicks are handled by onOptionsItemSelected()
        switch (item.getItemId()) {
        case R.id.menu_settings:
            displaySettings(item);
            return true;
        case R.id.menu_about:
            displayAbout(item);
            return true;
        case R.id.menu_storelocation:
            storeLocation(item);
            return true;
        case R.id.menu_refresh:
            refresh(item);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
         MenuItem mi = menu.findItem(R.id.menu_storelocation);
         if (mBound) {
             // enable store location button if a location is set
             mi.setEnabled(mService.getLocation() != null);
         }

         return super.onPrepareOptionsMenu(menu);
    }

    /**
     * Refresh display : refresh the values of Location Provider, Location, ...
     */
    protected void refreshDisplay() {
        // only refresh items if activity is bound to service
        if (!isBound()) {
            return;
        }

        refreshCrouton();
    }

    /**
     * Update which crouton should be displayed.
     */
    protected final void refreshCrouton() {
        // only refresh items if activity is bound to service
        if (!isBound()) {
            return;
        }

        Navigator navigator = getService().getNavigator();

        // if location is inaccurate, display warning
        if (!navigator.isLocationAccurate()) {
            crInaccurateLocation.show();
        } else {
            crInaccurateLocation.cancel();
            // if bearing is inaccurate, display warning
            if (!navigator.isBearingAccurate()) {
                crInaccurateDirection.show();
            } else {
                crInaccurateDirection.cancel();
            }
        }
    }

    /**
     * Returns bound state to Location Service.
     *
     * @return boolean Bound State
     */
    protected final boolean isBound() {
        return mBound;
    }

    /**
     * Returns Location Service.
     *
     * @return LocationService
     */
    protected final LocationService getService() {
        return mService;
    }

    /**
     * Defines callbacks for service binding, passed to bindService().
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(
                final ComponentName className, final IBinder service) {
            // We've bound to LocationService, cast the IBinder
            // and get LocationService instance
            LocationBinder binder = (LocationBinder) service;
            mService = binder.getService();
            mBound = true;

            // We want to monitor the service for as long as we are
            // connected to it.
            binder.registerCallback(mCallback);

            refreshDisplay();
        }

        @Override
        public void onServiceDisconnected(final ComponentName arg0) {
            mBound = false;
        }
    };

    /**
     * This implementation is used to receive callbacks
     * from the remote service.
     */
    private ILocationServiceCallback mCallback
        = new ILocationServiceCallback.Stub() {
        /**
         * Called by the LocationService when a location is updated,
         * it gets the new location and refreshes the display.
         */
        public void locationUpdated() {
            refreshDisplay();
        }

        /**
         * Called by the LocationService when a location provider is updated,
         * it gets the new location provider and refreshes the display.
         */
        public void providerUpdated() {
            refreshDisplay();
        }
    };
}
