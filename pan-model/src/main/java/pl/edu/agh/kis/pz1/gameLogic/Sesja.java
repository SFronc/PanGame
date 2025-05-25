package pl.edu.agh.kis.pz1.gameLogic;

import pl.edu.agh.kis.pz1.card.Card;
import pl.edu.agh.kis.pz1.card.Deck;
import pl.edu.agh.kis.pz1.exceptions.WyjatekStrategii;
import pl.edu.agh.kis.pz1.player.Player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Sesja {
    private final List<Player> gracze; //Pierwszy gracz z listy jest hostem
    private boolean czyRozpoczeta = false;
    private final int maxGraczy;
    private final Strategy strategia;
    private int czasDlaGraczy = 0;
    private int status = 0;
    private int poprzedniStatus = 0;
    private ScheduledExecutorService executorService;
    private ScheduledFuture<?> currentTask;
    private final String kodSesji;

    public Sesja(Player host, int maxGraczy, Strategy strategia, String kodSesji) {
        this.maxGraczy = maxGraczy;
        gracze = new ArrayList<Player>();
        gracze.add(host);
        this.strategia = strategia;
        strategia.addSesja(this);
        this.kodSesji = kodSesji;
    }

    public boolean czyRozpoczeta() {
        return czyRozpoczeta;
    }

    public boolean czyJestMiejsce() {
        return gracze.size() < maxGraczy;
    }

    public void dodajGracza(Player gracz) {
        gracze.add(gracz);
        napiszDoWszystkich(null,"Gracz "+gracz.getNickName()+" dołączył do gry!");
    }

    public List<Player> listaGraczy(){
        return gracze;
    }

    public void usunGracza(Player host, Player gracz) {
        gracze.remove(gracz);
        napiszDoWszystkich(host, "Gracz "+gracz.getNickName()+" został wyrzucony przez "+host.getNickName()+".");
    }

    public void napiszDoWszystkich(Player gracz, String msg) {
        try {
            if(gracz != null) {
                msg = "["+gracz.getNickName()+"]: "+msg;
            }
            for (Player p : gracze) {
                if (!p.equals(gracz)) {
                    p.napiszDoGracza(msg);
                }
            }
        }
        catch (IOException e) {
            System.out.println("Blad przy wypisywaniu woadomosci do graczy na sesji!");
        }
    }

    public void start(Player gracz) throws IOException {
        executorService = Executors.newScheduledThreadPool(1);
        if(!czyRozpoczeta() && gracze.size() >= 2 && gracz.equals(gracze.get(0))) {
            napiszDoWszystkich(null, "Gra sie rozpoczela!");
            czyRozpoczeta = true;
            try {
                rozpocznijRozgrywke();
            }
            catch (WyjatekStrategii e) {
                System.out.println(e.getMessage());
                napiszDoWszystkich(null,"Wystąpił błąd załadowanej strategii. Wybierz inną lub spróbuj ponownie.");
                czyRozpoczeta = false;
            }
        }
        else{
            if(gracze.size() < 2){
                gracz.napiszDoGracza("Nie można wystartować sesji! Zbyt mało graczy.");
            }
            else{
                gracz.napiszDoGracza("Nie masz uprawnień do rozpoczęcia rozgrywki.");
            }
        }
    }

    private void rozpocznijRozgrywke() throws WyjatekStrategii {
        if(gracze.size() > strategia.maksymalnaLiczbaGraczy()){
            throw new WyjatekStrategii(strategia,"Przekroczono maksymalną ilość graczy przewidzianą na tą strategię! (W rozgrywce: "+gracze.size()+", maksymalnie w strategii: "+strategia.maksymalnaLiczbaGraczy()+")");
        }

        Deck talia = strategia.wygenerujTalie();

        if(talia == null || talia.getDeck().isEmpty()) {
            throw new WyjatekStrategii(strategia,"Strategia wygenerowala niepoprawna talie kart!");
        }
        talia.shuffle();

        int czasNaRuchy = strategia.czasNaRuchy();
        this.czasDlaGraczy = czasNaRuchy;
        if(czasNaRuchy < 0){
            throw new WyjatekStrategii(strategia,"Strategia wygenerowałą niepoprawny czas na ruchy graczy.");
        }

        int liczbaKartDlaGracza = talia.getRemainingCardsSize()/gracze.size();

        if(talia.getDeck().size() < (liczbaKartDlaGracza*gracze.size())){
            throw new WyjatekStrategii(strategia,"Niewystarczająca ilość kart dla graczy w rozgrywce!");
        }

        ustawCzasDlaGraczy(czasNaRuchy);

        for(Player p : gracze) {
            p.setCards(new ArrayList<>());
            p.setCzyPrzegral(false);
            for(int i=0;i<liczbaKartDlaGracza;i++){
                p.addCard(talia.drawCard());
            }
        }

        status = strategia.rozpocznij(gracze);
        poprzedniStatus = status;
        if(status < 0 || status >= gracze.size()) throw new WyjatekStrategii(strategia,"Strategia wyznaczyła niepoprawny numer gracza rozpoczynająćego!");

        try {
            gracze.get(status).napiszDoGracza("\nTeraz Twoja kolej! ");
            if (czasDlaGraczy > 0) {
                gracze.get(status).napiszDoGracza("Czas na ruch to " + czasDlaGraczy + " sekund!\n");
                rozpocznijOdliczanieDlaGracza(gracze.get(status));
            }
        }
        catch (IOException e){
            czyRozpoczeta=false;
            napiszDoWszystkich(null,"Blad rozgrywki!");
            System.out.println("Blad podczas rozpoczynania rozgrywki!");
        }
    }

    public void wyswietlGraczy(Player gracz) throws IOException {
        gracz.napiszDoGracza("Gracze w lobby ("+kodSesji+")");
        for (Player p : gracze) {
            StringBuilder sb = new StringBuilder();
            sb.append(p.getNickName());
            if(p.equals(gracze.get(0))) {
                sb.append(" (host)");
            }
            if (p.equals(gracz)) {
                sb.append(" (Ty)");
            }
            sb.append("\n");
            gracz.napiszDoGracza(sb.toString());
        }
    }

    private void ustawCzasDlaGraczy(int czasNaRuchy){
        for (Player p : gracze) {
            p.ustawCzasNaRUchy(czasNaRuchy);
        }
    }

    public void obslozPolecenieRozgrywki(Player gracz, String msg) throws IOException {
        if(msg==null && czasDlaGraczy > 0){
            gracz.napiszDoGracza("\nTeraz Twoja kolej! ");
            gracz.napiszDoGracza("Czas na ruch to "+czasDlaGraczy+" sekund!\n");
            rozpocznijOdliczanieDlaGracza(gracz);
            poprzedniStatus = status;
            return;
        }
        if(gracz.czyPrzegral()){
            gracz.napiszDoGracza("Już przegrałeś, nie możesz dalej grać!");
            return;
        }
        if(gracz.pozostalaIloscKart() == 0){
            gracz.napiszDoGracza("Nie masz już kart!");
            return;
        }
        if(msg.equalsIgnoreCase("karty")){
            pokazKartyGracza(gracz);
            return;
        }

        if(!gracz.equals(gracze.get(status))){
            gracz.napiszDoGracza("Nie Twoja kolej! Poczekaj.");
            return;
        }

        status = strategia.obslozRuchGracza(gracz,msg);
        if(status == poprzedniStatus){
            return;
        }
        poprzedniStatus = status;

        if(czasDlaGraczy > 0){
            if(!currentTask.isDone()) currentTask.cancel(false);
        }

        if(!(status >= -1 && status < gracze.size())){
            czyRozpoczeta = false;
            throw new WyjatekStrategii(strategia, "Strategia zwróciła niepoprawny status gry!");
        }
        else if(status == -1){
            zakonczGre();
        }
        else{
            gracze.get(status).napiszDoGracza("\nTeraz Twoja kolej! ");
            if(czasDlaGraczy > 0){
                /*if (.isShutdown() || scheduler.isTerminated()) {
                    scheduler = Executors.newScheduledThreadPool(1);
                }*/
                gracze.get(status).napiszDoGracza("Czas na ruch to "+czasDlaGraczy+" sekund!");
                rozpocznijOdliczanieDlaGracza(gracze.get(status));
            }
        }
    }

    public void zakonczGre(){
        executorService.shutdown();
        czyRozpoczeta = false;
        napiszDoWszystkich(null,"Koniec gry! Podsumowanie:\n");
        for(Player p : gracze){
            if(p.czyPrzegral()){
                napiszDoWszystkich(null,"Gracz "+p.getNickName()+" przegrał z powodu upłynięcai czasu na ruch.\n");
            }
            else if(p.pozostalaIloscKart() > 0){
                napiszDoWszystkich(null,"Gracz "+p.getNickName()+" przegrał z powodu pozostania z kartami.\n");
            }
            else{
                napiszDoWszystkich(null,"Gracz "+p.getNickName()+" pozbył się wszystkich kart!\n");
            }
        }
    }

    private void rozpocznijOdliczanieDlaGracza(Player gracz) {
        currentTask = executorService.schedule(() -> {
            status = strategia.obslozKoniecCzasu(gracz);
            if(status == -1){
                zakonczGre();
            }
            else{
                try {
                    obslozPolecenieRozgrywki(gracze.get(status),null);
                }
                catch (IOException e){
                    System.out.println(e.getMessage());
                }
            }
        }, czasDlaGraczy, TimeUnit.SECONDS);

    }


    private void pokazKartyGracza(Player gracz) throws IOException{
        gracz.napiszDoGracza("\nTwoje karty: ");
        StringBuilder sb = new StringBuilder();
        int i = 1;
        for(Card k : gracz.getCards()){
            sb.append(i).append(". ").append(k.toString()).append("\n");
            //gracz.napiszDoGracza(i + ". " + k.toString());
            i++;
        }
        gracz.napiszDoGracza(sb.toString());
    }

    public int obslozWyjscieGracza(Player gracz) throws IOException{ //zwraca ile graczy zostalo
        if(czyRozpoczeta){
            if(status == gracze.indexOf(gracz)){
                if(czasDlaGraczy > 0 && !currentTask.isDone()){
                    currentTask.cancel(false);
                }
                int pozostali = strategia.usunGracza(gracz);
                if(pozostali<=1) zakonczGre();
                else{
                    int nastepny = strategia.nastepnyGracz(status);
                    if(czasDlaGraczy > 0) obslozPolecenieRozgrywki(gracze.get(nastepny),null);
                    else{
                        status = nastepny;
                        poprzedniStatus = status;
                        gracze.get(status).napiszDoGracza("\nTwoja kolej!\n");
                    }
                }
            }
            else{
                int pozostali = strategia.usunGracza(gracz);
                if(pozostali<=1) zakonczGre();
            }
        }
        else{
            if(gracze.indexOf(gracz) == 0 && gracze.size() >= 2){
                gracze.get(1).napiszDoGracza("Stajesz się nowym hostem sesji.");
                napiszDoWszystkich(gracze.get(1),"Staje się nowym hostem sesji.");
            }
            gracze.remove(gracz);
        }

        napiszDoWszystkich(gracz,"Opuszcza grę.");
        gracz.napiszDoGracza("Opuszczasz rozgrywkę.");



        return gracze.size();
    }

    public String getKodSesji(){
        return kodSesji;
    }

}
