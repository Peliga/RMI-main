import java.rmi.Remote;
import java.rmi.RemoteException;

public interface EnrollmentInterface extends Remote{
	// Lets us define API
	public String displayStudents() throws RemoteException;
    public String displayCourses() throws RemoteException;
	public int enrollCourse(int studentID, int studentCourseCode) throws RemoteException;
	public int addStudent(int std_id,String fname,String program)throws RemoteException;

}