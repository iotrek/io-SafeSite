package com.iosite.io_safesite.Util;

public class Constants {

    public static final String PREFERENCE_NAME = "io_safesite_prefrence";

    public static final String iositeBeaconID = "1234abcd";

    /* Base Url*/
    public static final String BASE_URL = "http://dev-covid.iosite.com/api/v1/";

    // urls
    public static final String REQUEST_GENERATE_OTP_URL = BASE_URL + "otp/generate";
    public static final String REQUEST_VALIDATE_OTP_URL = BASE_URL + "otp/validate";
    public static final String REQUEST_UPDATE_PROFILE_URL = BASE_URL + "users";
    public static final String REQUEST_GET_PROFILE_URL = BASE_URL + "user/profile";
    public static final String REQUEST_FCM_TOKEN_URL = BASE_URL + "fcm/token";
    public static final String REQUEST_PROXIMITY_BULK = BASE_URL + "proximity/bulk";
    public static final String REQUEST_EXPOSURE_BULK = BASE_URL + "exposure/bulk";


    // unique request id define here
    public static final int REQUEST_GENERATE_OTP = 1;
    public static final int REQUEST_VALIDATE_OTP = 2;
    public static final int REQUEST_UPDATE_PROFILE = 3;
    public static final int REQUEST_GET_PROFILE = 4;
    public static final int IMEI_PERMISSION_CODE = 1000;
    public static final int PERMISSION_CODE = 10001;
    public static final int REQUEST_FCM_TOKEN = 10;
    public static final int LOCATION_PERMISSION_CODE = 13;


    // request params
    public static final String PARAM_MOBILE = "mobile";
    public static final String PARAM_OTP = "otp";

    //for profile update
    public static final String PARAM_CONTACT_NUMBER = "contact_number";
    public static final String PARAM_FIRST_NAME = "first_name";
    public static final String PARAM_LAST_NAME = "last_name";
    public static final String PARAM_USER_GENDER = "gender";
    public static final String PARAM_USER_AGE = "age_group";
    public static final String PARAM_COVID19_STATUS = "covid19_status";
    public static final String PARAM_QUARANTINE_DATE = "quarantine_date";
    public static final String PARAM_ADDRESS = "address";  // array param
    public static final String PARAM_ADDRESS_ONE = "address1";
    public static final String PARAM_ADDRESS_TWO = "address2";
    public static final String PARAM_ADDRESS_CITY = "city";
    public static final String PARAM_ADDRESS_STATE = "state";
    public static final String PARAM_ADDRESS_COUNTRY = "country";
    public static final String PARAM_ADDRESS_PINCODE = "pincode";
    public static final String PARAM_TRAVEL_HISTORY = "travel_history";  // array param
    public static final String PARAM_TRAVEL_HISTORY_CITY = "city";
    public static final String PARAM_TRAVEL_HISTORY_STATE = "state";
    public static final String PARAM_TRAVEL_HISTORY_COUNTRY = "country";
    public static final String PARAM_TRAVEL_HISTORY_DATE = "date";
    public static final String PARAM_QUARANTINE_SINCE = "quarantine_since";

    public static final String PARAM_IMEI = "IMEI";
    public static final String PARAM_TIMESTAMP = "timestamp";
    public static final String PARAM_FCM_TOKEN = "fcm_token";
    public static final String PARAM_FCM_ID = "fcm_id";
    public static final String PARAM_LATITUDE = "latitude";
    public static final String PARAM_LONGITUDE = "longitude";

    public static final String PARAM_BATTERY = "battery";
    public static final String PARAM_ONBODY = "onbody";
    public static final String PARAM_EVENT_UPLINK = "event";
    public static final String PARAM_GPS_STATUS = "gps_status";


    public static final String PARAM_IS_CHARGING = "isCharging";
    public static final String PARAM_TYPE = "type";
    public static final String PARAM_LAT = "lat";
    public static final String PARAM_LONG = "long";
    public static final String PARAM_MSG_TYPE = "type";

    // exposure params
    public static final String PARAM_ID = "unique_id";
    public static final String PARAM_BEACON_ID = "proximity_user";
    public static final String PARAM_EXPOSURE_MEAN_DISTANCE = "mean_distance";
    public static final String PARAM_EXPOSURE_MEDIAN_DISTANCE = "median_distance";
    public static final String PARAM_EXPOSURE_MIN_DISTANCE = "min_distance";
    public static final String PARAM_EXPOSURE_MAX_DISTANCE = "max_distance";
    public static final String PARAM_EXPOSURE_MEAN_RSSI = "mean_rssi";
    public static final String PARAM_EXPOSURE_MEAN_TX_POWER = "mean_tx_power";
    public static final String PARAM_EXPOSURE_START = "exposure_start";
    public static final String PARAM_EXPOSURE_END = "exposure_end";
    public static final String PARAM_DATA_SETS = "datasets";

    public static final String PARAM_PROXIMITY_ARRAY_KEY = "proximity_data";
    public static final String PARAM_EXPOSURE_ARRAY_KEY = "exposure_data";




                // proximity params params
//    public static final String PARAM_ID = "id";
//    public static final String PARAM_BEACON_ID = "proximity_user";
    public static final String PARAM_PROXIMITY_RSSI = "rssi";
    public static final String PARAM_PROXIMITY_TX_POWER = "tx_power";
    public static final String PARAM_PROXIMITY_DISTANCE = "distance";
    public static final String PARAM_PROXIMITY_TIMESTAMP = "timestamp";


    public static String PREF_BATTERY_PERCENTAGE = "batteryPercent";
    public static String PREF_ON_OFF_BODY_VALUE = "offOnBodyValue";
    public static String PREF_IS_CHARGING = "is_charging";
    public static String PREF_IS_CHECKED_IN = "is_checked_in";
    public static String PREF_FCM_ID = "fcm_id";
    public static String PREF_FCM_TOKEN = "fcm_token";
    public static String PREF_MSG_TYPE = "msg_type";
    public static String PREF_EVENT_UPLINK = "event_uplink";

    // pref for profile
    public static final String PREF_CONTACT_NUMBER = "contact_number";
    public static final String PREF_FIRST_NAME = "first_name";
    public static final String PREF_LAST_NAME = "last_name";
    public static final String PREF_USER_AGE = "user_age";
    public static final String PREF_USER_GENDER = "gender";
    public static final String PREF_COVID19_STATUS = "covid19_status";
    public static final String PREF_ADDRESS = "address";  // array param
    public static final String PREF_ADDRESS_ONE = "address1";
    public static final String PREF_ADDRESS_TWO = "address2";
    public static final String PREF_ADDRESS_CITY = "city";
    public static final String PREF_ADDRESS_STATE = "state";
    public static final String PREF_ADDRESS_COUNTRY = "country";
    public static final String PREF_ADDRESS_PINCODE = "pincode";
    public static final String PREF_TRAVEL_HISTORY = "travel_history";  // array param
    public static final String PREF_TRAVEL_HISTORY_CITY = "city";
    public static final String PREF_TRAVEL_HISTORY_STATE = "state";
    public static final String PREF_TRAVEL_HISTORY_COUNTRY = "country";
    public static final String PREF_TRAVEL_HISTORY_DATE = "date";
    public static final String PREF_QUARANTINE_STATUS = "quarantine_status";
    public static final String PREF_QUARANTINE_SINCE = "quarantine_since";

    public static final String PREF_IS_FIRST_LOGIN = "is_first_login";
    public static final String PREF_EXPOSURE_TIME_DIFF = "exposure_time_diff";   // in minutes



    public static String PREF_USER_ID = "user_id";
    public static String PREF_USER_ACCESS_TOKEN = "access_token";
    public static String PREF_USER_BEACON_ID = "user_beacon_id";

}
