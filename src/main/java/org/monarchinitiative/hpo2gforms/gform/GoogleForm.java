package org.monarchinitiative.hpo2gforms.gform;

import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.Term;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GoogleForm {


    private final List<String> lines;

    private final static String DESCRIPTION = "Use this form to enter your opinion about HPO terms, definitions, comments, PMIDs, and synonyms";

    private final Ontology ontology;

    private final TermId targetId;

    private final int questionnairePart;



    public GoogleForm(List<Term> termList, Ontology hpoOntolgy, TermId targetId, int part) {
        List<FormItem> formItemList = new ArrayList<>();
        ontology = hpoOntolgy;
        this.targetId = targetId;
        questionnairePart = part;
        for (Term term: termList) {
            FormItem fitem = FormItem.fromTerm(term, this.ontology);
            formItemList.add(fitem);
        }
        lines = new ArrayList<>();
        lines.add("function hpo_questionnaire() {");
        addWithIndent(String.format("var form = FormApp.create('%s');", getQuestiionnaireTitle()));
        addWithIndent(String.format(" form.setDescription(\"%s\");", DESCRIPTION));
        //  function myfxn () {
        for (FormItem fitem: formItemList) {
            lines.add(fitem.getQuestionnaireItem());
        }
        lines.add("}");
    }

    public String getFunction() {
        return String.join("\n", lines);
    }


    private String getQuestiionnaireTitle() {
        Optional<Term> opt = ontology.termForTermId(targetId);
        if (opt.isPresent()) {
            Term term = opt.get();
            String label = term.getName();
            return String.format("%s (%s) part %d", label, targetId.getValue(), questionnairePart);
        } else {
            throw new PhenolRuntimeException("Could not find term for target " + targetId);
        }
    }

    private void addWithIndent(String line) {
        lines.add("\t" + line);
    }






}
