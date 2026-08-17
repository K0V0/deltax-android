package com.example.xdelta;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.text.InputType;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.view.ViewGroup;

public class ZobrazFormularMerania extends Activity {
	
	public int pocetHodnot;
	public double[] hodnoty = new double[25];
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.zobraz_formular_merania);
		// natahanie XML suboru so zacatym layoutom 
		LinearLayout container = (LinearLayout) findViewById(R.id.merania_container);
		// id layoutu 
		
		Intent intent = getIntent();
		String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
		pocetHodnot = Integer.valueOf(message);
							
		for (int i=0; i<pocetHodnot;)	
			{
			LinearLayout meraniaHodnotaContainer = new LinearLayout(this);
			meraniaHodnotaContainer.setOrientation(LinearLayout.HORIZONTAL);
			meraniaHodnotaContainer.setWeightSum(2f);
			// lavo hore pravo dole v pixloch
			
			for (int j=0; j<2; j++)
				{
				if (i < pocetHodnot)
					{
					EditText hodnota = new EditText(this);
					hodnota.setHint("hodnota"+(i+1));
					hodnota.setRawInputType(
							InputType.TYPE_CLASS_NUMBER |
							InputType.TYPE_NUMBER_FLAG_DECIMAL |
							InputType.TYPE_NUMBER_FLAG_SIGNED);
					// +1 lebo id 0 je pre Android neplatne resource ID
					hodnota.setId(i+1);
					LinearLayout.LayoutParams layoutParamsStlp = new LinearLayout.LayoutParams(
						0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
					if ((i%2==0)&&(i!=(pocetHodnot-1)))      { layoutParamsStlp.setMargins(0, 0, 8, 8); }
					else if ((i%2!=0)&&(i!=(pocetHodnot-1))) { layoutParamsStlp.setMargins(8, 0, 0, 8); }
					else if (i==(pocetHodnot-1)&&(i%2==0))	 { layoutParamsStlp.setMargins(0, 0, 16, 8); }
					meraniaHodnotaContainer.addView(hodnota, layoutParamsStlp);
					}
				i++;
				}
			container.addView(meraniaHodnotaContainer);
			}
	}
		
	 public void vypocitaj(View view) 
	 	{
		 String[] hodnoty = new String[pocetHodnot];
	 	 Intent zaujem = new Intent().setClass(ZobrazFormularMerania.this, ZiskajZadaneMerania.class);
	 	 boolean idemeDalej = true;
	  	 for (int i=0; i<pocetHodnot; i++)	
	 		{
	 		 EditText temp = (EditText) this.findViewById(i+1);
	 		 hodnoty[i] = temp.getText().toString();
	 		 try { double num = Double.parseDouble(hodnoty[i]); } 
	    	 catch (NumberFormatException e) { idemeDalej = false;  }
	 		}
	 	
	  	 if (idemeDalej)
			  	 {
				  zaujem.putExtra("pole_hodnot", hodnoty);
				  zaujem.putExtra("pocet_hodnot", String.valueOf(pocetHodnot));
				  startActivity(zaujem);
			  	 }
	  	 else	 {
			  	  AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		   		  alertDialog.setTitle("Nepovolené hodnoty");
		   		  alertDialog.setMessage("Skontrolujte zadané hodnoty či sú vyplnené všetky políčka alebo neobsahujú nepovolené znaky." +
		   		 						 "\n\nPovolené sú len číslice, znamienko a desatinná bodka.");
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
