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
import kotlin.collections.ArrayList
import kotlin.math.roundToInt


private const val APP_ID = "e3e837e5209e992789a2b21059048d7d"
private val ICON_URL = arrayOf("https://openweathermap.org/img/wn/", "@2x.png")

class WeatherActivity : AppCompatActivity() {

    private val city = "londres"
    private val lang = "fr"
    private val unit = "metric"

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather)

        fetchWeather().start()
        fetchForecast().start()
    }

    @RequiresApi(Build.VERSION_CODES.O)
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

                val urlIcon = URL(
                    ICON_URL.get(0) + (response.getJSONArray("weather")[0] as JSONObject).getString(
                        "icon"
                    ) + ICON_URL.get(1)
                )

                val connectionIcon = urlIcon.openConnection() as HttpsURLConnection
                if (connection.responseCode == 200) {
                    val inputStreamIcon = connectionIcon.inputStream
                    val bmp = BitmapFactory.decodeStream(inputStreamIcon)

                    updateUITodayWeather(response, bmp)

                    inputStreamIcon.close()

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

    private fun fetchForecast(): Thread {
        return Thread {
            val url = URL(
                "https://api.openweathermap.org/data/2.5/forecast/daily?q=" + city + "&units=" + unit + "&cnt=8&appid=" + APP_ID + "&lang=" + lang
            )
            val connection = url.openConnection() as HttpsURLConnection
            if (connection.responseCode == 200) {
                val inputSystem = connection.inputStream
                val inputStreamReader = InputStreamReader(inputSystem, "UTF-8")
                val response = JSONObject(inputStreamReader.readText())

                val bmpArray = ArrayList<Bitmap?>()
                for (i in 1..7) {
                    val urlIcon = URL(
                        ICON_URL[0] + ((response.getJSONArray("list")[i] as JSONObject).getJSONArray(
                            "weather"
                        )[0] as JSONObject).getString(
                            "icon"
                        ) + ICON_URL[1]
                    )

                    val connectionIcon = urlIcon.openConnection() as HttpsURLConnection
                    if (connection.responseCode == 200) {
                        val inputStreamIcon = connectionIcon.inputStream
                        bmpArray.add(BitmapFactory.decodeStream(inputStreamIcon))
                        inputStreamIcon.close()
                    } else bmpArray.add(null)
                }
                updateUIForecast(response, bmpArray)
                inputStreamReader.close()
                inputSystem.close()
            } else {
                Log.d("error : ", "error")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    private fun updateUITodayWeather(current: JSONObject, bmp: Bitmap) {
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
                currentTempTv.text = main.getDouble("temp").roundToInt().toString() + "°C"
                weatherDescTv.text = weather.getString("description")
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                minMaxTempTv.text = "Max. " + main.getDouble("temp_max").roundToInt()
                    .toString() + "°C Min. " + main.getDouble("temp_min").roundToInt()
                    .toString() + "°C"
                feelsLikeTv.text =
                    getString(R.string.feels_like) + " " + main.getDouble("feels_like").roundToInt()
                        .toString() + "°C"

                val simpleDateFormat = SimpleDateFormat("dd MMM HH:mm")
                timeTv.text = simpleDateFormat.format(current.getInt("dt") * 1000L + 3600000)


            }
        }

    }

    @SuppressLint("SetTextI18n")
    private fun updateUIForecast(forecast: JSONObject, bmpArray: ArrayList<Bitmap?>) {
        runOnUiThread {
            kotlin.run {
                val timeTvArray = getForecastTimeTv()
                val minMaxTvArray = getForecastMinMaxTv()
                val descTvArray = getForecastDescTv()
                val iconsArray = getForecastIcons()

                val list = forecast.getJSONArray("list")
                list.remove(0)

                for (i in 0..6) {
                    val simpleDateFormat = SimpleDateFormat("EE dd/MM")
                    timeTvArray[i].text =
                        simpleDateFormat.format((list[i] as JSONObject).getInt("dt") * 1000L + 3600000)
                            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                    minMaxTvArray[i].text =
                        "Max. " + (list[i] as JSONObject).getJSONObject("temp").getDouble("max")
                            .roundToInt()
                            .toString() + "°C Min. " + (list[i] as JSONObject).getJSONObject("temp")
                            .getDouble("min").roundToInt().toString() + "°C"

                    descTvArray[i].text =
                        ((list[i] as JSONObject).getJSONArray("weather")[0] as JSONObject).getString(
                            "description"
                        )
                    iconsArray[i].setImageBitmap(bmpArray[i])
                }

            }
        }
    }

    private fun getForecastTimeTv(): kotlin.collections.ArrayList<TextView> {
        val timeTvArray = kotlin.collections.ArrayList<TextView>()
        timeTvArray.add(findViewById(R.id.tv_date_0))
        timeTvArray.add(findViewById(R.id.tv_date_1))
        timeTvArray.add(findViewById(R.id.tv_date_2))
        timeTvArray.add(findViewById(R.id.tv_date_3))
        timeTvArray.add(findViewById(R.id.tv_date_4))
        timeTvArray.add(findViewById(R.id.tv_date_5))
        timeTvArray.add(findViewById(R.id.tv_date_6))
        return timeTvArray
    }

    private fun getForecastMinMaxTv(): kotlin.collections.ArrayList<TextView> {
        val minMaxTvArray = kotlin.collections.ArrayList<TextView>()
        minMaxTvArray.add(findViewById(R.id.tv_min_max_0))
        minMaxTvArray.add(findViewById(R.id.tv_min_max_1))
        minMaxTvArray.add(findViewById(R.id.tv_min_max_2))
        minMaxTvArray.add(findViewById(R.id.tv_min_max_3))
        minMaxTvArray.add(findViewById(R.id.tv_min_max_4))
        minMaxTvArray.add(findViewById(R.id.tv_min_max_5))
        minMaxTvArray.add(findViewById(R.id.tv_min_max_6))
        return minMaxTvArray
    }

    private fun getForecastDescTv(): kotlin.collections.ArrayList<TextView> {
        val descTvArray = kotlin.collections.ArrayList<TextView>()
        descTvArray.add(findViewById(R.id.desc_0))
        descTvArray.add(findViewById(R.id.desc_1))
        descTvArray.add(findViewById(R.id.desc_2))
        descTvArray.add(findViewById(R.id.desc_3))
        descTvArray.add(findViewById(R.id.desc_4))
        descTvArray.add(findViewById(R.id.desc_5))
        descTvArray.add(findViewById(R.id.desc_6))
        return descTvArray
    }

    private fun getForecastIcons(): kotlin.collections.ArrayList<ImageView> {
        val iconArray = kotlin.collections.ArrayList<ImageView>()
        iconArray.add(findViewById(R.id.icon_0))
        iconArray.add(findViewById(R.id.icon_1))
        iconArray.add(findViewById(R.id.icon_2))
        iconArray.add(findViewById(R.id.icon_3))
        iconArray.add(findViewById(R.id.icon_4))
        iconArray.add(findViewById(R.id.icon_5))
        iconArray.add(findViewById(R.id.icon_6))
        return iconArray
    }
}