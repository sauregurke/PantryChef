package com.sauregurke.myapplication;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

        listView.setOnItemClickListener((adapterView, view, position, id) -> {
            PopupMenu menu = new PopupMenu(IngredientActivity.this, view);

            menu.getMenuInflater().inflate(R.menu.ingredient_menu, menu.getMenu());

            menu.setOnMenuItemClickListener(menuItem -> {
                if (Objects.equals(menuItem.getTitle(), "@string/delete_ingredient")) {
                    Context context1 = getApplicationContext();
                    sqLiteDatabase = context1.openOrCreateDatabase(
                            "ingredients",
                            Context.MODE_PRIVATE,
                            null);

                    DBHelper db1 = new DBHelper(sqLiteDatabase);

                    Object itemToRemove = adapter.getItem(position);
                    assert itemToRemove != null;
                    String stringToRemove = itemToRemove.toString();

                    adapter.remove(itemToRemove);
                    db1.deleteIngredient(stringToRemove);

                    sqLiteDatabase.close();
                }

                else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(
                            IngredientActivity.this);
                    builder.setTitle("Edit Ingredient");
                    final EditText input = new EditText(
                            IngredientActivity.this);
                    input.setHint("Type Here");

                    builder.setView(input);
                    builder.setPositiveButton("SAVE", (dialog, index) -> {
                        r_Text = input.getText().toString();
                        ingredients.set(position, new Ingredient(r_Text, "user"));
                        Context context1 = getApplicationContext();

                        sqLiteDatabase = context1.openOrCreateDatabase(
                                "ingredients",
                                Context.MODE_PRIVATE,
                                null);
                        DBHelper db1 = new DBHelper(sqLiteDatabase);

                        Object itemToRemove = adapter.getItem(position);
                        assert itemToRemove != null;
                        String stringToRemove = itemToRemove.toString();

                        adapter.remove(itemToRemove);
                        db1.deleteIngredient(stringToRemove);

                        db1.writeIngredient(r_Text, "user");
                        adapter.add(r_Text);

                        sqLiteDatabase.close();
                    });

                    builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

                    builder.show();
                }
                return true;
            });
            menu.show();
        });

        ArrayList<String> newList = new ArrayList<>();

        ArrayAdapter emptyAdapter = new ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                newList);

        Button clearAll = findViewById(R.id.clearButton);

        clearAll.setOnClickListener(view -> {
            Context context12 = getApplicationContext();
            sqLiteDatabase = context12.openOrCreateDatabase(
                    "ingredients",
                    Context.MODE_PRIVATE,
                    null);

            DBHelper db12 = new DBHelper(sqLiteDatabase);

            listView.setAdapter(emptyAdapter);
            db12.clearIngredients();

            // read cleared ingredients database to refresh screen
            ingredients = db12.readIngredients("user");
            sqLiteDatabase.close();
        });

        Button findRecipes = findViewById(R.id.findRecipes);
        findRecipes.setOnClickListener(view -> {
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
        StringBuilder strIngredients = new StringBuilder();
        Log.i("Info", "ingredients are: " + ingredients);
        for (int i = 0; i < ingredients.size(); i++){
            Log.i("Info", strIngredients + " " + i);
            strIngredients.append(ingredients.get(i).getName());
            if(i != ingredients.size()-1){
                strIngredients.append(",");
            }
        }
        return strIngredients.toString();
    }

}

