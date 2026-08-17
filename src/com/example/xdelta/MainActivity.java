package com.example.xdelta;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.view.View;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.EditText;

public class MainActivity extends Activity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    
    public final static String EXTRA_MESSAGE = "com.example.xdelta.MESSAGE";
    
    public void generujFormularMerania(View view) 
    	{
    	 Intent intent = new Intent(MainActivity.this, ZobrazFormularMerania.class);
    	 
    	 EditText editText = (EditText) findViewById(R.id.pocet_merani);
    	 EditText editText2 = (EditText) findViewById(R.id.exponent);
    	 
    	 boolean hrameDalej = true;
    	 String pocetMerani = editText.getText().toString();
    	 try { int num = Integer.parseInt(pocetMerani); } 
    	 catch (NumberFormatException e) { hrameDalej = false;  }
    	 
    	 String exp = editText2.getText().toString();
    	 try { int num = Integer.parseInt(exp); } 
    	 catch (NumberFormatException e) { hrameDalej = false; }
    	 
    	 if (hrameDalej)
	    	 	{
		    	 intent.putExtra(EXTRA_MESSAGE, pocetMerani);
		    	 SharedPreferences preferences = getSharedPreferences("preferences", MODE_WORLD_WRITEABLE);
		    	 preferences.edit().putString("exponent",exp).commit(); 
		    	 startActivity(intent);
	    	 	}
    	 else	{
	    		 AlertDialog alertDialog = new AlertDialog.Builder(this).create();
	    		 alertDialog.setTitle("Nepovolené hodnoty");
	    		 alertDialog.setMessage("Skontrolujte zadanie či nie je prázdne alebo neobsahuje nepovolené znaky." +
	    		 						"\n\nPovolené sú len číslice [ 0-9 ] a v políčku pre hodnotu exponenu aj znamienko mínus [ - ]");
	    		 alertDialog.setButton("OK", new DialogInterface.OnClickListener() { 
	    		 public void onClick(DialogInterface dialog, int which) 
	    		 	{
	    			 // here you can add functions
	    		 	}
	    		});
	    		 alertDialog.setIcon(R.drawable.ic_launcher);
	    		 alertDialog.show();
    	 		}
    	}
    
}
