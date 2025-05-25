package pl.edu.agh.kis.pz1.gameLogic;

import pl.edu.agh.kis.pz1.card.Card;
import pl.edu.agh.kis.pz1.card.Deck;
import pl.edu.agh.kis.pz1.player.Player;

import java.util.List;

public interface Strategy {
    public int maksymalnaLiczbaGraczy();
    public Deck wygenerujTalie();
    public int czasNaRuchy();
    public int rozpocznij(List<Player> g); //Zwraca numer gracza ktory ma rozpaczac gre
    public int obslozRuchGracza(Player g,String msg); //Jesli status to -1, koniec rozgrywki. Inny status to numer gracza, ktory ma jako kolejny wykonac ruch. Jesli gracz dokonal niedozwolonego ruchu, metoda ma zwracac ponownie jego numer
    public int obslozKoniecCzasu(Player g); //Zwracana wartosc tak jak wyzej
    public void addSesja(Sesja sesja);
    public int usunGracza(Player gracz);
    public int nastepnyGracz(int i);

}
