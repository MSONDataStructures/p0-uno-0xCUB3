import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class SkulaA_UnoPlayer implements UnoPlayer {

    private Map<Color, Integer> playedColorCounts;
    private Map<Rank, Integer> playedRankCounts;
    private Color[] opponentLastCalledColors;

    public SkulaA_UnoPlayer() {
        playedColorCounts = new HashMap<>();
        playedRankCounts = new HashMap<>();
        opponentLastCalledColors = new Color[4]; // Assuming max 4 players
    }

    public int play(List<Card> hand, Card upCard, Color calledColor, GameState state) {
        updatePlayedCardCounts(state.getPlayedCards());
        updateOpponentCalledColors(state.getMostRecentColorCalledByUpcomingPlayers());

        int bestCardIndex = -1;
        int bestCardScore = -1000; // Initialize with a very low score

        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            int cardScore = evaluateCard(card, upCard, calledColor, state);

            if (cardScore > bestCardScore && card.canPlayOn(upCard, calledColor)) {
                bestCardIndex = i;
                bestCardScore = cardScore;
            }
        }

        return bestCardIndex;
    }

    private int evaluateCard(Card card, Card upCard, Color calledColor, GameState state) {
        int score = 0;

        // 1. Prioritize Wild Draw 4: Play it strategically
        if (card.getRank() == Rank.WILD_D4) {
            score += 50; // Base score for Wild Draw 4
            score += 5 * state.getNumCardsInHandsOfUpcomingPlayers()[0]; // Punish the next player
            // Consider playing it even if you have other options if the next player is close to winning
            if (state.getNumCardsInHandsOfUpcomingPlayers()[0] <= 2) {
                score += 100;
            }
            return score;
        }

        // 2. Prioritize Action Cards: Disrupt opponents
        if (card.getRank() == Rank.DRAW_TWO || card.getRank() == Rank.SKIP || card.getRank() == Rank.REVERSE) {
            score += 20; // Base score for action cards
            score += 3 * state.getNumCardsInHandsOfUpcomingPlayers()[0]; // Punish the next player
        }

        // 3. Match Upcard Color:  Good to continue the trend
        if (card.getColor() == upCard.getColor()) {
            score += 10;
        }

        // 4. Match Called Color: Good if a wild was just played
        if (upCard.getRank() == Rank.WILD || upCard.getRank() == Rank.WILD_D4) {
            if (card.getColor() == calledColor) {
                score += 8;
            }
        }

        // 5. Match Rank (if not number): Continue the action
        if (card.getRank() == upCard.getRank() && card.getRank() != Rank.NUMBER) {
            score += 8;
        }

        // 6. Match Number (if both are numbers)
        if (card.getRank() == Rank.NUMBER && upCard.getRank() == Rank.NUMBER &&
                card.getNumber() == upCard.getNumber()) {
            score += 5;
        }

        // 7. Play Cards You Have Many Of:  Call that color later
        if (card.getColor() != Color.NONE) {
            score += playedColorCounts.getOrDefault(card.getColor(), 0);
        }

        // 8. Avoid Cards Opponents Might Want
        if (card.getColor() != Color.NONE) {
            for (Color opponentColor : opponentLastCalledColors) {
                if (opponentColor == card.getColor()) {
                    score -= 5; // Penalize playing a color an opponent might want
                }
            }
        }

        // 9. Minimize Point Loss: If close to losing, get rid of high-point cards
        int totalScore = state.getTotalScoreOfUpcomingPlayers()[state.getTotalScoreOfUpcomingPlayers().length - 1];
        if (totalScore >= 400) {
            score -= card.forfeitCost(); // Try to get rid of high-value cards
        }

        return score;
    }

    public Color callColor(List<Card> hand) {
        // Call the color you have the most of
        return getMostFrequentColor(hand);
    }

    // Helper functions to update card counts and opponent called colors
    private void updatePlayedCardCounts(List<Card> playedCards) {
        playedColorCounts.clear();
        playedRankCounts.clear();

        for (Card card : playedCards) {
            playedColorCounts.put(card.getColor(), playedColorCounts.getOrDefault(card.getColor(), 0) + 1);
            playedRankCounts.put(card.getRank(), playedRankCounts.getOrDefault(card.getRank(), 0) + 1);
        }
    }

    private void updateOpponentCalledColors(Color[] recentCalledColors) {
        // Handle potential null values in recentCalledColors
        if (recentCalledColors != null) {
            opponentLastCalledColors = recentCalledColors;
        }
    }

    private Color getMostFrequentColor(List<Card> hand) {
        Map<Color, Integer> colorCounts = new HashMap<>();

        // Count the number of cards of each color in the hand
        for (Card card : hand) {
            if (card.getColor() != Color.NONE) {
                colorCounts.put(card.getColor(), colorCounts.getOrDefault(card.getColor(), 0) + 1);
            }
        }

        // Find the color with the most cards
        int maxCount = 0;
        Color mostFrequentColor = Color.RED; // Default
        for (Color color : colorCounts.keySet()) {
            if (colorCounts.get(color) > maxCount) {
                mostFrequentColor = color;
                maxCount = colorCounts.get(color);
            }
        }

        return mostFrequentColor;
    }
}