import java.util.*;

/**
 * <p>
 * An implementation of the UnoPlayer interface for the Uno game. This player
 * employs a strategic approach to maximize its chances of winning, incorporating
 * opponent modeling, strategic card usage, and defensive play to perform well
 * even in multi-player scenarios. It tracks opponent card choices, including
 * their preference for colors and frequency of action card usage, to make more
 * informed decisions.
 * </p>
 *
 * @since 1.0
 */
public class SkulaA_UnoPlayer implements UnoPlayer {

    private Map<Color, Integer> colorCounts;
    private Map<Rank, Integer> remainingRankCounts;
    private Color[] opponentLastCalledColors;
    private int[] opponentActionCardFrequency;

    /**
     * Constructs a new SkulaA_UnoPlayer with initialized game state trackers.
     * It sets up data structures to track card counts, opponent color calls,
     * and opponent action card usage frequency.
     */
    public SkulaA_UnoPlayer() {
        colorCounts = new HashMap<>();
        remainingRankCounts = new HashMap<>();
        opponentLastCalledColors = new Color[4]; // Assuming max 4 players
        opponentActionCardFrequency = new int[4];

        // Initialize card counts for a standard Uno deck
        for (Color color : Color.values()) {
            if (color != Color.NONE) {
                colorCounts.put(color, 19);
            }
        }
        for (Rank rank : Rank.values()) {
            switch (rank) {
                case NUMBER:
                    remainingRankCounts.put(rank, 36);
                    break;
                case SKIP:
                case REVERSE:
                case DRAW_TWO:
                    remainingRankCounts.put(rank, 8);
                    break;
                case WILD:
                case WILD_D4:
                    remainingRankCounts.put(rank, 4);
                    break;
            }
        }
    }

    /**
     * Determines and returns the index of the most strategically valuable card to
     * play from the hand. The decision considers opponent behavior, card availability,
     * and the current game state.
     *
     * @param hand        The list of cards in the player's hand.
     * @param upCard      The current upcard on the game pile.
     * @param calledColor The color called by the previous player if the upcard was Wild.
     * @param state       The current game state.
     * @return The index of the card to play, or -1 if no card can be played.
     */
    public int play(List<Card> hand, Card upCard, Color calledColor, GameState state) {
        updateRemainingCardCounts(state.getPlayedCards());
        updateOpponentCalledColors(state.getMostRecentColorCalledByUpcomingPlayers());
        updateOpponentActionCardFrequency(state);

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
     * Evaluates a card's strategic value based on the current game state and
     * opponent behavior. A higher score indicates a more valuable card to play.
     * The evaluation considers factors like card matching, action card potential,
     * opponent card counts, and potential point loss.
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

        // Wild Draw 4 Strategy: Prioritize using it against opponents with many cards
        // or those who frequently use action cards
        if (card.getRank() == Rank.WILD_D4) {
            score += 50;
            for (int i = 0; i < state.getNumCardsInHandsOfUpcomingPlayers().length; i++) {
                score += 10 * state.getNumCardsInHandsOfUpcomingPlayers()[i];
                score += 20 * opponentActionCardFrequency[i];
            }
            if (state.getNumCardsInHandsOfUpcomingPlayers()[0] <= 2) {
                score += 100;
            }
            return score; // Prioritize WILD_D4 in these situations
        }

        // Prioritize winning, but be cautious about emptying hand too early
        // when there are multiple opponents
        if (hand.size() <= 3) {
            score += 30;
            score -= 5 * (state.getNumCardsInHandsOfUpcomingPlayers().length - 1);
        }

        // Action Card Strategy: More valuable when opponents have many cards
        if (card.getRank() == Rank.DRAW_TWO || card.getRank() == Rank.SKIP || card.getRank() == Rank.REVERSE) {
            score += 20;
            for (int cards : state.getNumCardsInHandsOfUpcomingPlayers()) {
                score += 5 * cards;
            }
        }

        // Card Depletion: Gain points for playing matching cards
        if (card.getColor() == upCard.getColor()) {
            score += 15;
        }
        if (card.getRank() == upCard.getRank() && card.getRank() != Rank.NUMBER) {
            score += 12;
        }
        if (card.getRank() == Rank.NUMBER && upCard.getRank() == Rank.NUMBER && card.getNumber() == upCard.getNumber()) {
            score += 10;
        }

        // Called Color Strategy: Align with the called color after a Wild card
        if (upCard.getRank() == Rank.WILD || upCard.getRank() == Rank.WILD_D4) {
            if (card.getColor() == calledColor) {
                score += 12;
            }
        }

        // Card Advantage: Play cards of a color you have many of
        if (card.getColor() != Color.NONE) {
            score += colorCounts.getOrDefault(card.getColor(), 0);
        }

        // Opponent Card Deduction: Play cards opponents likely don't have
        if (card.getColor() != Color.NONE && colorCounts.get(card.getColor()) <= 5) {
            score += 15;
            for (Color opponentColor : opponentLastCalledColors) {
                if (opponentColor == card.getColor()) {
                    score -= 8; // Less valuable if an opponent called this color recently
                }
            }
        }

        // Minimize Point Loss: Be more cautious about point loss in the late game
        if (state.getTotalScoreOfUpcomingPlayers()[state.getTotalScoreOfUpcomingPlayers().length - 1] >= 400) {
            score -= card.forfeitCost() * 2;
        }

        return score;
    }

    /**
     * Determines the color to call after playing a Wild card.
     * It chooses the most frequent color in the player's hand.
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

        Color mostFrequentColor = Color.RED; // Default
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
     * Updates the internal trackers for the remaining card counts based on the played
     * cards. This helps the AI deduce which cards are less likely to be in opponents' hands.
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
     * Updates the tracker for colors called by opponents, which is used to infer
     * potential card holdings and play patterns.
     *
     * @param recentCalledColors An array of colors recently called by opponents.
     */
    private void updateOpponentCalledColors(Color[] recentCalledColors) {
        if (recentCalledColors != null) {
            opponentLastCalledColors = recentCalledColors;
        }
    }

    /**
     * Updates the tracker for the frequency of action card usage by opponents. This
     * information is used to assess risk levels and make strategic decisions,
     * particularly regarding the use of Wild Draw 4 cards.
     *
     * @param state The current game state.
     */
    private void updateOpponentActionCardFrequency(GameState state) {
        // Basic implementation: Increment frequency when an opponent calls a color,
        // implying an action card was played
        int numPlayers = state.getNumCardsInHandsOfUpcomingPlayers().length;
        for (int i = 1; i < numPlayers; i++) { // Skip current player
            if (state.getMostRecentColorCalledByUpcomingPlayers()[i] != Color.NONE) {
                opponentActionCardFrequency[i]++;
            }
        }
    }
}