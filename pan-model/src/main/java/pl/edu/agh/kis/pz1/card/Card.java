package pl.edu.agh.kis.pz1.card;

import java.util.Objects;

/**
 * Class representing card in a deck of cards
 * Every card has its own value (rank) and color (suit)
 * This class provides basic operations for a card
 *
 * @author Seweryn Fronc
 * @version 1.0
 */
public class Card implements Comparable<Card> {
    private final Rank rank;
    private final Suit suit;

    /**
     * Constructor creating new instance of card
     * @param rank Card value, for example "Ace", "King"
     * @param suit Card color, for example "Hearts", "Spades"
     */
    public Card(Rank rank, Suit suit){
        this.rank = rank;
        this.suit = suit;
    }

    public Card(int rank, int suit){
        this.rank = Rank.values()[rank];
        this.suit = Suit.values()[suit];
    }

    public enum Rank{
        TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING, ACE
    }
    public enum Suit{
        HEARTS, DIAMONDS, CLUBS, SPADES
    }

    /**
     * Returns card value
     * @return value, for example "Ace" or "2"
     */
    public Rank getRank(){
        return rank;
    }

    public Suit getSuit(){
        return suit;
    }

    @Override
    public String toString(){
        return rank + " " + suit;
    }

    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        Card card = (Card) o;
        return this.rank.equals(card.rank) && this.suit.equals(card.suit);
    }

    @Override
    public int hashCode(){
        return Objects.hash(rank, suit);
    }

    @Override
    public int compareTo(Card o) {
        return rank.compareTo(o.rank);
    }
}
