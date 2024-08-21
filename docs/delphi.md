# Delphi

A Delphi process

The Delphi method is a structured approach  for collecting experts' opinions on a specific issue through a series of questionnaires 
(see [Nasa et al., 2021](https://pubmed.ncbi.nlm.nih.gov/34322364/) for an introduction). The Delphi method is frequently
used to help reach expert consensus in many healthcare and medical domains.

This document summarizes the approach the HPO team is taking to adapt the Delphi method to ontology development and
explain how the Java application in this repository (hpo2gforms) is used to support this.

## Conceptualization

We suggest that groups interested in working with the HPO team to extend 
or refine HPO terms in a specific area contact us to define the scope of the 
project. We have conducted numerous workshops witbh clinical groups 
since 2009, and have found that it is most effective to schedule a certain
number of virtual (online) meetings to begin the work. 

## Content Validation

Following the online meetings, an onsite
meeting with typically between 5 and 20 participants over one to several days
is conducted to systematically review existing terms and the structure of the
relevant parts of the ontology. Definitions, comments, synonyms, and references
to the literature (PubMed identifiers, i.e., PMIDs) are proposed and discussed.

## Modified Delphi
All participants from the workshops as well as other interested individuals
are invited to participate in the modifier Delphi process. 
We call our process “modified” because the domain experts from medical 
domains rarely if ever have training in ontology engineering. Although the
HPO team provides training in this area during the meetings, it requires
years to become proficient in the area. Therefore, the HPO team will adjust
the terms and structure as required by the rules of ontology engineering.

We then use the hpo2gforms app to create Google questionnaires for the
terms that were worked on during the workshops. Participants are asked
to assess the terms and their components using a standardized form for
each term.

Typically, participants are requested to complete the questionnaires within
a period of several weeks. Responses are evaluted as described [here](evaluation.md).

Each term is then evaluated. If a participant has entered ``Accept``, ``n/a``,
or no answer for each component of a term and has not entered a text comment
requesting revision, then the response is graded as ``Term Accept``.
for each component

We defined consensus agreement for a term if at least 70% of participants scored the term as “agree” for all components and fewer than 15% of participants scored any component of the term as “disagree” (Figure ??? shows an example set of questions as they appeared in Google form).



For the first phase of the Delphi process, participants were given three weeks to complete the survey.  Participants were sent a reminder email if they had not completed the phase with one week remaining.  Participants who did not complete a phase by the deadline were deemed to have withdrawn from the study and were not invited to take part in subsequent phases.





The content was developed and refined within a series of on-site and virtual workshops. The onsite workshops were held at
