package com.sauregurke.myapplication;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class Recipes extends AppCompatActivity {

    private JSONArray JSONresponse;
    private List<Recipe> recipeList = new ArrayList<>();
    RequestQueue requestQueue;
    int responseCounter = 0;
    RecyclerView recipeDisplay;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipes);
        recipeDisplay = (RecyclerView) findViewById(R.id.recyclerView);

        Log.i("Info", "about to call createRecipes");

        Bundle bundle = getIntent().getExtras();
        String ingredients = bundle.getString("message");
        createRecipes(ingredients);
    }

    public void createRecipes(String searchText) {
        String url = "https://api.spoonacular.com/recipes/findByIngredients?ingredients="
                + searchText
                // API KEY
                + "&number=30&instructionsRequired=true&apiKey=ef1a4c29f9704500aa8bcd1e00bd0666";
        requestQueue = Volley.newRequestQueue(this);
        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    JSONresponse = response;
                    for (int i = 0; i < JSONresponse.length(); i++) {
                        JSONObject jsonResult;
                        jsonResult = JSONresponse.getJSONObject(i);
                        recipeList.add(new Recipe(
                                jsonResult.optString("id"),
                                jsonResult.optString("title"),
                                jsonResult.optString("image"),
                                null));
                    }
                    responseCounter--;

                    if (responseCounter == 0) {
                        launchRecipes();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i("Response contained an error: ", error.toString());
                    }
                }
        );
        responseCounter++;
        requestQueue.add(jsonObjectRequest);
    }

    private void launchRecipes() {

        ItemArrayAdapter adapter = new ItemArrayAdapter(
                R.layout.linear_recipe_layout,
                new ArrayList<Recipe>(recipeList),
                this);
        recipeDisplay = findViewById(R.id.recyclerView);
        recipeDisplay.setLayoutManager(new LinearLayoutManager(this));
        recipeDisplay.setItemAnimator(new DefaultItemAnimator());
        recipeDisplay.setAdapter(adapter);

    }
}