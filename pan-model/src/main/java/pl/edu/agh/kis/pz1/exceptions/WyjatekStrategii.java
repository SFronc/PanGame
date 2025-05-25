package pl.edu.agh.kis.pz1.exceptions;

import pl.edu.agh.kis.pz1.gameLogic.Strategy;

public class WyjatekStrategii extends RuntimeException {
    public WyjatekStrategii(Strategy strategia, String message) {
        super(strategia.getClass().getName() + ": " + message);
    }
}
