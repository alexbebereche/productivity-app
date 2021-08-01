package ro.ase.csie.licenta.DB.dao;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import ro.ase.csie.licenta.classes.entities.Task;
import ro.ase.csie.licenta.classes.entities.User;

@Database(entities = {Task.class, User.class}, version = 3)
public abstract class DatabaseManager extends RoomDatabase {
    public static final String DATABASE_NAME = "FinalProject.db";

    private static DatabaseManager INSTANCE;

    public abstract TaskDao taskDao();
    public abstract UserDao userDao();

    public static synchronized DatabaseManager getInstance(Context context){
        if(INSTANCE == null){
            INSTANCE = Room
                    .databaseBuilder(context.getApplicationContext(), DatabaseManager.class,
                            DATABASE_NAME)
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return INSTANCE;
    }
}
