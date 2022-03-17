
/*
 * File: Yahtzee.java
 * ------------------
 * This program will eventually play the Yahtzee game.
 */

import java.util.ArrayList;
import java.util.Arrays;

import acm.io.*;
import acm.program.*;
import acm.util.*;

public class Yahtzee extends GraphicsProgram implements YahtzeeConstants {

	public static void main(String[] args) {
		new Yahtzee().start(args);
	}

	public void run() {
		IODialog dialog = getDialog();
		nPlayers = dialog.readInt("Enter number of players");
		playerNames = new String[nPlayers];
		for (int i = 1; i <= nPlayers; i++) {
			playerNames[i - 1] = dialog.readLine("Enter name for player " + i);
		}
		categoriesMatrix = new int[nPlayers][N_CATEGORIES];
		// Changing default value to -1 to distinguish 0 and unplayed category
		for (int[] i : categoriesMatrix) {
			Arrays.fill(i, -1);
		}
		for (int i = 0; i < categoriesMatrix.length; i++) {
			for (int j = 0; j < categoriesMatrix[i].length; j++) {
				if ((j > SIXES - 1 && j < THREE_OF_A_KIND - 1) || j > CHANCE - 1) {
					categoriesMatrix[i][j] = 0;
				}
			}
		}
		display = new YahtzeeDisplay(getGCanvas(), playerNames);
		playGame();
	}

	private void playGame() {
		/* You fill this in */
		while (true) {
			for (int i = 0; i < playerNames.length; i++) {
				display.printMessage(playerNames[i] + "'s turn! Click \"Roll Dice\" button to roll the dice.");
				display.waitForPlayerToClickRoll(i + 1);
				display.displayDice(rollDice(true));

				for (int j = 0; j < 2; j++) {
					display.printMessage("Select the dice you wish to rick roll and click \"Roll Again\".");
					display.waitForPlayerToSelectDice();
					display.displayDice(rollDice(false));
				}
				updatingCategory(i + 1);
			}
			if (endgame()) {
				break;
			}
		}
	}

	/**
	 * Pre Condition - Every strategy is used
	 * 
	 * Post Condition - Game over
	 * 
	 * @return if game is over true
	 */
	private boolean endgame() {
		if (checkFinishConditions()) {
			int max = 0;
			String name = "";
			for (int j = 0; j < categoriesMatrix.length; j++) {
				display.updateScorecard(TOTAL, j + 1, categoriesMatrix[j][TOTAL - 1]);
				display.updateScorecard(UPPER_BONUS, j + 1, categoriesMatrix[j][UPPER_BONUS - 1]);
				display.updateScorecard(UPPER_SCORE, j + 1, categoriesMatrix[j][UPPER_SCORE - 1]);
				display.updateScorecard(LOWER_SCORE, j + 1, categoriesMatrix[j][LOWER_SCORE - 1]);

				if (max < categoriesMatrix[j][TOTAL - 1]) {
					max = categoriesMatrix[j][TOTAL - 1];
					name = playerNames[j];
				}
			}
			display.printMessage("Congratulations, " + name + ", you're the winner with a total score of " + max + "!");
			return true;
		}
		return false;
	}

	/**
	 * Pre Condition - Player has rolled dices and is ready to choose category
	 * 
	 * Post Condition - Player has chosen the category
	 * 
	 * @param player
	 */
	private void updatingCategory(int player) {
		display.printMessage("Select a category for this roll.");
		while (true) {
			int category = display.waitForPlayerToSelectCategory();
			if (categoriesMatrix[player - 1][category - 1] == -1) {
				sumingTotal(player, category);
				display.updateScorecard(category, player, getScore(category));
				display.updateScorecard(TOTAL, player, categoriesMatrix[player - 1][TOTAL - 1]);
				break;
			} else {
				display.printMessage("Oe debilo, sxva unda airchio!!!");
			}
		}
	}

	/**
	 * Pre Condition - User has selected category
	 * 
	 * Post Condition - UPPER_SCORE,LOWER_SCORE,TOTAL is updated in matrix
	 * 
	 * @param player
	 * @param category
	 */
	private void sumingTotal(int player, int category) {
		categoriesMatrix[player - 1][category - 1] = getScore(category);

		categoriesMatrix[player - 1][UPPER_SCORE - 1] += category - 1 < UPPER_SCORE - 1 ? getScore(category) : 0;
		categoriesMatrix[player - 1][UPPER_BONUS - 1] = categoriesMatrix[player - 1][UPPER_SCORE - 1] >= 63 ? 35 : 0;

		categoriesMatrix[player - 1][LOWER_SCORE - 1] += category - 1 > UPPER_BONUS - 1 ? getScore(category) : 0;

		categoriesMatrix[player - 1][TOTAL - 1] = categoriesMatrix[player - 1][UPPER_SCORE - 1]
				+ categoriesMatrix[player - 1][UPPER_BONUS - 1] + categoriesMatrix[player - 1][LOWER_SCORE - 1];
	}

	/**
	 * Pre Condition - Player has chosen category
	 * 
	 * Post Condition - Checks if everything is filled and returns relevant
	 * boolean
	 * 
	 * @return
	 */
	private boolean checkFinishConditions() {
		for (int i = 0; i < categoriesMatrix.length; i++) {
			for (int j = 0; j < categoriesMatrix[i].length; j++) {
				if (categoriesMatrix[i][j] == -1)
					return (false);
			}
		}
		return (true);
	}

	/**
	 * Pre Condition - Player used all rolling
	 * 
	 * Post Condition - Player chooses category and gets relevant points
	 * 
	 * @param category
	 * @return score
	 */
	private int getScore(int category) {
		int score = 0;
		if (category <= SIXES) {
			score = upperScoreChecker(category);
		} else if (category < SMALL_STRAIGHT || category == YAHTZEE) {
			score = sameKindChecker(category);
		} else if (category <= LARGE_STRAIGHT) {
			score = straightChecker(category);
		} else if (category == CHANCE) {
			score = sumArr(dices);
		}
		return score;
	}

	/**
	 * Pre Condition - User has chosen LARGE_STRAIGHT or SMALL_STRAIGHT
	 * 
	 * Post Condition - He gets relevant scores
	 * 
	 * @param category
	 * @return score
	 */
	private int straightChecker(int category) {
		Arrays.sort(dices);
		int countOnes = 0;

		int[] substractArr = new int[4];
		// Straightness means that it's increasing by one
		for (int i = 0; i < dices.length - 1; i++) {
			substractArr[i] = dices[i + 1] - dices[i];
		}

		for (int value : substractArr) {
			if (value == 1) {
				countOnes++;
			} else if (value != 0 && countOnes < 3) {
				countOnes = 0;
			}
		}
		/*
		 * Number of every subtraction is four, so if all of them are 1's, it's
		 * Large Straight. Same Logic applies to Small Straight
		 */
		if (category == LARGE_STRAIGHT && countOnes == 4) {
			return 40;
		} else if (category == SMALL_STRAIGHT && countOnes >= 3) {
			return 30;
		}
		return 0;
	}

	/**
	 * Pre Condition - Player has chosen THREE_OF_A_KIND, FOUR_OF_A_KIND,
	 * YAHTZEE or FULL_HOUSE
	 * 
	 * Post Condition - User gets relevant score
	 * 
	 * @return score
	 */
	private int sameKindChecker(int category) {
		int score = 0;
		int[][] duplicateCount = duplicateCounter(dices);

		if (category == FULL_HOUSE && checkingEveryDuplicate(duplicateCount, 3)
				&& checkingEveryDuplicate(duplicateCount, 2)) {
			score = 25;
		} else if (checkingEveryDuplicate(duplicateCount, 5) && category == YAHTZEE) {
			score = 50;
		} else if ((checkingEveryDuplicate(duplicateCount, 3) || checkingEveryDuplicate(duplicateCount, 4)
				|| checkingEveryDuplicate(duplicateCount, 5)) && category == THREE_OF_A_KIND) {
			score = sumArr(dices);
		} else if ((checkingEveryDuplicate(duplicateCount, 4) || checkingEveryDuplicate(duplicateCount, 5))
				&& category == FOUR_OF_A_KIND) {
			score = sumArr(dices);
		}
		return score;
	}

	/**
	 * Sums every int in array
	 * 
	 * @param a
	 *            array
	 * @return sum
	 */
	private int sumArr(int[] a) {
		int score = 0;
		for (int val : a) {
			score += val;
		}
		return score;
	}

	/**
	 * Pre Condition - We have array of dices
	 * 
	 * Post Condition - We have matrix of dices, with two columns and five rows.
	 * first column(0 index) tells us value of dice and the second(1 index) how
	 * many element we have with that value.
	 * 
	 * @param d
	 *            dice array
	 * @return matrix
	 */
	private int[][] duplicateCounter(int[] d) {
		ArrayList<Integer> arr = new ArrayList<Integer>();
		int[][] duplicateCount = new int[6][2];
		// Adds every dice to the array
		for (int i = 0; i < duplicateCount.length; i++) {
			duplicateCount[i][0] = i + 1;
		}
		for (int value : d) {
			arr.add(value);
		}

		for (int i = 0; i < arr.size(); i++) {
			int value = arr.get(i);
			while (arr.indexOf(value) != -1) {
				// Finds value in duplicateArray
				for (int j = 0; j < duplicateCount.length; j++) {
					if (duplicateCount[j][0] == value)
						duplicateCount[j][1]++;
				}
				arr.remove(arr.indexOf(value));
			}
		}
		return duplicateCount;
	}

	/**
	 * Checks if there is subarray with value x at index 1
	 * 
	 * @param m
	 *            matrix
	 * @param x
	 *            int of how many copies of value we have(index 1)
	 * @return true if exists with this amount of copies
	 */
	private boolean checkingEveryDuplicate(int[][] m, int x) {
		for (int i = 0; i < m.length; i++) {
			if (m[i][1] == x)
				return true;
		}
		return false;
	}

	/**
	 * Pre Condition - Player has chosen upper part of categories
	 * 
	 * Post Condition - Player gets relevant scores of chosen category
	 * 
	 * @param category
	 * @return score
	 */
	private int upperScoreChecker(int category) {
		int score = 0;
		for (int i : dices) {
			score += i == category ? i : 0;
		}
		return score;
	}

	/**
	 * Pre Condition - Dices aren't rolled or it's additional roll
	 * 
	 * Post Condition - Dices are rolled which is saved in integer's array
	 * 
	 * @param firstRoll
	 *            boolean which tells if it's first roll or not
	 * @return int[] of dices
	 */
	private int[] rollDice(boolean firstRoll) {
		for (int i = 0; i < 5; i++) {
			dices[i] = display.isDieSelected(i) == true || firstRoll == true ? rgen.nextInt(1, 6) : dices[i];
		}
		return dices;
	}

	/* Private instance variables */
	private int nPlayers;
	private String[] playerNames;
	private YahtzeeDisplay display;
	private RandomGenerator rgen = new RandomGenerator();
	private int[] dices = new int[N_DICE];
	private int[][] categoriesMatrix;
	private String THEMOSTIMPORTANTSTRING = "https://www.youtube.com/watch?v=o-YBDTqX_ZU&ab_channel=MusRest";
}
