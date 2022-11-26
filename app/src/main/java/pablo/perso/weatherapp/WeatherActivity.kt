package pablo.perso.weatherapp

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.io.InputStreamReader
import java.net.URL
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*
import javax.net.ssl.HttpsURLConnection


private const val APP_ID = "e3e837e5209e992789a2b21059048d7d"
private val ICON_URL = arrayOf("https://openweathermap.org/img/wn/", "@2x.png")

class WeatherActivity : AppCompatActivity() {

    private val city = "Lyon"
    private val lang = "fr"
    private val unit = "metric"

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather)

        fetchWeather().start()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun fetchWeather(): Thread {
        return Thread {
            val url = URL(
                "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&units=" + unit + "&cnt=7&appid=" + APP_ID + "&lang=" + lang
            )
            val connection = url.openConnection() as HttpsURLConnection
            val urlForecast = URL(
                "https://api.openweathermap.org/data/2.5/forecast/daily?q=" + city + "&units=" + unit + "&cnt=7&appid=" + APP_ID + "&lang=" + lang
            )
            val connectionForecast = urlForecast.openConnection() as HttpsURLConnection


            if (connection.responseCode == 200 && connectionForecast.responseCode == 200) {
                val inputSystem = connection.inputStream
                val inputStreamReader = InputStreamReader(inputSystem, "UTF-8")
                val response = JSONObject(inputStreamReader.readText())

                val inputSystemForecast = connectionForecast.inputStream
                val inputStreamReaderForecast = InputStreamReader(inputSystemForecast, "UTF-8")
                val responseForecast = JSONObject(inputStreamReaderForecast.readText())

                val urlIcon = URL(ICON_URL.get(0) + (response.getJSONArray("weather")[0] as JSONObject).getString("icon") + ICON_URL.get(1))

                val connectionIcon = urlIcon.openConnection() as HttpsURLConnection
                if(connection.responseCode == 200){
                    val inputStreamIcon = connectionIcon.inputStream
                    val bmp = BitmapFactory.decodeStream(inputStreamIcon)

                    updateUITodayWeather(response, responseForecast, bmp)

                    inputStreamIcon.close()

                } else {
                    //TODO
                }

                inputStreamReader.close()
                inputSystem.close()

                inputStreamReaderForecast.close()
                inputSystemForecast.close()
            } else {
                Log.d("error : ", "error")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    private fun updateUITodayWeather(current: JSONObject, forecast: JSONObject, bmp :Bitmap) {
        runOnUiThread {
            kotlin.run {
                val cityTv = findViewById<TextView>(R.id.tv_city_name)
                val currentTempTv = findViewById<TextView>(R.id.tv_current_temp)
                val weatherDescTv = findViewById<TextView>(R.id.tv_weather_desc)
                val minMaxTempTv = findViewById<TextView>(R.id.tv_min_max_temp)
                val feelsLikeTv = findViewById<TextView>(R.id.tv_feels_like)
                val timeTv = findViewById<TextView>(R.id.tv_time)

                val weatherIcon = findViewById<ImageView>(R.id.weather_icon)
                weatherIcon.setImageBitmap(bmp)

                val weather = current.getJSONArray("weather")[0] as JSONObject
                val main = current.getJSONObject("main")

                cityTv.text = current.getString("name")
                currentTempTv.text = main.getString("temp").dropLast(3) + "°C"
                weatherDescTv.text = weather.getString("description")
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                minMaxTempTv.text =
                    "Max. " + main.getString("temp_max").dropLast(3) + "°C Min. " + main.getString("temp_min").dropLast(3) + "°C"
                feelsLikeTv.text = getString(R.string.feels_like) + " " + main.getString("feels_like").dropLast(3) + "°C"

                Log.e( "updateUITodayWeather: ", current.getInt("dt").toString())
                val simpleDateFormat = SimpleDateFormat("dd MMM HH:mm")



                // val date = Date(current.getLong("dt") * 1000, simpleDateFormat)
                timeTv.text = simpleDateFormat.format(current.getInt("dt") * 1000L + 3600000)

            }
        }

    }
}