package com.tcn.bicicas.responses

const val STATIONS_SUCCESS_RESPONSE = """{
  "features": [
    {
      "geometry": {
        "coordinates": [
          -0.06999,
          39.995338
        ]
      },
      "properties": {
        "name": "01. Station 1",
        "bikes_available": 0,
        "anchors": [
          {
            "number": 1,
            "bicycle": null,
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
        "online": true
      }
    },
    {
      "geometry": {
        "coordinates": [
          -0.06999,
          39.995338
        ]
      },
      "properties": {
        "name": "02. Station 2",
        "bikes_available": 1,
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
        "online": true
      }
    },
    {
      "geometry": {
        "coordinates": [
          -0.06999,
          39.995338
        ]
      },
      "properties": {
        "name": "03. Station 3",
        "bikes_available": 2,
        "anchors": [
          {
            "number": 1,
            "bicycle": 1234,
            "incidents": [],
            "is_electric": false
          },
          {
            "number": 2,
            "bicycle": 5678,
            "incidents": [],
            "is_electric": false
          }
        ],
        "last_seen": "2021-12-30 19:12:34",
        "online": true
      }
    }
  ]
}"""