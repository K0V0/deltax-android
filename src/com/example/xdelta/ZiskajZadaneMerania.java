package com.example.xdelta;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;

import android.os.Bundle;
import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.text.Html;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ZiskajZadaneMerania extends Activity {
	
	LinearLayout.LayoutParams parametreTextu = new LinearLayout.LayoutParams(
			0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
	LinearLayout.LayoutParams parametreTabulky = new LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 4);
	
	int pocetHodnot = 0;
	int exponent = 0;	
	String[] hodnota;										// povodne hodnoty ziskane
	
	MathContext mc;											// presnost vypoctov, kolko desatinnych miest
	BigDecimal pocetmerani;									// pocet merani skonvertovane na bezstratovy bigdecimal format
	int pocetDesatinZaokruhlenie;							// pocet desatinnych miest na ktore sa zaokruhluje pri renderovani
	int pocetDekadZaokruhlenie;
	
	BigDecimal[] hodnotaDec;								// skonvertovane vstupne hodnoty na bigdecimal format
	BigDecimal aritmetickyPriemer;							// aritmeticky priemer
	BigDecimal[] delta;										// odchylka + -
	BigDecimal[] delta2;									// kvadraticka odchylka
	BigDecimal deltaPriemer;								// aritmeticky priemer absolutnych hodnot odchylok 
	BigDecimal delta2scitane;								// suma kvadratickych odchylok
	BigDecimal kvadratickaChybaPriemeru;					// stredna kvadraticka chyba aritmetickeho priemeru
	BigDecimal pravdepodobnaChybaPriemeru;					// stredna pravdepodobnA chyba aritmetickeho priemeru
	BigDecimal maximalnaChybaPriemeru;						// stredna maximalna chyba aritmetickeho priemeru
	
	String[] aritmetickyPriemerRender;						// 2 hodnoty - druha je exponent
	String[] deltaRender;									// krajsie vyzerajuce upravene hodnoty na vyrenderovanie, posledny prvok je 
	String[] delta2Render;									// desiatkovy nasobok
	String[] deltaPercent;									// 2 hodnoty - druha je exponent
	String[] deltaPriemerRender;							// 2 hodnoty - druha je exponent
	String[] kvadChybaRender;								// 2 hodnoty - druha je exponent
	String[] prChybaRender;
	String[] maxChybaRender;
	
	
	
	public void onCreate(Bundle savedInstanceState) 
		{
		 super.onCreate(savedInstanceState);
		 setContentView(R.layout.vyhodnotene_merania);
		 SystemoveListy.aplikujNa(this);
		
		 inicializovatHodnotyNaDefault();
		 		 
		 nacitajDecimalHodnoty();
		 pocitajDelty();
		 pocitajVysledneHodnoty();
		 aritmetickyPriemerRender = krajsiaDesatinnaHodnota(aritmetickyPriemer);
		 deltaRender = krajsieDesatinneHodnoty(delta);
		 delta2Render = krajsieDesatinneHodnoty(delta2);
		 deltaPercent = deltaVPercentach(delta, aritmetickyPriemer);
		 deltaPriemerRender = krajsiaDesatinnaHodnota(deltaPriemer);
		 kvadChybaRender = krajsiaDesatinnaHodnota(kvadratickaChybaPriemeru);
		 prChybaRender = krajsiaDesatinnaHodnota(pravdepodobnaChybaPriemeru);
		 maxChybaRender = krajsiaDesatinnaHodnota(maximalnaChybaPriemeru);
		 RenderujHlavnyExponent();
		 renderujHodnoty();
		 renderujZaverecneHodnoty();
		}
	
	void inicializovatHodnotyNaDefault()
		{
		 parametreTabulky.setMargins(0, 2, 0, 2);			
		 hodnota = getIntent().getStringArrayExtra("pole_hodnot");
		 pocetHodnot = Integer.valueOf(getIntent().getStringExtra("pocet_hodnot"));
		 SharedPreferences preferences = getSharedPreferences("preferences", MODE_PRIVATE);
		 exponent = Integer.valueOf(preferences.getString("exponent", null));
		 
		 mc = new MathContext(32);
		 pocetmerani = new BigDecimal(String.valueOf(pocetHodnot));
		 pocetDesatinZaokruhlenie = 3;
		 pocetDekadZaokruhlenie = 3;
		 
		 hodnotaDec = new BigDecimal[pocetHodnot];
		 aritmetickyPriemer = new BigDecimal("0");
		 deltaPriemer = new BigDecimal("0");
		 delta = new BigDecimal[pocetHodnot];
		 delta2 = new BigDecimal[pocetHodnot];
		 delta2scitane = new BigDecimal("0");
		 kvadratickaChybaPriemeru = new BigDecimal("0");
		 pravdepodobnaChybaPriemeru = new BigDecimal("0");
		 maximalnaChybaPriemeru = new BigDecimal("0");
		 
		 aritmetickyPriemerRender = new String[2];
		 deltaRender = new String[pocetHodnot+1];
		 delta2Render = new String[pocetHodnot+1];
		 deltaPercent = new String[pocetHodnot+1];
		 deltaPriemerRender = new String[2];
		 kvadChybaRender = new String[2];
		 prChybaRender = new String[2];
		 maxChybaRender = new String[2];
		}
		
	void RenderujHlavnyExponent()
		{
		 TextView exp_hlavny = (TextView) findViewById(R.id.hlavny_exponent);
		 exp_hlavny.setText(Html.fromHtml("x10<sup><small>"+Integer.toString(exponent)+"</small></sup>"));
		 TextView exp_delta = (TextView) findViewById(R.id.delta);
		 exp_delta.setText(Html.fromHtml("x10<sup><small>"+deltaRender[pocetHodnot]+"</small></sup>"));
		 TextView exp_delta2 = (TextView) findViewById(R.id.delta2);
		 exp_delta2.setText(Html.fromHtml("x10<sup><small>"+delta2Render[pocetHodnot]+"</small></sup>"));
		}
	
	void renderujHodnoty()
		{
		LinearLayout container = (LinearLayout) findViewById(R.id.meraniaVyhodnotene);
		for (int i=0; i<pocetHodnot; i++)
			{
			LinearLayout riadok = new LinearLayout(ZiskajZadaneMerania.this);
			if (i%2 != 0) { riadok.setBackgroundColor(Color.rgb(50,50,50)); }
			
				TextView merana_hodnota = new TextView(ZiskajZadaneMerania.this);
				merana_hodnota.setText(hodnota[i]);
				merana_hodnota.setGravity(Gravity.CENTER);
				riadok.addView(merana_hodnota, parametreTextu);
				
				TextView delta_hodnota = new TextView(ZiskajZadaneMerania.this);
				delta_hodnota.setText(deltaRender[i]);
				delta_hodnota.setGravity(Gravity.CENTER);
				riadok.addView(delta_hodnota, parametreTextu);
				
				TextView delta2_hodnota = new TextView(ZiskajZadaneMerania.this);
				delta2_hodnota.setText(delta2Render[i]);
				delta2_hodnota.setGravity(Gravity.CENTER);
				riadok.addView(delta2_hodnota, parametreTextu);
				
				TextView delta_percent_hodnota = new TextView(ZiskajZadaneMerania.this);
				delta_percent_hodnota.setText(deltaPercent[i]);
				delta_percent_hodnota.setGravity(Gravity.CENTER);
				riadok.addView(delta_percent_hodnota, parametreTextu);
				
			container.addView(riadok, parametreTabulky);
			}	
		}

	void renderujZaverecneHodnoty()
		{
		 TextView priemer = (TextView) findViewById(R.id.priemer);
		 priemer.setText(Html.fromHtml(aritmetickyPriemerRender[0] + " x10<sup><small>" + aritmetickyPriemerRender[1] + "</small></sup>"));
		 TextView delta_priemer = (TextView) findViewById(R.id.priemer_delta);
		 delta_priemer.setText(Html.fromHtml(deltaPriemerRender[0] + " x10<sup><small>" + deltaPriemerRender[1] + "</small></sup>"));
		 TextView kvad_chyba = (TextView) findViewById(R.id.kvad_chyba);
		 kvad_chyba.setText(Html.fromHtml(kvadChybaRender[0] + " x10<sup><small>" + kvadChybaRender[1] + "</small></sup>"));
		 TextView pr_chyba = (TextView) findViewById(R.id.pravdepodobna_chyba);
		 pr_chyba.setText(Html.fromHtml(prChybaRender[0] + " x10<sup><small>" + prChybaRender[1] + "</small></sup>"));
		 TextView max_chyba = (TextView) findViewById(R.id.maximalna_chyba);
		 max_chyba.setText(Html.fromHtml(maxChybaRender[0] + " x10<sup><small>" + maxChybaRender[1] + "</small></sup>"));
		 TextView vysledok = (TextView) findViewById(R.id.vysledna_hodnota);
		 BigDecimal chybaKorektor = new BigDecimal("10").pow(Integer.parseInt(aritmetickyPriemerRender[1])*(-1), mc);
		 BigDecimal chybaUpravena = new BigDecimal("0");
		 chybaUpravena = kvadratickaChybaPriemeru.multiply(chybaKorektor, mc);
		 String chybaUpravenaRender = (chybaUpravena.setScale(pocetDesatinZaokruhlenie, RoundingMode.HALF_UP).stripTrailingZeros()).toPlainString();
		 vysledok.setText(Html.fromHtml("( "+ aritmetickyPriemerRender[0] +" \u00B1 " + chybaUpravenaRender 
				 		  + " ) x10<sup><small>" + aritmetickyPriemerRender[1] + "</small></sup>"));
		}  
	
	void nacitajDecimalHodnoty()
		{
		if (exponent != 0)
				{
				String temp = "x";
				if (exponent > 0)
					{
					 temp = "1";
					 for (int i=0; i<exponent; i++)	{ temp = temp + "0"; }
					}
				if (exponent < 0)
					{
					 temp = "0.";
					 for (int i=0; i<Math.abs(exponent); i++)	
					 	{
						 if (i == ((Math.abs(exponent))-1))
							 	{
								 temp = temp + "1"; 
							 	}
						 else	{
							 	 temp = temp + "0";
						 		}
						}
					}
				
				BigDecimal multiplikator = new BigDecimal(temp);
			
				for (int i=0; i<pocetHodnot; i++)
					{
					 BigDecimal tmp = new BigDecimal(hodnota[i]);
					 hodnotaDec[i] = tmp.multiply(multiplikator, mc);
					}
				}
		else	{
				 for (int i=0; i<pocetHodnot; i++)
					{
					 hodnotaDec[i] = new BigDecimal(hodnota[i]);
					}
				}
		}
			
	void pocitajDelty()
		{
		BigDecimal temp = new BigDecimal("0");
		BigDecimal pocetmerani = new BigDecimal(String.valueOf(pocetHodnot));
		
		for (int i=0; i<pocetHodnot; i++)
			{
			 temp = temp.add(hodnotaDec[i]);
			}
		aritmetickyPriemer = temp.divide(pocetmerani, 32, RoundingMode.HALF_UP);
		
		for (int i=0; i<pocetHodnot; i++)
			{
			 delta[i] = (aritmetickyPriemer.subtract(hodnotaDec[i])).stripTrailingZeros();
			}
		
		for (int i=0; i<pocetHodnot; i++)
			{
			 delta2[i] = (delta[i].multiply(delta[i], mc)).stripTrailingZeros();
			}
		
		temp = new BigDecimal("0");
		for (int i=0; i<pocetHodnot; i++)
			{
			 temp = temp.add(delta[i].abs(mc)); 
			}
		 deltaPriemer = temp.divide(pocetmerani, 32, RoundingMode.HALF_UP);
		 
		temp = new BigDecimal("0");
		for (int i=0; i<pocetHodnot; i++)
			{
			 temp = temp.add(delta2[i]); 
			}
		 delta2scitane = temp;
		}

	void pocitajVysledneHodnoty()
		{
		 BigDecimal tmp = new BigDecimal("0");
		 BigDecimal k = new BigDecimal("1").divide(pocetmerani.multiply(pocetmerani.subtract(new BigDecimal("1")), mc), 32, RoundingMode.HALF_UP);
		 kvadratickaChybaPriemeru = bigSqrt((k.multiply(delta2scitane, mc)), mc);
		 tmp =  kvadratickaChybaPriemeru.multiply(new BigDecimal(2), mc);
		 pravdepodobnaChybaPriemeru = tmp.divide(new BigDecimal(3), 32, RoundingMode.HALF_UP);
		 maximalnaChybaPriemeru =  kvadratickaChybaPriemeru.multiply(new BigDecimal("3"), mc);
		}
	
	String[] krajsieDesatinneHodnoty(BigDecimal[] data)
		{
		 int pocet = data.length;
		 int delta_exponent = 0;
		 String[] deltaString = new String[pocet];
		 String[] deltaReturn = new String[pocet+1];
		 
		 for (int i=0; i<pocet; i++)
			{
			 deltaString[i] = data[i].stripTrailingZeros().toPlainString();
			}
		 
		 int suDesatinne = 0;
		 for (int i=0; i<pocet; i++)
			{
			 if (deltaString[i].contains("."))
			 	{
				 suDesatinne++;
			 	}
			}
		 
		 int pocetNulMax = 0;
		 int nulaNaZaciatku = 0;
		 BigDecimal posunCiarky = new BigDecimal(0);
		 if (suDesatinne > 0)				
		 	{
			 for (int i=0; i<pocet; i++)
				{
				 if (deltaString[i].contains("."))
				 	{
					 int poziciaBodka = deltaString[i].indexOf('.');
					 char[] temp = deltaString[i].toCharArray();
					 int zasah = 0;
					 int zasahNulaNaZaciatku = 0;
					 if (temp[0] == '0') { zasah++; zasahNulaNaZaciatku++; }
					 for (int j=poziciaBodka+1; j<temp.length; j++)
					 	{
						 if (temp[j] == '0') { zasah++; }
						 else				 { break; }
					 	}
					 if (pocetNulMax < zasah) { pocetNulMax = zasah; }
					 if (nulaNaZaciatku < zasahNulaNaZaciatku) { nulaNaZaciatku = zasahNulaNaZaciatku; }
					}
				}
			 delta_exponent = pocetNulMax*(-1);
			 posunCiarky = new BigDecimal(Math.pow(10, pocetNulMax));
		 	}
		 else
		 	{
			 for (int i=0; i<pocet; i++)
				{
				 if (deltaString[i].contains("0"))
				 	{
					 char[] temp = deltaString[i].toCharArray();
					 int zasah = 0;
					 for (int j=0; j<temp.length; j++)
					 	{
						 if (temp[j] == '0') { zasah++; }
					 	}
					 if (zasah > pocetNulMax) { pocetNulMax = zasah; }
					}
				}
			 delta_exponent = pocetNulMax;
			 posunCiarky = new BigDecimal(Math.pow(10, pocetNulMax*(-1)));
		 	}
				 
		 int posunMax = 0;
		 BigDecimal posunCiarky2 = new BigDecimal(0);
		 BigDecimal[] temp = new BigDecimal[pocet];
		 for (int i=0; i<pocet; i++)
			{
			 int posun = 0;
			 if (pocetNulMax > 0) { temp[i] = data[i].multiply(posunCiarky, mc); }		
			 else 				  { temp[i] = data[i]; }
			 
			 String temp3check = (temp[i].setScale(pocetDesatinZaokruhlenie, RoundingMode.HALF_UP).stripTrailingZeros()).toPlainString();
			 if (temp3check.indexOf(".") > pocetDekadZaokruhlenie)
				 	{
					 posun = (temp3check.indexOf(".") - pocetDekadZaokruhlenie) * (-1);
				 	}
			 if (posun < posunMax) { posunMax = posun; }
			}
		 
		 deltaReturn[pocet] = Integer.toString(delta_exponent-posunMax);
		 
		 posunCiarky2 = new BigDecimal("10").pow(posunMax, mc);
		 delta_exponent = delta_exponent - posunMax;
		 
		 BigDecimal[] finalny = new BigDecimal[pocet];
		 for (int i=0; i<pocet; i++)
			{
			 if (posunMax == 0)
				 	{
					 finalny[i] = temp[i];
				 	}
			 else	{
				 	  finalny[i] = temp[i].multiply(posunCiarky2, mc);
			 		}
			 
			 if (finalny[i].compareTo(BigDecimal.ZERO) == 0)
				 	{
					 deltaReturn[i] = "0";
				 	}
			 else	{
				 	 deltaReturn[i] = (finalny[i].setScale(pocetDesatinZaokruhlenie, RoundingMode.HALF_UP).stripTrailingZeros()).toPlainString();
			 		}
			 }
		 return deltaReturn;
		}

	String[] deltaVPercentach(BigDecimal[] data, BigDecimal porovnavac)
		{
		 BigDecimal tmp = new BigDecimal("0");
		 BigDecimal sto = new BigDecimal("100");
		 String[] navrat = new String[data.length];
		 for (int i=0; i<data.length; i++)
		 	{
			 tmp = (data[i].divide(porovnavac, 32, RoundingMode.HALF_UP).multiply(sto, mc));
			 navrat[i] = tmp.abs().setScale(3, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
		 	}
		 return navrat;
		}

	String[] krajsiaDesatinnaHodnota(BigDecimal data)
		{
		 String temp = new String("0");
		 String[] navrat  = new String[2];
		 temp = data.stripTrailingZeros().toPlainString();
		 int pocetNulMax = 0;
		 int delta_exponent = 0;
		 BigDecimal posunCiarky = new BigDecimal(0);
		 BigDecimal posunCiarky2 = new BigDecimal(0);
		 BigDecimal finalny = new BigDecimal(0);
		 int posun = 0;
		 
		 if (temp.contains(".")) 
			 	{ 
				 int poziciaBodka = temp.indexOf('.');
				 char[] temp2 = temp.toCharArray();
				 int zasah = 0;
				 if (temp2[0] == '0') { zasah++; }
				 for (int j=poziciaBodka+1; j<temp2.length; j++)
				 	{
					 if (temp2[j] == '0') { zasah++; }
					 else				 { break; }
				 	}
				 if (pocetNulMax < zasah) { pocetNulMax = zasah; }
				 delta_exponent = pocetNulMax*(-1);
				 posunCiarky = new BigDecimal("10").pow(pocetNulMax, mc);
			 	}
		 else	{
				 if (temp.contains("0"))
				 	{
					 char[] temp2 = temp.toCharArray();
					 int zasah = 0;
					 for (int j=0; j<temp2.length; j++)
					 	{
						 if (temp2[j] == '0') { zasah++; }
					 	}
					 if (zasah > pocetNulMax) { pocetNulMax = zasah; }
					 delta_exponent = pocetNulMax;
					 posunCiarky = new BigDecimal("10").pow(pocetNulMax*(-1), mc);
					}
		 		}
		 
		 BigDecimal temp3 = new BigDecimal("0");
		 if (pocetNulMax > 0) { temp3 = data.multiply(posunCiarky, mc); }		
		 else 				  { temp3 = data; }
		 String temp3check = (temp3.setScale(pocetDesatinZaokruhlenie, RoundingMode.HALF_UP).stripTrailingZeros()).toPlainString();
		 if (temp3check.indexOf(".") > pocetDekadZaokruhlenie)
			 	{
				 posun = (temp3check.indexOf(".") - pocetDekadZaokruhlenie) * (-1);
				 posunCiarky2 = new BigDecimal("10").pow(posun, mc);
				 finalny = temp3.multiply(posunCiarky2, mc);
				 delta_exponent = delta_exponent - posun; 
			 	}
		 else	{
			 	 finalny = temp3;
		 		}
		 
		 if (finalny.compareTo(BigDecimal.ZERO) == 0)
			 	{
				 navrat[0] = "0";
			 	}
		 else	{
			 	 navrat[0] = (finalny.setScale(pocetDesatinZaokruhlenie, RoundingMode.HALF_UP).stripTrailingZeros()).toPlainString();
		 		}
		 
		 navrat[1] = Integer.toString(delta_exponent);
		 return navrat;
		}
	
	public static BigDecimal bigSqrt(BigDecimal squarD, MathContext rootMC)
	  {
	    // Static constants - perhaps initialize in class Vladimir!
	    BigDecimal TWO = new BigDecimal(2);
	    double SQRT_10 = 3.162277660168379332;


	    // General number and precision checking
	    int sign = squarD.signum();
	    if(sign == -1)
	      throw new ArithmeticException("\nSquare root of a negative number: " + squarD);
	    else if(sign == 0)
	      return squarD.round(rootMC);

	    int prec = rootMC.getPrecision();           // the requested precision
	    if(prec == 0)
	      throw new IllegalArgumentException("\nMost roots won't have infinite precision = 0");

	    // Initial precision is that of double numbers 2^63/2 ~ 4E18
	    int BITS = 62;                              // 63-1 an even number of number bits
	    int nInit = 16;                             // precision seems 16 to 18 digits
	    MathContext nMC = new MathContext(18, RoundingMode.HALF_DOWN);


	    // Iteration variables, for the square root x and the reciprocal v
	    BigDecimal x = null, e = null;              // initial x:  x0 ~ sqrt()
	    BigDecimal v = null, g = null;              // initial v:  v0 = 1/(2*x)

	    // Estimate the square root with the foremost 62 bits of squarD
	    BigInteger bi = squarD.unscaledValue();     // bi and scale are a tandem
	    int biLen = bi.bitLength();
	    int shift = Math.max(0, biLen - BITS + (biLen%2 == 0 ? 0 : 1));   // even shift..
	    bi = bi.shiftRight(shift);                  // ..floors to 62 or 63 bit BigInteger

	    double root = Math.sqrt(bi.doubleValue());
	    BigDecimal halfBack = new BigDecimal(BigInteger.ONE.shiftLeft(shift/2));

	    int scale = squarD.scale();
	    if(scale % 2 == 1)                          // add half scales of the root to odds..
	      root *= SQRT_10;                          // 5 -> 2, -5 -> -3 need half a scale more..
	    scale = (int)Math.floor(scale/2.);          // ..where 100 -> 10 shifts the scale

	    // Initial x - use double root - multiply by halfBack to unshift - set new scale
	    x = new BigDecimal(root, nMC);
	    x = x.multiply(halfBack, nMC);                          // x0 ~ sqrt()
	    if(scale != 0)
	      x = x.movePointLeft(scale);

	    if(prec < nInit)                 // for prec 15 root x0 must surely be OK
	      return x.round(rootMC);        // return small prec roots without iterations

	    // Initial v - the reciprocal
	    v = BigDecimal.ONE.divide(TWO.multiply(x), nMC);        // v0 = 1/(2*x)


	    // Collect iteration precisions beforehand
	    ArrayList<Integer> nPrecs = new ArrayList<Integer>();

	    assert nInit > 3 : "Never ending loop!";                // assume nInit = 16 <= prec

	    // Let m be the exact digits precision in an earlier! loop
	    for(int m = prec+1; m > nInit; m = m/2 + (m > 100 ? 1 : 2))
	      nPrecs.add(m);


	    // The loop of "Square Root by Coupled Newton Iteration" for simpletons
	    for(int i = nPrecs.size()-1; i > -1; i--)
	    {
	      // Increase precision - next iteration supplies n exact digits
	      nMC = new MathContext(nPrecs.get(i), (i%2 == 1) ? RoundingMode.HALF_UP :
	                                                        RoundingMode.HALF_DOWN);

	      // Next x                                                 // e = d - x^2
	      e = squarD.subtract(x.multiply(x, nMC), nMC);
	      if(i != 0)
	        x = x.add(e.multiply(v, nMC));                          // x += e*v     ~ sqrt()
	      else
	      {
	        x = x.add(e.multiply(v, rootMC), rootMC);               // root x is ready!
	        break;
	      }

	      // Next v                                                 // g = 1 - 2*x*v
	      g = BigDecimal.ONE.subtract(TWO.multiply(x).multiply(v, nMC));

	      v = v.add(g.multiply(v, nMC));                            // v += g*v     ~ 1/2/sqrt()
	    }

	    return x;                        // return sqrt(squarD) with precision of rootMC
	  }
	
}
