package com.example.xdelta;

import android.app.Activity;
import android.graphics.Insets;
import android.os.Build;
import android.view.View;
import android.view.WindowInsets;

/**
 * Od Androidu 15 (a pre aplikacie s targetSdk 35+) sa okno kresli cez cely displej,
 * takze obsah lezi pod stavovym riadkom s hodinami aj pod navigacnou listou.
 * Tato trieda odsadi obsah aktivity o rozmery systemovych listov.
 */
final class SystemoveListy
	{
	 private SystemoveListy() { }

	 static void aplikujNa(Activity aktivita)
	 	{
		 final View obsah = aktivita.findViewById(android.R.id.content);

		 // povodne odsadenie z XML sa zachova a systemove listy sa k nemu pripocitaju
		 final int povodneVlavo  = obsah.getPaddingLeft();
		 final int povodneHore   = obsah.getPaddingTop();
		 final int povodneVpravo = obsah.getPaddingRight();
		 final int povodneDole   = obsah.getPaddingBottom();

		 obsah.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener()
			{
			 public WindowInsets onApplyWindowInsets(View v, WindowInsets listy)
				{
				 int vlavo, hore, vpravo, dole;

				 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
					{
					 Insets systemove = listy.getInsets(
							 WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
					 Insets klavesnica = listy.getInsets(WindowInsets.Type.ime());

					 vlavo  = systemove.left;
					 hore   = systemove.top;
					 vpravo = systemove.right;
					 // ked je vysunuta klavesnica, obsah sa odsadi nad nu
					 dole   = Math.max(systemove.bottom, klavesnica.bottom);
					}
				 else	{
					 vlavo  = listy.getSystemWindowInsetLeft();
					 hore   = listy.getSystemWindowInsetTop();
					 vpravo = listy.getSystemWindowInsetRight();
					 dole   = listy.getSystemWindowInsetBottom();
					}

				 v.setPadding(povodneVlavo  + vlavo,
						 	  povodneHore   + hore,
						 	  povodneVpravo + vpravo,
						 	  povodneDole   + dole);
				 return listy;
				}
			});

		 obsah.requestApplyInsets();
	 	}
	}
