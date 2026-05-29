package com.example.todoapp.business.taskService;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;

import com.example.todoapp.business.task.Task;
import com.example.todoapp.taskDAO.TaskDAO;


public class TaskService {

    private final TaskDAO taskDao; // objet tache qui va permettre de faire le lien entre la couche service et la couche dao

    public TaskService(TaskDAO taskDao) throws SQLException { // constructeur de la classe service qui prend en paramètre un objet TaskDao
        this.taskDao = taskDao;
    }

    public Task save(Task task) throws SQLException {  // méthode qui permet de sauvegarder une tache en utilisant la méthode save de l'objet TaskDao
        return taskDao.save(task);
    }

    public Optional<Task> findById(int id) throws SQLException { // méthode qui permet de trouver une tache par son id en utilisant la méthode findById de l'objet TaskDao
        return taskDao.findById(id);
    }

    public Collection<Task> findAll() throws SQLException { // méthode qui permet de trouver toutes les taches en utilisant la méthode findAll de l'objet TaskDao
        return taskDao.findAll();
    }

    public Collection<Task> findAll(boolean todoOnly) throws SQLException { // méthode qui permet de trouver toutes les taches, éventuellement filtrées par leur statut d'accomplissement, en utilisant la méthode findAll de l'objet TaskDao
        return taskDao.findAll(todoOnly);
    }

    public boolean update(Task task) throws SQLException {// méthode qui permet de mettre à jour une tache en utilisant la méthode update de l'objet TaskDao
        return taskDao.update(task);
    }

    public boolean deleteById(int id) throws SQLException { // méthode qui permet de supprimer une tache par son id en utilisant la méthode deleteById de l'objet TaskDao
        return taskDao.deleteById(id);
    }

    public void deleteAll() throws SQLException { // méthode qui permet de supprimer toutes les taches en utilisant la méthode deleteAll de l'objet TaskDao
        taskDao.deleteAll();
    }

    public int count() throws SQLException { // méthode qui permet de compter le nombre de taches en utilisant la méthode count de l'objet TaskDao
        return taskDao.count();
    }
}
