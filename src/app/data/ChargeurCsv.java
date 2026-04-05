package app.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import app.model.Jeu;
import app.model.Plateforme;
import app.model.SupportJeu;

/**
 * Charge un catalogue de jeux video depuis un fichier CSV local
 * ou depuis une URL distante.
 * <p>
 * Chaque ligne du CSV represente un support de jeu et est convertie
 * en objets {@link Jeu} et {@link SupportJeu}.
 */
public class ChargeurCsv {
    /**
     * URL du catalogue CSV utilisee lorsque aucune source locale n'est fournie.
     */
    public static final String URL_CSV_PAR_DEFAUT =
            "https://raw.githubusercontent.com/charlesbrantstec/VGSalesRatings/28980b2078f851b30d449186a45cb5127d81ea60/VG/output_csv/vg_data.csv";

    private static final int INDEX_NAME = 1;
    private static final int INDEX_PLATFORM = 2;
    private static final int INDEX_YEAR = 3;
    private static final int INDEX_GENRE = 4;
    private static final int INDEX_PUBLISHER = 5;
    private static final int INDEX_GLOBAL_SALES = 10;
    private static final int INDEX_CRITIC_SCORE = 11;
    private static final int INDEX_CRITIC_COUNT = 12;
    private static final int INDEX_USER_SCORE = 13;
    private static final int INDEX_USER_COUNT = 14;
    private static final int INDEX_DEVELOPER = 15;
    private static final int INDEX_RATING = 16;

    /**
     * Cree un chargeur CSV.
     */
    public ChargeurCsv() {
    }

    /**
     * Charge un catalogue complet depuis un chemin de fichier.
     *
     * @param chemin chemin du fichier CSV
     * @return une nouvelle plateforme peuplee avec les donnees du fichier
     * @throws IOException en cas d'erreur de lecture
     */
    public Plateforme chargerDepuisFichier(String chemin) throws IOException {
        return chargerDepuisFichier(Path.of(chemin));
    }

    /**
     * Charge un catalogue complet depuis un chemin de fichier.
     *
     * @param chemin chemin du fichier CSV
     * @return une nouvelle plateforme peuplee avec les donnees du fichier
     * @throws IOException en cas d'erreur de lecture
     */
    public Plateforme chargerDepuisFichier(Path chemin) throws IOException {
        Plateforme p = new Plateforme();
        chargerDansPlateforme(chemin, p);
        return p;
    }

    /**
     * Charge un catalogue complet depuis une URL CSV.
     *
     * @param url URL du fichier CSV
     * @return une nouvelle plateforme peuplee avec les donnees recuperees
     * @throws IOException en cas d'erreur de lecture
     */
    public Plateforme chargerDepuisUrl(String url) throws IOException {
        Plateforme p = new Plateforme();
        chargerDansPlateformeDepuisUrl(url, p);
        return p;
    }

    /**
     * Charge un catalogue complet depuis l'URL par defaut.
     *
     * @return une nouvelle plateforme peuplee avec les donnees du catalogue distant
     * @throws IOException en cas d'erreur de lecture
     */
    public Plateforme chargerDepuisUrlParDefaut() throws IOException {
        return chargerDepuisUrl(URL_CSV_PAR_DEFAUT);
    }

    /**
     * Charge un catalogue complet depuis une ressource embarquee dans le classpath.
     *
     * @param nomRessource nom de la ressource a lire
     * @return une nouvelle plateforme peuplee avec les donnees de la ressource
     * @throws IOException si la ressource est introuvable ou illisible
     */
    public Plateforme chargerDepuisRessource(String nomRessource) throws IOException {
        Plateforme p = new Plateforme();
        chargerDansPlateformeDepuisRessource(nomRessource, p);
        return p;
    }

    /**
     * Ajoute les donnees d'un fichier CSV a une plateforme existante.
     *
     * @param chemin chemin du fichier CSV
     * @param plateforme plateforme a enrichir
     * @throws IOException en cas d'erreur de lecture
     */
    public void chargerDansPlateforme(Path chemin, Plateforme plateforme) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(chemin)) {
            chargerDepuisLecteur(reader, plateforme);
        }
    }

    /**
     * Ajoute les donnees d'un CSV distant a une plateforme existante.
     *
     * @param url URL du fichier CSV
     * @param plateforme plateforme a enrichir
     * @throws IOException en cas d'erreur de lecture
     */
    public void chargerDansPlateformeDepuisUrl(String url, Plateforme plateforme) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(URI.create(url).toURL().openStream(), StandardCharsets.UTF_8))) {
            chargerDepuisLecteur(reader, plateforme);
        }
    }

    /**
     * Ajoute les donnees d'une ressource CSV embarquee a une plateforme existante.
     *
     * @param nomRessource nom de la ressource a lire depuis le classpath
     * @param plateforme plateforme a enrichir
     * @throws IOException si la ressource est introuvable ou illisible
     */
    public void chargerDansPlateformeDepuisRessource(String nomRessource, Plateforme plateforme) throws IOException {
        try (InputStream flux = ChargeurCsv.class.getResourceAsStream("/" + nomRessource)) {
            if (flux == null) {
                throw new IOException("Ressource introuvable : " + nomRessource);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(flux, StandardCharsets.UTF_8))) {
                chargerDepuisLecteur(reader, plateforme);
            }
        }
    }

    private void chargerDepuisLecteur(BufferedReader reader, Plateforme plateforme) throws IOException {
        String ligne = reader.readLine();
        if (ligne == null) {
            return;
        }

        while ((ligne = reader.readLine()) != null) {
            if (ligne.isBlank()) {
                continue;
            }
            traiterLigne(ligne, plateforme);
        }
    }

    private void traiterLigne(String ligne, Plateforme plateforme) {
        List<String> colonnes = decouper(ligne);
        if (colonnes.size() <= INDEX_RATING) {
            return;
        }

        String nom = texte(colonnes, INDEX_NAME);
        String support = texte(colonnes, INDEX_PLATFORM);
        if (nom.isEmpty() || support.isEmpty()) {
            return;
        }

        Jeu jeu = plateforme.trouverJeu(nom);
        if (jeu == null) {
            jeu = new Jeu(
                    nom,
                    texte(colonnes, INDEX_GENRE),
                    texte(colonnes, INDEX_PUBLISHER),
                    texte(colonnes, INDEX_RATING)
            );
            plateforme.ajouterJeu(jeu);
        }

        if (jeu.trouverSupport(support) != null) {
            return;
        }

        SupportJeu supportJeu = new SupportJeu(
                jeu,
                support,
                entier(colonnes, INDEX_YEAR),
                texte(colonnes, INDEX_DEVELOPER),
                decimal(colonnes, INDEX_GLOBAL_SALES),
                entier(colonnes, INDEX_CRITIC_COUNT),
                decimal(colonnes, INDEX_CRITIC_SCORE),
                entier(colonnes, INDEX_USER_COUNT),
                decimal(colonnes, INDEX_USER_SCORE)
        );
        jeu.ajouterSupport(supportJeu);
    }

    private List<String> decouper(String ligne) {
        List<String> colonnes = new ArrayList<>();
        StringBuilder mot = new StringBuilder();
        boolean dansGuillemets = false;

        for (int i = 0; i < ligne.length(); i++) {
            char c = ligne.charAt(i);

            if (c == '"') {
                dansGuillemets = !dansGuillemets;
                continue;
            }

            if (c == ',' && !dansGuillemets) {
                colonnes.add(mot.toString());
                mot.setLength(0);
            } else {
                mot.append(c);
            }
        }

        colonnes.add(mot.toString());
        return colonnes;
    }

    private String texte(List<String> colonnes, int index) {
        if (index >= colonnes.size()) {
            return "";
        }
        return colonnes.get(index).trim();
    }

    private int entier(List<String> colonnes, int index) {
        String valeur = texte(colonnes, index);
        if (valeur.isEmpty() || "N/A".equalsIgnoreCase(valeur)) {
            return 0;
        }
        try {
            return (int) Double.parseDouble(valeur);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private double decimal(List<String> colonnes, int index) {
        String valeur = texte(colonnes, index);
        if (valeur.isEmpty() || "N/A".equalsIgnoreCase(valeur) || "tbd".equalsIgnoreCase(valeur)) {
            return 0.0;
        }
        try {
            return Double.parseDouble(valeur);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
