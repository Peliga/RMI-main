import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.xml.crypto.dsig.Transform;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class Enrollment implements EnrollmentInterface, EnrollmentInitialInterface {
    private ArrayList<Student> students = new ArrayList<>();
    private ArrayList<Course> courses = new ArrayList<>();
    
    // Database connection details
    
    private static final String DB_URL = "jdbc:postgresql://localhost:12345/Student";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "admin";
    // Initialize database connection
    private Connection connection;

    public Enrollment(){
        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    // Initialize Student 
    public void initializeStudents()throws RemoteException {
        try {
            truncateTable("students"); 

            File xmlFile = new File("Xml files/Student.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document document = dBuilder.parse(xmlFile);
            document.getDocumentElement().normalize();

            NodeList studentList = document.getElementsByTagName("student");

            for (int i = 0; i < studentList.getLength(); i++) {
                Element studentElement = (Element) studentList.item(i);

                int id = Integer.parseInt(studentElement.getElementsByTagName("id").item(0).getTextContent());
                String name = studentElement.getElementsByTagName("name").item(0).getTextContent();
                String program = studentElement.getElementsByTagName("program").item(0).getTextContent();
                
                students.add(new Student(id, name, program));

                // Insert student data into the database
                insertStudentIntoDatabase(id, name, program);
            }
        } catch (SQLException | ParserConfigurationException | SAXException | IOException  e) {
            e.printStackTrace();
        }
    }

    // Add student to the xml
    public int addStudent(int std_id,String fname,String program)throws RemoteException{    
        int hasId = 0;
        try{
            for(int i =0;i< students.size();i++){
                if(std_id == students.get(i).getId()){
                        hasId ++;
                }
            }

            if(hasId > 0){
                System.out.println("Error Unknown client try to add already exist student ");
                return 0;
            }else{
                    students.add(new Student(std_id,fname,program));

                    insertStudentIntoDatabase(std_id, fname, program);
                    insertStudentXML(std_id, fname, program);  
                }
            }catch (SQLException   e) {
                e.printStackTrace();
                }
                System.out.println("Unknown client added new student");
                return 1;
    }

    // Insert Student Record to the XML
    private void insertStudentXML(int std_id,String fname, String stdPorgram){
        File studentXML = new File("Xml files/Student.xml");

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        
       try{
            DocumentBuilder builder = dbFactory.newDocumentBuilder();
            Document document = builder.parse(studentXML);

            document.getDocumentElement().normalize();

            // my root element in my xml file
            Node root = document.getDocumentElement();

            // this is my new student element created
            Element student = document.createElement("student");

            String str_id = String.valueOf(std_id);

            // create new element id
            Element student_id = document.createElement("id");
            student_id.appendChild(document.createTextNode(str_id));
            student.appendChild(student_id);

            //create element name
            Element name = document.createElement("name");
            name.appendChild(document.createTextNode(fname));
            student.appendChild(name);

            // create program element

            Element programELem = document.createElement("program");
            programELem.appendChild(document.createTextNode(stdPorgram));
            student.appendChild(programELem);

            // append new student NOde to Root NOde
            root.appendChild(student);
            removeWhitespaceNodes(document);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();

            Transformer transformer = transformerFactory.newTransformer();

            transformer.setOutputProperty(OutputKeys.INDENT,"yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount","2"); //xalan

            DOMSource domSource = new DOMSource(document);

            StreamResult stream = new StreamResult(studentXML);

            transformer.transform(domSource,stream);
        
       }catch(ParserConfigurationException | SAXException | IOException | TransformerException e){
        e.printStackTrace();
       }
    }
   
    // Initialize Course 
    int cnt =0 ;
    public void initializeCourses() {
        try {
            truncateTable("courses");

            File xmlFile = new File("Xml files/Course.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document document = dBuilder.parse(xmlFile);
            document.getDocumentElement().normalize();

            NodeList courseList = document.getElementsByTagName("course");

            for (int i = 0; i < courseList.getLength(); i++) {
                Element courseElement = (Element) courseList.item(i);

                int courseId = Integer.parseInt(courseElement.getElementsByTagName("course-id").item(0).getTextContent());
                String name = courseElement.getElementsByTagName("title").item(0).getTextContent();
                String description = courseElement.getElementsByTagName("description").item(0).getTextContent();
                courses.add(new Course(courseId, name, description));
                 
                insertCourseIntoDatabase(courseId, name, description);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Insert Student to the databse  student table
    private void insertStudentIntoDatabase(int id, String name, String program) throws SQLException {
        String query = "INSERT INTO students (id, name, program) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);
            statement.setString(2, name);
            statement.setString(3, program);
            statement.executeUpdate();
        }
    }

    // Insert Course to the Database course table
    private void insertCourseIntoDatabase(int courseId, String name, String description) throws SQLException {
        String query = "INSERT INTO courses (id, name, description) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, courseId);
            statement.setString(2, name);
            statement.setString(3, description);
            statement.executeUpdate();
        }
    }

    // Truncate Database table 
    private void truncateTable(String tableName) throws SQLException {
        String query = "TRUNCATE TABLE " + tableName + " CASCADE";
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(query);
        }
    }

    // Display All student
    public String displayStudents() throws RemoteException {
        String allStudents = "\n\nAll Students:\n";
        for (int i = 0; i < students.size(); i++) {
            if (students.size() != 0) {
                allStudents += "\nID: " + students.get(i).getId() + "\n";
                allStudents += "Full Name: " + students.get(i).getFullName() + "\n";
                allStudents += "Program: " + students.get(i).getProgram() + "\n";
                if (students.get(i).getCourseEnrolled().isEmpty()) {
                    allStudents += "Courses Enrolled: No courses enrolled yet.\n---------------";
                } else {
                    allStudents += "Courses Enrolled: " + students.get(i).getCourseEnrolled() + "\n---------------";
                }
            }
        }
        System.out.println("A request from unknown client has been processed: Displaying all students in the client...");
        return allStudents;
    }

    // Display All courses
    public String displayCourses() throws RemoteException {
        String allCourses = "\n\nAll Courses:\n";
        for (int i = 0; i < courses.size(); i++) {
            if (courses.size() != 0) {
                allCourses += "\nCourse Code: " + courses.get(i).getCCode() + "\n";
                allCourses += "Name: " + courses.get(i).getCName() + "\n";
                allCourses += "Description: " + courses.get(i).getCDescription() + "\n---------------";
            }
        }
        System.out.println("A request from unknown client has been processed: Displaying all courses in the client...");
        return allCourses;
    }

    // Enroll Student Course
    public int enrollCourse(int studentID, int studentCourseCode) throws SQLException, RemoteException {
        int haveStudID = 0;
        int haveCourse = 0;
        for (int i = 0; i < students.size(); i++) {
            if (studentID == students.get(i).getId()){
                haveStudID = 1;
            }
        }
        for (int i = 0; i < courses.size(); i++) {
            if (studentCourseCode==courses.get(i).getCCode()) {
                haveCourse = 1;
            }
        }
        if (haveStudID == 0 || haveCourse == 0) {
            System.out.println("A Client attempted to Enroll student. Error found to be an invalid student or course...");
            return 0;
        }
        for (int i = 0; i < students.size(); i++) {
            if (studentID == students.get(i).getId()) {

                students.get(i).setCourse(studentCourseCode);
                try {
                    insertEnrolledStudent(studentID, studentCourseCode);
                    insertEnrollStudentXML(studentID,studentCourseCode);
                } catch (SAXException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (TransformerException e) {
                    e.printStackTrace();
                }
               
            }
        }
        System.out.println("A Client successfully enrolled one student...");
        return 1;
    }

    // Insert Enrolled Student  to the Enrolled XML FILE
    private void  insertEnrollStudentXML(int std_id, int course_id) throws SAXException, IOException, TransformerException{
        File enrollXML = new File("Xml files/Enrolled.xml");
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document document = dBuilder.parse(enrollXML);
            
            document.getDocumentElement().normalize();

            Node root = document.getDocumentElement();

            Element enroll_student = document.createElement("enrolled");

            String str_id = String.valueOf(std_id);
            Element id = document.createElement("student_id");
            id.appendChild(document.createTextNode(str_id));
            enroll_student.appendChild(id);

            String str_course = String.valueOf(course_id);
            Element enroll_course = document.createElement("course");
            enroll_course.appendChild(document.createTextNode(str_course));
            enroll_student.appendChild(enroll_course);

            root.appendChild(enroll_student);

            removeWhitespaceNodes(document);

            TransformerFactory  transformerFactory = TransformerFactory.newInstance();

            Transformer transformer = transformerFactory.newTransformer();

            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");


            DOMSource domSource = new DOMSource(document);

            StreamResult streamResult = new StreamResult(enrollXML);

            transformer.transform(domSource,streamResult);

            System.out.println("New enrolled student recorded");

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

    
    }

    // Insert Enrolled student tp the database
    private void insertEnrolledStudent(int student_id,int course_id){
        String query = "INSERT INTO enrolled_students (student_id, course_id) VALUES (?, ?)";
                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.setInt(1, student_id);
                    statement.setInt(2, course_id);
                    statement.executeUpdate();
                }catch (Exception e) {
                    System.out.println("Error" + e);
                }
    }

    
    // initialize enroll student 

    public void initializeEnrolledStudent() throws RemoteException {
        try {
            // Step 1: Truncate the table
            truncateTable("enrolled_students");
            System.out.println("Table truncated successfully.");

            // Step 2: Parse the XML file
            File enrolledXML = new File("Enrolled.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = dbFactory.newDocumentBuilder();
            Document document = builder.parse(enrolledXML);

            document.getDocumentElement().normalize();
            System.out.println("XML document normalized.");

            NodeList enrolledList = document.getElementsByTagName("enrolled");
            System.out.println("NOde list is not giving");

            // Iterate through each enrolled student
            for (int i = 0; i < enrolledList.getLength(); i++) {
                Element student = (Element) enrolledList.item(i);

                int std_id = Integer.parseInt(student.getElementsByTagName("student_id").item(0).getTextContent());
                int course_id = Integer.parseInt(student.getElementsByTagName("course").item(0).getTextContent());


                // check its student and initialize course id if found
                boolean studentFound = false;
                for (int j = 0; j < students.size(); j++) {
                    if (std_id == students.get(j).getId()) {

                        students.get(j).setCourse(course_id);
                        insertEnrolledStudent(std_id, course_id);
                        studentFound = true;
                        break; 
                    }
                }

                if (!studentFound) {
                    System.err.println("Student ID " + std_id + " not found in the list.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // Remove Spaces in XML FILE
    private static void removeWhitespaceNodes(Node node) {
        Node child = node.getFirstChild();
        while (child != null) {
            Node next = child.getNextSibling();

            if(child.getNodeType() == Node.TEXT_NODE && child.getNodeValue().trim().isEmpty()){
                node.removeChild(child);
            }else if(child.getNodeType() == Node.ELEMENT_NODE){
                removeWhitespaceNodes(child);
            };
            child = next;
        }
       
    }
    
}
