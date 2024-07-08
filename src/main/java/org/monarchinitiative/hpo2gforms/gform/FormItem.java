package org.monarchinitiative.hpo2gforms.gform;

import org.monarchinitiative.hpo2gforms.cmd.GoogleFormsCommand;
import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenol.ontology.data.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record FormItem(
        Term term,
        String label,
        String pmids,
        String comment,
        String synonyms,
        String parents
) {


    /**
     * @param term an HPO term
     * @return a String for display on the Google forms that represents all of the synonyms of a term.
     */
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


    private String getDefinitionQuestion() {
        String d = term.getDefinition().length() > 2 ? term.getDefinition() : "None found";
        return String.format("""
                form.addMultipleChoiceItem()
                .setChoiceValues(['Approve','Disapprove of term', 'Needs work'])
                .setTitle("%s (definition): %s");
                """, term.id().getValue(), d);
    }

    private String getPmidQuestion() {
        String value;
        if (pmids().length() > 0 && pmids.contains("PMID") ) {
            value = pmids();
        } else {
            value = "None found";
        }
        return String.format("""
                form.addMultipleChoiceItem()
                .setChoiceValues(['Approve','Disapprove', 'Needs additional PMID'])
                .setTitle("%s (PMIDs): %s");
                """, term.id().getValue(),value);
    }

    private String getCommentQuestion() {
        String d = term.getComment().length() > 2 ? term.getComment() : "None found";
        return String.format("""
                form.addMultipleChoiceItem()
                .setChoiceValues(['Approve','Disapprove of comment', 'Needs work'])
                .setTitle("%s (Comment): %s");
                """, term.id().getValue(), d);
    }

    private String getSynonymsQuestion() {
        String value = synonyms().length() > 2 ? synonyms() : "None found";
        return String.format("""
                form.addMultipleChoiceItem()
                .setChoiceValues(['Approve','Disapprove of comment', 'Needs work'])
                .setTitle("%s (Synonyms): %s");
                """, term.id().getValue(), value);
    }




    public String getQuestionnaireItem() {
        StringBuilder sb = new StringBuilder();
        sb.append(getDefinitionQuestion());
        sb.append(getPmidQuestion());
        sb.append(getCommentQuestion());
        sb.append(getSynonymsQuestion());
        return sb.toString();
    }

    /**
     *
     * @param term an HPO term
     * @param hpoOntology Reference to HPO object
     * @return A string for display on a Google form that represents the parents of the current term
     */
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

    /**
     * @param term An HPO term
     * @return A string for disaply on Google forms representing all of the citations of the current term
     */
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

    /**
     * @param tid TermId of an HPO term that we want to display on a Google for
     * @param hpoOntology reference to HPO ontology
     * @return Object that can generate an item for a Google form
     */
    public static FormItem fromTerm(TermId tid, Ontology hpoOntology) {
        Optional<Term> opt = hpoOntology.termForTermId(tid);
        if (opt.isPresent()) {
            Term term = opt.get();
            String label = term.getName();
            String pmids = getPmids(term);
            String comment = term.getComment();
            String synonyms = getSynonymString(term);
            String parents = getParents(term, hpoOntology);
            return new FormItem(term, label, pmids, comment, synonyms, parents);
        } else {
            // should never happen
            throw new PhenolRuntimeException("Could not find term for id " + tid.toString());
        }
    }
}
