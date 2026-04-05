package app.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.time.LocalDate;
import java.util.Locale;
import java.util.TreeSet;

/**
 * Facade centrale de la plateforme.
 * <p>
 * Cette classe orchestre la gestion des membres, du catalogue, des recherches
 * et des actions metier exposees aux interfaces utilisateur.
 */
public class Plateforme {
    private final Map<String, Membre> membres;
    private final Map<String, Jeu> jeux;
    private Membre connecte;

    /**
     * Cree une plateforme vide et ajoute un compte administrateur par defaut.
     */
    public Plateforme() {
        this.membres = new LinkedHashMap<>();
        this.jeux = new LinkedHashMap<>();
        this.connecte = null;
        ajouterMembre(new Administrateur("admin"));
    }

    /**
     * Ajoute un membre a la plateforme.
     *
     * @param membre membre a enregistrer
     */
    public void ajouterMembre(Membre membre) {
        String cle = normaliser(membre.getPseudo());
        if (membres.containsKey(cle)) {
            throw new IllegalArgumentException("Pseudo deja utilise : " + membre.getPseudo());
        }
        membres.put(cle, membre);
    }

    /**
     * Inscrit un nouveau joueur.
     *
     * @param pseudo pseudo demande
     * @return joueur cree
     */
    public Joueur inscrireJoueur(String pseudo) {
        if (!estPseudoDisponible(pseudo)) {
            throw new IllegalArgumentException("Pseudo deja utilise : " + pseudo);
        }
        Joueur membre = new Joueur(pseudo);
        ajouterMembre(membre);
        return membre;
    }

    /**
     * Inscrit un membre avec le profil demande.
     *
     * @param pseudo pseudo demande
     * @param profil profil a creer
     * @return membre cree
     */
    public Membre inscrireMembre(String pseudo, ProfilMembre profil) {
        if (profil == null) {
            throw new IllegalArgumentException("Le profil ne peut pas etre null");
        }
        return switch (profil) {
            case JOUEUR -> inscrireJoueur(pseudo);
            case TESTEUR -> {
                exigerAdministrateurConnecte();
                yield inscrireTesteur(pseudo);
            }
            case ADMINISTRATEUR -> {
                exigerAdministrateurConnecte();
                yield inscrireAdministrateur(pseudo);
            }
        };
    }

    /**
     * Inscrit un testeur.
     *
     * @param pseudo pseudo demande
     * @return testeur cree
     */
    public Testeur inscrireTesteur(String pseudo) {
        if (!estPseudoDisponible(pseudo)) {
            throw new IllegalArgumentException("Pseudo deja utilise : " + pseudo);
        }
        Testeur membre = new Testeur(pseudo);
        ajouterMembre(membre);
        return membre;
    }

    /**
     * Inscrit un administrateur.
     *
     * @param pseudo pseudo demande
     * @return administrateur cree
     */
    public Administrateur inscrireAdministrateur(String pseudo) {
        if (!estPseudoDisponible(pseudo)) {
            throw new IllegalArgumentException("Pseudo deja utilise : " + pseudo);
        }
        Administrateur membre = new Administrateur(pseudo);
        ajouterMembre(membre);
        return membre;
    }

    /**
     * Connecte un membre existant.
     *
     * @param pseudo pseudo du membre
     * @return membre connecte
     */
    public Membre connecter(String pseudo) {
        Membre membre = trouverMembre(pseudo);
        if (membre == null) {
            throw new IllegalArgumentException("Pseudo inexistant : " + pseudo);
        }
        if (membre.estBloque()) {
            throw new IllegalStateException("Ce membre est bloque");
        }
        connecte = membre;
        return membre;
    }

    /**
     * Deconnecte le membre courant.
     */
    public void deconnecter() {
        connecte = null;
    }

    /**
     * Retourne le membre actuellement connecte.
     *
     * @return membre connecte ou {@code null}
     */
    public Membre getMembreConnecte() {
        return connecte;
    }

    /**
     * Recherche un membre par pseudo.
     *
     * @param pseudo pseudo recherche
     * @return membre correspondant ou {@code null}
     */
    public Membre trouverMembre(String pseudo) {
        return membres.get(normaliser(pseudo));
    }

    /**
     * Indique si un pseudo est libre.
     *
     * @param pseudo pseudo a verifier
     * @return {@code true} si le pseudo n'est pas encore utilise
     */
    public boolean estPseudoDisponible(String pseudo) {
        return !membres.containsKey(normaliser(pseudo));
    }

    /**
     * Desinscrit un joueur simple.
     *
     * @param pseudo pseudo du joueur a supprimer
     * @return {@code true} si le joueur a ete supprime
     */
    public boolean desinscrireJoueur(String pseudo) {
        Membre membre = trouverMembre(pseudo);
        if (!(membre instanceof Joueur) || membre instanceof Testeur) {
            return false;
        }
        return supprimerMembreParCle(normaliser(pseudo));
    }

    /**
     * Promeut un joueur en testeur.
     *
     * @param pseudo pseudo du joueur a promouvoir
     * @return nouveau testeur
     */
    public Testeur promouvoirJoueur(String pseudo) {
        exigerAdministrateurConnecte();
        Membre membre = trouverMembre(pseudo);
        if (!(membre instanceof Joueur joueur) || membre instanceof Testeur) {
            throw new IllegalArgumentException("Ce membre n'est pas un joueur promouvable");
        }
        Testeur testeur = new Testeur(joueur);
        transfererReferencesJoueur(joueur, testeur);
        remplacerMembre(joueur, testeur);
        return testeur;
    }

    /**
     * Promeut un testeur en administrateur.
     *
     * @param pseudo pseudo du testeur a promouvoir
     * @return nouvel administrateur
     */
    public Administrateur promouvoirTesteur(String pseudo) {
        exigerAdministrateurConnecte();
        Membre membre = trouverMembre(pseudo);
        if (!(membre instanceof Testeur testeur) || membre instanceof Administrateur) {
            throw new IllegalArgumentException("Ce membre n'est pas un testeur promouvable");
        }
        Administrateur administrateur = new Administrateur(testeur);
        transfererReferencesJoueur(testeur, administrateur);
        for (TestJeu test : testeur.getTests()) {
            test.remplacerAuteur(administrateur);
        }
        remplacerMembre(testeur, administrateur);
        return administrateur;
    }

    /**
     * Supprime un membre de la plateforme, quel que soit son profil.
     *
     * @param pseudo pseudo du membre a supprimer
     * @return {@code true} si le membre a ete supprime
     */
    public boolean supprimerMembre(String pseudo) {
        return supprimerMembreParCle(normaliser(pseudo));
    }

    /**
     * Retourne tous les membres inscrits.
     *
     * @return collection non modifiable des membres
     */
    public Collection<Membre> getMembres() {
        return Collections.unmodifiableCollection(membres.values());
    }

    /**
     * Ajoute un jeu au catalogue.
     *
     * @param jeu jeu a ajouter
     */
    public void ajouterJeu(Jeu jeu) {
        String cle = normaliser(jeu.getNom());
        if (jeux.containsKey(cle)) {
            throw new IllegalArgumentException("Jeu deja present : " + jeu.getNom());
        }
        jeux.put(cle, jeu);
    }

    /**
     * Recherche un jeu par son nom.
     *
     * @param nom nom du jeu
     * @return jeu correspondant ou {@code null}
     */
    public Jeu trouverJeu(String nom) {
        return jeux.get(normaliser(nom));
    }

    /**
     * Recherche des jeux a partir d'un texte libre.
     *
     * @param critere texte libre recherche
     * @return jeux correspondants
     */
    public List<Jeu> chercherJeux(String critere) {
        return chercherJeux(new CritereRechercheJeu(critere, null, null, null, null, null, null));
    }

    /**
     * Recherche des jeux a partir d'un ensemble de criteres.
     *
     * @param critere criteres de recherche
     * @return liste triee des jeux correspondants
     */
    public List<Jeu> chercherJeux(CritereRechercheJeu critere) {
        if (critere == null || critere.estVide()) {
            List<Jeu> resultat = new ArrayList<>(jeux.values());
            resultat.sort(Comparator.comparing(Jeu::getNom, String.CASE_INSENSITIVE_ORDER));
            return resultat;
        }
        List<Jeu> resultat = new ArrayList<>();
        for (Jeu jeu : jeux.values()) {
            if (correspond(jeu, critere)) {
                resultat.add(jeu);
            }
        }
        resultat.sort(Comparator.comparing(Jeu::getNom, String.CASE_INSENSITIVE_ORDER));
        return resultat;
    }

    /**
     * Retourne l'ensemble des jeux du catalogue.
     *
     * @return collection non modifiable des jeux
     */
    public Collection<Jeu> getJeux() {
        return Collections.unmodifiableCollection(jeux.values());
    }

    /**
     * Retourne les categories presentes dans le catalogue.
     *
     * @return liste triee des categories
     */
    public List<String> getCategoriesDisponibles() {
        TreeSet<String> valeurs = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (Jeu jeu : jeux.values()) {
            if (!jeu.getCategorie().isBlank()) {
                valeurs.add(jeu.getCategorie());
            }
        }
        return List.copyOf(valeurs);
    }

    /**
     * Retourne les editeurs presents dans le catalogue.
     *
     * @return liste triee des editeurs
     */
    public List<String> getEditeursDisponibles() {
        TreeSet<String> valeurs = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (Jeu jeu : jeux.values()) {
            if (!jeu.getEditeur().isBlank()) {
                valeurs.add(jeu.getEditeur());
            }
        }
        return List.copyOf(valeurs);
    }

    /**
     * Retourne les classifications age presentes dans le catalogue.
     *
     * @return liste triee des ratings
     */
    public List<String> getRatingsDisponibles() {
        TreeSet<String> valeurs = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (Jeu jeu : jeux.values()) {
            if (!jeu.getRating().isBlank()) {
                valeurs.add(jeu.getRating());
            }
        }
        return List.copyOf(valeurs);
    }

    /**
     * Retourne les plateformes presentes dans le catalogue.
     *
     * @return liste triee des plateformes
     */
    public List<String> getPlateformesDisponibles() {
        TreeSet<String> valeurs = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (Jeu jeu : jeux.values()) {
            for (SupportJeu support : jeu.getListeSupports()) {
                if (!support.getPlateforme().isBlank()) {
                    valeurs.add(support.getPlateforme());
                }
            }
        }
        return List.copyOf(valeurs);
    }

    /**
     * Retourne les developpeurs presents dans le catalogue.
     *
     * @return liste triee des developpeurs
     */
    public List<String> getDeveloppeursDisponibles() {
        TreeSet<String> valeurs = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (Jeu jeu : jeux.values()) {
            for (SupportJeu support : jeu.getListeSupports()) {
                if (!support.getDeveloppeur().isBlank()) {
                    valeurs.add(support.getDeveloppeur());
                }
            }
        }
        return List.copyOf(valeurs);
    }

    /**
     * Retourne les supports tries par nombre de jetons demandes.
     *
     * @return liste des supports tries par priorite de test
     */
    public List<SupportJeu> rechercherSupportsParJetons() {
        List<SupportJeu> supports = new ArrayList<>();
        for (Jeu jeu : jeux.values()) {
            supports.addAll(jeu.getListeSupports());
        }
        supports.sort((a, b) -> Integer.compare(b.getTotalJetons(), a.getTotalJetons()));
        return supports;
    }

    /**
     * Retourne les supports d'un jeu qui correspondent aux criteres fournis.
     *
     * @param jeu jeu a filtrer
     * @param critere criteres de recherche
     * @return supports correspondants
     */
    public List<SupportJeu> rechercherSupportsCorrespondants(Jeu jeu, CritereRechercheJeu critere) {
        if (jeu == null) {
            throw new IllegalArgumentException("Le jeu ne peut pas etre null");
        }
        boolean texteLibreCorrespondAuJeu = critere == null
                || critere.estVide()
                || correspondTexteLibreJeu(jeu, critere.texteLibre());

        List<SupportJeu> supports = new ArrayList<>();
        for (SupportJeu support : jeu.getListeSupports()) {
            if (supportCorrespond(support, critere, texteLibreCorrespondAuJeu)) {
                supports.add(support);
            }
        }
        return supports;
    }

    /**
     * Retourne un membre existant ou echoue si le pseudo est inconnu.
     *
     * @param pseudo pseudo du membre
     * @return membre correspondant
     */
    public Membre consulterMembre(String pseudo) {
        Membre membre = trouverMembre(pseudo);
        if (membre == null) {
            throw new IllegalArgumentException("Pseudo inexistant : " + pseudo);
        }
        return membre;
    }

    /**
     * Construit un resume textuel des informations visibles d'un membre.
     *
     * @param pseudo pseudo du membre
     * @return texte d'information
     */
    public String afficherInfosMembre(String pseudo) {
        Membre membre = consulterMembre(pseudo);
        StringBuilder texte = new StringBuilder();
        texte.append("Membre ").append(membre.getPseudo())
                .append(" [").append(membre.getTypeProfil()).append("]")
                .append(", bloque=").append(membre.estBloque());

        if (membre instanceof Joueur joueur) {
            texte.append(", jetons=").append(joueur.getJetons())
                    .append(", jeuxPossedes=").append(joueur.getJeuxPossedes().size())
                    .append(", tempsDeJeuTotal=").append(joueur.getTempsDeJeuTotal())
                    .append(", evaluations=").append(joueur.getNombreEvaluations());

            if (connecte instanceof Testeur) {
                texte.append(", votesRecus=+").append(joueur.getNombreVotesPositifsRecus())
                        .append(" / =").append(joueur.getNombreVotesNeutresRecus())
                        .append(" / -").append(joueur.getNombreVotesNegatifsRecus());
            }
        }

        if (membre instanceof Testeur testeur) {
            texte.append(", tests=").append(testeur.getNombreTests());
        }

        if (membre instanceof Joueur joueur) {
            List<Entry<Jeu, Integer>> lignes = new ArrayList<>(joueur.getTempsDeJeuParJeu().entrySet());
            lignes.sort(Comparator
                    .<Entry<Jeu, Integer>>comparingInt(Entry::getValue).reversed()
                    .thenComparing(entree -> entree.getKey().getNom(), String.CASE_INSENSITIVE_ORDER));

            if (!lignes.isEmpty()) {
                texte.append(System.lineSeparator()).append("Jeux possedes :");
                for (Entry<Jeu, Integer> entree : lignes) {
                    texte.append(System.lineSeparator())
                            .append(" - ").append(entree.getKey().getNom())
                            .append(" : ").append(entree.getValue()).append(" heure(s)");
                }
            }
        }

        return texte.toString();
    }

    /**
     * Retourne un jeu existant ou echoue si le jeu est introuvable.
     *
     * @param nomJeu nom du jeu
     * @return jeu correspondant
     */
    public Jeu consulterJeu(String nomJeu) {
        Jeu jeu = trouverJeu(nomJeu);
        if (jeu == null) {
            throw new IllegalArgumentException("Jeu introuvable : " + nomJeu);
        }
        return jeu;
    }

    /**
     * Construit un resume textuel des informations d'un jeu et de ses supports.
     *
     * @param nomJeu nom du jeu
     * @return texte d'information
     */
    public String afficherInfosJeu(String nomJeu) {
        Jeu jeu = consulterJeu(nomJeu);
        StringBuilder texte = new StringBuilder();
        texte.append("Jeu ").append(jeu.getNom())
                .append(" [categorie=").append(jeu.getCategorie())
                .append(", editeur=").append(jeu.getEditeur())
                .append(", rating=").append(jeu.getRating())
                .append(", supports=").append(jeu.getListeSupports().size())
                .append(']');

        for (SupportJeu support : jeu.getListeSupports()) {
            texte.append(System.lineSeparator())
                    .append(" - ").append(support.getPlateforme())
                    .append(" : sortie=").append(support.getAnneeSortie())
                    .append(", developpeur=").append(support.getDeveloppeur())
                    .append(", ventesMondiales=").append(formater(support.getVentesMondiales()))
                    .append(", critiquesInit=").append(support.getNombreCritiquesInitial())
                    .append(", scoreCritiquesInit=").append(formater(support.getScoreMoyenCritiquesInitial()))
                    .append(", evalsInit=").append(support.getNombreEvaluationsInitial())
                    .append(", scoreEvalsInit=").append(formater(support.getScoreMoyenEvaluationsInitial()))
                    .append(", evals=").append(support.getNombreEvaluationsTotal())
                    .append(", noteMoyenne=").append(formaterDecimal(support.getScoreMoyenEvaluationsTotal()))
                    .append(", critiques=").append(support.getNombreCritiquesTotal())
                    .append(", noteCritiques=").append(formaterDecimal(support.getScoreMoyenCritiquesTotal()))
                    .append(", jetons=").append(support.getTotalJetons())
                    .append(", test=").append(support.aUnTest() ? "oui" : "non");
        }

        return texte.toString();
    }

    /**
     * Ajoute un support possede a la bibliotheque du membre connecte.
     *
     * @param nomJeu nom du jeu
     * @param supportNom nom de la plateforme
     * @return support ajoute
     */
    public SupportJeu ajouterJeuPossedeAuMembreConnecte(String nomJeu, String supportNom) {
        Joueur joueur = exigerJoueurConnecte();
        SupportJeu support = trouverSupport(nomJeu, supportNom);
        joueur.ajouterJeu(support);
        return support;
    }

    /**
     * Ajoute du temps de jeu au membre connecte.
     *
     * @param nomJeu nom du jeu
     * @param supportNom nom de la plateforme
     * @param heures nombre d'heures a ajouter
     * @return nouveau temps de jeu sur le support
     */
    public int ajouterTempsDeJeuAuMembreConnecte(String nomJeu, String supportNom, int heures) {
        Joueur joueur = exigerJoueurConnecte();
        SupportJeu support = trouverSupport(nomJeu, supportNom);
        joueur.ajouterTempsJeu(support, heures);
        return joueur.getTempsDeJeu(support);
    }

    /**
     * Retourne les evaluations visibles d'un support, triees par note.
     *
     * @param nomJeu nom du jeu
     * @param supportNom nom de la plateforme
     * @return liste triee des evaluations
     */
    public List<Evaluation> lireEvaluations(String nomJeu, String supportNom) {
        SupportJeu support = trouverSupport(nomJeu, supportNom);
        List<Evaluation> evaluations = new ArrayList<>(support.getEvaluations());
        evaluations.sort(Comparator
                .comparingDouble(Evaluation::getNoteGlobale).reversed()
                .thenComparing(Evaluation::getDate));
        return evaluations;
    }

    /**
     * Lit le test d'un support pour le membre connecte.
     *
     * @param nomJeu nom du jeu
     * @param supportNom nom de la plateforme
     * @return test du support
     */
    public TestJeu lireTestAuMembreConnecte(String nomJeu, String supportNom) {
        return lireTest(nomJeu, supportNom);
    }

    /**
     * Lit le test d'un support.
     *
     * @param nomJeu nom du jeu
     * @param supportNom nom de la plateforme
     * @return test du support
     */
    public TestJeu lireTest(String nomJeu, String supportNom) {
        exigerJoueurConnecte();
        SupportJeu support = trouverSupport(nomJeu, supportNom);
        if (!support.aUnTest()) {
            throw new IllegalStateException("Aucun test n'est disponible pour ce support");
        }
        return support.getTest();
    }

    /**
     * Cree et publie une evaluation pour le membre connecte.
     *
     * @param nomJeu nom du jeu
     * @param supportNom nom de la plateforme
     * @param date date de publication
     * @param texte contenu de l'evaluation
     * @param version version du jeu evalue
     * @param note note globale sur 10
     * @return evaluation creee
     */
    public Evaluation ajouterEvaluationAuMembreConnecte(
            String nomJeu,
            String supportNom,
            LocalDate date,
            String texte,
            String version,
            double note
    ) {
        Joueur joueur = exigerJoueurConnecte();
        SupportJeu support = trouverSupport(nomJeu, supportNom);
        Evaluation evaluation = new Evaluation(joueur, support, date, texte, version, note);
        support.getJeu().ajouterEvaluation(evaluation);
        return evaluation;
    }

    /**
     * Enregistre un vote d'utilite sur une evaluation au nom du membre connecte.
     *
     * @param evaluation evaluation cible
     * @param vote type de vote
     */
    public void voterUtiliteEvaluationAuMembreConnecte(Evaluation evaluation, VoteUtilite vote) {
        Joueur joueur = exigerJoueurConnecte();
        SupportJeu support = evaluation.getSupportJeu();
        if (!joueur.possedeJeu(support)) {
            throw new IllegalStateException("Cette action est reservee aux membres qui possedent le jeu concerne");
        }
        if (joueur.getTempsDeJeu(support) < Jeu.DUREE_MINIMALE_EVALUATION_HEURES) {
            throw new IllegalStateException(
                    "Cette action est reservee aux membres ayant joue au moins "
                            + Jeu.DUREE_MINIMALE_EVALUATION_HEURES
                            + " heure(s) au support concerne"
            );
        }
        evaluation.voter(joueur, vote);
    }

    /**
     * Signale une evaluation au nom du testeur connecte.
     *
     * @param evaluation evaluation a signaler
     */
    public void signalerEvaluationAuMembreConnecte(Evaluation evaluation) {
        Testeur testeur = exigerTesteurConnecte();
        testeur.signalerEvaluation(evaluation);
    }

    /**
     * Supprime une evaluation au nom de l'administrateur connecte.
     *
     * @param evaluation evaluation a supprimer
     */
    public void supprimerEvaluationAuMembreConnecte(Evaluation evaluation) {
        Administrateur administrateur = exigerAdministrateurConnecte();
        administrateur.supprimerEvaluation(evaluation);
    }

    /**
     * Place des jetons sur un support au nom du membre connecte.
     *
     * @param nomJeu nom du jeu
     * @param supportNom nom de la plateforme
     * @param quantite quantite de jetons
     */
    public void placerJetonsAuMembreConnecte(String nomJeu, String supportNom, int quantite) {
        Joueur joueur = exigerJoueurConnecte();
        SupportJeu support = trouverSupport(nomJeu, supportNom);
        support.placerJetons(joueur, quantite);
    }

    /**
     * Retire des jetons places sur un support au nom du membre connecte.
     *
     * @param nomJeu nom du jeu
     * @param supportNom nom de la plateforme
     * @param quantite quantite de jetons
     */
    public void retirerJetonsAuMembreConnecte(String nomJeu, String supportNom, int quantite) {
        Joueur joueur = exigerJoueurConnecte();
        SupportJeu support = trouverSupport(nomJeu, supportNom);
        support.retirerJetons(joueur, quantite);
    }

    /**
     * Retourne les supports pour lesquels un test est demande.
     *
     * @return liste des supports a tester, tries par nombre de jetons
     */
    public List<SupportJeu> rechercherTestsARealiser() {
        List<SupportJeu> supports = new ArrayList<>();
        for (Jeu jeu : jeux.values()) {
            for (SupportJeu support : jeu.getListeSupports()) {
                if (!support.aUnTest() && support.getTotalJetons() > 0) {
                    supports.add(support);
                }
            }
        }
        supports.sort(Comparator
                .comparingInt(SupportJeu::getTotalJetons).reversed()
                .thenComparing(support -> support.getJeu().getNom(), String.CASE_INSENSITIVE_ORDER)
                .thenComparing(SupportJeu::getPlateforme, String.CASE_INSENSITIVE_ORDER));
        return supports;
    }

    /**
     * Retourne les supports que le testeur connecte peut effectivement tester.
     *
     * @return liste des supports realisables par le testeur connecte
     */
    public List<SupportJeu> rechercherTestsARealiserPourMembreConnecte() {
        Testeur testeur = exigerTesteurConnecte();
        List<SupportJeu> supports = new ArrayList<>();
        for (SupportJeu support : rechercherTestsARealiser()) {
            if (testeur.possedeJeu(support) && testeur.getTempsDeJeu(support) >= Jeu.DUREE_MINIMALE_TEST_HEURES) {
                supports.add(support);
            }
        }
        return supports;
    }

    /**
     * Cree et publie un test au nom du testeur connecte.
     *
     * @param nomJeu nom du jeu
     * @param supportNom nom de la plateforme
     * @param date date de publication
     * @param texte contenu textuel du test
     * @param version version testee
     * @param structure structure detaillee du test
     * @return test cree
     */
    public TestJeu ajouterTestAuMembreConnecte(
            String nomJeu,
            String supportNom,
            LocalDate date,
            String texte,
            String version,
            StructureTestJeu structure
    ) {
        Testeur testeur = exigerTesteurConnecte();
        SupportJeu support = trouverSupport(nomJeu, supportNom);
        TestJeu test = new TestJeu(testeur, support, date, texte, version, structure);
        support.getJeu().ajouterTest(test);
        return test;
    }

    @Override
    public String toString() {
        return "Plateforme{" +
                "membres=" + membres.size() +
                ", jeux=" + jeux.size() +
                ", membreConnecte=" + (connecte == null ? "invite" : connecte.getPseudo()) +
                '}';
    }

    private void remplacerMembre(Membre ancienMembre, Membre nouveauMembre) {
        String cle = normaliser(ancienMembre.getPseudo());
        membres.put(cle, nouveauMembre);
        if (connecte == ancienMembre) {
            connecte = nouveauMembre;
        }
    }

    private void transfererReferencesJoueur(Joueur ancienJoueur, Joueur nouveauJoueur) {
        for (Evaluation evaluation : ancienJoueur.getToutesLesEvaluations()) {
            evaluation.remplacerAuteur(nouveauJoueur);
        }
        for (Jeu jeu : jeux.values()) {
            for (SupportJeu support : jeu.getListeSupports()) {
                support.remplacerJoueur(ancienJoueur, nouveauJoueur);
            }
        }
    }

    private boolean supprimerMembreParCle(String cleNormalisee) {
        Membre membre = membres.remove(cleNormalisee);
        if (membre == null) {
            return false;
        }
        if (connecte == membre) {
            connecte = null;
        }
        return true;
    }

    private boolean correspond(Jeu jeu, CritereRechercheJeu critere) {
        if (!champCorrespond(jeu.getCategorie(), critere.categorie())) {
            return false;
        }
        if (!champCorrespond(jeu.getEditeur(), critere.editeur())) {
            return false;
        }
        if (!champCorrespond(jeu.getRating(), critere.rating())) {
            return false;
        }
        return !rechercherSupportsCorrespondants(jeu, critere).isEmpty();
    }

    private boolean supportCorrespond(SupportJeu support, CritereRechercheJeu critere, boolean texteLibreCorrespondAuJeu) {
        if (critere == null || critere.estVide()) {
            return true;
        }
        if (!champCorrespond(support.getPlateforme(), critere.plateforme())) {
            return false;
        }
        if (!champCorrespond(support.getDeveloppeur(), critere.developpeur())) {
            return false;
        }
        if (critere.testDisponible() != null && support.aUnTest() != critere.testDisponible()) {
            return false;
        }
        if (texteLibreCorrespondAuJeu) {
            return true;
        }
        return champCorrespond(support.getPlateforme(), critere.texteLibre())
                || champCorrespond(support.getDeveloppeur(), critere.texteLibre());
    }

    private boolean correspondTexteLibreJeu(Jeu jeu, String texte) {
        return champCorrespond(jeu.getNom(), texte)
                || champCorrespond(jeu.getCategorie(), texte)
                || champCorrespond(jeu.getEditeur(), texte)
                || champCorrespond(jeu.getRating(), texte);
    }

    private boolean champCorrespond(String valeur, String critere) {
        if (critere == null || critere.trim().isEmpty()) {
            return true;
        }
        return normaliser(valeur).contains(normaliser(critere));
    }

    private SupportJeu trouverSupport(String nomJeu, String supportNom) {
        Jeu jeu = consulterJeu(nomJeu);
        SupportJeu support = jeu.trouverSupport(supportNom);
        if (support == null) {
            throw new IllegalArgumentException("Support absent : " + supportNom + " pour " + nomJeu);
        }
        return support;
    }

    private Membre exigerMembreConnecte() {
        if (connecte == null) {
            throw new IllegalStateException("Action interdite selon le profil : aucun membre n'est connecte");
        }
        if (connecte.estBloque()) {
            throw new IllegalStateException("Action interdite selon le profil : le membre connecte est bloque");
        }
        return connecte;
    }

    private Joueur exigerJoueurConnecte() {
        Membre membre = exigerMembreConnecte();
        if (!(membre instanceof Joueur joueur)) {
            throw new IllegalStateException("Action interdite selon le profil : cette action est reservee a un joueur");
        }
        return joueur;
    }

    private Testeur exigerTesteurConnecte() {
        Membre membre = exigerMembreConnecte();
        if (!(membre instanceof Testeur testeur)) {
            throw new IllegalStateException("Action interdite selon le profil : cette action est reservee a un testeur");
        }
        return testeur;
    }

    private Administrateur exigerAdministrateurConnecte() {
        Membre membre = exigerMembreConnecte();
        if (!(membre instanceof Administrateur administrateur)) {
            throw new IllegalStateException("Action interdite selon le profil : cette action est reservee a un administrateur");
        }
        return administrateur;
    }

    private String formaterDecimal(double valeur) {
        return String.format(Locale.ROOT, "%.1f", valeur);
    }

    private String formater(double valeur) {
        return String.format(Locale.ROOT, "%.2f", valeur);
    }

    private String normaliser(String valeur) {
        if (valeur == null) {
            throw new IllegalArgumentException("La valeur ne peut pas etre null");
        }
        return valeur.trim().toLowerCase();
    }
}
