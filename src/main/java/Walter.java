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

        String[] tasks = new String[100];
        int taskCount = 0;
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String command = scanner.nextLine();
            System.out.println(separator);

            if (command.equals("bye")) {
                System.out.println("Walter: Bye. Hope to see you again soon!");
                System.out.println(separator);
                break;
            }

            if (command.equals("list")) {
                if (taskCount == 0) {
                    System.out.println("There are currently no items on your list.");
                } else {
                    System.out.println("Here are the current items on your list:");
                    for (int i = 0; i < taskCount; i++) {
                        System.out.println((i + 1) + ". " + tasks[i]);
                    }
                }
            } else if (taskCount < tasks.length) {
                tasks[taskCount] = command;
                taskCount++;
                System.out.println("Walter has added " + command + " to the list.");
            } else {
                System.out.println("Walter: Your task list is full.");
            }

            System.out.println(separator);
        }
    }
}
