package org.monarchinitiative.hpo2gforms.cmd;

import org.monarchinitiative.hpo2gforms.gform.FormItem;
import org.monarchinitiative.hpo2gforms.gform.GoogleForm;
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
        GoogleForm gform = new GoogleForm(hpoOntology, opt.get());
        String fxn = gform.getFunction();
        System.out.println(fxn); // todo output to file



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


}
