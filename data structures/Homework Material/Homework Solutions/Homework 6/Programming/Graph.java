import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class Graph {

  // Keep a fast index to nodes in the map
  private Map<Integer, Vertex> vertexNames;

  /**
   * Construct an empty Graph with a map. The map's key is the name of a vertex
   * and the map's value is the vertex object.
   */
  public Graph() {
    vertexNames = new HashMap<>();
  }

  /**
   * Adds a vertex to the graph. Throws IllegalArgumentException if two vertices
   * with the same name are added.
   * 
   * @param v
   *          (Vertex) vertex to be added to the graph
   */
  public void addVertex(Vertex v) {
    if (vertexNames.containsKey(v.name))
      throw new IllegalArgumentException("Cannot create new vertex with existing name.");
    vertexNames.put(v.name, v);
  }

  /**
   * Gets a collection of all the vertices in the graph
   * 
   * @return (Collection<Vertex>) collection of all the vertices in the graph
   */
  public Collection<Vertex> getVertices() {
    return vertexNames.values();
  }

  /**
   * Gets the vertex object with the given name
   * 
   * @param name
   *          (String) name of the vertex object requested
   * @return (Vertex) vertex object associated with the name
   */
  public Vertex getVertex(String name) {
    return vertexNames.get(name);
  }

  /**
   * Adds a directed edge from vertex u to vertex v
   * 
   * @param nameU
   *          (String) name of vertex u
   * @param nameV
   *          (String) name of vertex v
   * @param cost
   *          (double) cost of the edge between vertex u and v
   */
  public void addEdge(int nameU, int nameV, Double cost) {
    if (!vertexNames.containsKey(nameU))
      throw new IllegalArgumentException(nameU + " does not exist. Cannot create edge.");
    if (!vertexNames.containsKey(nameV))
      throw new IllegalArgumentException(nameV + " does not exist. Cannot create edge.");
    Vertex sourceVertex = vertexNames.get(nameU);
    Vertex targetVertex = vertexNames.get(nameV);
    Edge newEdge = new Edge(sourceVertex, targetVertex, cost);
    sourceVertex.addEdge(newEdge);
  }

  /**
   * Adds an undirected edge between vertex u and vertex v by adding a directed
   * edge from u to v, then a directed edge from v to u
   * 
   * @param name
   *          (String) name of vertex u
   * @param name2
   *          (String) name of vertex v
   * @param cost
   *          (double) cost of the edge between vertex u and v
   */
  public void addUndirectedEdge(int name, int name2, double cost) {
    addEdge(name, name2, cost);
    addEdge(name2, name, cost);
  }

  /**
   * Computes the euclidean distance between two points as described by their
   * coordinates
   * 
   * @param ux
   *          (double) x coordinate of point u
   * @param uy
   *          (double) y coordinate of point u
   * @param vx
   *          (double) x coordinate of point v
   * @param vy
   *          (double) y coordinate of point v
   * @return (double) distance between the two points
   */
  public double computeEuclideanDistance(double ux, double uy, double vx, double vy) {
    return Math.sqrt(Math.pow(ux - vx, 2) + Math.pow(uy - vy, 2));
  }

  /**
   * Computes euclidean distance between two vertices as described by their
   * coordinates
   * 
   * @param u
   *          (Vertex) vertex u
   * @param v
   *          (Vertex) vertex v
   * @return (double) distance between two vertices
   */
  public double computeEuclideanDistance(Vertex u, Vertex v) {
    return computeEuclideanDistance(u.x, u.y, v.x, v.y);
  }

  /**
   * Calculates the euclidean distance for all edges in the map using the
   * computeEuclideanCost method.
   */
  public void computeAllEuclideanDistances() {
    for (Vertex u : getVertices())
      for (Edge uv : u.adjacentEdges) {
        Vertex v = uv.target;
        uv.distance = computeEuclideanDistance(u.x, u.y, v.x, v.y);
      }
  }

  // STUDENT CODE STARTS HERE

  public void generateRandomVertices(int n) {
    vertexNames = new HashMap<>();
    Random random = new Random();
    for (int i = 0; i < n; i++) {
      if (i > 0) {
        List<Vertex> candidates = new LinkedList<>();
        for (int j = 0; j < 1000; j++) {
          candidates.add(new Vertex(i, random.nextInt(100), random.nextInt(100)));
        }
        Vertex bestCandidate = Collections.max(candidates, new Comparator<Vertex>() {
          @Override
          public int compare(Vertex o1, Vertex o2) {
            double o1MinDistance = computeEuclideanDistance(
                Collections.min(getVertices(), Comparator.comparing(v -> computeEuclideanDistance(v, o1))), o1);
            double o2MinDistance = computeEuclideanDistance(
                Collections.min(getVertices(), Comparator.comparing(v -> computeEuclideanDistance(v, o2))), o2);
            return Double.compare(o1MinDistance, o2MinDistance);
          }
        });

        vertexNames.put(i, bestCandidate);
      } else {
        vertexNames.put(i, new Vertex(i, random.nextInt(100), random.nextInt(100)));
      }
    }

    for (Vertex u : getVertices()) {
      for (Vertex v : getVertices()) {
        if (u != v) {
          addUndirectedEdge(u.name, v.name, 0);
        }
      }
    }

    computeAllEuclideanDistances();
  }

  public List<Edge> nearestNeighborTsp() {
    for (Vertex vertex : getVertices()) {
      vertex.known = false;
    }

    Vertex start = vertexNames.get(0);
    start.known = true;
    Vertex current = start;

    while (true) {
      List<Edge> unknownEdges = current.adjacentEdges.stream().filter(x -> !x.target.known)
          .collect(Collectors.toList());
      if (unknownEdges.isEmpty()) {
        start.prev = current;
        break;
      }
      Edge shortestEdge = Collections.min(unknownEdges, Comparator.comparing(x -> x.distance));
      shortestEdge.target.prev = shortestEdge.source;
      current = shortestEdge.target;
      current.known = true;
    }

    List<Edge> edges = new LinkedList<>();

    start = vertexNames.get(0);
    current = start.prev;
    edges.add(new Edge(start, current, computeEuclideanDistance(start, current)));

    while (current != start) {
      edges.add(new Edge(current, current.prev, computeEuclideanDistance(current, current.prev)));
      current = current.prev;
    }

    return edges;
  }

  private void permutation(ArrayList<Integer> array, int start, List<ArrayList<Integer>> permutations) {
    if (start == array.size()) {
      ArrayList<Integer> arrayCopy = new ArrayList<>(array);
      arrayCopy.add(0, 0);
      arrayCopy.add(arrayCopy.size(), 0);
      permutations.add(arrayCopy);
    }
    for (int i = start; i < array.size(); i++) {
      int temp = array.get(start);
      array.set(start, array.get(i));
      array.set(i, temp);
      permutation(array, start + 1, permutations);
      temp = array.get(start);
      array.set(start, array.get(i));
      array.set(i, temp);
    }
  }

  private double permutationToDistance(ArrayList<Integer> permutation) {
    double dist = 0;
    for (int i = 1; i < permutation.size(); i++) {
      dist += computeEuclideanDistance(vertexNames.get(permutation.get(i - 1)), vertexNames.get(permutation.get(i)));
    }
    return dist;
  }

  public List<Edge> bruteForceTsp() {
    ArrayList<Integer> array = new ArrayList<>();
    for (int i = 1; i < vertexNames.size(); i++) {
      array.add(i);
    }
    List<ArrayList<Integer>> permutations = new LinkedList<ArrayList<Integer>>();
    permutation(array, 0, permutations);

    ArrayList<Integer> minPermutation = Collections.min(permutations,
        Comparator.comparing(x -> permutationToDistance(x)));

    List<Edge> shortestPath = new LinkedList<Edge>();
    for (int i = 1; i < minPermutation.size(); i++) {
      Vertex u = vertexNames.get(minPermutation.get(i - 1));
      Vertex v = vertexNames.get(minPermutation.get(i));
      shortestPath.add(new Edge(u, v, computeEuclideanDistance(u, v)));
    }

    return shortestPath;
  }

  // STUDENT CODE ENDS HERE

  /**
   * Prints out the adjacency list of the graph for debugging
   */
  public void printAdjacencyList() {
    for (int u : vertexNames.keySet()) {
      StringBuilder sb = new StringBuilder();
      sb.append(u);
      sb.append(" -> [ ");
      for (Edge e : vertexNames.get(u).adjacentEdges) {
        sb.append(e.target.name);
        sb.append("(");
        sb.append(e.distance);
        sb.append(") ");
      }
      sb.append("]");
      System.out.println(sb.toString());
    }
  }
}
