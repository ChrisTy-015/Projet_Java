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

public class Plateforme {
    private final Map<String, Membre> membres;
    private final Map<String, Jeu> jeux;
    private Membre connecte;

    public Plateforme() {
        this.membres = new LinkedHashMap<>();
        this.jeux = new LinkedHashMap<>();
        this.connecte = null;
        ajouterMembre(new Administrateur("admin"));
    }

    public void ajouterMembre(Membre membre) {
        String cle = normaliser(membre.getPseudo());
        if (membres.containsKey(cle)) {
            throw new IllegalArgumentException("Pseudo deja utilise : " + membre.getPseudo());
        }
        membres.put(cle, membre);
    }

    public Joueur inscrireJoueur(String pseudo) {
        if (!estPseudoDisponible(pseudo)) {
            throw new IllegalArgumentException("Pseudo deja utilise : " + pseudo);
        }
        Joueur membre = new Joueur(pseudo);
        ajouterMembre(membre);
        return membre;
    }

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

    public Testeur inscrireTesteur(String pseudo) {
        if (!estPseudoDisponible(pseudo)) {
            throw new IllegalArgumentException("Pseudo deja utilise : " + pseudo);
        }
        Testeur membre = new Testeur(pseudo);
        ajouterMembre(membre);
        return membre;
    }

    public Administrateur inscrireAdministrateur(String pseudo) {
        if (!estPseudoDisponible(pseudo)) {
            throw new IllegalArgumentException("Pseudo deja utilise : " + pseudo);
        }
        Administrateur membre = new Administrateur(pseudo);
        ajouterMembre(membre);
        return membre;
    }

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

    public void deconnecter() {
        connecte = null;
    }

    public Membre getMembreConnecte() {
        return connecte;
    }

    public Membre trouverMembre(String pseudo) {
        return membres.get(normaliser(pseudo));
    }

    public boolean estPseudoDisponible(String pseudo) {
        return !membres.containsKey(normaliser(pseudo));
    }

    public boolean desinscrireJoueur(String pseudo) {
        Membre membre = trouverMembre(pseudo);
        if (!(membre instanceof Joueur) || membre instanceof Testeur) {
            return false;
        }
        return supprimerMembreParCle(normaliser(pseudo));
    }

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

    public boolean supprimerMembre(String pseudo) {
        return supprimerMembreParCle(normaliser(pseudo));
    }

    public Collection<Membre> getMembres() {
        return Collections.unmodifiableCollection(membres.values());
    }

    public void ajouterJeu(Jeu jeu) {
        String cle = normaliser(jeu.getNom());
        if (jeux.containsKey(cle)) {
            throw new IllegalArgumentException("Jeu deja present : " + jeu.getNom());
        }
        jeux.put(cle, jeu);
    }

    public Jeu trouverJeu(String nom) {
        return jeux.get(normaliser(nom));
    }

    public List<Jeu> chercherJeux(String critere) {
        return chercherJeux(new CritereRechercheJeu(critere, null, null, null, null, null, null));
    }

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

    public Collection<Jeu> getJeux() {
        return Collections.unmodifiableCollection(jeux.values());
    }

    public List<String> getCategoriesDisponibles() {
        TreeSet<String> valeurs = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (Jeu jeu : jeux.values()) {
            if (!jeu.getCategorie().isBlank()) {
                valeurs.add(jeu.getCategorie());
            }
        }
        return List.copyOf(valeurs);
    }

    public List<String> getEditeursDisponibles() {
        TreeSet<String> valeurs = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (Jeu jeu : jeux.values()) {
            if (!jeu.getEditeur().isBlank()) {
                valeurs.add(jeu.getEditeur());
            }
        }
        return List.copyOf(valeurs);
    }

    public List<String> getRatingsDisponibles() {
        TreeSet<String> valeurs = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (Jeu jeu : jeux.values()) {
            if (!jeu.getRating().isBlank()) {
                valeurs.add(jeu.getRating());
            }
        }
        return List.copyOf(valeurs);
    }

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

    public List<SupportJeu> rechercherSupportsParJetons() {
        List<SupportJeu> supports = new ArrayList<>();
        for (Jeu jeu : jeux.values()) {
            supports.addAll(jeu.getListeSupports());
        }
        supports.sort((a, b) -> Integer.compare(b.getTotalJetons(), a.getTotalJetons()));
        return supports;
    }

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

    public Membre consulterMembre(String pseudo) {
        Membre membre = trouverMembre(pseudo);
        if (membre == null) {
            throw new IllegalArgumentException("Pseudo inexistant : " + pseudo);
        }
        return membre;
    }

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

    public Jeu consulterJeu(String nomJeu) {
        Jeu jeu = trouverJeu(nomJeu);
        if (jeu == null) {
            throw new IllegalArgumentException("Jeu introuvable : " + nomJeu);
        }
        return jeu;
    }

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

    public SupportJeu ajouterJeuPossedeAuMembreConnecte(String nomJeu, String supportNom) {
        Joueur joueur = exigerJoueurConnecte();
        SupportJeu support = trouverSupport(nomJeu, supportNom);
        joueur.ajouterJeu(support);
        return support;
    }

    public int ajouterTempsDeJeuAuMembreConnecte(String nomJeu, String supportNom, int heures) {
        Joueur joueur = exigerJoueurConnecte();
        SupportJeu support = trouverSupport(nomJeu, supportNom);
        joueur.ajouterTempsJeu(support, heures);
        return joueur.getTempsDeJeu(support);
    }

    public List<Evaluation> lireEvaluations(String nomJeu, String supportNom) {
        SupportJeu support = trouverSupport(nomJeu, supportNom);
        List<Evaluation> evaluations = new ArrayList<>(support.getEvaluations());
        evaluations.sort(Comparator
                .comparingDouble(Evaluation::getNoteGlobale).reversed()
                .thenComparing(Evaluation::getDate));
        return evaluations;
    }

    public TestJeu lireTestAuMembreConnecte(String nomJeu, String supportNom) {
        return lireTest(nomJeu, supportNom);
    }

    public TestJeu lireTest(String nomJeu, String supportNom) {
        exigerJoueurConnecte();
        SupportJeu support = trouverSupport(nomJeu, supportNom);
        if (!support.aUnTest()) {
            throw new IllegalStateException("Aucun test n'est disponible pour ce support");
        }
        return support.getTest();
    }

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

    public void signalerEvaluationAuMembreConnecte(Evaluation evaluation) {
        Testeur testeur = exigerTesteurConnecte();
        testeur.signalerEvaluation(evaluation);
    }

    public void supprimerEvaluationAuMembreConnecte(Evaluation evaluation) {
        Administrateur administrateur = exigerAdministrateurConnecte();
        administrateur.supprimerEvaluation(evaluation);
    }

    public void placerJetonsAuMembreConnecte(String nomJeu, String supportNom, int quantite) {
        Joueur joueur = exigerJoueurConnecte();
        SupportJeu support = trouverSupport(nomJeu, supportNom);
        support.placerJetons(joueur, quantite);
    }

    public void retirerJetonsAuMembreConnecte(String nomJeu, String supportNom, int quantite) {
        Joueur joueur = exigerJoueurConnecte();
        SupportJeu support = trouverSupport(nomJeu, supportNom);
        support.retirerJetons(joueur, quantite);
    }

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
