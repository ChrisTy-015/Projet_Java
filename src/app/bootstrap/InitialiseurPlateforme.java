package app.bootstrap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import app.data.ChargeurCsv;
import app.model.Evaluation;
import app.model.Jeu;
import app.model.Plateforme;
import app.model.SupportJeu;
import app.model.Testeur;
import app.model.VoteUtilite;

/**
 * Prepare la plateforme au demarrage a partir d'un catalogue CSV ou,
 * en secours, d'un jeu de donnees de demonstration.
 */
public final class InitialiseurPlateforme {
    /**
     * Constructeur prive d'une classe utilitaire.
     */
    private InitialiseurPlateforme() {
    }

    /**
     * Charge les donnees initiales de l'application.
     * <p>
     * La source est choisie selon l'ordre suivant :
     * un argument explicite, un fichier local standard, l'URL CSV par defaut,
     * puis un catalogue de demonstration en cas d'echec.
     *
     * @param args arguments fournis a l'application
     * @return la plateforme initialisee ainsi que les messages de contexte associes
     */
    public static DonneesInitiales charger(String[] args) {
        ChargeurCsv chargeur = new ChargeurCsv();

        if (args != null && args.length > 0 && !args[0].isBlank()) {
            return chargerDepuisSource(chargeur, args[0].trim());
        }

        for (Path chemin : cheminsCsvPossibles()) {
            if (Files.exists(chemin)) {
                return chargerDepuisSource(chargeur, chemin.toString());
            }
        }

        try {
            Plateforme plateforme = chargeur.chargerDepuisRessource("vg_data.csv");
            return new DonneesInitiales(
                    plateforme,
                    "Catalogue charge depuis la ressource embarquee vg_data.csv.",
                    "Compte initial : admin (administrateur). Inscrivez ensuite des membres depuis l'application."
            );
        } catch (IOException e) {
            // Passage a la source distante par defaut.
        }

        try {
            Plateforme plateforme = chargeur.chargerDepuisUrlParDefaut();
            return new DonneesInitiales(
                    plateforme,
                    "Catalogue charge depuis l'URL CSV par defaut.",
                    "Compte initial : admin (administrateur). Inscrivez ensuite des membres depuis l'application."
            );
        } catch (IOException e) {
            return new DonneesInitiales(
                    creerPlateformeDemo(),
                    "Catalogue de demonstration charge (echec du chargement CSV automatique : " + e.getMessage() + ").",
                    "Comptes de demonstration : alice (joueur), bob (testeur), admin (administrateur)"
            );
        }
    }

    private static DonneesInitiales chargerDepuisSource(ChargeurCsv chargeur, String source) {
        try {
            Plateforme plateforme;
            if (source.startsWith("http://") || source.startsWith("https://")) {
                plateforme = chargeur.chargerDepuisUrl(source);
            } else {
                plateforme = chargeur.chargerDepuisFichier(source);
            }
            return new DonneesInitiales(
                    plateforme,
                    "Catalogue charge depuis " + source + ".",
                    "Compte initial : admin (administrateur). Inscrivez ensuite des membres depuis l'application."
            );
        } catch (IOException e) {
            return new DonneesInitiales(
                    creerPlateformeDemo(),
                    "Catalogue de demonstration charge (echec du chargement CSV depuis " + source + " : " + e.getMessage() + ").",
                    "Comptes de demonstration : alice (joueur), bob (testeur), admin (administrateur)"
            );
        }
    }

    private static List<Path> cheminsCsvPossibles() {
        return List.of(
                Path.of("vg_data.csv"),
                Path.of("data", "vg_data.csv")
        );
    }

    private static Plateforme creerPlateformeDemo() {
        Plateforme plateforme = new Plateforme();
        plateforme.inscrireJoueur("alice");
        plateforme.inscrireJoueur("charlie");
        plateforme.ajouterMembre(new Testeur("bob"));

        Jeu portal2 = new Jeu("Portal 2", "Puzzle", "Valve", "E10+");
        SupportJeu portal2Pc = new SupportJeu(portal2, "PC", 2011, "Valve", 4.1, 75, 95, 110, 8.8);
        SupportJeu portal2Ps3 = new SupportJeu(portal2, "PS3", 2011, "Valve", 1.5, 60, 94, 70, 8.7);
        portal2.ajouterSupport(portal2Pc);
        portal2.ajouterSupport(portal2Ps3);
        plateforme.ajouterJeu(portal2);

        Jeu hades = new Jeu("Hades", "Roguelike", "Supergiant Games", "T");
        SupportJeu hadesPc = new SupportJeu(hades, "PC", 2020, "Supergiant Games", 2.4, 55, 93, 90, 8.9);
        SupportJeu hadesSwitch = new SupportJeu(hades, "Switch", 2020, "Supergiant Games", 1.8, 40, 92, 65, 8.8);
        hades.ajouterSupport(hadesPc);
        hades.ajouterSupport(hadesSwitch);
        plateforme.ajouterJeu(hades);

        Jeu celeste = new Jeu("Celeste", "Plateforme", "Matt Makes Games", "E10+");
        SupportJeu celestePc = new SupportJeu(celeste, "PC", 2018, "Matt Makes Games", 1.2, 48, 91, 58, 8.6);
        celeste.ajouterSupport(celestePc);
        plateforme.ajouterJeu(celeste);

        plateforme.connecter("alice");
        plateforme.ajouterJeuPossedeAuMembreConnecte("Portal 2", "PC");
        plateforme.ajouterTempsDeJeuAuMembreConnecte("Portal 2", "PC", 6);
        plateforme.ajouterEvaluationAuMembreConnecte(
                "Portal 2",
                "PC",
                LocalDate.now().minusDays(2),
                "Excellent puzzle game, precis et inventif.",
                "1.0.0",
                9.0
        );
        plateforme.ajouterJeuPossedeAuMembreConnecte("Hades", "Switch");
        plateforme.ajouterTempsDeJeuAuMembreConnecte("Hades", "Switch", 8);
        plateforme.placerJetonsAuMembreConnecte("Hades", "Switch", 2);

        plateforme.connecter("charlie");
        plateforme.ajouterJeuPossedeAuMembreConnecte("Portal 2", "PC");
        plateforme.ajouterTempsDeJeuAuMembreConnecte("Portal 2", "PC", 2);
        plateforme.ajouterJeuPossedeAuMembreConnecte("Hades", "Switch");
        plateforme.ajouterTempsDeJeuAuMembreConnecte("Hades", "Switch", 4);
        plateforme.placerJetonsAuMembreConnecte("Hades", "Switch", 1);
        plateforme.voterUtiliteEvaluationAuMembreConnecte(
                plateforme.lireEvaluations("Portal 2", "PC").get(0),
                VoteUtilite.POSITIF
        );

        plateforme.connecter("bob");
        plateforme.ajouterJeuPossedeAuMembreConnecte("Hades", "Switch");
        plateforme.ajouterTempsDeJeuAuMembreConnecte("Hades", "Switch", 5);
        plateforme.ajouterJeuPossedeAuMembreConnecte("Portal 2", "PC");
        plateforme.ajouterTempsDeJeuAuMembreConnecte("Portal 2", "PC", 2);
        plateforme.deconnecter();

        return plateforme;
    }

    /**
     * Regroupe la plateforme initialisee et les messages d'information
     * affiches au demarrage.
     *
     * @param plateforme plateforme initialisee
     * @param msgCatalogue message de provenance du catalogue
     * @param msgComptes message sur les comptes disponibles
     */
    public record DonneesInitiales(
            Plateforme plateforme,
            String msgCatalogue,
            String msgComptes
    ) {
    }
}
