package ro.ase.csie.licenta.DB.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import ro.ase.csie.licenta.classes.entities.Task;

@Dao
public interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Task task);

    @Update
    void update(Task task);

    @Delete
    void delete(Task task);

    @Query("DELETE FROM Tasks WHERE userEmail=:userEmail")
    void deleteAllTasksForUser(String userEmail);

    @Query("SELECT * FROM Tasks WHERE userEmail=:userEmail ORDER BY priority")
    LiveData<List<Task>> getAllTasksForUser(String userEmail);
}
