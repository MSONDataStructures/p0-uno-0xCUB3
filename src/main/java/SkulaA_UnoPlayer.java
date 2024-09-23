import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class SkulaA_UnoPlayer implements UnoPlayer {

    private Map<Color, Integer> colorCounts;
    private Map<Rank, Integer> remainingRankCounts;
    private Color[] opponentLastCalledColors;

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

    public int play(List<Card> hand, Card upCard, Color calledColor, GameState state) {
        updateRemainingCardCounts(state.getPlayedCards());
        updateOpponentCalledColors(state.getMostRecentColorCalledByUpcomingPlayers());

        int bestCardIndex = -1;
        int bestCardScore = -1000; // Initialize with a very low score

        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            int cardScore = evaluateCard(card, upCard, calledColor, state, hand);

            if (cardScore > bestCardScore && card.canPlayOn(upCard, calledColor)) {
                bestCardIndex = i;
                bestCardScore = cardScore;
            }
        }

        return bestCardIndex;
    }

    private int evaluateCard(Card card, Card upCard, Color calledColor, GameState state, List<Card> hand) {
        int score = 0;

        // 1.  Wild Draw 4 Priority: Strategic and Aggressive
        if (card.getRank() == Rank.WILD_D4) {
            score += 50; // Base score
            score += 10 * state.getNumCardsInHandsOfUpcomingPlayers()[0]; // Heavy punishment
            if (state.getNumCardsInHandsOfUpcomingPlayers()[0] <= 2) {
                score += 100; // Play even with other options if next player is close to winning
            }
            return score; // High priority, no need to evaluate further
        }

        // 2. Prioritize Winning:
        if (hand.size() <= 3) {
            // If close to winning, focus heavily on matching color and rank
            if (card.getColor() == upCard.getColor()) {
                score += 30;
            }
            if (card.getRank() == upCard.getRank()) {
                score += 25;
            }
        }

        // 3. Action Cards: Disrupt and Deplete
        if (card.getRank() == Rank.DRAW_TWO || card.getRank() == Rank.SKIP || card.getRank() == Rank.REVERSE) {
            score += 20; // Base score
            score += 5 * state.getNumCardsInHandsOfUpcomingPlayers()[0]; // Punish the next player
        }

        // 4. Card Depletion: Get rid of cards quickly
        if (card.getColor() == upCard.getColor()) {
            score += 15; // Prioritize matching color
        }
        if (card.getRank() == upCard.getRank() && card.getRank() != Rank.NUMBER) {
            score += 12; // Prioritize matching rank (non-number cards)
        }
        if (card.getRank() == Rank.NUMBER && upCard.getRank() == Rank.NUMBER &&
                card.getNumber() == upCard.getNumber()) {
            score += 10; // Prioritize matching number
        }

        // 5. Called Color After Wild (if applicable)
        if (upCard.getRank() == Rank.WILD || upCard.getRank() == Rank.WILD_D4) {
            if (card.getColor() == calledColor) {
                score += 12; // Match the called color
            }
        }

        // 6. Play Cards You Have Many Of (for later Wild calls)
        if (card.getColor() != Color.NONE) {
            score += colorCounts.getOrDefault(card.getColor(), 0);
        }

        // 7. Play Cards Opponents Likely Don't Have:
        if (card.getColor() != Color.NONE) {
            if (colorCounts.get(card.getColor()) <= 5) { // If few of this color remain
                score += 15;
            }
            for (Color opponentColor : opponentLastCalledColors) {
                if (opponentColor == card.getColor()) {
                    score -= 8; // Penalize playing a color an opponent might want
                }
            }
        }

        // 8. Minimize Point Loss:  If losing, get rid of high cards
        if (state.getTotalScoreOfUpcomingPlayers()[state.getTotalScoreOfUpcomingPlayers().length - 1] >= 400) {
            score -= card.forfeitCost() * 2; // Prioritize point minimization when losing
        }

        return score;
    }

    public Color callColor(List<Card> hand) {
        // 1.  Force Opponent Draws:
        for (int i = 0; i < opponentLastCalledColors.length - 1; i++) {
            Color opponentColor = opponentLastCalledColors[i];
            if (opponentColor != null && colorCounts.get(opponentColor) == 0) {
                return opponentColor; // Call a color the next opponent is out of
            }
        }

        // 2. If no guaranteed force, call the color you have the most of
        return getMostFrequentColor(hand);
    }

    // Helper functions
    private void updateRemainingCardCounts(List<Card> playedCards) {
        // Reset counts to standard deck values
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

        // Decrement counts for played cards
        for (Card card : playedCards) {
            if (card.getColor() != Color.NONE) {
                colorCounts.put(card.getColor(), colorCounts.get(card.getColor()) - 1);
            }
            remainingRankCounts.put(card.getRank(), remainingRankCounts.get(card.getRank()) - 1);
        }
    }

    private void updateOpponentCalledColors(Color[] recentCalledColors) {
        if (recentCalledColors != null) {
            opponentLastCalledColors = recentCalledColors;
        }
    }

    private Color getMostFrequentColor(List<Card> hand) {
        Map<Color, Integer> colorCounts = new HashMap<>();
        for (Card card : hand) {
            if (card.getColor() != Color.NONE) {
                colorCounts.put(card.getColor(), colorCounts.getOrDefault(card.getColor(), 0) + 1);
            }
        }
        int maxCount = 0;
        Color mostFrequentColor = Color.RED;
        for (Color color : colorCounts.keySet()) {
            if (colorCounts.get(color) > maxCount) {
                mostFrequentColor = color;
                maxCount = colorCounts.get(color);
            }
        }
        return mostFrequentColor;
    }
}