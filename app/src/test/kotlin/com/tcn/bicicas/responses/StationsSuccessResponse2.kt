package com.tcn.bicicas.responses

const val STATIONS_SUCCESS_RESPONSE_2 = """{
  "type": "FeatureCollection",
  "features": [
    {
      "type": "Feature",
      "geometry": {
        "type": "Point",
        "coordinates": [
          -0.06999,
          39.995338
        ]
      },
      "properties": {
        "name": "01. UJI - FCHS",
        "bikes_total": 0,
        "bikes_available": 0,
        "anchors": [
          {
            "number": 1,
            "bicycle": 1234,
            "incidents": [],
            "is_electric": false
          },
          {
            "number": 2,
            "bicycle": null,
            "incidents": [],
            "is_electric": false
          }
        ],
        "last_seen": "2021-12-30 19:12:34",
        "online": true,
        "number_loans": "5",
        "incidents": 1
      }
    }
  ]
}"""