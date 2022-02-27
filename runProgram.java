package generateTrip;

public class runProgram {
    public static void main(String[] args){
        String LoadsCSV = "123Loadboard_CodeJam_2022_dataset.csv";
        String tripRequestsJSON = "123Loadboard_CodeJam_2022_input_sample_s300.json";
        tripRequestParser t = new tripRequestParser(LoadsCSV, tripRequestsJSON);
        String result = t.getTrips();
    }
}
