import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import static org.junit.jupiter.api.Assertions.*; //Supports assertions which are needed to test the methods.

class TrainTest {
    @Test
    public void testGetDepartureTime() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        Date departureTime = sdf.parse("7:00");
        Train train = new Train("Test Train", "New York", "Philadelphia", departureTime, new Date());
        assertEquals(departureTime, train.getDepartureTime());
    }
    @Test
    public void testGetArrivalTime() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        Date arrivalTime = sdf.parse("12:15");
        Train train = new Train("Test Train", "Washington", "New York", new Date(), arrivalTime);
        assertEquals(arrivalTime, train.getArrivalTime());
    }
    @Test
    public void testGetAvailableSeats() {
        Train train = new Train("Test Train", "Seattle", "Portland", new Date(), new Date());
        assertEquals(100, train.getAvailableSeats()); // Initially, all seats are available
    }
    @Test
    public void testBookSeats() {
        Train train = new Train("Test Train", "Seattle", "Portland", new Date(), new Date());
        train.bookSeats(20);
        assertEquals(80, train.getAvailableSeats()); // After booking 20 seats, 80 seats should be available
    }
    @Test
    public void testCancelSeats() {
        Train train = new Train("Test Train", "Seattle", "Portland", new Date(), new Date());
        train.bookSeats(15);
        train.cancelSeats(7);
        assertEquals(92, train.getAvailableSeats()); // After canceling 7 of the 15 booked seats, 92 seats should be available
    }
}