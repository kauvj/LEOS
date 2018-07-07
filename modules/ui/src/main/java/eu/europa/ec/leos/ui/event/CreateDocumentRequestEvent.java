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
package eu.europa.ec.leos.ui.event;

import eu.europa.ec.leos.domain.document.LeosCategory;

public class CreateDocumentRequestEvent {

    private LeosCategory category;
    private String[] templates;
    private String purpose;

    public CreateDocumentRequestEvent(LeosCategory category, String[] templates, String purpose) {
        this.category = category;
        this.templates = templates;
        this.purpose = purpose;
    }

    public LeosCategory getCategory() {
        return category;
    }

    public String[] getTemplates() {
        return templates;
    }

    public String getPurpose() {
        return purpose;
    }
}
