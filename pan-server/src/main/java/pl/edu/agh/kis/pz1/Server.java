package pl.edu.agh.kis.pz1;

import pl.edu.agh.kis.pz1.card.Card;
import pl.edu.agh.kis.pz1.exceptions.WyjatekStrategii;
import pl.edu.agh.kis.pz1.gameLogic.DomyslnaStrategia;
import pl.edu.agh.kis.pz1.gameLogic.Sesja;
import pl.edu.agh.kis.pz1.gameLogic.Strategy;
import pl.edu.agh.kis.pz1.player.Player;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Server {
    private static final int PORT = 8080;
    private Selector selector;
    private ServerSocketChannel serverSocket;
    private final Map<SocketChannel, Player> gracze = new LinkedHashMap<>();
    private final Map<Player, Sesja> sesjeGraczy = new LinkedHashMap<>();
    private final Map<String, Sesja> kodySesji = new LinkedHashMap<>();
    private final Set<String> zajeteNazwyGraczy = new HashSet<>();


    public static void main(String[] args) throws IOException {
        new Server().start();
    }

    public void start() throws IOException {
        serverSocket = ServerSocketChannel.open();
        serverSocket.bind(new InetSocketAddress(PORT));
        serverSocket.configureBlocking(false);
        selector = Selector.open();
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("Server uruchomiony (PORT: " + PORT + ")");

        while(true) {
            selector.select();
            Iterator<SelectionKey> it = selector.selectedKeys().iterator();
            while(it.hasNext()) {
                SelectionKey key = it.next();
                it.remove();

                if(key.isAcceptable()) {
                    obslozNowePolaczenie();
                }
                else if(key.isReadable()) {
                    obslozKlienta(key);
                }
            }
        }
    }

    private void obslozNowePolaczenie() throws IOException {
        SocketChannel klient = serverSocket.accept();
        klient.configureBlocking(false);
        klient.register(selector, SelectionKey.OP_READ);
        System.out.println("Gracz "+klient.getRemoteAddress()+" polaczony.");
        Player gracz = new Player(klient);
        gracz.ustawSciezke("["+PORT+"]: ");
        gracze.put(klient, gracz);
        gracz.napiszDoGracza("Podaj swoja nazwe w grze (nazwa musi skladac sie z liter, a po nich opcjonalnie cyfry, max 20 znakow): ");
    }

    private void obslozKlienta(SelectionKey key){
        SocketChannel klient = (SocketChannel) key.channel();
        Player gracz = gracze.get(klient);
        System.out.println("Obsluga klienta...");
        try {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            int odczyt = klient.read(buffer);

            if(odczyt == -1) {
                klient.close();
                zajeteNazwyGraczy.remove(gracze.get(klient).getNickName());
                gracze.remove(klient);
                System.out.println("Gracz "+klient.getRemoteAddress()+" rozlaczyl sie. ");
            }

            else if(odczyt >= 0) {
                buffer.flip();
                String msg = new String(buffer.array(), 0, buffer.limit()).trim();
                System.out.println(klient.getRemoteAddress()+": "+msg);
                wykonajPolecenie(klient, msg);
            }

        }
        catch(Exception e) {
            System.out.println("Blad przy obsludze polaczenia: "+e.getMessage());
            try{
                //zajeteNazwyGraczy.remove(gracze.get(klient).getNickName());
                zajeteNazwyGraczy.remove(gracz.getNickName());
                gracze.remove(klient);
                klient.close();
            }
            catch(Exception e2) {
                System.out.println("Blad przy zamykaniu polaczenia: "+e2.getMessage());
            }
        }

    }

    private void wykonajPolecenie(SocketChannel klient, String msg) throws IOException {
        Player gracz = gracze.get(klient);
        if(gracz == null) {
            System.out.println("Nie znaleziono gracza powiazanego z tym kanalem!");
            klient.close();
            return;
        }

        if(gracz.getNickName().isEmpty()){
            if(!msg.isEmpty() && msg.length() <= 20 && msg.matches("^[a-zA-Z]+[0-9]*$")){
                if(zajeteNazwyGraczy.contains(msg)){
                    gracz.napiszDoGracza("Wpisana nazwa jest juz uzywana.");
                }
                else {
                    zajeteNazwyGraczy.add(msg);
                    gracz.setNickName(msg);
                    gracz.ustawSciezke("["+PORT+"]["+msg+"]: ");
                    gracz.napiszDoGracza("Pomyślnie ustawiono nick na "+msg);
                }
            }
            else{
                gracz.napiszDoGracza("Niepoprawna nazwa uzytkownika. Sproboj ponownie (musi skaldac sie z conajmniej jednej litery i dow. ilosci cyfer z tym ze po cyfrze nie moze wystapic ponownie litera)");
            }
        }
        else if(!gracz.getCzyWSesji()){
            //create maxGraczy strategia(opcjonalnie)
            //join kodSesji
            String[] elementy = msg.split(" ");

            if(elementy.length == 3 && elementy[0].equalsIgnoreCase("create")){
                try{
                    int maxGraczy = Integer.parseInt(elementy[1]);

                    try {
                        Strategy strategia = loader(elementy[2]);
                        System.out.println(strategia.czasNaRuchy());
                        stworzNowaSesje(gracz,maxGraczy,strategia);
                    }
                    catch (Exception e){
                        gracz.napiszDoGracza("\nNie udało się wczytać wybranej strategii.\n");

                    }

                }
                catch (NumberFormatException e){
                    gracz.napiszDoGracza("Niepoprawny argument (max liczba graczy).");
                }

            }
            else if(elementy.length == 2 && elementy[0].equalsIgnoreCase("create")){
                try{
                    int maxGraczy = Integer.parseInt(elementy[1]);
                    Strategy strategia = new DomyslnaStrategia();
                    stworzNowaSesje(gracz, maxGraczy, strategia);
                }
                catch (NumberFormatException e){
                    gracz.napiszDoGracza("Niepoprawny argument (max liczba graczy).");
                }

            }
            else if(elementy.length == 2 && elementy[0].equalsIgnoreCase("join")){
                if(kodySesji.containsKey(elementy[1])){
                    Sesja sesja = kodySesji.get(elementy[1]);
                    if(!sesja.czyRozpoczeta() && sesja.czyJestMiejsce()){
                        sesja.dodajGracza(gracz);
                        //gracz.ustawSciezke("["+PORT+"]["+elementy[1]+"]["+gracz.getNickName()+"]: ");
                        gracz.setCzyWSesji(true);
                        sesjeGraczy.put(gracz,sesja);
                        gracz.napiszDoGracza("Dołączyłeś do lobby ("+elementy[1]+")");
                    }
                    else {
                        gracz.napiszDoGracza("Nie mozna dolaczyc do sesji. Rozgrywka juz sie rozpoczela lub nie ma miejsca.");
                    }
                }
                else{
                    gracz.napiszDoGracza("Niepoprawny kod dostepu ("+elementy[1]+").");
                }
            }
            else{
                gracz.napiszDoGracza("Nie znalezniono polecenia lub nieprawidlowa liczba argumentow polecenia.");
            }
        }
        else if(!sesjeGraczy.get(gracz).czyRozpoczeta()){
            //kick gracz
            //start
            //msg wiadomosc
            //show - pokazuje graczy obecnych w sesji
            //exit opuszcza sesje
            if(msg.equalsIgnoreCase("exit")){
                opuscSesje(gracz);
                return;
            }
            wykonajPolecenieLobby(gracz, msg);
        }
        else{
            if(msg.equalsIgnoreCase("exit")){
                opuscSesje(gracz);
                return;
            }
            rozgrywka(gracz,msg);
        }

    }

    private String wygenerujKodSesjiNaPodstawieDatyUtworzenia(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String timestamp = LocalDateTime.now().format(formatter);

        // Generowanie losowego identyfikatora UUID
        String randomPart = UUID.randomUUID().toString().substring(0, 4);

        return timestamp+randomPart;
    }

    private void stworzNowaSesje(Player gracz, int maxGraczy, Strategy strategia) throws IOException {
        if(maxGraczy < 2 || maxGraczy > strategia.maksymalnaLiczbaGraczy()){
            gracz.napiszDoGracza("Podano nieprawidłową liczbę osób. Minimalna to 2, maksymalna (dla wybranej strategii) to "+strategia.maksymalnaLiczbaGraczy()+".");
            return;
        }
        String kodSesji = wygenerujKodSesjiNaPodstawieDatyUtworzenia();
        Sesja sesja = new Sesja(gracz, maxGraczy, strategia, kodSesji);

        sesjeGraczy.put(gracz,sesja);
        kodySesji.put(kodSesji, sesja);
        gracz.setCzyWSesji(true);
        gracz.napiszDoGracza("Utworzono nowa sesje. Kod dostepu: "+kodSesji);
        gracz.ustawSciezke("["+PORT+"]["+kodSesji+"][ "+gracz.getNickName()+"]: ");
        System.out.println("Utworzono nowa sesje o kodzie dostepu: "+kodSesji);
    }

    private Player znajdzGracza(List<Player> gracze, String nick){
        for(Player p : gracze){
            if(p.getNickName().equals(nick)){
                return p;
            }
        }

        return null;
    }

    private void wykonajPolecenieLobby(Player gracz, String msg) throws IOException {
        String[] elementy = msg.split(" ");
        Sesja sesja = sesjeGraczy.get(gracz);

        switch (elementy[0].toLowerCase()){
            case "kick":
                if(elementy.length != 2){
                    gracz.napiszDoGracza("Niepoprawny argument polecenia kick!");
                    return;
                }

                List<Player> gracze = sesja.listaGraczy();
                if(!gracze.get(0).equals(gracz)){
                    gracz.napiszDoGracza("Brak uprawnien (nie jestes hostem)!");
                    return;
                }

                Player graczDoWyrzucenia = znajdzGracza(gracze, elementy[1]);
                if(graczDoWyrzucenia != null && !graczDoWyrzucenia.equals(gracz)){
                    graczDoWyrzucenia.napiszDoGracza("Zostales wyrzucony przez hosta!");
                    //graczDoWyrzucenia.ustawSciezke("["+PORT+"]["+gracz.getNickName()+"]: ");
                    sesjeGraczy.remove(graczDoWyrzucenia);
                    sesja.usunGracza(gracz,graczDoWyrzucenia);
                    graczDoWyrzucenia.setCzyWSesji(false);
                    gracz.napiszDoGracza("Pomyślnie wyrzucono gracza "+graczDoWyrzucenia.getNickName()+".");
                }
                else{
                    gracz.napiszDoGracza("Nie znaleziono podanego gracza!");
                    return;
                }
                break;
            case "start":
                sesja.start(gracz);
                break;
            case "msg":
                if(elementy.length >= 2){
                    StringBuilder ms = new StringBuilder();
                    for(int i = 1; i < elementy.length; i++){
                        ms.append(elementy[i]);
                        if(i < elementy.length - 1){
                            ms.append(" ");
                        }
                    }
                    sesja.napiszDoWszystkich(gracz,ms.toString());
                }
                break;
            case "show":
                if(elementy.length == 1){
                    pokazGraczy(gracz);
                }
                break;
            default:
                gracz.napiszDoGracza("Nierozpoznane polecenie lobby!");
                break;
        }
    }

    private void rozgrywka(Player gracz, String msg) throws IOException {
        Sesja sesja = sesjeGraczy.get(gracz);
        if(sesja==null){
            gracz.napiszDoGracza("Błąd przy odczytywaniu Twojej sesji!");
            gracz.setCzyWSesji(false);
            sesjeGraczy.remove(gracz);
            return;
        }

        try {
            sesja.obslozPolecenieRozgrywki(gracz, msg);
        }
        catch (WyjatekStrategii w){
            sesja.napiszDoWszystkich(null,"Błąd stanu strategii!");
            gracz.napiszDoGracza(w.getMessage());
        }

    }

    private void pokazGraczy(Player gracz) throws IOException {
        Sesja sesja = sesjeGraczy.get(gracz);
        if(sesja == null){
            gracz.napiszDoGracza("Błąd! Nie odnaleziono Twojej sesji!");
            return;
        }

        sesja.wyswietlGraczy(gracz);
    }

    private void opuscSesje(Player gracz) throws IOException {
        if(gracz.getCzyWSesji()){
            Sesja sesjaGracza = sesjeGraczy.get(gracz);
            int zostaloGraczy = sesjaGracza.obslozWyjscieGracza(gracz);
            if(zostaloGraczy == 0){
                kodySesji.remove(sesjaGracza.getKodSesji());
            }
            sesjeGraczy.remove(gracz);
            gracz.setCzyWSesji(false);
        }
    }


    public Strategy loader(String sciezka) throws Exception {
        File plik = new File(sciezka);
        if(!plik.exists()){
            throw new IllegalArgumentException("Plik strategii nie istnieje!");
        }

        String nazwaKlasy = plik.getName().replace(".class","");

        URL url = plik.getParentFile().toURI().toURL();
        URLClassLoader cloader = new URLClassLoader(new URL[]{url});

        Class<?> clazz = cloader.loadClass(nazwaKlasy);

        if(Strategy.class.isAssignableFrom(clazz)){
            return (Strategy) clazz.getDeclaredConstructor().newInstance();
        }
        else throw new ClassNotFoundException("Kalsa nie implementuje interfejsu Strategii.");
    }



}

