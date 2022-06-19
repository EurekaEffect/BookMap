package eureka.bid;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Main {
    public static List<Storage> updates = new ArrayList<>();
    public static List<String> output = new ArrayList<>();

    public static Storage storage = new Storage();

    public static void main(String[] args) throws Exception {
        BufferedReader bf = new BufferedReader(new FileReader("input.txt"));
        BufferedWriter bw = new BufferedWriter(new FileWriter("output.txt"));

        long startTime = System.currentTimeMillis();

        List<String> input = bf.lines().toList();
        for (String line : input) {
            String[] data = line.replace(" ", "").split(",");

            switch (getTask(data[0])) {
                case Update -> {
                    int price = Integer.parseInt(data[1]);
                    int size = Integer.parseInt(data[2]);
                    Action action = getAction(data[3]);

                    updates.add(new Storage(price, size, action));
                }
                case Query -> {
                    int price = -1;
                    try {
                        price = Integer.parseInt(data[2]);
                    } catch (Exception e) {
                        System.out.println("[Query] catched number exception, using -1 instead of specified price.");
                    }

                    switch (data[1]) {
                        case "best_bid" -> output.add(storage.bestBid(updates).price + "," + storage.bestBid(updates).size);
                        //bw.write(storage.bestBid(updates).price + "," + storage.bestBid(updates).size + "\n");
                        case "best_ask" -> output.add(storage.bestAsk(updates).price + "," + storage.bestAsk(updates).size);
                        //bw.write(storage.bestAsk(updates).price + "," + storage.bestAsk(updates).size + "\n");
                        case "size" -> output.add(String.valueOf(storage.getSize(updates, price)));
                        // bw.write(storage.getSize(updates, price) + "\n");
                        default -> System.out.println("unknown query.");
                    }
                }
                case Order -> {
                    int size = 0;
                    try {
                        size = Integer.parseInt(data[2]);
                    } catch (Exception e) {
                        System.out.println("[Order] catched number exception, using 0 instead of specified price.");
                    }

                    switch (getAct(data[1])) {
                        case Buy -> storage.bestAsk(updates).size -= size;
                        case Sell -> storage.bestBid(updates).size -= size;
                    }
                }
            }
        }

        for (int i = 0; i < output.size(); i++) {
            StringBuffer sb = new StringBuffer();

            String out = i == output.size() - 1 ? sb.append(output.get(i)).toString() : sb.append(output.get(i)).append("\n").toString();
            bw.write(out);
        }

        bw.close();
        System.out.println("Program ended in " + (System.currentTimeMillis() - startTime));
    }

    private static Task getTask(String letter) {
        return switch (letter) {
            case "u" -> Task.Update;
            case "q" -> Task.Query;
            case "o" -> Task.Order;
            default -> {
                try {
                    throw new Exception();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    private static Action getAction(String name) {
        name = name.toLowerCase();

        return switch (name) {
            case "bid" -> Action.Bid;
            case "spread" -> Action.Spread;
            case "ask" -> Action.Ask;
            default -> {
                try {
                    throw new Exception();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    private static Act getAct(String name) {
        name = name.toLowerCase();

        return switch (name) {
            case "buy" -> Act.Buy;
            case "sell" -> Act.Sell;
            default -> {
                try {
                    throw new Exception();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    public enum Task {
        Update, Query, Order
    }

    public enum Action {
        Bid, Spread, Ask
    }

    public enum Act {
        Buy, Sell
    }

    public static Main instance;

    public Main() {
        instance = this;
    }

    public static class Storage {
        private String string;
        private int price, size;
        private Action action;
        private Act act;

        public Storage() {
        }

        public Storage(Act act, int size) {
            this.act = act;
            this.size = size;
        }

        public Storage(String string, int price) {
            this.string = string;
            this.price = price;
        }

        public Storage(int price, int size, Action action) {
            this.price = price;
            this.size = size;
            this.action = action;
        }

        public int getSize(List<Storage> storages, int price) {
            for (Storage storage : storages) {
                if (storage.price == price) return storage.size;
            }

            return -1;
        }

        public Storage bestBid(List<Storage> storages) {
            Comparator<Storage> comparator = Comparator.comparingInt(this::getPrice);
            return storages.stream().filter(storage -> storage.action == Action.Bid).max(comparator).get();
        }

        public Storage bestAsk(List<Storage> storages) {
            Comparator<Storage> comparator = Comparator.comparingInt(this::getPrice);
            return storages.stream().filter(storage -> storage.action == Action.Ask).max(comparator).get();
        }

        private int getPrice(Storage storage) {
            return storage.size == 0 ? storage.size : storage.price;
        }
    }
}
