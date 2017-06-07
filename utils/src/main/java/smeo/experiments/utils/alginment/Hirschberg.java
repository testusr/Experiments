package smeo.experiments.utils.alginment;

import java.io.*;
import java.util.*;

import static smeo.experiments.utils.alginment.Hirschberg.MatchableRate.*;

public class Hirschberg<T extends Hirschberg.MatchableRate> {

	public List<T> lcs(T[] a, T[] b) {
		int n = a.length;
		int m = b.length;

		switch (n) {
		case 0:
			return Collections.<T> emptyList();

		case 1:
			final T t = a[0];
			for (int i = 0; i < m; i++) {
				if (b[i].matches(t)) {
					return Collections.singletonList(t);
				}
			}
			return Collections.<T> emptyList();

		default:
			int i = n / 2;
			T[] aHead = Arrays.copyOfRange(a, 0, i);
			T[] aTail = Arrays.copyOfRange(a, i, n);
			int[] forward = calculateLcs(aHead, b);
			int[] backward = calculateLcs(reverse(aTail), reverse(b));

			int k = indexOfBiggerSum(forward, backward);

			T[] bHead = Arrays.copyOfRange(b, 0, k);
			T[] bTail = Arrays.copyOfRange(b, k, m);
			return concatenate(lcs(aHead, bHead), lcs(aTail, bTail));
		}
	}

	/** Returns the index in which the two LCS meet. */
	private int indexOfBiggerSum(int[] forward, int[] backward) {
		int tmp, k = -1, max = -1, m = forward.length - 1;
		for (int j = 0; j <= m; j++) {
			tmp = forward[j] + backward[m - j];
			if (tmp > max) {
				max = tmp;
				k = j;
			}
		}
		return k;
	}

	/**
	 * Uses the Smith-Waterman algorithm to calculate the score table
	 * (or distance table) using only 2 rows.
	 *
	 */
	private int[] calculateLcs(T[] a, T[] b) {
		final int m = b.length;

		System.out.println(System.currentTimeMillis() + " - calculateLcs a: " + a.length + " b: " + b.length);
		int[][] array = new int[2][m + 1];
		int[] curr = array[0];
		int[] prev = array[1];
		int[] tmp;

		for (T x : a) {
			// swap(curr, prev)
			tmp = curr;
			curr = prev;
			prev = tmp;

			// it's the Smith-Waterman algorithm
			for (int i = 0; i < m; i++) {
				T y = b[i];
				if (x.matches(y)) {
					curr[i + 1] = prev[i] + 1;
				} else {
					curr[i + 1] = Math.max(curr[i], prev[i + 1]);
				}
			}
		}
		return curr;
	}

	List<T> concatenate(List<T> a, List<T> b) {
		if (a.isEmpty()) {
			return b;
		}
		if (b.isEmpty()) {
			return a;
		}
		List<T> l = new ArrayList<>(a.size() + b.size());
		l.addAll(a);
		l.addAll(b);
		return l;
	}

	/** The given array is not modified. */
	T[] reverse(final T[] array) {
		@SuppressWarnings("unchecked")
		T[] reversed = array.clone();
		Collections.reverse(Arrays.asList(reversed));
		return reversed;
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

		Hirschberg.MatchableRate[] a = loadMatchableArrayFromCsv(1000000, args[0]);
		Hirschberg.MatchableRate[] b = loadMatchableArrayFromCsv(1000000, args[1]);

		Hirschberg hirschberg = new Hirschberg<MatchableRate>();
		System.out.println("start matching " + new Date());
		final List lcs = hirschberg.lcs(a, b);

		assignMatches(lcs, a, b);
		Hirschberg.MatchableRate[] z = (Hirschberg.MatchableRate[]) lcs.toArray(new Hirschberg.MatchableRate[lcs.size()]);

		System.out.println("finished matching " + new Date());
		printMatches(30, a);
		System.out.println("LCS = " + z.length);
		System.out.println("Sent (aseq)= " + a.length);
		System.out.println("Received (bseq) = " + a.length);
		System.out.println("Not Matched (aseq) / not received from b = " + notMatchCount(b));
		System.out.println("Not Matched (bseq) / never been send by a= " + notMatchCount(a));

		try {
			matchedRatesToCsv("/tmp/matches_hirschberg_asequence.csv", a);
			matchedRatesToCsv("/tmp/matches_hirschberg_bsequence.csv", b);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static int notMatchCount(Hirschberg.MatchableRate[] a) {
		int notMatched = 0;
		for (int i = 0; i < a.length; i++) {
			if (a[i].match() == null) {
				notMatched++;
			}
		}
		return notMatched;
	}

	private static void assignMatches(List<Hirschberg.MatchableRate> lcs, Hirschberg.MatchableRate[] a, Hirschberg.MatchableRate[] b) {
		int aIndex = 0;
		int bIndex = 0;
		for (Hirschberg.MatchableRate currLcsRate : lcs) {
			Hirschberg.MatchableRate firstAMatch = null;
			Hirschberg.MatchableRate firstBMatch = null;
			for (; aIndex < a.length && firstAMatch == null; aIndex++) {
				firstAMatch = currLcsRate.matches(a[aIndex]) ? a[aIndex] : null;
			}
			for (; bIndex < b.length && firstBMatch == null; bIndex++) {
				firstBMatch = currLcsRate.matches(b[bIndex]) ? b[bIndex] : null;
			}

			if (firstAMatch != null && firstBMatch != null) {
				firstAMatch.assignMatch(firstBMatch);
				firstBMatch.assignMatch(firstAMatch);
			}
		}
	}

	public static class MatchableRate {
		long timestamp;
		double rate;
		private MatchableRate match;

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

		public boolean matches(MatchableRate toMatch) {
			if (toMatch instanceof MatchableRate) {
				return (Math.abs(((MatchableRate) toMatch).rate - rate) < 0.000001);
			}
			return false;
		}

		MatchableRate match() {
			return match;
		}

		public boolean assignMatch(MatchableRate matchable) {
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

		public static MatchableRate[] loadMatchableArrayFromCsv(int noOfLinesToLoad, String filename) {
			List<MatchableRate> matchableList = new ArrayList<>();
			int nLines = 0;
			try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
				String line;
				while ((line = br.readLine()) != null && nLines < noOfLinesToLoad) {
					MatchableRate matchable = MatchableRate.fromCsv(line, ',');
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
			return matchableList.toArray(new MatchableRate[matchableList.size()]);
		}

		public static void matchedRatesToCsv(String filename, MatchableRate[] matches) throws IOException {
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

		public static void printMatches(int noOfRatesToPrint, MatchableRate[] matches) {
			for (int i = 0; i < noOfRatesToPrint && i < matches.length; i++) {
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