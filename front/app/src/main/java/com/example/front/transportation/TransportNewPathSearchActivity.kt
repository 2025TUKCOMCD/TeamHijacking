package com.example.front.transportation

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.front.R
import com.example.front.databinding.ActivityTransportNewPathSearchBinding
import com.example.front.transportation.data.searchPath.PathRouteResult
import com.example.front.transportation.processor.RouteProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/*"경로 클릭시" 출력될 화면, 새 경로를 찾는 화면과 구 경로를
* 재사용할 때 동일하게 사용된다. 피그마의 경로 클릭시 경우 참고.*/
class TransportNewPathSearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTransportNewPathSearchBinding
    private lateinit var routeAdapter: RouteRecycleView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransportNewPathSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val startLat = intent.getDoubleExtra("startLat", 37.513841)
        val startLng = intent.getDoubleExtra("startLng", 127.101823)
        val endLat = intent.getDoubleExtra("endLat", 37.476813)
        val endLng = intent.getDoubleExtra("endLng", 126.964156)

        Log.d("TransportNewPathSearchActivity", "Start Latitude: $startLat")
        Log.d("TransportNewPathSearchActivity", "Start Longitude: $startLng")
        Log.d("TransportNewPathSearchActivity", "End Latitude: $endLat")
        Log.d("TransportNewPathSearchActivity", "End Longitude: $endLng")

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = RouteProcessor.fetchAndProcessRoutes(startLat, startLng, endLat, endLng)
                // Initialize RecyclerView
                routeAdapter = RouteRecycleView(result) { route ->
                    navigateToRouteDetail(route)
                }
                binding.routeRecyclerView.layoutManager = LinearLayoutManager(this@TransportNewPathSearchActivity)
                binding.routeRecyclerView.adapter = routeAdapter

                Log.d("RouteProcessor", "Fetched ${result.size} routes.")

            } catch (e: Exception) {
                Log.e("RouteProcessor", "경로 탐색 중 오류 발생", e)
            }
        }
    }

    private fun navigateToRouteDetail(route: PathRouteResult) {
        // Navigate to TransNewPathDetailActivity with routeStationsAndBuses
        val intent = Intent(this, TransNewPathDetailActivity::class.java)
        val routeStationsAndBusesString = route.busDetails.joinToString(",") { it }
        intent.putExtra("routeStationsAndBuses", routeStationsAndBusesString)
        startActivity(intent)
    }
    class RouteRecycleView(
        private val routes: List<PathRouteResult>,
        private val onItemClick: (PathRouteResult) -> Unit
    ) : RecyclerView.Adapter<RouteRecycleView.RouteViewHolder>() {

        inner class RouteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val transitCountView: TextView = view.findViewById(R.id.transitCountView)
            val totalTimeView: TextView = view.findViewById(R.id.totalTimeView)
            val detailedPathView: TextView = view.findViewById(R.id.detatiledPathView)
            val mainTransitTypesView: TextView = view.findViewById(R.id.mainTransitTypesView)

            init {
                view.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        onItemClick(routes[position])
                    }
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.route_recycle, parent, false)
            return RouteViewHolder(view)
        }

        override fun onBindViewHolder(holder: RouteViewHolder, position: Int) {
            val route = routes[position]
            holder.transitCountView.text = "${route.transitCount} 회"
            holder.totalTimeView.text = "${route.totalTime} 분"
            holder.detailedPathView.text = route.detailedPath
            holder.mainTransitTypesView.text = route.mainTransitTypes
        }

        override fun getItemCount(): Int = routes.size
    }
}


