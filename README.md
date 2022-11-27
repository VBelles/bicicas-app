# Unofficial Bicicas App

This project is the continuation of Unofficial Bicicas app from 2017. It contains the source code of
the [playstore application](https://play.google.com/store/apps/details?id=com.tcn.bicicas) and it
will help to track bugs and new features to implement.

Unofficial Bicicas is a nonprofit android application, that allows users to check Bicicas benches
availability and to unlock bicycles with the generated pin.

Note that some aspects of the published application, such as api endpoints or pin generation library
are not available on this repository. That decision is made with the only aim of keeping things "
secure" and to avoid legal issues (please, if someone wants to ~scare~ contact me because is not
happy about this nonprofit project, that only aims to help people and does not involve any security
risk, please, contact via email).

<p align="center">
<img src="https://github.com/VBelles/bicicas-app/blob/master/media/pin.png" width="260"/> <img src="https://github.com/VBelles/bicicas-app/blob/master/media/list.png" width="260"/> <img src="https://github.com/VBelles/bicicas-app/blob/master/media/map.png" width="260"/> 
</p>

## Prerequisites

* Latest Android Studio Canary version
* A local.properties file in the root of the project including these properties:

``` properties
MAPS_API_KEY=my_maps_api_key
OAUTH_CLIENT_ID=my_auth_endpoint_client_id
OAUTH_CLIENT_SECRET=my_auth_client_secret
OAUTH_ENDPOINT=https://auth.endpoint.me
STATUS_ENDPOINT=https://status.endpoint.me
ENCRYPT_PASSWORD=my_encryption_pass
```

## Whats next

* More testing
* Bug fixing
* New features

## Contributions

Any issue, idea or pull request will be very welcomed.

## License

Unofficial Bicicas App is licensed under GNU General Public License v3.0,
check [LICENSE](https://github.com/VBelles/bicicas-app/blob/master/LICENSE) for further information.
