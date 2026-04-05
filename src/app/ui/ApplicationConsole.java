package app.ui;

import java.time.LocalDate;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import app.data.ChargeurCsv;
import app.model.CritereRechercheJeu;
import app.model.Administrateur;
import app.model.Evaluation;
import app.model.Jeu;
import app.model.Joueur;
import app.model.Membre;
import app.model.Plateforme;
import app.model.ProfilMembre;
import app.model.SupportJeu;
import app.model.StructureTestJeu;
import app.model.TestJeu;
import app.model.Testeur;
import app.model.VoteUtilite;

public class ApplicationConsole {
    private final Plateforme plateforme;
    private final String msgCatalogue;
    private final String msgComptes;
    private final Scanner scanner;
    private boolean quitter;

    public ApplicationConsole() {
        this(new String[0]);
    }

    public ApplicationConsole(String[] args) {
        Initialisation init = initialiserPlateforme(args);
        this.plateforme = init.plateforme();
        this.msgCatalogue = init.msgCatalogue();
        this.msgComptes = init.msgComptes();
        this.scanner = new Scanner(System.in);
        this.quitter = false;
    }

    public void lancer() {
        afficherAccueil();
        while (!quitter) {
            System.out.println();
            if (plateforme.getMembreConnecte() == null) {
                afficherMenuInvite();
                traiterChoixInvite(lireEntier("Choix : "));
            } else if (plateforme.getMembreConnecte() instanceof Administrateur) {
                afficherMenuAdministrateur();
                traiterChoixAdministrateur(lireEntier("Choix : "));
            } else if (plateforme.getMembreConnecte() instanceof Testeur) {
                afficherMenuTesteur();
                traiterChoixTesteur(lireEntier("Choix : "));
            } else {
                afficherMenuJoueur();
                traiterChoixJoueur(lireEntier("Choix : "));
            }
        }
        System.out.println("Fermeture de l'application.");
    }

    private void afficherAccueil() {
        System.out.println("=== Plateforme de jeux ===");
        System.out.println(msgComptes);
        System.out.println(msgCatalogue);
    }

    private void afficherMenuInvite() {
        System.out.println("=== Menu principal / invite ===");
        System.out.println("1. Se connecter");
        System.out.println("2. S'inscrire comme joueur");
        System.out.println("3. Chercher un jeu");
        System.out.println("4. Afficher un jeu");
        System.out.println("5. Lire les evaluations d'un jeu");
        System.out.println("0. Quitter");
    }

    private void afficherMenuJoueur() {
        System.out.println("=== Menu joueur (" + plateforme.getMembreConnecte().getPseudo() + ") ===");
        afficherOptionsCommunesConnecte();
        System.out.println("12. Se desinscrire");
        System.out.println("13. Se deconnecter");
        System.out.println("0. Quitter");
    }

    private void afficherMenuTesteur() {
        System.out.println("=== Menu testeur (" + plateforme.getMembreConnecte().getPseudo() + ") ===");
        afficherOptionsCommunesConnecte();
        System.out.println("12. Rechercher un test a realiser");
        System.out.println("13. Ajouter un test");
        System.out.println("14. Signaler une evaluation");
        System.out.println("15. Se deconnecter");
        System.out.println("0. Quitter");
    }

    private void afficherMenuAdministrateur() {
        System.out.println("=== Menu administrateur (" + plateforme.getMembreConnecte().getPseudo() + ") ===");
        afficherOptionsCommunesConnecte();
        afficherOptionsModeration();
        System.out.println("22. Se deconnecter");
        System.out.println("0. Quitter");
    }

    private void afficherOptionsCommunesConnecte() {
        System.out.println("1. Chercher un jeu");
        System.out.println("2. Afficher un jeu");
        System.out.println("3. Afficher un membre");
        System.out.println("4. Afficher mon profil");
        System.out.println("5. Ajouter un jeu possede");
        System.out.println("6. Ajouter du temps de jeu");
        System.out.println("7. Lire les evaluations d'un jeu");
        System.out.println("8. Lire le test d'un jeu");
        System.out.println("9. Ajouter une evaluation");
        System.out.println("10. Evaluer l'utilite d'une evaluation");
        System.out.println("11. Gerer mes jetons");
    }

    private void afficherOptionsModeration() {
        System.out.println("12. Rechercher un test a realiser");
        System.out.println("13. Ajouter un test");
        System.out.println("14. Signaler une evaluation");
        System.out.println("15. Supprimer une evaluation");
        System.out.println("16. Bloquer un membre");
        System.out.println("17. Debloquer un membre");
        System.out.println("18. Promouvoir un joueur en testeur");
        System.out.println("19. Promouvoir un testeur en administrateur");
        System.out.println("20. Desinscrire un joueur");
        System.out.println("21. Inscrire un membre");
    }

    private void traiterChoixInvite(int choix) {
        switch (choix) {
            case 1 -> executer(this::seConnecter);
            case 2 -> executer(this::inscrireJoueur);
            case 3 -> executer(this::chercherJeux);
            case 4 -> executer(this::afficherJeu);
            case 5 -> executer(this::lireEvaluations);
            case 0 -> quitter = true;
            default -> afficherChoixInvalide();
        }
    }

    private void traiterChoixJoueur(int choix) {
        if (traiterChoixCommunConnecte(choix)) {
            return;
        }
        switch (choix) {
            case 12 -> executer(this::seDesinscrire);
            case 13 -> executer(this::seDeconnecter);
            case 0 -> quitter = true;
            default -> afficherChoixInvalide();
        }
    }

    private void traiterChoixTesteur(int choix) {
        if (traiterChoixCommunConnecte(choix)) {
            return;
        }
        switch (choix) {
            case 12 -> executer(this::rechercherTestsARealiser);
            case 13 -> executer(this::ajouterTest);
            case 14 -> executer(this::signalerEvaluation);
            case 15 -> executer(this::seDeconnecter);
            case 0 -> quitter = true;
            default -> afficherChoixInvalide();
        }
    }

    private void traiterChoixAdministrateur(int choix) {
        if (traiterChoixCommunConnecte(choix)) {
            return;
        }
        switch (choix) {
            case 12 -> executer(this::rechercherTestsARealiser);
            case 13 -> executer(this::ajouterTest);
            case 14 -> executer(this::signalerEvaluation);
            case 15 -> executer(this::supprimerEvaluation);
            case 16 -> executer(this::bloquerMembre);
            case 17 -> executer(this::debloquerMembre);
            case 18 -> executer(this::promouvoirJoueurEnTesteur);
            case 19 -> executer(this::promouvoirTesteurEnAdministrateur);
            case 20 -> executer(this::desinscrireJoueurAdmin);
            case 21 -> executer(this::inscrireMembreAdmin);
            case 22 -> executer(this::seDeconnecter);
            case 0 -> quitter = true;
            default -> afficherChoixInvalide();
        }
    }

    private boolean traiterChoixCommunConnecte(int choix) {
        switch (choix) {
            case 1 -> executer(this::chercherJeux);
            case 2 -> executer(this::afficherJeu);
            case 3 -> executer(this::afficherMembre);
            case 4 -> executer(this::afficherProfilConnecte);
            case 5 -> executer(this::ajouterJeuPossede);
            case 6 -> executer(this::ajouterTempsDeJeu);
            case 7 -> executer(this::lireEvaluations);
            case 8 -> executer(this::lireTest);
            case 9 -> executer(this::ajouterEvaluation);
            case 10 -> executer(this::evaluerUtiliteEvaluation);
            case 11 -> executer(this::gererJetons);
            default -> {
                return false;
            }
        }
        return true;
    }

    private void seConnecter() {
        String pseudo = lireTexte("Pseudo : ");
        Membre membre = plateforme.connecter(pseudo);
        System.out.println("Connexion reussie en tant que " + membre.getTypeProfil() + ".");
    }

    private void inscrireJoueur() {
        String pseudo = lireTexte("Choisissez un pseudo : ");
        Joueur joueur = plateforme.inscrireJoueur(pseudo);
        System.out.println("Inscription reussie pour " + joueur.getPseudo() + " en tant que joueur.");
    }

    private void inscrireMembreAdmin() {
        String pseudo = lireTexte("Choisissez un pseudo : ");
        ProfilMembre profil = lireProfilMembre();
        Membre membre = plateforme.inscrireMembre(pseudo, profil);
        System.out.println("Inscription reussie pour " + membre.getPseudo()
                + " en tant que " + membre.getTypeProfil() + ".");
    }

    private void seDeconnecter() {
        plateforme.deconnecter();
        System.out.println("Deconnexion effectuee.");
    }

    private void chercherJeux() {
        CritereRechercheJeu critere = lireCritereRecherche();
        List<Jeu> jeux = plateforme.chercherJeux(critere);
        if (jeux.isEmpty()) {
            System.out.println("Aucun jeu trouve.");
            return;
        }

        for (Jeu jeu : jeux) {
            System.out.println("- " + jeu.getNom() + " (" + jeu.getCategorie() + ")");
            for (SupportJeu support : plateforme.rechercherSupportsCorrespondants(jeu, critere)) {
                System.out.println("  -> " + support.getPlateforme() + ", test=" + (support.aUnTest() ? "oui" : "non")
                        + ", jetons=" + support.getTotalJetons());
            }
        }
    }

    private void afficherJeu() {
        String nom = lireTexte("Nom exact du jeu : ");
        System.out.println(plateforme.afficherInfosJeu(nom));
    }

    private void afficherProfilConnecte() {
        String nom = plateforme.getMembreConnecte().getPseudo();
        System.out.println(plateforme.afficherInfosMembre(nom));
    }

    private void afficherMembre() {
        String pseudo = lireTexte("Pseudo du membre : ");
        System.out.println(plateforme.afficherInfosMembre(pseudo));
    }

    private void ajouterJeuPossede() {
        String nomJeu = lireTexte("Nom du jeu : ");
        String plateformeSupport = lireTexte("Plateforme : ");
        plateforme.ajouterJeuPossedeAuMembreConnecte(nomJeu, plateformeSupport);
        System.out.println("Jeu ajoute a votre bibliotheque.");
    }

    private void ajouterTempsDeJeu() {
        String nomJeu = lireTexte("Nom du jeu : ");
        String plateformeSupport = lireTexte("Plateforme : ");
        int heures = lireEntierNonNegatif("Nombre d'heures a ajouter : ", "Le temps de jeu ne peut pas etre negatif.");
        int total = plateforme.ajouterTempsDeJeuAuMembreConnecte(nomJeu, plateformeSupport, heures);
        System.out.println("Temps de jeu total sur ce support : " + total + " heure(s).");
    }

    private void lireEvaluations() {
        ChoixSupport choix = lireChoixSupport();
        List<Evaluation> evaluations = plateforme.lireEvaluations(choix.nomJeu(), choix.support());
        afficherEvaluations(evaluations);
    }

    private void lireTest() {
        ChoixSupport choix = lireChoixSupport();
        TestJeu test = plateforme.lireTestAuMembreConnecte(choix.nomJeu(), choix.support());
        afficherTest(test);
    }

    private void ajouterEvaluation() {
        ChoixSupport choix = lireChoixSupport();
        double note = lireNoteSurDix("Note globale (/10) : ");
        String version = lireTexte("Version / build : ");
        String texte = lireTexte("Texte de l'evaluation : ");
        plateforme.ajouterEvaluationAuMembreConnecte(
                choix.nomJeu(),
                choix.support(),
                LocalDate.now(),
                texte,
                version,
                note
        );
        System.out.println("Evaluation publiee. Duree minimale requise : "
                + Jeu.DUREE_MINIMALE_EVALUATION_HEURES + " heure(s).");
    }

    private void evaluerUtiliteEvaluation() {
        Evaluation evaluation = selectionnerEvaluation();
        if (evaluation == null) {
            return;
        }

        System.out.println("1. Positif");
        System.out.println("2. Neutre");
        System.out.println("3. Negatif");
        int choix = lireEntier("Votre vote : ");
        VoteUtilite vote = lireVote(choix);
        if (vote == null) {
            afficherChoixInvalide();
            return;
        }

        plateforme.voterUtiliteEvaluationAuMembreConnecte(evaluation, vote);
        System.out.println("Vote enregistre.");
    }

    private void gererJetons() {
        boolean retour = false;
        while (!retour) {
            Joueur joueur = (Joueur) plateforme.getMembreConnecte();
            System.out.println("=== Gestion des jetons ===");
            System.out.println("Jetons disponibles : " + joueur.getJetons());
            System.out.println("1. Voir les supports demandes par jetons");
            System.out.println("2. Placer des jetons");
            System.out.println("3. Retirer des jetons");
            System.out.println("0. Retour");

            int choix = lireEntier("Choix : ");
            switch (choix) {
                case 1 -> afficherSupportsDemandes();
                case 2 -> placerJetons();
                case 3 -> retirerJetons();
                case 0 -> retour = true;
                default -> afficherChoixInvalide();
            }
        }
    }

    private void afficherSupportsDemandes() {
        List<SupportJeu> supports = plateforme.rechercherTestsARealiser();
        if (supports.isEmpty()) {
            System.out.println("Aucun support sans test avec des jetons places.");
            return;
        }

        for (SupportJeu support : supports) {
            System.out.println("- " + support.getJeu().getNom() + " / " + support.getPlateforme()
                    + " : " + support.getTotalJetons() + " jeton(s)");
        }
    }

    private void placerJetons() {
        ChoixSupport choix = lireChoixSupport();
        int nbJetons = lireEntierStrictementPositif("Nombre de jetons a placer : ", "Le nombre de jetons doit etre strictement positif.");
        plateforme.placerJetonsAuMembreConnecte(choix.nomJeu(), choix.support(), nbJetons);
        System.out.println("Jetons places.");
    }

    private void retirerJetons() {
        ChoixSupport choix = lireChoixSupport();
        int nbJetons = lireEntierStrictementPositif("Nombre de jetons a retirer : ", "Le nombre de jetons doit etre strictement positif.");
        plateforme.retirerJetonsAuMembreConnecte(choix.nomJeu(), choix.support(), nbJetons);
        System.out.println("Jetons retires.");
    }

    private void rechercherTestsARealiser() {
        List<SupportJeu> supports = plateforme.rechercherTestsARealiserPourMembreConnecte();
        if (supports.isEmpty()) {
            System.out.println("Aucun test realisable actuellement avec votre bibliotheque et votre temps de jeu.");
            return;
        }

        System.out.println("Supports a tester :");
        for (SupportJeu support : supports) {
            System.out.println("- " + support.getJeu().getNom() + " / " + support.getPlateforme()
                    + " (" + support.getTotalJetons() + " jeton(s))");
        }
    }

    private void ajouterTest() {
        ChoixSupport choix = lireChoixSupport();
        String version = lireTexte("Version / build testee : ");
        String texte = lireTexte("Texte du test : ");
        Map<String, Integer> notes = saisirNotes("Categorie testee", true);
        List<String> pointsForts = saisirListe("Point fort");
        List<String> pointsFaibles = saisirListe("Point faible");

        String conditions = lireTexte("Conditions du test (optionnel) : ");
        List<String> jeuxSimilaires = saisirListe("Jeu similaire conseille");
        Map<String, Integer> notesGenre = saisirNotes("Categorie specifique au genre", false);

        StructureTestJeu structure = new StructureTestJeu(
                notes,
                pointsForts,
                pointsFaibles,
                conditions,
                jeuxSimilaires,
                notesGenre
        );
        TestJeu test = plateforme.ajouterTestAuMembreConnecte(
                choix.nomJeu(),
                choix.support(),
                LocalDate.now(),
                texte,
                version,
                structure
        );
        System.out.println("Test publie. Les jetons du support ont ete liberes.");
        afficherTest(test);
    }

    private void signalerEvaluation() {
        Evaluation evaluation = selectionnerEvaluation();
        if (evaluation == null) {
            return;
        }

        plateforme.signalerEvaluationAuMembreConnecte(evaluation);
        System.out.println("Evaluation signalee.");
    }

    private void supprimerEvaluation() {
        Evaluation evaluation = selectionnerEvaluation();
        if (evaluation == null) {
            return;
        }

        plateforme.supprimerEvaluationAuMembreConnecte(evaluation);
        System.out.println("Evaluation supprimee.");
    }

    private void bloquerMembre() {
        Administrateur administrateur = (Administrateur) plateforme.getMembreConnecte();
        String pseudo = lireTexte("Pseudo du membre a bloquer : ");
        administrateur.bloquerMembre(plateforme.consulterMembre(pseudo));
        System.out.println("Membre bloque.");
    }

    private void debloquerMembre() {
        Administrateur administrateur = (Administrateur) plateforme.getMembreConnecte();
        String pseudo = lireTexte("Pseudo du membre a debloquer : ");
        administrateur.debloquerMembre(plateforme.consulterMembre(pseudo));
        System.out.println("Membre debloque.");
    }

    private void promouvoirJoueurEnTesteur() {
        String pseudo = lireTexte("Pseudo du joueur a promouvoir : ");
        plateforme.promouvoirJoueur(pseudo);
        System.out.println("Joueur promu testeur.");
    }

    private void promouvoirTesteurEnAdministrateur() {
        String pseudo = lireTexte("Pseudo du testeur a promouvoir : ");
        plateforme.promouvoirTesteur(pseudo);
        System.out.println("Testeur promu administrateur.");
    }

    private void desinscrireJoueurAdmin() {
        String pseudo = lireTexte("Pseudo du joueur a desinscrire : ");
        if (!plateforme.desinscrireJoueur(pseudo)) {
            throw new IllegalArgumentException("Ce pseudo ne correspond pas a un joueur simple desinscriptible");
        }
        System.out.println("Joueur desinscrit.");
    }

    private void seDesinscrire() {
        String pseudo = plateforme.getMembreConnecte().getPseudo();
        if (!plateforme.desinscrireJoueur(pseudo)) {
            throw new IllegalStateException("Seul un joueur simple peut se desinscrire");
        }
        System.out.println("Desinscription effectuee.");
    }

    private ChoixSupport lireChoixSupport() {
        String nomJeu = lireTexte("Nom du jeu : ");
        String support = lireTexte("Plateforme : ");
        return new ChoixSupport(nomJeu, support);
    }

    private Evaluation selectionnerEvaluation() {
        ChoixSupport choix = lireChoixSupport();
        List<Evaluation> evaluations = plateforme.lireEvaluations(choix.nomJeu(), choix.support());
        if (evaluations.isEmpty()) {
            System.out.println("Aucune evaluation disponible pour ce support.");
            return null;
        }

        afficherEvaluations(evaluations);
        int index = lireEntier("Numero de l'evaluation : ");
        if (index < 1 || index > evaluations.size()) {
            throw new IllegalArgumentException("Numero d'evaluation invalide");
        }
        return evaluations.get(index - 1);
    }

    private void afficherEvaluations(List<Evaluation> evaluations) {
        if (evaluations.isEmpty()) {
            System.out.println("Aucune evaluation disponible.");
            return;
        }

        for (int i = 0; i < evaluations.size(); i++) {
            Evaluation evaluation = evaluations.get(i);
            System.out.println((i + 1) + ". " + evaluation.getAuteur().getPseudo()
                    + " | note=" + evaluation.getNoteGlobale()
                    + " | utilite=" + evaluation.getScoreUtilite()
                    + " | signalee=" + evaluation.estSignalee());
            System.out.println("   " + evaluation.getTexte());
        }
    }

    private void afficherTest(TestJeu test) {
        System.out.println("Test de " + test.getAuteur().getPseudo()
                + " | version=" + test.getVersionBuild()
                + " | date=" + test.getDate());
        System.out.println(test.getTexte());

        if (!test.getNotesParCategorie().isEmpty()) {
            System.out.println("Notes par categorie :");
            test.getNotesParCategorie().forEach((categorie, note) ->
                    System.out.println(" - " + categorie + " : " + note + "/100"));
        }

        if (!test.getNotesSpecifiquesAuGenre().isEmpty()) {
            System.out.println("Notes specifiques au genre :");
            test.getNotesSpecifiquesAuGenre().forEach((categorie, note) ->
                    System.out.println(" - " + categorie + " : " + note + "/100"));
        }

        if (!test.getPointsForts().isEmpty()) {
            System.out.println("Points forts :");
            for (String pointFort : test.getPointsForts()) {
                System.out.println(" - " + pointFort);
            }
        }

        if (!test.getPointsFaibles().isEmpty()) {
            System.out.println("Points faibles :");
            for (String pointFaible : test.getPointsFaibles()) {
                System.out.println(" - " + pointFaible);
            }
        }

        if (!test.getConditionsTest().isEmpty()) {
            System.out.println("Conditions du test : " + test.getConditionsTest());
        }

        if (!test.getJeuxSimilaires().isEmpty()) {
            System.out.println("Jeux similaires conseilles :");
            for (String jeuSimilaire : test.getJeuxSimilaires()) {
                System.out.println(" - " + jeuSimilaire);
            }
        }
    }

    private VoteUtilite lireVote(int choix) {
        return switch (choix) {
            case 1 -> VoteUtilite.POSITIF;
            case 2 -> VoteUtilite.NEUTRE;
            case 3 -> VoteUtilite.NEGATIF;
            default -> null;
        };
    }

    private Map<String, Integer> saisirNotes(String libelle, boolean obligatoire) {
        Map<String, Integer> notes = new LinkedHashMap<>();
        int nbNotes = 0;
        while (true) {
            String categorie = lireTexte(libelle + " (laisser vide pour terminer) : ");
            if (categorie.isEmpty()) {
                if (obligatoire && nbNotes == 0) {
                    System.out.println("Ajoutez au moins une note.");
                    continue;
                }
                return notes;
            }
            int note = lireNoteSurCent("Note (/100) : ");
            notes.put(categorie, note);
            nbNotes++;
        }
    }

    private List<String> saisirListe(String libelle) {
        List<String> valeurs = new ArrayList<>();
        while (true) {
            String texte = lireTexte(libelle + " (laisser vide pour terminer) : ");
            if (texte.isEmpty()) {
                return valeurs;
            }
            valeurs.add(texte);
        }
    }

    private CritereRechercheJeu lireCritereRecherche() {
        System.out.println("1. Recherche rapide");
        System.out.println("2. Recherche guidee par criteres");
        int choix = lireEntier("Mode de recherche : ");
        if (choix == 1) {
            return new CritereRechercheJeu(
                    lireTexte("Critere de recherche : "),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }
        if (choix != 2) {
            throw new IllegalArgumentException("Mode de recherche invalide");
        }

        String texteLibre = lireTexte("Texte libre (optionnel) : ");
        String categorie = choisirOption("Categorie", plateforme.getCategoriesDisponibles());
        String editeur = choisirOption("Editeur", plateforme.getEditeursDisponibles());
        String rating = choisirOption("Rating", plateforme.getRatingsDisponibles());
        String plateformeSupport = choisirOption("Plateforme", plateforme.getPlateformesDisponibles());
        String developpeur = choisirOption("Developpeur", plateforme.getDeveloppeursDisponibles());
        Boolean testDisponible = choisirDisponibiliteTest();

        return new CritereRechercheJeu(
                videVersNull(texteLibre),
                categorie,
                editeur,
                rating,
                plateformeSupport,
                developpeur,
                testDisponible
        );
    }

    private String choisirOption(String libelle, List<String> options) {
        if (options.isEmpty()) {
            return null;
        }
        System.out.println("Selection " + libelle + " :");
        System.out.println("0. Tous");
        for (int i = 0; i < options.size(); i++) {
            System.out.println((i + 1) + ". " + options.get(i));
        }
        int choix = lireEntier("Choix " + libelle + " : ");
        if (choix == 0) {
            return null;
        }
        if (choix < 0 || choix > options.size()) {
            throw new IllegalArgumentException("Choix invalide pour " + libelle);
        }
        return options.get(choix - 1);
    }

    private Boolean choisirDisponibiliteTest() {
        System.out.println("Filtrer sur la presence d'un test :");
        System.out.println("0. Tous");
        System.out.println("1. Avec test");
        System.out.println("2. Sans test");
        int choix = lireEntier("Choix test : ");
        return switch (choix) {
            case 0 -> null;
            case 1 -> true;
            case 2 -> false;
            default -> throw new IllegalArgumentException("Choix test invalide");
        };
    }

    private ProfilMembre lireProfilMembre() {
        System.out.println("Profil a inscrire :");
        System.out.println("1. Joueur");
        System.out.println("2. Testeur");
        System.out.println("3. Administrateur");
        int choix = lireEntier("Choix profil : ");
        return switch (choix) {
            case 1 -> ProfilMembre.JOUEUR;
            case 2 -> ProfilMembre.TESTEUR;
            case 3 -> ProfilMembre.ADMINISTRATEUR;
            default -> throw new IllegalArgumentException("Choix de profil invalide");
        };
    }

    private String videVersNull(String valeur) {
        return valeur == null || valeur.isBlank() ? null : valeur;
    }

    private String lireTexte(String invite) {
        System.out.print(invite);
        return scanner.nextLine().trim();
    }

    private int lireEntier(String invite) {
        while (true) {
            String valeur = lireTexte(invite);
            try {
                return Integer.parseInt(valeur);
            } catch (NumberFormatException e) {
                System.out.println("Saisie invalide : entrez un entier.");
            }
        }
    }

    private double lireDouble(String invite) {
        while (true) {
            String valeur = lireTexte(invite);
            try {
                return Double.parseDouble(valeur);
            } catch (NumberFormatException e) {
                System.out.println("Saisie invalide : entrez un nombre.");
            }
        }
    }

    private int lireEntierNonNegatif(String invite, String messageErreur) {
        while (true) {
            int valeur = lireEntier(invite);
            if (valeur < 0) {
                System.out.println(messageErreur);
                continue;
            }
            return valeur;
        }
    }

    private int lireEntierStrictementPositif(String invite, String messageErreur) {
        while (true) {
            int valeur = lireEntier(invite);
            if (valeur <= 0) {
                System.out.println(messageErreur);
                continue;
            }
            return valeur;
        }
    }

    private double lireNoteSurDix(String invite) {
        while (true) {
            double note = lireDouble(invite);
            if (note < 0.0 || note > 10.0) {
                System.out.println("Note hors intervalle : la note doit etre comprise entre 0 et 10.");
                continue;
            }
            return note;
        }
    }

    private int lireNoteSurCent(String invite) {
        while (true) {
            int note = lireEntier(invite);
            if (note < 0 || note > 100) {
                System.out.println("Note hors intervalle : la note doit etre comprise entre 0 et 100.");
                continue;
            }
            return note;
        }
    }

    private void afficherChoixInvalide() {
        System.out.println("Choix invalide.");
    }

    private void executer(Action action) {
        try {
            action.executer();
        } catch (RuntimeException e) {
            String message = e.getMessage();
            if (message == null || message.isBlank()) {
                System.out.println("Erreur : une erreur inattendue s'est produite.");
                return;
            }
            System.out.println("Erreur : " + message);
        }
    }

    private Initialisation initialiserPlateforme(String[] args) {
        ChargeurCsv chargeur = new ChargeurCsv();

        if (args != null && args.length > 0 && !args[0].isBlank()) {
            return chargerPlateformeDepuisSource(chargeur, args[0].trim());
        }

        for (Path chemin : cheminsCsvPossibles()) {
            if (Files.exists(chemin)) {
                return chargerPlateformeDepuisSource(chargeur, chemin.toString());
            }
        }

        try {
            Plateforme p = chargeur.chargerDepuisUrlParDefaut();
            return new Initialisation(
                    p,
                    "Catalogue charge depuis l'URL CSV par defaut.",
                    "Compte initial : admin (administrateur). Inscrivez ensuite des membres depuis le menu."
            );
        } catch (IOException e) {
            return new Initialisation(
                creerPlateformeDemonstration(),
                    "Catalogue de demonstration charge (echec du chargement CSV automatique : " + e.getMessage() + ").",
                    "Comptes de demonstration : alice (joueur), bob (testeur), admin (administrateur)"
            );
        }
    }

    private Initialisation chargerPlateformeDepuisSource(ChargeurCsv chargeur, String source) {
        try {
            Plateforme p;
            if (source.startsWith("http://") || source.startsWith("https://")) {
                p = chargeur.chargerDepuisUrl(source);
            } else {
                p = chargeur.chargerDepuisFichier(source);
            }
            return new Initialisation(
                    p,
                    "Catalogue charge depuis " + source + ".",
                    "Compte initial : admin (administrateur). Inscrivez ensuite des membres depuis le menu."
            );
        } catch (IOException e) {
            return new Initialisation(
                    creerPlateformeDemonstration(),
                    "Catalogue de demonstration charge (echec du chargement CSV depuis " + source + " : " + e.getMessage() + ").",
                    "Comptes de demonstration : alice (joueur), bob (testeur), admin (administrateur)"
            );
        }
    }

    private List<Path> cheminsCsvPossibles() {
        return List.of(
                Path.of("vg_data.csv"),
                Path.of("data", "vg_data.csv")
        );
    }

    private Plateforme creerPlateformeDemonstration() {
        Plateforme p = new Plateforme();
        p.inscrireJoueur("alice");
        p.inscrireJoueur("charlie");
        p.ajouterMembre(new Testeur("bob"));

        Jeu portal2 = new Jeu("Portal 2", "Puzzle", "Valve", "E10+");
        SupportJeu portal2Pc = new SupportJeu(portal2, "PC", 2011, "Valve", 4.1, 75, 95, 110, 8.8);
        SupportJeu portal2Ps3 = new SupportJeu(portal2, "PS3", 2011, "Valve", 1.5, 60, 94, 70, 8.7);
        portal2.ajouterSupport(portal2Pc);
        portal2.ajouterSupport(portal2Ps3);
        p.ajouterJeu(portal2);

        Jeu hades = new Jeu("Hades", "Roguelike", "Supergiant Games", "T");
        SupportJeu hadesPc = new SupportJeu(hades, "PC", 2020, "Supergiant Games", 2.4, 55, 93, 90, 8.9);
        SupportJeu hadesSwitch = new SupportJeu(hades, "Switch", 2020, "Supergiant Games", 1.8, 40, 92, 65, 8.8);
        hades.ajouterSupport(hadesPc);
        hades.ajouterSupport(hadesSwitch);
        p.ajouterJeu(hades);

        Jeu celeste = new Jeu("Celeste", "Plateforme", "Matt Makes Games", "E10+");
        SupportJeu celestePc = new SupportJeu(celeste, "PC", 2018, "Matt Makes Games", 1.2, 48, 91, 58, 8.6);
        celeste.ajouterSupport(celestePc);
        p.ajouterJeu(celeste);

        p.connecter("alice");
        p.ajouterJeuPossedeAuMembreConnecte("Portal 2", "PC");
        p.ajouterTempsDeJeuAuMembreConnecte("Portal 2", "PC", 6);
        p.ajouterEvaluationAuMembreConnecte(
                "Portal 2",
                "PC",
                LocalDate.now().minusDays(2),
                "Excellent puzzle game, precis et inventif.",
                "1.0.0",
                9.0
        );
        p.ajouterJeuPossedeAuMembreConnecte("Hades", "Switch");
        p.ajouterTempsDeJeuAuMembreConnecte("Hades", "Switch", 8);
        p.placerJetonsAuMembreConnecte("Hades", "Switch", 2);

        p.connecter("charlie");
        p.ajouterJeuPossedeAuMembreConnecte("Portal 2", "PC");
        p.ajouterTempsDeJeuAuMembreConnecte("Portal 2", "PC", 2);
        p.ajouterJeuPossedeAuMembreConnecte("Hades", "Switch");
        p.ajouterTempsDeJeuAuMembreConnecte("Hades", "Switch", 4);
        p.placerJetonsAuMembreConnecte("Hades", "Switch", 1);
        p.voterUtiliteEvaluationAuMembreConnecte(
                p.lireEvaluations("Portal 2", "PC").get(0),
                VoteUtilite.POSITIF
        );

        p.connecter("bob");
        p.ajouterJeuPossedeAuMembreConnecte("Hades", "Switch");
        p.ajouterTempsDeJeuAuMembreConnecte("Hades", "Switch", 5);
        p.ajouterJeuPossedeAuMembreConnecte("Portal 2", "PC");
        p.ajouterTempsDeJeuAuMembreConnecte("Portal 2", "PC", 2);
        p.deconnecter();

        return p;
    }

    @FunctionalInterface
    private interface Action {
        void executer();
    }

    @FunctionalInterface
    private interface AjoutNote {
        void ajouter(String categorie, int note);
    }

    private record Initialisation(
            Plateforme plateforme,
            String msgCatalogue,
            String msgComptes
    ) {
    }

    private record ChoixSupport(String nomJeu, String support) {
    }
}
