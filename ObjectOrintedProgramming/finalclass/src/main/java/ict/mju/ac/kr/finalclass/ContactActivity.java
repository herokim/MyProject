package ict.mju.ac.kr.finalclass;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

public class ContactActivity extends AppCompatActivity {

    private TextView txtTel, txtDesc, txtAddress;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        txtTel = (TextView) findViewById(R.id.textViewtel);
        txtDesc = (TextView) findViewById(R.id.textViewDesc);
        txtAddress = (TextView) findViewById(R.id.textViewAddress);
        imageView = (ImageView) findViewById(R.id.imageView);

        Intent intent = getIntent();
        contact contact = (contact)intent.getExtras().get("contact");

        setTitle(contact.getName());

        txtTel.setText(contact.getTel());
        txtAddress.setText(contact.getAddress());
        txtDesc.setText(contact.getDesc());
    }
}
