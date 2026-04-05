package app.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    public Jeu getJeu() {
        return jeu;
    }

    public String getPlateforme() {
        return plateforme;
    }

    public int getAnneeSortie() {
        return anneeSortie;
    }

    public String getDeveloppeur() {
        return developpeur;
    }

    public double getVentesMondiales() {
        return ventesMondiales;
    }

    public int getNombreCritiquesInitial() {
        return nombreCritiquesInitial;
    }

    public double getScoreMoyenCritiquesInitial() {
        return scoreMoyenCritiquesInitial;
    }

    public int getNombreEvaluationsInitial() {
        return nombreEvaluationsInitial;
    }

    public double getScoreMoyenEvaluationsInitial() {
        return scoreMoyenEvaluationsInitial;
    }

    public void ajouterEvaluation(Evaluation evaluation) {
        if (evaluation == null) {
            throw new IllegalArgumentException("L'evaluation ne peut pas etre null");
        }
        if (evaluation.getSupportJeu() != this) {
            throw new IllegalArgumentException("L'evaluation n'est pas associee a ce support");
        }
        evaluations.add(evaluation);
    }

    public List<Evaluation> getEvaluations() {
        List<Evaluation> visibles = new ArrayList<>();
        for (Evaluation evaluation : evaluations) {
            if (!evaluation.estSupprimee()) {
                visibles.add(evaluation);
            }
        }
        return Collections.unmodifiableList(visibles);
    }

    public int getNombreEvaluationsPlateforme() {
        return getEvaluations().size();
    }

    public double getScoreMoyenEvaluationsPlateforme() {
        return getEvaluations().stream().mapToDouble(Evaluation::getNoteGlobale).average().orElse(0.0);
    }

    public int getNombreEvaluationsTotal() {
        return nombreEvaluationsInitial + getNombreEvaluationsPlateforme();
    }

    public double getScoreMoyenEvaluationsTotal() {
        int total = getNombreEvaluationsTotal();
        if (total == 0) {
            return 0.0;
        }
        double sommeInitiale = nombreEvaluationsInitial * scoreMoyenEvaluationsInitial;
        double sommePlateforme = getEvaluations().stream().mapToDouble(Evaluation::getNoteGlobale).sum();
        return (sommeInitiale + sommePlateforme) / total;
    }

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

    public TestJeu getTest() {
        return test;
    }

    public boolean aUnTest() {
        return test != null;
    }

    public int getNombreCritiquesPlateforme() {
        return test == null ? 0 : 1;
    }

    public double getScoreMoyenCritiquesPlateforme() {
        return test == null ? 0.0 : test.calculerNoteMoyenne();
    }

    public int getNombreCritiquesTotal() {
        return nombreCritiquesInitial + getNombreCritiquesPlateforme();
    }

    public double getScoreMoyenCritiquesTotal() {
        int total = getNombreCritiquesTotal();
        if (total == 0) {
            return 0.0;
        }
        double sommeInitiale = nombreCritiquesInitial * scoreMoyenCritiquesInitial;
        double sommePlateforme = test == null ? 0.0 : test.calculerNoteMoyenne();
        return (sommeInitiale + sommePlateforme) / total;
    }

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

    public int getTotalJetons() {
        return jetonsParJoueur.values().stream().mapToInt(Integer::intValue).sum();
    }

    public Map<Joueur, Integer> getJetonsParJoueur() {
        return Collections.unmodifiableMap(jetonsParJoueur);
    }

    public void remplacerJoueur(Joueur ancienJoueur, Joueur nouveauJoueur) {
        if (ancienJoueur == null || nouveauJoueur == null) {
            throw new IllegalArgumentException("Les joueurs ne peuvent pas etre null");
        }
        Integer nbJetons = jetonsParJoueur.remove(ancienJoueur);
        if (nbJetons != null) {
            jetonsParJoueur.merge(nouveauJoueur, nbJetons, Integer::sum);
        }
    }

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
