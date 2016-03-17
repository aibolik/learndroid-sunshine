package kz.learndroid.app.sunshine;

import android.app.LauncherActivity;
import android.content.Intent;
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

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements AdapterView.OnItemClickListener {

    public MainActivityFragment() {
    }

    ArrayAdapter mForecastAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        String[] weather = {
                "Today - Cloudy - 8/6",
                "Sunday - Sunny - 10/12",
                "Monday - Cloudy - 8/6",
                "Tuesday - Cloudy - 8/6",
                "Wednesday - Cloudy - 8/6",
                "Thursday - Cloudy - 8/6",
                "Friday - Cloudy - 8/6"
        };
        List weekForecast = new ArrayList(Arrays.asList(weather));
        mForecastAdapter = new ArrayAdapter(getActivity(), R.layout.list_item_forecast, R.id.textview_forecast, weekForecast);

        ListView forecast = (ListView) rootView.findViewById(R.id.listview_forecast);
        forecast.setAdapter(mForecastAdapter);

        forecast.setOnItemClickListener(this);
//        forecast.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                //Toast.makeText(getActivity(), "Мы нажали на позицию " + position, Toast.LENGTH_LONG).show();
//                Intent intent = new Intent(getActivity(), DetailActivity.class);
//                String detailWeather = (String) mForecastAdapter.getItem(position);
//                intent.putExtra(Intent.EXTRA_TEXT, detailWeather);
//                startActivity(intent);
//            }
//        });

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
            FetchWeatherTask task = new FetchWeatherTask(getActivity());
            task.execute("Astana");
        }

        return true;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(getActivity(), DetailActivity.class);
        String detailWeather = (String) mForecastAdapter.getItem(position);
        intent.putExtra(Intent.EXTRA_TEXT, detailWeather);
        startActivity(intent);

    }
}
