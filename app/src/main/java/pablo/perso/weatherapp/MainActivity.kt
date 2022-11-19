package pablo.perso.weatherapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.SearchView
import android.widget.SearchView.OnQueryTextListener
import com.android.car.ui.toolbar.Toolbar.OnSearchListener

class MainActivity : AppCompatActivity() {

    public final var INTENT_CITY = "city"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val searchView = findViewById<SearchView>(R.id.searchView)
        val listView = findViewById<ListView>(R.id.listView)
        val lvContent = ArrayList<String>()
        lvContent.add("Paris")
        lvContent.add("Lyon")
        lvContent.add("Marseille")
        val arrayAdapter: ArrayAdapter<String> = ArrayAdapter(
            this, android.R.layout.simple_list_item_1, lvContent
        )
        listView.adapter = arrayAdapter

        searchView.setSubmitButtonEnabled(true)


        searchView.setOnQueryTextListener(object : OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    lvContent.add(0, query)
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }

        })

        listView.setOnItemClickListener { adapterView, view, i, l ->
            val intent = Intent(this, WeatherActivity::class.java)
            intent.putExtra(INTENT_CITY, lvContent.get(i))
            startActivity(intent)
        }
    }
}