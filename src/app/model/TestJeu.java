package app.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represente un test publie par un testeur pour un support de jeu.
 */
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

    /**
     * Cree un test minimal.
     *
     * @param auteur auteur du test
     * @param support support teste
     * @param date date de publication
     * @param texte contenu du test
     * @param version version ou build testee
     */
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

    /**
     * Cree un test complet a partir d'une structure immuable.
     *
     * @param auteur auteur du test
     * @param support support teste
     * @param date date de publication
     * @param texte contenu du test
     * @param version version ou build testee
     * @param structure structure detaillee du test
     */
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

    /**
     * Retourne l'auteur du test.
     *
     * @return auteur du test
     */
    public Testeur getAuteur() {
        return auteur;
    }

    void remplacerAuteur(Testeur auteur) {
        this.auteur = Objects.requireNonNull(auteur, "L'auteur ne peut pas etre null");
    }

    /**
     * Retourne le support de jeu teste.
     *
     * @return support associe
     */
    public SupportJeu getSupportJeu() {
        return support;
    }

    /**
     * Retourne la date de publication du test.
     *
     * @return date de publication
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * Retourne le texte principal du test.
     *
     * @return contenu textuel du test
     */
    public String getTexte() {
        return texte;
    }

    /**
     * Retourne la version ou le build teste.
     *
     * @return version ou build
     */
    public String getVersionBuild() {
        return version;
    }

    /**
     * Ajoute une note generale par categorie.
     *
     * @param categorie categorie evaluee
     * @param note note sur 100
     */
    public void ajouterNoteCategorie(String categorie, int note) {
        if (categorie == null || categorie.trim().isEmpty()) {
            throw new IllegalArgumentException("La categorie ne peut pas etre vide");
        }
        verifierNote(note);
        notes.put(categorie, note);
    }

    /**
     * Retourne les notes generales par categorie.
     *
     * @return map non modifiable des notes
     */
    public Map<String, Integer> getNotesParCategorie() {
        return Collections.unmodifiableMap(notes);
    }

    /**
     * Ajoute un point fort au test.
     *
     * @param pointFort point fort a enregistrer
     */
    public void ajouterPointFort(String pointFort) {
        if (pointFort == null || pointFort.trim().isEmpty()) {
            throw new IllegalArgumentException("Le point fort ne peut pas etre vide");
        }
        pointsForts.add(pointFort);
    }

    /**
     * Retourne les points forts du test.
     *
     * @return liste non modifiable des points forts
     */
    public List<String> getPointsForts() {
        return Collections.unmodifiableList(pointsForts);
    }

    /**
     * Ajoute un point faible au test.
     *
     * @param pointFaible point faible a enregistrer
     */
    public void ajouterPointFaible(String pointFaible) {
        if (pointFaible == null || pointFaible.trim().isEmpty()) {
            throw new IllegalArgumentException("Le point faible ne peut pas etre vide");
        }
        pointsFaibles.add(pointFaible);
    }

    /**
     * Retourne les points faibles du test.
     *
     * @return liste non modifiable des points faibles
     */
    public List<String> getPointsFaibles() {
        return Collections.unmodifiableList(pointsFaibles);
    }

    /**
     * Retourne les conditions de realisation du test.
     *
     * @return conditions de test
     */
    public String getConditionsTest() {
        return conditions;
    }

    /**
     * Met a jour les conditions de realisation du test.
     *
     * @param conditions conditions de test
     */
    public void setConditionsTest(String conditions) {
        this.conditions = Objects.requireNonNull(conditions, "Les conditions ne peuvent pas etre null").trim();
    }

    /**
     * Ajoute un jeu similaire conseille.
     *
     * @param nom nom du jeu similaire
     */
    public void ajouterJeuSimilaire(String nom) {
        if (nom == null || nom.trim().isEmpty()) {
            throw new IllegalArgumentException("Le jeu similaire ne peut pas etre vide");
        }
        jeuxSimilaires.add(nom);
    }

    /**
     * Retourne les jeux similaires recommandes.
     *
     * @return liste non modifiable des jeux similaires
     */
    public List<String> getJeuxSimilaires() {
        return Collections.unmodifiableList(jeuxSimilaires);
    }

    /**
     * Ajoute une note specifique au genre du jeu.
     *
     * @param categorie categorie specifique
     * @param note note sur 100
     */
    public void ajouterNoteSpecifiqueAuGenre(String categorie, int note) {
        if (categorie == null || categorie.trim().isEmpty()) {
            throw new IllegalArgumentException("La categorie ne peut pas etre vide");
        }
        verifierNote(note);
        notesGenre.put(categorie, note);
    }

    /**
     * Retourne les notes specifiques au genre.
     *
     * @return map non modifiable des notes specifiques
     */
    public Map<String, Integer> getNotesSpecifiquesAuGenre() {
        return Collections.unmodifiableMap(notesGenre);
    }

    /**
     * Calcule la note moyenne du test a partir de l'ensemble des notes saisies.
     *
     * @return note moyenne sur 100
     */
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
