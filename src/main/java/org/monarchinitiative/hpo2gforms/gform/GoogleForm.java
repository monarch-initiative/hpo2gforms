package org.monarchinitiative.hpo2gforms.gform;

import java.util.ArrayList;
import java.util.List;

public class GoogleForm {


    private final List<String> lines;

    private final static String DESCRIPTION = "Use this form to enter your opinion about HPO terms, definitions, comments, PMIDs, and synonyms";

    public GoogleForm(String title) {
        lines = new ArrayList<>();
        lines.add("function hpo_questionnaire() {");
        addWithIndent(String.format("var form = FormApp.create('%s');", title));
        addWithIndent(String.format(" form.setDescription(\"DESCRIPTION\");"));
      //  function myfxn () {

    }

    private void addWithIndent(String line) {
        lines.add("\t" + line);
    }






}
