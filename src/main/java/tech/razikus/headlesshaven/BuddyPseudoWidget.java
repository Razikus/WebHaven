package tech.razikus.headlesshaven;

import java.util.HashMap;

public class BuddyPseudoWidget extends PseudoWidget{
    public HashMap<Integer, BuddyState> buddyStateHashMap = new HashMap<>();

    private String myPlayerName;

    public BuddyPseudoWidget(PseudoWidget original) {
        super(original);
    }


    public void addNewBuddy(Integer id, BuddyState state) {
        synchronized (buddyStateHashMap) {
            buddyStateHashMap.put(id, state);
        }
    }

    public BuddyState getBuddy(Integer id) {
        synchronized (buddyStateHashMap) {
            return buddyStateHashMap.get(id);
        }
    }

    public String getBuddyName(Integer id) {
        synchronized (buddyStateHashMap) {
            BuddyState state =  buddyStateHashMap.get(id);
            if(state != null) {
                return state.getName();
            } else {
                return "???";
            }
        }

    }

    public String getMyPlayerName() {
        return myPlayerName;
    }

    private BuddyState parseArgsToBuddyState(Object[] args) {
        Integer id = (Integer) args[0];
        String name = (String) args[1];
        Integer R = (Integer) args[2]; // @TODO ITS NOT RGB XD
        Integer G = (Integer) args[3];
        Integer B = (Integer) args[4];
        return new BuddyState(id, name, R, G, B);
    }

    @Override
    public void ReceiveMessage(WidgetMessage message) {
        switch (message.getName()) {
            case "add":
                BuddyState buddyState = parseArgsToBuddyState(message.getArgs());
                addNewBuddy(buddyState.getId(), buddyState);
                break;
            case "pname":
                String firstArg = (String) message.getArgs()[0];
                this.myPlayerName = firstArg;
                break;
            default:
                System.out.println("UNHANDLED BUDDY MESSAGE:" + message);
        }
    }
}
