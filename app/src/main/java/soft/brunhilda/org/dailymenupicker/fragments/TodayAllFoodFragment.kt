package soft.brunhilda.org.dailymenupicker.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.content_food_today.*
import noman.googleplaces.*
import soft.brunhilda.org.dailymenupicker.adapters.FoodEntityAdapter
import soft.brunhilda.org.dailymenupicker.R
import soft.brunhilda.org.dailymenupicker.entity.FoodEntityAdapterItem
import soft.brunhilda.org.dailymenupicker.entity.RestaurantWeekData
import soft.brunhilda.org.dailymenupicker.preparers.NearestPlacesDataPreparer
import soft.brunhilda.org.dailymenupicker.resolvers.CachedRestDataResolver
import soft.brunhilda.org.dailymenupicker.transformers.FoodAdapterTransformer

class TodayAllFoodFragment : Fragment() {

    private val dataPreparer = NearestPlacesDataPreparer.getInstance()
    private val dataResolver = CachedRestDataResolver.getInstance()
    private val dataTransformer = FoodAdapterTransformer.getInstance()

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dataPreparer.callback = this::placesPreparationIsFinished
        dataPreparer.findPlaces()
    }

    fun placesPreparationIsFinished(places: List<Place>) {
        dataResolver.callback = this::placesResolvingIsFinished
        dataResolver.resolvePlaces(places)
    }

    fun placesResolvingIsFinished(places: Map<Place, RestaurantWeekData?>) {
        val adapterItems = dataTransformer.transform(places)
        adapterItems.sortWith(compareBy { it.preferenceEvaluation })

        today_food_list_view.adapter = FoodEntityAdapter(context, adapterItems)
    }


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.content_food_today, container, false)
    }

}