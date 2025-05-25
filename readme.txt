Program do gry w grę karcianą "Pan". 
Link do opisu: https://pl.wikipedia.org/wiki/Pan_(gra_karciana)
Po uruchomieniu serwera i połączeniu się z nim za pomocą klienta (należy wpisać port serwera przy uruchamianiu klienta, domyślnie 8080) gracz musi wybrać swoją nazwę. Następnie gracz ma do dyspozycji 3 polecenia:
-create iloscOsobWSesji 
-create iloscOsobWSesji sciezkaDoKlasyImplementującejInterfejsStrategii - dla przykładu w folderze głównym znajduje się plik "StrategiaDziesiecSekund.class", który w odróżnieniu od domyślnej strategii implementuje czas, w którym gracz musi wykonać ruch, inaczej zostanie uznany za przegranego
-join numerSesji - numerSesji wygenrrowany przy jej tworzeniu

Po utworzeniu sesji gracz tworzący staje się jej hostem, tzn. może uruchomić rozgrywkę lub wyrzucać obecnych graczy z lobby. Po opuszczeniu hosta jego rolę przejmuje kolejny gracz, który dołączył po nim.
Będąc w lobby gracze mają do dyspozycji komendy:
-show -pokazuje obecnych graczy w sesji
-exit(opuszczenie sesji)
-msg trescWiadomosci - wszyscy obecni na sesji widzą wiadomości od gracza
-kick nazwaGracza - dostępne dla hosta, wyrzucenie gracza
-start - uruchomienie rozgrywki

Po rozpoczęciu się rozgrywki gracz, który ma kartę "9 kier" dokłada ją na stos.
Gracz, którego kolej następuje dostaje stosowne powiadomienie. Za po mocą komend gracz ma opcję wzięcia 3 kart ze stosu (lub mniej, jeśli nie ma więcej, z wyłączeniem pierwszej karty) lub dodania na stos karty o "wyższej" lub "równej" co do wartości karty znajdującej się na stosie. Gracz może rzucić jedną kartę, 3 jednocześnie (o tej samej wartości) lub 4 (o tej samej wartości). Celem gracza jest pozbycie się wszystkich kart.
Podczas rozgrywki dostępne są polecenia:
-karty - pokazuje dostępne karty gracza
-stos - pokazuje ostatnią kartę na stosie
-wez - bierze 3 (lub mniej, jeśli nie ma więcej) karty ze stosu
-exit - wyjście z rozgrywki. Wówczas gra jest kontynuowana, o ile co najmniej 2 gracze wciąż mają karty
-wybierz numerKarty - dodaje wybraną kartę na stos
-wybierz numerPeirwszejKarty numerDrugiejKarty numerTrzeciejKarty - tak jak wyżej z tą różnicą, że można jednocześnie wyłożyć 3 karty o tej samej wartości (kolory nie muszą się zgadzać)
-wybierz  numerPeirwszejKarty numerDrugiejKarty numerTrzeciejKarty numerCzwartejKarty - tak jak wyżej, tylko 4 karty.