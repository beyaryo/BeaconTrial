package id.developer.lynx.cubeacontry;

/**
 * Created by Bend on 10/15/2016.
 */

public class ObjectBeacon implements Comparable<ObjectBeacon>{

    private String mac, uuid;
    private Double jarak;
    private Integer major;

    public ObjectBeacon(String mac, String uuid, Integer major, Double jarak){
        this.mac = mac;
        this.uuid = uuid;
        this.major = major;
        this.jarak = jarak;
    }

    public Double getJarak() {
        return jarak;
    }

    public String getMac() {
        return mac;
    }

    public String getUuid() {
        return uuid;
    }

    public Integer getMajor() {
        return major;
    }

    @Override
    public int compareTo(ObjectBeacon o) {
        return this.getJarak().compareTo(o.getJarak());
    }
}
