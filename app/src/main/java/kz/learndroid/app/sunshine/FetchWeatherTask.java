package kz.learndroid.app.sunshine;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.text.format.Time;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;

import kz.learndroid.app.sunshine.data.WeatherContract;
import kz.learndroid.app.sunshine.data.WeatherDbHelper;

/**
 * Created by aibol on 3/17/16.
 */
public class FetchWeatherTask extends AsyncTask<String, Void, Void> {

    Context mContext;

    public FetchWeatherTask(Context context) {
        mContext = context;
    }

    long addLocation(String locationSetting, String cityName, double lat, double lon) {
        long locationId;

        // First, check if the location with this city name exists in the db
//        Cursor locationCursor = mContext.getContentResolver().query(
//                WeatherContract.LocationEntry.CONTENT_URI,
//                new String[]{WeatherContract.LocationEntry._ID},
//                WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ?",
//                new String[]{locationSetting},
//                null);

        WeatherDbHelper dbHelper = new WeatherDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor locationCursor = db.query(
                WeatherContract.LocationEntry.TABLE_NAME,
                new String[]{WeatherContract.LocationEntry._ID},
                WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ?",
                new String[]{locationSetting},
                null,
                null,
                null
        );

        if (locationCursor.moveToFirst()) {
            int locationIdIndex = locationCursor.getColumnIndex(WeatherContract.LocationEntry._ID);
            locationId = locationCursor.getLong(locationIdIndex);
        } else {
            // Now that the content provider is set up, inserting rows of data is pretty simple.
            // First create a ContentValues object to hold the data you want to insert.
            ContentValues locationValues = new ContentValues();

            // Then add the data, along with the corresponding name of the data type,
            // so the content provider knows what kind of value is being inserted.
            locationValues.put(WeatherContract.LocationEntry.COLUMN_CITY_NAME, cityName);
            locationValues.put(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING, locationSetting);
            locationValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LAT, lat);
            locationValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LONG, lon);

            // Finally, insert location data into the database.
//            Uri insertedUri = mContext.getContentResolver().insert(
//                    WeatherContract.LocationEntry.CONTENT_URI,
//                    locationValues
//            );
            db = dbHelper.getWritableDatabase();
            locationId = db.insert(
                    WeatherContract.LocationEntry.TABLE_NAME,
                    null,
                    locationValues
            );
        }

        locationCursor.close();
        return locationId;
    }

    @Override
    protected Void doInBackground(String... params) {

        Log.d("AsyncTask", "Запущен");

        String city = params[0];

        if(city == null) {
            return null;
        }

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String forecastJsonStr = null;

        try {
            // Construct the URL for the OpenWeatherMap query
            // Possible parameters are avaiable at OWM's forecast API page, at
            // http://openweathermap.org/API#forecast

            //
            // URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q=" + city + "&mode=json&units=metric&cnt=7&appid=ffd2bf2379705399609ab1651de867b9");

            final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
            final String QUERY_PARAM = "q";
            final String FORMAT_PARAM = "mode";
            final String UNITS_PARAM = "units";
            final String DAYS_PARAM = "cnt";
            final String APPID_PARAM = "APPID";


            Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                    .appendQueryParameter(QUERY_PARAM, city)
                    .appendQueryParameter(FORMAT_PARAM, "json")
                    .appendQueryParameter(UNITS_PARAM, "metric")
                    .appendQueryParameter(DAYS_PARAM, "7")
                    .appendQueryParameter(APPID_PARAM, "ffd2bf2379705399609ab1651de867b9")
                    .build();

            URL url = new URL(builtUri.toString());

            Log.v("Наш URL", builtUri.toString());

            //URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7&appid=ffd2bf2379705399609ab1651de867b9");

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            forecastJsonStr = buffer.toString();

            Log.d("Наша погода", forecastJsonStr);

            getWeatherDataFromJson(forecastJsonStr, city);

        } catch (IOException e) {
            //Log.e("FragmentException", e.getMessage());
            e.printStackTrace();
            // If the code didn't successfully get the weather data, there's no point in attemping
            // to parse it.
            return null;
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e("MainActivityFragment", "Error closing stream", e);
                }
            }
        }

        return null;
    }

//    @Override
//    protected void onPostExecute(String[] result) {
//        if(result != null) {
//            mForecastAdapter.clear();
//            for(String forecastDay : result) {
//                mForecastAdapter.add(forecastDay);
//            }
//        }
//    }

    private String getReadableDateString(long time){
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
        return shortenedDateFormat.format(time);
    }

    /**
     * Prepare the weather high/lows for presentation.
     */
    private String formatHighLows(double high, double low) {
        // For presentation, assume the user doesn't care about tenths of a degree.
        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);

        String highLowStr = roundedHigh + "/" + roundedLow;
        return highLowStr;
    }

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     *
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    private void getWeatherDataFromJson(String forecastJsonStr, String locationSetting)
            throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
        final String OWM_LIST = "list";
        final String OWM_WEATHER = "weather";
        final String OWM_TEMPERATURE = "temp";
        final String OWM_MAX = "max";
        final String OWM_MIN = "min";
        final String OWM_DESCRIPTION = "main";
        final String OWM_WEATHER_ID = "id";

        final String OWM_PRESSURE = "pressure";
        final String OWM_HUMIDITY = "humidity";
        final String OWM_WINDSPEED = "speed";
        final String OWM_WIND_DIRECTION = "deg";

        final String OWM_CITY = "city";
        final String OWM_CITY_NAME = "name";
        final String OWM_COORD = "coord";

        final String OWM_LATITUDE = "lat";
        final String OWM_LONGITUDE = "lon";

        JSONObject forecastJson = new JSONObject(forecastJsonStr);
        JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

        JSONObject cityJson = forecastJson.getJSONObject(OWM_CITY);
        String cityName = cityJson.getString(OWM_CITY_NAME);

        JSONObject cityCoord = cityJson.getJSONObject(OWM_COORD);
        double cityLatitude = cityCoord.getDouble(OWM_LATITUDE);
        double cityLongitude = cityCoord.getDouble(OWM_LONGITUDE);

        long locationId = addLocation(locationSetting, cityName, cityLatitude, cityLongitude);

        Time dayTime = new Time();
        dayTime.setToNow();

        // we start at the day returned by local time. Otherwise this is a mess.
        int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

        // now we work exclusively in UTC
        dayTime = new Time();

        WeatherDbHelper dbHelper = new WeatherDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        for(int i = 0; i < weatherArray.length(); i++) {
            // For now, using the format "Day, description, hi/low"
            long dateTime;
            double pressure;
            int humidity;
            double windSpeed;
            double windDirection;

            double high;
            double low;

            String description;
            int weatherId;

            JSONObject dayForecast = weatherArray.getJSONObject(i);

            pressure = dayForecast.getDouble(OWM_PRESSURE);
            humidity = dayForecast.getInt(OWM_HUMIDITY);
            windSpeed = dayForecast.getDouble(OWM_WINDSPEED);
            windDirection = dayForecast.getDouble(OWM_WIND_DIRECTION);

            dateTime = dayTime.setJulianDay(julianStartDay + i);
            //day = getReadableDateString(dateTime);

            // description is in a child array called "weather", which is 1 element long.
            JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
            description = weatherObject.getString(OWM_DESCRIPTION);
            weatherId = weatherObject.getInt(OWM_WEATHER_ID);

            // Temperatures are in a child object called "temp".  Try not to name variables
            // "temp" when working with temperature.  It confuses everybody.
            JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
            high = temperatureObject.getDouble(OWM_MAX);
            low = temperatureObject.getDouble(OWM_MIN);

            ContentValues weatherValues = new ContentValues();

            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_LOC_KEY, locationId);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DATE, dateTime);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, humidity);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE, pressure);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED, windSpeed);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DEGREES, windDirection);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, high);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP, low);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC, description);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID, weatherId);

            long insertId = db.insert(
                    WeatherContract.WeatherEntry.TABLE_NAME,
                    null,
                    weatherValues
            );

            Log.d("ID новой погоды", insertId + "");
        }
    }

}
