package ok;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Wordle {

	public static void fout(String s) {
		System.out.print(s);
	}
	
	public static void cout(String s) {
		System.out.println(s);
	}
	
	public static void cout(double s) {
		System.out.println(s + "");
	}
	
	public static void cout(int s) {
		System.out.println(s + "");
	}
	
	public static void cout(long s) {
		System.out.println(s + "");
	}
	
	public static double log2(double x) {
		if(x <= 0) return 0;
		double res = Math.log(x)/Math.log(2);
		return res;
	}
	
	public static double getSigmoid(long x) {
		double res = 1.0/(1 + Math.pow(Math.E, -(x-1806546.0)/259721.0));
		return res;
	}
	
	public static int toDec(String s) {
		int val = 0;
		int three = 1;
		for(int i = s.length()-1; i >= 0; i--) {
			val += three*(s.charAt(i) - '0');
			three *= 3;
		}
		return val;
	}
	
	public static class word implements Comparable<word> {
		String name;
		long freq;
		double p;
		public word(String name, long freq) {
			this.name = name;
			this.freq = freq;
			this.p = 0;
		}
		
		public word(String name, long freq, double p) {
			this.name = name;
			this.freq = freq;
			this.p = p;
		}
		
		@Override
	    public int compareTo(word w) {
			long value = this.freq - w.freq;
			if(value > 0) value = 1;
			if(value < 0) value = -1;
			return (int) value;
	    }
	}
	
	public static class entropy implements Comparable<entropy> {
		word w;
		double p;
		
		public entropy(word w, double p) {
			this.w = w;
			this.p = p;
		}
		
		@Override
	    public int compareTo(entropy e) {
			if(this.p + this.w.p > e.p + e.w.p) return 1;
			else if(this.p + this.w.p < e.p + e.w.p) return -1;
			return 0;
	    }
	}
	
	static ArrayList<word> words = new ArrayList<word>();
	static ArrayList<word> answers = new ArrayList<word>();
	static HashMap<String, String> table = new HashMap<String, String>();
	static ArrayList<String> patterns = new ArrayList<String>();
	static Random rand = new Random();
	static ArrayList<entropy>[] cont = new ArrayList[243];
	
	//get all possible patterns
	public static void calcPatterns(int depth, String curr) {
		if(depth == 0) {
			patterns.add(curr);
			return;
		}
		calcPatterns(depth-1, curr + "0");
		calcPatterns(depth-1, curr + "1");
		calcPatterns(depth-1, curr + "2");
	}
	
	static {
		try {
			String s;
			String[] h;
			BufferedReader r = new BufferedReader(new FileReader("src/ok/Files/words.txt"));
			while((s = r.readLine()) != null) {
				h = s.split(" ");
				words.add(new word(h[0], Long.parseLong(h[1])));
			}
			r.close();
			r = new BufferedReader(new FileReader("src/ok/Files/answers.txt"));
			while((s = r.readLine()) != null) {
				h = s.split(" ");
				answers.add(new word(h[0], Long.parseLong(h[1])));
			}
			r.close();
		} catch(IOException e) {
			cout("Incorrect file");
		}
		/*for(String i : words) {
			for(String j : words) {
				String key = i + " " + j;
				String value = getValue(i, j);
				table.put(key, value);
			}
		}*/
		calcPatterns(5, "");
		try {
			entropy curr;
			String s;
			BufferedReader f = new BufferedReader(new FileReader("src/ok/Files/2ndmove.txt"));
			for(int i = 0; i < cont.length; i++) {
				cont[i] = new ArrayList<entropy>();
				s = f.readLine();
				String[] help = s.split(" ");
				for(int j = 1; j < help.length; j++) {
					String[] tmp = help[j].split("-");
					double p = Double.parseDouble(help[j].split(tmp[1] + '-')[1]);
					curr = new entropy(new word(tmp[0], 12711, p), Double.parseDouble(tmp[1]));
					cont[i].add(curr);
				}
			}
			f.close();
		} catch(IOException e) {
			cout("Wrong file name");
		}
		getPOfBeingAnAnswer();
	}
	
	//first - guess, second - answer
	public static String getValue(String x, String y) {
		StringBuilder ans = new StringBuilder("00000");
		int n = x.length();
		boolean[] isOccupied = {false, false, false, false, false};
		for(int i = 0; i < n; i++) {
			if(x.charAt(i) == y.charAt(i)) {
				ans.setCharAt(i, '2');
				isOccupied[i] = true;
			}
		}
		for(int i = 0; i < n; i++) {
			if(ans.charAt(i) == '2') continue;
			for(int j = 0; j < n; j++) {
				if(x.charAt(i) == y.charAt(j) && !isOccupied[j]) {
					ans.setCharAt(i, '1');
					isOccupied[j] = true;
					break;
				}
			}
		}
		return ans.toString();
	}
	
	//get all possible words out of options with a given pattern
	public static ArrayList<word> getPossibleWords(String guess, String result, ArrayList<word> options) {
		ArrayList<word> ans = new ArrayList<word>();
		for(word x : options) {
			if(getValue(guess, x.name).equals(result)) ans.add(x);
		}
		return ans;
	}

	//get a perfect (or not) guess from a list
	public static String getAGuessFromAList(ArrayList<word> l) {
		word res = l.get(0);
		return res.name;
	}
	
	public static ArrayList<Double> getPatterns(word s, ArrayList<word> options) {
		ArrayList<Double> res = new ArrayList<Double>();
		for(int i = 0; i < patterns.size(); i++) {
			ArrayList<word> l = getPossibleWords(s.name, patterns.get(i), options);
			res.add(l.size()*1./options.size());
		}
		return res;
	}
	
	public static double getEntropy(ArrayList<Double> p) {
		double res = 0;
		for(double x : p) {
			res -= x*log2(x);
		}
		return res;
	}
	
	public static void getTheBestOpening() throws IOException {
		BufferedWriter w = new BufferedWriter(new FileWriter("src/ok/Files/entropies.txt"));
		ArrayList<entropy> bestGuesses = new ArrayList<entropy>();
		for(word x : words) {
			double d = getEntropy(getPatterns(x, answers));
			bestGuesses.add(new entropy(x, d));
		}
		Collections.sort(bestGuesses);
		Collections.reverse(bestGuesses);
		for(entropy e : bestGuesses) {
			w.write(e.w.name + " " + e.p + " " + e.w.p + '\n');
		}
		w.close();
	}
	
	public static void calculateAllContinuationsForTheBestCalculatedOpening() throws IOException {
		String opening;
		BufferedReader f = new BufferedReader(new FileReader("src/ok/Files/entropies.txt"));
		opening = f.readLine();
		opening = opening.split(" ")[0];
		f.close();
		for(String ans : patterns) {
			BufferedWriter w = new BufferedWriter(new FileWriter("src/ok/Files/2ndmove.txt", true));
			ArrayList<word> l = answers;
			l = getPossibleWords(opening, ans, l);
			l = calcP(l);
			HashMap<String, Double> mp = new HashMap<String, Double>();
			for(word x : words) {
				mp.put(x.name, 0.0);
			}
			for(word x : l) {
				mp.put(x.name, x.p);
			}
			word curr;
			for(int i = 0; i < words.size(); i++) {
				curr = words.get(i);
				curr.p = mp.get(curr.name);
				words.set(i, curr);
			}
			ArrayList<entropy> bestGuesses = new ArrayList<entropy>();
			for(word x : words) {
				double d = getEntropy(getPatterns(x, l));
				bestGuesses.add(new entropy(x, d));
			}
			Collections.sort(bestGuesses);
			Collections.reverse(bestGuesses);
			w.write(ans + " ");
			for(int i = 0; i < Math.min(bestGuesses.size(), 100); i++) {
				w.write(bestGuesses.get(i).w.name + "-" + bestGuesses.get(i).p + '-' + bestGuesses.get(i).w.p + " ");
			}
			w.write('\n');
			cout("For a pattern " + ans + " the best one is " + bestGuesses.get(0).w.name + " with an entropy of " + bestGuesses.get(0).p + " and p of " + bestGuesses.get(0).w.p);
			w.close();
		}
	}
	
	public static void getPOfBeingAnAnswer() {
		word curr;
		for(int i = 0; i < answers.size(); i++) {
			curr = answers.get(i);
			curr.p = getSigmoid(curr.freq);
			answers.set(i, curr);
		}
		double sum = 0;
		for(word w : answers) {
			sum += w.p;
		}
		for(int i = 0; i < answers.size(); i++) {
			curr = answers.get(i);
			curr.p = curr.p/sum;
			answers.set(i, curr);
		}
		HashMap<String, Double> t = new HashMap<String, Double>();
		for(word w : words) {
			t.put(w.name, 0.0);
		}
		for(word w : answers) {
			t.put(w.name, w.p);
		}
		for(int i = 0; i < words.size(); i++) {
			curr = words.get(i);
			curr.p = t.get(curr.name);
			words.set(i, curr);
		}
	}
	
	public static ArrayList<word> calcP(ArrayList<word> l) {
		double sum = 0;
		for(word w : l) {
			sum += w.p;
		}
		ArrayList<word> res = new ArrayList<word>();
		word curr;
		for(int i = 0; i < l.size(); i++) {
			curr = l.get(i);
			curr.p = curr.p/sum;
			res.add(curr);
		}
		return res;
	}
	
	public static int play() throws IOException {
		String guess = "", ans = "";
		BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
		ArrayList<word> l = answers;
		int tries = 0;
		ArrayList<String> guesses = new ArrayList<String>();
		ArrayList<String> patterns = new ArrayList<String>();
		BufferedReader f = new BufferedReader(new FileReader("src/ok/Files/entropies.txt"));
		guess = f.readLine();
		guess = guess.split(" ")[0];
		f.close();
		cout("Your opening choice should be: " + guess);
		while(l.size() >= 1 && tries < 6) {
			cout("Enter the result");
			ans = r.readLine();
			if(ans.equals("end")) return -1;
			ans.trim();
			guesses.add(guess);
			patterns.add(ans);
			if(ans.equals("22222")) {
				cout("Yay, that's the answer, you got it in " + (tries+1) + " tries.");
				break;
			}
			l = getPossibleWords(guess, ans, l);
			ArrayList<entropy> bestGuesses = new ArrayList<entropy>();
			l = calcP(l);
			HashMap<String, Double> mp = new HashMap<String, Double>();
			for(word x : words) {
				mp.put(x.name, 0.0);
			}
			for(word x : l) {
				mp.put(x.name, x.p);
			}
			word curr;
			for(int i = 0; i < words.size(); i++) {
				curr = words.get(i);
				curr.p = mp.get(curr.name);
				words.set(i, curr);
			}
			if(tries == 0) {
				bestGuesses = cont[toDec(ans)];
			}
			else {
				for(word x : words) {
					double d = getEntropy(getPatterns(x, l));
					bestGuesses.add(new entropy(x, d));
				}
				Collections.sort(bestGuesses);
				Collections.reverse(bestGuesses);
			}
			if(l.size() == 1) {
				cout("The answer should be: " + l.get(0).name);
				break;
			}
			cout("There are " + l.size() + " options left, top 5 suggestions: ");
			for(int i = 0; i < Math.min(bestGuesses.size(), 5); i++) {
				cout(bestGuesses.get(i).w.name + ", entropy - " + bestGuesses.get(i).p + ", p - " + bestGuesses.get(i).w.p);
			}
			cout("Some possible answers: ");
			for(int i = 0; i < Math.min(l.size(), 3); i++) {
				cout(l.get(i).name + ", p -  " + l.get(i).p);
			}
			guess = bestGuesses.get(0).w.name;
			tries++;
		}
		//r.close();
		for(int i = 0; i < guesses.size(); i++) {
			cout(guesses.get(i) + " " + patterns.get(i));
		}
		return 0;
	}
	
	public static void playWithAnswer(String ans) {
		ArrayList<word> l = words;
		int tries = 1;
		String guess = "", pattern = "";
		cout(ans);
		while(l.size() > 0) {
			guess = getAGuessFromAList(l);
			pattern = getValue(guess, ans);
			cout(guess + " " + pattern);
			if(pattern.equals("22222")) break;
			l = getPossibleWords(guess, pattern, l);
			tries++;
		}
		if(!guess.equals(ans)) {
			cout("For a word " + ans + " it didn't manage to win, guess - " + guess);
		}
		else if(tries > 6) {
			cout("For a word " + ans + " it didn't manage to win in 6 turns but in " + tries);
		}
		else cout("Got it in " + tries + " tries.");
	}
	
	public static void runSumilationOfAllPossibleGames() throws IOException {
		int[] cnts = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		long t = 0;
		long onlyWins = 0;
		int cntOfOnlyWins = 0;
		int cnt = 0;
		String opening;
		BufferedReader f = new BufferedReader(new FileReader("C:/Users/H P/Desktop/ProjectFiles/Wordle/entropies.txt"));
		opening = f.readLine();
		opening = opening.split(" ")[0];
		f.close();
		int k = 0;
		for(word ans : answers) {
			k++;
			ArrayList<word> l = answers;
			int tries = 1;
			String guess = opening, pattern = "";
			while(l.size() > 0) {
				pattern = getValue(guess, ans.name);
				if(pattern.equals("22222")) break;
				l = getPossibleWords(guess, pattern, l);
				if(l.size() == 1) {
					guess = l.get(0).name;
					tries++;
					break;
				}
				l = calcP(l);
				HashMap<String, Double> mp = new HashMap<String, Double>();
				for(word x : words) {
					mp.put(x.name, 0.0);
				}
				for(word x : l) {
					mp.put(x.name, x.p);
				}
				word curr;
				for(int i = 0; i < words.size(); i++) {
					curr = words.get(i);
					curr.p = mp.get(curr.name);
					words.set(i, curr);
				}
				if(tries == 1) {
					guess = cont[toDec(pattern)].get(0).w.name;
				}
				else {
					ArrayList<entropy> bestGuesses = new ArrayList<entropy>();
					for(word x : words) {
						double d = getEntropy(getPatterns(x, l));
						bestGuesses.add(new entropy(x, d));
					}
					Collections.sort(bestGuesses);
					Collections.reverse(bestGuesses);
					guess = bestGuesses.get(0).w.name;
				}
				tries++;
			}
			if(!guess.equals(ans.name)) {
				cout("For a word " + ans.name + " it didn't manage to win, guess - " + guess);
			}
			else {
				onlyWins += tries;
				cntOfOnlyWins++;
			}
			cnt++;
			t += tries;
			cnts[tries]++;
			cout(k*100./2315 + " " + ans.name + " " + tries + " " + t*1./cnt);
		}
		cout("It had " + (cnt - cntOfOnlyWins) + " losses");
		cout("Average length of a game - " + (t*1./cnt));
		cout("Average length of a winning game - " + (onlyWins*1./cntOfOnlyWins));
		for(int i = 0; i < cnts.length; i++) {
			if(cnts[i] == 0) continue;
			cout("In " + i + " tries: " + cnts[i]);
		}
	}
	
	public static void playLotsOfGames() throws IOException {
		while(true) {
			if(play() == -1) return;
			cout("\n\nNEW GAME:");
		}
	}
	
	public static void main(String[] args) throws Exception {
		//playWithAnswer("aahed");
		//runSumilationOfAllPossibleGames();
		playLotsOfGames();
		//calculateAllContinuationsForTheBestCalculatedOpening();
		//getTheBestOpening();
	}
}
