package app.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import app.bootstrap.InitialiseurPlateforme;
import app.model.Administrateur;
import app.model.CritereRechercheJeu;
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

public class ApplicationGraphique {
    private final Plateforme plateforme;
    private final String msgCatalogue;
    private final String msgComptes;

    private JFrame fenetre;
    private JLabel infoCompte;
    private JLabel infoCatalogue;
    private JPanel panneauActions;
    private JTextArea sortie;

    public ApplicationGraphique(String[] args) {
        InitialiseurPlateforme.DonneesInitiales init = InitialiseurPlateforme.charger(args);
        this.plateforme = init.plateforme();
        this.msgCatalogue = init.msgCatalogue();
        this.msgComptes = init.msgComptes();
    }

    public void lancer() {
        SwingUtilities.invokeLater(this::creerFenetre);
    }

    private void creerFenetre() {
        appliquerLookAndFeel();

        fenetre = new JFrame("Plateforme de jeux video");
        fenetre.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        fenetre.setMinimumSize(new Dimension(1100, 720));
        fenetre.setLayout(new BorderLayout(12, 12));

        JPanel enTete = new JPanel(new GridLayout(2, 1, 0, 4));
        enTete.setBorder(BorderFactory.createEmptyBorder(12, 12, 0, 12));
        infoCompte = new JLabel();
        infoCatalogue = new JLabel();
        enTete.add(infoCompte);
        enTete.add(infoCatalogue);

        panneauActions = new JPanel();
        panneauActions.setLayout(new BoxLayout(panneauActions, BoxLayout.Y_AXIS));
        JScrollPane scrollActions = new JScrollPane(panneauActions);
        scrollActions.setPreferredSize(new Dimension(290, 0));
        scrollActions.setBorder(BorderFactory.createTitledBorder("Actions"));

        sortie = new JTextArea();
        sortie.setEditable(false);
        sortie.setLineWrap(true);
        sortie.setWrapStyleWord(true);
        sortie.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        JScrollPane scrollSortie = new JScrollPane(sortie);
        scrollSortie.setBorder(BorderFactory.createTitledBorder("Affichage"));

        fenetre.add(enTete, BorderLayout.NORTH);
        fenetre.add(scrollActions, BorderLayout.WEST);
        fenetre.add(scrollSortie, BorderLayout.CENTER);

        rafraichirInterface();
        afficherAccueil();

        fenetre.setLocationRelativeTo(null);
        fenetre.setVisible(true);
    }

    private void appliquerLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // fallback silencieux
        }
    }

    private void rafraichirInterface() {
        Membre membre = plateforme.getMembreConnecte();
        String texteCompte = membre == null
                ? "Mode invite"
                : "Connecte : " + membre.getPseudo() + " (" + membre.getTypeProfil() + ")";
        infoCompte.setText(texteCompte + "  |  " + msgComptes);
        infoCatalogue.setText(msgCatalogue);

        panneauActions.removeAll();
        ajouterActionsInvite();
        if (membre != null) {
            ajouterActionsConnecte();
            if (membre instanceof Administrateur) {
                ajouterActionsAdmin();
            } else if (membre instanceof Testeur) {
                ajouterActionsTesteur();
            } else {
                ajouterActionsJoueur();
            }
        }
        panneauActions.revalidate();
        panneauActions.repaint();
    }

    private void ajouterActionsInvite() {
        ajouterBloc("Acces",
                bouton("Se connecter", this::seConnecter),
                bouton("S'inscrire comme joueur", this::inscrireJoueur),
                bouton("Chercher un jeu", this::chercherJeux),
                bouton("Afficher un jeu", this::afficherJeu),
                bouton("Lire les evaluations", this::lireEvaluations));
    }

    private void ajouterActionsConnecte() {
        ajouterBloc("Espace membre",
                bouton("Chercher un jeu", this::chercherJeux),
                bouton("Afficher un jeu", this::afficherJeu),
                bouton("Afficher un membre", this::afficherMembre),
                bouton("Afficher mon profil", this::afficherProfil),
                bouton("Ajouter un jeu possede", this::ajouterJeuPossede),
                bouton("Ajouter du temps de jeu", this::ajouterTemps),
                bouton("Lire les evaluations", this::lireEvaluations),
                bouton("Lire le test", this::lireTest),
                bouton("Ajouter une evaluation", this::ajouterEvaluation),
                bouton("Evaluer une evaluation", this::voterEvaluation),
                bouton("Voir les demandes de test", this::voirDemandesTests),
                bouton("Placer des jetons", this::placerJetons),
                bouton("Retirer des jetons", this::retirerJetons));
    }

    private void ajouterActionsJoueur() {
        ajouterBloc("Compte joueur",
                bouton("Se desinscrire", this::seDesinscrire),
                bouton("Se deconnecter", this::seDeconnecter));
    }

    private void ajouterActionsTesteur() {
        ajouterBloc("Compte testeur",
                bouton("Tests a realiser", this::rechercherTests),
                bouton("Ajouter un test", this::ajouterTest),
                bouton("Signaler une evaluation", this::signalerEvaluation),
                bouton("Se deconnecter", this::seDeconnecter));
    }

    private void ajouterActionsAdmin() {
        ajouterBloc("Administration",
                bouton("Tests a realiser", this::rechercherTests),
                bouton("Ajouter un test", this::ajouterTest),
                bouton("Signaler une evaluation", this::signalerEvaluation),
                bouton("Supprimer une evaluation", this::supprimerEvaluation),
                bouton("Bloquer un membre", this::bloquerMembre),
                bouton("Debloquer un membre", this::debloquerMembre),
                bouton("Promouvoir un joueur", this::promouvoirJoueur),
                bouton("Promouvoir un testeur", this::promouvoirTesteur),
                bouton("Desinscrire un joueur", this::desinscrireJoueur),
                bouton("Inscrire un membre", this::inscrireMembreAdmin),
                bouton("Se deconnecter", this::seDeconnecter));
    }

    private void ajouterBloc(String titre, JButton... boutons) {
        JPanel bloc = new JPanel(new GridLayout(0, 1, 0, 6));
        bloc.setBorder(BorderFactory.createTitledBorder(titre));
        for (JButton bouton : boutons) {
            bloc.add(bouton);
        }
        JPanel conteneur = new JPanel(new BorderLayout());
        conteneur.add(bloc, BorderLayout.NORTH);
        conteneur.setMaximumSize(new Dimension(Integer.MAX_VALUE, bloc.getPreferredSize().height + 12));
        panneauActions.add(conteneur);
    }

    private JButton bouton(String texte, ActionUi action) {
        JButton bouton = new JButton(texte);
        bouton.addActionListener(e -> executer(action));
        return bouton;
    }

    private void executer(ActionUi action) {
        try {
            action.executer();
            rafraichirInterface();
        } catch (RuntimeException e) {
            String message = e.getMessage();
            if (message == null || message.isBlank()) {
                message = "Une erreur inattendue s'est produite.";
            }
            JOptionPane.showMessageDialog(fenetre, message, "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void afficherAccueil() {
        StringBuilder texte = new StringBuilder();
        texte.append("Plateforme d'evaluation collaborative de jeux video").append(System.lineSeparator()).append(System.lineSeparator());
        texte.append(msgComptes).append(System.lineSeparator());
        texte.append(msgCatalogue).append(System.lineSeparator()).append(System.lineSeparator());
        texte.append("La version graphique est active.").append(System.lineSeparator());
        texte.append("Utilise le panneau de gauche pour naviguer selon ton profil.");
        afficherTexte(texte.toString());
    }

    private void seConnecter() {
        String pseudo = demanderTexte("Pseudo : ");
        if (pseudo == null) {
            return;
        }
        Membre membre = plateforme.connecter(pseudo);
        afficherTexte("Connexion reussie en tant que " + membre.getTypeProfil() + ".");
    }

    private void inscrireJoueur() {
        String pseudo = demanderTexte("Choisissez un pseudo : ");
        if (pseudo == null) {
            return;
        }
        Joueur joueur = plateforme.inscrireJoueur(pseudo);
        afficherTexte("Inscription reussie pour " + joueur.getPseudo() + " en tant que joueur.");
    }

    private void inscrireMembreAdmin() {
        String pseudo = demanderTexte("Choisissez un pseudo : ");
        if (pseudo == null) {
            return;
        }
        ProfilMembre profil = demanderProfilMembre();
        if (profil == null) {
            return;
        }
        Membre membre = plateforme.inscrireMembre(pseudo, profil);
        afficherTexte("Inscription reussie pour " + membre.getPseudo()
                + " en tant que " + membre.getTypeProfil() + ".");
    }

    private void seDeconnecter() {
        plateforme.deconnecter();
        afficherTexte("Deconnexion effectuee.");
    }

    private void seDesinscrire() {
        String pseudo = plateforme.getMembreConnecte().getPseudo();
        if (!plateforme.desinscrireJoueur(pseudo)) {
            throw new IllegalStateException("Seul un joueur simple peut se desinscrire");
        }
        afficherTexte("Desinscription effectuee.");
    }

    private void chercherJeux() {
        CritereRechercheJeu critere = demanderCritereRecherche();
        if (critere == null) {
            return;
        }
        List<Jeu> jeux = plateforme.chercherJeux(critere);
        if (jeux.isEmpty()) {
            afficherTexte("Aucun jeu trouve.");
            return;
        }
        afficherTexte(formaterResultatsRecherche(jeux, critere));
    }

    private void afficherJeu() {
        Jeu jeu = demanderJeu("Afficher un jeu");
        if (jeu == null) {
            return;
        }
        afficherTexte(formaterJeu(jeu));
    }

    private void afficherMembre() {
        String pseudo = demanderTexte("Pseudo du membre : ");
        if (pseudo == null) {
            return;
        }
        afficherTexte(plateforme.afficherInfosMembre(pseudo));
    }

    private void afficherProfil() {
        afficherTexte(plateforme.afficherInfosMembre(plateforme.getMembreConnecte().getPseudo()));
    }

    private void ajouterJeuPossede() {
        ChoixSupport choix = demanderSupport("Ajouter un jeu possede");
        if (choix == null) {
            return;
        }
        plateforme.ajouterJeuPossedeAuMembreConnecte(choix.nomJeu(), choix.support());
        afficherTexte("Jeu ajoute a votre bibliotheque : " + choix.nomJeu() + " sur " + choix.support() + ".");
    }

    private void ajouterTemps() {
        SaisieTemps saisie = demanderSaisieTemps();
        if (saisie == null) {
            return;
        }
        int total = plateforme.ajouterTempsDeJeuAuMembreConnecte(
                saisie.choix().nomJeu(),
                saisie.choix().support(),
                saisie.heures()
        );
        afficherTexte("Temps de jeu total sur " + saisie.choix().nomJeu()
                + " / " + saisie.choix().support()
                + " : " + total + " heure(s).");
    }

    private void lireEvaluations() {
        ChoixSupport choix = demanderSupport("Lire les evaluations");
        if (choix == null) {
            return;
        }
        List<Evaluation> evaluations = plateforme.lireEvaluations(choix.nomJeu(), choix.support());
        afficherTexte(formaterEvaluations(choix, evaluations));
    }

    private void lireTest() {
        ChoixSupport choix = demanderSupport("Lire le test");
        if (choix == null) {
            return;
        }
        TestJeu test = plateforme.lireTestAuMembreConnecte(choix.nomJeu(), choix.support());
        afficherTexte(formaterTest(test));
    }

    private void ajouterEvaluation() {
        SaisieEvaluation saisie = demanderSaisieEvaluation();
        if (saisie == null) {
            return;
        }
        plateforme.ajouterEvaluationAuMembreConnecte(
                saisie.choix().nomJeu(),
                saisie.choix().support(),
                LocalDate.now(),
                saisie.texte(),
                saisie.version(),
                saisie.note()
        );
        List<Evaluation> evaluations = plateforme.lireEvaluations(saisie.choix().nomJeu(), saisie.choix().support());
        afficherTexte("Evaluation publiee.\n\n" + formaterEvaluations(saisie.choix(), evaluations));
    }

    private void voterEvaluation() {
        Evaluation evaluation = choisirEvaluation();
        if (evaluation == null) {
            return;
        }
        Object choix = JOptionPane.showInputDialog(
                fenetre,
                "Vote d'utilite :",
                "Evaluation d'utilite",
                JOptionPane.QUESTION_MESSAGE,
                null,
                new Object[]{"Positif", "Neutre", "Negatif"},
                "Positif"
        );
        if (choix == null) {
            return;
        }
        VoteUtilite vote = switch (choix.toString()) {
            case "Positif" -> VoteUtilite.POSITIF;
            case "Neutre" -> VoteUtilite.NEUTRE;
            default -> VoteUtilite.NEGATIF;
        };
        plateforme.voterUtiliteEvaluationAuMembreConnecte(evaluation, vote);
        afficherTexte("Vote enregistre.");
    }

    private void voirDemandesTests() {
        List<SupportJeu> supports = plateforme.rechercherTestsARealiser();
        if (supports.isEmpty()) {
            afficherTexte("Aucun support sans test avec des jetons places.");
            return;
        }
        StringBuilder texte = new StringBuilder("Demandes de test").append(System.lineSeparator()).append(System.lineSeparator());
        for (SupportJeu support : supports) {
            texte.append("- ")
                    .append(support.getJeu().getNom())
                    .append(" / ")
                    .append(support.getPlateforme())
                    .append(" : ")
                    .append(support.getTotalJetons())
                    .append(" jeton(s)")
                    .append(System.lineSeparator());
        }
        afficherTexte(texte.toString());
    }

    private void placerJetons() {
        SaisieJetons saisie = demanderSaisieJetons("Placer des jetons", "Nombre de jetons a placer : ");
        if (saisie == null) {
            return;
        }
        plateforme.placerJetonsAuMembreConnecte(
                saisie.choix().nomJeu(),
                saisie.choix().support(),
                saisie.quantite()
        );
        afficherTexte("Jetons places sur " + saisie.choix().nomJeu()
                + " / " + saisie.choix().support()
                + " : " + saisie.quantite() + " jeton(s).");
    }

    private void retirerJetons() {
        SaisieJetons saisie = demanderSaisieJetons("Retirer des jetons", "Nombre de jetons a retirer : ");
        if (saisie == null) {
            return;
        }
        plateforme.retirerJetonsAuMembreConnecte(
                saisie.choix().nomJeu(),
                saisie.choix().support(),
                saisie.quantite()
        );
        afficherTexte("Jetons retires de " + saisie.choix().nomJeu()
                + " / " + saisie.choix().support()
                + " : " + saisie.quantite() + " jeton(s).");
    }

    private void rechercherTests() {
        List<SupportJeu> supports = plateforme.rechercherTestsARealiserPourMembreConnecte();
        if (supports.isEmpty()) {
            afficherTexte("Aucun test realisable actuellement avec votre bibliotheque et votre temps de jeu.");
            return;
        }
        StringBuilder texte = new StringBuilder("Supports a tester").append(System.lineSeparator()).append(System.lineSeparator());
        for (SupportJeu support : supports) {
            texte.append("- ")
                    .append(support.getJeu().getNom())
                    .append(" / ")
                    .append(support.getPlateforme())
                    .append(" (")
                    .append(support.getTotalJetons())
                    .append(" jeton(s))")
                    .append(System.lineSeparator());
        }
        afficherTexte(texte.toString());
    }

    private void ajouterTest() {
        ChoixSupport choix = demanderSupport("Ajouter un test");
        if (choix == null) {
            return;
        }
        String version = demanderTexte("Version / build testee : ");
        if (version == null) {
            return;
        }
        String texte = demanderTexte("Texte du test : ");
        if (texte == null) {
            return;
        }
        Map<String, Integer> notes = saisirNotes("Categorie testee", true);
        if (notes == null) {
            return;
        }
        List<String> pointsForts = saisirListe("Point fort");
        if (pointsForts == null) {
            return;
        }
        List<String> pointsFaibles = saisirListe("Point faible");
        if (pointsFaibles == null) {
            return;
        }

        String conditions = demanderTexte("Conditions du test (optionnel) : ");
        if (conditions == null) {
            return;
        }
        List<String> jeuxSimilaires = saisirListe("Jeu similaire conseille");
        if (jeuxSimilaires == null) {
            return;
        }
        Map<String, Integer> notesGenre = saisirNotes("Categorie specifique au genre", false);
        if (notesGenre == null) {
            return;
        }
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
        afficherTexte("Test publie.\n\n" + formaterTest(test));
    }

    private void signalerEvaluation() {
        Evaluation evaluation = choisirEvaluation();
        if (evaluation == null) {
            return;
        }
        plateforme.signalerEvaluationAuMembreConnecte(evaluation);
        afficherTexte("Evaluation signalee.");
    }

    private void supprimerEvaluation() {
        Evaluation evaluation = choisirEvaluation();
        if (evaluation == null) {
            return;
        }
        plateforme.supprimerEvaluationAuMembreConnecte(evaluation);
        afficherTexte("Evaluation supprimee.");
    }

    private void bloquerMembre() {
        String pseudo = demanderTexte("Pseudo du membre a bloquer : ");
        if (pseudo == null) {
            return;
        }
        ((Administrateur) plateforme.getMembreConnecte()).bloquerMembre(plateforme.consulterMembre(pseudo));
        afficherTexte("Membre bloque.");
    }

    private void debloquerMembre() {
        String pseudo = demanderTexte("Pseudo du membre a debloquer : ");
        if (pseudo == null) {
            return;
        }
        ((Administrateur) plateforme.getMembreConnecte()).debloquerMembre(plateforme.consulterMembre(pseudo));
        afficherTexte("Membre debloque.");
    }

    private void promouvoirJoueur() {
        String pseudo = demanderTexte("Pseudo du joueur a promouvoir : ");
        if (pseudo == null) {
            return;
        }
        plateforme.promouvoirJoueur(pseudo);
        afficherTexte("Joueur promu testeur.");
    }

    private void promouvoirTesteur() {
        String pseudo = demanderTexte("Pseudo du testeur a promouvoir : ");
        if (pseudo == null) {
            return;
        }
        plateforme.promouvoirTesteur(pseudo);
        afficherTexte("Testeur promu administrateur.");
    }

    private void desinscrireJoueur() {
        String pseudo = demanderTexte("Pseudo du joueur a desinscrire : ");
        if (pseudo == null) {
            return;
        }
        if (!plateforme.desinscrireJoueur(pseudo)) {
            throw new IllegalArgumentException("Ce pseudo ne correspond pas a un joueur simple desinscriptible");
        }
        afficherTexte("Joueur desinscrit.");
    }

    private Evaluation choisirEvaluation() {
        ChoixSupport choix = demanderSupport("Choisir une evaluation");
        if (choix == null) {
            return null;
        }
        List<Evaluation> evaluations = plateforme.lireEvaluations(choix.nomJeu(), choix.support());
        if (evaluations.isEmpty()) {
            afficherTexte("Aucune evaluation disponible pour ce support.");
            return null;
        }

        String[] options = new String[evaluations.size()];
        for (int i = 0; i < evaluations.size(); i++) {
            Evaluation evaluation = evaluations.get(i);
            options[i] = (i + 1) + ". "
                    + evaluation.getAuteur().getPseudo()
                    + " | note=" + evaluation.getNoteGlobale()
                    + " | utilite=" + evaluation.getScoreUtilite();
        }

        Object choixOption = JOptionPane.showInputDialog(
                fenetre,
                "Choisissez une evaluation :",
                "Evaluations",
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );
        if (choixOption == null) {
            return null;
        }
        for (int i = 0; i < options.length; i++) {
            if (options[i].equals(choixOption)) {
                return evaluations.get(i);
            }
        }
        return null;
    }

    private Map<String, Integer> saisirNotes(String libelle, boolean obligatoire) {
        Map<String, Integer> notes = new LinkedHashMap<>();
        int nbNotes = 0;
        while (true) {
            String categorie = demanderTexte(libelle + " (laisser vide pour terminer) : ");
            if (categorie == null) {
                if (obligatoire && nbNotes == 0) {
                    return null;
                }
                return notes;
            }
            if (categorie.isEmpty()) {
                if (obligatoire && nbNotes == 0) {
                    JOptionPane.showMessageDialog(fenetre, "Ajoutez au moins une note.", "Information", JOptionPane.INFORMATION_MESSAGE);
                    continue;
                }
                return notes;
            }
            Integer note = demanderEntier("Note (/100) : ", 0, 100);
            if (note == null) {
                if (obligatoire && nbNotes == 0) {
                    return null;
                }
                return notes;
            }
            notes.put(categorie, note);
            nbNotes++;
        }
    }

    private List<String> saisirListe(String libelle) {
        List<String> valeurs = new ArrayList<>();
        while (true) {
            String valeur = demanderTexte(libelle + " (laisser vide pour terminer) : ");
            if (valeur == null) {
                return null;
            }
            if (valeur.isEmpty()) {
                return valeurs;
            }
            valeurs.add(valeur);
        }
    }

    private Jeu demanderJeu(String titre) {
        List<Jeu> jeux = listerJeuxTries();
        if (jeux.isEmpty()) {
            JOptionPane.showMessageDialog(fenetre, "Aucun jeu n'est disponible.", "Information", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }

        JComboBox<String> jeuCombo = new JComboBox<>();
        for (Jeu jeu : jeux) {
            jeuCombo.addItem(jeu.getNom());
        }

        JPanel panneau = new JPanel(new GridLayout(0, 2, 8, 8));
        panneau.add(new JLabel("Jeu"));
        panneau.add(jeuCombo);

        int resultat = JOptionPane.showConfirmDialog(
                fenetre,
                panneau,
                titre,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        if (resultat != JOptionPane.OK_OPTION) {
            return null;
        }
        return jeux.get(jeuCombo.getSelectedIndex());
    }

    private ChoixSupport demanderSupport(String titre) {
        SupportFormulaire formulaire = creerFormulaireSupport();
        if (formulaire == null) {
            return null;
        }

        int resultat = JOptionPane.showConfirmDialog(
                fenetre,
                formulaire.panneau(),
                titre,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        if (resultat != JOptionPane.OK_OPTION) {
            return null;
        }
        return new ChoixSupport(
                formulaire.jeuCombo().getSelectedItem().toString(),
                formulaire.supportCombo().getSelectedItem().toString()
        );
    }

    private SaisieTemps demanderSaisieTemps() {
        while (true) {
            SupportFormulaire formulaire = creerFormulaireSupport();
            if (formulaire == null) {
                return null;
            }
            JTextField heuresField = new JTextField();
            formulaire.panneau().add(new JLabel("Heures a ajouter"));
            formulaire.panneau().add(heuresField);

            int resultat = JOptionPane.showConfirmDialog(
                    fenetre,
                    formulaire.panneau(),
                    "Ajouter du temps de jeu",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );
            if (resultat != JOptionPane.OK_OPTION) {
                return null;
            }
            try {
                int heures = Integer.parseInt(heuresField.getText().trim());
                if (heures < 0) {
                    throw new NumberFormatException();
                }
                return new SaisieTemps(
                        new ChoixSupport(
                                formulaire.jeuCombo().getSelectedItem().toString(),
                                formulaire.supportCombo().getSelectedItem().toString()
                        ),
                        heures
                );
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(fenetre, "Entrez un nombre entier positif ou nul.", "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private SaisieJetons demanderSaisieJetons(String titre, String libelleQuantite) {
        while (true) {
            SupportFormulaire formulaire = creerFormulaireSupport();
            if (formulaire == null) {
                return null;
            }
            JTextField quantiteField = new JTextField();
            formulaire.panneau().add(new JLabel(libelleQuantite));
            formulaire.panneau().add(quantiteField);

            int resultat = JOptionPane.showConfirmDialog(
                    fenetre,
                    formulaire.panneau(),
                    titre,
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );
            if (resultat != JOptionPane.OK_OPTION) {
                return null;
            }
            try {
                int quantite = Integer.parseInt(quantiteField.getText().trim());
                if (quantite <= 0) {
                    throw new NumberFormatException();
                }
                return new SaisieJetons(
                        new ChoixSupport(
                                formulaire.jeuCombo().getSelectedItem().toString(),
                                formulaire.supportCombo().getSelectedItem().toString()
                        ),
                        quantite
                );
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(fenetre, "Entrez un nombre entier strictement positif.", "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private SaisieEvaluation demanderSaisieEvaluation() {
        while (true) {
            SupportFormulaire formulaire = creerFormulaireSupport();
            if (formulaire == null) {
                return null;
            }

            JTextField noteField = new JTextField();
            JTextField versionField = new JTextField();
            JTextArea texteArea = new JTextArea(6, 24);
            texteArea.setLineWrap(true);
            texteArea.setWrapStyleWord(true);
            JScrollPane scrollTexte = new JScrollPane(texteArea);

            formulaire.panneau().add(new JLabel("Note globale (/10)"));
            formulaire.panneau().add(noteField);
            formulaire.panneau().add(new JLabel("Version / build"));
            formulaire.panneau().add(versionField);

            JPanel panneauGlobal = new JPanel(new BorderLayout(8, 8));
            panneauGlobal.add(formulaire.panneau(), BorderLayout.NORTH);
            panneauGlobal.add(scrollTexte, BorderLayout.CENTER);
            scrollTexte.setBorder(BorderFactory.createTitledBorder("Texte de l'evaluation"));

            int resultat = JOptionPane.showConfirmDialog(
                    fenetre,
                    panneauGlobal,
                    "Ajouter une evaluation",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );
            if (resultat != JOptionPane.OK_OPTION) {
                return null;
            }

            try {
                double note = Double.parseDouble(noteField.getText().trim());
                if (note < 0.0 || note > 10.0) {
                    throw new NumberFormatException();
                }
                String version = versionField.getText().trim();
                String texte = texteArea.getText().trim();
                if (version.isEmpty() || texte.isEmpty()) {
                    JOptionPane.showMessageDialog(fenetre, "La version et le texte sont obligatoires.", "Erreur", JOptionPane.ERROR_MESSAGE);
                    continue;
                }
                return new SaisieEvaluation(
                        new ChoixSupport(
                                formulaire.jeuCombo().getSelectedItem().toString(),
                                formulaire.supportCombo().getSelectedItem().toString()
                        ),
                        note,
                        version,
                        texte
                );
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(fenetre, "Entrez une note valide entre 0 et 10.", "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private SupportFormulaire creerFormulaireSupport() {
        List<Jeu> jeux = listerJeuxTries();
        if (jeux.isEmpty()) {
            JOptionPane.showMessageDialog(fenetre, "Aucun jeu n'est disponible.", "Information", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }

        JComboBox<String> jeuCombo = new JComboBox<>();
        for (Jeu jeu : jeux) {
            jeuCombo.addItem(jeu.getNom());
        }
        JComboBox<String> supportCombo = new JComboBox<>();
        mettreAJourSupports(formulaireJeuSelectionne(jeuCombo, jeux), supportCombo);
        jeuCombo.addActionListener(e -> mettreAJourSupports(formulaireJeuSelectionne(jeuCombo, jeux), supportCombo));

        JPanel panneau = new JPanel(new GridLayout(0, 2, 8, 8));
        panneau.add(new JLabel("Jeu"));
        panneau.add(jeuCombo);
        panneau.add(new JLabel("Plateforme"));
        panneau.add(supportCombo);
        return new SupportFormulaire(panneau, jeuCombo, supportCombo);
    }

    private Jeu formulaireJeuSelectionne(JComboBox<String> jeuCombo, List<Jeu> jeux) {
        int index = Math.max(0, jeuCombo.getSelectedIndex());
        return jeux.get(index);
    }

    private void mettreAJourSupports(Jeu jeu, JComboBox<String> supportCombo) {
        supportCombo.removeAllItems();
        for (SupportJeu support : listerSupportsTries(jeu)) {
            supportCombo.addItem(support.getPlateforme());
        }
    }

    private String demanderTexte(String message) {
        Object valeur = JOptionPane.showInputDialog(fenetre, message, "Saisie", JOptionPane.QUESTION_MESSAGE, null, null, "");
        if (valeur == null) {
            return null;
        }
        return valeur.toString().trim();
    }

    private ProfilMembre demanderProfilMembre() {
        Object choix = JOptionPane.showInputDialog(
                fenetre,
                "Profil a inscrire :",
                "Inscription",
                JOptionPane.QUESTION_MESSAGE,
                null,
                new Object[]{"Joueur", "Testeur", "Administrateur"},
                "Joueur"
        );
        if (choix == null) {
            return null;
        }
        return switch (choix.toString()) {
            case "Joueur" -> ProfilMembre.JOUEUR;
            case "Testeur" -> ProfilMembre.TESTEUR;
            default -> ProfilMembre.ADMINISTRATEUR;
        };
    }

    private CritereRechercheJeu demanderCritereRecherche() {
        JTextField texteLibre = new JTextField();
        JComboBox<String> categorie = creerCombo("Toutes", plateforme.getCategoriesDisponibles());
        JComboBox<String> editeur = creerCombo("Tous", plateforme.getEditeursDisponibles());
        JComboBox<String> rating = creerCombo("Tous", plateforme.getRatingsDisponibles());
        JComboBox<String> plateformeSupport = creerCombo("Toutes", plateforme.getPlateformesDisponibles());
        JComboBox<String> developpeur = creerCombo("Tous", plateforme.getDeveloppeursDisponibles());
        JComboBox<String> testDisponible = new JComboBox<>(new String[]{"Tous", "Avec test", "Sans test"});

        JPanel panneau = new JPanel(new GridLayout(0, 2, 8, 8));
        panneau.add(new JLabel("Texte libre"));
        panneau.add(texteLibre);
        panneau.add(new JLabel("Categorie"));
        panneau.add(categorie);
        panneau.add(new JLabel("Editeur"));
        panneau.add(editeur);
        panneau.add(new JLabel("Rating"));
        panneau.add(rating);
        panneau.add(new JLabel("Plateforme"));
        panneau.add(plateformeSupport);
        panneau.add(new JLabel("Developpeur"));
        panneau.add(developpeur);
        panneau.add(new JLabel("Presence d'un test"));
        panneau.add(testDisponible);

        int resultat = JOptionPane.showConfirmDialog(
                fenetre,
                panneau,
                "Recherche de jeux",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        if (resultat != JOptionPane.OK_OPTION) {
            return null;
        }

        return new CritereRechercheJeu(
                videVersNull(texteLibre.getText()),
                selectionCombo(categorie),
                selectionCombo(editeur),
                selectionCombo(rating),
                selectionCombo(plateformeSupport),
                selectionCombo(developpeur),
                lirePresenceTest(testDisponible)
        );
    }

    private JComboBox<String> creerCombo(String valeurToutes, List<String> options) {
        JComboBox<String> combo = new JComboBox<>();
        combo.addItem(valeurToutes);
        for (String option : options) {
            combo.addItem(option);
        }
        return combo;
    }

    private String selectionCombo(JComboBox<String> combo) {
        Object selection = combo.getSelectedItem();
        if (selection == null || combo.getSelectedIndex() == 0) {
            return null;
        }
        return selection.toString();
    }

    private Boolean lirePresenceTest(JComboBox<String> combo) {
        return switch (combo.getSelectedIndex()) {
            case 1 -> true;
            case 2 -> false;
            default -> null;
        };
    }

    private String videVersNull(String valeur) {
        return valeur == null || valeur.isBlank() ? null : valeur.trim();
    }

    private Integer demanderEntier(String message, int min, int max) {
        while (true) {
            String valeur = demanderTexte(message);
            if (valeur == null) {
                return null;
            }
            try {
                int nombre = Integer.parseInt(valeur);
                if (nombre < min || nombre > max) {
                    JOptionPane.showMessageDialog(fenetre, "Entrez une valeur entre " + min + " et " + max + ".", "Erreur", JOptionPane.ERROR_MESSAGE);
                    continue;
                }
                return nombre;
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(fenetre, "Entrez un entier valide.", "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private Double demanderDouble(String message, double min, double max) {
        while (true) {
            String valeur = demanderTexte(message);
            if (valeur == null) {
                return null;
            }
            try {
                double nombre = Double.parseDouble(valeur);
                if (nombre < min || nombre > max) {
                    JOptionPane.showMessageDialog(fenetre, "Entrez une valeur entre " + min + " et " + max + ".", "Erreur", JOptionPane.ERROR_MESSAGE);
                    continue;
                }
                return nombre;
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(fenetre, "Entrez un nombre valide.", "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String formaterEvaluations(ChoixSupport choix, List<Evaluation> evaluations) {
        if (evaluations.isEmpty()) {
            return "Aucune evaluation disponible.";
        }
        StringBuilder texte = new StringBuilder();
        texte.append("Evaluations").append(System.lineSeparator());
        texte.append("Jeu : ").append(choix.nomJeu()).append(System.lineSeparator());
        texte.append("Support : ").append(choix.support()).append(System.lineSeparator());
        texte.append("Total : ").append(evaluations.size()).append(System.lineSeparator()).append(System.lineSeparator());
        for (int i = 0; i < evaluations.size(); i++) {
            Evaluation evaluation = evaluations.get(i);
            texte.append("[").append(i + 1).append("] ")
                    .append(evaluation.getAuteur().getPseudo())
                    .append(System.lineSeparator())
                    .append("  Date : ").append(evaluation.getDate())
                    .append(" | version : ").append(evaluation.getVersionBuild())
                    .append(System.lineSeparator())
                    .append("  Note : ")
                    .append(evaluation.getNoteGlobale())
                    .append("/10 | utilite : ")
                    .append(evaluation.getScoreUtilite())
                    .append(" | votes : +")
                    .append(evaluation.getVotesPositifs())
                    .append(" / =")
                    .append(evaluation.getVotesNeutres())
                    .append(" / -")
                    .append(evaluation.getVotesNegatifs())
                    .append(System.lineSeparator())
                    .append("  Signalee : ")
                    .append(evaluation.estSignalee() ? "oui" : "non")
                    .append(System.lineSeparator())
                    .append("  Texte : ")
                    .append(evaluation.getTexte())
                    .append(System.lineSeparator())
                    .append(System.lineSeparator());
        }
        return texte.toString();
    }

    private String formaterTest(TestJeu test) {
        StringBuilder texte = new StringBuilder();
        texte.append("Test de ").append(test.getAuteur().getPseudo())
                .append(" | version=").append(test.getVersionBuild())
                .append(" | date=").append(test.getDate())
                .append(System.lineSeparator())
                .append(System.lineSeparator())
                .append(test.getTexte())
                .append(System.lineSeparator());

        if (!test.getNotesParCategorie().isEmpty()) {
            texte.append(System.lineSeparator()).append("Notes par categorie :").append(System.lineSeparator());
            ajouterNotes(texte, test.getNotesParCategorie());
        }
        if (!test.getNotesSpecifiquesAuGenre().isEmpty()) {
            texte.append(System.lineSeparator()).append("Notes specifiques au genre :").append(System.lineSeparator());
            ajouterNotes(texte, test.getNotesSpecifiquesAuGenre());
        }
        if (!test.getPointsForts().isEmpty()) {
            texte.append(System.lineSeparator()).append("Points forts :").append(System.lineSeparator());
            ajouterListe(texte, test.getPointsForts());
        }
        if (!test.getPointsFaibles().isEmpty()) {
            texte.append(System.lineSeparator()).append("Points faibles :").append(System.lineSeparator());
            ajouterListe(texte, test.getPointsFaibles());
        }
        if (!test.getConditionsTest().isEmpty()) {
            texte.append(System.lineSeparator()).append("Conditions du test : ").append(test.getConditionsTest()).append(System.lineSeparator());
        }
        if (!test.getJeuxSimilaires().isEmpty()) {
            texte.append(System.lineSeparator()).append("Jeux similaires conseilles :").append(System.lineSeparator());
            ajouterListe(texte, test.getJeuxSimilaires());
        }
        return texte.toString();
    }

    private void ajouterNotes(StringBuilder texte, Map<String, Integer> notes) {
        for (Map.Entry<String, Integer> entree : notes.entrySet()) {
            texte.append(" - ")
                    .append(entree.getKey())
                    .append(" : ")
                    .append(entree.getValue())
                    .append("/100")
                    .append(System.lineSeparator());
        }
    }

    private void ajouterListe(StringBuilder texte, List<String> valeurs) {
        for (String valeur : valeurs) {
            texte.append(" - ").append(valeur).append(System.lineSeparator());
        }
    }

    private void afficherTexte(String texte) {
        sortie.setText(texte);
        sortie.setCaretPosition(0);
    }

    private List<Jeu> listerJeuxTries() {
        List<Jeu> jeux = new ArrayList<>(plateforme.getJeux());
        jeux.sort(Comparator.comparing(Jeu::getNom, String.CASE_INSENSITIVE_ORDER));
        return jeux;
    }

    private List<SupportJeu> listerSupportsTries(Jeu jeu) {
        List<SupportJeu> supports = new ArrayList<>(jeu.getListeSupports());
        supports.sort(Comparator.comparing(SupportJeu::getPlateforme, String.CASE_INSENSITIVE_ORDER));
        return supports;
    }

    private String formaterResultatsRecherche(List<Jeu> jeux, CritereRechercheJeu critere) {
        StringBuilder texte = new StringBuilder();
        texte.append("Resultats de recherche").append(System.lineSeparator());
        texte.append("Jeux trouves : ").append(jeux.size()).append(System.lineSeparator());
        ajouterFiltresRecherche(texte, critere);
        texte.append(System.lineSeparator());

        for (Jeu jeu : jeux) {
            texte.append(jeu.getNom())
                    .append(" [").append(jeu.getCategorie()).append("]")
                    .append(System.lineSeparator());
            texte.append("  Editeur : ").append(jeu.getEditeur())
                    .append(" | Rating : ").append(jeu.getRating())
                    .append(System.lineSeparator());
            texte.append("  Supports correspondants :").append(System.lineSeparator());
            for (SupportJeu support : plateforme.rechercherSupportsCorrespondants(jeu, critere)) {
                texte.append("   - ").append(support.getPlateforme())
                        .append(" | sortie ").append(support.getAnneeSortie())
                        .append(" | dev ").append(support.getDeveloppeur())
                        .append(" | test ").append(support.aUnTest() ? "oui" : "non")
                        .append(" | jetons ").append(support.getTotalJetons())
                        .append(" | evals ").append(support.getNombreEvaluationsTotal())
                        .append(" | note moy ").append(formaterDecimal(support.getScoreMoyenEvaluationsTotal())).append("/10")
                        .append(System.lineSeparator());
            }
            texte.append(System.lineSeparator());
        }
        return texte.toString();
    }

    private void ajouterFiltresRecherche(StringBuilder texte, CritereRechercheJeu critere) {
        List<String> filtres = new ArrayList<>();
        if (critere.texteLibre() != null) {
            filtres.add("texte=" + critere.texteLibre());
        }
        if (critere.categorie() != null) {
            filtres.add("categorie=" + critere.categorie());
        }
        if (critere.editeur() != null) {
            filtres.add("editeur=" + critere.editeur());
        }
        if (critere.rating() != null) {
            filtres.add("rating=" + critere.rating());
        }
        if (critere.plateforme() != null) {
            filtres.add("plateforme=" + critere.plateforme());
        }
        if (critere.developpeur() != null) {
            filtres.add("developpeur=" + critere.developpeur());
        }
        if (critere.testDisponible() != null) {
            filtres.add("test=" + (critere.testDisponible() ? "oui" : "non"));
        }
        texte.append("Filtres : ");
        if (filtres.isEmpty()) {
            texte.append("aucun");
            return;
        }
        texte.append(String.join(" | ", filtres));
    }

    private String formaterJeu(Jeu jeu) {
        StringBuilder texte = new StringBuilder();
        texte.append("Fiche jeu").append(System.lineSeparator()).append(System.lineSeparator());
        texte.append("Nom : ").append(jeu.getNom()).append(System.lineSeparator());
        texte.append("Categorie : ").append(jeu.getCategorie()).append(System.lineSeparator());
        texte.append("Editeur : ").append(jeu.getEditeur()).append(System.lineSeparator());
        texte.append("Rating : ").append(jeu.getRating()).append(System.lineSeparator());
        texte.append("Supports : ").append(jeu.getListeSupports().size()).append(System.lineSeparator()).append(System.lineSeparator());

        for (SupportJeu support : listerSupportsTries(jeu)) {
            texte.append("[").append(support.getPlateforme()).append("]").append(System.lineSeparator());
            texte.append("  Sortie : ").append(support.getAnneeSortie())
                    .append(" | Developpeur : ").append(support.getDeveloppeur())
                    .append(System.lineSeparator());
            texte.append("  Ventes mondiales : ").append(formaterVentes(support.getVentesMondiales())).append(System.lineSeparator());
            texte.append("  Critiques initiales : ").append(support.getNombreCritiquesInitial())
                    .append(" | score moyen : ").append(formaterDecimal(support.getScoreMoyenCritiquesInitial())).append("/100")
                    .append(System.lineSeparator());
            texte.append("  Evaluations initiales : ").append(support.getNombreEvaluationsInitial())
                    .append(" | score moyen : ").append(formaterDecimal(support.getScoreMoyenEvaluationsInitial())).append("/10")
                    .append(System.lineSeparator());
            texte.append("  Sur la plateforme : evals=").append(support.getNombreEvaluationsPlateforme())
                    .append(" | note moy=").append(formaterDecimal(support.getScoreMoyenEvaluationsPlateforme())).append("/10")
                    .append(" | critiques=").append(support.getNombreCritiquesPlateforme())
                    .append(" | note critiques=").append(formaterDecimal(support.getScoreMoyenCritiquesPlateforme())).append("/100")
                    .append(System.lineSeparator());
            texte.append("  Totaux : evals=").append(support.getNombreEvaluationsTotal())
                    .append(" | note moy=").append(formaterDecimal(support.getScoreMoyenEvaluationsTotal())).append("/10")
                    .append(" | critiques=").append(support.getNombreCritiquesTotal())
                    .append(" | note critiques=").append(formaterDecimal(support.getScoreMoyenCritiquesTotal())).append("/100")
                    .append(System.lineSeparator());
            texte.append("  Test disponible : ").append(support.aUnTest() ? "oui" : "non")
                    .append(" | jetons demandes : ").append(support.getTotalJetons())
                    .append(System.lineSeparator())
                    .append(System.lineSeparator());
        }
        return texte.toString();
    }

    private String formaterDecimal(double valeur) {
        return String.format(Locale.ROOT, "%.1f", valeur);
    }

    private String formaterVentes(double ventes) {
        return String.format(Locale.ROOT, "%.2f million(s)", ventes);
    }

    @FunctionalInterface
    private interface ActionUi {
        void executer();
    }

    private record SupportFormulaire(JPanel panneau, JComboBox<String> jeuCombo, JComboBox<String> supportCombo) {
    }

    private record SaisieTemps(ChoixSupport choix, int heures) {
    }

    private record SaisieJetons(ChoixSupport choix, int quantite) {
    }

    private record SaisieEvaluation(ChoixSupport choix, double note, String version, String texte) {
    }

    private record ChoixSupport(String nomJeu, String support) {
    }
}
