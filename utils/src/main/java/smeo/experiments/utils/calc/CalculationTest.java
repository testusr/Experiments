package smeo.experiments.utils.calc;

import java.math.BigDecimal;

/**
 * Floating point values due to their nature come with slight rounding issues. That is one reason why
 * BigDecimals should be used for numeric precise calculations.
 * As in high performance environments we for the most part try to avoid using immutable objects BigDecimals
 * are sometimes not an option. Once we know how precise we have to be we can work around that issues
 * by using a precision value and rounding for our floating point calculations.
 *
 */
public class CalculationTest {
    int mio1 = 1000000;
    int mio2 = 2000000;
    int mio5 = 5000000;
    int k500 = 500000;

    double[][] dprices = {{1.33, mio1}, {2.042, mio2}, {5.7, mio5}};
    float[][] fprices = {{1.33f, mio1}, {2.042f, mio2}, {5.7f, mio5}};
    BigDecimal[][] bdPrices = {{bd("1.33"), bd(""+mio1)}, {bd("2.042"), bd(""+mio2)}, {bd("5.7"), bd(""+mio5)}};

    private BigDecimal bd(String s) {
        return new BigDecimal(s);
    }

    public double doubleDiv(double a, double b, int precision){
        double multiplikator = Math.pow(10, precision);
        return Math.round((a/b)*(int)multiplikator) / multiplikator;
    }

    public float floatDiv(float a, float b, int precision){
        float multiplikator = (float)Math.pow(10, precision);
        return Math.round((a/b)*(int)multiplikator) / multiplikator;
    }

    public double calculateWithDoubles(double amount, int precision){
        double summedBaseAmount = 0;
        double summedQuoteAmount = 0;

        for (int i = 0; i < dprices.length; i++){
            double[] currPrice = dprices[i];
            summedBaseAmount += currPrice[1];
            summedQuoteAmount += (currPrice[1] * currPrice[0]) ;

            if (summedBaseAmount > amount){
                double overfilled = amount - summedBaseAmount;
                summedQuoteAmount += overfilled*currPrice[0];
                break;
            }

        }
        return doubleDiv(summedQuoteAmount , amount, precision);
    }

    public float calculateWithFloat(float amount, int precision){
        float summedBaseAmount = 0;
        float summedQuoteAmount = 0;
        for (int i = 0; i < dprices.length; i++){
            float[] currPrice = fprices[i];
            summedBaseAmount += currPrice[1];
            summedQuoteAmount += currPrice[1] * currPrice[0];

            if (summedBaseAmount > amount){
                float overfilled = amount - summedBaseAmount;
                summedQuoteAmount += overfilled*currPrice[0];
                break;
            }

        }

        return floatDiv(summedQuoteAmount, amount, precision);
    }

    public BigDecimal calculateWithBigDecimal(BigDecimal amount){
        BigDecimal summedBaseAmount = BigDecimal.ZERO;
        BigDecimal summedQuoteAmount = BigDecimal.ZERO;
        for (int i = 0; i < bdPrices.length; i++){
            BigDecimal[] currPrice = bdPrices[i];
            summedBaseAmount = summedBaseAmount.add(currPrice[1]);
            summedQuoteAmount = summedQuoteAmount.add(currPrice[1].multiply(currPrice[0]));

            if (summedBaseAmount.compareTo(amount) > 0){
                BigDecimal overfilled = amount.subtract(summedBaseAmount);
                summedQuoteAmount = summedQuoteAmount.add(overfilled.multiply(currPrice[0]));
                break;
            }

        }
        return summedQuoteAmount.divide(amount);
    }

    public void test(){
        System.out.println("double");
        System.out.println(calculateWithDoubles(3333,3));
        System.out.println(calculateWithDoubles(k500,3));
        System.out.println(calculateWithDoubles(mio1,3));
        System.out.println(calculateWithDoubles(mio2,3));
        System.out.println("float");
        System.out.println(calculateWithFloat(3333,3));
        System.out.println(calculateWithFloat(k500,3));
        System.out.println(calculateWithFloat(mio1,3));
        System.out.println(calculateWithFloat(mio2,3));
        System.out.println("BigDecimal");
        System.out.println(calculateWithBigDecimal(new BigDecimal(3333)));
        System.out.println(calculateWithBigDecimal(new BigDecimal(500000)));
        System.out.println(calculateWithBigDecimal(new BigDecimal(1000000)));
        System.out.println(calculateWithBigDecimal(new BigDecimal(2000000)));

    }

    public static void main(String[] args) {
        new CalculationTest().test();
    }
}
