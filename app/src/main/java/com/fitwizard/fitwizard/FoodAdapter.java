package com.fitwizard.fitwizard;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class FoodAdapter extends ArrayAdapter<FoodData> {
    public FoodAdapter(Context context, List<FoodData> foods) {
        super(context, 0, foods);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        FoodData food = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.food_data_list_item, parent, false);
        }

        TextView nameText = convertView.findViewById(R.id.foodName);
        TextView detailsText = convertView.findViewById(R.id.foodDetails);

        assert food != null;
        nameText.setText(food.getName());
        String details = getContext().getString(R.string.food_details, food.getCalories(), food.getProtein());
        detailsText.setText(details);

        return convertView;
    }
}