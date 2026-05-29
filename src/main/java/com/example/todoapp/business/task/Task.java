package com.example.todoapp.business.task;
/**
 * Information de l'objet tache (permet d'afficher les parametres quand on écrit l'appel de l'objet tache dans le code)
 * @param id            id tache
 * @param title         titre de la tache
 * @param description   description de la tache
 * @param done          status de la tache
 */
public record Task(Integer id, String title, String description, boolean done) {

    public Task(String title, String description, boolean done) { // constructeur de la classe Task qui prend en paramètre le titre, la description et le status de la tache.
        this(null, title, description, done);
    }

    public Task(String title, String description) {
        this(null, title, description, false); // constructeur de la classe Task qui prend en paramètre le titre et la description de la tache sans que la tache est été specifiée
    }
}
