package pl.edu.agh.kis.pz1.player;

import pl.edu.agh.kis.pz1.card.Card;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.*;

public class Player {
    private List<Card> cards;
    private int chips; //zetony
    private final SocketChannel channel;
    private String nickName;
    //private int idSesji = -1;
    private boolean czyWSesji = false;
    private String sciezka;
    private int pozostalyCzas;
    private boolean czyPrzegral = false;

    public Player(SocketChannel channel) {
        this.channel = channel;
        this.cards = new ArrayList<Card>();
        this.chips = 0;
        this.nickName = "";
        this.sciezka = "";
    }

    public Player(int id, SocketChannel channel, String nickName) {
        this.channel = channel;
        this.cards = new ArrayList<Card>();
        this.chips = 0;
        this.nickName = nickName;
        this.sciezka = "";
    }

    public void setCzyPrzegral(boolean czyPrzegral) {
        this.czyPrzegral = czyPrzegral;
    }

    public boolean czyPrzegral() {
        return czyPrzegral;
    }


    public List<Card> getCards() {
        return cards;
    }

    public int getChips() {
        return chips;
    }

    public SocketChannel getChannel() {
        return channel;
    }

    public boolean getCzyWSesji(){
        return czyWSesji;
    }

    public void setCzyWSesji(boolean czyWSesji){
        this.czyWSesji = czyWSesji;
    }

    public void ustawSciezke(String sciezka){
        this.sciezka = sciezka;
    }

    public String pokazSciezke(){
        return sciezka;
    }

    public void addChips(int chips) {
        this.chips += chips;
    }

    public void subChips(int chips) {
        if(this.chips < chips){
            throw new IllegalArgumentException("No enough chips!");
        }
        this.chips -= chips;
    }

    public void addCard(Card card) {
        this.cards.add(card);
    }

    public void setCards(List<Card> cards) {
        this.cards = cards;
    }

    public Card removeCard(Card card) {
        if(this.cards.contains(card)){
            cards.remove(card);
            return card;
        }
        else{
            return null;
        }
    }

    public String getNickName(){
        return nickName;
    }

    public void setNickName(String nickName){
        this.nickName = nickName;
    }

    public void napiszDoGracza(String msg) throws IOException {
        if(channel != null){
            channel.write(ByteBuffer.wrap(msg.getBytes()));
        }
    }

    public void ustawCzasNaRUchy(int czas){
        this.pozostalyCzas = czas;
    }

    public int getPozostalyCzas(){
        return pozostalyCzas;
    }

    public int pozostalaIloscKart(){
        return cards.size();
    }


    @Override
    public String toString() {
        return "Player "+"."+"; Chips: "+this.chips+", Cards: "+this.cards;
    }


}
