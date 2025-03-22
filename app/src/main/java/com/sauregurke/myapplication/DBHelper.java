package com.sauregurke.myapplication;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

public class DBHelper {
    SQLiteDatabase ingredientDatabase;

    public void createTable(){
        ingredientDatabase.execSQL("CREATE TABLE IF NOT EXISTS ingredients " +
                "(id INTENGER PRIMARY KEY, username TEXT, name TEXT)");
    }
    public DBHelper(SQLiteDatabase sqLiteDatabase) { this.ingredientDatabase = sqLiteDatabase; }

    public void clearIngredients() {
        createTable();
        ingredientDatabase.execSQL("DROP TABLE IF EXISTS ingredients");
    }

    public void deleteIngredient(String ingredient){
        createTable();
        ingredientDatabase.execSQL("DELETE FROM ingredients WHERE name = '%s'",
                new Object[]{ingredient});
    }
    public void writeIngredient(String name, String user){
        createTable();
        String query = "INSERT INTO ingredients (name, username) VALUES (?, ?)";
        ingredientDatabase.execSQL(query, new Object[]{name, user});
    }

    public ArrayList<Ingredient> readIngredients(String username) {
        createTable();

        Cursor c = ingredientDatabase.rawQuery("SELECT * FROM ingredients " +
                "WHERE username = ?", new String[]{username});

        int nameColumn = c.getColumnIndex("name");
        ArrayList<Ingredient> derivedList = new ArrayList<>();

        c.moveToFirst();

        while (!c.isAfterLast()) {
            String name = c.getString(nameColumn);

            Ingredient i = new Ingredient(name, username);
            derivedList.add(i);
            c.moveToNext();
        }

        c.close();
        ingredientDatabase.close();
        return derivedList;
    }


}
