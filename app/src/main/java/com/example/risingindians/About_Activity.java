package com.example.risingindians;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;

public class About_Activity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        ImageView facebookLink = (ImageView) findViewById(R.id.facebook);
        facebookLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent facebookintent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/risingindians/"));
                startActivity(facebookintent);
            }
        });

        ImageView linkedinLink = (ImageView) findViewById(R.id.linkedin);
        linkedinLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent linkedinintent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.linkedin.com/in/rising-indians-75b952b7/?originalSubdomain=in"));
                startActivity(linkedinintent);
            }
        });


        ImageView websiteLink = (ImageView) findViewById(R.id.www);
        websiteLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent websiteintent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.risingindians.org"));
                startActivity(websiteintent);
            }
        });


    }
}
