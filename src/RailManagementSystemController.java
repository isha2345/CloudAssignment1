import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date; //The java.util.Date class represents a specific instant in time, with millisecond precision.
import java.util.HashMap;
import java.time.Duration; //Represents a duration of time between two instants
import java.time.Instant; //Represents an instantaneous point on the time-line.
import java.time.LocalDateTime; //It is used in this code to perform calculations involving the current time and the arrival time of trains.
import java.time.LocalTime;
import java.time.ZoneId; //Identifies the zone
import java.time.format.DateTimeFormatter; //Provides Date formatting

public class RailManagementSystemController { // Controller class for the Rail Management System. Handles interactions between the Model class and the View class.
    private RailManagementSystemModel model;// Model instance for the Rail Management System.
    private RailManagementSystemView view; //View instance for the Rail Management System.
    private FeedbackForm feedbackForm; // Form for user feedback.
    private List<Ticket> ticketList = new ArrayList<>(); //List to store tickets.
    //Constructor for the RailManagementSystemController.
    public RailManagementSystemController(RailManagementSystemModel model, RailManagementSystemView view) {

        this.model = model;
        this.view = view;
        final RailManagementSystemView finalView = view;
        final RailManagementSystemModel finalModel= model;
        feedbackForm = new FeedbackForm();

        view.addRefreshButtonListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateTrainInformation();
            }
        });

        view.addCancelTicketButtonListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedTicketIndex = view.getSelectedTicketIndex();
                if (selectedTicketIndex >= 0) {
                    Ticket selectedTicket = view.getTicketComboBox().getItemAt(selectedTicketIndex);
                    boolean cancellationResult = finalModel.cancelTicket(selectedTicket);
                    if (cancellationResult) {
                        JOptionPane.showMessageDialog(finalView.getFrame(), "Ticket canceled successfully!");
                        updateTicketList();
                    } else {
                        JOptionPane.showMessageDialog(finalView.getFrame(), "Ticket cancellation failed. Please try again.");
                    }
                    updateTrainInformation();
                } else {
                    JOptionPane.showMessageDialog(finalView.getFrame(), "Please select a ticket to cancel.");
                }
            }
        });

        view.addBookTicketButtonListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedTrainIndex = finalView.getSelectedTrainIndex();
                int numTickets = finalView.getNumTickets();
                boolean needRamp = finalView.isNeedRampSelected();
                boolean needWheelchair = finalView.isNeedWheelchairSelected();
                if (selectedTrainIndex >= 0 && numTickets > 0) {
                    Train selectedTrain = finalView.getTrainComboBox().getItemAt(selectedTrainIndex);
                    boolean bookingResult = finalModel.bookTicket(selectedTrain, numTickets, needRamp, needWheelchair);
                    if (bookingResult) {
                        Ticket bookedTicket = new Ticket(selectedTrain, numTickets, needRamp, needWheelchair);
                        ticketList.add(bookedTicket);
                        updateTicketList();
                        JOptionPane.showMessageDialog(finalView.getFrame(), "Ticket(s) booked successfully!");
                        feedbackForm.setVisible(true);
                    } else {
                        JOptionPane.showMessageDialog(finalView.getFrame(), "Ticket booking failed. Please check availability.");
                    }
                    updateTrainInformation();
                } else {
                    JOptionPane.showMessageDialog(finalView.getFrame(), "Please select a train and specify a valid number of tickets.");
                }
            }
        });

        view.getViewAllTicketsButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                displayAllTickets();
            }
        });

        view.setTicketComboBoxModel(new DefaultComboBoxModel<>(model.getTickets().toArray(new Ticket[0])));
        finalView.setTrainComboBoxModel(new DefaultComboBoxModel<>(model.getTrains().toArray(new Train[0])));
    }

    private void displayAllTickets() {  //Displays all tickets grouped by train number.
        Map<String, List<Ticket>> groupedTickets = model.groupTicketsByTrainNumber();
        StringBuilder groupedTicketsInfo = new StringBuilder("Grouped Tickets:\n");

        for (Map.Entry<String, List<Ticket>> entry : groupedTickets.entrySet()) {
            String trainNumber = entry.getKey();
            List<Ticket> tickets = entry.getValue();

            groupedTicketsInfo.append("Train Number: ").append(trainNumber).append("\n");

            for (Ticket ticket : tickets) {
                groupedTicketsInfo.append(ticket.getTicketInfo()).append("\n\n");
            }
        }

        view.setGroupedTicketsAreaText(groupedTicketsInfo.toString());
    }

    public void updateTicketList() { //Updates the ticket list in the view.
        List<Ticket> ticketList = model.getTickets();
        view.setTicketComboBoxModel(new DefaultComboBoxModel<>(ticketList.toArray(new Ticket[0])));
    }

    public void updateTrainInformation() {  // Updates train information in the view.
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        StringBuilder trainInfo = new StringBuilder("Train Schedule:\n");
        for (Train train : model.getTrains()) {
            LocalTime currentTime = Instant.now().atZone(ZoneId.systemDefault()).toLocalTime();
            LocalTime arrivalTime = train.getArrivalTime().toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
            Duration duration = Duration.between(currentTime, arrivalTime).abs();
            long hours = duration.toHours();
            long minutes = duration.minusHours(hours).toMinutes();
            long seconds = duration.minusHours(hours).minusMinutes(minutes).getSeconds();
            LocalDateTime departureLocalTime = LocalDateTime.ofInstant(train.getDepartureTime().toInstant(), ZoneId.systemDefault());
            LocalDateTime arrivalLocalTime = LocalDateTime.ofInstant(train.getArrivalTime().toInstant(), ZoneId.systemDefault());
            String departureTime = departureLocalTime.toLocalTime().format(timeFormatter);
            String arivalTime = arrivalLocalTime.toLocalTime().format(timeFormatter);
            trainInfo.append(train.toString() + " (Departure Time: " + departureTime +
                    ", Arrival Time: " + arivalTime +
                    ", Time Difference: " + hours + " hours " + minutes + " minutes " + seconds + " seconds)\n");
        }
        view.getTrainUpdatesArea().setText(trainInfo.toString());
    }

    public static void main(String[] args) { //Main method to start the application.

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                RailManagementSystemModel model = new RailManagementSystemModel();
                RailManagementSystemView view = new RailManagementSystemView();
                RailManagementSystemController controller = new RailManagementSystemController(model, view);

                view.getFrame().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                view.getFrame().add(view.getMainPanel());
                view.getFrame().pack();
                view.getFrame().setVisible(true);

                view.getRefreshButton().addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        controller.updateTrainInformation();
                    }
                });
            }
        });
    }
}

class RailManagementSystemModel { //Model class for the Rail Management System. Manages trains and tickets.

    private List<Train> trains; //List of trains in the system.
    private List<Ticket> tickets; //List of tickets in the system.

    public RailManagementSystemModel(){ //Constructor for the RailManagementSystemModel. Initializes trains and tickets.
        trains = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        try {
            Date departureTime1 = sdf.parse("00:55");
            Date arrivalTime1 = sdf.parse("23:00");
            Date departureTime2 = sdf.parse("00:56");
            Date arrivalTime2 = sdf.parse("21:00");
            Date departureTime3 = sdf.parse("19:00");
            Date arrivalTime3 = sdf.parse("18:50");
            trains.add(new Train("Train 1", "Boston", "Portland", departureTime1, arrivalTime1));
            trains.add(new Train("Train 2", "Chicago", "Madison", departureTime2, arrivalTime2));
            trains.add(new Train("Train 3", "San Fransisco", "Los Angeles", departureTime3, arrivalTime3));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        tickets = new ArrayList<>();
    }

    public List<Train> getTrains() {
        return trains;
    }

    public boolean cancelTicket(Ticket ticket) { //Cancels a ticket and updates the available seats for the associated train.
        if (ticket != null) {
            Train train = ticket.getTrain();
            int numSeats = ticket.getNumSeats();
            if (train != null && numSeats > 0) {
                train.cancelSeats(numSeats);
                tickets.remove(ticket);
                return true;
            }
        }
        return false;
    }
    //Books a ticket and updates the available seats for the associated train.
    public boolean bookTicket(Train train, int numTickets, boolean needRamp, boolean needWheelchair) {
        if (train != null && numTickets > 0) {
            if (train.getAvailableSeats() >= numTickets) {
                train.bookSeats(numTickets);
                tickets.add(new Ticket(train, numTickets, needRamp, needWheelchair));
                return true;
            }
        }
        return false;
    }

    public List<Ticket> getTickets() {
        return tickets;
    }

    public Map<String, List<Ticket>> groupTicketsByTrainNumber() { // Groups tickets by train number.
        Map<String, List<Ticket>> groupedTickets = new HashMap<>();

        for (Ticket ticket : tickets) {
            String trainNumber = ticket.getTrain().getName();
            groupedTickets.computeIfAbsent(trainNumber, k -> new ArrayList<>()).add(ticket);
        }

        return groupedTickets;
    }
}

class Train { //Class representing a Train.

    private String name;
    private String source;
    private String destination;
    private int totalSeats;
    private int bookedSeats;
    private Date departureTime;
    private Date arrivalTime;
    public Train(String name, String source, String destination, Date departureTime, Date arrivalTime) {
        this.name = name;
        this.source = source;
        this.destination = destination;
        this.totalSeats = 100;
        this.bookedSeats = 0;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
    }

    public Date getDepartureTime() {
        return departureTime;
    }

    public Date getArrivalTime() {
        return arrivalTime;
    }

    public int getAvailableSeats() {
        return totalSeats - bookedSeats;
    }

    public void bookSeats(int numSeats) {
        bookedSeats += numSeats;
    }

    public void cancelSeats(int numSeats) {
        if (numSeats > 0 && numSeats <= bookedSeats) {
            bookedSeats -= numSeats;
        }
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name + " - From " + source + " to " + destination + " (" + getAvailableSeats() + " seats available)";
    }

}
class Ticket { //Class representing a Ticket.

    private Train train;
    private int numSeats;
    private boolean needRamp;
    private boolean needWheelchair;
    public Ticket(Train train, int numSeats, boolean needRamp, boolean needWheelchair) {
        this.train = train;
        this.numSeats = numSeats;
        this.needRamp = needRamp;
        this.needWheelchair = needWheelchair;
    }

    public Train getTrain() {
        return train;
    }

    public int getNumSeats() {
        return numSeats;
    }

    public String getTicketInfo() {
        return  "Number of Seats: " + numSeats +
                "\nNeed Ramp: " + needRamp +
                "\nNeed Wheelchair: " + needWheelchair;
    }

    @Override
    public String toString() {
        return "Ticket for " + train.getName() + " - " + numSeats + " seat(s)";
    }
}

@SuppressWarnings("serial")
class FeedbackForm extends JFrame { //FeedbackForm class for collecting user feedback after booking a ticket.

    private JTextArea feedbackTextArea;
    private JButton submitButton;
    private JComboBox<Integer> likelihoodComboBox;

    public FeedbackForm() {
        setTitle("Feedback Form");
        setSize(450, 200);
        setLocationRelativeTo(null);
        feedbackTextArea = new JTextArea(1, 1);
        submitButton = new JButton("Submit Feedback");
        likelihoodComboBox = new JComboBox<>(new DefaultComboBoxModel<>(new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10}));
        likelihoodComboBox.setRenderer(new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                return label;
            }
        });

        JPanel likelihoodPanel = new JPanel(new GridLayout(2, 1));
        likelihoodPanel.add(new JLabel("How likely are you to recommend us to a friend or family member?"));
        likelihoodPanel.add(likelihoodComboBox);

        JPanel commentsPanel=new JPanel(new GridLayout(2,1));
        commentsPanel.add(new JLabel("Add your valuable comments"));
        commentsPanel.add(feedbackTextArea);

        JPanel submitPanel=new JPanel();
        submitPanel.add(submitButton);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(likelihoodPanel, BorderLayout.NORTH);
        panel.add(commentsPanel, BorderLayout.CENTER);
        panel.add(submitPanel, BorderLayout.SOUTH);
        add(panel);

        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String feedback = feedbackTextArea.getText();
                int likelihood = (Integer) likelihoodComboBox.getSelectedItem();
                JOptionPane.showMessageDialog(null, "Feedback submitted successfully!\nYour Recommendation out of 10 : " + likelihood+"\nYour Comments : "+feedback);
                dispose();
            }
        });
    }
}

class RailManagementSystemView {

    private JFrame frame;
    private JPanel mainPanel;
    private JTextArea trainUpdatesArea;
    private JButton refreshButton;
    private JComboBox<Train> trainComboBox;
    private JTextField numTicketsField;
    private JButton bookTicketButton;
    private JComboBox<Ticket> ticketComboBox;
    private JButton cancelTicketButton;
    private JCheckBox needRampCheckbox;
    private JCheckBox needWheelchairCheckbox;
    private JButton viewAllTicketsButton;
    private JTextArea groupedTicketsArea;

    public RailManagementSystemView() { //View class for the application.

        frame = new JFrame("Rail Management Ticket Booking System");
        mainPanel = new JPanel(new BorderLayout());
        trainUpdatesArea = new JTextArea(10, 30);
        trainUpdatesArea.setEditable(false);
        refreshButton = new JButton("Refresh");

        JPanel bookingPanel = new JPanel();
        bookingPanel.setLayout(new FlowLayout());
        trainComboBox = new JComboBox<>();
        ticketComboBox = new JComboBox<>();
        numTicketsField = new JTextField(5);
        bookTicketButton = new JButton("Book Ticket");
        needRampCheckbox = new JCheckBox("Need Ramp");
        needWheelchairCheckbox = new JCheckBox("Need Wheelchair");
        cancelTicketButton = new JButton("Cancel Ticket");
        viewAllTicketsButton = new JButton("View All Tickets");
        groupedTicketsArea = new JTextArea(10, 30);
        groupedTicketsArea.setEditable(false);

        bookingPanel.add(new JLabel("Select Train: "));
        bookingPanel.add(trainComboBox);
        bookingPanel.add(new JLabel("Number of Tickets: "));
        bookingPanel.add(numTicketsField);
        bookingPanel.add(needRampCheckbox);
        bookingPanel.add(needWheelchairCheckbox);
        bookingPanel.add(bookTicketButton);

        JPanel ticketCancellationPanel = new JPanel();
        ticketCancellationPanel.setLayout(new FlowLayout());
        ticketCancellationPanel.add(new JLabel("Select Ticket to Cancel: "));
        ticketCancellationPanel.add(ticketComboBox);
        ticketCancellationPanel.add(cancelTicketButton);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        buttonPanel.add(viewAllTicketsButton);
        buttonPanel.add(cancelTicketButton);
        buttonPanel.add(refreshButton);

        mainPanel.add(new JScrollPane(trainUpdatesArea), BorderLayout.CENTER);
        mainPanel.add(bookingPanel, BorderLayout.NORTH);
        mainPanel.add(ticketCancellationPanel, BorderLayout.SOUTH);
        mainPanel.add(buttonPanel, BorderLayout.EAST);
        mainPanel.add(new JScrollPane(groupedTicketsArea), BorderLayout.WEST);
    }

    public JFrame getFrame() {
        return frame;
    }

    public JComboBox<Ticket> getTicketComboBox() {
        return ticketComboBox;
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public JTextArea getTrainUpdatesArea() {
        return trainUpdatesArea;
    }

    public JButton getRefreshButton() {
        return refreshButton;
    }

    public JComboBox<Train> getTrainComboBox() {
        return trainComboBox;
    }

    public JTextField getNumTicketsField() {
        return numTicketsField;
    }

    public JButton getBookTicketButton() {
        return bookTicketButton;
    }

    public void setTrainComboBoxModel(DefaultComboBoxModel<Train> model) {
        trainComboBox.setModel(model);
    }

    public void addRefreshButtonListener(ActionListener listener) {
        refreshButton.addActionListener(listener);
    }

    public void addBookTicketButtonListener(ActionListener listener) {
        bookTicketButton.addActionListener(listener);
    }

    public int getSelectedTrainIndex() {
        return trainComboBox.getSelectedIndex();
    }

    public void addCancelTicketButtonListener(ActionListener listener) {
        cancelTicketButton.addActionListener(listener);
    }

    public boolean isNeedRampSelected() {
        return needRampCheckbox.isSelected();
    }

    public boolean isNeedWheelchairSelected() {
        return needWheelchairCheckbox.isSelected();
    }

    public int getSelectedTicketIndex() {
        return ticketComboBox.getSelectedIndex();
    }

    public void setTicketComboBoxModel(DefaultComboBoxModel<Ticket> model) {
        ticketComboBox.setModel(model);
    }

    public JButton getViewAllTicketsButton() {
        return viewAllTicketsButton;
    }

    public void setGroupedTicketsAreaText(String text) {
        groupedTicketsArea.setText(text);
    }

    public int getNumTickets() {
        try {
            return Integer.parseInt(numTicketsField.getText());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
