import java.util.*;

interface Observer {
    void update(Graph graph);
}

class GraphObserver implements Observer {
    @Override
    public void update(Graph graph) {
        Map<String, Node> nodes = graph.getNodes();
        List<WeightedEdge> edges = graph.getEdges();

        System.out.print("Количество узлов: " + nodes.size() + " (");
        boolean isFirst = true;
        for (String nodeName : nodes.keySet()) {
            if (!isFirst) {
                System.out.print(", ");
            }
            System.out.print(nodeName);
            isFirst = false;
        }
        System.out.println(")");

        System.out.print("Количество ребер: " + edges.size() + " (");
        isFirst = true;
        for (WeightedEdge edge : edges) {
            if (!isFirst) {
                System.out.print(", ");
            }
            System.out.print(edge.getSource().getName() + " -> " + edge.getDestination().getName());
            isFirst = false;
        }
        System.out.println(")");
    }
}

interface Command {
    boolean handle(String input, Graph graph, Scanner scanner);
}

class AddNodeCommand implements Command {
    @Override
    public boolean handle(String input, Graph graph, Scanner scanner) {
        if (input.equals("add_node")) {
            System.out.print("Введите имя узла: ");
            String nodeName = scanner.nextLine();
            System.out.print("Введите описание узла: ");
            String nodeDescription = scanner.nextLine();
            graph.addNode(new NodeImpl(nodeName, nodeDescription));
            return true;
        }
        return false;
    }
}

class AddEdgeCommand implements Command {
    @Override
    public boolean handle(String input, Graph graph, Scanner scanner) {
        if (input.equals("add_edge")) {
            System.out.print("Введите имя узла источника: ");
            String sourceName = scanner.nextLine();
            System.out.print("Введите имя узла приемника: ");
            String destinationName = scanner.nextLine();
            System.out.print("Введите описание отношения: ");
            String relationship = scanner.nextLine();
            Node source = graph.getNode(sourceName);
            Node destination = graph.getNode(destinationName);
            if (source != null && destination != null) {
                graph.addEdge(new EdgeImpl(source, destination, relationship));
            } else {
                System.out.println("Один или оба узла не найдены");
            }
            return true;
        }
        return false;
    }
}

class AddWeightCommand implements Command {
    @Override
    public boolean handle(String input, Graph graph, Scanner scanner) {
        if (input.equals("add_weight")) {
            System.out.print("Введите имя узла источника: ");
            String sourceName = scanner.nextLine();
            System.out.print("Введите имя узла приемника: ");
            String destinationName = scanner.nextLine();
            System.out.print("Введите вес ребра: ");
            int weight = scanner.nextInt();
            scanner.nextLine(); // Очистка буфера

            Node source = graph.getNode(sourceName);
            Node destination = graph.getNode(destinationName);
            if (source != null && destination != null) {
                for (WeightedEdge edge : graph.getEdges()) {
                    if (edge.getSource() == source && edge.getDestination() == destination) {
                        edge.setWeight(weight);
                        System.out.println("Вес ребра установлен.");
                        return true;
                    }
                }
                System.out.println("Ребро не найдено.");
            } else {
                System.out.println("Один или оба узла не найдены");
            }
        }
        return false;
    }
}

class InfoCommand implements Command {
    @Override
    public boolean handle(String input, Graph graph, Scanner scanner) {
        if (input.equals("info")) {
            System.out.print("Введите имя узла для просмотра описания: ");
            String nodeNameInfo = scanner.nextLine();
            Node node = graph.getNode(nodeNameInfo);
            if (node != null) {
                System.out.println("Описание узла '" + node.getName() + "': " + node.getDescription());
            } else {
                System.out.println("Узел с именем '" + nodeNameInfo + "' не найден.");
            }
            return true;
        }
        return false;
    }
}

class PrintCommand implements Command {
    @Override
    public boolean handle(String input, Graph graph, Scanner scanner) {
        if (input.equals("print")) {
            graph.printAdjacencyMatrix();
            graph.printIncidenceMatrix();
            return true;
        }
        return false;
    }
}

class ExitCommand implements Command {
    @Override
    public boolean handle(String input, Graph graph, Scanner scanner) {
        if (input.equals("exit")) {
            scanner.close();
            System.exit(0);
            return true;
        }
        return false;
    }
}

interface Node {
    String getName();
    String getDescription();
}

class NodeImpl implements Node {
    private String name;
    private String description;

    NodeImpl(String name, String description) {
        this.name = name;
        this.description = description;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }
}

interface Edge {
    Node getSource();
    Node getDestination();
    String getRelationship();
}

class EdgeImpl implements Edge {
    private Node source;
    private Node destination;
    private String relationship;

    EdgeImpl(Node source, Node destination, String relationship) {
        this.source = source;
        this.destination = destination;
        this.relationship = relationship;
    }

    @Override
    public Node getSource() {
        return source;
    }

    @Override
    public Node getDestination() {
        return destination;
    }

    @Override
    public String getRelationship() {
        return relationship;
    }
}

// Новый интерфейс для взвешенных ребер
interface WeightedEdge extends Edge {
    int getWeight();
    void setWeight(int weight);
}

// Декоратор для взвешенных ребер
class WeightedEdgeDecorator implements WeightedEdge {
    private Edge edge;
    private int weight;

    WeightedEdgeDecorator(Edge edge) {
        this.edge = edge;
        this.weight = 1; // Установим начальный вес равным 1
    }

    @Override
    public Node getSource() {
        return edge.getSource();
    }

    @Override
    public Node getDestination() {
        return edge.getDestination();
    }

    @Override
    public String getRelationship() {
        return edge.getRelationship();
    }

    @Override
    public int getWeight() {
        return weight;
    }

    @Override
    public void setWeight(int weight) {
        this.weight = weight;
    }
}

class Graph {
    private static Graph instance;
    private Map<String, Node> nodes;
    private List<WeightedEdge> edges;
    private List<Observer> observers = new ArrayList<>();
    Map<String, Node> getNodes() {
        return nodes;
    }

    private Graph() {
        nodes = new HashMap<>();
        edges = new ArrayList<>();
    }

    public static Graph getInstance() {
        if (instance == null) {
            instance = new Graph();
        }
        return instance;
    }

    Node getNode(String nodeName) {
        return nodes.get(nodeName);
    }

    void addNode(Node node) {
        nodes.put(node.getName(), node);
        notifyObservers();
    }

    void addEdge(Edge edge) {
        edges.add(new WeightedEdgeDecorator(edge));
        notifyObservers();
    }

    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    private void notifyObservers() {
        for (Observer observer : observers) {
            observer.update(this);
        }
    }

    public int getNodeCount() {
        return nodes.size();
    }

    public int getEdgeCount() {
        return edges.size();
    }

    List<WeightedEdge> getEdges() {
        return edges;
    }

    void printAdjacencyMatrix() {
        int n = nodes.size();
        String[] nodeNames = new String[n];
        int[] nodeIndices = new int[n];
        int index = 0;
        for (Map.Entry<String, Node> entry : nodes.entrySet()) {
            nodeNames[index] = entry.getKey();
            nodeIndices[index] = index;
            index++;
        }

        System.out.println("Матрица смежности:");
        System.out.print("    ");
        for (String name : nodeNames) {
            System.out.printf("%-4s", name);
        }
        System.out.println();

        for (int i = 0; i < n; i++) {
            System.out.printf("%-4s", nodeNames[i]);
            for (int j = 0; j < n; j++) {
                boolean hasEdge = false;
                int weight = 0;
                for (WeightedEdge edge : edges) {
                    if (edge.getSource() == nodes.get(nodeNames[i]) && edge.getDestination() == nodes.get(nodeNames[j])) {
                        hasEdge = true;
                        weight = edge.getWeight();
                        break;
                    }
                }
                System.out.printf("%-4s", hasEdge ? weight : "0");
            }
            System.out.println();
        }
    }

    void printIncidenceMatrix() {
        int n = nodes.size();
        int m = edges.size();
        String[] nodeNames = new String[n];
        int[] nodeIndices = new int[n];
        int index = 0;
        for (Map.Entry<String, Node> entry : nodes.entrySet()) {
            nodeNames[index] = entry.getKey();
            nodeIndices[index] = index;
            index++;
        }

        System.out.println("\nМатрица инцидентности:");
        System.out.print("    ");
        for (int i = 0; i < m; i++) {
            System.out.printf("%-4d", i + 1);
        }
        System.out.println();

        for (int i = 0; i < n; i++) {
            System.out.printf("%-4s", nodeNames[i]);
            for (int j = 0; j < m; j++) {
                WeightedEdge edge = edges.get(j);
                int value = 0;
                if (edge.getSource() == nodes.get(nodeNames[i])) {
                    value = edge.getWeight();
                } else if (edge.getDestination() == nodes.get(nodeNames[i])) {
                    value = -edge.getWeight();
                }
                System.out.printf("%-4d", value);
            }
            System.out.println();
        }
    }
}

public class GraphConsoleApp {
    private static List<Command> commands = Arrays.asList(
            new AddNodeCommand(),
            new AddEdgeCommand(),
            new AddWeightCommand(),
            new InfoCommand(),
            new PrintCommand(),
            new ExitCommand()
    );

    public static void main(String[] args) {
        Graph graph = Graph.getInstance();
        Scanner scanner = new Scanner(System.in);

        GraphObserver observer = new GraphObserver();
        graph.addObserver(observer);

        while (true) {
            System.out.print("Введите команду (add_node, add_edge, add_weight, info, print, exit): ");
            String input = scanner.nextLine();

            boolean handled = false;
            for (Command command : commands) {
                if (command.handle(input, graph, scanner)) {
                    handled = true;
                    break;
                }
            }
            if (!handled) {
                System.out.println("Неизвестная команда");
            }
        }
    }
} 