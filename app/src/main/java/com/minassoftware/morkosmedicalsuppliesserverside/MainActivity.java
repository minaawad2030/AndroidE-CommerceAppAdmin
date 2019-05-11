package com.minassoftware.morkosmedicalsuppliesserverside;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import info.hoang8f.widget.FButton;

public class MainActivity extends AppCompatActivity {

    Button signIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        signIn=(Button)findViewById(R.id.Btn_SignIn);

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this,SignIn.class);
                startActivity(intent);
            }
        });
    }
}
