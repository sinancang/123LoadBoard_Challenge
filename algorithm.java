package generateTrip;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

import edu.princeton.cs.introcs.StdRandom;
import org.jgrapht.Graph;
import org.jgrapht.graph.*;

public class algorithm {

    // convert problem into a graph
    public void antColony(LinkedList<Load> loads, Request r){
        Graph<loadVertex, DefaultWeightedEdge> g = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        loadVertex root = new loadVertex(-1);
        g.addVertex(root);
        LinkedList<Load> feasibleLoads = getFeasibleLoads(r.startTime, r.maxDestTime,r.startLat, r.startLon, loads);

        // initially add each feasible load as a neighbor of the root
        for (Load l: feasibleLoads){
            loadVertex currentVertex = new loadVertex(l.load_id);
            g.addVertex(currentVertex);
            DefaultWeightedEdge edge = g.addEdge(root, currentVertex);

            // calculate profit/time
            double profitTime = calculateProfitTime(r.startLat, r.startLon, l);

            // initially the edge weight is profitTime
            g.setEdgeWeight(edge, profitTime);
        }

        double[] profitOfSolutions = new double[100];
        LinkedList[] list = new LinkedList[100];

        for (int i = 0; i < 100; i++) {
            LinkedList<Integer> solution = new LinkedList<>();
            getSolution(g, root, solution, loads, r);

            // calculate solution profit
            double moneyMade = 0;
            for (int j: solution){
                moneyMade += loads.get(j).amount;
            }
            double cost = 0;
            for (int j: solution){
                cost += (getDistance(r.startLat, r.startLon, loads.get(j).lat1, loads.get(j).lon1)
                        +getDistance(loads.get(j).lat1, loads.get(j).lat1, loads.get(j).lat2, loads.get(j).lon2))*044;
            }

            profitOfSolutions[i] = moneyMade - cost;
            list[i] = solution;
        }

        // compare profits
        for (int j = 0; j < 100; j++) {
            double max = profitOfSolutions[0];
            int maxIndex = 0;

            for (int i = 1; i < profitOfSolutions.length; i++) {
                if (profitOfSolutions[i] > max) {
                    maxIndex = i;
                    max = profitOfSolutions[i];
                }
            }
            // multiply the weight of every edge on solution by 1.50 - (j/100)
        }

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

    // iterates over neighbors of currentNode
    // calculates edge weight for each neighbor
    // picks one neighbor to move to depending on edge weight
    // calculates feasible neighbors of new vertex
    // adds feasible loads as neighbors
    private void getSolution(Graph g, loadVertex currentNode, LinkedList<Integer> solution, LinkedList<Load> loads, Request r){
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
                if(StdRandom.bernoulli(p)){
                    solution.add(v.load_id);

                    // again, feasibility check
                    // start time is load start time plus time needed
                    // endtime remains the same
                    double dist = getDistance(loads.get(v.load_id).lat1, loads.get(v.load_id).lon1,
                            loads.get(v.load_id).lat2, loads.get(v.load_id).lon2);
                    double hoursNeeded = dist / 55.0;
                    LocalDateTime loadEndtime = loads.get(v.load_id).startTime.plusHours((long)hoursNeeded);
                    LinkedList<Load> feasibleLoads = getFeasibleLoads(loadEndtime, r.maxDestTime,
                            loads.get(v.load_id).lat2,loads.get(v.load_id).lon2, loads);

                    // add feasible loads as vertices to g
                    for (Load l: feasibleLoads){
                        // if vertex for l is not in g, then addvertex will not do anything.
                        loadVertex currentVertex = new loadVertex(l.load_id);
                        g.addVertex(currentVertex);
                        DefaultWeightedEdge edge = (DefaultWeightedEdge) g.addEdge(v, currentVertex);

                        // calculate profit/time
                        double profitTime = calculateProfitTime(r.startLat, r.startLon, l);

                        // initially the edge weight is profitTime
                        g.setEdgeWeight(edge, profitTime);
                    }

                    // recurse to find another load to add to solution
                    getSolution(g, v, solution, loads, r);
                    break;
                }
            }
        }
    }

    // remove unfeasible loads
    // if driver can reach from current location to the starting point of the load in time
    // and if the driver can complete the delivery before his endtime
    // then add loads to feasible loads
    private LinkedList<Load> getFeasibleLoads(LocalDateTime startTime, LocalDateTime endTime, double startLat,
                                                     double startLon, LinkedList<Load> loads){
        LinkedList<Load> feasibleLoads = new LinkedList<>();
        for (int i = 0; i < loads.size(); i++){
            double timeNeeded = getTime(getDistance(startLat, startLon, loads.get(i).lat1, loads.get(i).lon1));

            // if driver can make it in time to pick up
            if (startTime.plusHours((long) timeNeeded).isBefore(loads.get(i).startTime)){

                // if driver can complete delivery before end of shift
                timeNeeded += getTime(getDistance(loads.get(i).lat1, loads.get(i).lon1, loads.get(i).lat2, loads.get(i).lon2));
                if(startTime.plusHours((long) timeNeeded).isBefore(endTime)){
                    // driver can deliver in time. load is feasible
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
