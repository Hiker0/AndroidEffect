package com.allen.androideffect.cyclerview.design.snackbar;

import android.os.Bundle;
import android.app.Activity;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.allen.androideffect.R;

public class SnackbarActivity extends AppCompatActivity {
    Button mButton;
    View root ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snackbar);
        root = findViewById(R.id.root);
        mButton = (Button) findViewById(R.id.show);
        mButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                showSnackbar();
            }
        });
    }

    void showSnackbar(){
        Snackbar bar = Snackbar.make(root, "adsfadsf?", 2000);
        bar.setAction("yes",new View.OnClickListener(){
            @Override
            public void onClick(View v) {

            }
        });

        bar.show();
    }
}
