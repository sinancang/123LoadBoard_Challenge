package generateTrip;
import java.time.*;

public class Request {
    private final int id;
    public final double startLat;
    public final double startLon;
    public final LocalDateTime startTime;
    public final LocalDateTime maxDestTime;

    public Request(int id, double startLat, double startLon, LocalDateTime startTime, LocalDateTime maxDestTime){
        this.id = id;
        this.startLat = startLat;
        this.startLon = startLon;
        this.startTime = startTime;
        this.maxDestTime = maxDestTime;
    }
}
