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
        System.out.println("What are you looking for today?");
        System.out.println(separator);
        System.out.println("See you back here soon!");
        System.out.println(separator);
    }
}
