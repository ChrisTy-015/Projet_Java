package app.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TestJeu {
    private Testeur auteur;
    private final SupportJeu support;
    private final LocalDate date;
    private final String texte;
    private final String version;
    private final Map<String, Integer> notes;
    private final List<String> pointsForts;
    private final List<String> pointsFaibles;
    private String conditions;
    private final List<String> jeuxSimilaires;
    private final Map<String, Integer> notesGenre;

    public TestJeu(Testeur auteur, SupportJeu support, LocalDate date, String texte, String version) {
        this.auteur = Objects.requireNonNull(auteur, "L'auteur ne peut pas etre null");
        this.support = Objects.requireNonNull(support, "Le support de jeu ne peut pas etre null");
        this.date = Objects.requireNonNull(date, "La date ne peut pas etre null");
        this.texte = Objects.requireNonNull(texte, "Le texte ne peut pas etre null").trim();
        this.version = Objects.requireNonNull(version, "La version ne peut pas etre null").trim();
        this.notes = new LinkedHashMap<>();
        this.pointsForts = new ArrayList<>();
        this.pointsFaibles = new ArrayList<>();
        this.conditions = "";
        this.jeuxSimilaires = new ArrayList<>();
        this.notesGenre = new LinkedHashMap<>();
    }

    public TestJeu(
            Testeur auteur,
            SupportJeu support,
            LocalDate date,
            String texte,
            String version,
            StructureTestJeu structure
    ) {
        this(auteur, support, date, texte, version);
        Objects.requireNonNull(structure, "La structure du test ne peut pas etre null");
        structure.notesParCategorie().forEach(this::ajouterNoteCategorie);
        structure.pointsForts().forEach(this::ajouterPointFort);
        structure.pointsFaibles().forEach(this::ajouterPointFaible);
        if (!structure.conditionsTest().isEmpty()) {
            setConditionsTest(structure.conditionsTest());
        }
        structure.jeuxSimilaires().forEach(this::ajouterJeuSimilaire);
        structure.notesSpecifiquesAuGenre().forEach(this::ajouterNoteSpecifiqueAuGenre);
    }

    public Testeur getAuteur() {
        return auteur;
    }

    void remplacerAuteur(Testeur auteur) {
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

    public void ajouterNoteCategorie(String categorie, int note) {
        if (categorie == null || categorie.trim().isEmpty()) {
            throw new IllegalArgumentException("La categorie ne peut pas etre vide");
        }
        verifierNote(note);
        notes.put(categorie, note);
    }

    public Map<String, Integer> getNotesParCategorie() {
        return Collections.unmodifiableMap(notes);
    }

    public void ajouterPointFort(String pointFort) {
        if (pointFort == null || pointFort.trim().isEmpty()) {
            throw new IllegalArgumentException("Le point fort ne peut pas etre vide");
        }
        pointsForts.add(pointFort);
    }

    public List<String> getPointsForts() {
        return Collections.unmodifiableList(pointsForts);
    }

    public void ajouterPointFaible(String pointFaible) {
        if (pointFaible == null || pointFaible.trim().isEmpty()) {
            throw new IllegalArgumentException("Le point faible ne peut pas etre vide");
        }
        pointsFaibles.add(pointFaible);
    }

    public List<String> getPointsFaibles() {
        return Collections.unmodifiableList(pointsFaibles);
    }

    public String getConditionsTest() {
        return conditions;
    }

    public void setConditionsTest(String conditions) {
        this.conditions = Objects.requireNonNull(conditions, "Les conditions ne peuvent pas etre null").trim();
    }

    public void ajouterJeuSimilaire(String nom) {
        if (nom == null || nom.trim().isEmpty()) {
            throw new IllegalArgumentException("Le jeu similaire ne peut pas etre vide");
        }
        jeuxSimilaires.add(nom);
    }

    public List<String> getJeuxSimilaires() {
        return Collections.unmodifiableList(jeuxSimilaires);
    }

    public void ajouterNoteSpecifiqueAuGenre(String categorie, int note) {
        if (categorie == null || categorie.trim().isEmpty()) {
            throw new IllegalArgumentException("La categorie ne peut pas etre vide");
        }
        verifierNote(note);
        notesGenre.put(categorie, note);
    }

    public Map<String, Integer> getNotesSpecifiquesAuGenre() {
        return Collections.unmodifiableMap(notesGenre);
    }

    public double calculerNoteMoyenne() {
        return concatenerToutesLesNotes().stream().mapToInt(Integer::intValue).average().orElse(0.0);
    }

    private List<Integer> concatenerToutesLesNotes() {
        List<Integer> toutes = new ArrayList<>(notes.values());
        toutes.addAll(notesGenre.values());
        return toutes;
    }

    private void verifierNote(int note) {
        if (note < 0 || note > 100) {
            throw new IllegalArgumentException("La note doit etre comprise entre 0 et 100");
        }
    }

    @Override
    public String toString() {
        return "TestJeu{" +
                "auteur='" + auteur.getPseudo() + '\'' +
                ", jeu='" + support.getJeu().getNom() + '\'' +
                ", plateforme='" + support.getPlateforme() + '\'' +
                ", date=" + date +
                ", versionBuild='" + version + '\'' +
                ", noteMoyenne=" + calculerNoteMoyenne() +
                '}';
    }
}
