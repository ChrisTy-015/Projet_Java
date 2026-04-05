package app.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represente un jeu video reference dans le catalogue.
 * <p>
 * Un jeu peut exister sur plusieurs supports et centralise
 * les regles metier de publication des evaluations et des tests.
 */
public class Jeu {
    /**
     * Temps minimal de jeu requis pour publier une evaluation.
     */
    public static final int DUREE_MINIMALE_EVALUATION_HEURES = 1;
    /**
     * Temps minimal de jeu requis pour publier un test.
     */
    public static final int DUREE_MINIMALE_TEST_HEURES = 1;

    private final String nom;
    private final String categorie;
    private final String editeur;
    private final String rating;
    private final List<SupportJeu> listeSupports;

    /**
     * Cree un jeu.
     *
     * @param nom nom du jeu
     * @param categorie categorie du jeu
     * @param editeur editeur du jeu
     * @param rating classification age
     */
    public Jeu(String nom, String categorie, String editeur, String rating) {
        this.nom = Objects.requireNonNull(nom, "Le nom ne peut pas etre null").trim();
        this.categorie = Objects.requireNonNull(categorie, "La categorie ne peut pas etre null").trim();
        this.editeur = Objects.requireNonNull(editeur, "L'editeur ne peut pas etre null").trim();
        this.rating = Objects.requireNonNull(rating, "Le rating ne peut pas etre null").trim();
        this.listeSupports = new ArrayList<>();
    }

    /**
     * Retourne le nom du jeu.
     *
     * @return nom du jeu
     */
    public String getNom() {
        return nom;
    }

    /**
     * Retourne la categorie du jeu.
     *
     * @return categorie du jeu
     */
    public String getCategorie() {
        return categorie;
    }

    /**
     * Retourne l'editeur du jeu.
     *
     * @return editeur du jeu
     */
    public String getEditeur() {
        return editeur;
    }

    /**
     * Retourne la classification age du jeu.
     *
     * @return rating du jeu
     */
    public String getRating() {
        return rating;
    }

    /**
     * Ajoute un support au jeu.
     *
     * @param support support a associer
     */
    public void ajouterSupport(SupportJeu support) {
        if (support == null) {
            throw new IllegalArgumentException("Le support de jeu ne peut pas etre null");
        }
        if (!listeSupports.contains(support)) {
            listeSupports.add(support);
        }
    }

    /**
     * Retourne la liste des supports associes au jeu.
     *
     * @return liste non modifiable des supports
     */
    public List<SupportJeu> getListeSupports() {
        return Collections.unmodifiableList(listeSupports);
    }

    /**
     * Recherche un support par nom de plateforme.
     *
     * @param plateforme nom de la plateforme
     * @return support correspondant ou {@code null}
     */
    public SupportJeu trouverSupport(String plateforme) {
        if (plateforme == null) {
            return null;
        }
        for (SupportJeu support : listeSupports) {
            if (support.getPlateforme().equalsIgnoreCase(plateforme.trim())) {
                return support;
            }
        }
        return null;
    }

    /**
     * Ajoute une evaluation a l'un des supports du jeu
     * apres verification des preconditions metier.
     *
     * @param evaluation evaluation a publier
     */
    public void ajouterEvaluation(Evaluation evaluation) {
        if (evaluation == null) {
            throw new IllegalArgumentException("L'evaluation ne peut pas etre null");
        }
        SupportJeu support = evaluation.getSupportJeu();
        verifierSupport(support);
        verifierEvaluation(evaluation.getAuteur(), support);
        verifierEvaluationUnique(evaluation.getAuteur(), support);
        support.ajouterEvaluation(evaluation);
        evaluation.getAuteur().ajouterEvaluation(evaluation);
    }

    /**
     * Ajoute un test a l'un des supports du jeu
     * apres verification des preconditions metier.
     *
     * @param test test a publier
     */
    public void ajouterTest(TestJeu test) {
        if (test == null) {
            throw new IllegalArgumentException("Le test ne peut pas etre null");
        }
        SupportJeu support = test.getSupportJeu();
        verifierSupport(support);
        verifierTest(test.getAuteur(), support);
        support.ajouterTest(test);
        test.getAuteur().ajouterTest(test);
        test.getAuteur().ajouterJetons(5);
        support.libererTousLesJetons();
    }

    private void verifierEvaluation(Joueur auteur, SupportJeu support) {
        if (!auteur.possedeJeu(support)) {
            throw new IllegalArgumentException("Le joueur doit posseder ce support pour l'evaluer");
        }
        if (auteur.getTempsDeJeu(support) < DUREE_MINIMALE_EVALUATION_HEURES) {
            throw new IllegalArgumentException(
                    "Le joueur doit avoir joue au moins " + DUREE_MINIMALE_EVALUATION_HEURES + " heure(s) pour evaluer ce support"
            );
        }
    }

    private void verifierEvaluationUnique(Joueur auteur, SupportJeu support) {
        for (Evaluation evaluation : support.getEvaluations()) {
            if (evaluation.getAuteur().getPseudo().equalsIgnoreCase(auteur.getPseudo())) {
                throw new IllegalStateException("Ce joueur a deja publie une evaluation pour ce support");
            }
        }
    }

    private void verifierTest(Testeur auteur, SupportJeu support) {
        if (!auteur.possedeJeu(support)) {
            throw new IllegalArgumentException("Le testeur doit posseder ce support pour publier un test");
        }
        if (auteur.getTempsDeJeu(support) < DUREE_MINIMALE_TEST_HEURES) {
            throw new IllegalArgumentException(
                    "Le testeur doit avoir joue au moins " + DUREE_MINIMALE_TEST_HEURES + " heure(s) pour publier un test"
            );
        }
        if (support.getTotalJetons() <= 0) {
            throw new IllegalStateException("Aucun jeton n'est place sur ce support");
        }
    }

    private void verifierSupport(SupportJeu support) {
        if (support == null || support.getJeu() != this || !listeSupports.contains(support)) {
            throw new IllegalArgumentException("Ce support n'appartient pas a ce jeu");
        }
    }

    @Override
    public String toString() {
        return "Jeu{" +
                "nom='" + nom + '\'' +
                ", categorie='" + categorie + '\'' +
                ", editeur='" + editeur + '\'' +
                ", rating='" + rating + '\'' +
                ", supports=" + listeSupports.size() +
                '}';
    }
}
