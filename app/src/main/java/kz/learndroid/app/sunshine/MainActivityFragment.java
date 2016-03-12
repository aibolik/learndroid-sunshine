package kz.learndroid.app.sunshine;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    public MainActivityFragment() {
    }

    ArrayAdapter mForecastAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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

        return rootView;
    }
}
