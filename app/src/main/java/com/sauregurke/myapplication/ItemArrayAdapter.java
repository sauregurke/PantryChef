package com.sauregurke.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class ItemArrayAdapter extends RecyclerView.Adapter<ItemArrayAdapter.ViewHolder> {

        private final int listItemLayout;

        private final ArrayList<Recipe> itemList;

        private final Context context;

        RequestQueue requestQueueLink;

        JSONObject JSONResponse;

        int responseCounterLink = 0;

        public ItemArrayAdapter(int layoutId, ArrayList<Recipe> itemList, Context context) {
            listItemLayout = layoutId;
            this.itemList = itemList;
            this.context = context;
        }

        @Override
        public int getItemCount() {
            return itemList == null ? 0 : itemList.size();
        }


        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(listItemLayout, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder,
                                     @SuppressLint("RecyclerView") final int listPosition) {
            TextView item = holder.item;
            item.setText(itemList.get(listPosition).getName());
            holder.itemview.setOnClickListener(view ->
                    getRecipeLinks(itemList.get(listPosition).getID(), listPosition));

        }

        private void getRecipeLinks(String ID, int position){
            if (ID == null) {
                Log.i("ERROR", "Recipe ID is NULL");
            } else {
                String url = "https://api.spoonacular.com/recipes/"
                        + ID
                        + "/information?apiKey=ef1a4c29f9704500aa8bcd1e00bd0666";
                requestQueueLink = Volley.newRequestQueue(context);
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                        Request.Method.GET,
                        url,
                        null,
                        response -> {
                            try {
                                JSONResponse = response;
                                JSONObject jsonObject1 = new JSONObject(response.toString());
                                itemList.get(position).setLink(jsonObject1.optString("spoonacularSourceUrl"));

                                responseCounterLink--;

                                if (responseCounterLink == 0) {
                                    launchRecipeLink(itemList.get(position).getLink());
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        },
                        error -> Log.i("The response returned an error: ", error.toString())
                );

                responseCounterLink++;
                requestQueueLink.add(jsonObjectRequest);
            }
    }

    public void launchRecipeLink(String link){
        Log.i("Link launch info", link);
        Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(link));
        context.startActivity(intent);
    }

        static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            public TextView item;
            View itemview;
            public ViewHolder(View itemView) {
                super(itemView);
                itemView.setOnClickListener(this);
                item = itemView.findViewById(R.id.recipe_name);
                itemview = itemView;
            }

            @Override
            public void onClick(View view) {

            }
        }
    }

