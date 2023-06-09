package com.example.nettytcpclient;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements NettyTCPSocket.TcpDataListener {

    private TextView result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btn = findViewById(R.id.btn_connect);
        EditText address = findViewById(R.id.tv_address);
        result = findViewById(R.id.tv_result);
        btn.setOnClickListener((view) ->{
            String str = address.getText().toString().trim();
            if(str != null){
                String[] info = str.split(":");
                NettyTCPSocket.connect(info[0],Integer.parseInt(info[1]));
                NettyTCPSocket.setTcpDataListener(this);
            }
        });
    }

    @Override
    public void getDataListener(String msg) {
        result.setText(msg);
    }

    @Override
    public void TcpStatusListener(String status) {
        Toast.makeText(this,status,Toast.LENGTH_SHORT).show();
    }
}