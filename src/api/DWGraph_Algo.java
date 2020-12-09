package api;

import com.google.gson.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.util.*;

public class DWGraph_Algo implements dw_graph_algorithms {
    private directed_weighted_graph g;
    private HashMap<Integer, Boolean> vis;
    private HashMap<Integer, node_data> daddy;
    private int[] low;
    private Stack<Integer> stack;
    private int count;
    private List<List<Integer>> Scc;
    private HashMap<Integer, Double> weights;

    @Override
    public void init(directed_weighted_graph g) {
        this.g = g;
    }

    @Override
    public directed_weighted_graph getGraph() {
        return g;
    }

    @Override
    public directed_weighted_graph copy() {
        directed_weighted_graph gn = new DWGraph_DS();

        for (node_data n : this.g.getV()) {
            gn.addNode(n); // Adding the nodes to the copied grpah
        }

        for (node_data n1 : this.g.getV()) {
            for (edge_data n2 : this.g.getE(n1.getKey())) {
                gn.connect(n1.getKey(), n2.getDest(), g.getEdge(n1.getKey(), n2.getDest()).getWeight()); // Connecting the nodes to their neighbors
            }
        }
        return gn;
    }

    private List<List<Integer>> Tarzan() {
        int V = g.nodeSize();
        low = new int[V];
        vis = new HashMap<>();
        stack = new Stack<>();
        Scc = new ArrayList<>();

        for (node_data n : g.getV()) {
            vis.put(n.getKey(), false);
        }

        for (int i = 0; i < V; i++)
            if (!vis.get(i)) dfs(i);

        return Scc;
    }

    private void dfs(int i) {
        low[i] = count++;
        vis.put(i, true);
        stack.push(i);
        int min = low[i];
        for (edge_data e : g.getE(i)) {
            if (!vis.get(e.getDest()))
                dfs(e.getDest());
            if (low[e.getDest()] < min)
                min = low[e.getDest()];
        }
        if (min < low[i]) {
            low[i] = min;
            return;
        }
        List<Integer> component = new ArrayList<>();
        int k;
        do {
            k = stack.pop();
            component.add(k);
            low[k] = g.nodeSize();
        } while (k != i);
        Scc.add(component);
    }

    /**
     * Returns true if and only if (iff) there is a valid path from each node to each
     * other node. NOTE: assume directional graph (all n*(n-1) ordered pairs).
     */
    @Override
    public boolean isConnected() {
        if (g.nodeSize() == 0 || g.nodeSize() == 1)
            return true;
        Tarzan();
        return Scc.size() <= 1; // if there are more than one component return false
    }

    @Override
    public double shortestPathDist(int src, int dest) {
        if (shortestPath(src, dest) == null) return -1;
        return weights.get(dest);
    }

    private void Dijkstra(node_data src) {
        Queue<node_data> pq = new PriorityQueue<>();
        vis = new HashMap<>();
        weights = new HashMap<>();
        daddy = new HashMap<>();

        for (node_data n : g.getV()) {
            vis.put(n.getKey(), false);
            weights.put(n.getKey(), Double.MAX_VALUE);
            daddy.put(n.getKey(), null);
        }

        g.getNode(src.getKey()).setWeight(0);
        weights.replace(src.getKey(), 0.0);
        pq.add(src);

        while (!pq.isEmpty()) {
            node_data u = pq.poll();

            for (edge_data n : g.getE(u.getKey())) {
                if (!vis.get(n.getSrc())) {
                    double alt = weights.get(u.getKey()) + g.getEdge(u.getKey(), n.getDest()).getWeight();
                    if (alt < weights.get(n.getDest())) {
                        weights.put(n.getDest(), alt);
                        daddy.put(n.getDest(), u);
                        pq.add(g.getNode(n.getDest()));
                    }
                }
            }
            vis.replace(u.getKey(), true); // mark node who saw all neighbors
        }
    }

    @Override
    public List<node_data> shortestPath(int src, int dest) {
        if (g.getNode(src) == null || g.getNode(dest) == null) // Checking if the keys exist
            return null;
        Dijkstra(g.getNode(src)); //Goes through the graph

        Stack<node_data> path = new Stack<>(); // To keep path in stack
        LinkedList<node_data> reverse = new LinkedList<>(); // To reverse the path from end to start

        path.add(g.getNode(dest)); // Adding the destination
        for (node_data i = daddy.get(dest); i != null; i = daddy.get(i.getKey())) { // Going backwards in Prev
            path.add(i);
        }
        if (path.peek() != g.getNode(src)) // If the path doesn't contain source, that means it's not connected from source to dest
            return null;

        while (!path.isEmpty()) {
            reverse.add(path.pop()); // reversing the path
        }

        return reverse;
    }

    @Override
    public boolean save(String file) {
        JsonObject graph = new JsonObject();
        JsonArray Edges = new JsonArray();
        JsonArray Nodes = new JsonArray();
        System.out.println("json");
        try {
            for (node_data n : g.getV()) {
                String st=n.getLocation().x() + "," + n.getLocation().y() + "," + n.getLocation().z();
                JsonObject node = new JsonObject();
                node.addProperty("pos", st);
                node.addProperty("id", n.getKey());
                Nodes.add(node);
                for (edge_data e : g.getE(n.getKey())) {
                    JsonObject edge = new JsonObject();
                    edge.addProperty("src", e.getSrc());
                    edge.addProperty("w", e.getWeight());
                    edge.addProperty("dest", e.getDest());
                    Edges.add(edge);
                }
            }
            graph.add("Edges", Edges);
            graph.add("Nodes", Nodes);
            PrintWriter pw = new PrintWriter(new File(file+".json"));
            pw.write(graph.toString());
            pw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean load(String file) {
        try {
            JSONObject graph = new JSONObject(new String(Files.readAllBytes(Paths.get(file))));
            JSONArray edges=graph.getJSONArray("Edges");
            JSONArray nodes=graph.getJSONArray("Nodes");
            directed_weighted_graph G= new DWGraph_DS();
            for (int i = 0; i < nodes.length() ; i++) {
                JSONObject n= nodes.getJSONObject(i);
                int key= n.getInt("id");
                node_data p= new NodeData(key);
                double x,y,z;
                String[] arr= (n.getString("pos")).split(",");
                x=Double.parseDouble(arr[0]);
                y=Double.parseDouble(arr[1]);
                z=Double.parseDouble(arr[2]);
                geo_location m= new GeoLocation(x,y,z);
                p.setLocation(m);
                G.addNode(p);
            }
            for (int i = 0; i <edges.length(); i++) {
                JSONObject e= edges.getJSONObject(i);
                int src = e.getInt("src");
                double w = e.getDouble("w");
                int dest = e.getInt("dest");
                G.connect(src,dest,w);
            }
            init(G);
            return true;
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return true;
    }
}





