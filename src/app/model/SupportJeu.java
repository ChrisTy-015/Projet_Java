package app.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represente un support concret d'un jeu pour une plateforme donnee.
 * <p>
 * Le support agrege les donnees initiales importees du catalogue,
 * les evaluations publiees sur la plateforme, le test eventuel
 * et les jetons places par les joueurs.
 */
public class SupportJeu {
    private final Jeu jeu;
    private final String plateforme;
    private final int anneeSortie;
    private final String developpeur;
    private final double ventesMondiales;
    private final int nombreCritiquesInitial;
    private final double scoreMoyenCritiquesInitial;
    private final int nombreEvaluationsInitial;
    private final double scoreMoyenEvaluationsInitial;
    private final List<Evaluation> evaluations;
    private final Map<Joueur, Integer> jetonsParJoueur;
    private TestJeu test;

    /**
     * Cree un support de jeu.
     *
     * @param jeu jeu auquel le support appartient
     * @param plateforme nom de la plateforme cible
     * @param anneeSortie annee de sortie
     * @param developpeur developpeur du support
     * @param ventesMondiales ventes mondiales initiales
     * @param nombreCritiques nombre de critiques importees
     * @param scoreMoyenCritiques score moyen des critiques importees
     * @param nombreEvaluations nombre d'evaluations importees
     * @param scoreMoyenEvaluations score moyen des evaluations importees
     */
    public SupportJeu(
            Jeu jeu,
            String plateforme,
            int anneeSortie,
            String developpeur,
            double ventesMondiales,
            int nombreCritiques,
            double scoreMoyenCritiques,
            int nombreEvaluations,
            double scoreMoyenEvaluations
    ) {
        this.jeu = Objects.requireNonNull(jeu, "Le jeu ne peut pas etre null");
        this.plateforme = Objects.requireNonNull(plateforme, "La plateforme ne peut pas etre null").trim();
        this.anneeSortie = anneeSortie;
        this.developpeur = Objects.requireNonNull(developpeur, "Le developpeur ne peut pas etre null").trim();
        this.ventesMondiales = ventesMondiales;
        this.nombreCritiquesInitial = nombreCritiques;
        this.scoreMoyenCritiquesInitial = scoreMoyenCritiques;
        this.nombreEvaluationsInitial = nombreEvaluations;
        this.scoreMoyenEvaluationsInitial = scoreMoyenEvaluations;
        this.evaluations = new ArrayList<>();
        this.jetonsParJoueur = new LinkedHashMap<>();
        this.test = null;
    }

    /**
     * Retourne le jeu parent de ce support.
     *
     * @return jeu associe
     */
    public Jeu getJeu() {
        return jeu;
    }

    /**
     * Retourne le nom de la plateforme.
     *
     * @return nom de la plateforme
     */
    public String getPlateforme() {
        return plateforme;
    }

    /**
     * Retourne l'annee de sortie.
     *
     * @return annee de sortie
     */
    public int getAnneeSortie() {
        return anneeSortie;
    }

    /**
     * Retourne le developpeur du support.
     *
     * @return nom du developpeur
     */
    public String getDeveloppeur() {
        return developpeur;
    }

    /**
     * Retourne les ventes mondiales initiales du support.
     *
     * @return ventes mondiales
     */
    public double getVentesMondiales() {
        return ventesMondiales;
    }

    /**
     * Retourne le nombre de critiques importees depuis le catalogue.
     *
     * @return nombre de critiques initial
     */
    public int getNombreCritiquesInitial() {
        return nombreCritiquesInitial;
    }

    /**
     * Retourne le score moyen des critiques importees.
     *
     * @return score moyen initial des critiques
     */
    public double getScoreMoyenCritiquesInitial() {
        return scoreMoyenCritiquesInitial;
    }

    /**
     * Retourne le nombre d'evaluations importees depuis le catalogue.
     *
     * @return nombre d'evaluations initial
     */
    public int getNombreEvaluationsInitial() {
        return nombreEvaluationsInitial;
    }

    /**
     * Retourne le score moyen des evaluations importees.
     *
     * @return score moyen initial des evaluations
     */
    public double getScoreMoyenEvaluationsInitial() {
        return scoreMoyenEvaluationsInitial;
    }

    /**
     * Ajoute une evaluation au support.
     *
     * @param evaluation evaluation a associer
     */
    public void ajouterEvaluation(Evaluation evaluation) {
        if (evaluation == null) {
            throw new IllegalArgumentException("L'evaluation ne peut pas etre null");
        }
        if (evaluation.getSupportJeu() != this) {
            throw new IllegalArgumentException("L'evaluation n'est pas associee a ce support");
        }
        evaluations.add(evaluation);
    }

    /**
     * Retourne les evaluations visibles du support.
     *
     * @return liste non modifiable des evaluations non supprimees
     */
    public List<Evaluation> getEvaluations() {
        List<Evaluation> visibles = new ArrayList<>();
        for (Evaluation evaluation : evaluations) {
            if (!evaluation.estSupprimee()) {
                visibles.add(evaluation);
            }
        }
        return Collections.unmodifiableList(visibles);
    }

    /**
     * Retourne le nombre d'evaluations publiees sur la plateforme.
     *
     * @return nombre d'evaluations visibles
     */
    public int getNombreEvaluationsPlateforme() {
        return getEvaluations().size();
    }

    /**
     * Retourne la note moyenne des evaluations publiees sur la plateforme.
     *
     * @return note moyenne locale
     */
    public double getScoreMoyenEvaluationsPlateforme() {
        return getEvaluations().stream().mapToDouble(Evaluation::getNoteGlobale).average().orElse(0.0);
    }

    /**
     * Retourne le nombre total d'evaluations, importees et locales.
     *
     * @return nombre total d'evaluations
     */
    public int getNombreEvaluationsTotal() {
        return nombreEvaluationsInitial + getNombreEvaluationsPlateforme();
    }

    /**
     * Retourne la note moyenne totale du support.
     *
     * @return note moyenne globale
     */
    public double getScoreMoyenEvaluationsTotal() {
        int total = getNombreEvaluationsTotal();
        if (total == 0) {
            return 0.0;
        }
        double sommeInitiale = nombreEvaluationsInitial * scoreMoyenEvaluationsInitial;
        double sommePlateforme = getEvaluations().stream().mapToDouble(Evaluation::getNoteGlobale).sum();
        return (sommeInitiale + sommePlateforme) / total;
    }

    /**
     * Associe un test au support.
     *
     * @param test test a enregistrer
     */
    public void ajouterTest(TestJeu test) {
        if (test == null) {
            throw new IllegalArgumentException("Le test ne peut pas etre null");
        }
        if (test.getSupportJeu() != this) {
            throw new IllegalArgumentException("Le test n'est pas associe a ce support");
        }
        if (this.test != null) {
            throw new IllegalStateException("Un test existe deja pour ce support");
        }
        this.test = test;
    }

    /**
     * Retourne le test associe au support, s'il existe.
     *
     * @return test du support ou {@code null}
     */
    public TestJeu getTest() {
        return test;
    }

    /**
     * Indique si un test a deja ete publie pour ce support.
     *
     * @return {@code true} si un test existe
     */
    public boolean aUnTest() {
        return test != null;
    }

    /**
     * Retourne le nombre de critiques issues de la plateforme.
     *
     * @return 1 si un test existe, sinon 0
     */
    public int getNombreCritiquesPlateforme() {
        return test == null ? 0 : 1;
    }

    /**
     * Retourne le score moyen des critiques issues de la plateforme.
     *
     * @return score moyen local des critiques
     */
    public double getScoreMoyenCritiquesPlateforme() {
        return test == null ? 0.0 : test.calculerNoteMoyenne();
    }

    /**
     * Retourne le nombre total de critiques, importees et locales.
     *
     * @return nombre total de critiques
     */
    public int getNombreCritiquesTotal() {
        return nombreCritiquesInitial + getNombreCritiquesPlateforme();
    }

    /**
     * Retourne le score moyen total des critiques.
     *
     * @return score moyen global des critiques
     */
    public double getScoreMoyenCritiquesTotal() {
        int total = getNombreCritiquesTotal();
        if (total == 0) {
            return 0.0;
        }
        double sommeInitiale = nombreCritiquesInitial * scoreMoyenCritiquesInitial;
        double sommePlateforme = test == null ? 0.0 : test.calculerNoteMoyenne();
        return (sommeInitiale + sommePlateforme) / total;
    }

    /**
     * Place des jetons d'un joueur sur le support.
     *
     * @param joueur joueur qui place les jetons
     * @param quantite quantite placee
     */
    public void placerJetons(Joueur joueur, int quantite) {
        if (joueur == null) {
            throw new IllegalArgumentException("Le joueur ne peut pas etre null");
        }
        if (quantite <= 0) {
            throw new IllegalArgumentException("La quantite doit etre strictement positive");
        }
        if (aUnTest()) {
            throw new IllegalStateException("Impossible de placer des jetons sur un jeu deja teste");
        }
        joueur.retirerJetons(quantite);
        jetonsParJoueur.merge(joueur, quantite, Integer::sum);
    }

    /**
     * Retire des jetons prealablement places par un joueur.
     *
     * @param joueur joueur qui retire les jetons
     * @param quantite quantite retiree
     */
    public void retirerJetons(Joueur joueur, int quantite) {
        if (joueur == null) {
            throw new IllegalArgumentException("Le joueur ne peut pas etre null");
        }
        if (quantite <= 0) {
            throw new IllegalArgumentException("La quantite doit etre strictement positive");
        }
        int dejaPlaces = jetonsParJoueur.getOrDefault(joueur, 0);
        if (quantite > dejaPlaces) {
            throw new IllegalArgumentException("Pas assez de jetons places sur ce jeu");
        }
        if (quantite == dejaPlaces) {
            jetonsParJoueur.remove(joueur);
        } else {
            jetonsParJoueur.put(joueur, dejaPlaces - quantite);
        }
        joueur.ajouterJetons(quantite);
    }

    /**
     * Retourne le nombre total de jetons demandes sur le support.
     *
     * @return total des jetons places
     */
    public int getTotalJetons() {
        return jetonsParJoueur.values().stream().mapToInt(Integer::intValue).sum();
    }

    /**
     * Retourne la repartition des jetons par joueur.
     *
     * @return map non modifiable des jetons par joueur
     */
    public Map<Joueur, Integer> getJetonsParJoueur() {
        return Collections.unmodifiableMap(jetonsParJoueur);
    }

    /**
     * Remplace une reference de joueur par une autre dans les jetons places.
     *
     * @param ancienJoueur joueur a remplacer
     * @param nouveauJoueur nouveau joueur a associer
     */
    public void remplacerJoueur(Joueur ancienJoueur, Joueur nouveauJoueur) {
        if (ancienJoueur == null || nouveauJoueur == null) {
            throw new IllegalArgumentException("Les joueurs ne peuvent pas etre null");
        }
        Integer nbJetons = jetonsParJoueur.remove(ancienJoueur);
        if (nbJetons != null) {
            jetonsParJoueur.merge(nouveauJoueur, nbJetons, Integer::sum);
        }
    }

    /**
     * Restitue a chaque joueur tous les jetons qu'il avait places sur ce support.
     */
    public void libererTousLesJetons() {
        for (Map.Entry<Joueur, Integer> entree : jetonsParJoueur.entrySet()) {
            entree.getKey().ajouterJetons(entree.getValue());
        }
        jetonsParJoueur.clear();
    }

    @Override
    public String toString() {
        return "SupportJeu{" +
                "jeu='" + jeu.getNom() + '\'' +
                ", plateforme='" + plateforme + '\'' +
                ", anneeSortie=" + anneeSortie +
                ", developpeur='" + developpeur + '\'' +
                ", ventesMondiales=" + ventesMondiales +
                ", jetons=" + getTotalJetons() +
                '}';
    }
}
