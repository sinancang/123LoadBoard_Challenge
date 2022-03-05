package generateTrip;

public class runProgram {
    public static void main(String[] args){
        System.out.println("Hello!");
        String LoadsCSV = "generateTrip/123Loadboard_CodeJam_2022_dataset.csv";
        String tripRequestsJSON = "generateTrip/123Loadboard_CodeJam_2022_input_sample_s300.json";
        tripRequestParser t = new tripRequestParser(LoadsCSV, tripRequestsJSON);
        System.out.println("With a little bit of luck, we can find the optimal solution!");
        String result = t.getTrips();
        System.out.println(result);
    }
}
