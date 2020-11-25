package api;

public class NodeData implements node_data {
    private final int key;
    private static int counter=0;
    private int tag;
    private geo_location g;
    private double weight;
    private String info;

    public NodeData(){
        key=counter++;
        tag=0;
        g=null;
        weight=0;
        info=null;
    }

    public NodeData(int key){
        this.key=key;
        tag=0;
        g=null;
        weight=0;
        info=null;
    }

    @Override
    public int getKey() {
        return key;
    }

    @Override
    public geo_location getLocation() {
        return g;
    }

    @Override
    public void setLocation(geo_location p) {
        g=p;
    }

    @Override
    public double getWeight() {
        return weight;
    }

    @Override
    public void setWeight(double w) {
    weight=w;
    }

    @Override
    public String getInfo() {
        return info;
    }

    @Override
    public void setInfo(String s) {
    info=s;
    }

    @Override
    public int getTag() {
        return tag;
    }

    @Override
    public void setTag(int t) {
    tag=t;
    }

    @Override
    public String toString() {
        return " key=" + key+".";
    }
}
