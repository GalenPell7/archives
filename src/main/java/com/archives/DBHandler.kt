package com.archives

import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * Contains a companion object that creates a connection with the desired Connection URL.
 * The URL will echo the JSON Object and the class will parse out the markers and place them in a
 * hashmap.
 */
class DBHandler {
    companion object {
        fun getStories(): HashMap<String,MarkerInfo> {
            val markers = HashMap<String,MarkerInfo>()
            val url = "http://box2020.temp.domains/~heliesia/"

                try {
                    val connection = URL(url)
                    val urlConnection = connection.openConnection() as HttpURLConnection

                    urlConnection.requestMethod = "POST"
                    urlConnection.doOutput = true
                    urlConnection.setRequestProperty("Accept-Charset", "UTF-8")
                    
                    val w = DataOutputStream(urlConnection.outputStream)
                    w.flush()
                    w.close()

                    val inp = BufferedInputStream(urlConnection.inputStream)
                    val reader = inp.bufferedReader().use(BufferedReader::readText)
                    println(reader)

                    val json = JSONObject(reader)
                    // Parse JSONObject
                    for (i in 1 until json.length() + 1) {
                        val jsonID = JSONObject(json.getString("$i"))
                        val markerInfo = MarkerInfo()
                        markerInfo.storyID = jsonID.getString("Id")
                        markerInfo.title = jsonID.getString("Title")
                        markerInfo.abstract = jsonID.getString("Description")
                        markerInfo.externalResource = jsonID.getString("Resource")
                        markerInfo.longitude = jsonID.getDouble("Longitude")
                        markerInfo.latitude = jsonID.getDouble("Latitude")
                        markerInfo.category = jsonID.getString("Period")
                        markerInfo.icon = jsonID.getString("Icon")
                        markers.put(markerInfo.title.toString(),markerInfo)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            return markers
        }

    }

}
