package marshrutik.marshrutik;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import java.util.ArrayList;
import java.util.List;

//адаптер для отображения listView с чекбоксами для построения маршрута по достопримечательностям
public class SightAdapter extends BaseAdapter {
    Context context;
    LayoutInflater layoutInflater;
    List<Sights> arraySights;
    SightAdapter(Context mContext, List<Sights> sights) {
        context = mContext;
        arraySights = sights;
        layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return arraySights.size();
    }

    @Override
    public Object getItem(int position) {
        return arraySights.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position; //вот тут какая-то шляпа
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = layoutInflater.inflate(R.layout.sight_list_item, parent, false);
        }
        CheckBox checkBox = (CheckBox)view.findViewById(R.id.checkBoxSight);
        Sights sights = getSights(position);
        checkBox.setText(sights.getSightName());
        checkBox.setTag(position);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                getSights((Integer)buttonView.getTag()).setSightSelected(isChecked);
            }
        });

        return view;
    }

    private Sights getSights(int position) {
        return ((Sights)getItem(position));
    }

    public List<Sights> getCheckedSights() {
        List<Sights> checkedSights = new ArrayList<>();
        for (Sights sights: arraySights) {
            if (sights.isSightSelected()) {
                checkedSights.add(sights);
            }
        }
        return checkedSights;
    }
}
