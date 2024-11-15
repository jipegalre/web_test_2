package com.example.myapplication;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;


    ImageView imgView;
    TextView textView;
    String site_url = "http://10.0.2.2:8000";
    String token ="6c1df0add089e2c84f8c94c337f3285e40c18c1d";

    JSONObject post_json;
    String imageUrl = null;
    Bitmap bmImg = null;
    OutputStreamWriter outputStreamWriter = null;

    CloadImage taskDownload;
    UploadImage taskUpload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        textView = (TextView)findViewById(R.id.textView);

    }
    public void onClickDownload(View v) {
        if (taskDownload != null && taskDownload.getStatus() == AsyncTask.Status.RUNNING) {
            taskDownload.cancel(true);
        }
        Log.e("down", "before");
        taskDownload = new CloadImage();
        Log.e("down", "ready");
        taskDownload.execute(site_url + "/api_root/Post/");
        Toast.makeText(getApplicationContext(), site_url+"Download", Toast.LENGTH_LONG).show();
    }
    public void onClickUpload(View v) {
        if (taskUpload != null && taskUpload.getStatus() == AsyncTask.Status.RUNNING) {
            taskUpload.cancel(true);
        }

        taskUpload = new UploadImage();
        Log.e("down", "ready");
        taskUpload.execute(site_url + "/api_root/Post/");
        Toast.makeText(getApplicationContext(), site_url+"Upload", Toast.LENGTH_LONG).show();
    }

    private class CloadImage extends AsyncTask<String, Integer, List<Bitmap>> {
        @Override
        protected List<Bitmap> doInBackground(String... urls) {
            List<Bitmap> bitmapList = new ArrayList<>();
            try {

                String apiUrl = urls[0];
                Log.e("http", apiUrl);
                URL urlAPI = new URL(apiUrl);

                HttpURLConnection conn = (HttpURLConnection) urlAPI.openConnection();

                conn.setRequestProperty("Authorization", "Token " + token);
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(3000);
                conn.setReadTimeout(3000);

                int responseCode = conn.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {

                    InputStream is = conn.getInputStream();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    StringBuilder result = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    is.close();

                    String strJson = result.toString();
                    JSONArray aryJson = new JSONArray(strJson);
                    Log.e("Get", "success");
                    for (int i = 0; i < aryJson.length(); i++) {
                        post_json = (JSONObject) aryJson.get(i);
                        imageUrl = post_json.getString("image");
                        if (!imageUrl.equals("")) {
                            URL myImageUrl = new URL(imageUrl);
                            conn = (HttpURLConnection) myImageUrl.openConnection();
                            InputStream imgStream = conn.getInputStream();
                            Bitmap imageBitmap = BitmapFactory.decodeStream(imgStream);
                            bitmapList.add(imageBitmap);
                            imgStream.close();
                        }
                    }

                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return bitmapList;

        }
        @Override
        protected void onPostExecute(List<Bitmap> images) {
            if (images.isEmpty()) {
                textView.setText("불러올 이미지가 없습니다.");
            } else {
                textView.setText("이미지 로드 성공!");
                RecyclerView recyclerView = findViewById(R.id.recyclerView);
                ImageAdapter adapter = new ImageAdapter(images);
                recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                recyclerView.setAdapter(adapter);
            }
        }
    }

    private class UploadImage extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                try {
                    URL url = new URL(urls[0]);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Authorization", "JWT "+token);
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setDoOutput(true);
                    connection.setDoInput(true);

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("author", 1);
                    jsonObject.put("title", "안드로이드 upload 테스트");
                    jsonObject.put("text", "안드로이드로 작성된 upload 테스트 입력 입니다.");
                    jsonObject.put("created_date", "2024-11-05T16:34:00+09:00");
                    jsonObject.put("published_date", "2024-11-05T16:34:00+09:00");


                    connection.getOutputStream();


                    outputStreamWriter =new OutputStreamWriter(connection.getOutputStream());
                    outputStreamWriter.write(jsonObject.toString());

                    outputStreamWriter.flush();

                    connection.connect();

                    if (connection.getResponseCode() == 200) {
                        Log.e("uploadImage", "Success");
                    }
                    connection.disconnect();
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } catch (Exception e) {
                Log.e("uploadImage", "Exception in uploadImage: " + e.getMessage());
            }

            Log.e("uploadtask", "task finished");
            return null;
        }
    }

}