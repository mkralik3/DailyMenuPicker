package soft.brunhilda.org.dailymenupicker.fragments

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import soft.brunhilda.org.dailymenupicker.R
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.widget.LinearLayoutManager
import android.widget.LinearLayout
import com.google.android.gms.maps.*
import soft.brunhilda.org.dailymenupicker.ComparablePlace
import soft.brunhilda.org.dailymenupicker.adapters.FoodEntityAdapter_recycler
import soft.brunhilda.org.dailymenupicker.entity.RestaurantWeekData
import soft.brunhilda.org.dailymenupicker.resolvers.CachedRestDataResolver
import soft.brunhilda.org.dailymenupicker.transformers.FoodAdapterTransformer
import com.google.android.gms.maps.MapView
import kotlinx.android.synthetic.main.list_days.*
import soft.brunhilda.org.dailymenupicker.database.DatabaseManager

class ParticularRestaurantFragment : ParentFragment(), OnMapReadyCallback {
    private lateinit var place: ComparablePlace
    private lateinit var mapView: MapView

    private val dataResolver = CachedRestDataResolver()
    private val dataTransformer = FoodAdapterTransformer.getInstance()

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val databaseManager = DatabaseManager(context)
        place = this.arguments.getSerializable("googlePlace") as ComparablePlace

        val myFab = view?.findViewById(R.id.fab) as FloatingActionButton
        setUpFavouriteButton(databaseManager, myFab, view)

        placesPreparationIsFinished(mutableSetOf(place))
    }

    private fun setUpFavouriteButton(databaseManager: DatabaseManager, myFab: FloatingActionButton, view: View) {
        if (databaseManager.isPlaceInDb(place.placeId)) {
            myFab.setImageResource(android.R.drawable.ic_delete)
        } else {
            myFab.setImageResource(android.R.drawable.ic_menu_save)
        }
        myFab.setOnClickListener {
            if (databaseManager.isPlaceInDb(place.placeId)) {
                databaseManager.deleteFavouritePlace(place)
                Snackbar
                        .make(view, "Place was removed from the favourite places", Snackbar.LENGTH_LONG)
                        .setAction("UNDO",View.OnClickListener { view ->
                            Snackbar
                                    .make(view, "Place was added to the favourite places!", Snackbar.LENGTH_SHORT)
                                    .show()
                            myFab.callOnClick()
                        })
                        .show()
                myFab.setImageResource(android.R.drawable.ic_menu_save)
            } else {
                databaseManager.addFavouritePlace(place)
                Snackbar
                        .make(view, "Place was added to the favourite places", Snackbar.LENGTH_LONG)
                        .setAction("UNdo",View.OnClickListener { view ->
                            Snackbar
                                    .make(view, "Place was removed from the favourite places!", Snackbar.LENGTH_SHORT)
                                    .show()
                            myFab.callOnClick()
                        })
                        .show()
                myFab.setImageResource(android.R.drawable.ic_delete)
            }
        }
    }

    private fun placesPreparationIsFinished(places: Set<ComparablePlace>) {
        dataResolver.resolvePlaces(places.toList(), this::placesResolvingIsFinished)
    }

    private fun placesResolvingIsFinished(places: Map<ComparablePlace, RestaurantWeekData?>) {
        if (context != null) {
            val data = places.values.first()?.findTodayMenu() ?: return //TODO change to weekData.getDayData
            day_view_monday.layoutManager = LinearLayoutManager(context, LinearLayout.VERTICAL, false)
            day_view_monday.adapter = FoodEntityAdapter_recycler(
                    dataTransformer.transform(place, data))
            day_view_tuesday.layoutManager = LinearLayoutManager(context, LinearLayout.VERTICAL, false)
            day_view_tuesday.adapter = FoodEntityAdapter_recycler(
                    dataTransformer.transform(place, data))
            day_view_wednesday.layoutManager = LinearLayoutManager(context, LinearLayout.VERTICAL, false)
            day_view_wednesday.adapter = FoodEntityAdapter_recycler(
                    dataTransformer.transform(place, data))
            day_view_thursday.layoutManager = LinearLayoutManager(context, LinearLayout.VERTICAL, false)
            day_view_thursday.adapter = FoodEntityAdapter_recycler(
                    dataTransformer.transform(place, data))
            day_view_friday.layoutManager = LinearLayoutManager(context, LinearLayout.VERTICAL, false)
            day_view_friday.adapter = FoodEntityAdapter_recycler(
                    dataTransformer.transform(place, data))
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater?.inflate(R.layout.content_particular_restaurant, container, false)

        mapView = view?.findViewById(R.id.mapwhere) as MapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        return view
    }

    override fun onMapReady(googleMap: GoogleMap) {
        val mapManager = MapViewManager(activity, context, googleMap,place)
        if (mapManager.checkPermission())
            mapManager.createMap()
        else
            requestPermissions(
                    arrayOf(
                            android.Manifest.permission.ACCESS_COARSE_LOCATION,
                            android.Manifest.permission.ACCESS_FINE_LOCATION
                    ),1)
    }

    override fun onResume() {
        super.onResume()
        activity.title = place.name
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(grantResults.filter { it==PackageManager.PERMISSION_DENIED }.isNotEmpty()){
            System.err.println("Permission was denied, permission: $permissions")
            fragmentManager.popBackStackImmediate() //sorry, get back
        }else{
            val mapView = activity.findViewById(R.id.mapwhere) as MapView
            mapView.getMapAsync(this)
        }
    }
}