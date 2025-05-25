package pl.edu.agh.kis.pz1.card;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {
    private List<Card> deck;

    private boolean[] jakie_karty;

    public void sorted(){
        Collections.sort(deck,(a, b) -> {
            int cmp = a.getRank().ordinal() - b.getRank().ordinal();
            if(cmp == 0){
                cmp = a.getSuit().ordinal() - b.getSuit().ordinal();
            }
            return cmp;
        });
    }

    public void shuffle(){
        Collections.shuffle(deck);
    }

    public Deck(){
        deck = new ArrayList<>(52);
        for(Card.Rank rank : Card.Rank.values()){
            for(Card.Suit suit : Card.Suit.values()){
                deck.add(new Card(rank,suit));
            }
        }
    }

    //public Deck(boolean dwojki, boolean trojki, boolean czworki, boolean piatki, boolean szostki, boolean siodemki, boolean osemki, boolean dziewiatki, boolean dziesiatki, boolean jopki, boolean damy, boolean krole, boolean asy, int ile_pikow, int ile_kierow, int ile_trefli, int ile_karo ) {//TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING, ACE
    public Deck(int ile_pikow, int ile_kierow, int ile_trefli, int ile_karo, boolean... jakie){
        deck = new ArrayList<>();

        if(jakie.length != 13){
            throw new IllegalArgumentException("Zła ilość parametrów w konstruktorze talii");
        }

        wygenerujKarty(jakie,ile_pikow,3);
        wygenerujKarty(jakie,ile_kierow,0);
        wygenerujKarty(jakie,ile_trefli,2);
        wygenerujKarty(jakie,ile_karo,1);

    }

    private void wygenerujKarty(boolean[] jakie, int ile, int kolor){
        for(int i=0;i<ile;i++){
            for(int j=0;j<13;j++){
                if(jakie[j]){
                    deck.add(new Card(j,kolor));
                }
            }
        }
    }


    public Card drawCard(){
      if(deck.isEmpty()){
          throw new IllegalStateException("Deck is empty");
      }
      return deck.remove(0);
    }

    public void returnCard(Card card){
        deck.add(card);
    }

    public int getRemainingCardsSize(){
        return deck.size();
    }

    public List<Card> getDeck(){
        return deck;
    }
}
