package com.tcn.bicicas.data.datasource.remote.serialization

import com.tcn.bicicas.data.model.Station
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Converter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StationConverter : Converter<ResponseBody, Array<Station>> {

    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale("es", "ES"))

    override fun convert(value: ResponseBody): Array<Station> {
        val featuresArray = JSONObject(value.string()).getJSONArray("features")
        return Array(featuresArray.length()) {
            parseStation(featuresArray.getJSONObject(it))
        }
    }

    private fun parseStation(json: JSONObject): Station {
        val coordinatesJson = json.getJSONObject("geometry").getJSONArray("coordinates")
        val propertiesJson = json.getJSONObject("properties")
        val anchorsJsonArray = propertiesJson.getJSONArray("anchors")
        val (id, name) = propertiesJson.getString("name").split(".", limit = 2)
        val anchors =
            List(anchorsJsonArray.length()) { parseAnchor(anchorsJsonArray.getJSONObject(it)) }
        return Station(
            id = id.trim(),
            name = name.uppercase().trim(),
            bikesAvailable = propertiesJson.getInt("bikes_available"),
            anchors = anchors,
            electricBikesAvailable = anchors.count { !it.hasIncident && it.isElectric && it.bicycleId != null },
            incidents = anchors.count { it.hasIncident },
            online = propertiesJson.getBoolean("online"),
            lastSeen = dateFormatter.parse(propertiesJson.getString("last_seen")) ?: Date(),
            latitude = coordinatesJson.getDouble(1),
            longitude = coordinatesJson.getDouble(0),
            favorite = false
        )
    }

    private fun parseAnchor(json: JSONObject): Station.Anchor {
        return Station.Anchor(
            number = json.getInt("number"),
            bicycleId = json.optInteger("bicycle"),
            isElectric = json.optBoolean("is_electric"),
            hasIncident = json.optJSONArray("incidents")?.let { it.length() > 0 } ?: false
        )
    }

    private fun JSONObject.optInteger(name: String): Int? {
        return if (isNull(name) || !has(name)) null else optInt(name)
    }

}