package generateTrip;

import java.time.LocalDateTime;
import java.util.*;

import edu.princeton.cs.introcs.StdRandom;
import org.jgrapht.Graph;
import org.jgrapht.graph.*;
import org.jgrapht.util.ArrayUtil;

public class algorithm {
    private final Map<Integer, loadVertex> idToVertex =
            new LinkedHashMap<Integer, loadVertex>();
    private Map<Integer, Load> idToLoad;

    // convert problem into a graph
    public LinkedList<Integer> antColony(LinkedList<Load> loads, Request r, Map<Integer, Load> idToLoad) {
        System.out.println("We'll attack this optimization problem with ants!");
        this.idToLoad = idToLoad;
        Graph<loadVertex, DefaultWeightedEdge> g = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        loadVertex root = new loadVertex(-1);
        g.addVertex(root);
        idToVertex.put(root.load_id, root);

        System.out.println("Doing some pre-solving...");
        LinkedList<Load> feasibleLoads = getFeasibleLoads(r.startTime, r.maxDestTime, r.startLat, r.startLon, loads);
        loads =feasibleLoads;
        // initially add each feasible load as a neighbor of the root
        for (Load l : loads) {
            loadVertex currentVertex = new loadVertex(l.load_id);
            g.addVertex(currentVertex);
            idToVertex.put(currentVertex.load_id, currentVertex);

            DefaultWeightedEdge edge = g.addEdge(root, currentVertex);


            // calculate profit/time
            double profitTime = calculateProfitTime(r.startLat, r.startLon, l);

            // if profitTime is negative, break
            if (profitTime < 0){
                break;
            }

            // initially the edge weight is profitTime
            g.setEdgeWeight(edge, profitTime);
        }

        LinkedList<Integer> optimalSolution = new LinkedList<>();

        // repeat this process 10 times
        int o = 0;
        while(o < 10) {
            ArrayList<Double> profitOfSolutions = new ArrayList<>();
            ArrayList<LinkedList> listOfSolutions = new ArrayList<>();

            System.out.println("Deploying an army of 5 ants!");
            // deploy 5 ants to generate 5 stochastic solutions
            for (int i = 0; i < 5; i++) {
                LinkedList<Integer> solution = new LinkedList<>();
                getSolution(g, root, solution, loads, r,0);

                // calculate solution profit
                double moneyMade = 0;
                for (int j: solution) {
                    moneyMade += idToLoad.get(j).amount;
                }
                double cost = 0;
                for (int j : solution) {
                    cost += (getDistance(r.startLat, r.startLon, idToLoad.get(j).lat1, idToLoad.get(j).lon1)
                            + getDistance(idToLoad.get(j).lat1, idToLoad.get(j).lat1, idToLoad.get(j).lat2, idToLoad.get(j).lon2)) * 044;
                }

                profitOfSolutions.add(i, moneyMade - cost);
                listOfSolutions.add(i, solution);
            }

            System.out.println("Comparing solutions...");
            // compare profits of solutions
            for (int j = 0; j < 5; j++) {
                double max = profitOfSolutions.get(0);
                int maxIndex = 0;

                for (int i = 1; i < profitOfSolutions.size(); i++) {
                    if (profitOfSolutions.get(i) > max) {
                        maxIndex = i;
                        max = profitOfSolutions.get(i);

                        // if this is the last round of ants, we've found our approximation
                        if (o == 9){
                            optimalSolution = listOfSolutions.get(i);
                        }
                    }
                }

                // update pheromones
                // multiply the weight of every edge on solution by 1.50 - (j/10)
                System.out.println("Updating pheromones");
                LinkedList<Integer> solution = listOfSolutions.get(maxIndex);
                for (int k = 0; k < solution.size() - 1; k++) {
                    loadVertex v1 = idToVertex.get(solution.get(k));
                    loadVertex v2 = idToVertex.get(solution.get(k+1));
                    DefaultWeightedEdge e = g.getEdge(v1,v2);
                    if (e != null) {
                        g.setEdgeWeight(e, g.getEdgeWeight(e) * (1.50 - (double) j / 10));
                    }
                }

                // remove max element and repeat
                profitOfSolutions.remove(maxIndex);
                listOfSolutions.remove(maxIndex);
            }
            o++;
        }
        return optimalSolution;
    }


    private double getTime(double displacement){return displacement/55.0;}

    class loadVertex{
        final int load_id;
        Boolean visited = false;
        loadVertex(int load_id){
            this.load_id = load_id;
        }
    }

    private double calculateProfitTime(double startLat, double startLon, Load l){
        double displacement = (getDistance(startLat, startLon, l.lat1, l.lon1)
                +getDistance(l.lat1, l.lon1,l.lat2,l.lon2));
        double profit = l.amount - (displacement*0.40);
        double time = getTime(displacement);
        double profitTime = profit / time;
        return profitTime;
    }

    private Boolean isLegal(int load_id, Request r){
        Load l = idToLoad.get(load_id);
        double hoursNeeded = getDistance(l.lat1, l.lon1,l.lat2, l.lon2)/55.0;

        // load can't be delivered in time
        if (l.startTime.plusHours((long)hoursNeeded).isAfter(r.maxDestTime)){
            return false;
        }
        return true;
    }

    // iterates over neighbors of currentNode
    // calculates edge weight for each neighbor
    // picks one neighbor to move to depending on edge weight
    // calculates feasible neighbors of new vertex
    // adds feasible loads as neighbors
    private void getSolution(Graph g, loadVertex currentNode, LinkedList<Integer> solution, LinkedList<Load> loads, Request r, int n){
        if (n == 5){
            return;
        }
        // get all neighbors of vertex
        Set<DefaultWeightedEdge> edges = g.outgoingEdgesOf(currentNode);

        double totalWeight = 0;
        for (DefaultWeightedEdge e: edges){
            totalWeight += g.getEdgeWeight(e);
        }

        // roll the dice for each possible edge
        for (DefaultWeightedEdge e: edges){
            loadVertex v = (loadVertex) g.getEdgeTarget(e);
            if(v.visited != true){
                double p = g.getEdgeWeight(e)/ totalWeight;

                // the edge is picked
                if(isLegal(v.load_id, r) && StdRandom.bernoulli(p)){
                    n++;
                    solution.add(v.load_id);

                    // again, feasibility check
                    // start time is load start time plus time needed
                    // endtime remains the same
                    double dist = getDistance(idToLoad.get(v.load_id).lat1,idToLoad.get(v.load_id).lon1,
                            idToLoad.get(v.load_id).lat2, idToLoad.get(v.load_id).lon2);
                    double hoursNeeded = dist / 55.0;
                    LocalDateTime loadEndtime = idToLoad.get(v.load_id).startTime.plusHours((long)hoursNeeded);
                    LinkedList<Load> feasibleLoads = getFeasibleLoads(loadEndtime, r.maxDestTime,
                            idToLoad.get(v.load_id).lat2,idToLoad.get(v.load_id).lon2, loads);
                    loads = feasibleLoads;

                    // add feasible loads as vertices to g
                    for (Load l: loads){
                        // if vertex for l is in g, then addvertex will not do anything.
                        loadVertex currentVertex = new loadVertex(l.load_id);
                        g.addVertex(currentVertex);
                        idToVertex.put(currentVertex.load_id, currentVertex);
                        DefaultWeightedEdge edge = (DefaultWeightedEdge) g.addEdge(v, currentVertex);

                        // calculate profit/time
                        double profitTime = calculateProfitTime(r.startLat, r.startLon, l);

                        // non profitable loads are not feasible
                        if (profitTime < 0){
                            break;
                        }

                        // initially the edge weight is profitTime
                        g.setEdgeWeight(edge, profitTime);
                    }

                    // recurse to find another load to add to solution
                    getSolution(g, v, solution, loads, r, n);

                    // break to look no further for a choice
                    return;
                }
            }
        }
    }

    // remove completely unfeasible loads
    // ie the loads which have a starting time that has passed or after end of shift
    private LinkedList<Load> getFeasibleLoads(LocalDateTime startTime, LocalDateTime endTime, double startLat,
                                                     double startLon, LinkedList<Load> loads){
        LinkedList<Load> feasibleLoads = new LinkedList<>();
        for (int i = 0; i < loads.size(); i++){
            if(!loads.get(i).startTime.isBefore(startTime)){
                double hoursNeeded = getDistance(loads.get(i).lat1, loads.get(i).lon1,loads.get(i).lat2
                        , loads.get(i).lon2)/55.0;
                if(!loads.get(i).startTime.plusHours((long) hoursNeeded).isAfter(endTime)){
                    feasibleLoads.add(loads.get(i));
                }
            }
        }

        return feasibleLoads;
    }

    // haversine formula for distance in miles
    public static double getDistance(double lat1, double lon1, double lat2, double lon2){
        final int R = 6371;
        Double latDistance = toRad(lat2-lat1);
        Double lonDistance = toRad(lon2-lon1);
        Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) *
                        Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        Double distance = R * c;

        // convert from km to mile
        distance = 0.621371192 * distance;
        return distance;
    }

    private static Double toRad(Double value) {
        return value * Math.PI / 180;
    }
}
