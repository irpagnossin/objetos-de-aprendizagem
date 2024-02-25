package cepa.edu.math;

import java.util.Vector;
import org.w3c.dom.Element;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;

/**
 *
 * @author irpagnossin
 */
public abstract class AbstractMouseOverOutListener implements EventListener {

    protected Vector<Element> targets = new Vector<Element>();
    protected Vector<Boolean> locks = new Vector<Boolean>();

    public abstract void handleEvent(Event event);

    public void addElement (Element element) {
        locks.add(false);
        targets.add(element);
    }

    public void removeElement (Element element) {
        locks.remove(targets.indexOf(element));
        targets.remove(element);
    }

    public void lockElement (Element element) {
        locks.set(targets.indexOf(element), true);
    }

    public void unlockElement (Element element) {
        locks.set(targets.indexOf(element), false);
    }

    public boolean getLock (Element element) {
        return locks.get(targets.indexOf(element));
    }

    public void toggleLock (Element element) {
        if (getLock(element)) unlockElement(element);
        else lockElement(element);
    }
}
