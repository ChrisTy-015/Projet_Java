package app.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Joueur extends Membre {
    public static final int JETONS_INITIAUX = 3;

    private final List<SupportJeu> supportsPossedes;
    private final Map<SupportJeu, Integer> tempsParSupport;
    private final List<Evaluation> evaluations;
    private int jetons;
    private int paliersVotesPositifs;

    public Joueur(String pseudo) {
        super(pseudo);
        this.supportsPossedes = new ArrayList<>();
        this.tempsParSupport = new LinkedHashMap<>();
        this.evaluations = new ArrayList<>();
        this.jetons = JETONS_INITIAUX;
        this.paliersVotesPositifs = 0;
    }

    protected void copierEtatDe(Joueur autre) {
        if (autre == null) {
            throw new IllegalArgumentException("Le joueur source ne peut pas etre null");
        }
        supportsPossedes.addAll(autre.supportsPossedes);
        tempsParSupport.putAll(autre.tempsParSupport);
        evaluations.addAll(autre.evaluations);
        jetons = autre.jetons;
        paliersVotesPositifs = autre.paliersVotesPositifs;
        if (autre.estBloque()) {
            bloquer();
        }
    }

    @Override
    public String getTypeProfil() {
        return "joueur";
    }

    public void ajouterJeu(SupportJeu support) {
        if (support == null) {
            throw new IllegalArgumentException("Le support de jeu ne peut pas etre null");
        }
        if (!supportsPossedes.contains(support)) {
            supportsPossedes.add(support);
        }
        tempsParSupport.putIfAbsent(support, 0);
    }

    public void ajouterTempsDeJeu(SupportJeu support, int heures) {
        ajouterTempsJeu(support, heures);
    }

    public void ajouterTempsJeu(SupportJeu support, int heures) {
        if (support == null) {
            throw new IllegalArgumentException("Le support de jeu ne peut pas etre null");
        }
        if (heures < 0) {
            throw new IllegalArgumentException("Le temps de jeu ne peut pas etre negatif");
        }
        if (!possedeSupport(support)) {
            throw new IllegalArgumentException("Le joueur ne possede pas ce support");
        }
        tempsParSupport.merge(support, heures, Integer::sum);
    }

    public boolean possedeSupport(SupportJeu support) {
        return support != null && supportsPossedes.contains(support);
    }

    public boolean possedeJeu(SupportJeu support) {
        return possedeSupport(support);
    }

    public boolean possedeJeu(Jeu jeu) {
        if (jeu == null) {
            return false;
        }
        for (SupportJeu support : supportsPossedes) {
            if (support.getJeu() == jeu) {
                return true;
            }
        }
        return false;
    }

    public List<Jeu> getJeuxPossedes() {
        LinkedHashSet<Jeu> jeux = new LinkedHashSet<>();
        for (SupportJeu support : supportsPossedes) {
            jeux.add(support.getJeu());
        }
        return List.copyOf(jeux);
    }

    public List<SupportJeu> getSupportsPossedes() {
        return Collections.unmodifiableList(supportsPossedes);
    }

    public int getTempsDeJeu(SupportJeu support) {
        if (support == null) {
            return 0;
        }
        return tempsParSupport.getOrDefault(support, 0);
    }

    public int getTempsDeJeu(Jeu jeu) {
        int total = 0;
        for (Map.Entry<SupportJeu, Integer> entree : tempsParSupport.entrySet()) {
            if (entree.getKey().getJeu() == jeu) {
                total += entree.getValue();
            }
        }
        return total;
    }

    public int getTempsDeJeuTotal() {
        return tempsParSupport.values().stream().mapToInt(Integer::intValue).sum();
    }

    public Map<Jeu, Integer> getTempsDeJeuParJeu() {
        Map<Jeu, Integer> tempsParJeu = new LinkedHashMap<>();
        for (SupportJeu support : supportsPossedes) {
            tempsParJeu.merge(support.getJeu(), getTempsDeJeu(support), Integer::sum);
        }
        return Collections.unmodifiableMap(tempsParJeu);
    }

    public void ajouterEvaluation(Evaluation evaluation) {
        if (evaluation == null) {
            throw new IllegalArgumentException("L'evaluation ne peut pas etre null");
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

    public int getNombreEvaluations() {
        return getEvaluations().size();
    }

    public int getNombreVotesPositifsRecus() {
        return getEvaluations().stream().mapToInt(Evaluation::getVotesPositifs).sum();
    }

    public int getNombreVotesNeutresRecus() {
        return getEvaluations().stream().mapToInt(Evaluation::getVotesNeutres).sum();
    }

    public int getNombreVotesNegatifsRecus() {
        return getEvaluations().stream().mapToInt(Evaluation::getVotesNegatifs).sum();
    }

    List<Evaluation> getToutesLesEvaluations() {
        return Collections.unmodifiableList(evaluations);
    }

    public void notifierVotePositifRecu() {
        int paliers = getNombreVotesPositifsRecus() / 10;
        if (paliers > paliersVotesPositifs) {
            ajouterJetons(paliers - paliersVotesPositifs);
            paliersVotesPositifs = paliers;
        }
    }

    public int getJetons() {
        return jetons;
    }

    public void ajouterJetons(int quantite) {
        if (quantite < 0) {
            throw new IllegalArgumentException("La quantite doit etre positive");
        }
        jetons += quantite;
    }

    public void retirerJetons(int quantite) {
        if (quantite < 0) {
            throw new IllegalArgumentException("La quantite doit etre positive");
        }
        if (quantite > jetons) {
            throw new IllegalArgumentException("Jetons insuffisants");
        }
        jetons -= quantite;
    }

    @Override
    public String toString() {
        return getTypeProfil() + "{" +
                "pseudo='" + getPseudo() + '\'' +
                ", bloque=" + estBloque() +
                ", jetons=" + jetons +
                ", supportsPossedes=" + supportsPossedes.size() +
                ", tempsDeJeuTotal=" + getTempsDeJeuTotal() +
                '}';
    }
}
