import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * @author Aleksandr Larionov R3137
 * This is main class of console program
 */
public class Parser {
    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {

        FileInputStream fileInputStream = null;
        try {
            String input = System.getenv("input");
            fileInputStream = new FileInputStream(input);
        } catch (NullPointerException e) {
            System.out.println("Cant find env variable");
            System.exit(0);
        }catch (FileNotFoundException e){
            System.out.println("File not found");
            System.exit(0);
        }



        String xmlString = "";


        BufferedInputStream bf = new BufferedInputStream(fileInputStream);

        BufferedReader r = new BufferedReader(
                new InputStreamReader(bf, StandardCharsets.UTF_8));

        xmlString = r.readLine();



        Vector<Ticket> TicketCollection = new Vector<>();
        LocalDateTime data = LocalDateTime.now();



        if (xmlString.length() > 0) {// Получение фабрики, чтобы после получить билдер документов
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            // Получили из фабрики билдер, который парсит XML, создает структуру Document в виде иерархического дерева.
            DocumentBuilder builder = factory.newDocumentBuilder();
            // Запарсили XML, создав структуру Document. Теперь у нас есть доступ ко всем элементам, каким нам нужно.
            Document document = builder.parse(new ByteArrayInputStream(xmlString.getBytes(StandardCharsets.UTF_8)));
            // Получение списка всех элементов  внутри корневого элемента (getDocumentElement возвращает ROOT элемент XML файла).
            NodeList ticketElements = document.getDocumentElement().getElementsByTagName("ticket");

            // Перебор всех элементов
            Vector<Integer> ErrorsString = new Vector<>();
            boolean errors = false;
            for (int i = 0; i < ticketElements.getLength(); i++) {
                Node ticket = ticketElements.item(i);
                // Получение атрибутов каждого элемента
                NamedNodeMap attributes = ticket.getAttributes();

                LocalDateTime creationDate = null;

                try {
                    creationDate = LocalDateTime.parse(attributes.getNamedItem("creation_date").getNodeValue());
                } catch (DateTimeParseException e) {
                    errors = true;
                    ErrorsString.add(i);
                } catch (NullPointerException e) {
                    creationDate = null;
                }

                Integer id = null;
                try {         // проверяем есть ли id и считываем их
                    id = Integer.parseInt(attributes.getNamedItem("id").getNodeValue());
                } catch (NullPointerException e) {
                    id = null;
                } catch (NumberFormatException e) {
                    errors = true;
                    ErrorsString.add(i);
                }

                Integer idEvent = null;
                try {         // проверяем есть ли id и считываем их
                    idEvent = Integer.parseInt(attributes.getNamedItem("eventid").getNodeValue());
                } catch (NumberFormatException e) {
                    errors = true;
                    ErrorsString.add(i);
                }catch (NullPointerException e) {
                    idEvent = null;
                }


                try {
                    String name = attributes.getNamedItem("name").getNodeValue();




                    Double l = Double.parseDouble(attributes.getNamedItem("coordinates").getNodeValue().split(" ")[0]);
                    Coordinates coordinates = new Coordinates(l,
                            Long.parseLong(attributes.getNamedItem("coordinates").getNodeValue().split(" ")[1]));
                    String eventname = attributes.getNamedItem("eventname").getNodeValue();
                    Integer eventage = Integer.parseInt(attributes.getNamedItem("eventage").getNodeValue());
                    Long eventcount = Long.parseLong(attributes.getNamedItem("eventcount").getNodeValue());
                    String eventtype = attributes.getNamedItem("eventtype").getNodeValue();
                    Event event = new Event(idEvent, eventname, eventage, eventcount, eventtype);


                    String type = attributes.getNamedItem("type").getNodeValue();

                    Double price = Double.parseDouble(attributes.getNamedItem("price").getNodeValue());

                    TicketCollection.add(new Ticket(id, name, coordinates, event, price, type, creationDate));
                } catch (Exception e) {
                    errors = true;
                    ErrorsString.add(i);
                }

            }

            if (errors) {
                List<String> unique = new LinkedList<>();
                for (Integer integer : ErrorsString){
                    unique.add(integer.toString());
                }
                Set<String> uniqueElement = new HashSet<String>(unique);
                System.out.println("Invalid fields of elements were found. These elements will not be added to collection: " + uniqueElement);
            }
        }
        changeIds(TicketCollection);
        changeEventIds(TicketCollection);
        Scanner console = new Scanner(System.in);
        processingCommands(console, TicketCollection, false, data);

    }
    /**
     * Method that is reading and executing commands from console or file
     * @param scanner java.util.scanner from System.in or some file(script)
     * @param TicketCollection Vector Ticket, collection of tickets
     * @param fromScript variable that indicates source of commands
     * @throws ParserConfigurationException if problems with parsing script
     */
    public static void processingCommands(Scanner scanner, Vector<Ticket> TicketCollection, boolean fromScript, LocalDateTime date) throws  ParserConfigurationException {
        boolean exitStatus = false;
        while (!exitStatus){
            String[] text = null;
            if (scanner.hasNext()){
                text = scanner.nextLine().replaceAll("^\\s+", "").split(" ", 2);
            } else {
                System.exit(0);
            }
            String command = text[0];
            String argument;
            try{
                argument = text[1];
            } catch (ArrayIndexOutOfBoundsException e) {
                argument = null;
            }
            switch (command) {
                case ("save"):
                    Document newDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
                    Element rootElement = newDocument.createElement("Ticket");
                    newDocument.appendChild(rootElement);
                    for (Ticket ticket : TicketCollection) {
                        Element ticketfield = newDocument.createElement("ticket");
                        rootElement.appendChild(ticketfield);
                        ticketfield.setAttribute("id", ticket.getId().toString());
                        ticketfield.setAttribute("name", ticket.getName());
                        String coordinatesField = ticket.getCoords().getX() + " " + ticket.getCoords().getY();
                        ticketfield.setAttribute("coordinates", coordinatesField);

                        ticketfield.setAttribute("eventid", ticket.event.getIdTicket().toString());
                        ticketfield.setAttribute("eventname", ticket.event.getNameTicket());
                        ticketfield.setAttribute("eventage", ticket.event.getMinAge().toString());
                        ticketfield.setAttribute("eventcount", ticket.event.getTicketsCount().toString());
                        ticketfield.setAttribute("eventtype", ticket.event.getEventType().toString());

                        ticketfield.setAttribute("creation_date", ticket.getCreationDate().toString());
                        String typeField = ticket.type.toString();
                        ticketfield.setAttribute("type", typeField);
                        ticketfield.setAttribute("price", Double.toString(ticket.getPrice()));
                    }
                    writeDocument(newDocument, System.getenv("output"));
                    System.out.println("The command was executed");
                    break;
                case ("help"):
                    if (argument != null) System.out.println("'help' command was detected");
                    System.out.println(
                            "help : вывести справку по доступным командам\n" +
                                    "info : вывести в стандартный поток вывода информацию о коллекции (тип, дата инициализации, количество элементов и т.д.)\n" +
                                    "show : вывести в стандартный поток вывода все элементы коллекции в строковом представлении\n" +
                                    "add {element} : добавить новый элемент в коллекцию\n"+
                                    "update id {element} : обновить значение элемента коллекции, id которого равен заданному\n"+
                                    "remove id : удалить элемент из коллекции по его id\n" +
                                    "clear : очистить коллекцию\n"+
                                    "save : сохранить коллекцию в файл\n"+
                                    "execute_script file_name : считать и исполнить скрипт из указанного файла. В скрипте содержатся команды в таком же виде, в котором их вводит пользователь в интерактивном режиме.\n"+
                                    "insert_at index {element} : добавить новый элемент в заданную позицию\n"+
                                    "add_if_min {element} : добавить новый элемент в коллекцию, если его значение меньше, чем у наименьшего элемента этой коллекции\n"+
                                    "shuffle : перемешать элементы коллекции в случайном порядке\n"+
                                    "average_of_price : вывести среднее значение поля price для всех элементов коллекции\n"+
                                    "count_by_price price : вывести количество элементов, значение поля price которых равно заданному\n"+
                                    "print_unique_event : вывести уникальные значения поля event всех элементов в коллекции\n");
                    break;
                case ("info"):
                    if (argument != null) System.out.println("'info' command was detected");
                    System.out.println("TicketCollection's type : Vector  \n" + "number of elements : " + TicketCollection.size()+"\n" + "data:"+date);
                    break;
                case ("show"):
                    if (argument != null) System.out.println("'show' command was detected");
                    for (Ticket ticket : TicketCollection){
                        System.out.println(ticket.toString());
                    }
                    break;
                case ("update"):
                    try {
                        int id = Integer.parseInt(argument);
                        boolean existIdStatus = false;
                        for (Ticket ticket : TicketCollection) {
                            if (ticket.getId() == id){
                                existIdStatus = true;
                            }
                        }
                        if (existIdStatus){
                            Ticket inputTicket = inputTicket();
                            for (Ticket ticket : TicketCollection){
                                if (ticket.getId() == id){
                                    ticket.update(inputTicket);
                                }
                            }
                            System.out.println("Элемент обновлен");
                        } else {
                            System.out.println("Таких элементов нет, введите show , чтобы увидеть эллементы коллекции");
                        }

                    } catch (NumberFormatException e){
                        System.out.println("Ошибка: неправильный ввод, попробуйте еще раз");
                    }
                    break;
                case ("remove"):
                    try {
                        int id = Integer.parseInt(argument);
                        boolean isRemoved = false;
                        for (Iterator<Ticket> iterator = TicketCollection.iterator(); iterator.hasNext(); ) {
                            Ticket nextticket = iterator.next();
                            if (nextticket.getId() == id){
                                iterator.remove();
                                isRemoved = true;
                            }
                        }
                        if (isRemoved) {
                            System.out.println("Элемент был удален");
                        } else {
                            System.out.println("Нет элемента с таким id ");
                        }
                    } catch (NumberFormatException e){
                        System.out.println("Неправильный ввод, попробуйте еще раз");
                    }
                    break;
                case ("add"):
                    if (argument != null) System.out.println("'add' command was detected");
                    Ticket addTicket = inputTicket();
                    TicketCollection.add(addTicket);
                    System.out.println("Элемент добавлен");
                    break;
                case ("add_if_min"):
                    if (argument != null) System.out.println("'add_if_min' command was detected");


                    List<Double> list = new LinkedList<>();
                    for (Ticket ticket: TicketCollection){
                        list.add(ticket.getPrice());
                    }
                    Double min = Collections.min(list);
                    Ticket inputTicketMin = inputTicket();
                    if (inputTicketMin.getPrice() < min){
                        TicketCollection.add(inputTicketMin);
                        System.out.println("Элемент был добавлен");
                    }
                    else {
                        System.out.println("Элемент НЕ был добавлен");
                    }
                    break;
                case ("insert_at"):
                    if (argument != null) System.out.println("'insert_at' command was detected");
                    try {
                        Ticket insertElement = inputTicket();
                        TicketCollection.insertElementAt(insertElement, Integer.parseInt(argument));
                    } catch (NumberFormatException e){
                        System.out.println("Ошибка: укажите индекс");
                    }
                    System.out.println("The command was executed");
                    break;
                case ("shuffle"):
                    if (argument != null) System.out.println("'shuffle' command was detected");
                    Collections.shuffle(TicketCollection);
                    System.out.println("The command was executed");
                    break;
                case ("average_of_price"):
                    if (argument != null) System.out.println("'average_of_price' command was detected");
                    double x = 0;
                    for (Ticket ticket:TicketCollection){
                        x+=ticket.getPrice();
                    }
                    System.out.println("Среднее значение price для всех эллементов:" + x/TicketCollection.size());
                    break;
                case ("count_by_price"):
                    if (argument != null) System.out.println("'count_by_price' command was detected");
                    try {
                        int numOfElement = 0;
                        for (Ticket ticket : TicketCollection) {
                            if (ticket.getPrice() == Double.parseDouble(argument)) {
                                numOfElement += 1;
                            }
                        }
                        System.out.println("Колличество эллементов , у которых price равен заданному:" + numOfElement);
                    } catch (NullPointerException e){
                        System.out.println("Ошибка: было указано пустое поле, введите значение цены");
                }  catch ( NumberFormatException e){
                    System.out.println("Ошибка: неправильный ввод, укажите цену");
                }
                    break;
                case ("print_unique_event"):
                    if (argument != null) System.out.println("'print_unique_event' command was detected");
                    List<String> unique = new LinkedList<>();
                    for (Ticket ticket:TicketCollection){
                        unique.add(ticket.event.getEventType().toString());
                    }
                    Set<String> uniqueElement = new HashSet<String>(unique);
                    System.out.println("Unique elements of eventType: " + uniqueElement.toString());
                    break;
                case ("execute_script") :
                    if (fromScript) {
                        System.out.println("Danger of recursion. Command 'execute_script' skipped");
                        break;
                    } else {
                        try {
                            File script = new File(argument);
                            processingCommands(new Scanner(script), TicketCollection, true, LocalDateTime.now());
                        } catch (FileNotFoundException e) {
                            System.out.println("Неверный путь, попробуйте еще раз");
                        } catch (NullPointerException e) {
                            System.out.println("Введите путь, попробуйте еще раз");
                        }
                        break;
                    }
                case ("clear"):
                    if (argument != null) System.out.println("'clean' command was detected");
                    TicketCollection.clear();
                    System.out.println("The command was executed");
                    break;
                case ("exit"):
                    if (argument != null) System.out.println("'exit' command was detected");
                    exitStatus = true;
                    System.out.println("The command was executed");
                    break;
                default:
                    System.out.println("Invalid command. Try 'help' to see list of commands");
            }
        }
    }
    /**
     * Method that transforms Document format to xml string and writes it in file
     * @param document Document to write
     * @param path Path to file
     * @throws TransformerFactoryConfigurationError if error in transforming
     */
    public static void writeDocument(Document document, String path) throws TransformerFactoryConfigurationError {
        Transformer transformer;
        DOMSource domSource;
        FileWriter stream;
        try {
            transformer = TransformerFactory.newInstance().newTransformer();
            domSource = new DOMSource(document);
            String output = path;
            File file = new File(path);
            if (file.canWrite()) {
                stream = new FileWriter(output);
                StreamResult result = new StreamResult(stream);
                transformer.transform(domSource, result);
            } else {
                System.out.println("Permission to edit file denied");
            }
        } catch (TransformerException e) {
            e.printStackTrace(System.out);
        } catch (FileNotFoundException e){
            System.out.println("File not found. Try again");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * Method goes through the set and change duplicate id's of tickets
     * @param tickets Vector of Tickets
     */
    public static void changeIds(Vector<Ticket> tickets){
        LinkedHashSet<Integer> newid = new LinkedHashSet<>();
        for (Ticket ticket : tickets) {
            if (!newid.add(ticket.getId())) {
                ticket.setId(new Random().nextInt());
            }
        }
    }
    public static void changeEventIds(Vector<Ticket> tickets){
        LinkedHashSet<Integer> newid = new LinkedHashSet<>();
        for (Ticket ticket : tickets) {
            if (!newid.add(ticket.event.getIdTicket())) {
                ticket.event.setIdEvent(new Random().nextInt());
            }
        }
    }
    /**
     * Method that is reading ticket fields from console to create new Ticket
     * @return new Ticket
     * @throws NumberFormatException if problems with format of input fields
     */
    public static Ticket inputTicket() throws NumberFormatException {
        Scanner consoleScanner = new Scanner(System.in);
        int exceptionStatus = 0; // для проверки на исключения парсинга и несоответсвия правилам
        System.out.println("Enter name");
        String name = "";
        while (exceptionStatus == 0){
            if (consoleScanner.hasNext()){
                name = consoleScanner.nextLine();
                if ((name != null) && (name.length() > 0)) {
                    exceptionStatus = 1;
                } else {
                    System.out.println("field can't be empty. Try again");
                }
            } else {
                System.exit(0);
            }
        }
        System.out.println("Enter x coordinate (double)");
        double x = inputAnyDouble();
        System.out.println("Enter y coordinate (Lonf)");
        Long y = inputAnyLong();
        Coordinates coordinates = new Coordinates(x, y);
        System.out.println("Enter price (Double, positive)");
        Double price = inputPositiveDouble();
        System.out.println("Enter TicketType(VIP, USUAL, BUDGETARY, CHEAP)");
        String TicketType = null;
        if (consoleScanner.hasNext()){
            TicketType = consoleScanner.nextLine();
        } else {
            System.exit(0);
        }
        TicketType type = inputTicketType(TicketType);
        int exceptionStat2 = 0;
        System.out.println("Enter EventName ");
        String EventName = null;


        while (exceptionStat2 == 0){
            if (consoleScanner.hasNext()){
                EventName = consoleScanner.nextLine();
                if ((EventName != null) && (EventName.length() > 0)) {
                    exceptionStat2 = 1;
                } else {
                    System.out.println("field can't be empty. Try again");
                }
            } else {
                System.exit(0);
            }
        }

        System.out.println("Enter minAge (int)");
        int minAge = inputPositiveInt();
        System.out.println("Enter ticketCount (long)");
        long ticketCount = inputPositiveLong();
        System.out.println("Enter EventType(CONCERT, FOOTBALL, BASKETBALL, OPERA,THEATRE_PERFORMANCE)");
        String EventType = null;
        if (consoleScanner.hasNext()){
            EventType = consoleScanner.nextLine();

        } else {
            System.exit(0);
        }

        EventType eventType = inputEventType(EventType);
        Event event = new Event(null, EventName, minAge,ticketCount, eventType.toString());
        Ticket inputTicket = new Ticket(null, name, coordinates, event, price, type.toString(), null );
        return inputTicket;
    }
    /**
     * Method that reads any Long field from console
     * @return Long field
     */
    public static Long inputAnyLong(){
        int exceptionStatus = 0;
        Scanner inputScanner = new Scanner(System.in);
        Long x = null;
        if (inputScanner.hasNext()){
            while (exceptionStatus == 0){
                try {
                    x = Long.parseLong(inputScanner.nextLine());
                    exceptionStatus = 1;
                } catch (NumberFormatException e) {
                    System.out.println("Input must be Long. Try again");
                }
            }
        } else {
            System.exit(0);
        }
        return x;
    }

    /**
     * Method that reads positive Long field from console
     * @return Long field
     */
    public static Long inputPositiveLong(){
        int exceptionStatus = 0;
        Scanner inputScanner = new Scanner(System.in);
        Long x = null;
        if (inputScanner.hasNext()){
            while (exceptionStatus >= 0){
                try {
                    x = Long.parseLong(inputScanner.nextLine());
                    if (x <= 0) {
                        exceptionStatus = 2;
                    } else {
                        exceptionStatus = -1;
                    }
                } catch (NumberFormatException e) {
                    exceptionStatus = 1;
                }
                switch (exceptionStatus) {
                    case (1):
                        System.out.println("Input must be long. Try again.");
                        break;
                    case (2):
                        System.out.println("Input cant be <= 0. Try again");
                        break;
                }
            }
        } else {
            System.exit(0);
        }

        return x;
    }
    /**
     * Method that reads positive Integer field from console
     * @return Integer field
     */
    public static Integer inputPositiveInt(){
        int exceptionStatus = 0;
        Scanner inputScanner = new Scanner(System.in);
        Integer x = null;
        if (inputScanner.hasNext()){
            while (exceptionStatus >= 0){
                try {
                    x = Integer.parseInt(inputScanner.nextLine());
                    if (x < 0) {
                        exceptionStatus = 2;
                    } else {
                        exceptionStatus = -1;
                    }
                } catch (NumberFormatException e) {
                    exceptionStatus = 1;
                }
                switch (exceptionStatus) {
                    case (1):
                        System.out.println("Input must be int. Try again.");
                        break;
                    case (2):
                        System.out.println("Input cant be < 0. Try again");
                        break;
                }
            }
        } else {
            System.exit(0);
        }

        return x;
    }
    /**
     * Method that reads any Double field from console
     * @return Double field
     */
    public static Double inputAnyDouble(){
        int exceptionStatus = 0;
        Scanner inputScanner = new Scanner(System.in);
        Double x = null;
        if (inputScanner.hasNext()){
            while (exceptionStatus == 0){
                try {
                    x = Double.parseDouble(inputScanner.nextLine());
                    exceptionStatus = 1;
                } catch (NumberFormatException e) {
                    System.out.println("Input must be Duble. Try again.");
                }
            }
        } else {
            System.exit(0);
        }
        return x;
    }
    /**
     * Method that reads positive Double field from console
     * @return Double field
     */
    public static Double inputPositiveDouble(){
        int exceptionStatus = 0;
        Scanner inputScanner = new Scanner(System.in);
        Double x = null;
        if (inputScanner.hasNext()){
            while (exceptionStatus >= 0){
                try {
                    x = Double.parseDouble(inputScanner.nextLine());
                    if (x <= 0) {
                        exceptionStatus = 2;
                    } else {
                        exceptionStatus = -1;
                    }
                } catch (NumberFormatException e) {
                    exceptionStatus = 1;
                }
                switch (exceptionStatus) {
                    case (1):
                        System.out.println("Input must be Double. Try again.");
                        break;
                    case (2):
                        System.out.println("Input can't be less than 0. Try again");
                        break;
                }
            }
        } else {
            System.exit(0);
        }
        return x;
    }
    /**
     * Method transform String (received from console) to TicketType. If doesn't match any type then read next string from console
     * @param type String to transform
     * @return received type
     */
    public static TicketType inputTicketType(String type) {
        int exceptionStatus = 0;
        Scanner inputScanner = new Scanner(System.in);
        TicketType ticketType = TicketType.CHEAP;
        while (exceptionStatus == 0){
            switch (type){
                case ("CHEAP"):
                    ticketType = TicketType.CHEAP;
                    exceptionStatus = 1;
                    break;
                case ("VIP"):
                    ticketType = TicketType.VIP;
                    exceptionStatus = 1;
                    break;
                case ("BUDGETARY"):
                    ticketType = TicketType.BUDGETARY;
                    exceptionStatus = 1;
                    break;
                case ("USUAL"):
                    ticketType = TicketType.USUAL;
                    exceptionStatus = 1;
                    break;
                default:
                    System.out.println("Invalid Tickettype. Try again");
                    type = inputScanner.nextLine();
                    break;
            }
        }
        return ticketType;
    }
    /**
     * Method transform String (received from console) to EventType. If doesn't match any type then read next string from console
     * @param type String to transform
     * @return received type
     */
    public static EventType inputEventType(String type) {
        int exceptionStatus = 0;
        Scanner inputScanner = new Scanner(System.in);
        EventType eventType = EventType.CONCERT;
        while (exceptionStatus == 0){
            switch (type){
                case ("CONCERT"):
                    eventType = EventType.CONCERT;
                    exceptionStatus = 1;
                    break;
                case ("BASKETBALL"):
                    eventType = EventType.BASKETBALL;
                    exceptionStatus = 1;
                    break;
                case ("FOOTBALL"):
                    eventType = EventType.FOOTBALL;
                    exceptionStatus = 1;
                    break;
                case ("OPERA"):
                    eventType = EventType.OPERA;
                    exceptionStatus = 1;
                    break;
                case ("THEATRE_PERFORMANCE"):
                    eventType = EventType.THEATRE_PERFORMANCE;
                    exceptionStatus = 1;
                    break;
                default:
                    System.out.println("Invalid eventType. Try again");
                    type = inputScanner.nextLine();
                    break;
            }
        }
        return eventType;
    }
}


