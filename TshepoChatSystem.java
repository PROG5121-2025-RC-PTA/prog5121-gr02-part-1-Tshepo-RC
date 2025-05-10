import javax.swing.JOptionPane;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TshepoChatSystem {
    private static Map<String, Login> registeredUsers = new HashMap<>();
    private static Scanner scanner = new Scanner(System.in);
    private static List<Message> sentMessages = new ArrayList<>();
    private static List<Message> storedMessages = new ArrayList<>();
    private static int messageCounter = 0;
    private static String currentUser = null;

    public static void main(String[] args) {
        while (true) {
            if (currentUser == null) {
                showAuthMenu();
            } else {
                showChatMenu();
            }
        }
    }

    private static void showAuthMenu() {
        System.out.println("\n=== Authentication System ===");
        System.out.println("1. Register");
        System.out.println("2. Login");
        System.out.println("3. Exit");
        System.out.print("Select an option: ");
        
        int choice = getIntInput();
        switch (choice) {
            case 1:
                registerUser();
                break;
            case 2:
                loginUser();
                break;
            case 3:
                System.out.println("Exiting system. Goodbye!");
                System.exit(0);
            default:
                System.out.println("Invalid option. Please try again.");
        }
    }

    private static void showChatMenu() {
        System.out.println("\n=== Welcome to QuickChat ===");
        System.out.println("1. Send Messages");
        System.out.println("2. Show recently sent messages");
        System.out.println("3. Quit");
        System.out.print("Select an option: ");
        
        int choice = getIntInput();
        switch (choice) {
            case 1:
                sendMessages();
                break;
            case 2:
                System.out.println("Coming Soon.");
                break;
            case 3:
                currentUser = null;
                System.out.println("Logged out successfully.");
                break;
            default:
                System.out.println("Invalid option. Please try again.");
        }
    }

    private static void registerUser() {
        System.out.println("\n--- User Registration ---");
        
        String username = getValidUsername();
        String password = getValidPassword();
        String phone = getValidPhoneNumber();
        
        Login newUser = new Login(username, password, phone);
        String registrationStatus = newUser.registerUser();
        System.out.println(registrationStatus);
        
        if (registrationStatus.equals("User registered successfully.")) {
            registeredUsers.put(username, newUser);
        }
    }

    private static void loginUser() {
        System.out.println("\n--- User Login ---");
        
        if (registeredUsers.isEmpty()) {
            System.out.println("No users registered. Please register first.");
            return;
        }
        
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        
        Login user = registeredUsers.get(username);
        if (user == null) {
            System.out.println("Username not found. Please register or try again.");
            return;
        }
        
        boolean loginSuccess = user.loginUser(username, password);
        System.out.println(user.returnLoginStatus(loginSuccess));
        
        if (loginSuccess) {
            currentUser = username;
            System.out.println("Your registered phone number: " + user.getPhoneNumber());
        }
    }

    private static void sendMessages() {
        System.out.println("\n--- Send Messages ---");
        System.out.print("How many messages would you like to send? ");
        int numMessages = getIntInput();
        
        for (int i = 0; i < numMessages; i++) {
            System.out.println("\nMessage " + (i + 1) + " of " + numMessages);
            Message message = createMessage();
            
            String action = message.sentMessage();
            switch (action.toLowerCase()) {
                case "send":
                    sentMessages.add(message);
                    messageCounter++;
                    showMessageDetails(message);
                    break;
                case "store":
                    storedMessages.add(message);
                    System.out.println("Message successfully stored.");
                    break;
                case "disregard":
                    System.out.println("Message disregarded.");
                    break;
            }
        }
        
        System.out.println("\nTotal messages sent: " + Message.returnTotalMessages(sentMessages));
    }

    private static Message createMessage() {
        String messageId = generateMessageId();
        System.out.print("Enter recipient's phone number (e.g., +27718693002): ");
        String recipient = scanner.nextLine();
        
        System.out.print("Enter your message (max 250 chars): ");
        String text = scanner.nextLine();
        
        while (text.length() > 250) {
            System.out.println("Message exceeds 250 characters by " + (text.length() - 250) + 
                              ", please reduce size.");
            System.out.print("Enter your message (max 250 chars): ");
            text = scanner.nextLine();
        }
        
        return new Message(messageId, recipient, text);
    }

    private static void showMessageDetails(Message message) {
        String details = "Message Details:\n" +
                         "Message ID: " + message.getMessageId() + "\n" +
                         "Message Hash: " + message.createMessageHash() + "\n" +
                         "Recipient: " + message.getRecipient() + "\n" +
                         "Message: " + message.getText();
        
        JOptionPane.showMessageDialog(null, details, "Message Sent", JOptionPane.INFORMATION_MESSAGE);
        System.out.println(details);
    }

    private static String generateMessageId() {
        Random rand = new Random();
        return String.format("%010d", rand.nextInt(1000000000));
    }

    private static String getValidUsername() {
        String username;
        while (true) {
            System.out.print("Enter username (must contain '_' and be ≤5 chars): ");
            username = scanner.nextLine().trim();
            
            if (username.isEmpty()) {
                System.out.println("Username cannot be empty.");
                continue;
            }
            
            if (registeredUsers.containsKey(username)) {
                System.out.println("Username already exists. Please choose another.");
                continue;
            }
            
            if (!username.contains("_")) {
                System.out.println("Username must contain an underscore (_).");
            } else if (username.length() > 5) {
                System.out.println("Username must be 5 characters or less.");
            } else {
                break;
            }
        }
        return username;
    }

    private static String getValidPassword() {
        String password;
        while (true) {
            System.out.print("Enter password: ");
            password = scanner.nextLine();
            
            if (password.length() < 8) {
                System.out.println("Password must be at least 8 characters.");
            } else if (!Pattern.compile("[A-Z]").matcher(password).find()) {
                System.out.println("Password must contain at least one capital letter.");
            } else if (!Pattern.compile("[0-9]").matcher(password).find()) {
                System.out.println("Password must contain at least one number.");
            } else if (!Pattern.compile("[^a-zA-Z0-9]").matcher(password).find()) {
                System.out.println("Password must contain at least one special character.");
            } else {
                break;
            }
        }
        return password;
    }

    private static String getValidPhoneNumber() {
        String phone;
        while (true) {
            System.out.print("Enter South African cell number (+27xxxxxxxxx): ");
            phone = scanner.nextLine().replaceAll("[\\s-]", "");
            
            if (!phone.startsWith("+27")) {
                System.out.println("Number must start with +27.");
            } else if (phone.length() != 12) {
                System.out.println("Number must be 12 characters including +27 (e.g., +27123456789).");
            } else if (!phone.substring(1).matches("\\d+")) {
                System.out.println("Number must contain only digits after +27.");
            } else {
                break;
            }
        }
        return phone;
    }

    private static int getIntInput() {
        while (true) {
            try {
                int input = Integer.parseInt(scanner.nextLine());
                return input;
            } catch (NumberFormatException e) {
                System.out.print("Invalid input. Please enter a number: ");
            }
        }
    }

    static class Login {
        private final String username;
        private final String password;
        private final String phoneNumber;
        private String registrationStatus;
        private String loginStatus;

        public Login(String username, String password, String phoneNumber) {
            this.username = username;
            this.password = password;
            this.phoneNumber = phoneNumber;
        }

        public boolean checkUserName() {
            return username.contains("_") && username.length() <= 5;
        }

        public boolean checkPasswordComplexity() {
            boolean hasCapital = password.matches(".*[A-Z].*");
            boolean hasNumber = password.matches(".*\\d.*");
            boolean hasSpecialChar = password.matches(".*[^a-zA-Z0-9].*");
            return password.length() >= 8 && hasCapital && hasNumber && hasSpecialChar;
        }

        public boolean checkCellPhoneNumber() {
            return phoneNumber.startsWith("+27") && 
                   phoneNumber.length() == 12 && 
                   phoneNumber.substring(1).matches("\\d+");
        }

        public String registerUser() {
            if (!checkUserName()) {
                registrationStatus = "Username is not correctly formatted.";
                return registrationStatus;
            }
            if (!checkPasswordComplexity()) {
                registrationStatus = "Password does not meet complexity requirements.";
                return registrationStatus;
            }
            if (!checkCellPhoneNumber()) {
                registrationStatus = "Cellphone number is not correctly formatted.";
                return registrationStatus;
            }
            registrationStatus = "User registered successfully.";
            return registrationStatus;
        }

        public boolean loginUser(String enteredUsername, String enteredPassword) {
            return this.username.equals(enteredUsername) && 
                   this.password.equals(enteredPassword);
        }

        public String returnLoginStatus(boolean loginSuccess) {
            loginStatus = loginSuccess ? 
                "Welcome " + username + ", it is great to see you again." :
                "Username or password incorrect, please try again.";
            return loginStatus;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }
    }

    static class Message {
        private String messageId;
        private String recipient;
        private String text;
        private String status;

        public Message(String messageId, String recipient, String text) {
            this.messageId = messageId;
            this.recipient = recipient;
            this.text = text;
        }

        public boolean checkMessageID() {
            return messageId != null && messageId.length() <= 10;
        }

        public int checkRecipientCell() {
            if (recipient == null || recipient.length() != 12 || !recipient.startsWith("+27")) {
                return -1;
            }
            try {
                Long.parseLong(recipient.substring(1));
                return 0;
            } catch (NumberFormatException e) {
                return -1;
            }
        }

        public String createMessageHash() {
            String[] words = text.split("\\s+");
            String firstWord = words.length > 0 ? words[0] : "";
            String lastWord = words.length > 1 ? words[words.length-1] : firstWord;
            
            return (messageId.substring(0, 2) + ":" + firstWord.toUpperCase() + 
                   lastWord.toUpperCase()).replaceAll("\\s+", "");
        }

        public String sentMessage() {
            System.out.println("\nWhat would you like to do with this message?");
            System.out.println("1. Send Message");
            System.out.println("2. Store Message to send later");
            System.out.println("3. Disregard Message");
            System.out.print("Select an option: ");
            
            int choice = new Scanner(System.in).nextInt();
            switch (choice) {
                case 1:
                    status = "sent";
                    storeMessage();
                    return "send";
                case 2:
                    status = "stored";
                    storeMessage();
                    return "store";
                case 3:
                    status = "disregarded";
                    return "disregard";
                default:
                    System.out.println("Invalid option. Message will be disregarded.");
                    return "disregard";
            }
        }

        public void storeMessage() {
            JSONObject messageJson = new JSONObject();
            messageJson.put("messageId", messageId);
            messageJson.put("recipient", recipient);
            messageJson.put("text", text);
            messageJson.put("status", status);
            messageJson.put("hash", createMessageHash());
            
            try {
                JSONArray messagesArray;
                if (Files.exists(Paths.get("messages.json"))) {
                    String content = new String(Files.readAllBytes(Paths.get("messages.json")));
                    messagesArray = new JSONArray(content);
                } else {
                    messagesArray = new JSONArray();
                }
                
                messagesArray.put(messageJson);
                
                try (FileWriter file = new FileWriter("messages.json")) {
                    file.write(messagesArray.toString());
                }
            } catch (IOException e) {
                System.out.println("Error storing message: " + e.getMessage());
            }
        }

        public static String printMessages(List<Message> messages) {
            StringBuilder sb = new StringBuilder();
            for (Message msg : messages) {
                sb.append("ID: ").append(msg.getMessageId())
                  .append(" | To: ").append(msg.getRecipient())
                  .append(" | ").append(msg.getText().substring(0, Math.min(20, msg.getText().length())))
                  .append(msg.getText().length() > 20 ? "..." : "")
                  .append("\n");
            }
            return sb.toString();
        }

        public static int returnTotalMessages(List<Message> messages) {
            return messages.size();
        }

        public String getMessageId() { return messageId; }
        public String getRecipient() { return recipient; }
        public String getText() { return text; }
    }
}