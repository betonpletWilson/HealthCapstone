package com.fitwizard.fitwizard;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.Locale;

public class FoodAdapter extends ArrayAdapter<FoodData> {
    public FoodAdapter(Context context, List<FoodData> foods) {
        super(context, 0, foods);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        FoodData food = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.food_data_list_item, parent, false);
        }

        TextView nameText = convertView.findViewById(R.id.foodName);
        TextView detailsText = convertView.findViewById(R.id.foodDetails);

        assert food != null;
        nameText.setText(food.getName());
        String details = String.format(Locale.getDefault(), "%d cal, %.1fg protein",
                food.getCalories(), food.getProtein_g());
        detailsText.setText(details);

        return convertView;
    }
}