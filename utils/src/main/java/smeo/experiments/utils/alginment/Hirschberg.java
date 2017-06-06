package smeo.experiments.utils.alginment;

import org.apache.commons.lang3.ArrayUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.copyOfRange;
import static smeo.experiments.utils.alginment.Hirschberg.MatchableRate.*;

public class Hirschberg
{
	static int K[][];

	public static Matchable[] findLCS_String(Matchable[] x, Matchable[] y)
	{
		int k, i, j;
		int m, n;
		Matchable[] y1, y2;
		Matchable[] x1, x2;
		String C = "";

		m = x.length; // m = length of x
		n = y.length; // n = length of y

		/*
		 * =====================================================
		 * Base case 1: ""
		 * =====================================================
		 */
		if (m == 0)
		{
			return new Matchable[0]; // LCS = ""
		}

		/*
		 * =====================================================
		 * Base case 2: x = "?"
		 * =====================================================
		 */
		if (m == 1)
		{
			/*
			 * =====================================
			 * The input x consists of 1 character
			 * Find the single common character in y
			 * =====================================
			 */
			for (i = 0; i < n; i++)
				if (y[i].matchTo(x[0]))
					return (x); // Found: LCS = x

			return new Matchable[0]; // Not found: LCS = ""
		}

		/*
		 * =====================================================
		 * General case: x has 2 or more characters
		 * =====================================================
		 */
		int c = solveLCS(x, y); // This is the sum of the correct split
		int c1 = 0, c2 = 0;

		/*
		 * System.out.println("LCS( " + x + "," + y + ") = " + c );
		 */

		x1 = copyOfRange(x, 0, m / 2); // First half of x
		x2 = copyOfRange(x, m / 2, m); // Second half of x

		/*
		 * --------------------------------------------------
		 * Find a correct split of y
		 * --------------------------------------------------
		 */
		for (k = 0; k < n; k++)
		{

			c1 = solveLCS(x1, copyOfRange(y, 0, k)); // LCS of first half
			c2 = solveLCS(x2, copyOfRange(y, k, n)); // LCS of second half
			/*
			 * System.out.println("Trying: ");
			 * System.out.println(" " + x1
			 * + "<->" + y.substring(0, k) + " ==> " + c1);
			 * System.out.println(" " + x2
			 * + "<->" + y.substring(j, k) + " ==> " + c2);
			 */
			if (c1 + c2 == c)
				break; // Found a correct split of y !!!
		}
		/*
		 * if ( c1 + c2 != c )
		 * {
		 * System.out.println("x1 + x2 == z NOT FOUND ???");
		 * }
		 */
		/*
		 * --------------------------------------------------
		 * Here: k = a correct split of y ....
		 * 
		 * Solve smaller problems
		 * --------------------------------------------------
		 */

		y1 = copyOfRange(y, 0, k);
		y2 = copyOfRange(y, k, n);

		// System.out.println("   LCS_String(" + x1.length + "," + y1.length + ")");
		Matchable[] sol1 = findLCS_String(x1, y1);

		// System.out.println("   LCS_String(" + x2.length + "," + y2.length + ")");
		Matchable[] sol2 = findLCS_String(x2, y2);

		/*
		 * ------------------------------------------------------------
		 * Use solution of smaller problems to solve original problem
		 * ------------------------------------------------------------
		 */
		return ArrayUtils.addAll(sol1, sol2);
	}

	/*
	 * ==============================================================
	 * solveLCS(x,y): find the number of characters in the
	 * Longest Common Substring of x and y
	 * 
	 * This is the linear space algorithm to find length of LCS
	 * Except: I ADDED a statement to return K[1][n] at the end
	 * ==============================================================
	 */
	public static int solveLCS(Matchable[] x, Matchable[] y)
	{
		int i, j;

		if (x.length == 0 || y.length == 0)
			return 0;

		for (j = 0; j < y.length + 1; j++)
			K[1][j] = 0; // x = "" ===> LCS = 0

		for (i = 1; i < x.length + 1; i++)
		{
			/*
			 * =====================================================
			 * Recycle phase: copy row K[1][...] to row K[0][...]
			 * =====================================================
			 */
			for (j = 0; j < y.length + 1; j++)
				K[0][j] = K[1][j];

			K[1][0] = 0; // y = "" ===> LCS = 0

			for (j = 1; j < y.length + 1; j++)
			{
				if (x[i - 1].matchTo(y[j - 1]))
				{
					K[1][j] = K[0][j - 1] + 1;
				}
				else
				{
					K[1][j] = Math.max(K[0][j], K[1][j - 1]);
				}
			}
		}

		return K[1][y.length]; // ***** I added this
	}

	public static void main(String[] args)
	{

		// Scanner in = new Scanner(System.in);
		//
		// String x;
		// String y;
		// String z;
		// int i, j, r;
		//
		// System.out.print("x = ");
		// x = in.next();
		// System.out.print("y = ");
		// y = in.next();

		Matchable[] a = loadMatchableArrayFromCsv(1000, args[0]);
		Matchable[] b = loadMatchableArrayFromCsv(1000, args[1]);

		K = new int[2][b.length + 1]; // Linear space !!!

		Matchable[] z = findLCS_String(a, b);

		printMatches(30, z);
		System.out.println("LCS = " + z.length);

		try {
			matchedRatesToCsv("/tmp/matches_hirschber.csv", z);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	interface Matchable {
		boolean matches(Matchable toMatch);

		boolean matchTo(Matchable matchable);
	}

	public static class MatchableRate implements Matchable {
		long timestamp;
		double rate;
		private Matchable match;

		public MatchableRate(long timestamp, double rate) {
			this.timestamp = timestamp;
			this.rate = rate;
		}

		public long timestamp() {
			return timestamp;
		}

		public double rate() {
			return rate;
		}

		@Override
		public boolean matches(Matchable toMatch) {
			if (toMatch instanceof MatchableRate) {
				return (Math.abs(((MatchableRate) toMatch).rate - rate) < 0.000001);
			}
			return false;
		}

		Matchable match() {
			return match;
		}

		@Override
		public boolean matchTo(Matchable matchable) {
			if (matches(matchable)) {
				this.match = matchable;
				return true;
			}
			return false;
		}

		@Override
		public String toString() {
			return String.valueOf(rate);
		}

		/**
		 * expects a format of timestamp, rate example
		 * example:
		 * 1493942400037,1.09805
		 * 1493942400065,1.09804
		 * ...
		 *
		 * @param csvLine
		 * @param delimiter
		 * @return
		 */
		public static MatchableRate fromCsv(String csvLine, char delimiter) {
			final String[] split = csvLine.split(String.valueOf(delimiter));
			try {
				if (split.length == 2) {
					return new MatchableRate(Long.valueOf(split[0]), Double.valueOf(split[1]));
				}
			} catch (Exception e) {
				System.err.println("could not create matchable rate from csv line '" + csvLine + "'");
			}
			return null;
		}

		public static Matchable[] loadMatchableArrayFromCsv(int noOfLinesToLoad, String filename) {
			List<Matchable> matchableList = new ArrayList<>();
			int nLines = 0;
			try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
				String line;
				while ((line = br.readLine()) != null && nLines < noOfLinesToLoad) {
					Matchable matchable = MatchableRate.fromCsv(line, ',');
					if (matchable != null) {
						matchableList.add(matchable);
						nLines++;
					}
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return matchableList.toArray(new Matchable[matchableList.size()]);
		}

		public static void matchedRatesToCsv(String filename, Matchable[] matches) throws IOException {
			File fout = new File(filename);
			FileOutputStream fos = new FileOutputStream(fout);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

			for (int i = 0; i < matches.length; i++) {
				MatchableRate currMachable = (MatchableRate) matches[i];
				MatchableRate matched = (MatchableRate) currMachable.match();
				if (matched != null) {
					final String csvLine = currMachable.timestamp() + "," + currMachable.toString() + "," + matched.timestamp() + "," + matched.toString()
							+ ", "
							+ (matched.timestamp() - currMachable.timestamp() + "\n");
					bw.write(csvLine);
				} else {
					final String csvLine = currMachable.timestamp() + "," + currMachable.toString() + ", , ,\n";
					bw.write(csvLine);
				}
			}
			bw.flush();
			bw.close();
			System.out.println("matches written to file '" + filename + "'");
		}

		public static void printMatches(int noOfRatesToPrint, Matchable[] matches) {
			for (int i = 0; i < noOfRatesToPrint; i++) {
				MatchableRate currMachable = (MatchableRate) matches[i];
				MatchableRate matched = (MatchableRate) currMachable.match();
				if (matched != null) {
					System.out.println(currMachable.toString() + "|" + matched.toString() + ", " + (matched.timestamp() - currMachable.timestamp()));
				} else {
					System.out.println(currMachable.toString() + "|-,-");
				}
			}
		}
	}
}