package cepa.edu.util;

import java.util.Observable;

public class LanguageObservable extends Observable {
    @Override
    public void notifyObservers(){
        setChanged();
        super.notifyObservers();
    }
}
