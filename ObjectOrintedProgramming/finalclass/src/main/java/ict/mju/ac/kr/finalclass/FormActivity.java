package ict.mju.ac.kr.finalclass;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ButtonBarLayout;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class FormActivity extends AppCompatActivity implements View.OnClickListener{

    private Button button;
    private EditText editName, editDesc, editTel, editAddr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);

        editName = (EditText) findViewById(R.id.editTextName);
        editDesc = (EditText) findViewById(R.id.editTextDescrtipion);
        editTel = (EditText) findViewById(R.id.editTel);
        editAddr = (EditText) findViewById(R.id.editTextAddress);
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        String name = editName.getText().toString();
        String tel = editTel.getText().toString();
        String desc = editDesc.getText().toString();
        String addr = editAddr.getText().toString();

        contact contact = new contact(name, tel, addr, desc, "xxxx");
        Intent intent = getIntent();
        intent.putExtra("contact", contact);
        setResult(RESULT_OK, intent);
        finish();


    }
}
