package ro.ase.csie.licenta;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ro.ase.csie.licenta.classes.entities.Task;

import static org.junit.Assert.*;

public class TaskTest {

    // test fixture
    static Task task;
    static String initialName;
    static int priority;
    static int noOfPomodoros;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        initialName = "Study for exam";
        priority = 1;
        noOfPomodoros = 2;
    }

    @Before
    public void setUp() throws Exception {
        task = new Task("Study for exam", 1, 2);
    }

    @Test
    public void test() {
        Task task = new Task("Task", 1, 3);
        assertEquals(task.getName(), "Task");
    }

    @Test
    public void testRangeMax(){

    }

    @Test
    public void testUpperLimit(){

    }
    
    @Test
    public void testRight(){

    }
}
