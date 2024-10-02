import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * An implementation of the UnoPlayer interface for the Uno game. This player
 * employs a strategic approach to maximize its chances of winning. It prioritizes
 * playing cards to deplete its hand quickly, especially when close to winning.
 * It considers various factors such as the current upcard, the called color (if any),
 * the number of cards in opponents' hands, and the overall game state to make
 * informed decisions.
 * </p>
 * @since 1.0
 */
public class SkulaA_UnoPlayer implements UnoPlayer {

    private Map<Color, Integer> colorCounts;
    private Map<Rank, Integer> remainingRankCounts;
    private Color[] opponentLastCalledColors;

    /**
     * Constructs a new SkulaA_UnoPlayer with initialized game state trackers.
     */
    public SkulaA_UnoPlayer() {
        colorCounts = new HashMap<>();
        remainingRankCounts = new HashMap<>();
        opponentLastCalledColors = new Color[4]; // Assuming max 4 players

        // Initialize card counts (assuming standard Uno deck)
        for (Color color : Color.values()) {
            if (color != Color.NONE) {
                colorCounts.put(color, 19); // 19 cards of each color
            }
        }
        for (Rank rank : Rank.values()) {
            switch (rank) {
                case NUMBER:
                    remainingRankCounts.put(rank, 36); // 36 number cards
                    break;
                case SKIP:
                case REVERSE:
                case DRAW_TWO:
                    remainingRankCounts.put(rank, 8); // 8 of each action card
                    break;
                case WILD:
                case WILD_D4:
                    remainingRankCounts.put(rank, 4); // 4 Wild and 4 Wild Draw 4
                    break;
            }
        }
    }

    /**
     * Determines and returns the index of the most strategically valuable card to
     * play from the hand.
     *
     * @param hand        The list of cards in the player's hand.
     * @param upCard      The current upcard on the game pile.
     * @param calledColor The color called by the previous player if the upcard was a
     *                   Wild card.
     * @param state       The current game state.
     * @return The index of the card to play, or -1 if no card can be played.
     */
    public int play(List<Card> hand, Card upCard, Color calledColor, GameState state) {
        updateRemainingCardCounts(state.getPlayedCards());
        updateOpponentCalledColors(state.getMostRecentColorCalledByUpcomingPlayers());

        int bestCardIndex = -1;
        int bestCardScore = Integer.MIN_VALUE;

        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            if (card.canPlayOn(upCard, calledColor)) {
                int cardScore = evaluateCard(card, upCard, calledColor, state, hand);
                if (cardScore > bestCardScore) {
                    bestCardIndex = i;
                    bestCardScore = cardScore;
                }
            }
        }

        return bestCardIndex;
    }

    /**
     * Evaluates a card's strategic value based on the current game state.
     * A higher score indicates a more valuable card to play.
     *
     * @param card        The card being evaluated.
     * @param upCard      The current upcard.
     * @param calledColor The called color (if any).
     * @param state       The current game state.
     * @param hand        The player's current hand.
     * @return The strategic value of the card.
     */
    private int evaluateCard(Card card, Card upCard, Color calledColor, GameState state, List<Card> hand) {
        int score = 0;

        // Prioritize playing Wild Draw 4 strategically
        if (card.getRank() == Rank.WILD_D4) {
            score += 50 + 10 * state.getNumCardsInHandsOfUpcomingPlayers()[0];
            if (state.getNumCardsInHandsOfUpcomingPlayers()[0] <= 2) {
                score += 100;
            }
            return score;
        }

        // Prioritize winning
        if (hand.size() <= 3) {
            if (card.getColor() == upCard.getColor()) {
                score += 30;
            }
            if (card.getRank() == upCard.getRank()) {
                score += 25;
            }
        }

        // Action cards
        if (card.getRank() == Rank.DRAW_TWO || card.getRank() == Rank.SKIP || card.getRank() == Rank.REVERSE) {
            score += 20 + 5 * state.getNumCardsInHandsOfUpcomingPlayers()[0];
        }

        // Card Depletion
        if (card.getColor() == upCard.getColor()) {
            score += 15;
        }
        if (card.getRank() == upCard.getRank() && card.getRank() != Rank.NUMBER) {
            score += 12;
        }
        if (card.getRank() == Rank.NUMBER && upCard.getRank() == Rank.NUMBER && card.getNumber() == upCard.getNumber()) {
            score += 10;
        }

        // Called color after wild
        if (upCard.getRank() == Rank.WILD || upCard.getRank() == Rank.WILD_D4) {
            if (card.getColor() == calledColor) {
                score += 12;
            }
        }

        // Play cards you have many of
        if (card.getColor() != Color.NONE) {
            score += colorCounts.getOrDefault(card.getColor(), 0);
        }

        // Play cards opponents likely don't have
        if (card.getColor() != Color.NONE && colorCounts.get(card.getColor()) <= 5) {
            score += 15;
            for (Color opponentColor : opponentLastCalledColors) {
                if (opponentColor == card.getColor()) {
                    score -= 8;
                }
            }
        }

        // Minimize point loss
        if (state.getTotalScoreOfUpcomingPlayers()[state.getTotalScoreOfUpcomingPlayers().length - 1] >= 400) {
            score -= card.forfeitCost() * 2;
        }

        return score;
    }

    /**
     * Determines and returns the color to call after playing a Wild card.
     * This implementation chooses the most frequent color in the player's hand.
     *
     * @param hand The player's current hand.
     * @return The chosen color to call.
     */
    public Color callColor(List<Card> hand) {
        Map<Color, Integer> handColorCounts = new HashMap<>();
        for (Card card : hand) {
            if (card.getColor() != Color.NONE) {
                handColorCounts.put(card.getColor(), handColorCounts.getOrDefault(card.getColor(), 0) + 1);
            }
        }

        Color mostFrequentColor = Color.RED; // Default to RED
        int maxCount = 0;
        for (Map.Entry<Color, Integer> entry : handColorCounts.entrySet()) {
            if (entry.getValue() > maxCount) {
                mostFrequentColor = entry.getKey();
                maxCount = entry.getValue();
            }
        }

        return mostFrequentColor;
    }

    /**
     * Updates internal trackers for the remaining card counts based on the played
     * cards.
     *
     * @param playedCards The list of cards played so far in the game.
     */
    private void updateRemainingCardCounts(List<Card> playedCards) {
        for (Card card : playedCards) {
            if (card.getColor() != Color.NONE) {
                colorCounts.put(card.getColor(), colorCounts.get(card.getColor()) - 1);
            }
            remainingRankCounts.put(card.getRank(), remainingRankCounts.get(card.getRank()) - 1);
        }
    }

    /**
     * Updates the tracker for colors called by opponents based on recent calls.
     *
     * @param recentCalledColors An array of colors recently called by opponents.
     */
    private void updateOpponentCalledColors(Color[] recentCalledColors) {
        if (recentCalledColors != null) {
            opponentLastCalledColors = recentCalledColors;
        }
    }
}