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

import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class ItemArrayAdapter extends RecyclerView.Adapter<ItemArrayAdapter.ViewHolder> {

        //All methods in this adapter are required for a bare minimum recyclerview adapter
        private int listItemLayout;
        private ArrayList<Recipe> itemList;
        private Context context;
        // Constructor of the class
        public ItemArrayAdapter(int layoutId, ArrayList<Recipe> itemList, Context context) {
            listItemLayout = layoutId;
            this.itemList = itemList;
            this.context = context;
        }

        // get the size of the list
        @Override
        public int getItemCount() {
            return itemList == null ? 0 : itemList.size();
        }


        // specify the row layout file and click for each row
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(listItemLayout, parent, false);
            ViewHolder myViewHolder = new ViewHolder(view);
            return myViewHolder;
        }

        // load data in each row element
        @Override
        public void onBindViewHolder(final ViewHolder holder, @SuppressLint("RecyclerView") final int listPosition) {
            TextView item = holder.item;
            item.setText(itemList.get(listPosition).getName());
            holder.itemview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getRecipeLinks(itemList.get(listPosition).getid(), listPosition);
//                    Log.i("info", itemList.get(listPosition).getName());
                }
            });

        }


        RequestQueue requestQueueLink;
        JSONObject JSONresponse;
        int responsecounterLink = 0;

        private void getRecipeLinks(String ID, int position){
            if(ID == null){
                Log.i("ERROR", "Recipe ID is NULL");
            }else {
                String url = "https://api.spoonacular.com/recipes/" + ID + "/information?apiKey=ef1a4c29f9704500aa8bcd1e00bd0666";
                requestQueueLink = Volley.newRequestQueue(context);
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONresponse = response;
                            JSONObject jsonObject1 = new JSONObject(response.toString());
                            itemList.get(position).setLink(jsonObject1.optString("spoonacularSourceUrl"));

                            responsecounterLink--;

                            if (responsecounterLink == 0) {
                                launchRecipeLink(itemList.get(position).getLink());
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.i("the res is error in second call:", error.toString());
                        }
                    }
                );

                responsecounterLink++;
                requestQueueLink.add(jsonObjectRequest);
            }
    }

    public void launchRecipeLink(String link){
        Log.i("Link launch info", link);
        Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(link));
        context.startActivity(intent);
    }

        // Static inner class to initialize the views of rows
        static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            public TextView item;
            View itemview;
            public ViewHolder(View itemView) {
                super(itemView);
                itemView.setOnClickListener(this);
                item = (TextView) itemView.findViewById(R.id.recipe_name);
                itemview = itemView;
            }
            @Override
            public void onClick(View view) {

            }
        }
    }

