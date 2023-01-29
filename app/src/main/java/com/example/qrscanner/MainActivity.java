package com.example.qrscanner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.google.zxing.Result;
import android.widget.Toast;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    CodeScanner codeScanner;
    CodeScannerView scannerView;
    TextView resultData;
    Button rescan;
    TextView name;

    @SuppressLint({"MissingInflatedId", "ResourceAsColor"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        scannerView = findViewById(R.id.scannerView);
        codeScanner = new CodeScanner(this,scannerView);
        resultData = findViewById(R.id.result);
        rescan = findViewById(R.id.button);
        name = findViewById(R.id.name);


        codeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull Result result) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run()
                    {
                        resultData.setText(result.getText());
                        if(resultData.getText().equals("https://docs.google.com/spreadsheets/d/1pFS5ZEB2_828Pz0OEaEWqC7gawudNLXSk9tVhdwzjj4/edit#gid=0"))
                            addToSheet();
                        else
                            Toast.makeText(MainActivity.this,"not the class QR code",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        rescan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                codeScanner.startPreview();
            }

        });
    }

    private void addToSheet(){
        final ProgressDialog dialog = ProgressDialog.show(MainActivity.this, "Adding Item","Please wait...");
        final String student = name.getText().toString().trim();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, "https://script.google.com/macros/s/AKfycbzAvOrW9dmc2Q_N7oRmmebCOLZrODlyxl2XYfy2SoZBYVHj-YX2dJHlpAsvA7tCb_EW/exec", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                dialog.dismiss();
                Toast.makeText(MainActivity.this,""+response,Toast.LENGTH_SHORT).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dialog.dismiss();
            }
        }
        ){
            @Nullable
            @Override
            protected Map<String, String> getParams(){
                Map<String, String> params = new HashMap<>();
                params.put("action","addItem");
                params.put("student",student);
                return params;
            }
        };
        int timeOut = 5000;
        RetryPolicy retryPolicy = new DefaultRetryPolicy(timeOut,0,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(retryPolicy);

        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
        queue.add(stringRequest);

    }
    @Override
    protected void onResume() {
        super.onResume();
        codeScanner.startPreview();
    }
}