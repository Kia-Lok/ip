import java.util.Scanner;

/**
 * The entry point for the Walter chatbot.
 */
public class Walter {
    public static void main(String[] args) {
        String separator = "____________________________________________________________";
        String banner = """
                ██╗    ██╗ █████╗ ██╗  ████████╗███████╗██████╗
                ██║    ██║██╔══██╗██║  ╚══██╔══╝██╔════╝██╔══██╗
                ██║ █╗ ██║███████║██║     ██║   █████╗  ██████╔╝
                ██║███╗██║██╔══██║██║     ██║   ██╔══╝  ██╔══██╗
                ╚███╔███╔╝██║  ██║███████╗██║   ███████╗██║  ██║
                 ╚══╝╚══╝ ╚═╝  ╚═╝╚══════╝╚═╝   ╚══════╝╚═╝  ╚═╝
                """;
        System.out.println(separator);
        System.out.print(banner);
        System.out.println("Howdy! I'm Walter!");
        System.out.println("What can I do for you?");
        System.out.println(separator);

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String command = scanner.nextLine();
            System.out.println(separator);

            if (command.equals("bye")) {
                System.out.println("     Bye. Hope to see you again soon!");
                System.out.println(separator);
                break;
            }

            System.out.println("Walter: " + command);
            System.out.println(separator);
        }
    }
}
