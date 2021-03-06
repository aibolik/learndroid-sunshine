package kz.learndroid.app.sunshine;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import kz.learndroid.app.sunshine.data.WeatherContract;

/**
 * Created by aibol on 3/18/16.
 */
public class ForecastAdapter extends CursorAdapter {
    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    public static final int VIEW_TYPE_TODAY = 0;
    public static final int VIEW_TYPE_NEXT_DAYS = 1;
    public static final int VIEW_TYPE_COUNT = 2;

    @Override
    public int getItemViewType(int position) {
        if(position == 0) {
            return VIEW_TYPE_TODAY;
        }
        else {
            return VIEW_TYPE_NEXT_DAYS;
        }
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        int viewType = getItemViewType(cursor.getPosition());

        int resourceId;

        if(viewType == VIEW_TYPE_TODAY) {
            resourceId = R.layout.list_item_forecast_today;
        } else {
            resourceId = R.layout.list_item_forecast;
        }

        View view = LayoutInflater.from(context).inflate(resourceId, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);

        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();


        long dateInMillis = cursor.getLong(ForecastFragment.COL_DATE) - 86400000;

        Log.d("currentTime", System.currentTimeMillis() + "");
        Log.d("dateInMillis", dateInMillis + "");

        String friendlyDate = Utility.getFriendlyDayString(context, dateInMillis);
        String shortDesc = cursor.getString(ForecastFragment.COL_SHORT_DESC);
        int highTemp = cursor.getInt(ForecastFragment.COL_MAX_TEMP);
        int lowTemp = cursor.getInt(ForecastFragment.COL_MIN_TEMP);
        int weatherId = cursor.getInt(ForecastFragment.COL_WEATHER_ID);

        int viewType = getItemViewType(cursor.getPosition());

        if(viewType == VIEW_TYPE_TODAY) {
            viewHolder.iconView.setImageDrawable(context.getResources().getDrawable(
                    Utility.getArtResourceForWeatherCondition(weatherId)
            ));
        } else {
            viewHolder.iconView.setImageDrawable(context.getResources().getDrawable(
                    Utility.getIconResourceForWeatherCondition(weatherId)
            ));
        }
        viewHolder.dateTextView.setText(friendlyDate);
        viewHolder.forecastTextView.setText(shortDesc);
        viewHolder.highTempTextView.setText(context.getString(R.string.format_temperature, highTemp));
        viewHolder.lowTempTextView.setText(context.getString(R.string.format_temperature, lowTemp));
    }

    class ViewHolder {
        ImageView iconView;
        TextView forecastTextView;
        TextView dateTextView;
        TextView highTempTextView;
        TextView lowTempTextView;

        public ViewHolder(View view) {
            iconView = (ImageView) view.findViewById(R.id.list_item_icon);
            dateTextView = (TextView) view.findViewById(R.id.list_item_date_textview);
            forecastTextView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
            highTempTextView = (TextView) view.findViewById(R.id.list_item_high_textview);
            lowTempTextView = (TextView) view.findViewById(R.id.list_item_low_textview);
        }
    }

}
