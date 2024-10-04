import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Scanner;

public class WinRateCalculator {

    public static final String PLAYER_FILENAME = "lib/players.txt";

    // Total # of games = NUM_SIMULATIONS * GAMES_PER_SIMULATION
    public static final int NUM_SIMULATIONS = 100;
    public static final int GAMES_PER_SIMULATION = 20;

    public static void main(String[] args) {

        try {
            // Load player data and get the first player's name
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