package kz.learndroid.app.sunshine;

import android.app.LauncherActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import kz.learndroid.app.sunshine.data.WeatherContract;
import kz.learndroid.app.sunshine.data.WeatherDbHelper;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment implements AdapterView.OnItemClickListener {

    public ForecastFragment() {
    }

    String columns[] = {
            WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP
    };

    public static final int COL_ID = 0;
    public static final int COL_DATE = 1;
    public static final int COL_SHORT_DESC = 2;
    public static final int COL_MIN_TEMP = 3;
    public static final int COL_MAX_TEMP = 4;

    ForecastAdapter mForecastAdapter;
    WeatherDbHelper dbHelper;
    SQLiteDatabase db;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new WeatherDbHelper(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        db = dbHelper.getReadableDatabase();

        long startDate = System.currentTimeMillis();

        Cursor locId = db.query(
                WeatherContract.LocationEntry.TABLE_NAME,
                new String[]{WeatherContract.LocationEntry._ID},
                WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ?",
                new String[]{"Almaty"},
                null,
                null,
                null
        );
        locId.moveToFirst();
        long locationId = locId.getLong(0);

        Cursor weatherData = db.query(
                WeatherContract.WeatherEntry.TABLE_NAME,
                columns,
                WeatherContract.WeatherEntry.COLUMN_LOC_KEY + " = ? AND " +
                        WeatherContract.WeatherEntry.COLUMN_DATE + " >= ?",
                new String[] {String.valueOf(locationId), String.valueOf(startDate)},
                null,
                null,
                WeatherContract.WeatherEntry.COLUMN_DATE + " ASC"
        );
        mForecastAdapter = new ForecastAdapter(getActivity(), weatherData, 0);

        ListView forecast = (ListView) rootView.findViewById(R.id.listview_forecast);
        forecast.setAdapter(mForecastAdapter);

        forecast.setOnItemClickListener(this);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            updateWeather();
        }

        return true;
    }

    private void updateWeather() {
        FetchWeatherTask task = new FetchWeatherTask(getActivity());
        task.execute("Almaty");
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(getActivity(), DetailActivity.class);
        Cursor c = (Cursor) mForecastAdapter.getItem(position);
        long id_weather = c.getLong(0);
        intent.putExtra(Intent.EXTRA_TEXT, id_weather);
        startActivity(intent);

    }
}
