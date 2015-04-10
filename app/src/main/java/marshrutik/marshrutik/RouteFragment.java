package marshrutik.marshrutik;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

//фрагмент маршрута, который выдается в поиске, с картинкой, названием, описанием и темами
public class RouteFragment extends Fragment implements View.OnClickListener{

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("TEST", "Enter onCreateView");
        View rootView =
                inflater.inflate(R.layout.route_fragment, container, false);
        RelativeLayout mainLayout = (RelativeLayout)rootView.findViewById(R.id.mainLayout);
        TextView routeTitle = (TextView)rootView.findViewById(R.id.textViewRouteTitle);
        TextView routeDescription = (TextView)
                rootView.findViewById(R.id.textViewRouteDescription);
        Bundle args = getArguments();
        if (!args.isEmpty()) {
            routeTitle.setText(args.getString("title"));
            routeDescription.setText(args.getString("description"));
        }
        mainLayout.setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onClick(View v) {
        Toast.makeText(getActivity(),
                "Pressed the layout", Toast.LENGTH_SHORT).show();
    }

}
