package com.example.kyungimlee.ourmenu;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
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
import com.google.api.services.translate.model.TranslationsListResponse;
import com.google.api.services.translate.model.TranslationsResource;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequest;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.GoogleCloudVisionV1AnnotateImageRequest;
import com.google.api.services.vision.v1.model.GoogleCloudVisionV1AnnotateImageResponse;
import com.google.api.services.vision.v1.model.GoogleCloudVisionV1BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.GoogleCloudVisionV1BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.GoogleCloudVisionV1Block;
import com.google.api.services.vision.v1.model.GoogleCloudVisionV1BoundingPoly;
import com.google.api.services.vision.v1.model.GoogleCloudVisionV1EntityAnnotation;
import com.google.api.services.vision.v1.model.GoogleCloudVisionV1Feature;
import com.google.api.services.vision.v1.model.GoogleCloudVisionV1Image;
import com.google.api.services.vision.v1.model.GoogleCloudVisionV1Page;
import com.google.api.services.vision.v1.model.GoogleCloudVisionV1Paragraph;
import com.google.api.services.vision.v1.model.GoogleCloudVisionV1Symbol;
import com.google.api.services.vision.v1.model.GoogleCloudVisionV1TextAnnotation;
import com.google.api.services.vision.v1.model.GoogleCloudVisionV1Vertex;
import com.google.api.services.vision.v1.model.GoogleCloudVisionV1Word;
import com.snatik.polygon.Point;
import com.snatik.polygon.Polygon;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.primitives.Ints.max;
import static java.lang.System.out;

public class MenuBoardActivity extends AppCompatActivity {

    private static final String API_KEY = "AIzaSyDON76sNwTcC2AuXU2L_y31z7BtHYP74Ko";
    private static final String ANDROID_CERT_HEADER = "X-Android-Cert";
    private static final String ANDROID_PACKAGE_HEADER = "X-Android-Package";
    public static final int WRITE_PERMISSIONS_REQUEST = 4;
    private static final int FROM_CROPPING = 0;
    private static final int FROM_SAVED = 1;

    //detected break type
    private static String DETECTED_BREAK;
    private static final String SPACE = "SPACE";
    private static final String SURE_SPACE = "SURE_SPACE";
    private static final String EOL_SURE_SPACE = "EOL_SURE_SPACE";
    private static final String LINE_BREAK = "LINE_BREAK";

    private static final String TAG = MainActivity.class.getSimpleName();
    private String choice = "";
    private String detected_lang = "";
    private ImageView main_view;
    private Bitmap original_bitmap;
    private Bitmap boundary_bitmap;
    protected Bitmap cur_bitmap;
    private TextView loading_str;
    protected Canvas bit_canvas;

    private List<String> target_txt = new ArrayList<>();
    private List<TranslationsResource> translated_txt = new ArrayList<>();
    private List<List<GoogleCloudVisionV1Vertex> > paraVertices = new ArrayList<List<GoogleCloudVisionV1Vertex> >(); // Set of vertices of paragraph
    private List<List<GoogleCloudVisionV1Vertex> > wordVertices = new ArrayList<List<GoogleCloudVisionV1Vertex> >(); // Set of vertices of paragraph
    protected List<GoogleCloudVisionV1Vertex> curVertex = new ArrayList<>();
    protected List<GoogleCloudVisionV1AnnotateImageResponse> annotationList = new ArrayList<>();
    protected List<Point> points = new ArrayList<>();
    List<Polygon> wordPolygons = new ArrayList<>();
    protected int offsetX = 0;
    protected int offsetY = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_board);

        //set image view
        main_view = (ImageView) findViewById(R.id.menu_img);
        loading_str = (TextView) findViewById(R.id.loading_str);

        //accept information from a previous activity
        Bundle uridata = getIntent().getExtras();
        if(uridata == null){
            return;
        }

        //get image uri
        String struri = uridata.getString("imageUri");
        Uri uri = Uri.parse(struri);

        //check if an user chose the language to get service
        //default language is "Korean"
        isLanguageChosen();

        //save original bitmapbitmap =
        try {
            original_bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        cur_bitmap = original_bitmap.copy(Bitmap.Config.ARGB_8888,true);;
        //boundary_bitmap = original_bitmap.copy(Bitmap.Config.ARGB_8888, true);
        main_view.setImageBitmap(cur_bitmap);

        switch (uridata.getInt("header")){
            case FROM_CROPPING:
                uploadImage(cur_bitmap);
                break;
            case FROM_SAVED:
                //read file

                //get annotation or whatever else parameter

                //resize the image, attach image to bitmap parameter

                //attach bitmap to main view

                //draw translation or boundaries using annotation file

                break;
        }

        //set touch listener to image view
        main_view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int xPos = 0, yPos = 0;
                int viewHeight = view.getHeight();
                int viewWidth = view.getWidth();
                int mH = view.getMeasuredHeight();
                int mW = view.getMeasuredHeight();
                //When touch event happens, get x y position of the event
                xPos = (int)motionEvent.getX();
                yPos = (int)motionEvent.getY();
                offsetY = (cur_bitmap.getWidth() * yPos) / viewWidth;
                offsetX = (cur_bitmap.getWidth() * xPos) / viewWidth;
                Point curpt = new Point(offsetX, offsetY);

                if(paraVertices != null && wordVertices != null){

                    switch(motionEvent.getAction()){
                        case MotionEvent.ACTION_DOWN:
                            points.add(curpt);
                            break;
                        case MotionEvent.ACTION_MOVE:
                            //움직이는동안은 현재 좌표를 계속 저장
                            points.add(curpt);
                            break;
                        case MotionEvent.ACTION_UP:
                            //드래그가 끝나면 드래그되었던 점들에 대해 단어 polygon들에 속하는지 각각 검사한다
                            //하나의 점에 대해 겹치는 단어박스 찾고 배열리스트에서 삭제
                            //List<Point> tmp = new ArrayList<>();
                            //tmp = points;
                            Map<Integer, String> hashmap = new HashMap<>();
                            for(Point p : points){
                                int idx = 0;
                                for(Polygon pl : wordPolygons){
                                    if(pl.contains(p)){
                                        //해시맵에 인덱스(키) 텍스트(값)으로 저장
                                        hashmap.put(idx, target_txt.get(idx));
                                        idx++;
                                        break;
                                    }else idx++;
                                }
                            }
                            show(hashmap);
                            hashmap.clear();
                            points.clear();
                            break;
                    }
                }
                return true;
            }
        });

    }

    public void show(Map<Integer, String> map){
        String str = "";
        Set<Integer> keySet = map.keySet(); // keySet 얻기
        Iterator<Integer> keyIterator = keySet.iterator();
        while(keyIterator.hasNext()) {
            Integer key = keyIterator.next();
            String value = map.get(key);
            //drawBoundary(wordVertices.get(key), 10, Color.RED);
            str = str + " " + value;
        }
        map.clear();
        Toast.makeText(MenuBoardActivity.this, str, Toast.LENGTH_SHORT).show();
        startResultActivity(str);
        return;
    }

    private void startResultActivity(String str) {
        Intent i = new Intent(this, ResultActivity.class);
        i.putExtra("selectedLang", detected_lang);
        i.putExtra("inputText", str);
        startActivity(i);
    }

    public void Invalidate(int idx){
        Paint pen = new Paint();
        pen.setStyle(Paint.Style.STROKE);
        //pen.setTextSize((int)getTextSize(result.get(1).getTextAnnotations().get(1).getBoundingPoly()));
        pen.setStrokeCap(Paint.Cap.BUTT);
        pen.setStrokeJoin(Paint.Join.MITER);

        //draw word with yellow pen
        pen.setColor(Color.RED);
        pen.setStrokeWidth(20);

        Path path = new Path();
        path.moveTo(0,0);
        path.lineTo(500,500);

        //System.out.printf("Vertices : %d %d\n", paraVertices.get(j).get(0).getX(), paraVertices.get(j).get(0).getY());

        bit_canvas.drawPath(path, pen);
        Toast.makeText(MenuBoardActivity.this, target_txt.get(idx), Toast.LENGTH_SHORT).show();

        return;
    }

    private void isLanguageChosen(){
        //check if an user made language choice
        File langFile = new File(getExternalPath()+"/OurMenu/setting/languageChoice.txt");
        if(langFile.exists()){
            byte[] buffer = readFile(langFile);
            choice = new String(buffer);
        }else{
            //if user didn's select the language
            //Show message
            Toast.makeText(MenuBoardActivity.this, "No lanugage chose. Use korean as a default language", Toast.LENGTH_SHORT).show();
            choice = "ko";
        }
        return;
    }

    /**
     * 파일 읽어 오기
     * @param file
     */
    private byte[] readFile(File file){
        int readcount=0;
        if(file!=null&&file.exists()){
            try {
                FileInputStream fis = new FileInputStream(file);
                readcount = (int)file.length();
                byte[] buffer = new byte[readcount];
                fis.read(buffer);
                for(int i=0 ; i<file.length();i++){
                    Log.d(TAG, ""+buffer[i]);
                }
                fis.close();

                return buffer;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_save) {
            //Ask permission for access to external storage
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            //Manifest.permission.WRITE_EXTERNAL_STORAGE가 접근 승낙 상태 일때
                //Show message
                Toast.makeText(MenuBoardActivity.this, "Permission acquired", Toast.LENGTH_SHORT).show();

            } else{
            //Manifest.permission.WRITE_EXTERNAL_STORAGE가 접근 거절 상태 일때
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},0);
                //Show message
                Toast.makeText(MenuBoardActivity.this, "Access denied", Toast.LENGTH_SHORT).show();

            }

            //Show message
            Toast.makeText(MenuBoardActivity.this, "파일 저장 시작", Toast.LENGTH_SHORT).show();

            try {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                cur_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                byte[] imageBytes = byteArrayOutputStream.toByteArray();

                //Save image
                String date = getDate();
                File file = new File(getExternalPath()+"/OurMenu/res/pictures/pic_" + date + ".jpg");
                FileOutputStream fileOutput = null;
                fileOutput = new FileOutputStream(file);
                fileOutput.write(imageBytes);
                fileOutput.close();

                //Save annotations
                File file2 = new File(getExternalPath()+"/OurMenu/res/annotations/annotation_" + date + ".txt");
                writeFile(file2, annotationList.toString().getBytes());

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

    //get current date, time
    public static String getDate(){
        SimpleDateFormat dateFormat = new  SimpleDateFormat("yyMMdd_HHmm", java.util.Locale.getDefault());
        Date date = new Date();
        String strDate = dateFormat.format(date);
        return strDate;
    }

    @SuppressLint("LongLogTag")
    public void uploadImage(Bitmap bm) {
        if (bm != null) {
            try {
                // scale the image to save on bandwidth
                //Bitmap tmp = scaleBitmapDown(bm, 2000);
                callCloudVision(bm);

            } catch (IOException e) {
                Log.d("MenuBoardActivity:uploadImage()", "Image picking failed because " + e.getMessage());
                Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
            }
        } else {
            Log.d("MenuBoardActivity:uploadImage()", "Image picker gave us a null image.");
            Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 파일에 내용 쓰기
     * @param file
     * @param file_content
     * @return
     */
    private boolean writeFile(File file , byte[] file_content){
        boolean result;
        FileOutputStream fos;
        if(file!=null&&file.exists()&&file_content!=null){
            try {
                fos = new FileOutputStream(file);
                try {
                    fos.write(file_content);
                    fos.flush();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (FileNotFoundException e) {
                Toast.makeText(MenuBoardActivity.this, "Failed to write file", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
            result = true;
        }else{
            result = false;
        }
        return result;
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
                            textDetection.setType("DOCUMENT_TEXT_DETECTION");
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
                    List<GoogleCloudVisionV1AnnotateImageResponse> responses = response.getResponses();

                    //Do translation for each paragraph
                    try {
                        Translate.Translations.List translationreq = translate.translations().list(target_txt, choice);
                        TranslationsListResponse translationsResponse = translationreq.execute();

                        //save translated text in an array 'translated_txt'
                        translated_txt = translationsResponse.getTranslations();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    annotationList = responses;

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
                //When OCR is finished
                loading_str.setText("OCR finished");
                drawBoundary(result);

                //Make every word polygon
                try{
                    for(int j = 0; j<wordVertices.size(); j++){
                        int[] x = new int[4];
                        int[] y = new int[4];
                        List<GoogleCloudVisionV1Vertex> vertex = wordVertices.get(j);
                        for(int i = 0; i<4; i++){
                            x[i] = vertex.get(i).getX();
                            y[i] = vertex.get(i).getY();
                        }
                        Polygon poly = Polygon.Builder().addVertex(new Point(x[0], y[0]))
                                .addVertex(new Point(x[1], y[1]))
                                .addVertex(new Point(x[2], y[2]))
                                .addVertex(new Point(x[3], y[3])).build();
                        wordPolygons.add(poly);
                    }
                }catch (IndexOutOfBoundsException e){
                    Log.d("wordVertices idx error", "loop failed because " + e.getMessage());
                    Toast.makeText(getApplicationContext(), "Please try OCR again", Toast.LENGTH_LONG).show();
                }
                String dl = result.get(0).getFullTextAnnotation().getPages().get(0).getProperty().getDetectedLanguages().get(0).getLanguageCode();
                if(dl == "en"){
                    detected_lang = "English";
                }else detected_lang = "Others";
            }
        }.execute();
    }
    private void drawBoundary(List<GoogleCloudVisionV1AnnotateImageResponse> result) {

        boolean startFlag = false;
        int idx = 0;

        //drawing text
        Canvas canvas = new Canvas(cur_bitmap);
        canvas.drawARGB(75,0,0,0);
        Paint pen = new Paint();
        pen.setStyle(Paint.Style.STROKE);
        //pen.setTextSize((int)getTextSize(result.get(1).getTextAnnotations().get(1).getBoundingPoly()));
        pen.setStrokeCap(Paint.Cap.BUTT);
        pen.setStrokeJoin(Paint.Join.MITER);

        for(GoogleCloudVisionV1AnnotateImageResponse res : result){
            for(GoogleCloudVisionV1Page page : res.getFullTextAnnotation().getPages()){
                for(GoogleCloudVisionV1Block block : page.getBlocks()){

                    for(GoogleCloudVisionV1Paragraph paragraph : block.getParagraphs()){

                        //initialize paragraph string
                        String para_txt = "";

                        for(GoogleCloudVisionV1Word word : paragraph.getWords()){
                            //draw word with yellow pen
                            pen.setColor(Color.YELLOW);
                            pen.setStrokeWidth(3);

                            List<GoogleCloudVisionV1Vertex> vertex = word.getBoundingBox().getVertices();
                            wordVertices.add(vertex);
                            canvas.drawPath(getRectPath(vertex), pen);

                            //initialize word txt
                            String word_txt = "";

                            //add words to paragraph
                            for(GoogleCloudVisionV1Symbol symbol : word.getSymbols()){
                                word_txt += symbol.getText();
                            }
                            //save each word txt
                            target_txt.add(word_txt);
                        }
                    }
                }
            }
        }


    }
    private void _drawBoundary(List<GoogleCloudVisionV1Vertex> vertex, int strokeWidth, int color) {

        boolean startFlag = false;
        int idx = 0;

        //Always draw things on cur_bitmap
        //cur_bitmap = boundary_bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(cur_bitmap);

        if(strokeWidth == 3)
            canvas.drawARGB(75,0,0,0);

        Paint pen = new Paint();
        pen.setStyle(Paint.Style.STROKE);
        //pen.setTextSize((int)getTextSize(result.get(1).getTextAnnotations().get(1).getBoundingPoly()));
        pen.setStrokeCap(Paint.Cap.BUTT);
        pen.setStrokeJoin(Paint.Join.MITER);

        //draw word with yellow pen
        pen.setColor(color);
        pen.setStrokeWidth(strokeWidth);

        canvas.drawPath(getRectPath(vertex), pen);

    }
    private void callParaRectCloudVision(final Bitmap bitmap) throws IOException{
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
                            textDetection.setType("DOCUMENT_TEXT_DETECTION");
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
                    List<GoogleCloudVisionV1AnnotateImageResponse> responses = response.getResponses();

                    boolean startFlag = false;

                    Canvas canvas = new Canvas(bitmap);
                    canvas.drawARGB(175,0,0,0);
                    Paint pen = new Paint();
                    pen.setStyle(Paint.Style.STROKE);
                    pen.setStrokeWidth(3);
                    //pen.setTextSize((int)getTextSize(result.get(1).getTextAnnotations().get(1).getBoundingPoly()));
                    pen.setColor(Color.YELLOW);
                    pen.setStrokeCap(Paint.Cap.BUTT);
                    pen.setStrokeJoin(Paint.Join.MITER);
                    List<String> target_list = new ArrayList<>();
                    List<TranslationsResource> translationsResponseList = new ArrayList<>();
                    List<List<GoogleCloudVisionV1Vertex> > vertex = new ArrayList<List<GoogleCloudVisionV1Vertex>>();
                    boolean start = false;
/*
                    for(GoogleCloudVisionV1AnnotateImageResponse res : responses){
                        for(GoogleCloudVisionV1EntityAnnotation entitiy : res.getTextAnnotations()){
                            if(!start){
                                start = true;
                                continue;
                            }
                            target_list.add(entitiy.getDescription());
                            vertex.add(entitiy.getBoundingPoly().getVertices());
                        }
                    }
*/
                    for(GoogleCloudVisionV1AnnotateImageResponse res : responses){
                        for(GoogleCloudVisionV1Page page : res.getFullTextAnnotation().getPages()){
                            for(GoogleCloudVisionV1Block block : page.getBlocks()){
                                for(GoogleCloudVisionV1Paragraph paragraph : block.getParagraphs()){
                                    vertex.add(paragraph.getBoundingBox().getVertices());
                                    String parastr = "";
                                    for(GoogleCloudVisionV1Word word : paragraph.getWords()){
                                        String wordstr = "";
                                        for(GoogleCloudVisionV1Symbol symbol : word.getSymbols()){
                                            wordstr += symbol.getText();
                                            if(symbol.getProperty().getDetectedBreak() != null){
                                                DETECTED_BREAK = symbol.getProperty().getDetectedBreak().getType();
                                                System.out.printf("word %s break %s\n", wordstr, symbol.getProperty().getDetectedBreak().getType());
                                                switch (DETECTED_BREAK){
                                                    case SPACE:
                                                        wordstr += " ";
                                                        break;
                                                    case SURE_SPACE:
                                                        wordstr += "        ";
                                                        break;
                                                    case LINE_BREAK:
                                                        wordstr += " \n ";
                                                        break;
                                                    case EOL_SURE_SPACE:
                                                        wordstr += " \n ";
                                                        break;
                                                }
                                            }
                                        }
                                        parastr += wordstr;
                                    }
                                    target_list.add(parastr);
                                }
                            }
                        }
                    }

                    //execute translation for a paragraph
                    try {
                        Translate.Translations.List translationreq = translate.translations().list(target_list, choice);
                        TranslationsListResponse translationsResponse = translationreq.execute();
                        translationsResponseList = translationsResponse.getTranslations();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }int idx = 0;
                    for(TranslationsResource ress : translationsResponseList){
                        out.printf("Translated : %s\n", ress.getTranslatedText());

                        //get vertices of translated text
                        List<GoogleCloudVisionV1Vertex> vertices = vertex.get(idx);
                        pen.setTextSize((float) 40);
                        //pen.setTextSize((int)_getTextSize(vertices, ress.getTranslatedText()));
/*
                        //Using multiline drawing
                        pen.setTextSize((float) 40);
                        String paragraph = ress.getTranslatedText();
                        int x= vertices.get(0).getX();
                        int y= vertices.get(0).getY();
                        for(String line : paragraph.split("\n")){
                            canvas.drawText(line, x, y, pen);
                            y += pen.ascent() + pen.descent();
                        }

*/
                        //Using Static Layout
                        //Draw translation in the bounding box
                        RectF rect = new RectF(vertices.get(0).getX().floatValue(),
                                vertices.get(0).getY().floatValue(),
                                vertices.get(2).getX().floatValue(),
                                vertices.get(2).getY().floatValue());


                        StaticLayout sl = new StaticLayout(ress.getTranslatedText(), new TextPaint(pen), (int)rect.width(), Layout.Alignment.ALIGN_NORMAL, 1, 1, false);

                        canvas.save();
                        canvas.translate(rect.left, rect.top);
                        sl.draw(canvas);
                        canvas.restore();

                        //canvas.drawTextOnPath(translationsResponseList.get(idx).getTranslatedText(), getPath(vertices),0,0, pen);
                        idx++;
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
/*
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


                        List<String> target_list = new ArrayList<>();
                        target_list.add(annotation.getDescription());

                        //Get full translated text
                        List<GoogleCloudVisionV1Vertex> vertex = annotation.getBoundingPoly().getVertices();
                        pen.setTextSize((int)getTextSize(vertex) + 5);
                        out.printf("Text: %s\nPosition : ", annotation.getDescription());
                        canvas.drawTextOnPath(str, getPath(vertex),0,0, pen);
                        idx++;
                    }
                }*/
            }
        }.execute();
    }
    private void callBackupCloudVision(final Bitmap bitmap) throws IOException{
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
                            textDetection.setType("DOCUMENT_TEXT_DETECTION");
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
                    List<GoogleCloudVisionV1AnnotateImageResponse> responses = response.getResponses();

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

            }
        }.execute();
    }
    private void callRectCloudVision(final Bitmap bitmap) throws IOException{
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
                    List<GoogleCloudVisionV1AnnotateImageResponse> responses = response.getResponses();

                    boolean startFlag = false;

                    Canvas canvas = new Canvas(bitmap);
                    canvas.drawARGB(175,0,0,0);
                    Paint pen = new Paint();
                    pen.setStyle(Paint.Style.STROKE);
                    pen.setStrokeWidth(3);
                    //pen.setTextSize((int)getTextSize(result.get(1).getTextAnnotations().get(1).getBoundingPoly()));
                    pen.setColor(Color.YELLOW);
                    pen.setStrokeCap(Paint.Cap.BUTT);
                    pen.setStrokeJoin(Paint.Join.MITER);
                    List<String> target_list = new ArrayList<>();
                    List<TranslationsResource> translationsResponseList = new ArrayList<>();
                    List<List<GoogleCloudVisionV1Vertex> > vertex = new ArrayList<List<GoogleCloudVisionV1Vertex>>();
                    boolean start = false;

                    for(GoogleCloudVisionV1AnnotateImageResponse res : responses){
                        for(GoogleCloudVisionV1EntityAnnotation entitiy : res.getTextAnnotations()){
                            if(!start){
                                start = true;
                                continue;
                            }
                            target_list.add(entitiy.getDescription());
                            vertex.add(entitiy.getBoundingPoly().getVertices());
                        }
                    }

                    //execute translation for a word
                    try {
                        Translate.Translations.List translationreq = translate.translations().list(target_list, choice);
                        TranslationsListResponse translationsResponse = translationreq.execute();
                        translationsResponseList = translationsResponse.getTranslations();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }int idx = 0;
                    for(TranslationsResource ress : translationsResponseList){
                        out.printf("Translated : %s\n", ress.getTranslatedText());

                        //draw translated text
                        List<GoogleCloudVisionV1Vertex> vertices = vertex.get(idx);
                        pen.setTextSize((int)getTextSize(vertices)-10);
                        //pen.setTextSize((float) 30);

                        //Draw translation in the bounding box
                        RectF rect = new RectF(vertices.get(0).getX().floatValue(),
                                vertices.get(0).getY().floatValue(),
                                vertices.get(2).getX().floatValue(),
                                vertices.get(2).getY().floatValue());


                        StaticLayout sl = new StaticLayout(translationsResponseList.get(idx).getTranslatedText(), new TextPaint(pen), (int)rect.width(), Layout.Alignment.ALIGN_CENTER, 1, 1, false);

                        canvas.save();
                        canvas.translate(rect.left, rect.top);
                        sl.draw(canvas);
                        canvas.restore();

                        //canvas.drawTextOnPath(translationsResponseList.get(idx).getTranslatedText(), getPath(vertices),0,0, pen);
                        idx++;
                    }

                    //int idx = 0;
                    for(GoogleCloudVisionV1AnnotateImageResponse res : responses){
                        for(GoogleCloudVisionV1EntityAnnotation entity : res.getTextAnnotations()){
                            if(!start){
                                start = true;
                                continue;
                            }
                            //draw translated text
                            //List<GoogleCloudVisionV1Vertex> vertex = entity.getBoundingPoly().getVertices();
                            //pen.setTextSize((int)getTextSize(vertex));
                            //out.printf("Text: %s\nPosition : ", entity.getDescription());
                            //canvas.drawTextOnPath(translationsResponseList.get(idx).getTranslatedText(), getPath(vertex),0,0, pen);
                            idx++;
                        }
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
/*
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


                        List<String> target_list = new ArrayList<>();
                        target_list.add(annotation.getDescription());

                        //Get full translated text
                        List<GoogleCloudVisionV1Vertex> vertex = annotation.getBoundingPoly().getVertices();
                        pen.setTextSize((int)getTextSize(vertex) + 5);
                        out.printf("Text: %s\nPosition : ", annotation.getDescription());
                        canvas.drawTextOnPath(str, getPath(vertex),0,0, pen);
                        idx++;
                    }
                }*/
            }
        }.execute();
    }
    private void callPathCloudVision(final Bitmap bitmap) throws IOException{
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
                    List<GoogleCloudVisionV1AnnotateImageResponse> responses = response.getResponses();

                    boolean startFlag = false;

                    Canvas canvas = new Canvas(bitmap);
                    canvas.drawARGB(175,0,0,0);
                    Paint pen = new Paint();
                    pen.setStyle(Paint.Style.STROKE);
                    pen.setStrokeWidth(3);
                    //pen.setTextSize((int)getTextSize(result.get(1).getTextAnnotations().get(1).getBoundingPoly()));
                    pen.setColor(Color.YELLOW);
                    pen.setStrokeCap(Paint.Cap.BUTT);
                    pen.setStrokeJoin(Paint.Join.MITER);
                    List<String> target_list = new ArrayList<>();
                    List<TranslationsResource> translationsResponseList = new ArrayList<>();
                    List<List<GoogleCloudVisionV1Vertex> > vertex = new ArrayList<List<GoogleCloudVisionV1Vertex>>();
                    boolean start = false;

                    for(GoogleCloudVisionV1AnnotateImageResponse res : responses){
                        for(GoogleCloudVisionV1EntityAnnotation entitiy : res.getTextAnnotations()){
                            if(!start){
                                start = true;
                                continue;
                            }
                            target_list.add(entitiy.getDescription());
                            vertex.add(entitiy.getBoundingPoly().getVertices());
                        }
                    }

                    //execute translation for a word
                    try {
                        Translate.Translations.List translationreq = translate.translations().list(target_list, "ko");
                        TranslationsListResponse translationsResponse = translationreq.execute();
                        translationsResponseList = translationsResponse.getTranslations();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }int idx = 0;
                    for(TranslationsResource ress : translationsResponseList){
                        out.printf("Translated : %s\n", ress.getTranslatedText());

                        //draw translated text
                        List<GoogleCloudVisionV1Vertex> vertices = vertex.get(idx);
                        pen.setTextSize((int)getTextSize(vertices));
                        canvas.drawTextOnPath(translationsResponseList.get(idx).getTranslatedText(), getPath(vertices),0,0, pen);idx++;
                    }

                    //int idx = 0;
                    for(GoogleCloudVisionV1AnnotateImageResponse res : responses){
                        for(GoogleCloudVisionV1EntityAnnotation entity : res.getTextAnnotations()){
                            if(!start){
                                start = true;
                                continue;
                            }
                            //draw translated text
                            //List<GoogleCloudVisionV1Vertex> vertex = entity.getBoundingPoly().getVertices();
                            //pen.setTextSize((int)getTextSize(vertex));
                            //out.printf("Text: %s\nPosition : ", entity.getDescription());
                            //canvas.drawTextOnPath(translationsResponseList.get(idx).getTranslatedText(), getPath(vertex),0,0, pen);
                            idx++;
                        }
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
/*
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


                        List<String> target_list = new ArrayList<>();
                        target_list.add(annotation.getDescription());

                        //Get full translated text
                        List<GoogleCloudVisionV1Vertex> vertex = annotation.getBoundingPoly().getVertices();
                        pen.setTextSize((int)getTextSize(vertex) + 5);
                        out.printf("Text: %s\nPosition : ", annotation.getDescription());
                        canvas.drawTextOnPath(str, getPath(vertex),0,0, pen);
                        idx++;
                    }
                }*/
            }
        }.execute();
    }
    private void callDocumentCloudVision(final Bitmap bitmap) throws IOException{
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
                            textDetection.setType("DOCUMENT_TEXT_DETECTION");
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
                    List<GoogleCloudVisionV1AnnotateImageResponse> responses = response.getResponses();

                    boolean startFlag = false;

                    Canvas canvas = new Canvas(bitmap);
                    canvas.drawARGB(175,0,0,0);

                    for(GoogleCloudVisionV1AnnotateImageResponse r : responses){
                        GoogleCloudVisionV1TextAnnotation textAnnotation = r.getFullTextAnnotation();
                        for(GoogleCloudVisionV1Page page : textAnnotation.getPages()){
                            String pageTxt = "";
                            out.println(page.getProperty().getDetectedBreak());
                            out.println(page.getProperty().getDetectedLanguages());
                            for(GoogleCloudVisionV1Block block : page.getBlocks()){
                                String blockTxt = "";
                                for(GoogleCloudVisionV1Paragraph paragraph : block.getParagraphs()){
                                    String paraTxt = "";
                                    List<TranslationsResource> translationsResponseList = new ArrayList<>();
                                    List<String> target_list = new ArrayList<>();
                                    for(GoogleCloudVisionV1Word word : paragraph.getWords()){
                                        String wordTxt = "";
                                        GoogleCloudVisionV1BoundingPoly wordBound = new GoogleCloudVisionV1BoundingPoly();
                                        for(GoogleCloudVisionV1Symbol symbol : word.getSymbols()){

                                            //make word text using symbols
                                            wordTxt += symbol.getText();
                                            //out.printf("\nBreak Type: %s", symbol.getProperty().getDetectedBreak().getType());

                                            //calculate 1 word bound
                                            //wordBound = addBoundary(wordBound.getVertices(), symbol.getBoundingBox().getVertices());
                                        }
                                        target_list.add(paraTxt);
                                        paraTxt = paraTxt + " " + wordTxt;
                                    }
                                    out.printf("Paragraph: %s\n", paraTxt);
                                    out.printf("Bounds: %s\n", paragraph.getBoundingBox());

                                    //Do translation for each detected paragraph
                                    try {
                                        Translate.Translations.List translationreq = translate.translations().list(target_list, "zh");
                                        TranslationsListResponse translationsResponse = translationreq.execute();
                                        translationsResponseList = translationsResponse.getTranslations();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                    //Draw translation in the bounding box
                                    RectF rect = new RectF(paragraph.getBoundingBox().getVertices().get(0).getX().floatValue(),
                                            paragraph.getBoundingBox().getVertices().get(0).getY().floatValue(),
                                            paragraph.getBoundingBox().getVertices().get(2).getX().floatValue(),
                                            paragraph.getBoundingBox().getVertices().get(2).getY().floatValue());

                                    Paint pen = new Paint();
                                    pen.setStyle(Paint.Style.STROKE);
                                    pen.setStrokeWidth(3);
                                    //pen.setTextSize((int)getTextSize(result.get(1).getTextAnnotations().get(1).getBoundingPoly()));
                                    pen.setColor(Color.YELLOW);
                                    pen.setStrokeCap(Paint.Cap.BUTT);
                                    pen.setStrokeJoin(Paint.Join.MITER);


                                    //Get full translated text
                                    //pen.setTextSize((int)getTextSize(vertex) + 5);
                                    pen.setTextSize(45);

                                    StaticLayout sl = new StaticLayout(paraTxt, new TextPaint(pen), (int)rect.width(), Layout.Alignment.ALIGN_CENTER, 1, 1, false);

                                    canvas.save();
                                    canvas.translate(rect.left, rect.top);
                                    sl.draw(canvas);
                                    canvas.restore();
                                    //drawTranslation(translationsResponseList.get(0).getTranslatedText(), );
                                    blockTxt = blockTxt + "\n" + paraTxt;
                                }
                                out.printf("Block: %s\n\n", blockTxt);
                                out.printf("BlockBounds: %s\n\n", blockTxt);
                                pageTxt = pageTxt + "\n\n" + blockTxt;
                            }
                        }
                        out.println(textAnnotation.getText());
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
/*
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


                        List<String> target_list = new ArrayList<>();
                        target_list.add(annotation.getDescription());

                        //Get full translated text
                        List<GoogleCloudVisionV1Vertex> vertex = annotation.getBoundingPoly().getVertices();
                        pen.setTextSize((int)getTextSize(vertex) + 5);
                        out.printf("Text: %s\nPosition : ", annotation.getDescription());
                        canvas.drawTextOnPath(str, getPath(vertex),0,0, pen);
                        idx++;
                    }
                }*/
            }
        }.execute();
    }

    private void drawTranslation(String str, List<GoogleCloudVisionV1Vertex> vertex){
        //drawing text
        Canvas canvas = new Canvas(cur_bitmap);
        canvas.drawARGB(175,0,0,0);
        Paint pen = new Paint();
        pen.setStyle(Paint.Style.STROKE);
        pen.setStrokeWidth(3);
        //pen.setTextSize((int)getTextSize(result.get(1).getTextAnnotations().get(1).getBoundingPoly()));
        pen.setColor(Color.YELLOW);
        pen.setStrokeCap(Paint.Cap.BUTT);
        pen.setStrokeJoin(Paint.Join.MITER);


        //Get full translated text
        //pen.setTextSize((int)getTextSize(vertex) + 5);
        pen.setTextSize(45);
        canvas.drawTextOnPath(str, getPath(vertex),0,0, pen);

        return;
    }
    private GoogleCloudVisionV1BoundingPoly addBoundary(List<GoogleCloudVisionV1Vertex> source, List<GoogleCloudVisionV1Vertex> dest){
        GoogleCloudVisionV1BoundingPoly result = new GoogleCloudVisionV1BoundingPoly();
        List<GoogleCloudVisionV1Vertex> vertices = null;

        for(int i = 0; i<4; i++){
            GoogleCloudVisionV1Vertex vertex = new GoogleCloudVisionV1Vertex();
            vertex.setX(max(source.get(i).getX(), dest.get(i).getX()));
            vertex.setY(max(source.get(i).getY(), dest.get(i).getY()));
            vertices.set(i, vertex);
        }
        result.setVertices(vertices);

        return result;
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

    public Path getRectPath(List<GoogleCloudVisionV1Vertex> vertices){

        List<Integer> pointsX = new ArrayList<>();
        List<Integer> pointsY = new ArrayList<>();
        Path path = new Path();

        for (GoogleCloudVisionV1Vertex vertex : vertices){
            pointsX.add(vertex.getX());
            pointsY.add(vertex.getY());
        }out.print("\n");

        path.moveTo(pointsX.get(0), pointsY.get(0));
        path.lineTo(pointsX.get(1), pointsY.get(1));
        path.lineTo(pointsX.get(2), pointsY.get(2));
        path.lineTo(pointsX.get(3), pointsY.get(3));
        path.lineTo(pointsX.get(0), pointsY.get(0));

        return path;
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

}
