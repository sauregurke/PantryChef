package com.sauregurke.myapplication;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Objects;

public class IngredientActivity extends AppCompatActivity {
    private String r_Text = " ";
    SQLiteDatabase sqLiteDatabase;
    public static ArrayList<Ingredient> ingredients = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ingredients);

        Context context = getApplicationContext();
        sqLiteDatabase = context.openOrCreateDatabase("ingredients",
                Context.MODE_PRIVATE,
                null);
        DBHelper db = new DBHelper(sqLiteDatabase);
        ingredients = db.readIngredients("user");

        ArrayList<String> displayIngredients = new ArrayList<>();
        for (Ingredient i : ingredients) {
            displayIngredients.add(String.format("%s", i.getName()));
        }

        ListView listView = findViewById(R.id.ingredientList);
        ArrayAdapter adapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_1,
                displayIngredients);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                PopupMenu menu = new PopupMenu(IngredientActivity.this, view);

                menu.getMenuInflater().inflate(R.menu.ingredient_menu, menu.getMenu());

                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if (Objects.equals(menuItem.getTitle(), "@string/delete_ingredient")) {
                            Context context = getApplicationContext();
                            sqLiteDatabase = context.openOrCreateDatabase(
                                    "ingredients",
                                    Context.MODE_PRIVATE,
                                    null);

                            DBHelper db = new DBHelper(sqLiteDatabase);

                            Object itemToRemove = adapter.getItem(position);
                            String stringToRemove = itemToRemove.toString();

                            adapter.remove(itemToRemove);
                            db.deleteIngredient(stringToRemove);

                            sqLiteDatabase.close();
                            return true;
                        }

                        else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(
                                    IngredientActivity.this);
                            builder.setTitle("Edit Ingredient");
                            final EditText input = new EditText(
                                    IngredientActivity.this);
                            input.setHint("Type Here");

                            builder.setView(input);
                            builder.setPositiveButton("SAVE", new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int index) {
                                    r_Text = input.getText().toString();
                                    ingredients.set(position, new Ingredient(r_Text, "user"));
                                    Context context = getApplicationContext();

                                    sqLiteDatabase = context.openOrCreateDatabase(
                                            "ingredients",
                                            Context.MODE_PRIVATE,
                                            null);
                                    DBHelper db = new DBHelper(sqLiteDatabase);

                                    Object itemToRemove = adapter.getItem(position);
                                    String stringToRemove = itemToRemove.toString();

                                    adapter.remove(itemToRemove);
                                    db.deleteIngredient(stringToRemove);

                                    db.writeIngredient(r_Text, "user");
                                    adapter.add(r_Text);

                                    sqLiteDatabase.close();
                                }
                            });

                            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });

                            builder.show();
                            return true;
                        }
                    }
                });
                menu.show();
            }
        });

        ArrayList<String> newList = new ArrayList<>();

        ArrayAdapter emptyAdapter = new ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                newList);

        Button clearAll = findViewById(R.id.clearButton);

        clearAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context context = getApplicationContext();
                sqLiteDatabase = context.openOrCreateDatabase(
                        "ingredients",
                        Context.MODE_PRIVATE,
                        null);

                DBHelper db = new DBHelper(sqLiteDatabase);

                listView.setAdapter(emptyAdapter);
                db.clearIngredients();

                // read cleared ingredients database to refresh screen
                ingredients = db.readIngredients("user");
                sqLiteDatabase.close();
            }
        });

        Button findRecipes = findViewById(R.id.findRecipes);
        findRecipes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("Length of ingredients array: ", String.valueOf(ingredients.size()));
                if (ingredients.size() == 0) {

                    Toast toast = Toast.makeText(
                            view.getContext(),
                            "No ingredients available",
                            Toast.LENGTH_SHORT);

                    toast.show();

                } else {
                    addRecipes(view);
                }
            }
        });

    }
    public void addMoreIngredients(View view){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void addRecipes(View view){
        Intent intent = new Intent(this, Recipes.class);

        String ingredientString = createStringFromList(ingredients);
        intent.putExtra("message", ingredientString);

        startActivity(intent);
    }

    public String createStringFromList(ArrayList<Ingredient> ingredients){
        String strIngredients = "";
        Log.i("Info", "ingredients are: " + String.valueOf(ingredients));
        for(int i = 0; i < ingredients.size(); i++){
            Log.i("Info", strIngredients + String.valueOf(i));
            strIngredients += ingredients.get(i).getName();
            if(i != ingredients.size()-1){
                strIngredients += ",";
            }

        }
        return strIngredients;
    }

}

