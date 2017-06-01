package smeo.experiments.utils.alginment;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by smeo on 31.05.17.
 */
public class LevenSteinDistance {
	public static final int NO_MATCH = -1;

	public static void main(String[] args) {
		Matchable[] a = loadMatchableArrayFromCsv(10000, args[0]);
		Matchable[] b = loadMatchableArrayFromCsv(10000, args[1]);

		int[] matchesForA = align(a, b);
		printMatchedRates(10000, a, b, matchesForA);
	}

	private static Matchable[] loadMatchableArrayFromCsv(int noOfLinesToLoad, String filename) {
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

	private static void printMatchedRates(int noOfRatesToPrint, Matchable[] a, Matchable[] b, int[] matchesAtoB) {
		for (int i = 0; i < noOfRatesToPrint; i++) {

			final int matchedIndex = matchesAtoB[i];
			final MatchableRate aMatchable = (MatchableRate) a[i];

			if (matchedIndex != NO_MATCH) {
				final MatchableRate bMatchable = (MatchableRate) b[matchedIndex];
				System.out.println(aMatchable.toString() + "|" + bMatchable.toString() + ", " + (bMatchable.timestamp() - aMatchable.timestamp()));
			} else {
				System.out.println(aMatchable.toString() + "|-,-");
			}
		}
	}

	public static int[] align(Matchable[] a, Matchable[] b) {
		int[] bMatchOnA = new int[Math.max(a.length, b.length)];

		final int xsize = a.length + 1;
		final int ysize = b.length + 1;
		IntContainer T = new IntContainer(xsize, ysize);

		for (int i = 0; i <= a.length; i++)
			T.setValue(i, 0, i);

		for (int i = 0; i <= b.length; i++)
			T.setValue(0, i, i);

		for (int i = 1; i <= a.length; i++) {
			for (int j = 1; j <= b.length; j++) {
				if (a[i - 1].matches(b[j - 1]))
					T.setValue(i, j, T.getValue((i - 1), (j - 1)));
				else
					T.setValue(i, j, (Math.min(T.getValue((i - 1), j), T.getValue(i, (j - 1))) + 1));
			}
		}

		StringBuilder aa = new StringBuilder(), bb = new StringBuilder();
		int notMatcheBEntries = 0;
		int notMatcheAEntries = 0;
		for (int i = a.length, j = b.length; i > 0 || j > 0;) {
			if (i > 0 && T.getValue(i, j) == T.getValue((i - 1), j) + 1) {
				bMatchOnA[--i] = NO_MATCH;
				notMatcheAEntries++;
				// aa.append(String.valueOf(a[i])).append("|");
				// aa.append("-").append("|");
			} else if (j > 0 && T.getValue(i, j) == T.getValue(i, (j - 1)) + 1) {
				// aMatchOnB[--j] = NO_MATCH;
				--j;
				notMatcheBEntries++;
				// aa.append("-").append("|");
			} else if (i > 0 && j > 0 && T.getValue(i, j) == T.getValue((i - 1), (j - 1))) {
				bMatchOnA[--i] = --j;

				// aa.append(String.valueOf(a[i])).append("|");
				// aa.append(String.valueOf(b[j])).append("|");
			}
			// aa.append("\n");
		}
		System.out.println("not matched from b sequence (messages received not send): " + notMatcheBEntries);
		System.out.println("not matched from a sequence (message loss) : " + notMatcheAEntries);

		return bMatchOnA;
	}

	private static boolean equals(double b, double a) {
		return (Math.abs(a - b) < 0.000001);
	}

	interface Matchable {
		boolean matches(Matchable toMatch);
	}

	private static class MatchableRate implements Matchable {
		long timestamp;
		double rate;

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
	}

	private static class IntContainer {
		final int[][] values;

		public IntContainer(int xsize, int ysize) {
			values = new int[xsize][ysize];
		}

		public void setValue(int x, int y, int value) {
			values[x][y] = value;
		}

		public int getValue(int x, int y) {
			return values[x][y];
		}
	}
}
