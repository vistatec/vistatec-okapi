package net.sf.okapi.filters.openoffice;


class ParametersBuilder {
    private Parameters params = new Parameters();

    Parameters build(){
        return params;
    }

    ParametersBuilder extractReferences(boolean param){
        this.params.setExtractReferences(param);
        return this;
    }

    ParametersBuilder extractMetadata(boolean param){
        this.params.setExtractMetadata(param);
        return this;
    }

    ParametersBuilder encodeCharacterEntityReferenceGlyphs(boolean param){
        this.params.setEncodeCharacterEntityReferenceGlyphs(param);
        return this;
    }
}
