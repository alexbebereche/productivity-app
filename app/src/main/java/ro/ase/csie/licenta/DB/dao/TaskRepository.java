package ro.ase.csie.licenta.DB.dao;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

import ro.ase.csie.licenta.classes.entities.Task;

public class TaskRepository {
    private TaskDao taskDao;

    public TaskRepository(Application application){
        DatabaseManager database = DatabaseManager.getInstance(application);
        taskDao = database.taskDao();
    }

    public void insert(Task task){
        new InsertTaskAsyncTask(taskDao).execute(task);
    }

    public void update(Task task){
        new UpdateTaskAsyncTask(taskDao).execute(task);
    }

    public void delete(Task task){
        new DeleteTaskAsyncTask(taskDao).execute(task);
    }

    public void deleteAllTasksForUser(String user){
        new DeleteAllTaskForUserAsyncTask(taskDao, user).execute();
    }

    //room authomatically executes the db operations that return the LiveData on the backgrond thread
    //added
    public LiveData<List<Task>> getAllTasksForUser(String userEmail){
        return taskDao.getAllTasksForUser(userEmail);
    }

    private static class InsertTaskAsyncTask extends AsyncTask<Task, Void, Void>{
        private TaskDao taskDao;

        private InsertTaskAsyncTask(TaskDao taskDao){
            this.taskDao = taskDao;
        }

        @Override
        protected Void doInBackground(Task... tasks) {
            taskDao.insert(tasks[0]);
            return null;
        }
    }

    private static class UpdateTaskAsyncTask extends AsyncTask<Task, Void, Void>{
        private TaskDao taskDao;

        private UpdateTaskAsyncTask(TaskDao taskDao){
            this.taskDao = taskDao;
        }

        @Override
        protected Void doInBackground(Task... tasks) {
            taskDao.update(tasks[0]);
            return null;
        }
    }

    private static class DeleteTaskAsyncTask extends AsyncTask<Task, Void, Void>{
        private TaskDao taskDao;

        private DeleteTaskAsyncTask(TaskDao taskDao){
            this.taskDao = taskDao;
        }

        @Override
        protected Void doInBackground(Task... tasks) {
            taskDao.delete(tasks[0]);
            return null;
        }
    }

    private static class DeleteAllTaskForUserAsyncTask extends AsyncTask<String, Void, Void>{
        private TaskDao taskDao;
        private String user;

        private DeleteAllTaskForUserAsyncTask(TaskDao taskDao, String user){
            this.taskDao = taskDao;
            this.user = user;
        }

        @Override
        protected Void doInBackground(String... strings) {
            taskDao.deleteAllTasksForUser(user);
            return null;
        }
    }

}
