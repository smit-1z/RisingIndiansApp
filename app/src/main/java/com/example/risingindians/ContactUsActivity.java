package com.example.risingindians;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ContactUsActivity extends AppCompatActivity {

    private TextInputLayout your_name;
    private TextInputLayout your_email;
    private TextInputLayout mail_subject;
    private TextInputLayout message;

    private Button send_mail_btn;
    private TextView caller;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_contact_us);

        ActionBar actionBar;
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        ColorDrawable colorDrawable
                = new ColorDrawable(Color.parseColor("#E26347"));

        // Set BackgroundDrawable
        actionBar.setBackgroundDrawable(colorDrawable);

        your_name = findViewById(R.id.your_name);
        your_email = findViewById(R.id.your_email);
        mail_subject = findViewById(R.id.subject);
        message = findViewById(R.id.message);

        send_mail_btn = findViewById(R.id.send_mail_btn);



        send_mail_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name      = your_name.getEditText().getText().toString();
                String email     = your_email.getEditText().getText().toString() + "@gmail.com";
                String subject   = mail_subject.getEditText().getText().toString();
                String mail_message   = message.getEditText().getText().toString();

                your_name.setError(null);
                your_email.setError(null);
                mail_subject.setError(null);
                message.setError(null);

                if (TextUtils.isEmpty(name)){
                    your_name.setError("Enter Your Name");
                    your_name.requestFocus();
                    return;
                }

                Boolean onError = false;
                if (!isValidEmail(email)) {
                    onError = true;
                    your_email.setError("Invalid Email");
                    return;
                }

                if (TextUtils.isEmpty(subject)){
                    mail_subject.setError("Enter Your Subject");
                    mail_subject.requestFocus();
                    return;
                }

                if (TextUtils.isEmpty(mail_message)){
                    message.setError("Enter Your Message");
                    message.requestFocus();
                    return;
                }

                Intent sendEmail = new Intent(android.content.Intent.ACTION_SEND);

                /* Fill it with Data */
                sendEmail.setType("plain/text");
                sendEmail.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"risingindiansonline@gmail.com"});
                sendEmail.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
                sendEmail.putExtra(android.content.Intent.EXTRA_TEXT,
                        mail_message + "\n \n \n Thank You \n -" + name);

                /* Send it off to the Activity-Chooser */
                startActivity(Intent.createChooser(sendEmail, "Send mail..."));


            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        //Get a Tracker (should auto-report)


    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onStop() {
        super.onStop();

    }


    // validating email id

    private boolean isValidEmail(String email) {
        String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public void call(View v)
    {
        caller = findViewById(R.id.phonenumber);
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:8980862848"));
        startActivity(intent);
    }





}

