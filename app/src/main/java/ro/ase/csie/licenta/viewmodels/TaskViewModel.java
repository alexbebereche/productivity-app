package ro.ase.csie.licenta.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import ro.ase.csie.licenta.classes.entities.Task;
import ro.ase.csie.licenta.DB.dao.TaskRepository;

public class TaskViewModel extends AndroidViewModel {
    private final TaskRepository taskRepository;

    public TaskViewModel(@NonNull Application application) {
        super(application);
        taskRepository = new TaskRepository(application);
    }

    public void insert(Task task){
        taskRepository.insert(task);
    }
    public void update(Task task){
        taskRepository.update(task);
    }
    public void delete(Task task){
        taskRepository.delete(task);
    }

    public void deleteAllTasksForUser(String user){
        taskRepository.deleteAllTasksForUser(user);
    }

    public LiveData<List<Task>> getAllTasksForUser(String email){
        return taskRepository.getAllTasksForUser(email);
    }

}
