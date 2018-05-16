package soft.brunhilda.org.dailymenupicker.fragments

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.support.v4.content.ContextCompat
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import soft.brunhilda.org.dailymenupicker.ComparablePlace

class   MapViewManager(
        private val activity: Activity,
        private val context: Context,
        private val googleMap: GoogleMap,
        private val place: ComparablePlace
){
    fun createMap(){
        if(!this.checkPermission()){
            return
        }
        googleMap.uiSettings.setAllGesturesEnabled(true)
        googleMap.isMyLocationEnabled = true

        val locationManager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val myLatlng = LatLng(
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).latitude,
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).longitude)
        val markerLatlng = LatLng(place.latitude, place.longitude)

        val cameraPosition: CameraPosition = CameraPosition.Builder()
                .target(markerLatlng)
                .zoom(13.0f).build()
        val cameraUpdate: CameraUpdate = CameraUpdateFactory . newCameraPosition (cameraPosition)
        googleMap.moveCamera(cameraUpdate)
        googleMap.addMarker(MarkerOptions()
                .position(markerLatlng)
                .title(place.name)).showInfoWindow()

        val bounds = LatLngBounds.Builder()
                .include(markerLatlng)
                .include(myLatlng)
                .build()
        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
    }

    fun checkPermission(): Boolean{
        return ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                &&
                ContextCompat.checkSelfPermission(context,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    }
}