import kr.io.seemore.transportation.data.DB.DBSaveDTO
import kr.io.seemore.transportation.data.DB.GetRouteResponseDTO
import kr.io.seemore.transportation.data.DB.SavedRouteResponseDTO
import kr.io.seemore.transportation.data.searchPath.Route
import kr.io.seemore.transportation.data.searchPath.RouteRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface RouteService {
    @POST("api/route")
    suspend fun getRoute(@Body request: RouteRequest): List<Route>

    @POST("api/DBSaveRoute")
    suspend fun saveDBRoute(@Body request: DBSaveDTO): SavedRouteResponseDTO

    @POST("api/updateFavorite")
    suspend fun updateFavorite(@Body request: DBSaveDTO): SavedRouteResponseDTO

    @GET("api/DBGetRoute")
    suspend fun getSavedRoute(@Query("loginId") loginId: String): List<GetRouteResponseDTO>

//    @POST("api/DBDeleteRoute")
//    suspend fun deleteDBRoute(@Body request: RouteRequest): List<Route>

}
