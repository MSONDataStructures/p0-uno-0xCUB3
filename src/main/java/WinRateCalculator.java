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
        int alexanderWins = 0;

        try {
            for (int i = 0; i < NUM_SIMULATIONS; i++) {
                System.out.println("Starting Uno Simulation #" + (i + 1));
                alexanderWins += runSimulation();
            }

            double winPercentage = (double) alexanderWins / (NUM_SIMULATIONS * GAMES_PER_SIMULATION) * 100;
            System.out.println("\n--------------------");
            System.out.println("Overall Win Percentage for Alexander: " + winPercentage + "%");
            System.out.println("--------------------\n");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int runSimulation() throws Exception {
        ArrayList<String> playerNames = new ArrayList<String>();
        ArrayList<String> playerClasses = new ArrayList<String>();
        loadPlayerData(playerNames, playerClasses);

        int alexanderWinsInSimulation = 0;
        Scoreboard s = new Scoreboard(playerNames.toArray(new String[0]));

        for (int i = 0; i < GAMES_PER_SIMULATION; i++) {
            Game g = new Game(s, playerClasses);
            g.play();

            // Check if Alexander won the game
            if (s.getPlayerList()[s.getWinner()].equals("Alexander")) {
                alexanderWinsInSimulation++;
            }
        }

        System.out.println("Alexander wins in this simulation: " + alexanderWinsInSimulation);
        return alexanderWinsInSimulation;
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