package kz.learndroid.app.sunshine;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {

    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        TextView detailText = (TextView) rootView.findViewById(R.id.detail_text);
        Intent intent = getActivity().getIntent();
        long id_weather = intent.getLongExtra(Intent.EXTRA_TEXT, 0);

        detailText.setText(String.valueOf(id_weather));

        return rootView;
    }
}
