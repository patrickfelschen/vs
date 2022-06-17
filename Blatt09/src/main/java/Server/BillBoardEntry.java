package Server;

import org.json.JSONObject;

import java.util.Date;
import java.util.Objects;

public class BillBoardEntry {
    private int id;
    private String name;
    private String owner_ip;
    private long timestamp;

    public BillBoardEntry(int id, String name, String owner_ip) {
        this.id = id;
        this.name = name;
        this.owner_ip = owner_ip;
        this.timestamp = new Date().getTime();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwner_ip() {
        return owner_ip;
    }

    public void setOwner_ip(String owner_ip) {
        this.owner_ip = owner_ip;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BillBoardEntry that = (BillBoardEntry) o;
        return id == that.id && timestamp == that.timestamp && name.equals(that.name) && owner_ip.equals(that.owner_ip);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, owner_ip, timestamp);
    }

    public String toJsonString() {
        return toJsonObject().toString(4);
    }

    public JSONObject toJsonObject() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", id);
        jsonObject.put("name", name);
        jsonObject.put("owner_ip", owner_ip);
        jsonObject.put("timestamp", timestamp);
        return jsonObject;
    }

    @Override
    public String toString() {
        return "BillBoardEntry{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", owner_ip='" + owner_ip + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
