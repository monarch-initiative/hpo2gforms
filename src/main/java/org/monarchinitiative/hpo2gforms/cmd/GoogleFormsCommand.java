package org.monarchinitiative.hpo2gforms.cmd;

import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;


/**
 * The logic of this class is that we want to create Java-script like Google Apps code
 * to create a questionnaire for workshop participants to vote on existing HPO term names
 * and definitions. We start with a given HPO id and the code will create a questionnaire
 * for that term and all of its descendants.
 */
@CommandLine.Command(name = "forms",
        mixinStandardHelpOptions = true,
        description = "Create Google Forms Code")
public class GoogleFormsCommand extends HPOCommand implements Callable<Integer> {
    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleFormsCommand.class);

    @CommandLine.Option(names={"-t","--target"}, required = true, description = "HPO term id (number only)")
    private String targetHpoId;

    @CommandLine.Option(names={"-o","--outfile"}, description = "outfile name (default: script.txt)")
    private String outFileName = "script.txt";


    @Override
    public Integer call() throws Exception {
        // check that target id is valid
        Ontology hpoOntology = OntologyLoader.loadOntology(new File(hpopath));
        Optional<TermId> opt = getTargetId(hpoOntology);
        if (opt.isEmpty()) {
            return 1;
        }
        List<FormItem> formItemList = new ArrayList<>();
        TermId targetId = opt.get();
        FormItem fitem = FormItem.fromTerm(targetId, hpoOntology);
        formItemList.add(fitem);
        for (TermId tid: hpoOntology.graph().getDescendants(targetId)) {
            fitem = FormItem.fromTerm(targetId, hpoOntology);
            formItemList.add(fitem);
        }



        return 0;
    }


    private Optional<TermId> getTargetId(Ontology hpoOntology) {
        if ( targetHpoId.length() != 7) {
            System.err.printf("[ERROR] target ID must be a seven digit number that corresponds to an HPO id\n");
            System.err.printf("[ERROR] for insance, 0002021, which corresponds to Pyloric stenosis HP:0002021\n");
            return Optional.empty();
        }
        TermId hpoId;
        try {
            int idnumb = Integer.parseInt(targetHpoId);
            hpoId = TermId.of("HP", targetHpoId);
            return Optional.of(hpoId);
        } catch (NumberFormatException e) {
            System.err.printf("[ERROR] target ID must be a seven digit number that corresponds to an HPO id\n");
            System.err.printf("[ERROR] for insance, 0002021, which corresponds to Pyloric stenosis HP:0002021\n");
            return  Optional.empty();
        }
    }


    static class FormItem {

        private final Term term;
        private final String label;
        private final String pmids ;
        private final  String comment ;
        private final String synonyms;
        private final   String parents;

        FormItem(Term term, String label, String pmids, String comment, String synonyms, String parents) {
            this.term = term;
            this.label = label;
            this.pmids = pmids;
            this.comment = comment;
            this.synonyms = synonyms;
            this.parents = parents;
        }


        private static String getSynonymString(Term term) {
            StringBuilder sb = new StringBuilder();
            for (TermSynonym tsyn: term.getSynonyms()) {
                String label = tsyn.getValue();
                String scope = tsyn.getScope().toString();
                String stype = tsyn.getSynonymTypeName();
                String s = String.format("%s [%s;%s]", label, scope, stype);
                sb.append(s);
            }
            return sb.toString();
        }

        private static String getParents(Term term, Ontology hpoOntology) {
            List<String> termList = new ArrayList<>();
            for (TermId tid: hpoOntology.graph().getParents(term.id())) {
                Optional<Term> opt = hpoOntology.termForTermId(tid);
                if (opt.isPresent()) {
                    termList.add(String.format("%s [%s]", opt.get().getName(), tid.getValue()));
                }
            }
            return String.join("; ", termList);
        }

        private static String getPmids(Term term) {
            List<String> pmidList = term.getPmidXrefs().stream().
                    filter(SimpleXref::isPmid).
                    map(SimpleXref::getId).toList();
            if (pmidList.isEmpty()) {
                return "No PMIDs found";
            } else {
                return String.join("; ", pmidList);
            }
        }

        public static FormItem fromTerm(TermId tid, Ontology hpoOntology) {
            Optional<Term> opt = hpoOntology.termForTermId(tid);
            if (opt.isPresent()) {
                Term term = opt.get();
                String label = term.getName();
                String pmids = FormItem.getPmids(term);
                String comment = term.getComment();
                String synonyms = FormItem.getSynonymString(term);
                String parents = getParents(term, hpoOntology);
                return new FormItem(term, label, pmids, comment, synonyms, parents);
            } else {
                // should never happen
                throw new PhenolRuntimeException("Could not find term for id " + tid.toString());
            }

        }
    }
}
