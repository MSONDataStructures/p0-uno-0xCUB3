import java.util.*;
import java.io.*;

/**
 * <p>An application to calculate and visualize the win rates of all
 * players listed in the {@code players.txt} file. It simulates numerous
 * Uno games, tracks wins, and provides a text-based bar graph to display
 * the final win percentage for each player.</p>
 *
 * @since 1.0
 */
public class WinRateCalculator {

    public static final String PLAYER_FILENAME = "lib/players.txt";
    public static final int NUM_SIMULATIONS = 100;
    public static final int GAMES_PER_SIMULATION = 20;

    /**
     * Main method to run the Uno win rate simulation and visualization.
     *
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {
        try {
            ArrayList<String> playerNames = new ArrayList<>();
            ArrayList<String> playerClasses = new ArrayList<>();
            loadPlayerData(playerNames, playerClasses);

            int numPlayers = playerNames.size();
            int[] playerWins = new int[numPlayers]; // Track wins for each player

            for (int i = 0; i < NUM_SIMULATIONS; i++) {
                runSimulation(playerNames, playerClasses, playerWins);
            }

            System.out.println("\n--------------------");
            System.out.println("Overall Win Percentages:");
            for (int i = 0; i < numPlayers; i++) {
                double winPercentage = (double) playerWins[i] / (NUM_SIMULATIONS * GAMES_PER_SIMULATION) * 100;
                printWinPercentageBar(playerNames.get(i), winPercentage);
            }
            System.out.println("--------------------\n");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Executes a single Uno simulation and updates the playerWins array.
     *
     * @param playerNames The list of player names.
     * @param playerClasses The list of player class prefixes.
     * @param playerWins The array to store the number of wins for each player.
     * @throws Exception If an error occurs during the simulation.
     */
    private static void runSimulation(ArrayList<String> playerNames,
                                      ArrayList<String> playerClasses,
                                      int[] playerWins) throws Exception {

        Scoreboard s = new Scoreboard(playerNames.toArray(new String[0]));
        for (int i = 0; i < GAMES_PER_SIMULATION; i++) {
            Game g = new Game(s, playerClasses);
            g.play();
            playerWins[s.getWinner()]++;
        }
    }

    /**
     * Loads player data from the specified file.
     *
     * @param playerNames The list to store player names.
     * @param playerClasses The list to store player class prefixes.
     * @throws Exception If an error occurs while reading the player file.
     */
    private static void loadPlayerData(ArrayList<String> playerNames, ArrayList<String> playerClasses)
            throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(PLAYER_FILENAME));
        String playerLine = br.readLine();
        while (playerLine != null) {
            Scanner line = new Scanner(playerLine).useDelimiter(",");
            playerNames.add(line.next());
            playerClasses.add(line.next() + "_UnoPlayer");
            playerLine = br.readLine();
        }
    }

    /**
     * Prints a bar representing the win percentage for a player.
     *
     * @param playerName The name of the player.
     * @param winPercentage The win percentage to visualize.
     */
    private static void printWinPercentageBar(String playerName, double winPercentage) {
        System.out.print(String.format("%-15s: ", playerName)); // Adjust formatting if needed
        for (int j = 0; j < winPercentage; j += 2) { // Each '#' represents 2%
            System.out.print("█");
        }
        System.out.println(String.format(" (%.1f%%)", winPercentage));
    }
}