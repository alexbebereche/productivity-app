package ro.ase.csie.licenta.classes.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;


import java.io.Serializable;

import static androidx.room.ForeignKey.CASCADE;

@Entity(tableName = "Tasks",
    foreignKeys = @ForeignKey(entity = User.class,
            parentColumns = "email",
            childColumns = "userEmail",
            onDelete = CASCADE
))
public class Task implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String name;
    private int priority;
    private int noOfPomodoros;
    private boolean hasTimer;

    // user
    private String userEmail;

    public Task(String name, int priority, boolean hasTimer, int noOfPomodoros, String userEmail) {
        this.name = name;
        this.priority = priority;
        this.hasTimer = hasTimer;
        this.noOfPomodoros = noOfPomodoros;
        this.userEmail = userEmail;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getPriority() {
        return priority;
    }

    public int getNoOfPomodoros() {
        return noOfPomodoros;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setNoOfPomodoros(int noOfPomodoros) {
        this.noOfPomodoros = noOfPomodoros;
    }

    public boolean hasTimer() {
        return hasTimer;
    }

    public void setHasTimer(boolean hasTimer) {
        this.hasTimer = hasTimer;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
}
