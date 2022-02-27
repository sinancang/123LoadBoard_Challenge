package generateTrip;

import java.time.LocalDateTime;
import java.util.Date;

public class Load {
    public final int load_id;
    public final double lat1;
    public final double lon1;
    public final double lat2;
    public final double lon2;
    public final int amount;
    public final LocalDateTime startTime;

    public Load(int load_id, double lat1, double lon1, double lat2, double lon2, int amount, LocalDateTime startTime){
        this.load_id = load_id;
        this.lat1 = lat1;
        this.lon1 = lon1;
        this.lat2 = lat2;
        this.lon2 = lon2;
        this.amount = amount;
        this.startTime = startTime;
    }
}
