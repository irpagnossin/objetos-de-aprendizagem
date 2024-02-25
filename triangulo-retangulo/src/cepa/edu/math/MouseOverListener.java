/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cepa.edu.math;

import org.w3c.dom.Element;
import org.w3c.dom.events.Event;

/**
 *
 * @author irpagnossin
 */
public class MouseOverListener extends AbstractMouseOverOutListener {

    @Override
    public void handleEvent(Event event) {
        for (Element e : targets) {
            
            if (locks.get(targets.indexOf(e))) return;

            e.setAttribute("opacity", "1");
        }
    }
}
