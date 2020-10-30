package com.example.risingindians;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;

public class DonateFragment extends Fragment {

    WebView webView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_donate,container,false);

        webView = view.findViewById(R.id.payment_webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("https://www.payumoney.com/paybypayumoney/#/0EEEF0D5961D01863A1301F1C3D15C08");
        return view;

    }
}
