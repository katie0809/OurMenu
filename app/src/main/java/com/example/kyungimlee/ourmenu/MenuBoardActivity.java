package com.example.kyungimlee.ourmenu;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
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
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
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
import com.google.api.services.vision.v1.model.GoogleCloudVisionV1TextAnnotationDetectedLanguage;
import com.google.api.services.vision.v1.model.GoogleCloudVisionV1Vertex;
import com.google.api.services.vision.v1.model.GoogleCloudVisionV1Word;
import com.snatik.polygon.Point;
import com.snatik.polygon.Polygon;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.StringTokenizer;
import java.util.TreeMap;

import static java.lang.System.load;
import static java.lang.System.out;

public class MenuBoardActivity extends AppCompatActivity {

    private static final String API_KEY = "AIzaSyDON76sNwTcC2AuXU2L_y31z7BtHYP74Ko";
    private static final String ANDROID_CERT_HEADER = "X-Android-Cert";
    private static final String ANDROID_PACKAGE_HEADER = "X-Android-Package";
    public static final int WRITE_PERMISSIONS_REQUEST = 4;
    private static final int FROM_CROPPING = 0;
    private static final int FROM_SAVED = 1;
    private boolean FILE_SAVED;

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
    protected Bitmap cur_bitmap;
    private TextView loading_str;

    private List<String> target_txt = new ArrayList<>();
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
        FILE_SAVED = false;

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

        //save original bitmap
        try {
            original_bitmap = scaleBitmapDown(MediaStore.Images.Media.getBitmap(getContentResolver(), uri), 600);
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

                //When image is loaded
                loading_str.setText("Image loaded");
                String imagePath = uridata.getString("imagePath");

                try{
                    //convert uri to path
                    //data_path is a path for "wordVertices" file
                    //txt_path is a path for "target_txt" file
                    String data_path = "", txt_path="";
                    StringTokenizer st = new StringTokenizer(imagePath,".");
                    String token = st.nextToken();
                    data_path = token + ".txt";
                    txt_path = token + "_.txt";

                    //data_path = getExternalPath() + "OurMenu/res/pictures/pic_171201_1704.txt";
                    File f = new File(data_path);
                    File txt = new File(txt_path);
                    FileReader fr = null, frr = null;
                    BufferedReader br = null, brr=null;
                    String read = "";
                    Queue<Integer> queue = new LinkedList<>();

                    //read WordVertices
                    fr = new FileReader(f);
                    br = new BufferedReader(fr);
                    while((read = br.readLine()) != null){
                        StringTokenizer stt = new StringTokenizer(read, " ");
                        while(stt.hasMoreTokens()){
                            String vertice = "";
                            vertice = stt.nextToken();
                            queue.add(Integer.parseInt(vertice));
                        }
                        read = "";
                    }
                    fr.close();
                    br.close();
                    read = "";

                    //read target_txt
                    frr = new FileReader(txt);
                    brr = new BufferedReader(frr);
                    while((read=brr.readLine()) != null){
                        target_txt.add(read);
                    }
                    frr.close();
                    brr.close();

                    int loop = queue.size()/8;
                    for(int j = 0; j<loop; j++){
                        List<GoogleCloudVisionV1Vertex> vertices = new ArrayList<>();
                        for(int i = 0; i<4; i++){
                            GoogleCloudVisionV1Vertex vertex = new GoogleCloudVisionV1Vertex();
                            vertex.setX(queue.poll());
                            vertex.setY(queue.poll());
                            vertices.add(i, vertex);
                        }
                        wordVertices.add(j, vertices);
                    }

                    //draw boundaries
                    drawBoundary();

                    //Make every word polygon
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

                    loading_str.setText("file loading finished");


                }catch (IndexOutOfBoundsException e){
                    Log.d("wordVertices idx error", "loop failed because " + e.getMessage());
                    Toast.makeText(getApplicationContext(), "Please try OCR again", Toast.LENGTH_LONG).show();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                break;
        }

        //set click listener to text view

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

                if(wordVertices != null){

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
                            //tmp = points;
                            List<Integer> order = new ArrayList<>();
                            Map<Integer, String> hashmap = new HashMap<>();
                            for(Point p : points){
                                int idx = 0;
                                for(Polygon pl : wordPolygons){
                                    if(pl.contains(p)){
                                        //해시맵에 인덱스(키) 텍스트(값)으로 저장
                                        hashmap.put(idx, target_txt.get(idx));

                                        //순서 저장
                                        /*
                                        if(order.size() > 0){
                                            if(idx != order.get(order.size() - 1)){
                                                order.add(idx);
                                            }
                                        }
                                        else{
                                            order.add(idx);
                                        }*/
                                        idx++;
                                        break;
                                    }else idx++;
                                }
                            }
                            show(hashmap);
                            //show(hashmap, order);
                            hashmap.clear();
                            points.clear();
                            break;
                    }
                }
                return true;
            }
        });

    }

    private String uri_to_path(Uri uri){
        String res = null;
        String[] image_data = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, image_data, null, null,null);
        if(cursor.moveToFirst()){
            int col = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res=cursor.getString(col);
        }
        cursor.close();
        return res;
    }
    public void show(Map<Integer, String> map){
        String str = "";

        /*
        Make string as an order that user swiped.
        If an user swiped backwards, message shows from the last word.

        for(Integer key : order){
            String value = map.get(key);
            //drawBoundary(wordVertices.get(key), 10, Color.RED);
            str = str + " " + value;
        }
         */

        /*
        Make string with an order of Left -> Right, Up -> Down.
        Priority : Up > Left > Right > Down
         */

        Map<Integer, String> ordered_map = new TreeMap<>();
        ordered_map.putAll(map);
        Iterator<Integer> keys = ordered_map.keySet().iterator();
        while(keys.hasNext()){
            Integer key = keys.next();
            String value = ordered_map.get(key);
            str = str + " " + value;
        }

        int tmp = 0;
        map.clear();
        //Toast.makeText(MenuBoardActivity.this, str, Toast.LENGTH_SHORT).show();
        //Show detected text
        loading_str.setText(str);

        //call translation
        List<String> list = new ArrayList<>();
        list.add(str);
        try {
            callCloudTranslation(list);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return;
    }

    private void startResultActivity(String str) {
        Intent i = new Intent(this, ResultActivity.class);
        i.putExtra("selectedLang", detected_lang);
        i.putExtra("inputText", str);
        startActivity(i);
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

        if(FILE_SAVED)
            menu.findItem(R.id.action_save).setVisible(false);

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
            //OCR이 아직 수행되지 않은 경우
            if(wordVertices == null)
                return false;

            //Ask permission for access to external storage
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            //Manifest.permission.WRITE_EXTERNAL_STORAGE가 접근 승낙 상태 일때
                //Show message
                //Toast.makeText(MenuBoardActivity.this, "Permission acquired", Toast.LENGTH_SHORT).show();

            } else{
            //Manifest.permission.WRITE_EXTERNAL_STORAGE가 접근 거절 상태 일때
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},0);
                //Show message
                //Toast.makeText(MenuBoardActivity.this, "Access denied", Toast.LENGTH_SHORT).show();
                loading_str.setText("Permission denied for writing storage");
            }

            //Show message
            //Toast.makeText(MenuBoardActivity.this, "파일 저장 시작", Toast.LENGTH_SHORT).show();

            try {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                original_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
//                cur_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                byte[] imageBytes = byteArrayOutputStream.toByteArray();

                //Save image
                String date = getDate();
                File file = new File(getExternalPath()+"/OurMenu/res/pictures/pic_" + date + ".jpg");
                FileOutputStream fileOutput = null;
                fileOutput = new FileOutputStream(file);
                fileOutput.write(imageBytes);
                fileOutput.close();

                //Save annotations
                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(getExternalPath()+"/OurMenu/res/pictures/pic_" + date + ".txt"));
                BufferedWriter bufferedWriter2 = new BufferedWriter(new FileWriter(getExternalPath()+"/OurMenu/res/pictures/pic_" + date + "_.txt"));

                for(List<GoogleCloudVisionV1Vertex> vertexList : wordVertices){
                    for(GoogleCloudVisionV1Vertex vertex : vertexList){
                        String s = vertex.getX() + " " + vertex.getY();
                        bufferedWriter.write(s);
                        bufferedWriter.newLine();
                    }
                }
                bufferedWriter.close();

                for(String str : target_txt){
                    bufferedWriter2.write(str);
                    bufferedWriter2.newLine();
                }
                bufferedWriter2.close();

                //Show message
                FILE_SAVED = true;
                invalidateOptionsMenu();
                loading_str.setText("File Saved");

            } catch (IOException e) {
                //Show message
                //Toast.makeText(MenuBoardActivity.this, "파일 저장 실패", Toast.LENGTH_SHORT).show();
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
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
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
                    Log.d(TAG, "failed to make API request because of other IOException " + e.getMessage());
                }
                return null;
            }

            protected void onPostExecute(List<GoogleCloudVisionV1AnnotateImageResponse> result){
                //When OCR is finished
                loading_str.setText("OCR finished");
                if(result != null) {
                    drawBoundary(result);
                }
                else{
                    loading_str.setText(R.string.err_msg_ocr);
                    Toast.makeText(getApplicationContext(), "OCR failed, please try again", Toast.LENGTH_LONG).show();
                    return;
                }

                //Make every word polygon
                try{
                    for(int j = 0; j<wordVertices.size(); j++){
                        Integer[] x = new Integer[4];
                        Integer[] y = new Integer[4];
                        List<GoogleCloudVisionV1Vertex> vertex = wordVertices.get(j);
                        for(int i = 0; i<4; i++){
                            x[i] = vertex.get(i).getX();
                            y[i] = vertex.get(i).getY();
                        }
                        for(int i = 0; i<4; i++){
                            if(x[i] == null){
                                switch (i){
                                    case 0:
                                        x[i] = x[3];
                                        break;
                                    case 1:
                                        x[i] = x[2];
                                        break;
                                    case 2:
                                        x[i] = x[1];
                                        break;
                                    case 3:
                                        x[i] = x[0];
                                        break;
                                }
                            }
                            if(y[i] == null){
                                switch (i){
                                    case 0:
                                        y[i] = y[3];
                                        break;
                                    case 1:
                                        y[i] = y[2];
                                        break;
                                    case 2:
                                        y[i] = y[1];
                                        break;
                                    case 3:
                                        y[i] = y[0];
                                        break;
                                }
                            }
                        }
                        Polygon poly = Polygon.Builder().addVertex(new Point(x[0], y[0]))
                                .addVertex(new Point(x[1], y[1]))
                                .addVertex(new Point(x[2], y[2]))
                                .addVertex(new Point(x[3], y[3])).build();
                        wordPolygons.add(poly);
                    }
                }catch (IndexOutOfBoundsException e){
                    Log.d("wordVertices idx error", "loop failed because " + e.getMessage());
                    loading_str.setText(R.string.err_msg_ocr);
                }catch (NullPointerException e){
                    loading_str.setText(R.string.err_msg_nullVertex);
                }
                int idx = 0, maxidx = 0;
                Float maxVal = 0.0f;
                for(GoogleCloudVisionV1TextAnnotationDetectedLanguage lang : result.get(0).getFullTextAnnotation().getPages().get(0).getProperty().getDetectedLanguages()){
                    if(maxVal < lang.getConfidence()){
                        maxVal = lang.getConfidence();
                        maxidx = idx;
                    }
                    idx++;
                }
                String dl = result.get(0).getFullTextAnnotation().getPages().get(0).getProperty().getDetectedLanguages().get(maxidx).getLanguageCode();
                if(dl.compareTo("en") == 0){
                    detected_lang = "English";
                }else detected_lang = "Others";
            }
        }.execute();
    }

    public void callCloudTranslation(final List<String> target) throws IOException{
        new AsyncTask<Object, Void, List<TranslationsResource>>(){
            @Override
            protected List<TranslationsResource> doInBackground(Object... objects) {
                String result = "";

                HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
                JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

                //Access Google Translation
                Translate translate;
                translate = new Translate.Builder(httpTransport, jsonFactory, null)
                        .setGoogleClientRequestInitializer(new TranslateRequestInitializer(API_KEY))
                        .setApplicationName("OurMenu").build();

                List<TranslationsResource> translated_txt = new ArrayList<>();

                //Do translation for each paragraph
                try {
                    Translate.Translations.List translationreq = translate.translations().list(target, choice);
                    TranslationsListResponse translationsResponse = translationreq.execute();

                    //save translated text in an array 'translated_txt'
                    translated_txt = translationsResponse.getTranslations();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return translated_txt;
            }

            @Override
            protected void onPostExecute(List<TranslationsResource> res) {

                String str = "";
                for(TranslationsResource resource : res){
                    str = str + " " + resource.getTranslatedText();
                }
                //translated_str.setText(str);
                showCustomDlg(str);
            }
        }.execute();
    }



    private void showCustomDlg(String result){

        CustomDialog dialog = new CustomDialog(this,loading_str.getText().toString(), result, detected_lang);
        dialog.setDialogListener(new MyDialogListener() {  // MyDialogListener 를 구현
            @Override
            public void onPositiveClicked(String email, String name) {
                Log.d("MyDialogListener","onPositiveClicked");
            }
            @Override
            public void onNegativeClicked() {
                Log.d("MyDialogListener","onNegativeClicked");
            }
        });

        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.gravity = Gravity.BOTTOM | Gravity.FILL_HORIZONTAL;
        dialog.getWindow().setAttributes(params);

        dialog.show();
    }

    private void drawBoundary(){

        boolean startFlag = false;
        int idx = 0;

        //drawing text
        Bitmap tempBitmap = Bitmap.createBitmap(cur_bitmap.getWidth(), cur_bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(tempBitmap);
        canvas.drawBitmap(cur_bitmap, 0, 0, null);
        canvas.drawARGB(75,0,0,0);
        Paint pen = new Paint();
        pen.setStyle(Paint.Style.STROKE);
        pen.setColor(Color.YELLOW);
        pen.setStrokeWidth(1);
        //pen.setTextSize((int)getTextSize(result.get(1).getTextAnnotations().get(1).getBoundingPoly()));
        pen.setStrokeCap(Paint.Cap.BUTT);
        pen.setStrokeJoin(Paint.Join.MITER);

        for(List<GoogleCloudVisionV1Vertex> vertex : wordVertices){
                canvas.drawPath(getRectPath(vertex), pen);
        }
        main_view.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));

    }

    private void drawBoundary(List<GoogleCloudVisionV1AnnotateImageResponse> result) {

        boolean startFlag = false;
        int idx = 0;
        //drawing text
        Bitmap tempBitmap = Bitmap.createBitmap(cur_bitmap.getWidth(), cur_bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(tempBitmap);
        canvas.drawBitmap(cur_bitmap, 0, 0, null);
        canvas.drawARGB(75,0,0,0);
        Paint pen = new Paint();
        pen.setStyle(Paint.Style.STROKE);
        pen.setColor(Color.YELLOW);
        pen.setStrokeWidth(1);
        //pen.setTextSize((int)getTextSize(result.get(1).getTextAnnotations().get(1).getBoundingPoly()));
        pen.setStrokeCap(Paint.Cap.BUTT);
        pen.setStrokeJoin(Paint.Join.MITER);int i = 0;

        for(GoogleCloudVisionV1AnnotateImageResponse res : result){
            for(GoogleCloudVisionV1Page page : res.getFullTextAnnotation().getPages()){
                for(GoogleCloudVisionV1Block block : page.getBlocks()){

                    for(GoogleCloudVisionV1Paragraph paragraph : block.getParagraphs()){

                        //initialize paragraph string
                        String para_txt = "";

                        for(GoogleCloudVisionV1Word word : paragraph.getWords()){

                            List<GoogleCloudVisionV1Vertex> vertex = word.getBoundingBox().getVertices();
                            wordVertices.add(vertex);
                            canvas.drawPath(getRectPath(vertex), pen);
                            //initialize word txt
                            String word_txt = "";

                            System.out.print(i);i++;
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

        main_view.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));

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

    public Path getRectPath(List<GoogleCloudVisionV1Vertex> vertices){

        List<Integer> pointsX = new ArrayList<>();
        List<Integer> pointsY = new ArrayList<>();
        List<Integer> excpX = new ArrayList<>();
        List<Integer> excpY = new ArrayList<>();

        Path path = new Path();

        try {
            for (GoogleCloudVisionV1Vertex vertex : vertices) {
                pointsX.add(vertex.getX());
                pointsY.add(vertex.getY());
            }
            for(int i = 0; i<4; i++){
                if(pointsX.get(i) == null){
                    switch (i){
                        case 0:
                            pointsX.set(i, pointsX.get(3));
                            break;
                        case 1:
                            pointsX.set(i, pointsX.get(2));
                            break;
                        case 2:
                            pointsX.set(i, pointsX.get(1));
                            break;
                        case 3:
                            pointsX.set(i, pointsX.get(0));
                            break;
                    }
                }
                if(pointsY.get(i) == null){
                    switch (i){
                        case 0:
                            pointsY.set(i, pointsY.get(3));
                            break;
                        case 1:
                            pointsY.set(i, pointsY.get(2));
                            break;
                        case 2:
                            pointsY.set(i, pointsY.get(1));
                            break;
                        case 3:
                            pointsY.set(i, pointsY.get(0));
                            break;
                    }
                }
            }
            path.moveTo(pointsX.get(0), pointsY.get(0));
            path.lineTo(pointsX.get(1), pointsY.get(1));
            path.lineTo(pointsX.get(2), pointsY.get(2));
            path.lineTo(pointsX.get(3), pointsY.get(3));
            path.lineTo(pointsX.get(0), pointsY.get(0));
        }catch (NullPointerException e){
            loading_str.setText(R.string.err_msg_nullVertex);
        }
        return path;
    }


    //***************** Currently not used ****************************//

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
