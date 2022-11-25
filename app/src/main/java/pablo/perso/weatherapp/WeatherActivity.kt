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
                Log.e("response : ", response.toString())
                inputStreamReader.close()
                inputSystem.close()
            } else {
                Log.d("error : ", "error")
            }
        }
    }

}