import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;

public interface EnrollmentInterface extends Remote{
	public String displayStudents() throws RemoteException;
    public String displayCourses() throws RemoteException;
    public int enrollCourse(int studentID, int studentCourseCode) throws RemoteException, SQLException;
    public int addStudent(int std_id,String fname,String program)throws RemoteException;
}