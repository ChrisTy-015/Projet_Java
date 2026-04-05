package app.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Represente un joueur inscrit sur la plateforme.
 * <p>
 * Un joueur possede des supports de jeu, du temps de jeu, des evaluations
 * publiees et un stock de jetons.
 */
public class Joueur extends Membre {
    /**
     * Nombre de jetons attribues a l'inscription.
     */
    public static final int JETONS_INITIAUX = 3;

    private final List<SupportJeu> supportsPossedes;
    private final Map<SupportJeu, Integer> tempsParSupport;
    private final List<Evaluation> evaluations;
    private int jetons;
    private int paliersVotesPositifs;

    /**
     * Cree un joueur a partir de son pseudo.
     *
     * @param pseudo pseudo du joueur
     */
    public Joueur(String pseudo) {
        super(pseudo);
        this.supportsPossedes = new ArrayList<>();
        this.tempsParSupport = new LinkedHashMap<>();
        this.evaluations = new ArrayList<>();
        this.jetons = JETONS_INITIAUX;
        this.paliersVotesPositifs = 0;
    }

    /**
     * Copie l'etat metier d'un autre joueur.
     *
     * @param autre joueur source
     */
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

    /**
     * Ajoute un support a la bibliotheque du joueur.
     *
     * @param support support possede
     */
    public void ajouterJeu(SupportJeu support) {
        if (support == null) {
            throw new IllegalArgumentException("Le support de jeu ne peut pas etre null");
        }
        if (!supportsPossedes.contains(support)) {
            supportsPossedes.add(support);
        }
        tempsParSupport.putIfAbsent(support, 0);
    }

    /**
     * Alias conservant l'intention metier d'ajout de temps de jeu.
     *
     * @param support support concerne
     * @param heures nombre d'heures a ajouter
     */
    public void ajouterTempsDeJeu(SupportJeu support, int heures) {
        ajouterTempsJeu(support, heures);
    }

    /**
     * Ajoute du temps de jeu sur un support possede par le joueur.
     *
     * @param support support concerne
     * @param heures nombre d'heures a ajouter
     */
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

    /**
     * Indique si le joueur possede explicitement un support donne.
     *
     * @param support support recherche
     * @return {@code true} si le support est present dans sa bibliotheque
     */
    public boolean possedeSupport(SupportJeu support) {
        return support != null && supportsPossedes.contains(support);
    }

    /**
     * Indique si le joueur possede un support donne.
     *
     * @param support support recherche
     * @return {@code true} si le support est possede
     */
    public boolean possedeJeu(SupportJeu support) {
        return possedeSupport(support);
    }

    /**
     * Indique si le joueur possede au moins un support du jeu donne.
     *
     * @param jeu jeu recherche
     * @return {@code true} si le joueur possede ce jeu
     */
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

    /**
     * Retourne la liste des jeux possedes, sans doublons de support.
     *
     * @return liste des jeux possedes
     */
    public List<Jeu> getJeuxPossedes() {
        LinkedHashSet<Jeu> jeux = new LinkedHashSet<>();
        for (SupportJeu support : supportsPossedes) {
            jeux.add(support.getJeu());
        }
        return List.copyOf(jeux);
    }

    /**
     * Retourne les supports possedes par le joueur.
     *
     * @return liste non modifiable des supports possedes
     */
    public List<SupportJeu> getSupportsPossedes() {
        return Collections.unmodifiableList(supportsPossedes);
    }

    /**
     * Retourne le temps de jeu enregistre pour un support.
     *
     * @param support support concerne
     * @return temps de jeu en heures
     */
    public int getTempsDeJeu(SupportJeu support) {
        if (support == null) {
            return 0;
        }
        return tempsParSupport.getOrDefault(support, 0);
    }

    /**
     * Retourne le temps de jeu cumule sur tous les supports d'un meme jeu.
     *
     * @param jeu jeu concerne
     * @return temps de jeu total
     */
    public int getTempsDeJeu(Jeu jeu) {
        int total = 0;
        for (Map.Entry<SupportJeu, Integer> entree : tempsParSupport.entrySet()) {
            if (entree.getKey().getJeu() == jeu) {
                total += entree.getValue();
            }
        }
        return total;
    }

    /**
     * Retourne le temps de jeu total du joueur.
     *
     * @return temps cumule sur tous les supports
     */
    public int getTempsDeJeuTotal() {
        return tempsParSupport.values().stream().mapToInt(Integer::intValue).sum();
    }

    /**
     * Retourne le temps de jeu agrege par jeu.
     *
     * @return map non modifiable du temps de jeu par jeu
     */
    public Map<Jeu, Integer> getTempsDeJeuParJeu() {
        Map<Jeu, Integer> tempsParJeu = new LinkedHashMap<>();
        for (SupportJeu support : supportsPossedes) {
            tempsParJeu.merge(support.getJeu(), getTempsDeJeu(support), Integer::sum);
        }
        return Collections.unmodifiableMap(tempsParJeu);
    }

    /**
     * Associe une evaluation au joueur.
     *
     * @param evaluation evaluation publiee par le joueur
     */
    public void ajouterEvaluation(Evaluation evaluation) {
        if (evaluation == null) {
            throw new IllegalArgumentException("L'evaluation ne peut pas etre null");
        }
        evaluations.add(evaluation);
    }

    /**
     * Retourne les evaluations visibles du joueur.
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
     * Retourne le nombre d'evaluations visibles du joueur.
     *
     * @return nombre d'evaluations
     */
    public int getNombreEvaluations() {
        return getEvaluations().size();
    }

    /**
     * Retourne le nombre total de votes positifs recus par les evaluations du joueur.
     *
     * @return nombre de votes positifs
     */
    public int getNombreVotesPositifsRecus() {
        return getEvaluations().stream().mapToInt(Evaluation::getVotesPositifs).sum();
    }

    /**
     * Retourne le nombre total de votes neutres recus par les evaluations du joueur.
     *
     * @return nombre de votes neutres
     */
    public int getNombreVotesNeutresRecus() {
        return getEvaluations().stream().mapToInt(Evaluation::getVotesNeutres).sum();
    }

    /**
     * Retourne le nombre total de votes negatifs recus par les evaluations du joueur.
     *
     * @return nombre de votes negatifs
     */
    public int getNombreVotesNegatifsRecus() {
        return getEvaluations().stream().mapToInt(Evaluation::getVotesNegatifs).sum();
    }

    List<Evaluation> getToutesLesEvaluations() {
        return Collections.unmodifiableList(evaluations);
    }

    /**
     * Met a jour la recompense en jetons du joueur lorsqu'il franchit
     * un nouveau palier de votes positifs recus.
     */
    public void notifierVotePositifRecu() {
        int paliers = getNombreVotesPositifsRecus() / 10;
        if (paliers > paliersVotesPositifs) {
            ajouterJetons(paliers - paliersVotesPositifs);
            paliersVotesPositifs = paliers;
        }
    }

    /**
     * Retourne le nombre de jetons actuellement disponibles.
     *
     * @return stock de jetons du joueur
     */
    public int getJetons() {
        return jetons;
    }

    /**
     * Credite le joueur d'un certain nombre de jetons.
     *
     * @param quantite quantite a ajouter
     */
    public void ajouterJetons(int quantite) {
        if (quantite < 0) {
            throw new IllegalArgumentException("La quantite doit etre positive");
        }
        jetons += quantite;
    }

    /**
     * Retire des jetons au stock disponible du joueur.
     *
     * @param quantite quantite a retirer
     */
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
