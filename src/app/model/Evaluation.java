package app.model;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

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

    public Joueur getAuteur() {
        return auteur;
    }

    void remplacerAuteur(Joueur auteur) {
        this.auteur = Objects.requireNonNull(auteur, "L'auteur ne peut pas etre null");
    }

    public SupportJeu getSupportJeu() {
        return support;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getTexte() {
        return texte;
    }

    public String getVersionBuild() {
        return version;
    }

    public double getNoteGlobale() {
        return note;
    }

    public int getVotesPositifs() {
        return positifs;
    }

    public int getVotesNeutres() {
        return neutres;
    }

    public int getVotesNegatifs() {
        return negatifs;
    }

    public void voterPositif() {
        positifs++;
        auteur.notifierVotePositifRecu();
    }

    public void voterNeutre() {
        neutres++;
    }

    public void voterNegatif() {
        negatifs++;
    }

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

    public boolean estSignalee() {
        return signalee;
    }

    public void signaler() {
        signalee = true;
    }

    public boolean estSupprimee() {
        return supprimee;
    }

    public void supprimer() {
        supprimee = true;
    }

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
