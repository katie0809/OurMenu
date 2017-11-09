package com.example.kyungimlee.ourmenu;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.translate.Translate;
import com.google.api.services.translate.TranslateRequestInitializer;
import com.google.api.services.translate.model.LanguagesListResponse;
import com.google.api.services.translate.model.LanguagesResource;
import com.google.api.services.translate.model.TranslationsListResponse;
import com.google.api.services.translate.model.TranslationsResource;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequest;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.*;
import com.soundcloud.android.crop.Crop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import static java.lang.System.out;

public class MenuBoardActivity extends AppCompatActivity {

    private static final String API_KEY = "AIzaSyDON76sNwTcC2AuXU2L_y31z7BtHYP74Ko";
    private static final String ANDROID_CERT_HEADER = "X-Android-Cert";
    private static final String ANDROID_PACKAGE_HEADER = "X-Android-Package";
    public static final int WRITE_PERMISSIONS_REQUEST = 4;

    private static final String TAG = MainActivity.class.getSimpleName();
    private ImageView main_img;
    private Bitmap bitmap;
    private TextView loading_str;

    List<TranslationsResource> translationsResponseList;
    List<String> translated_txt = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_board);

        //set image view
        main_img = (ImageView) findViewById(R.id.menu_img);
        loading_str = (TextView) findViewById(R.id.loading_str);

        //accept information from mainactivity
        Bundle uridata = getIntent().getExtras();
        if(uridata == null){
            return;
        }
        String struri = uridata.getString("imageUri");
        String selected_lang = uridata.getString("langChoice");
        Uri uri = Uri.parse(struri);

        uploadImage(uri);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_menu_board, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public String getExternalPath(){
        String sdPath = "";
        String ext = Environment.getExternalStorageState();
        if(ext.equals(Environment.MEDIA_MOUNTED)){
            sdPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
        }else{
            sdPath = getFilesDir() + "";
        }

        return sdPath;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_save) {
            //Ask permission for access to external storage
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            //Manifest.permission.WRITE_EXTERNAL_STORAGE가 접근 승낙 상태 일때
                //Show message
                Toast.makeText(MenuBoardActivity.this, "접근승낙", Toast.LENGTH_SHORT).show();

            } else{
            //Manifest.permission.WRITE_EXTERNAL_STORAGE가 접근 거절 상태 일때
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},0);
                //Show message
                Toast.makeText(MenuBoardActivity.this, "접근거절", Toast.LENGTH_SHORT).show();

            }

            //Show message
            Toast.makeText(MenuBoardActivity.this, "파일 저장 시작", Toast.LENGTH_SHORT).show();

            try {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                byte[] imageBytes = byteArrayOutputStream.toByteArray();

                //Save image
                File file = new File(getExternalPath() + "/cropped.jpg");
                FileOutputStream fileOutput = null;
                fileOutput = new FileOutputStream(file);
                fileOutput.write(imageBytes);
                fileOutput.close();

                //Show message
                Toast.makeText(MenuBoardActivity.this, "파일 저장 성공", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                //Show message
                Toast.makeText(MenuBoardActivity.this, "파일 저장 실패", Toast.LENGTH_SHORT).show();

                e.printStackTrace();
            }

            return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("LongLogTag")
    public void uploadImage(Uri uri) {
        if (uri != null) {
            try {
                // scale the image to save on bandwidth
                bitmap =
                        scaleBitmapDown(
                                MediaStore.Images.Media.getBitmap(getContentResolver(), uri),
                                2000);
                callCloudVision(bitmap);
                main_img.setImageBitmap(bitmap);


            } catch (IOException e) {
                Log.d("MenuBoardActivity:uploadImage()", "Image picking failed because " + e.getMessage());
                Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
            }
        } else {
            Log.d("MenuBoardActivity:uploadImage()", "Image picker gave us a null image.");
            Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
        }
    }

    private void callCloudVision(final Bitmap bitmap) throws IOException{
        loading_str.setText("uploading image");
        //Do the work in an async task
        new AsyncTask<Object, Void, List<GoogleCloudVisionV1AnnotateImageResponse> >(){
            @Override
            protected List<GoogleCloudVisionV1AnnotateImageResponse> doInBackground(Object... objects) {

                HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
                JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

                //Access Google Translation
                Translate translate;
                translate = new Translate.Builder(httpTransport, jsonFactory, null)
                        .setGoogleClientRequestInitializer(new TranslateRequestInitializer(API_KEY))
                        .setApplicationName("OurMenu").build();

                //Access Google Cloud Vision
                try {
                    VisionRequestInitializer requestInitializer =
                            new VisionRequestInitializer(API_KEY) {
                                /**
                                 * We override this so we can inject important identifying fields into the HTTP
                                 * headers. This enables use of a restricted cloud platform API key.
                                 */
                                @Override
                                protected void initializeVisionRequest(VisionRequest<?> visionRequest)
                                        throws IOException {
                                    super.initializeVisionRequest(visionRequest);

                                    String packageName = getPackageName();
                                    visionRequest.getRequestHeaders().set(ANDROID_PACKAGE_HEADER, packageName);

                                    String sig = PackageManagerUtils.getSignature(getPackageManager(), packageName);

                                    visionRequest.getRequestHeaders().set(ANDROID_CERT_HEADER, sig);
                                }
                            };

                    Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
                    builder.setVisionRequestInitializer(requestInitializer);

                    Vision vision = builder.build();

                    GoogleCloudVisionV1BatchAnnotateImagesRequest batchAnnotateImagesRequest =
                            new GoogleCloudVisionV1BatchAnnotateImagesRequest();

                    batchAnnotateImagesRequest.setRequests(new ArrayList<GoogleCloudVisionV1AnnotateImageRequest>() {{
                        GoogleCloudVisionV1AnnotateImageRequest annotateImageRequest = new GoogleCloudVisionV1AnnotateImageRequest();

                        // Add the image
                        GoogleCloudVisionV1Image base64EncodedImage = new GoogleCloudVisionV1Image();
                        // Convert the bitmap to a JPEG
                        // Just in case it's a format that Android understands but Cloud Vision
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
                        byte[] imageBytes = byteArrayOutputStream.toByteArray();

                        // Base64 encode the JPEG
                        base64EncodedImage.encodeContent(imageBytes);
                        annotateImageRequest.setImage(base64EncodedImage);

                        // add the features we want
                        annotateImageRequest.setFeatures(new ArrayList<GoogleCloudVisionV1Feature>() {{
                            GoogleCloudVisionV1Feature textDetection = new GoogleCloudVisionV1Feature();
                            textDetection.setType("TEXT_DETECTION");
                            textDetection.setMaxResults(10);
                            add(textDetection);
                        }});

                        // Add the list of one thing to the request
                        add(annotateImageRequest);
                    }});

                    Vision.Images.Annotate annotateRequest =
                            vision.images().annotate(batchAnnotateImagesRequest);
                    // Due to a bug: requests to Vision API containing large images fail when GZipped.
                    annotateRequest.setDisableGZipContent(true);
                    Log.d(TAG, "created Cloud Vision request object, sending request");

                    GoogleCloudVisionV1BatchAnnotateImagesResponse response = annotateRequest.execute();
                    //loading_str.setText("Executing OCR");

                    /*-------*/
                    //Get each text element and its positions

                    List<GoogleCloudVisionV1AnnotateImageResponse> responses = response.getResponses();
                    boolean startFlag = false;

                    //Get full detected text
                    String target_txt = responses.get(0).getTextAnnotations().get(0).getDescription();
                    List<String> target_list = new ArrayList<>();
                    target_list.add(target_txt);

                    //Do translation for each detected text
                    Translate.Translations.List translationreq = translate.translations().list(target_list, "ko");
                    TranslationsListResponse translationsResponse = translationreq.execute();
                    translationsResponseList = translationsResponse.getTranslations();

                    //Get full translated text
                    String str = translationsResponseList.get(0).getTranslatedText();

                    //Split text with " "
                    StringTokenizer st = new StringTokenizer(str, " ");
                    translated_txt.add(st.nextToken());
                    while(st.hasMoreTokens()){
                        translated_txt.add(st.nextToken());
                    }

                    return responses;

                } catch (GoogleJsonResponseException e) {
                    Log.d(TAG, "failed to make API request because " + e.getContent());
                } catch (IOException e) {
                    Log.d(TAG, "failed to make API request because of other IOException " +
                            e.getMessage());
                }

                return null;

            }
            protected void onPostExecute(List<GoogleCloudVisionV1AnnotateImageResponse> result){
                loading_str.setText("OCR finished");
                boolean startFlag = false;
                int idx = 0;

                //drawing text
                Canvas canvas = new Canvas(bitmap);
                canvas.drawARGB(175,0,0,0);
                Paint pen = new Paint();
                pen.setStyle(Paint.Style.STROKE);
                pen.setStrokeWidth(3);
                //pen.setTextSize((int)getTextSize(result.get(1).getTextAnnotations().get(1).getBoundingPoly()));
                pen.setColor(Color.YELLOW);
                pen.setStrokeCap(Paint.Cap.BUTT);
                pen.setStrokeJoin(Paint.Join.MITER);


                for (GoogleCloudVisionV1AnnotateImageResponse res : result) {

                    // For full list of available annotations, see http://g.co/cloud/vision/docs
                    for (GoogleCloudVisionV1EntityAnnotation annotation : res.getTextAnnotations()) {
                        if(!startFlag){
                            startFlag = true;
                            continue;
                        }
                        if(idx >= translated_txt.size()) idx--;
                        String str = translated_txt.get(idx);

                        /*
                        List<String> target_list = new ArrayList<>();
                        target_list.add(annotation.getDescription());

                        //Do translation for each detected text
                        try {
                            Translate.Translations.List translationreq = translate.translations().list(target_list, "ko");
                            TranslationsListResponse translationsResponse = translationreq.execute();
                            translationsResponseList = translationsResponse.getTranslations();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                        //Get full translated text
                        String str = translationsResponseList.get(0).getTranslatedText();*/
                        List<GoogleCloudVisionV1Vertex> vertex = annotation.getBoundingPoly().getVertices();
                        pen.setTextSize((int)getTextSize(vertex) + 5);
                        out.printf("Text: %s\nPosition : ", str);
                        canvas.drawTextOnPath(str, getPath(vertex),0,0, pen);
                        idx++;
                    }
                }
            }
        }.execute();
    }

    private double getTextSize(List<GoogleCloudVisionV1Vertex> bound) {
        int x1=0, x2=0, y1=0, y2=0;
        double size=0;

        x1 = bound.get(0).getX();
        x2 = bound.get(3).getX();
        y1 = bound.get(0).getY();
        y2 = bound.get(3).getY();

        size = Math.sqrt(((x1 - x2) * (x1 - x2)) + ((y1 - y2) * (y1 - y2)));

        return size;
    }

    public Path getPath(List<GoogleCloudVisionV1Vertex> vertices){

        int x1=0, x2=0, y1=0, y2=0;
        List<Integer> pointsX = new ArrayList<>();
        List<Integer> pointsY = new ArrayList<>();
        Path path = new Path();

        for (GoogleCloudVisionV1Vertex vertex : vertices){
            out.printf("%s ", vertex);
            pointsX.add(vertex.getX());
            pointsY.add(vertex.getY());
        }out.print("\n");
        x1 = (pointsX.get(0) + pointsX.get(3)) / 2;
        x2 = (pointsX.get(1) + pointsX.get(2)) / 2;
        y1 = (pointsY.get(0) + pointsY.get(3)) / 2;
        y2 = (pointsY.get(1) + pointsY.get(2)) / 2;

        path.moveTo(x1, y1);
        path.lineTo(x2, y2);

        return path;
    }

    public Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {

        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;

        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }


    private String convertResponseToString(GoogleCloudVisionV1BatchAnnotateImagesResponse response) {
        String message = "I found these things:\n\n";

        List<GoogleCloudVisionV1EntityAnnotation> labels = response.getResponses().get(0).getTextAnnotations();
        if (labels != null) {
            message += labels.get(0).getDescription();
        } else {
            message += "nothing";
        }

        return message;
    }

    private String getRealPathFromURI(Uri contentUri) {
        if (contentUri.getPath().startsWith("/storage")) {
            return contentUri.getPath(); }
        String id = DocumentsContract.getDocumentId(contentUri).split(":")[1];
        String[] columns = { MediaStore.Files.FileColumns.DATA };
        String selection = MediaStore.Files.FileColumns._ID + " = " + id;
        Cursor cursor = getContentResolver().query(MediaStore.Files.getContentUri("external"), columns, selection, null, null);
        try {
            int columnIndex = cursor.getColumnIndex(columns[0]);
            if (cursor.moveToFirst()) {
                return cursor.getString(columnIndex);
            }
        } finally {
            cursor.close();
        }
        return null;
    }

}
