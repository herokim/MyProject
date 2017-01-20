package ict.mju.ac.kr.finalclass;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    List<contact> contactList;
    ListView listViewContact;
    static final int FORM_ACTIVITY = 1;
    ArrayAdapter<contact> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, FormActivity.class);
                //startActivity(intent);
                startActivityForResult(intent,FORM_ACTIVITY);
                // Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                 //       .setAction("Action", null).show();
            }
        });

        listViewContact = (ListView) findViewById(R.id.listViewContact);

        contactList = new ArrayList<>();

        //sample
        contactList.add(new contact("d","11213","seoul", "myeonji","xxx"));
        contactList.add(new contact("e","11213","seoul", "myeonji","xxx"));
        contactList.add(new contact("a","11213","seoul", "myeonji","xxx"));
        contactList.add(new contact("b","11213","seoul", "myeonji","xxx"));
        contactList.add(new contact("c","11213","seoul", "myeonji","xxx"));
        contactList.add(new contact("f","11213","seoul", "myeonji","xxx"));
        contactList.add(new contact("g","11213","seoul", "myeonji","xxx"));
        contactList.add(new contact("h","11213","seoul", "myeonji","xxx"));
        sort();
        adapter = new ArrayAdapter<contact>(this, android.R.layout.simple_list_item_1, contactList);
        listViewContact.setAdapter(adapter);
        listViewContact.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                contact contact = contactList.get(position);
                Intent intent = new Intent(MainActivity.this, ContactActivity.class); //그냥 this 는 AdapterView.OnItemClickListener 가리킴
                intent.putExtra("contact",contact);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == FORM_ACTIVITY){
            if(resultCode == RESULT_OK){
                contact contact = (contact) data.getExtras().get("contact");
                contactList.add(contact);
                sort();
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void sort(){
        Collections.sort(contactList);
    }

}
