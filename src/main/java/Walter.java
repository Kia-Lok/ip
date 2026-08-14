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
                    System.out.println("Here are the tasks in your list:");
                    for (int i = 0; i < taskCount; i++) {
                        System.out.println((i + 1) + ". " + tasks[i]);
                    }
                }
            } else if (command.startsWith("mark ") || command.startsWith("done ")) {
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
            } else if (command.startsWith("todo ")) {
                Task task = new Todo(command.substring(5));
                taskCount = addTypedTask(tasks, taskCount, task);
            } else if (command.startsWith("deadline ")) {
                String taskDetails = command.substring(9);
                int delimiterIndex = taskDetails.indexOf(" /by ");
                String description = taskDetails.substring(0, delimiterIndex);
                String by = taskDetails.substring(delimiterIndex + 5);
                Task task = new Deadline(description, by);
                taskCount = addTypedTask(tasks, taskCount, task);
            } else if (command.startsWith("event ")) {
                String taskDetails = command.substring(6);
                Task task;
                int atDelimiterIndex = taskDetails.indexOf(" /at ");
                if (atDelimiterIndex >= 0) {
                    String description = taskDetails.substring(0, atDelimiterIndex);
                    String at = taskDetails.substring(atDelimiterIndex + 5);
                    task = new Event(description, at);
                } else {
                    int fromDelimiterIndex = taskDetails.indexOf(" /from ");
                    int toDelimiterIndex = taskDetails.indexOf(" /to ", fromDelimiterIndex + 7);
                    String description = taskDetails.substring(0, fromDelimiterIndex);
                    String from = taskDetails.substring(fromDelimiterIndex + 7, toDelimiterIndex);
                    String to = taskDetails.substring(toDelimiterIndex + 5);
                    task = new Event(description, from, to);
                }
                taskCount = addTypedTask(tasks, taskCount, task);
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

    /**
     * Adds a typed task and displays its representation and the updated task count.
     *
     * @param tasks Array storing all tasks.
     * @param taskCount Current number of stored tasks.
     * @param task Task to add.
     * @return Updated number of stored tasks.
     */
    private static int addTypedTask(Task[] tasks, int taskCount, Task task) {
        if (taskCount >= tasks.length) {
            System.out.println("Walter: Your task list is full.");
            return taskCount;
        }

        tasks[taskCount] = task;
        int updatedTaskCount = taskCount + 1;
        System.out.println("Walter has added this task:");
        System.out.println(task);
        String taskWord = updatedTaskCount == 1 ? "task" : "tasks";
        System.out.println("Now you have " + updatedTaskCount + " " + taskWord + " in the list.");
        return updatedTaskCount;
    }
}
