package pl.edu.agh.kis.pz1.gameLogic;

import pl.edu.agh.kis.pz1.card.Card;
import pl.edu.agh.kis.pz1.card.Deck;
import pl.edu.agh.kis.pz1.exceptions.WyjatekStrategii;
import pl.edu.agh.kis.pz1.player.Player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DomyslnaStrategia implements Strategy {
    private List<Player> gracze;
    private Set<Integer> graczeBezKart;
    private Set<Integer> graczePrzegrani;
    private List<Card> stosKart = new ArrayList<>();
    private Sesja sesja;


    @Override
    public int maksymalnaLiczbaGraczy(){
        return 4;
    }

    @Override
    public void addSesja(Sesja sesja){
        this.sesja = sesja;
    }

    @Override
    public Deck wygenerujTalie(){
        return new Deck(1,1,1,1,false,false,false,false,false,false,false,true,true,true,true,true,true);
    }

    @Override
    public int czasNaRuchy(){
        return 0;
    }


    @Override
    public int rozpocznij(List<Player> gracze){
        this.gracze = gracze;
        int rozpoczynajacy = znajdzGraczaRozpoczynajacego();
        graczeBezKart = new HashSet<>();
        graczePrzegrani = new HashSet<>();
        return (rozpoczynajacy+1)%gracze.size();
    }

    @Override
    public int obslozRuchGracza(Player gracz,String msg) {
        try {
            String[] elementy = msg.split(" ");
            if(elementy[0].equalsIgnoreCase("wybierz")){
                msg = elementy[0];
            }
            List<Integer> argumenty = new ArrayList<>();

            try {
                przeniesArgumentyDoListy(elementy, argumenty);
            }
            catch (NumberFormatException e) {
                gracz.napiszDoGracza("Niepoprawnie argumenty wyboru kart!");
                return gracze.indexOf(gracz);
            }

            switch (msg.toLowerCase()) {
                case "stos":
                    pokazKarteNaStosie(gracz);
                    return gracze.indexOf(gracz);
                case "wez":
                    if(!wezKartyZeStosu(gracz)){
                        gracz.napiszDoGracza("Nie mozna wziac kart se stosu!");
                        return gracze.indexOf(gracz);
                    }
                    return nastepnyGracz(gracze.indexOf(gracz)+1);
                case "wybierz":
                    if(!polozKartyNaStosie(gracz,argumenty)){
                        gracz.napiszDoGracza("Nie mozna polozyc na stos wybranych kart!");
                        return gracze.indexOf(gracz);
                    }
                    if(pozostalychWGrze() <= 1){
                        return -1;
                    }
                    return nastepnyGracz(gracze.indexOf(gracz)+1);
                default:
                    gracz.napiszDoGracza("Nierozpoznane polecenie rozgrywki!");
                    return gracze.indexOf(gracz);
            }
        }
        catch (Exception e) {
            throw new WyjatekStrategii(this,"Blad w metodzie obslozRuchGracza");
        }
    }

    private int znajdzGraczaRozpoczynajacego(){
        for(int i=0;i<gracze.size();i++){
            for(Card c : gracze.get(i).getCards()){
                if(c.getRank() == Card.Rank.NINE && c.getSuit() == Card.Suit.HEARTS){
                    stosKart.add(gracze.get(i).removeCard(c));
                    sesja.napiszDoWszystkich(gracze.get(i),"Kładzie na stos NINE HEARTS");
                    try {
                        gracze.get(i).napiszDoGracza("Kładziesz na stos NINE HEARTS");
                    }
                    catch (Exception e) {
                        System.out.println(e);
                        System.exit(0);
                    }
                    return i;
                }
            }
        }
        return 0;
    }

    @Override
    public int obslozKoniecCzasu(Player gracz){
        gracz.setCzyPrzegral(true);
        try {
            gracz.napiszDoGracza("Uplynął czas na ruch.");
        }
        catch (Exception e) {
            System.out.println("Nie udało się wysłąć wiadomości do gracza (metoda obslozKoniecCzasu)");
        }
        sesja.napiszDoWszystkich(gracz,"Przegrał z powodu upływu czasu na ruch.");
        graczePrzegrani.add(gracze.indexOf(gracz));

        if(pozostalychWGrze() <= 1) {
            for(Player p : gracze){
                if(!graczePrzegrani.contains(gracze.indexOf(p)) && !graczeBezKart.contains(gracze.indexOf(p))){
                    p.setCards(new ArrayList<>());
                    break;
                }
            }
            return -1;
        }
        else{
            int nastepny = nastepnyGracz(gracze.indexOf(gracz)+1);
            return nastepny;
        }
    }

    private void pokazKarteNaStosie(Player gracz) throws IOException {
        if(stosKart.isEmpty()){
            gracz.napiszDoGracza("Stos kart jest pusty.");
        }
        else{
            gracz.napiszDoGracza("Ostatnia karta na stosie: ");
            gracz.napiszDoGracza(stosKart.get(stosKart.size()-1).toString());
        }
    }

    @Override
    public int nastepnyGracz(int i){
        if(i >= gracze.size()){
            i = 0;
        }

        while(graczeBezKart.contains(i) || graczePrzegrani.contains(i)){
            i += 1;
            if(i >= gracze.size()) i=0;
        }

        return i;
    }

    private boolean wezKartyZeStosu(Player gracz) throws IOException {
        if(stosKart.size() <= 1) return false;

        StringBuilder sb = new StringBuilder();
        sb.append("\nBierzesz ze stosu:\n");
        int doKtorej = stosKart.size()-3;

        for(int i=stosKart.size()-1; i>=doKtorej; i--){
            if(i <= 0) break;
            sb.append(stosKart.get(stosKart.size()-1).toString());
            sb.append("\n");
            gracz.addCard(stosKart.remove(i));

        }

        sesja.napiszDoWszystkich(gracz,"Bierze karty ze stosu!");
        gracz.napiszDoGracza(sb.toString());

        return true;
    }

    private int pozostalychWGrze(){
        return gracze.size() - graczeBezKart.size() - graczePrzegrani.size();
    }

    private boolean polozKartyNaStosie(Player gracz, List<Integer> argumenty){
        if(argumenty.size() > gracz.getCards().size()) return false;

        int pozycjaKarty;
        Card karta;

        switch(argumenty.size()){
            case 1:
                if(!sprawdzPoprawnoscArgumentow(gracz,argumenty)) return false;
                pozycjaKarty = argumenty.get(0) - 1;
                karta = gracz.getCards().get(pozycjaKarty);
                if(!stosKart.isEmpty()){
                    Card kartaZeStosu = stosKart.get(stosKart.size()-1);
                    if(karta.compareTo(kartaZeStosu) < 0) return false;
                }
                stosKart.add(gracz.getCards().remove(pozycjaKarty));
                sesja.napiszDoWszystkich(gracz,"Wyklada na stos karty: ");
                sesja.napiszDoWszystkich(null,karta.toString()+"\n");
                if(gracz.getCards().isEmpty()){
                    graczeBezKart.add(gracze.indexOf(gracz));
                    sesja.napiszDoWszystkich(gracz,"Pozbył się wszystkich kart!");
                }
                return true;
            case 3:
                if(!sprawdzPoprawnoscArgumentow(gracz,argumenty)) return false;
                if(!sprawdzCzyKartySaTeSame(gracz,argumenty)) return false;

                pozycjaKarty = argumenty.get(0) - 1;
                karta = gracz.getCards().get(pozycjaKarty);
                if(!stosKart.isEmpty()){
                    Card kartaZeStosu = stosKart.get(stosKart.size()-1);
                    if(karta.compareTo(kartaZeStosu) < 0) return false;
                }

                dodajDoStosu(gracz,argumenty);
                return true;
            case 4:
                if(!sprawdzPoprawnoscArgumentow(gracz,argumenty)) return false;
                if(!sprawdzCzyKartySaTeSame(gracz,argumenty)) return false;

                pozycjaKarty = argumenty.get(0) - 1;
                karta = gracz.getCards().get(pozycjaKarty);
                if(!stosKart.isEmpty()){
                    Card kartaZeStosu = stosKart.get(stosKart.size()-1);
                    if(karta.compareTo(kartaZeStosu) < 0) return false;
                }

                dodajDoStosu(gracz,argumenty);
                return true;
            default:
                return false;
        }
    }

    public void przeniesArgumentyDoListy(String[] elementy, List<Integer> argumenty) throws NumberFormatException {
        for(int i=1;i<elementy.length;i++){
            argumenty.add(Integer.parseInt(elementy[i]));
        }
    }

    private boolean sprawdzPoprawnoscArgumentow(Player gracz, List<Integer> argumenty){
        for(Integer i : argumenty){
            if(i < 1 || i > gracz.getCards().size()) return false;
        }
        return true;
    }

    private boolean sprawdzCzyKartySaTeSame(Player gracz, List<Integer> argumenty){
        List<Card> kartyGracza = gracz.getCards();
        Set<Integer> numeryKart = new HashSet<>();

        numeryKart.add(argumenty.get(0));
        Card karta = kartyGracza.get(argumenty.get(0)-1);

        for(int i=1;i<argumenty.size();i++){
            numeryKart.add(argumenty.get(i));
            if(kartyGracza.get(argumenty.get(i)-1).getRank() != karta.getRank()) return false;{}
        }

        return numeryKart.size() == argumenty.size();
    }

    private void dodajDoStosu(Player gracz, List<Integer> argumenty){
        sesja.napiszDoWszystkich(gracz,"Wyklada na stos karty: ");
        List<Card> pzoostaleKarty = new ArrayList<>();
        List<Card> kartyGracza = gracz.getCards();

        StringBuilder sb = new StringBuilder();

        for(int i=0;i<kartyGracza.size();i++){
            if(!argumenty.contains(i+1)) pzoostaleKarty.add(kartyGracza.get(i));
            else{
                stosKart.add(kartyGracza.get(i));
                sb.append(kartyGracza.get(i).toString());
                sb.append("\n");
            }
        }
        sesja.napiszDoWszystkich(null,sb.toString());

        gracz.setCards(pzoostaleKarty);
        if(gracz.getCards().isEmpty()){
            graczeBezKart.add(gracze.indexOf(gracz));
            sesja.napiszDoWszystkich(gracz,"Pozbył się wszystkich kart!");
        }
    }

    public int usunGracza(Player gracz){
        gracze.remove(gracz);

        graczeBezKart.remove(gracz);
        graczePrzegrani.remove(gracz);
        return pozostalychWGrze();
    }
}
