/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.sample.cloudvision;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //public static final String FILE_NAME = "temp.jpg";
    private static final String CLOUD_VISION_API_KEY_FOR_ANDROID = "AIzaSyBn9K0Zb6MhXimFoqufOFJbbQdykuzdiuw";
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int GALLERY_IMAGE_REQUEST = 1;
    private static final int STORAGE_WRITE_REQUEST = 2;
    private static final String Path = Environment.getExternalStorageDirectory().getAbsolutePath() +
            File.separator + "DCIM" + File.separator + "CloudVision";
    private static final String SAVEPATH = Environment.getExternalStorageDirectory().getAbsolutePath()
            + File.separator + "CloudVision";
    private static final String SAVEFILEPATH = "result.json";
    private String temp = "";
    private String finalResult = "";
    private ArrayList<String> itemList = new ArrayList<String>();
    private ArrayList<Integer> cntList = new ArrayList<Integer>();
    private File file = null;


    //private ArrayList<String> arrayForJSON = new ArrayList<String>();
    int count = 0;


    private TextView mImageDetails;
    private Button button1;
    private Button button2;
    private Button button3;

    public String Result;

    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        button1 = (Button) findViewById(R.id.button1);
        button2 = (Button) findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button3);

        mImageDetails = (TextView) findViewById(R.id.image_details);

        button1.setOnClickListener(this);
        button2.setOnClickListener(this);
        button3.setOnClickListener(this);

        PermissionUtils.requestPermission(this,
                STORAGE_WRITE_REQUEST,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        File dir = makeDirectory(SAVEPATH);

        file = makeFile(dir, (SAVEPATH + SAVEFILEPATH));
    }

    public void uploadImage(Bitmap extractedFiles) {
        if (extractedFiles != null) {
            try {
                // scale the image to save on bandwidth
                Bitmap bitmap =
                        scaleBitmapDown(extractedFiles, 1200);

                callCloudVision(bitmap);

            } catch (IOException e) {
                Log.d(TAG, "Image picking failed because " + e.getMessage());
                Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
            }
        } else {
            Log.d(TAG, "Image picker gave us a null image.");
            Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
        }

    }

    private void callCloudVision(final Bitmap bitmap) throws IOException {
        // Switch text to loading
        //mImageDetails.setText(R.string.loading_message);

        // Do the real work in an async task, because we need to use the network anyway
        new AsyncTask<Object, Void, String>() {
            @Override
            protected String doInBackground(Object... params) {
                try {
                    HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
                    JsonFactory jsonFactory = GsonFactory.getDefaultInstance();


                    Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
                    builder.setVisionRequestInitializer(new
                            VisionRequestInitializer(CLOUD_VISION_API_KEY_FOR_ANDROID));
                    //   builder.setVisionRequestInitializer(new2
                    //         VisionRequestInitializer(CLOUD_VISION_API_Key_FOR_WEB));
                    Vision vision = builder.build();

                    BatchAnnotateImagesRequest batchAnnotateImagesRequest =
                            new BatchAnnotateImagesRequest();
                    batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {{
                        AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();

                        // Add the image
                        Image base64EncodedImage = new Image();
                        // Convert the bitmap to a JPEG
                        // Just in case it's a format that Android understands but Cloud Vision
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
                        byte[] imageBytes = byteArrayOutputStream.toByteArray();

                        // Base64 encode the JPEG
                        base64EncodedImage.encodeContent(imageBytes);
                        annotateImageRequest.setImage(base64EncodedImage);

                        // add the features we want
                        annotateImageRequest.setFeatures(new ArrayList<Feature>() {{
                            Feature labelDetection = new Feature();
                            labelDetection.setType("LABEL_DETECTION");
                            labelDetection.setMaxResults(10);
                            add(labelDetection);
                        }});

                        // Add the list of one thing to the request
                        add(annotateImageRequest);
                    }});

                    Vision.Images.Annotate annotateRequest =
                            vision.images().annotate(batchAnnotateImagesRequest);
                    // Due to a bug: requests to Vision API containing large images fail when GZipped.
                    annotateRequest.setDisableGZipContent(true);
                    Log.d(TAG, "created Cloud Vision request object, sending request");

                    BatchAnnotateImagesResponse response = annotateRequest.execute();
                    return convertResponseToString(response);

                } catch (GoogleJsonResponseException e) {
                    Log.d(TAG, "failed to make API request because " + e.getContent());
                } catch (IOException e) {
                    Log.d(TAG, "failed to make API request because of other IOException " +
                            e.getMessage());
                }
                return "Cloud Vision API request failed. Check logs for details.";
            }

            protected void onPostExecute(String result) {
                Result += result;
            }

        }.execute();
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

    private String convertResponseToString(BatchAnnotateImagesResponse response) {
        String message = "";

        List<EntityAnnotation> labels = response.getResponses().get(0).getLabelAnnotations();
        if (labels != null) {
            for (EntityAnnotation label : labels) {

                message += String.format("%s\n",label.getDescription());

            }
        } else {
            message += "nothing\n";

        }

        return message;
    }

    @Override
    public void onClick(View v) {



        if (v.getId() == R.id.button1) {

            Result = "";
            Toast.makeText(this, "Please Wait....", Toast.LENGTH_LONG).show();

           // if (PermissionUtils.requestPermission(
                    //this,
                    //GALLERY_IMAGE_REQUEST,
                  //  android.Manifest.permission.READ_EXTERNAL_STORAGE)) {


                //mImageDetails.setText("Loading...");
                try {
                    extractingImage();
                } catch (IOException e) {

                    mImageDetails.setText("Error!");

                    e.printStackTrace();
                }
              //  Toast.makeText(this, "Done", Toast.LENGTH_LONG).show();
            //}

        } else if (v.getId() == R.id.button2) {
            String[] temp = Result.split("\n");

            for(int index = 0; index< temp.length ; index++){

                int count = 1;

                //반복문을 돌면서 itemList에 등록되었느지 확인
                if(!itemList.contains(temp[index])){
                    itemList.add(temp[index]);
                    // item 이 몇 개인지 카운트

                    for(int cntItem = index + 1; cntItem < temp.length ; cntItem ++){
                        if(temp[index].equals(temp[cntItem])){
                            count+=1;
                        }
                    }

                    cntList.add(count);

                }else{
                    continue;
                }

            }

            //cnt를 비교하여 itemList, cntList 정렬
            for(int sourceIndex = 0 ; sourceIndex <cntList.size()-1;sourceIndex++){
                for(int targetIndex = sourceIndex+1;targetIndex < cntList.size()-1;targetIndex++){
                    if(cntList.get(sourceIndex) < cntList.get(targetIndex)){
                        int moveCnt = 0;
                        String moveItem = "";

                        //cntList 이동
                        moveCnt = cntList.get(targetIndex);
                        cntList.set(targetIndex,cntList.get(sourceIndex));
                        cntList.set(sourceIndex,moveCnt);

                        //itemList 이동
                        moveItem = itemList.get(targetIndex);
                        itemList.set(targetIndex,itemList.get(sourceIndex));
                        itemList.set(sourceIndex,moveItem);
                    }
                }
            }

            // end 데이터 유형 및 개수를 설정
            // 데이터 유형별로 중복개수와 함께 출력

            for(int index =0; index < itemList.size() ; index++){
                finalResult += itemList.get(index) + " - " + cntList.get(index) + "\n";
                //arrayForJSON.add(index,itemList.get(index)+" - " + cntList.get(index));
            }

            mImageDetails.setText(finalResult);

        }

        else if(v.getId() == R.id.button3){

            JSONObject Result = new JSONObject();
            JSONArray resultArray = new JSONArray();
            try{
                JSONArray arr = new JSONArray();
                for(int i = 0; i< itemList.size(); i++){
                    JSONObject sObject = new JSONObject();
                    sObject.put("item",itemList.get(i));
                    sObject.put("count",cntList.get(i));

                    resultArray.put(sObject);
                }
                Result.put("Personal Priority", resultArray);

            }catch (Exception e){};

        }
    }

    public void extractingImage() throws IOException {

        File gallery = new File(Path);

        FilenameFilter filenameFilter = new FilenameFilter() {

            @Override
            public boolean accept(File dir, String filename) {
                String lowerCase = filename.toLowerCase();

                if (lowerCase.endsWith("jpg")) {
                    return true;
                } else if (lowerCase.endsWith("png")) {
                    return true;
                } else if (lowerCase.endsWith("jpeg")) {
                    return true;
                } else {
                    return false;
                }
            }
        };

        File[] files = gallery.listFiles(filenameFilter);


        for (File f : files) {

            temp = temp + f.getCanonicalPath() + "\n";
            this.count ++;

        }

        DecodingPath(temp);

    }

    public void DecodingPath(String s) {

        String[] ImageArray = s.split("\n");

        for(String name : ImageArray){
            Bitmap bitmap = BitmapFactory.decodeFile(name);
            uploadImage(bitmap);
        }

    }

    /**
     * 디렉토리 생성
     * @return dir
     */
    private File makeDirectory(String dir_path){
        File dir = new File(dir_path);
        if (!dir.exists())
        {
            dir.mkdirs();
            Log.i( TAG , "!dir.exists" );
        }else{
            Log.i( TAG , "dir.exists" );
        }

        return dir;
    }
    /**
     * 파일 생성
     * @param dir
     * @return file
     */
    private File makeFile(File dir , String file_path){
        File file = null;
        boolean isSuccess = false;
        if(dir.isDirectory()){
            file = new File(file_path);
            if(file!=null&&!file.exists()){
                Log.i( TAG , "!file.exists" );
                try {
                    isSuccess = file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally{
                    Log.i(TAG, "파일생성 여부 = " + isSuccess);
                }
            }else{
                Log.i( TAG , "file.exists" );
            }
        }
        return file;
    }

}