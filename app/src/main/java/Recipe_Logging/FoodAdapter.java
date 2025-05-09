package Recipe_Logging;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.fitwizard.fitwizard.R;
import java.util.List;
import java.util.Locale;

public class FoodAdapter extends ArrayAdapter<FoodData> {
    public FoodAdapter(Context context, List<FoodData> foods) {
        super(context, 0, foods);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        FoodData food = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.food_data_list_item,
                            parent, false);
        }

        TextView nameText    = convertView.findViewById(R.id.foodName);
        TextView detailsText = convertView.findViewById(R.id.foodDetails);

        assert food != null;
        nameText.setText(food.getName());

        /* ---- FIX: ensure calories goes to %d as an int ---- */
        int cals = Math.round(food.getCalories());
        String details = String.format(
                Locale.getDefault(),
                "%d cal, %.1fg protein",
                cals,
                food.getProtein());

        detailsText.setText(details);
        return convertView;
    }
}
