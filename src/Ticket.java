import java.time.LocalDateTime;
import java.util.Random;

/**
 * Ticket class describing elements of collection
 */
public class Ticket{
    private Integer id;
    private String  name;
    public Coordinates coordinates;
    public Event event;
    private double price ;
    public TicketType type;
    private LocalDateTime creationDate;


    /**
     * Standard constructor
     * @param id - id of ticket, generated automatically
     * @param name - name of ticket
     * @param coordinates - coordinates of ticket
     * @param creationDate creation date of ticket , generated automatically
     * @param price - price of ticket
     * @param e - Event of ticket
     * @param t - String description for TicketType of ticke
     */
    public Ticket(Integer id, String name, Coordinates coordinates, Event e, double price, String t, LocalDateTime creationDate) {//, , TicketType t
        this.id = id;
        if (id == null) this.id = new Random().nextInt();

        if ((name != null) && (name.length() != 0)) {
            this.name = name;
        } else {
            throw new NumberFormatException();
        }

        if (coordinates != null) {
            this.coordinates = coordinates;
        } else {
            throw new NumberFormatException();
        }

        this.creationDate = creationDate;
        if (creationDate == null) this.creationDate = LocalDateTime.now();

        if (price > 0) {
            this.price = price;
        } else {
            throw new NumberFormatException();
        }
        switch (t){
            case "BUDGETARY":
                this.type = TicketType.BUDGETARY;
                break;
            case "CHEAP":
                this.type = TicketType.CHEAP;
                break;
            case "USUAL":
                this.type = TicketType.USUAL;
                break;
            case "VIP":
                this.type = TicketType.VIP;
                break;
            default: throw new NumberFormatException();
        }

        this.event = e;
    }


    public Integer getId() {
        return id;
    }

    public double getPrice(){
        return price;
    }

    public String getName() {
        return name;
    }

    public java.time.LocalDateTime getCreationDate() {
        return creationDate;
    }

    public Coordinates getCoords() {
        coordinates.getX();
        coordinates.getY();
        return coordinates;
    }

    public Event getEvent(){
        event.getEventType();
        event.getIdTicket();
        event.getMinAge();
        event.getNameTicket();
        event.getTicketsCount();
        return event;
    }
    /**
     * update of ticket, used when command 'update' called.
     * @param ticket - the ticket that will change the ticket that has the method 'update()' called
     */
    public void update(Ticket ticket){
        this.id = ticket.getId();
        this.name = ticket.getName();
        this.coordinates = ticket.getCoords();
        this.event = ticket.getEvent();
        this.type = ticket.getType();
        this.creationDate = ticket.getCreationDate();
        this.price = ticket.getPrice();
        this.price = ticket.getPrice();
    }

    public TicketType getType(){
        return type;
    }
    public void setId(Integer id){
        this.id = id;
    }


    @Override
    public String toString() {
        return "["+id+" " +name+" " + " "+coordinates.getX()+" "+ coordinates.getY()+" " + " " +event.getIdTicket() + " " + event.getNameTicket() + " " + event.getMinAge()+ " " + event.getTicketsCount() + " " + event.getEventType() + " " + price + " "+ type + "]";
    }

}
/**
 * class describing coordinates of ticket
 */
class Coordinates {
    public double x;
    public Long y; //Поле не может быть null

    public Coordinates(double x, Long y) {
        this.x = x;
        if (y != null) {
            this.y = y;
        }
    }
    public double getX(){return x;}
    public Long getY(){return y;}

}
/**
 * class event - describe Event in ticket
 */
class Event {
    private Integer id; //Значение поля должно быть больше 0, Значение этого поля должно быть уникальным, Значение этого поля должно генерироваться автоматически
    private String name; //Поле не может быть null, Строка не может быть пустой
    private int minAge;
    private long ticketsCount; //Значение поля должно быть больше 0
    private EventType eventType; //Поле может быть null
    /**
     * Standard constructor
     * @param id - id of event, generated automatically
     * @param name - name of event
     * @param minAge - minAge for this Event
     * @param ticketsCount - ticketsCount of event
     * @param s - String description for EventType
     */
    public Event(Integer id, String name, int minAge, long ticketsCount, String s){
        this.id = id;
        if (id == null) this.id = new Random().nextInt();


        if ((name != null) && (name.length() != 0)) {
            this.name = name;
        } else {
            throw new NumberFormatException();
        }

        this.minAge = minAge;

        if (ticketsCount > 0) {
            this.ticketsCount = ticketsCount;
        } else {
            throw new NumberFormatException();
        }


        switch (s){
            case "OPERA":
                this.eventType = EventType.OPERA;
                break;
            case "CONCERT":
                this.eventType = EventType.CONCERT;
                break;
            case "FOOTBALL":
                this.eventType = EventType.FOOTBALL;
                break;
            case "BASKETBALL":
                this.eventType = EventType.BASKETBALL;
                break;
            case "THEATRE_PERFORMANCE":
                this.eventType = EventType.THEATRE_PERFORMANCE;
                break;
            default: throw new NumberFormatException();
        }
    }
    public Integer getIdTicket(){return id;}
    public String getNameTicket(){return name;}
    public Integer getMinAge(){return minAge;}
    public Long getTicketsCount(){return ticketsCount;}

    public EventType getEventType() {
        return eventType;
    }
    public void setIdEvent(Integer id){
        this.id = id;
    }
}
/**
 * class describing type of Ticket
 */
enum TicketType {
    VIP,
    USUAL,
    BUDGETARY,
    CHEAP;
}
/**
 * class describing type of Event
 */
enum EventType {
    CONCERT,
    FOOTBALL,
    BASKETBALL,
    OPERA(),
    THEATRE_PERFORMANCE;
}


