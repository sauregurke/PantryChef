package com.sauregurke.myapplication;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;
import java.io.InputStream;
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

        rTextButton.setOnClickListener(v -> runTextRecognition());
    }

    public void writeIngredient(View view){

        EditText input = findViewById(R.id.ingredientEditText);
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
                (dialog, which) -> {
                    switch (which) {
                        case 0: // New photo
                            openCamera();
                            break;
                        case 1: // Pick from gallery
                            openGallery();
                            break;
                        case 2: // Never mind
                            dialog.dismiss();
                            break;
                    }
                });
        builder.show();
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        openCameraActivityLauncher.launch(cameraIntent);
    }

    ActivityResultLauncher<Intent> openCameraActivityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    assert result.getData() != null;
                    Intent data = result.getData();

                    if (data.getExtras() != null) {
                        try {
                            Bitmap selectedImage = (Bitmap) data.getExtras().get("data");

                            presentImage(selectedImage);

                            ImageView imageView = findViewById(R.id.receiptImageView);
                            imageView.setVisibility(View.VISIBLE);
                        } catch (NullPointerException e) {
                            Toast.makeText(getApplicationContext(),
                                    "Please try to select an image again.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
    );

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        openGalleryActivityLauncher.launch(galleryIntent);
    }

    ActivityResultLauncher<Intent> openGalleryActivityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    assert result.getData() != null;
                    Uri selectedImage = result.getData().getData();

                    if (selectedImage != null) {
                        try {
                            InputStream imageStream =
                                    getContentResolver().openInputStream(selectedImage);

                            Bitmap bitmap = BitmapFactory.decodeStream(imageStream);

                            presentImage(bitmap);

                            ImageView imageView = findViewById(R.id.receiptImageView);
                            imageView.setImageBitmap(bitmap);
                            imageView.setVisibility(View.VISIBLE);

                        } catch (IOException e) {
                            Toast.makeText(getApplicationContext(),
                                    "Please try to select an image again.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
    );

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MULTIPLE_PERMISSIONS_REQUEST_ID) {
            if (ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(getApplicationContext(),
                        "PantryChef needs camera access to scan your receipt.",
                        Toast.LENGTH_SHORT).show();
            } else {
                chooseImage(MainActivity.this);
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
                    listPermissionsNeeded.toArray(new String[0]),
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
                        texts -> {
                            rTextButton.setEnabled(true);
                            processTextRecognitionResult(texts);
                        })
                .addOnFailureListener(
                        e -> {
                            // Task failed with an exception
                            rTextButton.setEnabled(true);
                            e.printStackTrace();
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
            Toast.makeText(getApplicationContext(),
                    "No text could be found.",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        List<String> textBlocks = new ArrayList<>();
        rGraphicOverlay.clear();
        for (int i = 0; i < blocks.size(); i++) {
            List<Text.Line> lines = blocks.get(i).getLines();
            StringBuilder word = new StringBuilder();

            for (int j = 0; j < lines.size(); j++) {
                List<Text.Element> elements = lines.get(j).getElements();

                for (int k = 0; k < elements.size(); k++) {
                    if (elements.get(k).getText().matches(".*\\d.*") // any line with at least one digit
                            || elements.get(k).getText().length() < 3) {
                        continue;
                    }

                    if(isUpperCase(elements.get(k).getText())){
                        word.append(elements.get(k).getText());
                        if(k != elements.size()-1 && !word.toString().equals("") && word.length() > 1){
                            word.append(" ");
                        }
                    }

                }
                if(!word.toString().equals("") && !word.toString().equals(" ")) {
                    textBlocks.add(word.toString());
                }
                word = new StringBuilder();
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
        ArrayList<String> removals = new ArrayList<>();
        removals.add("CASH");
        removals.add("CHANGE");
        removals.add("DATE");
        removals.add("LOYALTY");
        removals.add("NET");
        removals.add("NET ");
        removals.add("SPECIAL");
        removals.add("SPECIAL ");
        removals.add("SUBTOTAL");
        removals.add("TOTAL");

        textBlocks.removeAll(removals);

        for (int i = 0; i < textBlocks.size(); i++) {
            db.writeIngredient(textBlocks.get(i), "user");
        }
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

    public void presentImage(Bitmap imageMap) {
        rGraphicOverlay.clear();
        rSelectedImage = imageMap;

        if (rSelectedImage != null) {
            Pair<Integer, Integer> targetedSize = getTargetedWidthHeight();

            int targetWidth = targetedSize.first;
            int maxHeight = targetedSize.second;

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