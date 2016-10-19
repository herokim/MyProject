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
import android.widget.ImageView;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //public static final String FILE_NAME = "temp.jpg";
    private static final String CLOUD_VISION_API_KEY_FOR_ANDROID = "AIzaSyBn9K0Zb6MhXimFoqufOFJbbQdykuzdiuw";
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int GALLERY_IMAGE_REQUEST = 1;
    int count = 0;


    private TextView mImageDetails;
    private Button button1;
    private Button button2;

    public String Result;

    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        button1 = (Button) findViewById(R.id.button1);
        button2 = (Button) findViewById(R.id.button2);

        mImageDetails = (TextView) findViewById(R.id.image_details);

        button1.setOnClickListener(this);
        button2.setOnClickListener(this);

    }

/*
    public void startGalleryChooser() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select a photo"),
                GALLERY_IMAGE_REQUEST);
    }
  */
    /*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            uploadImage(data.getData());
        }
    }*/
/*
    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (PermissionUtils.permissionGranted(
                requestCode,
                GALLERY_IMAGE_REQUEST,
                grantResults)) {

        }
    }*/

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

//    private String convertResponseToString(BatchAnnotateImagesResponse response) {
//    private ArrayList<String> convertResponseToString(BatchAnnotateImagesResponse response) {
private String convertResponseToString(BatchAnnotateImagesResponse response) {
        String message = "";

        //ArrayList<String> message = new ArrayList<>();
        //List message = new ArrayList<String>();

        List<EntityAnnotation> labels = response.getResponses().get(0).getLabelAnnotations();
        if (labels != null) {
            for (EntityAnnotation label : labels) {

                message += String.format("%s\n",label.getDescription());

                //message.add(String.format("%s",label.getDescription()));

                //message.add(label.getDescription());


            }
        } else {
            message += "nothing\n";
            //message.add("nothing");

        }

        return message;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button1) {

            Result = "";
            Toast.makeText(this, "Please Wait....", Toast.LENGTH_LONG).show();

            if (PermissionUtils.requestPermission(
                    this,
                    GALLERY_IMAGE_REQUEST,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE)) {


                mImageDetails.setText("Loading...");
                try {
                    extractingImage();
                } catch (IOException e) {

                    mImageDetails.setText("Error!");

                    e.printStackTrace();
                }
                Toast.makeText(this, "Done", Toast.LENGTH_LONG).show();
            }

        } else if (v.getId() == R.id.button2) {
            String[] temp = Result.split("\n");

            ArrayList<String> itemList = new ArrayList<String>();

            ArrayList<Integer> cntList = new ArrayList<Integer>();

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

            String finalResult = "";

            for(int index =0; index < itemList.size() ; index++){
                finalResult += itemList.get(index) + " - " + cntList.get(index) + "\n";
            }

            mImageDetails.setText(finalResult);

        }
    }

    public void extractingImage() throws IOException {

        String Path = Environment.getExternalStorageDirectory().getAbsolutePath() +
                File.separator + "DCIM" + File.separator + "Camera";

        String temp = "";

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

        int i = 1;

        for(String name : ImageArray){
            Bitmap bitmap = BitmapFactory.decodeFile(name);
            uploadImage(bitmap);
        }

        /*
        for (String call : ImageArray) {

            mImageDetails.setText(call + "\t" + i);
            i ++;
            Bitmap bitmap = BitmapFactory.decodeFile(call);
            uploadImage(bitmap);


        }*/

    }

}