package servlet;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Duration;
import java.time.Instant;

public class Sqrt_n_Calculation {
    public static void main(String[] args) {
        // 精度の設定（100万桁）
        int precision = 1000000;
        //平方根を計算したい自然数n
        int n = 7;
        // タイマー開始
        Instant start = Instant.now();

        // ルートnを計算
        BigDecimal sqrt2 = sqrt(BigDecimal.valueOf(n), precision);

        // タイマー終了
        Instant end = Instant.now();

        // 結果の表示
         System.out.println(sqrt2);

        // 経過時間を表示
        System.out.println("計算時間: " + Duration.between(start, end).toSeconds() + " 秒");
    }

    public static BigDecimal sqrt(BigDecimal value, int precision) {
        MathContext mc = new MathContext(precision);
        BigDecimal x = new BigDecimal(Math.sqrt(value.doubleValue()));
        BigDecimal two = BigDecimal.valueOf(2);
        BigDecimal prevX;
        do {
            prevX = x;
            x = x.add(value.divide(x, mc)).divide(two, mc);
        } while (prevX.subtract(x).abs().compareTo(BigDecimal.valueOf(1).scaleByPowerOfTen(-precision)) > 0);
        return x;
    }
}
