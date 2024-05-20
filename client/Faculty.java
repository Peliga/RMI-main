import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class Faculty{
	public static void main(String[] args){
		try{
			// Get the references of exported object from RMI Registry...

			//locate the registry.
			Registry registry = LocateRegistry.getRegistry("127.0.0.1", 9100);

			// Get the references of exported object from the RMI Registry...
			EnrollmentInitialInterface p1 = (EnrollmentInitialInterface) registry.lookup("access_enrollment");
			EnrollmentInterface p2 = (EnrollmentInterface) registry.lookup("access_enrollment");

			
			Scanner scanner = new Scanner(System.in);
			int userSelection = 0;
			// Start Menu
			do{
				System.out.println("\n\n ==== Menu ==== \n");
				System.out.println("Key [1] - Display All Students");
				System.out.println("Key [2] - Display All Courses");
				System.out.println("Key [3] - Enroll Student");
				System.out.println("Key [4] - Add Student");
				System.out.println("Key [0] - Exit");
				System.out.print("Type here: ");
				userSelection = scanner.nextInt();
				if(userSelection == 1){
					System.out.println(p2.displayStudents());
				}else if(userSelection == 2){
					System.out.println(p2.displayCourses());
				}else if(userSelection == 3){
					Scanner scn = new Scanner(System.in);
					Scanner scn2 = new Scanner(System.in);
					System.out.print("\n\nEnter Student ID: ");
					int studentID = scn.nextInt();
					System.out.print("Enter Course Code: ");
					int studentCourseCode = scn2.nextInt();
					int result = 0;
					result = p2.enrollCourse(studentID, studentCourseCode);
					if(result == 0){
						System.out.println("Invalid student or course");
					}else{
						System.out.println("Student has been successfully enrolled a course with a code "+studentCourseCode);
					}
				}else if(userSelection==4){
						Scanner scn = new Scanner(System.in);
					 System.out.println("Add Student");
					 System.out.print("Student ID: ");
					 int std_id = scn.nextInt();
					 scn.nextLine();
					 System.out.print("Student name: ");
					String fname = scn.nextLine();
					System.out.print("Student program: ");
					String program = scn.nextLine();

					int cnt_std = p2.addStudent(std_id,fname,program);

					if(cnt_std == 0){
						System.out.println("Error student ID already exist, try again");
					}else{
						System.out.println("New student added");
					}
				}else{
					System.out.println("\n\nInvalid key!");
				}
			}while(userSelection != 0);
			System.out.println("\n\nProgram successfully exited.");
		}catch(Exception e){
			System.out.println("Client side error..." + e);
		}
	}
}