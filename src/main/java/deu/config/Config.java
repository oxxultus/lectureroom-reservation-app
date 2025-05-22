package deu.config;

public class Config {
    public Server server;
    public User user;

    public static class Server {
        public String host;
        public int port;
    }

    public static class User {
        public String id;
        public String password;
    }
}