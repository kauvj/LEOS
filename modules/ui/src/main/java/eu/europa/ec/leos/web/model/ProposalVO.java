/*
 * Copyright 2017 European Commission
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *     https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */
package eu.europa.ec.leos.web.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/* View Object*/
public class ProposalVO {
    private MetadataVO metaData;
    private DocumentVO explanatoryMemorandum;
    private DocumentVO legalText;
    private List<CollaboratorVO> collaborators = new ArrayList();

    public MetadataVO getMetaData() {
        return metaData;
    }

    public void setMetaData(MetadataVO metaData) {
        this.metaData = metaData;
    }

    public DocumentVO getExplanatoryMemorandum() {
        return explanatoryMemorandum;
    }

    public void setExplanatoryMemorandum(DocumentVO explanatoryMemorandum) {
        this.explanatoryMemorandum = explanatoryMemorandum;
    }

    public DocumentVO getLegalText() {
        return legalText;
    }

    public void setLegalText(DocumentVO legalText) {
        this.legalText = legalText;
    }

    public void addCollaborators(List<CollaboratorVO> collaborators) {
        this.collaborators.addAll(collaborators);
    }

    public List<CollaboratorVO> getCollaborators() {
        return Collections.unmodifiableList(collaborators);
    }
}