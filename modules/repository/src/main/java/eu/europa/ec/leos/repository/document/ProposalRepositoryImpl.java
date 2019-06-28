/*
 * Copyright 2019 European Commission
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
package eu.europa.ec.leos.repository.document;

import eu.europa.ec.leos.domain.cmis.LeosCategory;
import eu.europa.ec.leos.domain.cmis.document.Proposal;
import eu.europa.ec.leos.domain.cmis.metadata.ProposalMetadata;
import eu.europa.ec.leos.repository.LeosRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Proposal Repository implementation.
 *
 * @constructor Creates a specific Proposal Repository, injected with a generic LEOS Repository.
 */
@Repository
public class ProposalRepositoryImpl implements ProposalRepository {

    private static final Logger logger = LoggerFactory.getLogger(ProposalRepositoryImpl.class);

    private final LeosRepository leosRepository;

    @Autowired
    public ProposalRepositoryImpl(LeosRepository leosRepository) {
        this.leosRepository = leosRepository;
    }

    @Override
    public Proposal createProposal(String templateId, String path, String name, ProposalMetadata metadata) {
        logger.debug("Creating Proposal... [template=" + templateId + ", path=" + path + ", name=" + name + "]");
        Proposal proposal = leosRepository.createDocument(templateId, path, name, metadata, Proposal.class);
        return leosRepository.updateInitialCreationProperties(proposal.getId(), proposal.getCreatedBy(), proposal.getCreationInstant(), Proposal.class);
    }

    @Override
    public Proposal createProposalFromContent(String path, String name, ProposalMetadata metadata, byte[] contentBytes) {
        logger.debug("Creating Proposal With Content... [path=" + path + ", name=" + name + "]");
        Proposal proposal = leosRepository.createDocumentFromContent(path, name, metadata, Proposal.class, LeosCategory.PROPOSAL.name(), contentBytes);
        return leosRepository.updateInitialCreationProperties(proposal.getId(), proposal.getCreatedBy(), proposal.getCreationInstant(), Proposal.class);
    }

    @Override
    public Proposal updateProposal(String id, ProposalMetadata metadata) {
        logger.debug("Updating Proposal metadata... [id=" + id + "]");
        return leosRepository.updateDocument(id, metadata, Proposal.class);
    }

    @Override
    public Proposal updateProposal(String id, List<String> milestoneComments, byte[] content, boolean major, String comment) {
        logger.debug("Updating Proposal metadata... [id=" + id + "]");
        return leosRepository.updateMilestoneComments(id, content, milestoneComments, major, comment, Proposal.class);
    }

    @Override
    public Proposal updateMilestoneComments(String id, List<String> milestoneComments) {
        logger.debug("Updating Proposal metadata... [id=" + id + "]");
        return leosRepository.updateMilestoneComments(id, milestoneComments, Proposal.class);
    }

    @Override
    public Proposal updateProposal(String id, byte[] content) {
        logger.debug("Updating Proposal content... [id=" + id + "]");
        return leosRepository.updateDocument(id, content, false, "Content updated.", Proposal.class);
    }

    @Override
    public Proposal updateProposal(String id, ProposalMetadata metadata, byte[] content, boolean major, String comment) {
        logger.debug("Updating Proposal metadata and content... [id=" + id + ", major=" + major + "]");
        return leosRepository.updateDocument(id, metadata, content, major, comment, Proposal.class);
    }

    @Override
    public Proposal findProposalById(String id, boolean latest) {
        logger.debug("Finding Proposal by ID... [id=" + id + ", latest=" + latest + "]");
        return leosRepository.findDocumentById(id, Proposal.class, latest);
    }
}
