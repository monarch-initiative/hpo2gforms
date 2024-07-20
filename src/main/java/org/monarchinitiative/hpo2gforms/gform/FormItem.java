package org.monarchinitiative.hpo2gforms.gform;

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
        List<Term> parents
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

    private String getHeader() {
        String msg = String.format("The following questions refer to %s (%s). Indicate whether you approve of the term in principle. If you want to propose changes or additions enter the new text in the text boxes and otherwise leave them blank."
                , TextBolder.encodeBold(term.getName()), TextBolder.encodeBold(term.id().getValue()));
        return String.format("form.addSectionHeaderItem().setTitle(\"%s\")\n", msg);
    }

    private String getBoldedTerm() {
        return String.format("%s (%s)", TextBolder.encodeBold(label()), term.id().getValue());
    }

    private String getInPrinciple() {
        return String.format("""
                form.addMultipleChoiceItem()
                .setChoiceValues(['Accept','Reject', 'Accept with revisions'])
                .setTitle("%s");
                """, getBoldedTerm());
    }


    private String getDefinitionQuestion() {
        String d = term.getDefinition().length() > 2 ? term.getDefinition() : "None found";
        return String.format("""
                form.addTextItem().setHelpText("add revised definition or leave blank")
                .setTitle("%s (definition): %s");
                """, getBoldedTerm(), d);
    }

    private String getPmidQuestion() {
        String value;
        if (!pmids().isEmpty() && pmids.contains("PMID") ) {
            value = pmids();
        } else {
            value = "None found";
        }
        return String.format("""
                 form.addTextItem().setHelpText("add PMID or leave blank")
                .setTitle("%s (PMIDs): %s");
                """, getBoldedTerm(),value);
    }

    private String getCommentQuestion() {
        String d = term.getComment().length() > 2 ? term.getComment() : "None found";
        return String.format("""
                 form.addTextItem().setHelpText("add revised comment or leave blank")
                .setTitle("%s (Comment): %s");
                """, getBoldedTerm(), d);
    }

    private String getSynonymsQuestion() {
        String value = synonyms().length() > 2 ? synonyms() : "None found";
        return String.format("""
                 form.addTextItem().setHelpText("add synonyms or leave blank")
                .setTitle("%s (Synonyms): %s");
                """, getBoldedTerm(), value);
    }

    private String getParentsQuestion() {
        List<String> parentString = new ArrayList<>();
        if (parents.isEmpty()) {
            return "please report error"; // we should never actually be processing the root, so this should never happen
        }
        for (Term term: parents) {
            String urlString = String.format("https://hpo.jax.org/browse/term/%s", term.id().getValue());
            String label = String.format("%s (%s)", term.getName(), urlString);
            parentString.add(label);
        }
        String value = String.join(" and ", parentString);
        return String.format("""
                 form.addTextItem().setHelpText("Check whether parent(s) are appropriate")
                .setTitle("%s (Parents) %s);");
                """, getBoldedTerm(), value);
    }




    public String getQuestionnaireItem() {
        return getHeader() +
                getInPrinciple() +
                getDefinitionQuestion() +
                getCommentQuestion() +
                getPmidQuestion() +
                getParentsQuestion() +
                getSynonymsQuestion();
    }


    private static String escape(String value) {
        return value.replaceAll("\"", "")
                .replaceAll("'", "");
    }
    /**
     *
     * @param term an HPO term
     * @param hpoOntology Reference to HPO object
     * @return A string for display on a Google form that represents the parents of the current term
     */
    private static List<Term> getParents(Term term, Ontology hpoOntology) {
        List<Term> termList = new ArrayList<>();
        for (TermId tid: hpoOntology.graph().getParents(term.id())) {
            Optional<Term> opt = hpoOntology.termForTermId(tid);
            opt.ifPresent( termList::add);
        }
        return termList;
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
            String comment = escape(term.getComment());
            String synonyms = getSynonymString(term);
            List<Term> parents = getParents(term, hpoOntology);
            return new FormItem(term, label, pmids, comment, synonyms, parents);
        } else {
            // should never happen
            throw new PhenolRuntimeException("Could not find term for id " + tid.toString());
        }
    }
}
