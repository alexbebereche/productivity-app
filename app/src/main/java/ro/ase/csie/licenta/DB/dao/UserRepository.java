package ro.ase.csie.licenta.DB.dao;

import android.app.Application;
import android.os.AsyncTask;

import ro.ase.csie.licenta.classes.entities.User;

public class UserRepository {
    private UserDao userDao;

    public UserRepository(Application application){
        DatabaseManager database = DatabaseManager.getInstance(application);

        userDao = database.userDao();
    }

    public void insert(User user){
        new UserRepository.InsertUserAsyncTask(userDao).execute(user);
    }

    public void delete(User user){
        new UserRepository.DeleteUserAsyncTask(userDao).execute(user);
    }

    private static class InsertUserAsyncTask extends AsyncTask<User, Void, Void> {
        private UserDao userDao;

        private InsertUserAsyncTask(UserDao userDao){
            this.userDao = userDao;
        }

        @Override
        protected Void doInBackground(User... users) {
            userDao.insert(users[0]);
            return null;
        }
    }

    private static class DeleteUserAsyncTask extends AsyncTask<User, Void, Void>{
        private UserDao userDao;

        private DeleteUserAsyncTask(UserDao userDao){
            this.userDao = userDao;
        }

        @Override
        protected Void doInBackground(User... users) {
            userDao.delete(users[0]);
            return null;
        }
    }


}
