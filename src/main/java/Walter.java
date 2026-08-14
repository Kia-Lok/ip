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

        Task[] tasks = new Task[100];
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
            } else if (command.startsWith("mark ")) {
                int taskNumber = Integer.parseInt(command.substring(5));
                Task task = tasks[taskNumber - 1];
                task.markAsDone();
                System.out.println("Walter has marked this task as done:");
                System.out.println(task);
            } else if (command.startsWith("unmark ")) {
                int taskNumber = Integer.parseInt(command.substring(7));
                Task task = tasks[taskNumber - 1];
                task.markAsNotDone();
                System.out.println("Walter has marked this task as not done yet:");
                System.out.println(task);
            } else if (taskCount < tasks.length) {
                tasks[taskCount] = new Task(command);
                taskCount++;
                System.out.println("Walter has added " + command + " to the list.");
            } else {
                System.out.println("Walter: Your task list is full.");
            }

            System.out.println(separator);
        }
    }
}
