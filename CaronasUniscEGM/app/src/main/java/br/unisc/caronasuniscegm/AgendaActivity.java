package br.unisc.caronasuniscegm;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

/**
 * Created by MateusFelipe on 11/10/2015.
 */
public class AgendaActivity extends AppCompatActivity {

    private Button mButton;

   public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agenda);


       mButton = (Button) findViewById( R.id.add_ride );

       mButton.setOnClickListener(new View.OnClickListener() {
           public void onClick(View v) {
               Intent intent = new Intent(getApplicationContext(), ConfigureRideActivity.class);
               startActivity(intent);
           }
       });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_agenda, menu);
        return true;
    }


}
