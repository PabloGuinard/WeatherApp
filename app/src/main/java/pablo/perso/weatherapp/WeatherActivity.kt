package pablo.perso.weatherapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.JsonReader
import android.util.Log
import android.widget.TextView
import org.json.JSONObject
import java.net.URL
import java.io.InputStreamReader
import java.util.*
import javax.net.ssl.HttpsURLConnection

private const val APP_ID = "e3e837e5209e992789a2b21059048d7d"

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
                updateUITodayWeather(response)
                inputStreamReader.close()
                inputSystem.close()
            } else {
                Log.d("error : ", "error")
            }
        }
    }

    private fun updateUITodayWeather(response: JSONObject) {
        runOnUiThread {
            kotlin.run {
                val cityTv = findViewById<TextView>(R.id.tv_city_name)
                val currentTempTv = findViewById<TextView>(R.id.tv_current_temp)
                val weatherDescTv = findViewById<TextView>(R.id.tv_weather_desc)
                val minMaxTempTv = findViewById<TextView>(R.id.tv_min_max_temp)

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