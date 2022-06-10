package de.hsos.vs;

public class BillBoardJsonAdapter extends BillBoard implements BillBoardAdapterIf {
    public BillBoardJsonAdapter(String ctxt) {
        super(ctxt);
    }

    @Override
    public String readEntries(String caller_ip) {
        StringBuilder result = new StringBuilder();
        result.append('[');
        for (BillBoardEntry e : billboard) {
            result.append(readEntry(e.id, caller_ip));
            result.append(',');
        }
        result.append(']');
        return result.toString();
    }

    @Override
    public String readEntry(int idx, String caller_ip) {
        BillBoardEntry bbe = getEntry(idx);
        if (bbe == null) {
            System.err.println("BillBoardServer - readEntry: Objekt null; ggf. ist Idx falsch");
            return null;
        }

        bbe.toJson().put("owner", bbe.belongsToCaller(caller_ip));

        return bbe.toJson().toString();
    }

}
