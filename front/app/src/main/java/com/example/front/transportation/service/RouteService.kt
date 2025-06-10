import com.example.front.transportation.data.DB.DBSave
import com.example.front.transportation.data.searchPath.Route
import com.example.front.transportation.data.searchPath.RouteRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface RouteService {
    @POST("api/route")
    suspend fun getRoute(@Body request: RouteRequest): List<Route>

    @POST("api/DBSaveRoute")
    suspend fun saveDBRoute(@Body request: DBSave): Boolean

    @POST("api/DBDeleteRoute")
    suspend fun deleteDBRoute(@Body request: RouteRequest): List<Route>
}
