package de.hsos.vs;

import org.json.JSONArray;
import org.json.JSONObject;

public class BillBoardJsonAdapter extends BillBoard implements BillBoardAdapterIf {
  public BillBoardJsonAdapter(String ctxt) {
    super(ctxt);
  }

  @Override
  public String readEntries(String caller_ip) {
    JSONArray jsonArray = new JSONArray();
    for (BillBoardEntry e : billboard) {
      jsonArray.put(readJsonEntry(e.id, caller_ip));
    }

    return jsonArray.toString(4).replace("\\", "");
  }

  @Override
  public String readEntry(int idx, String caller_ip) {
    BillBoardEntry bbe = getEntry(idx);
    if (bbe == null) {
      System.err.println("BillBoardServer - readEntry: Objekt null; ggf. ist Idx falsch");
      return null;
    }

    return bbe.toJson().put("owner", bbe.belongsToCaller(caller_ip)).toString();
  }

  private JSONObject readJsonEntry(int idx, String caller_ip) {
    BillBoardEntry bbe = getEntry(idx);
    if (bbe == null) {
      System.err.println("BillBoardServer - readEntry: Objekt null; ggf. ist Idx falsch");
      return null;
    }

    return bbe.toJson().put("owner", bbe.belongsToCaller(caller_ip));
  }
}
