import java.util.HashSet;
import java.util.Scanner;

public class StudentIDManagement {

    public static void main(String[] args) {

        HashSet<String> studentIDs = new HashSet<>();
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("\n===== Student ID Management System =====");
            System.out.println("1. Add Student ID");
            System.out.println("2. Remove Student ID");
            System.out.println("3. Search Student ID");
            System.out.println("4. Display All Student IDs");
            System.out.println("5. Exit");
            System.out.print("Enter your choice: ");

            int choice = sc.nextInt();
            sc.nextLine(); // to avoid newline issue

            switch (choice) {

                case 1:
                    System.out.print("Enter Student ID to add: ");
                    String addID = sc.nextLine();

                    if (studentIDs.add(addID)) {
                        System.out.println("Student ID added successfully!");
                    } else {
                        System.out.println("Duplicate ID! This ID already exists.");
                    }
                    break;

                case 2:
                    System.out.print("Enter Student ID to remove: ");
                    String removeID = sc.nextLine();

                    if (studentIDs.remove(removeID)) {
                        System.out.println("Student ID removed successfully!");
                    } else {
                        System.out.println("ID not found in the system.");
                    }
                    break;

                case 3:
                    System.out.print("Enter Student ID to search: ");
                    String searchID = sc.nextLine();

                    if (studentIDs.contains(searchID)) {
                        System.out.println("Student ID exists in the system.");
                    } else {
                        System.out.println("Student ID not found.");
                    }
                    break;

                case 4:
                    System.out.println("\nAll Student IDs:");
                    if (studentIDs.isEmpty()) {
                        System.out.println("No student IDs found.");
                    } else {
                        for (String id : studentIDs) {
                            System.out.println(id);
                        }
                    }
                    break;

                case 5:
                    System.out.println("Exiting... Thank you!");
                    sc.close();
                    System.exit(0);
                    break;

                default:
                    System.out.println("Invalid choice! Please try again.");
            }
        }
    }
}
