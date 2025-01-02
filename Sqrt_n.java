/*
指定された数値nの平方根をニュートン法で小数第5位まで求めるプログラム。
*/

package experiment;

import java.text.DecimalFormat;

public class Sqrt_n {

	public static void main(String[] args) {
		
		DecimalFormat decimalFormat = new DecimalFormat("#.#####");
				
		double x = 3.0;
		double x2 = 0.0;
		//この変数nに平方根を求めたい数値を"n.0"で入力。
		double n = 5.0;
		boolean im = false;
		
		if ( n < 0 ) {
			n = Math.abs(n);
			im = true;
		}
		
		while ( true ) {
			
			x2 = x - (( x * x - n ) / (x * 2));
			
			if ( Math.abs(x2 - x) < 0.00000001 ) { break; }
			
			x = x2;
			
		}
		
		String result = decimalFormat.format(x);
		
		if ( !im ) {
			System.out.print(result);
		} else {
			System.out.print(result + "*i");
		}
	}

}
