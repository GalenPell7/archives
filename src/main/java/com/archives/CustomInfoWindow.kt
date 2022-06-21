package com.archives

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter
import com.google.android.gms.maps.model.Marker
import kotlinx.coroutines.*

/**
 * Class creates a custom info window and assigns each window according to its passed values.
 * Class needs a context to inflate the template.
 */
class CustomInfoWindow(private var context: Context) : InfoWindowAdapter {
    var inflater: LayoutInflater? = null
    private lateinit var title : TextView
    private lateinit var description : TextView
    private lateinit var displayedImage : ImageView


    override fun getInfoContents(marker: Marker): View? {
        return null
    }

    /**
     * Assigns title and snippet of the Info box according to what the passed marker is.
     * @return a custom info window with desired title and snippet
     */
    @OptIn(DelicateCoroutinesApi::class)
    @SuppressLint("InflateParams")
    override fun getInfoWindow(marker: Marker): View {
        var v : View? = null
        inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        v = inflater!!.inflate(R.layout.custom_window_template, null)
        title = v.findViewById<View>(R.id.title_info_window) as TextView

        displayedImage = v.findViewById<View>(R.id.image) as ImageView
        description = v.findViewById<View>(R.id.external_links) as TextView

        val title = marker.title.toString()
        val snippet = marker.snippet.toString()

        setUI(title, snippet)

        return v

    }

    /**
     * Set image for CustomInfoBox, assign title and description
     */
    private fun setUI(titleString : String, marker : String) {
            when (titleString) {
                "George Washington" -> displayedImage.setImageResource(R.drawable.sydney)
                "Ivar The Boneless" -> displayedImage.setImageResource(R.drawable.ivar_the_boneless)
                "Pyramid of Giza" -> displayedImage.setImageResource(R.drawable.pyramid_giza)
                "Battle of Thermopylae" -> displayedImage.setImageResource(R.drawable.leonidas_monument)
                "Battle of Hattin" -> displayedImage.setImageResource(R.drawable.hattin)
                "Battle of Verdun" -> displayedImage.setImageResource(R.drawable.verdun)
                "1909 Denmark Expedition" -> displayedImage.setImageResource(R.drawable.denmark_exp)
                "The Telephone" -> displayedImage.setImageResource(R.drawable.telephone_old)
                "The 1936 Olympics" -> displayedImage.setImageResource(R.drawable.olympics)
                "Marcus Aurelius" -> displayedImage.setImageResource(R.drawable.marcus)
                "Lucius Cornelius Sulla" -> displayedImage.setImageResource(R.drawable.sulla)
                "James Cleveland Owens" -> displayedImage.setImageResource(R.drawable.jesse_owens)
                "Independence Hall" -> displayedImage.setImageResource(R.drawable.independence_hall)
                "Rosa Parks" -> displayedImage.setImageResource(R.drawable.rosa_parks)
                "Miracle On Ice" -> displayedImage.setImageResource(R.drawable.miracle_on_ice)
                "Hannibal" -> displayedImage.setImageResource(R.drawable.hannibal)
                "Gias Julius Ceasar" -> displayedImage.setImageResource(R.drawable.julius)
                "Knights Templar" -> displayedImage.setImageResource(R.drawable.knights_templar)
                "Centralia Mine Fire" -> displayedImage.setImageResource(R.drawable.centralia)
            }

            title.text = titleString
            description.text = marker
    }
}