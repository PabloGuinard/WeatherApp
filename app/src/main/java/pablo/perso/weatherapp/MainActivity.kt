package pablo.perso.weatherapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.text.InputType
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.SearchView
import android.widget.SearchView.OnQueryTextListener
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


const val INTENT_CITY = "city"
val RECENT_SEARCHES = stringPreferencesKey("recent_searches")

class MainActivity : AppCompatActivity() {

    val lvContent = ArrayList<String>()

    private val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
        name = "searches"
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StrictMode.setVmPolicy(
            VmPolicy.Builder(StrictMode.getVmPolicy())
                .detectLeakedClosableObjects()
                .build()
        )
        setContentView(R.layout.activity_main)

        val searchView = findViewById<SearchView>(R.id.searchView)
        val listView = findViewById<ListView>(R.id.listView)
        runBlocking {
                getSearches()
        }
        val arrayAdapter: ArrayAdapter<String> = ArrayAdapter(
            this, android.R.layout.simple_list_item_1, lvContent
        )
        listView.adapter = arrayAdapter

        searchView.isSubmitButtonEnabled = true
        searchView.inputType = InputType.TYPE_TEXT_FLAG_CAP_SENTENCES


        searchView.setOnQueryTextListener(object : OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    lvContent.add(0, query)
                    runBlocking {
                        launch { saveSearchesToPreferencesStore(lvContent) }
                    }
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }

        })

        listView.setOnItemClickListener { _, _, pos, _ ->
            val intent = Intent(this, WeatherActivity::class.java)
            intent.putExtra(INTENT_CITY, lvContent[pos])
            startActivity(intent)
        }

        listView.setOnItemLongClickListener { _, _, pos, _ ->
            runOnUiThread{
                lvContent.removeAt(pos)
                arrayAdapter.notifyDataSetChanged()
                runBlocking { launch { saveSearchesToPreferencesStore(lvContent) } }
            }
            true
        }

    }

    private suspend fun saveSearchesToPreferencesStore(searches: ArrayList<String>) {
        userPreferencesDataStore.edit { preferences ->
            preferences[RECENT_SEARCHES] = searches.joinToString(separator = "%")
        }
    }

    private fun getSearchesFromPreferencesStore(): Flow<String> = userPreferencesDataStore.data
        .map { preferences ->
            preferences[RECENT_SEARCHES] ?: ""
        }
    private fun getSearches() {
        var searches = ""
        runBlocking {
            launch {
                getSearchesFromPreferencesStore().collect { value ->
                    searches += value
                    lvContent.clear()
                    lvContent.addAll(searches.split("%"))
                    this.cancel()
                }
            }
        }
    }
}