package generateTrip;

import java.io.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

public class tripRequestParser {
    private LinkedList<Load> loads = new LinkedList<>();
    private LinkedList<Request> requests = new LinkedList<>();
    private final Map<Integer, Load> idToLoad = new LinkedHashMap<Integer, Load>();

    // sets up the trip request handler
    public tripRequestParser(String loadsCSV, String tripRequestsJSON){
        System.out.println("Parsing dataset...");
        // parse input and data;
        parseLoadsCSV(loadsCSV);
        System.out.println("Done!");
        System.out.println("Parsing trip requests...");
        parseRequestsJSON(tripRequestsJSON);
        System.out.println("Done!");
    }

    // returns json for trips
    public String getTrips(){
        JSONArray result = new JSONArray();
        String jason = "[\n";

        // handle requests one by one
        for (int i = 0; i < requests.size(); i++){
            LinkedList<Integer> optimalSolution = new LinkedList<>();

            System.out.println("Calling ant colony for request number " + i);
            // call ant colony
            algorithm a = new algorithm();
            optimalSolution = a.antColony(loads, requests.get(i), idToLoad);

            jason += "    {" + "        \n \"input_trip_id\": " + requests.get(i).id + ",\n";
            jason += "        \"load_ids\": [";
            for(int j = 0; j < optimalSolution.size() - 1;j++){
                jason += " " + optimalSolution.get(j) + ",";
            }
            jason += " " + optimalSolution.get(optimalSolution.size() - 1) + " ]\n    },";

        }
        jason += "]";

        return jason;
    }

    // parse list of loads
    private void parseLoadsCSV(String path){
        String line = "";
        try {
            // use bufferedReader since it's more efficient for big files
            BufferedReader br = new BufferedReader(new FileReader(path));

            // skip first line
            br.readLine();

            // create objects for each load
            while((line = br.readLine()) != null){
                String[] values = line.split(",");
                String year = String.valueOf("" + values[10].charAt(0)+values[10].charAt(1)+values[10].charAt(2)+values[10].charAt(3));
                String month = String.valueOf("" + values[10].charAt(5)+values[10].charAt(6));
                String day = String.valueOf("" + values[10].charAt(8)+values[10].charAt(9));
                String hour = String.valueOf("" + values[10].charAt(11)+values[10].charAt(12));
                String minute = String.valueOf("" + values[10].charAt(14)+values[10].charAt(15));

                LocalDateTime time = LocalDateTime.of(Integer.parseInt(year),Integer.parseInt(month),
                        Integer.parseInt(day),Integer.parseInt(hour),Integer.parseInt(minute));

                Load l = new Load(Integer.parseInt(values[0]), Double.parseDouble(values[3]), Double.parseDouble(values[4]),
                        Double.parseDouble(values[7]), Double.parseDouble(values[8]), Integer.parseInt(values[9]),
                        time);

                // add to list of loads
                loads.add(l);
                idToLoad.put(l.load_id, l);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // parse list of requests
    private void parseRequestsJSON(String path) {
        String JSON = "";
        String line = "";
        try {
            // use bufferedReader since it's more efficient for big files
            BufferedReader br = new BufferedReader(new FileReader(path));

            // copy file to string
            while ((line = br.readLine()) != null) {
                JSON += line;
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        JSONArray inputRequests = new JSONArray(JSON);
        for (int i = 0; i < inputRequests.length(); ++i) {
            JSONObject obj = inputRequests.getJSONObject(i);

            String syear = String.valueOf(""+((String) obj.get("start_time")).charAt(0) + ((String) obj.get("start_time")).charAt(1)
                    + ((String) obj.get("start_time")).charAt(2)+((String) obj.get("start_time")).charAt(3));
            String smonth = String.valueOf("" + ((String) obj.get("start_time")).charAt(5) + ((String) obj.get("start_time")).charAt(6));
            String sday = String.valueOf(""+((String) obj.get("start_time")).charAt(8) + ((String) obj.get("start_time")).charAt(9));
            String shour = String.valueOf(""+((String) obj.get("start_time")).charAt(11) + ((String) obj.get("start_time")).charAt(12));
            String sminute = String.valueOf(""+((String) obj.get("start_time")).charAt(14) + ((String) obj.get("start_time")).charAt(15));

            LocalDateTime start = LocalDateTime.of(Integer.parseInt(syear), Integer.parseInt(smonth), Integer.parseInt(sday),
                    Integer.parseInt(shour), Integer.parseInt(sminute));

            String eyear = String.valueOf(""+((String) obj.get("max_destination_time")).charAt(0) + ((String) obj.get("max_destination_time")).charAt(1)
                    + ((String) obj.get("max_destination_time")).charAt(2)+((String) obj.get("max_destination_time")).charAt(3));
            String emonth = String.valueOf(""+((String) obj.get("max_destination_time")).charAt(5) + ((String) obj.get("max_destination_time")).charAt(6));
            String eday = String.valueOf(""+((String) obj.get("max_destination_time")).charAt(8) + ((String) obj.get("max_destination_time")).charAt(9));
            String ehour = String.valueOf(""+((String) obj.get("max_destination_time")).charAt(11) + ((String) obj.get("max_destination_time")).charAt(12));
            String eminute = String.valueOf(""+((String) obj.get("max_destination_time")).charAt(14) + ((String) obj.get("max_destination_time")).charAt(15));

            LocalDateTime end = LocalDateTime.of(Integer.parseInt(eyear), Integer.parseInt(emonth), Integer.parseInt(eday),
                    Integer.parseInt(ehour), Integer.parseInt(eminute));

            Request r = new Request((Integer) obj.get("input_trip_id"), (Double) obj.get("start_latitude"),
                    (Double) obj.get("start_longitude"), start, end);
            requests.add(r);
        }
    }
}
