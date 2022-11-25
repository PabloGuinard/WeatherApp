package pablo.perso.weatherapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import javax.net.ssl.HttpsURLConnection


private const val APP_ID = "e3e837e5209e992789a2b21059048d7d"
private val ICON_URL = arrayOf("https://openweathermap.org/img/wn/", "@2x.png")

class WeatherActivity : AppCompatActivity() {

    private val city = "Lyon"
    private val lang = "fr"
    private val unit = "metric"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather)

        fetchWeather().start()
    }

    private fun fetchWeather(): Thread {
        return Thread {
            val url = URL(
                "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&units=" + unit + "&cnt=7&appid=" + APP_ID + "&lang=" + lang
            )
            val connection = url.openConnection() as HttpsURLConnection


            if (connection.responseCode == 200) {
                val inputSystem = connection.inputStream
                val inputStreamReader = InputStreamReader(inputSystem, "UTF-8")
                val response = JSONObject(inputStreamReader.readText())

                val urlIcon = URL(ICON_URL.get(0) + (response.getJSONArray("weather")[0] as JSONObject).getString("icon") + ICON_URL.get(1))

                val connectionIcon = urlIcon.openConnection() as HttpsURLConnection
                if(connection.responseCode == 200){
                    val inputStreamIcon = connectionIcon.inputStream
                    val bmp = BitmapFactory.decodeStream(inputStreamIcon)

                    updateUITodayWeather(response, bmp)
                } else {
                    //TODO
                }


                inputStreamReader.close()
                inputSystem.close()
            } else {
                Log.d("error : ", "error")
            }
        }
    }

    private fun updateUITodayWeather(response: JSONObject, bmp :Bitmap) {
        runOnUiThread {
            kotlin.run {
                val cityTv = findViewById<TextView>(R.id.tv_city_name)
                val currentTempTv = findViewById<TextView>(R.id.tv_current_temp)
                val weatherDescTv = findViewById<TextView>(R.id.tv_weather_desc)
                val minMaxTempTv = findViewById<TextView>(R.id.tv_min_max_temp)

                val weatherIcon = findViewById<ImageView>(R.id.weather_icon)
                weatherIcon.setImageBitmap(bmp)

                val weather = response.getJSONArray("weather")[0] as JSONObject
                val main = response.getJSONObject("main")

                cityTv.text = response.getString("name")
                currentTempTv.text = main.getString("temp")
                weatherDescTv.text = weather.getString("description")
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } + "°C"
                minMaxTempTv.text =
                    "Min. " + main.getString("temp_min") + "°C Max. " + main.getString("temp_max") + "°C"

            }
        }

    }
}