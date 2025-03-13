package com.sauregurke.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    SQLiteDatabase ingredientDatabase;
    private ImageView rImageView;
    private Button rTextButton;
    private Bitmap rSelectedImage;
    private com.sauregurke.myapplication.GraphicOverlay rGraphicOverlay;
    private Integer rImageMaxWidth;
    private Integer rImageMaxHeight;

    public static final int MULTIPLE_PERMISSIONS_REQUEST_ID = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rImageView = findViewById(R.id.receiptImageView);
        rTextButton = findViewById(R.id.scanTextButton);
        rGraphicOverlay = findViewById(R.id.graphic_overlay);

        rTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runTextRecognition();
            }
        });
    }

    public void writeIngredient(View view){

        EditText input = (EditText) findViewById(R.id.ingredientEditText);
        Context context = getApplicationContext();

        ingredientDatabase = context.openOrCreateDatabase("ingredients",
                Context.MODE_PRIVATE,
                null);
        DBHelper db = new DBHelper(ingredientDatabase);

        db.writeIngredient(input.getText().toString(),"user");
        input.setText("");
    }

    public void viewIngredients(View view) {
        Intent intent = new Intent(this, IngredientActivity.class);
        startActivity(intent);
    }

    public void takePicture(View view) {

        if (checkAndRequestPermissions(MainActivity.this)) {
            chooseImage(MainActivity.this);
        }

    }

    private void chooseImage(Context context){

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose an option");
        builder.setItems(new CharSequence[]{"Take Photo", "Choose from Gallery", "Cancel"},
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0: // Take a photo
                                openCamera();
                                break;
                            case 1: // Pick from gallery
                                openGallery();
                                break;
                            case 2: // Cancel
                                dialog.dismiss();
                                break;
                        }
                    }
                });
        builder.show();
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, 0);
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, 1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case MULTIPLE_PERMISSIONS_REQUEST_ID:
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(getApplicationContext(),
                                    "PantryChef needs camera access to work.",
                                    Toast.LENGTH_SHORT).show();

                } else if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(getApplicationContext(),
                            "PantryChef needs access to your storage to work.",
                            Toast.LENGTH_SHORT).show();

                } else {
                    chooseImage(MainActivity.this);
                }
                break;
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case 0:
                    if (resultCode == RESULT_OK && data != null) {

                        Bitmap selectedImage = (Bitmap) data.getExtras().get("data");

                        presentImage(selectedImage);
                        ImageView imageView = findViewById(R.id.receiptImageView);
                        imageView.setVisibility(View.VISIBLE);
                    }
                    break;
                case 1:
                    if (resultCode == RESULT_OK && data != null) {
                        Uri selectedImage = data.getData();
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        if (selectedImage != null) {
                            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                            if (cursor != null) {
                                cursor.moveToFirst();

                                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                String picturePath = cursor.getString(columnIndex);
                                getImage(picturePath);
                                ImageView imageView = (ImageView) findViewById(R.id.receiptImageView);
                                imageView.setVisibility(View.VISIBLE);
                                cursor.close();
                            }
                        }

                    }
                    break;
            }
        }
    }

    public static boolean checkAndRequestPermissions(final Activity context) {
        List<String> listPermissionsNeeded = new ArrayList<>();

        int cameraPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA);

        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(context,
                    listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),
                    MULTIPLE_PERMISSIONS_REQUEST_ID);
            return false;
        }
        return true;
    }

    private void runTextRecognition() {
        if (rSelectedImage == null) {
            Toast toast = Toast.makeText(
                    this.getApplicationContext(),
                    "Please upload a receipt.",
                    Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        InputImage image = InputImage.fromBitmap(rSelectedImage, 0);
        TextRecognizer recognizer = TextRecognition.getClient(
                new TextRecognizerOptions.Builder().build());
        rTextButton.setEnabled(false);
        recognizer.process(image)
                .addOnSuccessListener(
                        new OnSuccessListener<Text>() {
                            @Override
                            public void onSuccess(Text texts) {
                                rTextButton.setEnabled(true);
                                processTextRecognitionResult(texts);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Task failed with an exception
                                rTextButton.setEnabled(true);
                                e.printStackTrace();
                            }
                        });
    }

    public static boolean isUpperCase(String s) {
        for (int i = 0; i < s.length(); i++)
            if (!Character.isUpperCase(s.charAt(i)))
                return false;
        return true;
    }

    private void processTextRecognitionResult(Text texts) {
        List<Text.TextBlock> blocks = texts.getTextBlocks();

        if (blocks.size() == 0) {
            showToast("No text found.");
            return;
        }
        List<String> textblocks = new ArrayList<>();
        rGraphicOverlay.clear();
        for (int i = 0; i < blocks.size(); i++) {
            List<Text.Line> lines = blocks.get(i).getLines();
            String word = "";
            for (int j = 0; j < lines.size(); j++) {
                List<Text.Element> elements = lines.get(j).getElements();
                for (int k = 0; k < elements.size(); k++) {
                    if (elements.get(k).getText().matches(".*\\d.*")
                            || elements.get(k).getText().length() < 3) {
                        continue;
                    }

                    if(isUpperCase(elements.get(k).getText())){
                        word += elements.get(k).getText();
                        if(k != elements.size()-1 && !word.equals("") && word.length() > 1){
                            word += " ";
                        }
                    }

                }
                if(!word.equals("") && !word.equals(" ")) {
                    textblocks.add(word);
                }
                word = "";
            }
        }

        Toast toast = Toast.makeText(
                this.getApplicationContext(),
                "Receipt successfully scanned!",
                Toast.LENGTH_SHORT);
        toast.show();

        Context context = getApplicationContext();
        ingredientDatabase = context.openOrCreateDatabase(
                "ingredients",
                Context.MODE_PRIVATE,
                null);

        DBHelper db = new DBHelper(ingredientDatabase);
        ArrayList<String> removals = new ArrayList<String>();
        removals.add("DATE");
        removals.add("SPECIAL");
        removals.add("NET");
        removals.add("NET ");
        removals.add("SPECIAL ");
        removals.add("SUBTOTAL");
        removals.add("LOYALTY");
        removals.add("TOTAL");
        removals.add("CASH");
        removals.add("CHANGE");
        textblocks.removeAll(removals);
        Log.i("checkText", String.valueOf(textblocks));

        for (int i = 0; i < textblocks.size(); i++) {
            db.writeIngredient(textblocks.get(i), "user");
        }
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private Integer getImageMaxWidth() {
        if (rImageMaxWidth == null) {
            rImageMaxWidth = rImageView.getWidth();
        }

        return rImageMaxWidth;
    }

    private Integer getImageMaxHeight() {
        if (rImageMaxHeight == null) {
            rImageMaxHeight = rImageView.getHeight();
        }

        return rImageMaxHeight;
    }

    private Pair<Integer, Integer> getTargetedWidthHeight() {
        int targetWidth;
        int targetHeight;
        int maxWidthForPortraitMode = getImageMaxWidth();
        int maxHeightForPortraitMode = getImageMaxHeight();
        targetWidth = maxWidthForPortraitMode;
        targetHeight = maxHeightForPortraitMode;
        return new Pair<>(targetWidth, targetHeight);
    }

    public void getImage(String imageName) {
        presentImage(imageName);
    }

    public void presentImage(String imageName) {
        rGraphicOverlay.clear();
        rSelectedImage = BitmapFactory.decodeFile(imageName);
        if (rSelectedImage != null) {
            // Get View dimensions
            Pair<Integer, Integer> targetedSize = getTargetedWidthHeight();

            int targetWidth = targetedSize.first;
            int maxHeight = targetedSize.second;

            // Determine how much to scale down the image
            float scaleFactor =
                    Math.max(
                            (float) rSelectedImage.getWidth() / (float) targetWidth,
                            (float) rSelectedImage.getHeight() / (float) maxHeight);

            Bitmap resizedBitmap =
                    Bitmap.createScaledBitmap(
                            rSelectedImage,
                            (int) (rSelectedImage.getWidth() / scaleFactor),
                            (int) (rSelectedImage.getHeight() / scaleFactor),
                            true);

            rImageView.setImageBitmap(resizedBitmap);
            rSelectedImage = resizedBitmap;
        }
    }

    public void presentImage(Bitmap imageMap) {
        rGraphicOverlay.clear();
        rSelectedImage = imageMap;
        if (rSelectedImage != null) {
            // View dimensions
            Pair<Integer, Integer> targetedSize = getTargetedWidthHeight();

            int targetWidth = targetedSize.first;
            int maxHeight = targetedSize.second;

            // Determine how much to scale down the image
            float scaleFactor =
                    Math.max(
                            (float) rSelectedImage.getWidth() / (float) targetWidth,
                            (float) rSelectedImage.getHeight() / (float) maxHeight);

            Bitmap resizedBitmap =
                    Bitmap.createScaledBitmap(
                            rSelectedImage,
                            (int) (rSelectedImage.getWidth() / scaleFactor),
                            (int) (rSelectedImage.getHeight() / scaleFactor),
                            true);

            rImageView.setImageBitmap(resizedBitmap);
            rSelectedImage = resizedBitmap;
        }
    }


}