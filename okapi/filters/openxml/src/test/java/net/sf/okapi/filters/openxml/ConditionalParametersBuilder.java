package net.sf.okapi.filters.openxml;


class ConditionalParametersBuilder {
    private ConditionalParameters params;

    ConditionalParameters build() {
        return params;
    }

    ConditionalParametersBuilder() {
        params = new ConditionalParameters();
    }

    ConditionalParametersBuilder replaceNoBreakHyphenTag(boolean param) {
        this.params.setReplaceNoBreakHyphenTag(param);
        return this;
    }

    ConditionalParametersBuilder ignoreSoftHyphenTag(boolean param) {
        this.params.setIgnoreSoftHyphenTag(param);
        return this;
    }

    ConditionalParametersBuilder addLineSeparatorCharacter(boolean param) {
        this.params.setAddLineSeparatorCharacter(param);
        return this;
    }

    ConditionalParametersBuilder cleanupAggressively(boolean param) {
        this.params.setCleanupAggressively(param);
        return this;
    }

    ConditionalParametersBuilder addTabAsCharacter(boolean param) {
        this.params.setAddTabAsCharacter(param);
        return this;
    }

    ConditionalParametersBuilder lineSeparatorAsChar(boolean param) {
        this.params.setAddLineSeparatorCharacter(param);
        return this;
    }

    ConditionalParametersBuilder lineSeparatorReplacement(char param) {
        this.params.setLineSeparatorReplacement(param);
        return this;
    }

    ConditionalParametersBuilder automaticallyAcceptRevisions(boolean param) {
        this.params.setAutomaticallyAcceptRevisions(param);
        return this;
    }

    ConditionalParametersBuilder translateWordHidden(boolean param) {
        this.params.setTranslateWordHidden(param);
        return this;
    }

    ConditionalParametersBuilder translateDocProperties(boolean param) {
        this.params.setTranslateDocProperties(param);
        return this;
    }
}
