import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Scanner;

/**
 * <p>An application to calculate the win rate of the first player listed in the
 * {@code players.txt} file. It simulates numerous Uno games and tracks how often
 * the first player emerges victorious. The win percentage is then calculated and
 * printed to the console.</p>
 *
 * <p>The simulation parameters, such as the number of simulations and games per
 * simulation, are defined as constants within the class.</p>
 *
 * @since 1.0
 */
public class WinRateCalculator {

    /**
     * <p>The name of the file containing the list of players and their
     * corresponding UnoPlayer class prefixes. This file should be located
     * in the "lib" directory relative to the working directory. Each line
     * in the file should follow the format: "PlayerName,ClassPrefix". For
     * instance, a line might look like "Alice,Amazing".</p>
     */
    public static final String PLAYER_FILENAME = "lib/players.txt";

    /**
     * <p>The number of Uno simulations to run. Each simulation consists of
     * multiple games.</p>
     */
    public static final int NUM_SIMULATIONS = 100;

    /**
     * <p>The number of Uno games to play within each simulation.</p>
     */
    public static final int GAMES_PER_SIMULATION = 20;

    /**
     * <p>Main method to run the Uno win rate simulation. Loads player data
     * from the specified file, runs the simulations, calculates the win
     * percentage of the first player listed, and prints the results to
     * the console.</p>
     *
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {

        try {
            // Load player data and retrieve the first player's name
            ArrayList<String> playerNames = new ArrayList<>();
            ArrayList<String> playerClasses = new ArrayList<>();
            loadPlayerData(playerNames, playerClasses);
            String firstPlayerName = playerNames.get(0);

            int firstPlayerWins = 0;
            for (int i = 0; i < NUM_SIMULATIONS; i++) {
                System.out.println("Starting Uno Simulation #" + (i + 1));
                firstPlayerWins += runSimulation(playerNames, playerClasses);
            }

            double winPercentage = (double) firstPlayerWins / (NUM_SIMULATIONS * GAMES_PER_SIMULATION) * 100;
            System.out.println("\n--------------------");
            System.out.println("Overall Win Percentage for " + firstPlayerName + ": " + winPercentage + "%");
            System.out.println("--------------------\n");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * <p>Executes a single Uno simulation, consisting of a predefined number of games.
     * Tracks the number of wins for the first player in the provided list.</p>
     *
     * @param playerNames The list of player names participating in the simulation.
     * @param playerClasses The list of player class prefixes corresponding to each player.
     * @return The number of games won by the first player in the simulation.
     * @throws Exception If there is an error during the simulation.
     */
    private static int runSimulation(ArrayList<String> playerNames, ArrayList<String> playerClasses) throws Exception {
        int firstPlayerWinsInSimulation = 0;
        Scoreboard s = new Scoreboard(playerNames.toArray(new String[0]));

        for (int i = 0; i < GAMES_PER_SIMULATION; i++) {
            Game g = new Game(s, playerClasses);
            g.play();

            // Check if the first player won the game
            if (s.getWinner() == 0) {
                firstPlayerWinsInSimulation++;
            }
        }

        System.out.println(playerNames.get(0) + " wins in this simulation: " + firstPlayerWinsInSimulation);
        return firstPlayerWinsInSimulation;
    }

    /**
     * <p>Loads player data (names and class prefixes) from the specified file.</p>
     *
     * @param playerNames The list to populate with player names.
     * @param playerClasses The list to populate with player class prefixes.
     * @throws Exception If there is an error reading the player data file.
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
}