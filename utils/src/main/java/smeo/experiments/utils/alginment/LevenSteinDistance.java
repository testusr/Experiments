package smeo.experiments.utils.alginment;

/**
 * Created by smeo on 31.05.17.
 */
public class LevenSteinDistance {
    public static void main(String[] args) {
        System.out.println(align(new double[]{1.1, 2.2, 2.2, 3.3, 4.4}, new double[]{0.1, 2.2, 4.4, 5.5, 6.6}));
    }

    public static String align(double[] a, double[] b) {
        int[][] T = new int[a.length + 1][b.length + 1];

        for (int i = 0; i <= a.length; i++)
            T[i][0] = i;

        for (int i = 0; i <= b.length; i++)
            T[0][i] = i;

        for (int i = 1; i <= a.length; i++) {
            for (int j = 1; j <= b.length; j++) {
                if (equals(a[i - 1], b[j - 1]))
                    T[i][j] = T[i - 1][j - 1];
                else
                    T[i][j] = Math.min(T[i - 1][j], T[i][j - 1]) + 1;
            }
        }

        StringBuilder aa = new StringBuilder(), bb = new StringBuilder();

        for (int i = a.length, j = b.length; i > 0 || j > 0; ) {
            if (i > 0 && T[i][j] == T[i - 1][j] + 1) {
                aa.append(a[--i]).append("|");
                aa.append("-").append("|");
            } else if (j > 0 && T[i][j] == T[i][j - 1] + 1) {
                aa.append(b[--j]).append("|");
                aa.append("-").append("|");
            } else if (i > 0 && j > 0 && T[i][j] == T[i - 1][j - 1]) {
                aa.append(a[--i]).append("|");
                aa.append(b[--j]).append("|");
            }
            aa.append("\n");
        }

        return aa.reverse().toString();
    }

    private static boolean equals(double b, double a) {
        return (Math.abs(a - b) < 0.000001);
    }
}
