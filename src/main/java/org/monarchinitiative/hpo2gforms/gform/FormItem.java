package org.monarchinitiative.hpo2gforms.gform;

import org.monarchinitiative.phenol.ontology.data.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record FormItem(
        Term term,
        String label,
        String definition,
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


    private String getTermSummary() {
        String header = String.format("%s: %s (%s).",
                TextBolder.encodeBold("Term label (id)"),
                TextBolder.encodeBold(term.getName()), TextBolder.encodeBold(term.id().getValue()));
        String value = synonyms().length() > 1 ? synonyms() : "None found";
        String synonyms = String.format("%s: %s", TextBolder.encodeBold("Synonyms"), value);
        String parents = String.format("%s: %s", TextBolder.encodeBold("Parents"), getParentsString());
        String definition = String.format("%s: %s", TextBolder.encodeBold("Definition"), definition());
        String comment = String.format("%s: %s", TextBolder.encodeBold("Comment"), this.term.getComment());
        value =!pmids().isEmpty() && pmids.contains("PMID")  ? pmids() : "None found";
        String pmid = String.format("%s: %s", TextBolder.encodeBold("PMID"), value);
        String instruction = String.format("%s:",TextBolder.encodeBold("Add comments here (leave blank unless you have suggested revision)"));
        List<String> items = List.of(synonyms, parents, definition, comment, pmid, instruction);

        return String.format("""
                 var paragraphTextItem = form.addParagraphTextItem();
                 paragraphTextItem.setTitle('%s')
                 paragraphTextItem.setHelpText('%s');
                 """,
                header,
                String.join("\\n\\n", items)
                );
    }


    /**
     * This function creates a grid of radio buttons for the user to
     * rate the components of a term
      * @return a String with code for a Google App Script gridItem
     */
    private String getGrid() {
        return String.format("""
        var gridItem = form.addGridItem();
        gridItem.setTitle('[%s]')
                .setRows(['Term label', 'Synonyms',  'Parents(s)', 'Definition', 'Comment', 'PMIDs'])
                .setColumns(['Accept','Reject', 'Revise', 'n/a']);
        """, term.id().getValue());
    }







    private String getHeader() {
        String msg = String.format("%s (%s). The following subsections allow you to make suggestions for each part of the term, and the final question will ask whether you approve of the term overall."
                , TextBolder.encodeBold(term.getName()), TextBolder.encodeBold(term.id().getValue()));
        return String.format("form.addSectionHeaderItem().setTitle(\"%s\")\n", msg);
    }

    private String getBoldedTerm() {
        return String.format("%s (%s)", TextBolder.encodeBold(label()), term.id().getValue());
    }


    private String getParentsString() {
        List<String> parentString = new ArrayList<>();
        if (parents.isEmpty()) {
            return "please report error"; // we should never actually be processing the root, so this should never happen
        }
        for (Term term: parents) {
            String urlString = String.format("https://hpo.jax.org/browse/term/%s", term.id().getValue());
            String label = String.format("%s (%s)", term.getName(), urlString);
            parentString.add(label);
        }
       return String.join(" and ", parentString);
    }



    public String getQuestionnaireItem() {
        return  getTermSummary() + getGrid();
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
     * @param term an HPO term that we want to display on a Google for
     * @param hpoOntology reference to HPO ontology
     * @return Object that can generate an item for a Google form
     */
    public static FormItem fromTerm(Term term, Ontology hpoOntology) {
            String label = term.getName();
            String definition = escape(term.getDefinition());
            String pmids = getPmids(term);
            String comment = escape(term.getComment());
            String synonyms = getSynonymString(term);
            List<Term> parents = getParents(term, hpoOntology);
            return new FormItem(term, label, definition, pmids, comment, synonyms, parents);

    }
}
