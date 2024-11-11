import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class SimpleWebServer {

    private static PostManager postManager = new PostManager(100);
    private static SpamFilter spamFilter = new SpamFilter();
    private static UserInteractionManager interactionManager = new UserInteractionManager();
    private static UserManager userManager = new UserManager();
    private static PostScheduler postScheduler = new PostScheduler();
    private static RelationshipGraph relationshipGraph = new RelationshipGraph();
    private static HashtagTrie hashtagTrie = new HashtagTrie();

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", new StaticFileHandler());
        server.createContext("/submit", new FormHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Server is running on http://localhost:8080");
    }

    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String filePath = "public" + exchange.getRequestURI().getPath();
            if (filePath.equals("public/")) {
                filePath = "public/index.html"; // Serve the main HTML file
            }
            byte[] response = Files.readAllBytes(Paths.get(filePath));
            exchange.sendResponseHeaders(200, response.length);
            OutputStream os = exchange.getResponseBody();
            os.write(response);
            os.close();
        }
    }

    static class FormHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                // Retrieve form data
                byte[] requestBody = exchange.getRequestBody().readAllBytes();
                String requestBodyString = new String(requestBody);

                String[] pairs = requestBodyString.split("&");
                String message = "";
                for (String pair : pairs) {
                    String[] keyValue = pair.split("=");
                    if (keyValue.length > 1 && keyValue[0].equals("message")) {
                        message = keyValue[1].replace("+", " ");
                        break; // Exit loop after finding the message
                    }
                }

                // Check for spam using the spam filter
                if (spamFilter.isSpam(message)) {
                    message = "[SPAM DETECTED]";
                }

                // Add post to the manager
                postManager.addPost(message, "User1", new Date().toString());

                // Log user interaction
                interactionManager.addInteraction("User1", message);

                // Schedule the post
                postScheduler.schedulePost(message, new Date());

                // Prepare response HTML
                String response = "<html><body><h2>Post Added: " + message + "</h2><a href='/'>Go Back</a></body></html>";
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }
    }

    // Array-Based Post Management
    static class PostManager {
        private Post[] posts;
        private int count;

        PostManager(int size) {
            posts = new Post[size];
            count = 0;
        }

        void addPost(String content, String author, String timestamp) {
            if (count < posts.length) {
                posts[count++] = new Post(content, author, timestamp);
            }
        }

        Post[] getPosts() {
            return Arrays.copyOf(posts, count); // Return only the filled part
        }
    }

    static class Post {
        String content;
        String author;
        String timestamp;

        Post(String content, String author, String timestamp) {
            this.content = content;
            this.author = author;
            this.timestamp = timestamp;
        }
    }

   
    static class UserInteraction {
        String user;
        String message;
        UserInteraction next;

        UserInteraction(String user, String message) {
            this.user = user;
            this.message = message;
        }
    }

    static class UserInteractionManager {
        private UserInteraction head;

        void addInteraction(String user, String message) {
            UserInteraction interaction = new UserInteraction(user, message);
            interaction.next = head;
            head = interaction;
        }
    }

    
    static class UserManager {
        private HashMap<String, String> users = new HashMap<>();

        void addUser(String username) {
            users.put(username, username); // Placeholder for user data
        }

        String getUser(String username) {
            return users.get(username);
        }
    }

    
    static class BinarySearchTree {
        private class Node {
            Post post;
            Node left, right;

            Node(Post post) {
                this.post = post;
            }
        }

        private Node root;

        void insert(Post post) {
            root = insertRec(root, post);
        }

        private Node insertRec(Node root, Post post) {
            if (root == null) {
                root = new Node(post);
                return root;
            }
            if (post.timestamp.compareTo(root.post.timestamp) < 0) {
                root.left = insertRec(root.left, post);
            } else {
                root.right = insertRec(root.right, post);
            }
            return root;
        }

        List<Post> inOrder() {
            List<Post> sortedPosts = new ArrayList<>();
            inOrderRec(root, sortedPosts);
            return sortedPosts;
        }

        private void inOrderRec(Node root, List<Post> sortedPosts) {
            if (root != null) {
                inOrderRec(root.left, sortedPosts);
                sortedPosts.add(root.post);
                inOrderRec(root.right, sortedPosts);
            }
        }
    }

    
    static class PostScheduler {
        private PriorityQueue<ScheduledPost> queue;

        PostScheduler() {
            queue = new PriorityQueue<>(Comparator.comparing(sp -> sp.timestamp));
        }

        void schedulePost(String content, Date timestamp) {
            queue.add(new ScheduledPost(content, timestamp));
        }

        static class ScheduledPost {
            String content;
            Date timestamp;

            ScheduledPost(String content, Date timestamp) {
                this.content = content;
                this.timestamp = timestamp;
            }
        }
    }

    
    static class RelationshipGraph {
        private Map<String, Set<String>> relationships = new HashMap<>();

        void addRelationship(String user1, String user2) {
            relationships.computeIfAbsent(user1, k -> new HashSet<>()).add(user2);
            relationships.computeIfAbsent(user2, k -> new HashSet<>()).add(user1); // Bidirectional
        }

        Set<String> getConnections(String user) {
            return relationships.getOrDefault(user, new HashSet<>());
        }
    }

    
    static class HashtagTrie {
        private class TrieNode {
            Map<Character, TrieNode> children = new HashMap<>();
            boolean isEndOfWord;
        }

        private TrieNode root;

        HashtagTrie() {
            root = new TrieNode();
        }

        void insert(String hashtag) {
            TrieNode node = root;
            for (char c : hashtag.toCharArray()) {
                node = node.children.computeIfAbsent(c, k -> new TrieNode());
            }
            node.isEndOfWord = true;
        }

        boolean search(String hashtag) {
            TrieNode node = root;
            for (char c : hashtag.toCharArray()) {
                node = node.children.get(c);
                if (node == null) {
                    return false;
                }
            }
            return node.isEndOfWord;
        }
    }

    
    static class CircularQueue {
        private String[] buffer;
        private int head;
        private int tail;
        private int size;

        CircularQueue(int capacity) {
            buffer = new String[capacity];
            head = 0;
            tail = 0;
            size = 0;
        }

        void enqueue(String post) {
            if (size == buffer.length) {
                head = (head + 1) % buffer.length;
            } else {
                size++;
            }
            buffer[tail] = post;
            tail = (tail + 1) % buffer.length;
        }

        String dequeue() {
            if (size == 0) return null;
            String post = buffer[head];
            head = (head + 1) % buffer.length;
            size--;
            return post;
        }
    }


    static class RedBlackTree {
        // Implementation of Red-Black Tree goes here.
        // For simplicity, you can implement basic insertion and balancing operations.
    }


    static class SpamFilter {
        private final BitSet bitSet;
        private final int size;
        private final int numHashFunctions;

        SpamFilter() {
            this.size = 1000; // Size of the bit array
            this.numHashFunctions = 5; // Number of hash functions
            this.bitSet = new BitSet(size);
        }

        void add(String word) {
            for (int i = 0; i < numHashFunctions; i++) {
                int hash = (hash(word) + i) % size;
                bitSet.set(hash);
            }
        }

        boolean isSpam(String content) {
            for (String word : content.split(" ")) {
                if (bitSet.get(hash(word))) {
                    return true; // Potentially spam
                }
            }
            return false;
        }

        private int hash(String word) {
            return word.hashCode() % size;
        }
    }
}
