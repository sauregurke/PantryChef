package com.sauregurke.myapplication;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

public class DBHelper {
    SQLiteDatabase ingredientDatabase;

    public void createTable(){
        ingredientDatabase.execSQL("CREATE TABLE IF NOT EXISTS ingredients " +
                "(id INTENGER PRIMARY KEY, username TEXT,name TEXT)");
    }
    public DBHelper(SQLiteDatabase sqLiteDatabase) { this.ingredientDatabase = sqLiteDatabase; }

    public void clearIngredients() {
        createTable();
        ingredientDatabase.execSQL(String.format("DROP TABLE ingredients"));
    }

    public void deleteIngredient(String ingredient){
        createTable();
        ingredientDatabase.execSQL(String.format("DELETE FROM ingredients WHERE name = '%s'",
                ingredient));
    }
    public void writeIngredient(String name, String user){
        createTable();
        ingredientDatabase.execSQL(String.format("INSERT INTO ingredients (name, username) " +
                        "VALUES ('%s','%s')", name, user));
    }

    public ArrayList<Ingredient> readIngredients(String username) {
        createTable();

        Cursor c = ingredientDatabase.rawQuery(String.format("SELECT * from ingredients " +
                "where username like '%s'",username),null);
        int nameColumn = c.getColumnIndex("name");
        c.moveToFirst();

        ArrayList<Ingredient> derivedList = new ArrayList<>();

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
