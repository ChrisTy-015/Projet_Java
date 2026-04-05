package app.model;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represente une evaluation publiee par un joueur pour un support de jeu.
 * <p>
 * Une evaluation peut recevoir des votes d'utilite, etre signalee
 * et etre marquee comme supprimee.
 */
public class Evaluation {
    private Joueur auteur;
    private final SupportJeu support;
    private final LocalDate date;
    private final String texte;
    private final String version;
    private final double note;
    private int positifs;
    private int neutres;
    private int negatifs;
    private boolean signalee;
    private boolean supprimee;
    private final Set<String> votants;

    /**
     * Cree une evaluation.
     *
     * @param auteur auteur de l'evaluation
     * @param support support evalue
     * @param date date de publication
     * @param texte texte de l'evaluation
     * @param version version ou build evaluee
     * @param note note globale sur 10
     */
    public Evaluation(Joueur auteur, SupportJeu support, LocalDate date, String texte, String version, double note) {
        this.auteur = Objects.requireNonNull(auteur, "L'auteur ne peut pas etre null");
        this.support = Objects.requireNonNull(support, "Le support de jeu ne peut pas etre null");
        this.date = Objects.requireNonNull(date, "La date ne peut pas etre null");
        this.texte = Objects.requireNonNull(texte, "Le texte ne peut pas etre null").trim();
        this.version = Objects.requireNonNull(version, "La version ne peut pas etre null").trim();
        if (note < 0.0 || note > 10.0) {
            throw new IllegalArgumentException("Note hors intervalle : la note doit etre comprise entre 0 et 10");
        }
        this.note = note;
        this.positifs = 0;
        this.neutres = 0;
        this.negatifs = 0;
        this.signalee = false;
        this.supprimee = false;
        this.votants = new LinkedHashSet<>();
    }

    /**
     * Retourne l'auteur de l'evaluation.
     *
     * @return auteur de l'evaluation
     */
    public Joueur getAuteur() {
        return auteur;
    }

    void remplacerAuteur(Joueur auteur) {
        this.auteur = Objects.requireNonNull(auteur, "L'auteur ne peut pas etre null");
    }

    /**
     * Retourne le support evalue.
     *
     * @return support associe
     */
    public SupportJeu getSupportJeu() {
        return support;
    }

    /**
     * Retourne la date de publication.
     *
     * @return date de publication
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * Retourne le texte libre de l'evaluation.
     *
     * @return texte de l'evaluation
     */
    public String getTexte() {
        return texte;
    }

    /**
     * Retourne la version ou le build de jeu evalue.
     *
     * @return version ou build
     */
    public String getVersionBuild() {
        return version;
    }

    /**
     * Retourne la note globale sur 10.
     *
     * @return note globale
     */
    public double getNoteGlobale() {
        return note;
    }

    /**
     * Retourne le nombre de votes positifs.
     *
     * @return nombre de votes positifs
     */
    public int getVotesPositifs() {
        return positifs;
    }

    /**
     * Retourne le nombre de votes neutres.
     *
     * @return nombre de votes neutres
     */
    public int getVotesNeutres() {
        return neutres;
    }

    /**
     * Retourne le nombre de votes negatifs.
     *
     * @return nombre de votes negatifs
     */
    public int getVotesNegatifs() {
        return negatifs;
    }

    /**
     * Enregistre un vote positif et met a jour les recompenses eventuelles
     * de l'auteur.
     */
    public void voterPositif() {
        positifs++;
        auteur.notifierVotePositifRecu();
    }

    /**
     * Enregistre un vote neutre.
     */
    public void voterNeutre() {
        neutres++;
    }

    /**
     * Enregistre un vote negatif.
     */
    public void voterNegatif() {
        negatifs++;
    }

    /**
     * Enregistre un vote d'utilite pour cette evaluation.
     *
     * @param votant membre qui vote
     * @param vote type de vote enregistre
     */
    public void voter(Membre votant, VoteUtilite vote) {
        Objects.requireNonNull(votant, "Le votant ne peut pas etre null");
        Objects.requireNonNull(vote, "Le vote ne peut pas etre null");
        if (supprimee) {
            throw new IllegalStateException("Impossible de voter pour une evaluation supprimee");
        }
        if (auteur.getPseudo().equalsIgnoreCase(votant.getPseudo())) {
            throw new IllegalArgumentException("L'auteur ne peut pas voter pour sa propre evaluation");
        }

        String pseudo = normaliserPseudo(votant.getPseudo());
        if (!votants.add(pseudo)) {
            throw new IllegalStateException("Ce membre a deja vote pour cette evaluation");
        }

        switch (vote) {
            case POSITIF -> voterPositif();
            case NEUTRE -> voterNeutre();
            case NEGATIF -> voterNegatif();
        }
    }

    /**
     * Indique si l'evaluation a ete signalee.
     *
     * @return {@code true} si l'evaluation est signalee
     */
    public boolean estSignalee() {
        return signalee;
    }

    /**
     * Marque l'evaluation comme signalee.
     */
    public void signaler() {
        signalee = true;
    }

    /**
     * Indique si l'evaluation a ete marquee comme supprimee.
     *
     * @return {@code true} si l'evaluation est supprimee
     */
    public boolean estSupprimee() {
        return supprimee;
    }

    /**
     * Marque l'evaluation comme supprimee.
     */
    public void supprimer() {
        supprimee = true;
    }

    /**
     * Retourne le score d'utilite, calcule comme la difference
     * entre votes positifs et votes negatifs.
     *
     * @return score d'utilite
     */
    public int getScoreUtilite() {
        return positifs - negatifs;
    }

    private String normaliserPseudo(String pseudo) {
        return pseudo.trim().toLowerCase();
    }

    @Override
    public String toString() {
        return "Evaluation{" +
                "auteur='" + auteur.getPseudo() + '\'' +
                ", jeu='" + support.getJeu().getNom() + '\'' +
                ", plateforme='" + support.getPlateforme() + '\'' +
                ", date=" + date +
                ", versionBuild='" + version + '\'' +
                ", noteGlobale=" + note +
                ", utilite=" + getScoreUtilite() +
                '}';
    }
}
