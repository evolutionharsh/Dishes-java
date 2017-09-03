

import java.util.NoSuchElementException;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.HashSet;
import java.util.Stack;
import java.util.Iterator;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;

/**
 * Program to read a CSV file with a target price followed by
 * several menu item prices, and create a list of menu item 
 * combinations that exactly total that target price.
 * 
 * Each line of output will be a comma-separated list of item
 * names.  If multiple of an item should be included, then the
 * name is followed by "(n)", where "n" the quantity of that
 * item.
 * 
 * @author Harsh Pathania
 * @version 1.0.0
 * @since 1.0.0
 */
class Dishes {
	/**
	 * Class to represent a dish.  Since we only use this inside
	 * this small program and likely won't be expanded, we leave
	 * these fields public for now.
	 */
  private static class Dish {
    public int name;
    public int price;
    /**
     * Simple constructor to initialize values.  Kept simple,
     * since this is a private class.
     * 
     * @param name  simple name/description of the menu item
     * @param price  price of the menu item
     */
    public Dish(int name, int price) {
      this.name = name;
      this.price = price;
    }
  }
  
  /**
   * Helper class to do CSV processing.  Normally we would use
   * a library to do this, but since we are trying to contain
   * this program into a single file, without using maven
   * dependencies or anything like that, we will just create
   * this to do the processing.
   * 
   * It's interface is much like that of the Scanner class.
   */
  private static class CsvScanner {
    private InputStream in;

    /**
     * Creates a new instance which reads from an input stream
     * such as System.in, or a FileInputStream.
     */
    public CsvScanner(InputStream in) {
      this.in = in;
    }

    /**
     * Whether or not there is another field to be read from
     * the csv input (i.e. whether we've reached the end of
     * stream or not).
     * 
     * @return <code>true</code> if there are any more fields
     *         to be read;
     *         <code>false</code> otherwise.
     */
    public boolean hasNext() {
      try {
        return in.available() != 0;
      } catch (IOException e) {
        e.printStackTrace();
        return false;
      }
    }

    /**
     * Returns the next value in the CSV stream.  This accounts
     * for both newlines and commas.  It also accounts for fields
     * that have quotes around them, potentially with escaped
     * double quotes (i.e. "Dwayne ""the rock"" Johnson").
     * 
     * @return a string representing the unescaped and unquoted
     *         value in the next field.
     */
    public String next() {
      StringBuilder sb = new StringBuilder();
      try {
        char c = (char)in.read();
        if (c == '"') {
          while (true) {
            if (in.available() == 0) return null;
            c = (char)in.read();
            if (c == '"') {
              if (in.available() == 0) return sb.toString();
              c = (char)in.read();
              if (c == ',') return sb.toString();
              if (c == '\n') return sb.toString();
              if (c == '\r') {
                in.read();
                return sb.toString();
              }
              else if (c == '"'); // do nothing
              else {
                System.err.println("Bad CSV. Found a quote that wasn't escaped in a quoted string, while reading " + sb.toString());
                System.exit(1);
                return null;
              }
            }
            sb.append(c);
          }
        } else {
          while (in.available() != 0 && c != '\n' && c != '\r' && c != ',') {
            sb.append(c);
            c = (char)in.read();
          }
          if (c == '\r') in.read();
          return sb.toString();
        }
      } catch (IOException e) {
        System.err.println("IO exception while reading csv.");
        e.printStackTrace();
        System.exit(1);
        return null;
      }
    }
  }

  /**
   * This allows us to treat names as integers for improved speed
   * when hashing, while allowing us to easily get the string
   * representations later.
   */
  private static ArrayList<String> names = new ArrayList<String>();
  
  /**
   * Place for all solutions to go before they are printed out.
   * The hashset allows us to weed out duplicate solutions easily.
   */
  // TODO: rename to something more meaningful like solutions
  private static HashSet<String> output = new HashSet<String>();
  
  
  /**
   * Holds list of dishes (HashSet<Dish>) that can reach a particular
   * price (the Integer) from some lower price that has already
   * been reached.
   */
  // TODO: rename to something more meaningful like pathsToPrice
  private static TreeMap<Integer,HashSet<Dish>> m = new TreeMap<Integer, HashSet<Dish>>(); /* create collections which uses hashtable for storage of dishes */

  /**
   * Add all combinations to the solutions array.
   * 
   * @param rem  Target price at which to start finding solutions
   */
  // TODO: rename method to read better, like findCombinations
  // TODO: rename parameter to be more meaningful.  E.g. "target"
  private static void addCombos(int rem) {
    TreeMap<Integer, Integer> path = new TreeMap<Integer, Integer>(); /* Declaration of Treemap of integers */
    Stack<Iterator<Dish>> its = new Stack<Iterator<Dish>>();
    Stack<Dish> dishes = new Stack<Dish>(); /*stack of dishes */
    its.push(m.get(rem).iterator());
    /*Until and unless stack "its" is not empty loop continuous in while  */
    while (!its.empty()) {
      if (rem == 0) {
        StringBuilder sb = new StringBuilder();
        for (Integer name : path.keySet()) sb.append(names.get(name) + (path.get(name) > 1 ?"(" + path.get(name) + ")" : "") + ",");
        output.add(sb.toString().substring(0, sb.length()-1));
      }
      if (its.peek().hasNext()) {
        Dish x = its.peek().next();
        path.put(x.name, (path.containsKey(x.name) ? path.get(x.name) : 0)+1);
        rem -= x.price;
        dishes.push(x);
        its.push(m.get(rem).iterator());
      } else {
        if (!dishes.empty()) {
          rem += dishes.peek().price;
          if (path.get(dishes.peek().name) == null) System.out.println("The whole thing.");
          if (path.get(dishes.peek().name) == 1) {
            path.remove(dishes.peek().name);
          } else {
            path.put(dishes.peek().name, path.get(dishes.peek().name)-1);
          }
          dishes.pop();
        }
        its.pop();
      }
    }
  }
  /**
   * Main method
   */
  public static void main(String[] args) {
    int t;
    Dish d;
    CsvScanner in;

    // Prepare to read values
    if (args.length > 0) {
      try {
        in = new CsvScanner(new FileInputStream(args[0]));
      } catch (FileNotFoundException e) {
        System.err.println("File " + args[0] + " was not found, so not doing anything.");
        System.exit(6);
        return;
      }
    } else {
      in = new CsvScanner(System.in);
    }

    // Try reading target value
    try {
      if (!"Target Price".equals(in.next())) {
        System.err.println("First entry was not 'Target Price'.  Dieing.");
        System.exit(1);
      }
      t = (int)(Double.valueOf(in.next().substring(1))*100);
    } catch (NoSuchElementException e) {
      System.err.println("The input did not contain enough data.");
      System.exit(2);
      return;
    } catch (NumberFormatException e) {
      System.err.println("Target price was malformed: " + e.getMessage());
      System.exit(3);
      return;
    }

    // Add menu items to dp table
    m.put(0, new HashSet<Dish>());
    while (in.hasNext()) {
      try {
        names.add(in.next());
        d = new Dish(names.size()-1, (int)(Double.valueOf(in.next().substring(1))*100));
        ArrayList<Integer> keys = new ArrayList<Integer>();
        for (int k : m.keySet()) keys.add(k);
        for (Integer k : keys) {
          for (int i = k+d.price; i <= t; i += d.price) {
            if (!m.containsKey(i)) m.put(i, new HashSet<Dish>());
            m.get(i).add(d);
          }
        }
      } catch (NumberFormatException e) {
        System.err.println("Couldn't process one of the dishes prices: " + e.getMessage());
        System.exit(4);
      }
    }

    // Generate output
    if (m.containsKey(t)) {
      Dishes.addCombos(t);
      for (String s : output) {
        System.out.println(s);
      }
    } else {
      System.out.println("No combinations of dishes could match the target price.");
      System.exit(5);
    }
  }
}
